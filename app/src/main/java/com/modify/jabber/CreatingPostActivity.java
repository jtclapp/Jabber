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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

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
import com.modify.jabber.Adapter.ImageAdapter;
import com.modify.jabber.Fragments.MenuFragment;
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.Thread;
import com.modify.jabber.model.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatingPostActivity extends MenuActivity {
    Button create;
    EditText typedCaption;
    ImageView photo;
    ViewPager uploadedPhotos;
    private Uri imageUri;
    FirebaseUser fuser;
    StorageReference storageReference;
    String mUri,date,postid,mCurrentPhotoPath;
    ProfileMedia editPost;
    DatabaseReference databaseReference, toolbar_reference;
    HashMap<String, Object> hashMap;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    boolean isMenuFragmentLoaded;
    Fragment menuFragment;
    TextView title,photoCount;
    ImageView menuButton,backButton;
    CircleImageView profile_image;
    RelativeLayout relativeLayout;
    ImageAdapter imageAdapter;
    List<String> imageIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAddlayout(R.layout.activity_creating_post);

        Intent intent = getIntent();
        editPost = (ProfileMedia) intent.getSerializableExtra("EditPost");
        hashMap = new HashMap<>();
        imageIDs = new ArrayList<>();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child("Posts:" + fuser.getUid());
        create = findViewById(R.id.CreatePostButton);
        photo = findViewById(R.id.btn_get_image);
        photoCount = findViewById(R.id.PostPhotoCount);
        uploadedPhotos = findViewById(R.id.PostImages);
        typedCaption = findViewById(R.id.uploaded_caption);
        relativeLayout = findViewById(R.id.CreatingPostActivityItems);
        imageAdapter = new ImageAdapter(this,imageIDs);
        storageReference = FirebaseStorage.getInstance().getReference("FeedImages").child("FeedImages:" + fuser.getUid());
        title = findViewById(R.id.title_top);
        profile_image = findViewById(R.id.profile_image);
        menuButton = findViewById(R.id.menu_icon);
        backButton = findViewById(R.id.BackArrow);
        backButton.setVisibility(View.VISIBLE);
        isMenuFragmentLoaded = false;
        date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

        uploadedPhotos.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                photoCount.setText(uploadedPhotos.getCurrentItem() + 1 + "/" + imageIDs.size() + "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(CreatingPostActivity.this, MainActivity.class);
                start.putExtra("viewFragment",3);
                startActivity(start);
            }
        });
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMenuFragmentLoaded) {
                    loadMenuFragment();

                } if(isMenuFragmentLoaded) {
                    if (menuFragment.isAdded()) {
                        hideMenuFragment();
                    }
                }
            }
        });
        toolbar_reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        toolbar_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                title.setText(user.getUsername());
                if (user.getImageURL() == null) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(profile_image);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caption = typedCaption.getText().toString();
                if (editPost != null) {
                    HashMap<String, Object> hash = new HashMap<>();
                    if (mUri != null) {
                        hash.put("type", "image");
                    }
                    if (editPost.getImage1() == null && mUri == null) {
                        hash.put("type", "text");
                    }
                    if (caption.equals("")) {
                        hash.put("caption", "");
                    } else {
                        hash.put("caption", caption);
                    }
                    addImagesToHashmap();
                    databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child("Posts:" + fuser.getUid());
                    databaseReference.child(editPost.getId()).updateChildren(hash);
                    Intent start = new Intent(CreatingPostActivity.this, MainActivity.class);
                    start.putExtra("viewFragment",3);
                    startActivity(start);
                } else {
                    if (mUri == null) {
                        postid = databaseReference.push().getKey();
                        hashMap.put("id", postid);
                        hashMap.put("sender", fuser.getUid());
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
                        addImagesToHashmap();
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
                if (imageIDs.size() < 3) {
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
                } else {
                    Toast.makeText(CreatingPostActivity.this, "You can only upload 3 Photos.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        // this will run if updating a post.
        if(editPost != null)
        {
            if(editPost.getImage1() != null) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child("Posts:" + fuser.getUid());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ProfileMedia profileMedia = dataSnapshot.getValue(ProfileMedia.class);
                            if(profileMedia.getId().equals(editPost.getId()))
                            {
                                typedCaption.setText(profileMedia.getCaption());
                                if(profileMedia.getImage1() != null) {
                                    imageIDs.add(profileMedia.getImage1());
                                }
                                if(profileMedia.getImage2() != null) {
                                    imageIDs.add(profileMedia.getImage2());
                                }
                                if(profileMedia.getImage3() != null) {
                                    imageIDs.add(profileMedia.getImage3());
                                }
                                imageAdapter = new ImageAdapter(getApplicationContext(),imageIDs);
                                uploadedPhotos.setAdapter(imageAdapter);
                                if(imageIDs.size() > 0)
                                {
                                    photoCount.setText(uploadedPhotos.getCurrentItem() + 1 + "/" + imageIDs.size() + "");
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            create.setText("Update");
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

                        imageIDs.add(mUri);
                        imageAdapter = new ImageAdapter(getApplicationContext(),imageIDs);
                        uploadedPhotos.setAdapter(imageAdapter);
                        photoCount.setText(uploadedPhotos.getCurrentItem() + 1 + "/" + imageIDs.size() + "");

                        pd.dismiss();
                        Toast.makeText(CreatingPostActivity.this,"Image uploaded Successfully!",Toast.LENGTH_SHORT).show();
                        if(mCurrentPhotoPath != null) {
                            File file = new File(mCurrentPhotoPath);
                            file.delete();
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
    public void hideMenuFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
        fragmentTransaction.remove(menuFragment);
        relativeLayout.setVisibility(View.VISIBLE);
        fragmentTransaction.commit();
        isMenuFragmentLoaded = false;
    }
    public void loadMenuFragment(){
        FragmentManager fm = getSupportFragmentManager();
        menuFragment = fm.findFragmentById(R.id.container5);
        if(menuFragment == null){
            menuFragment = new MenuFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
            fragmentTransaction.add(R.id.container5,menuFragment);
            relativeLayout.setVisibility(View.GONE);
            fragmentTransaction.commit();
        }
        isMenuFragmentLoaded = true;
    }
    // Adds the URI of each uploaded image to the hashmap
    private void addImagesToHashmap()
    {
        for(int i = 0; i < imageIDs.size(); i++)
        {
            if(i == 0)
            {
                hashMap.put("image1",imageIDs.get(0));
            }
            if(i == 1)
            {
                hashMap.put("image2",imageIDs.get(1));
            }
            if(i == 2)
            {
                hashMap.put("image3",imageIDs.get(2));
            }
        }
    }
    private void status(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        toolbar_reference.updateChildren(hashMap);
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