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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class CreatingThreadActivity extends MenuActivity {
    private Uri imageUri;
    FirebaseUser fuser;
    HashMap<String, Object> hashMap;
    boolean isMenuFragmentLoaded;
    Fragment menuFragment;
    TextView title,threadTitle,imageCount,titleCount;
    EditText caption;
    ImageButton create;
    ImageView menuButton,backButton,photo;
    DatabaseReference databaseReference, toolbar_reference;
    CircleImageView profile_image;
    RelativeLayout relativeLayout;
    StorageReference storageReference;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    String mUri,date,threadID,mCurrentPhotoPath;
    ViewPager viewPager;
    ImageAdapter imageAdapter;
    List<String> imageIDs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAddlayout(R.layout.activity_creating_thread);

        hashMap = new HashMap<>();
        imageIDs = new ArrayList<>();
        title = findViewById(R.id.title_top);
        photo = findViewById(R.id.btn_get_thread_image);
        profile_image = findViewById(R.id.profile_image);
        imageCount = findViewById(R.id.photoCount);
        titleCount = findViewById(R.id.titleCount);
        relativeLayout = findViewById(R.id.CreatingThreadActivityItems);
        menuButton = findViewById(R.id.menu_icon);
        backButton = findViewById(R.id.BackArrow);
        threadTitle = findViewById(R.id.thread_title);
        caption = findViewById(R.id.uploaded_thread_caption);
        create = findViewById(R.id.CreateThreadButton);
        viewPager = findViewById(R.id.ThreadImages);
        backButton.setVisibility(View.VISIBLE);
        isMenuFragmentLoaded = false;
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        imageAdapter = new ImageAdapter(this,imageIDs);
        databaseReference = FirebaseDatabase.getInstance().getReference("Threads");
        storageReference = FirebaseStorage.getInstance().getReference("ThreadImages");
        date = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

        // Adds the content to Firebase
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String final_caption = caption.getText().toString();
                String final_title = threadTitle.getText().toString();
                threadID = databaseReference.push().getKey();
                if(!threadTitle.getText().toString().equals("")) {
                    hashMap.put("id", threadID);
                    hashMap.put("title", final_title);
                    hashMap.put("sender", fuser.getUid());
                }
                    if (mUri == null) {
                        hashMap.put("type", "text");
                    }
                    if(mUri != null) {
                        hashMap.put("type", "image");
                    }
                    // Title is needed for it to show within the Threads list
                    if(threadTitle.getText().toString().equals(""))
                    {
                       Toast.makeText(CreatingThreadActivity.this,"Please set a title.",Toast.LENGTH_SHORT).show();
                    }
                    if (caption.equals("")) {
                        hashMap.put("caption", "");
                    } else {
                        hashMap.put("caption", final_caption);
                    }
                    if (caption.equals("") && mUri == null) {
                        Toast.makeText(CreatingThreadActivity.this, "Please upload a picture or write what's on your mind.", Toast.LENGTH_SHORT).show();
                    } else {
                        hashMap.put("date", date);
                        addImagesToHashmap();
                        databaseReference.child(threadID).setValue(hashMap);
                        Intent start = new Intent(CreatingThreadActivity.this, MainActivity.class);
                        start.putExtra("viewFragment",2);
                        startActivity(start);
                    }
                }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(CreatingThreadActivity.this, MainActivity.class);
                start.putExtra("viewFragment",2);
                startActivity(start);
            }
        });
        // Controls the showing and hiding of the main menu
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
        final TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length
                titleCount.setText(String.valueOf(70 - s.length()) + " characters remaining");
            }

            public void afterTextChanged(Editable s) {
            }
        };
        threadTitle.addTextChangedListener(mTextEditorWatcher);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                imageCount.setText(viewPager.getCurrentItem() + 1 + "/" + imageIDs.size() + "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // This prompts the user, and will ask them if their choosing an image from gallery or camera
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // User can only upload a total of three images
                if (imageIDs.size() <= 2) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(CreatingThreadActivity.this);
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
                    Toast.makeText(CreatingThreadActivity.this, "You've already uploaded 3 photos.'",Toast.LENGTH_LONG).show();
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
    }
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = CreatingThreadActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    // uploadImage() stores the image, and adds certain data to the hashmap
    private void uploadImage() throws IOException {

        final ProgressDialog pd = new ProgressDialog(CreatingThreadActivity.this);
        pd.setTitle("Uploading");
        pd.setIcon(R.mipmap.ic_launcher_symbol);
        pd.show();

        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
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

                        imageIDs.add(mUri);
                        imageAdapter = new ImageAdapter(getApplicationContext(),imageIDs);
                        viewPager.setAdapter(imageAdapter);
                        imageCount.setText(viewPager.getCurrentItem() + 1 + "/" + imageIDs.size() + "");

                        pd.dismiss();
                        Toast.makeText(CreatingThreadActivity.this,"Image uploaded Successfully!",Toast.LENGTH_SHORT).show();
                        if(mCurrentPhotoPath != null) {
                            File file = new File(mCurrentPhotoPath);
                            file.delete();
                        }
                    } else {
                        Toast.makeText(CreatingThreadActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatingThreadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(CreatingThreadActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }
    // This gets called when moving from Gallery or Camera back to Jabber
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
            Toast.makeText(CreatingThreadActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
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
        menuFragment = fm.findFragmentById(R.id.container6);
        if(menuFragment == null){
            menuFragment = new MenuFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
            fragmentTransaction.add(R.id.container6,menuFragment);
            relativeLayout.setVisibility(View.GONE);
            fragmentTransaction.commit();
        }
        isMenuFragmentLoaded = true;
    }
    // Shows other users if you're currently online or not
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