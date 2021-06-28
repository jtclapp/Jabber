package com.modify.jabber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.modify.jabber.Fragments.ProfileFragment;
import com.modify.jabber.model.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CreatingPostActivity extends AppCompatActivity {
    Button create;
    EditText typedCaption;
    ImageView photo,uploadedPhoto;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    FirebaseUser fuser;
    StorageReference storageReference;
    Intent intent;
    String downloadUri,date;
    DatabaseReference databaseReference;
    HashMap<String, Object> hashMap;

    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creating_post);

        hashMap = new HashMap<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        intent = getIntent();
        create = findViewById(R.id.button);
        photo = findViewById(R.id.btn_get_image);
        uploadedPhoto = findViewById(R.id.PostedImage);
        typedCaption = findViewById(R.id.uploaded_caption);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        uploadedPhoto.setVisibility(View.INVISIBLE);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caption = typedCaption.getText().toString();
                date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                if(downloadUri == null)
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
                hashMap.put("date",date);
                databaseReference.child("Posts").push().setValue(hashMap);
                startActivity(new Intent(CreatingPostActivity.this, MainActivity.class));
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

        if (imageUri != null)
        {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            String fileNamePath = "FeedImages/" + "post_" + System.currentTimeMillis();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
            byte[] data = outputStream.toByteArray();
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(fileNamePath);
            reference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    downloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful())
                    {
                        hashMap.put("sender", fuser.getUid());
                        hashMap.put("message",downloadUri);
                        hashMap.put("type", "image");
                        Toast.makeText(CreatingPostActivity.this,"Image uploaded Successfully!",Toast.LENGTH_SHORT).show();
                    }
                    pd.dismiss();
                    uploadedPhoto.setVisibility(View.VISIBLE);
                    Picasso.with(CreatingPostActivity.this).load(imageUri).rotate(270).into(uploadedPhoto);

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