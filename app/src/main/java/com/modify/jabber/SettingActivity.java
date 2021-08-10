package com.modify.jabber;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modify.jabber.Fragments.MenuFragment;
import com.modify.jabber.model.Settings;
import com.modify.jabber.model.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingActivity extends MenuActivity {
    FirebaseUser fuser;
    DatabaseReference databaseReference, reference;
    int receivedColor,sentColor,receivedTextColor,sentTextColor;
    Button received,sent,receivedText,sentText;
    boolean isMenuFragmentLoaded;
    Fragment menuFragment;
    TextView title;
    ImageView menuButton;
    CircleImageView profile_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAddlayout(R.layout.activity_setting);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Settings").child("Settings-" + fuser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Settings settings = snapshot.getValue(Settings.class);
                receivedColor = Color.parseColor(settings.getReceivedColor());
                sentColor = Color.parseColor(settings.getSentColor());
                receivedTextColor = Color.parseColor(settings.getReceivedTextColor());
                sentTextColor = Color.parseColor(settings.getSentTextColor());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
            }
        });
        received = findViewById(R.id.ChangeReceivedMessageColor);
        sent = findViewById(R.id.ChangeSendMessageColor);
        receivedText = findViewById(R.id.ChangeReceivedTextColor);
        sentText = findViewById(R.id.ChangeSendTextColor);
        title = (TextView) findViewById(R.id.title_top);
        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        menuButton = (ImageView) findViewById(R.id.menu_icon);
        isMenuFragmentLoaded = false;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
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
        reference.addValueEventListener(new ValueEventListener() {
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

        received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPickerForReceived();
            }
        });
        sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPickerForSent();
            }
        });
        receivedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPickerForReceivedText();
            }
        });
        sentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPickerForSentText();
            }
        });
    }
    public void openColorPickerForReceived()
    {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, receivedColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                receivedColor = color;
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("receivedColor", "#" + Integer.toHexString(receivedColor));
                databaseReference.updateChildren(hashMap);
            }
        });
        colorPicker.show();
    }
    public void openColorPickerForSent()
    {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, sentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                sentColor = color;
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("sentColor", "#" + Integer.toHexString(sentColor));
                databaseReference.updateChildren(hashMap);
            }
        });
        colorPicker.show();
    }
    public void openColorPickerForReceivedText()
    {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, receivedTextColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                receivedTextColor = color;
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("receivedTextColor", "#" + Integer.toHexString(receivedTextColor));
                databaseReference.updateChildren(hashMap);
            }
        });
        colorPicker.show();
    }
    public void openColorPickerForSentText()
    {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, sentTextColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                sentTextColor = color;
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("sentTextColor", "#" + Integer.toHexString(sentTextColor));
                databaseReference.updateChildren(hashMap);
            }
        });
        colorPicker.show();
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
        menuFragment = fm.findFragmentById(R.id.container2);
        if(menuFragment == null){
            menuFragment = new MenuFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
            fragmentTransaction.add(R.id.container2,menuFragment);
            fragmentTransaction.commit();
        }
        isMenuFragmentLoaded = true;
    }
    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
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