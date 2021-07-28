package com.modify.jabber;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;
import com.modify.jabber.Adapter.MessageAdapter;
import com.modify.jabber.Fragments.APIService;
import com.modify.jabber.Notifications.Client;
import com.modify.jabber.Notifications.Data;
import com.modify.jabber.Notifications.MyResponse;
import com.modify.jabber.Notifications.Sender;
import com.modify.jabber.Notifications.Token;
import com.modify.jabber.model.Chat;
import com.modify.jabber.model.Settings;
import com.modify.jabber.model.User;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    FirebaseUser fuser;
    DatabaseReference reference,databaseReference;
    StorageReference storageReference;
    ImageButton btn_send,btn_image;
    EditText text_send;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    List<TextMessage> conversation;
    RecyclerView recyclerView;
    Intent intent;
    ValueEventListener seenListener;
    String userid,mCurrentPhotoPath;
    APIService apiService;
    EditText search_messages;
    Button suggestion1,suggestion2,suggestion3;
    boolean notify = false;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        btn_image = findViewById(R.id.btn_send_image);
        text_send = findViewById(R.id.text_send);
        suggestion1 = findViewById(R.id.suggestionButton1);
        suggestion2 = findViewById(R.id.suggestionButton2);
        suggestion3 = findViewById(R.id.suggestionButton3);

        search_messages = findViewById(R.id.search_messages);
        search_messages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                searchMessages(editable.toString());
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        suggestion1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(fuser.getUid(),userid,suggestion1.getText().toString());
            }
        });
        suggestion2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(fuser.getUid(),userid,suggestion2.getText().toString());
            }
        });
        suggestion3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(fuser.getUid(),userid,suggestion3.getText().toString());
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("Select An Image");
                builder.setIcon(R.mipmap.ic_launcher_symbol);
                builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notify = true;
                        dispatchTakePictureIntent();
                    }
                });
                builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notify = true;
                        openImage();
                    }
                });
                builder.show();
            }
        });
        storageReference = FirebaseStorage.getInstance().getReference("ChatImages-" + userid);
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    //Picasso.get().load(user.getImageURL()).fit().centerInside().rotate(270).into(profile_image);
                    Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(profile_image);
                }

                readMessages(fuser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewUserProfile.class);
                intent.putExtra("UserID", userid);
                startActivity(intent);
            }
        });
        seenMessage(userid);
        setColorOfButtons();
    }
    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendMessage(String sender, final String receiver, String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("type","text");
        hashMap.put("isseen", false);
        hashMap.put("date",currentDate());
        reference.child("Chats").push().setValue(hashMap);

        addUserToChatFragment();
        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendNotification(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher_color, username+": "+message, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void readMessages(final String myid, final String userid, final String imageurl){
        mchat = new ArrayList<>();
        conversation = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                conversation.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid))
                    {
                        suggestion1.setVisibility(View.VISIBLE);
                        suggestion2.setVisibility(View.VISIBLE);
                        suggestion3.setVisibility(View.VISIBLE);
                        conversation.add(TextMessage.createForRemoteUser(chat.getMessage(),System.currentTimeMillis(),userid));
                    }
                    if(chat.getReceiver().equals(userid) && chat.getSender().equals(myid))
                    {
                        suggestion1.setVisibility(View.GONE);
                        suggestion2.setVisibility(View.GONE);
                        suggestion3.setVisibility(View.GONE);
                        conversation.add(TextMessage.createForLocalUser(chat.getMessage(),System.currentTimeMillis()));
                    }
                }
                messageAdapter = new MessageAdapter(getApplicationContext(), mchat, imageurl);
                recyclerView.setAdapter(messageAdapter);
                getSmartReply(conversation);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void searchMessages(String s) {
        ArrayList<Chat> filteredList = new ArrayList<>();
        for(Chat chat : mchat)
        {
            if(chat.getMessage().toLowerCase().contains(s.toLowerCase()))
            {
                filteredList.add(chat);
            }
        }
        messageAdapter.filterList(filteredList);
    }
    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = MessageActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage() throws IOException
    {
                    final ProgressDialog pd = new ProgressDialog(MessageActivity.this);
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
                                    String mUri = downloadUri.toString();

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("sender", fuser.getUid());
                                    hashMap.put("receiver", userid);
                                    hashMap.put("message","" + mUri);
                                    hashMap.put("type", "image");
                                    hashMap.put("isseen", false);
                                    hashMap.put("date",currentDate());
                                    databaseReference.child("Chats").push().setValue(hashMap);
                                    addUserToChatFragment();
                                    pd.dismiss();
                                    final String msg = "Sent a photo...";
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            User user = dataSnapshot.getValue(User.class);
                                            if (notify) {
                                                sendNotification(userid, user.getUsername(), msg);
                                            }
                                            notify = false;
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    if(mCurrentPhotoPath != null) {
                                        File file = new File(mCurrentPhotoPath);
                                        if(file != null)
                                        {
                                            file.delete();
                                        }
                                    }
                                } else {
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        });
                    } else {
                        Toast.makeText(MessageActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MessageActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
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
    private void setColorOfButtons()
    {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Settings").child("Settings-" + fuser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sentColor;
                Settings settings = snapshot.getValue(Settings.class);
                sentColor = settings.getSentColor();
                    btn_image.getBackground().setColorFilter(Color.parseColor(sentColor), PorterDuff.Mode.SRC_IN);
                    btn_send.getBackground().setColorFilter(Color.parseColor(sentColor), PorterDuff.Mode.SRC_IN);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this,"Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getSmartReply(List<TextMessage> conv)
    {
        SmartReplyGenerator smartReply = SmartReply.getClient();
        smartReply.suggestReplies(conv)
                .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onSuccess(SmartReplySuggestionResult replySuggestionResult) {
                        if (replySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            suggestion1.setVisibility(View.GONE);
                            suggestion2.setVisibility(View.GONE);
                            suggestion3.setVisibility(View.GONE);

                        } else if (replySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            suggestion1.setText("");
                            suggestion2.setText("");
                            suggestion3.setText("");
                            for (SmartReplySuggestion suggestion : replySuggestionResult.getSuggestions()) {
                                String replyText = suggestion.getText();
                                if(suggestion1.getText().toString().equals(""))
                                {
                                    suggestion1.setText(replyText);
                                    continue;
                                }
                                if(suggestion2.getText().toString().equals(""))
                                {
                                    suggestion2.setText(replyText);
                                    continue;
                                }
                                if(suggestion3.getText().toString().equals(""))
                                {
                                    suggestion3.setText(replyText);
                                    continue;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        suggestion1.setVisibility(View.GONE);
                        suggestion2.setVisibility(View.GONE);
                        suggestion3.setVisibility(View.GONE);
                    }
                });
    }
    private void addUserToChatFragment()
    {
        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case  R.id.logout:
                FirebaseAuth.getInstance().signOut();
                // change this code because your app will crash
                startActivity(new Intent(MessageActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.settings:
                startActivity(new Intent(MessageActivity.this,SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.search:
                if(search_messages.getVisibility() == View.VISIBLE)
                {
                    search_messages.setVisibility(View.GONE);
                    search_messages.setText("");
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            username.setText(user.getUsername());
                            if (user.getImageURL().equals("default")){
                                profile_image.setImageResource(R.mipmap.ic_launcher);
                            } else {
                                //Picasso.get().load(user.getImageURL()).fit().centerInside().rotate(270).into(profile_image);
                                Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(profile_image);
                            }

                            readMessages(fuser.getUid(), userid, user.getImageURL());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    search_messages.setVisibility(View.VISIBLE);
                }
                return true;
        }
        return false;
    }
    public String currentDate()
    {
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        return currentDate;
    }
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(MessageActivity.this,MainActivity.class));
        finish();
    }
}