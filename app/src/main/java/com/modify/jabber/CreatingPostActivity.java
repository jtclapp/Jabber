package com.modify.jabber;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CreatingPostActivity extends AppCompatActivity {
    ImageButton create;
    EditText typedCaption;
    ImageView photo,uploadedPhoto;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    FirebaseUser fuser;
    StorageReference storageReference;
    String mUri,date;
    DatabaseReference databaseReference;
    HashMap<String, Object> hashMap;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creating_post);

        hashMap = new HashMap<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        create = findViewById(R.id.CreatePostButton);
        photo = findViewById(R.id.btn_get_image);
        uploadedPhoto = findViewById(R.id.PostedImage);
        typedCaption = findViewById(R.id.uploaded_caption);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("FeedImages");

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caption = typedCaption.getText().toString();
                date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                if(mUri == null)
                {
                    hashMap.put("sender", fuser.getUid());
                    hashMap.put("message","");
                    hashMap.put("type", "text");
                }
                if(caption.equals(""))
                {
                    hashMap.put("caption", "");
                }
                else
                {
                    hashMap.put("caption",caption);
                }
                if(caption.equals("") && mUri == null)
                {
                    Toast.makeText(CreatingPostActivity.this,"Please upload a picture or write what's on your mind.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    hashMap.put("date",date);
                    databaseReference.child("Posts").push().setValue(hashMap);
                    startActivity(new Intent(CreatingPostActivity.this, MainActivity.class));
                }
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
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
        ContentResolver contentResolver = CreatingPostActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() throws IOException {

        final ProgressDialog pd = new ProgressDialog(CreatingPostActivity.this);
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

                        hashMap.put("sender", fuser.getUid());
                        hashMap.put("message","" + mUri);
                        hashMap.put("type", "image");

                        Glide.with(CreatingPostActivity.this).load(imageUri).centerCrop().into(uploadedPhoto);
                        uploadedPhoto.setVisibility(View.VISIBLE);
                        pd.dismiss();
                        Toast.makeText(CreatingPostActivity.this,"Image uploaded Successfully!",Toast.LENGTH_SHORT).show();

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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

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
    }
}