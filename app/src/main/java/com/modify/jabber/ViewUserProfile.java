package com.modify.jabber;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modify.jabber.Adapter.ProfileAdapter;
import com.modify.jabber.Fragments.MenuFragment;
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewUserProfile extends MenuActivity {
    DatabaseReference reference,toolbar_reference;
    FirebaseUser fuser;
    ProfileAdapter profileAdapter;
    RecyclerView recyclerView;
    List<ProfileMedia> mprofile;
    LinearLayoutManager linearLayoutManager;
    String userid;
    boolean isMenuFragmentLoaded;
    Fragment menuFragment;
    TextView title;
    ImageView menuButton,backButton;
    CircleImageView profile_image;
    int previousPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAddlayout(R.layout.activity_view_user_profile);

        recyclerView = findViewById(R.id.view_recycler_view_Profile);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(ViewUserProfile.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        title = findViewById(R.id.title_top);
        profile_image = findViewById(R.id.profile_image);
        menuButton = findViewById(R.id.menu_icon);
        backButton = findViewById(R.id.BackArrow);
        backButton.setVisibility(View.VISIBLE);
        isMenuFragmentLoaded = false;

        Intent intent = getIntent();
        userid = intent.getStringExtra("UserID");
        previousPage = intent.getIntExtra("viewFragment",-1);
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(previousPage == -1)
                {
                    Intent start = new Intent(ViewUserProfile.this, MessageActivity.class);
                    start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    start.putExtra("userid", userid);
                    startActivity(start);
                }
                else {
                    Intent start = new Intent(ViewUserProfile.this, MainActivity.class);
                    start.putExtra("viewFragment",previousPage);
                    startActivity(start);
                }
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
                if(mprofile.isEmpty())
                {
                    mprofile.add(createStandIn());
                    profileAdapter = new ProfileAdapter(getApplicationContext(),mprofile);
                    recyclerView.setAdapter(profileAdapter);
                }
                else {
                    profileAdapter = new ProfileAdapter(getApplicationContext(), mprofile);
                    recyclerView.setAdapter(profileAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private ProfileMedia createStandIn()
    {
        return new ProfileMedia("",fuser.getUid(),"","","","","","");
    }
    private void status(String status){
        toolbar_reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        toolbar_reference.updateChildren(hashMap);
    }
    public void hideMenuFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
        fragmentTransaction.remove(menuFragment);
        fragmentTransaction.commit();
        isMenuFragmentLoaded = false;
    }
    public void loadMenuFragment(){
        FragmentManager fm = getSupportFragmentManager();
        menuFragment = fm.findFragmentById(R.id.container4);
        if(menuFragment == null){
            menuFragment = new MenuFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
            fragmentTransaction.add(R.id.container4,menuFragment);
            fragmentTransaction.commit();
        }
        isMenuFragmentLoaded = true;
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