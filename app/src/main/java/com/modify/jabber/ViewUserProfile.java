package com.modify.jabber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.modify.jabber.Adapter.ProfileAdapter;
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewUserProfile extends AppCompatActivity {
    CircleImageView image_profile;
    TextView username,bio;
    DatabaseReference reference;
    FirebaseUser fuser;
    StorageReference storageReference;
    ProfileAdapter profileAdapter;
    RecyclerView recyclerView;
    List<ProfileMedia> mprofile;
    ImageButton message;
    LinearLayoutManager linearLayoutManager;
    Intent intent;
    String userid;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);



        image_profile = findViewById(R.id.ViewProfile_image);
        bio = findViewById(R.id.ViewProfileBio);
        username = findViewById(R.id.View_username);
        message = findViewById(R.id.ViewChatButton);
        recyclerView = findViewById(R.id.view_recycler_view_Profile);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(ViewUserProfile.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user.getUsername() != null;
                username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        image_profile.setImageResource(R.mipmap.ic_launcher);
                        bio.setText(user.getBio());
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(image_profile);
                        bio.setText(user.getBio());
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent start = new Intent(ViewUserProfile.this, MessageActivity.class);
                start.putExtra("userid", userid);
                startActivity(start);
            }
        });
        readPosts();
    }
    private void readPosts()
    {
        mprofile = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mprofile.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ProfileMedia media = dataSnapshot.getValue(ProfileMedia.class);
                    assert media.getSender() != null;
                    if(userid.equals(media.getSender()))
                    {
                        mprofile.add(media);
                    }
                }
                profileAdapter = new ProfileAdapter(getApplicationContext(),mprofile);
                recyclerView.setAdapter(profileAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}