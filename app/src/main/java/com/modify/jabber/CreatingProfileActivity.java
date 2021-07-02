package com.modify.jabber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
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
    private Uri imageUri;
    FirebaseUser fuser;
    StorageReference storageReference;
    String mUri;
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
            public void onClick(View v) {
                openImage();
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUri == null || bio.getText().toString().equals("")) {
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
                    startActivity(new Intent(CreatingProfileActivity.this, MainActivity.class));
                }
            }
        });

    }
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = CreatingProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() throws IOException {

        final ProgressDialog pd = new ProgressDialog(CreatingProfileActivity.this);
        pd.setMessage("Uploading");
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


                        Glide.with(CreatingProfileActivity.this).load(imageUri).centerCrop().into(circleImageView);
                        pd.dismiss();
                        Toast.makeText(CreatingProfileActivity.this,"Image uploaded Successfully!",Toast.LENGTH_SHORT).show();

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
            Toast.makeText(CreatingProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

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
    }
}