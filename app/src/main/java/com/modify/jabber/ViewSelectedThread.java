package com.modify.jabber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.modify.jabber.Adapter.ImageAdapter;
import com.modify.jabber.model.Thread;
import com.modify.jabber.model.User;

import java.util.ArrayList;
import java.util.List;

public class ViewSelectedThread extends AppCompatActivity {
    String threadID;
    TextView title,caption,imageCount;
    ViewPager images;
    ImageAdapter imageAdapter;
    List<String> imageIDs;
    FirebaseUser fuser;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_selected_thread);

        title = findViewById(R.id.view_thread_title);
        caption = findViewById(R.id.view_thread_caption);
        images = findViewById(R.id.ViewThreadImages);
        imageCount = findViewById(R.id.view_photoCount);
        imageAdapter = new ImageAdapter(this,imageIDs);
        imageIDs = new ArrayList<>();

        Intent intent = getIntent();
        threadID = intent.getStringExtra("selectedThread");

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
                        imageCount.setText(images.getCurrentItem() + 1 + "/" + imageIDs.size() + "");

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
}