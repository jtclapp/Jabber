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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatingPostActivity extends AppCompatActivity {
    ImageButton create;
    EditText typedCaption;
    ImageView photo,uploadedPhoto;
    CircleImageView toolbar_image_profile;
    TextView toolbar_username;
    private Uri imageUri;
    FirebaseUser fuser;
    StorageReference storageReference;
    String mUri,date,postid,mCurrentPhotoPath;
    ProfileMedia editPost;
    DatabaseReference databaseReference, toolbarReference;
    HashMap<String, Object> hashMap;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creating_post);

        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        editPost = (ProfileMedia) intent.getSerializableExtra("EditPost");
        hashMap = new HashMap<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        create = findViewById(R.id.CreatePostButton);
        toolbar_image_profile = findViewById(R.id.toolbar3_profile_image);
        toolbar_username = findViewById(R.id.toolbar3_username);
        photo = findViewById(R.id.btn_get_image);
        uploadedPhoto = findViewById(R.id.PostedImage);
        typedCaption = findViewById(R.id.uploaded_caption);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("FeedImages");

        toolbarReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        toolbarReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user.getUsername() != null;
                toolbar_username.setText(user.getUsername());
                if(user.getImageURL().equals("default"))
                {
                    toolbar_image_profile.setImageResource(R.mipmap.ic_launcher);
                }
                else
                {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(toolbar_image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caption = typedCaption.getText().toString();
                date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                if (editPost != null) {
                    HashMap<String, Object> hash = new HashMap<>();
                    if (mUri != null) {
                        hash.put("message", "" + mUri);
                        hash.put("type", "image");
                    }
                    if (editPost.getMessage() == null && mUri == null) {
                        hash.put("type", "text");
                    }
                    if (caption.equals("")) {
                        hash.put("caption", "");
                    } else {
                        hash.put("caption", caption);
                    }
                    databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                    databaseReference.child(editPost.getId()).updateChildren(hash);
                    Intent editStart = new Intent(CreatingPostActivity.this, MainActivity.class);
                    editStart.putExtra("viewProfile",3);
                    startActivity(editStart);
                } else {
                    if (mUri == null) {
                        postid = databaseReference.push().getKey();
                        hashMap.put("id", postid);
                        hashMap.put("sender", fuser.getUid());
                        hashMap.put("message", "");
                        hashMap.put("type", "text");
                    }
                    if (caption.equals("")) {
                        hashMap.put("caption", "");
                    } else {
                        hashMap.put("caption", caption);
                    }
                    if (caption.equals("") && mUri == null) {
                        Toast.makeText(CreatingPostActivity.this, "Please upload a picture or write what's on your mind.", Toast.LENGTH_SHORT).show();
                    } else {
                        hashMap.put("date", date);
                        databaseReference.child(postid).setValue(hashMap);
                        Intent start = new Intent(CreatingPostActivity.this, MainActivity.class);
                        start.putExtra("viewProfile",3);
                        startActivity(start);
                    }
                }
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(CreatingPostActivity.this);
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
        // this will run if updating a already posted post.
        if(editPost != null)
        {
            if(editPost.getMessage() != null) {
                Glide.with(getApplicationContext()).load(editPost.getMessage()).centerCrop().into(uploadedPhoto);
            }
            typedCaption.setText(editPost.getCaption());
        }
    }
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = CreatingPostActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() throws IOException {

        final ProgressDialog pd = new ProgressDialog(CreatingPostActivity.this);
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
                        postid = databaseReference.push().getKey();
                        hashMap.put("id", postid);
                        hashMap.put("sender", fuser.getUid());
                        hashMap.put("message","" + mUri);
                        hashMap.put("type", "image");

                        Glide.with(getApplicationContext()).load(imageUri).centerCrop().into(uploadedPhoto);
                        uploadedPhoto.setVisibility(View.VISIBLE);
                        pd.dismiss();
                        Toast.makeText(CreatingPostActivity.this,"Image uploaded Successfully!",Toast.LENGTH_SHORT).show();
                        if(mCurrentPhotoPath != null) {
                            File file = new File(mCurrentPhotoPath);
                            if(file != null)
                            {
                                file.delete();
                            }
                        }
                    } else {
                        Toast.makeText(CreatingPostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatingPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(CreatingPostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            pd.dismiss();
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
            Toast.makeText(CreatingPostActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case  R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CreatingPostActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.settings:
                startActivity(new Intent(CreatingPostActivity.this,SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }

        return false;
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void status(String status){

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        toolbarReference.updateChildren(hashMap);
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