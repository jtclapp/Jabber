package com.modify.jabber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.modify.jabber.Adapter.ImageAdapter;
import com.modify.jabber.Fragments.MenuFragment;
import com.modify.jabber.model.Thread;
import com.modify.jabber.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewSelectedThread extends MenuActivity {
    String threadID;
    TextView title,caption,imageCount;
    ViewPager images;
    ImageAdapter imageAdapter;
    List<String> imageIDs;
    FirebaseUser fuser;
    DatabaseReference databaseReference,toolbar_reference;
    boolean isMenuFragmentLoaded;
    Fragment menuFragment;
    TextView menuTitle;
    ImageView menuButton,backButton;
    CircleImageView profile_image;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAddlayout(R.layout.activity_view_selected_thread);

        title = findViewById(R.id.view_thread_title);
        caption = findViewById(R.id.view_thread_caption);
        images = findViewById(R.id.ViewThreadImages);
        imageCount = findViewById(R.id.view_photoCount);
        imageAdapter = new ImageAdapter(this,imageIDs);
        relativeLayout = findViewById(R.id.ViewThreadItems);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        imageIDs = new ArrayList<>();
        menuTitle = findViewById(R.id.title_top);
        profile_image = findViewById(R.id.profile_image);
        menuButton = findViewById(R.id.menu_icon);
        backButton = findViewById(R.id.BackArrow);
        backButton.setVisibility(View.VISIBLE);
        isMenuFragmentLoaded = false;

        Intent intent = getIntent();
        threadID = intent.getStringExtra("selectedThread");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent start = new Intent(ViewSelectedThread.this, MainActivity.class);
                    start.putExtra("viewFragment",2);
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
                menuTitle.setText(user.getUsername());
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
        databaseReference = FirebaseDatabase.getInstance().getReference("Threads");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Thread thread = dataSnapshot.getValue(Thread.class);
                    if(thread.getId().equals(threadID))
                    {
                        title.setText(thread.getTitle());
                        caption.setText(thread.getCaption());
                        if(thread.getImage1() != null) {
                            imageIDs.add(thread.getImage1());
                        }
                        if(thread.getImage2() != null) {
                            imageIDs.add(thread.getImage2());
                        }
                        if(thread.getImage3() != null) {
                            imageIDs.add(thread.getImage3());
                        }
                        imageAdapter = new ImageAdapter(getApplicationContext(),imageIDs);
                        images.setAdapter(imageAdapter);
                        if(imageIDs.size() > 0)
                        {
                            imageCount.setText(images.getCurrentItem() + 1 + "/" + imageIDs.size() + "");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
            images.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                imageCount.setText(images.getCurrentItem() + 1 + "/" + imageIDs.size() + "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    public void hideMenuFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
        fragmentTransaction.remove(menuFragment);
        fragmentTransaction.commit();
        relativeLayout.setVisibility(View.VISIBLE);
        isMenuFragmentLoaded = false;
    }
    public void loadMenuFragment(){
        FragmentManager fm = getSupportFragmentManager();
        menuFragment = fm.findFragmentById(R.id.container7);
        if(menuFragment == null){
            menuFragment = new MenuFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
            fragmentTransaction.add(R.id.container7,menuFragment);
            fragmentTransaction.commit();
        }
        relativeLayout.setVisibility(View.GONE);
        isMenuFragmentLoaded = true;
    }
    private void status(String status){
        toolbar_reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

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