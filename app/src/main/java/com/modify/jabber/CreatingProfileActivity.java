package com.modify.jabber;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.modify.jabber.model.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatingProfileActivity extends AppCompatActivity {
    ImageButton photo;
    Button finish;
    EditText bio;
    CircleImageView circleImageView;
    DatabaseReference databaseReference;
    HashMap<String, Object> hashMap;
    private static final int IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    FirebaseUser fuser;
    User userDelete;
    StorageReference storageReference;
    String mUri,mCurrentPhotoPath;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creating_profile);
        photo = findViewById(R.id.InsertProfileButton);
        finish = findViewById(R.id.FinishSignUp);
        bio = findViewById(R.id.Bio);
        circleImageView = findViewById(R.id.CreateProfileImage);

        hashMap = new HashMap<>();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(CreatingProfileActivity.this);
                builder.setTitle("Select An Image");
                builder.setIcon(R.mipmap.ic_launcher_symbol);
                builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dispatchTakePictureIntent();
                    }
                });
                builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openImage();
                    }
                });
                builder.show();
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((mUri == null && userDelete.getImageURL().equals("default"))|| bio.getText().toString().equals("")) {
                    if (bio.getText().toString().equals("")) {
                        Toast.makeText(CreatingProfileActivity.this, "Please fill out your bio.", Toast.LENGTH_SHORT).show();
                    }
                    if (mUri == null) {
                        Toast.makeText(CreatingProfileActivity.this, "Please upload a profile picture.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    hashMap.put("bio",bio.getText().toString());
                    databaseReference.updateChildren(hashMap);
                    Intent start = new Intent(CreatingProfileActivity.this, MainActivity.class);
                    start.putExtra("viewProfile",2);
                    startActivity(start);
                }
            }
        });
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userDelete = dataSnapshot.getValue(User.class);
                    if (user.getImageURL().equals("default"))
                    {
                        Glide.with(getApplicationContext()).load(R.mipmap.ic_launcher_color).circleCrop().into(circleImageView);
                    } else {
                        if(getApplicationContext() != null) {
                            Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(circleImageView);
                        }
                        if(!user.getBio().equals("default bio"))
                        {
                            bio.setText(user.getBio());
                            finish.setText("Update");
                        }
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = CreatingProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() throws IOException {

        final ProgressDialog pd = new ProgressDialog(CreatingProfileActivity.this);
        pd.setTitle("Uploading");
        pd.setIcon(R.mipmap.ic_launcher_symbol);
        pd.show();

        if (imageUri != null){
            final  StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        mUri = downloadUri.toString();
                        hashMap.put("imageURL", "" + mUri);
                        databaseReference.updateChildren(hashMap);
                        Glide.with(getApplicationContext()).load(imageUri).centerCrop().into(circleImageView);
                        pd.dismiss();
                        Toast.makeText(CreatingProfileActivity.this,"Image uploaded Successfully!",Toast.LENGTH_SHORT).show();
                        if(mCurrentPhotoPath != null) {
                            File file = new File(mCurrentPhotoPath);
                            if(file != null)
                            {
                                file.delete();
                            }
                        }
                    } else {
                        Toast.makeText(CreatingProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatingProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(CreatingProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            imageUri = data.getData();
        }
        if(requestCode == 2 && resultCode == RESULT_OK) {
            // Making sure the user selected an image
                galleryAddPic();
        }
        if (uploadTask != null && uploadTask.isInProgress()){
            Toast.makeText(CreatingProfileActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            try {
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(CreatingProfileActivity.this, "...", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.modify.jabber.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 2);
            }
        }
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        if(contentUri != null)
        {
            imageUri = contentUri;
        }
    }
    private void status(String status){
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}