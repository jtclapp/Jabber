package com.modify.jabber;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modify.jabber.model.Settings;
import com.modify.jabber.model.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingActivity extends AppCompatActivity {
    CircleImageView toolbar_image_profile;
    TextView toolbar_username;
    DatabaseReference toolbarReference,databaseReference;
    FirebaseUser fuser;
    int receivedColor,sentColor;
    Button received,sent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar_image_profile = findViewById(R.id.toolbar5_profile_image);
        toolbar_username = findViewById(R.id.toolbar5_username);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        toolbarReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        toolbarReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user.getUsername() != null;
                toolbar_username.setText(user.getUsername());
                if(user.getImageURL().equals("default"))
                {
                    toolbar_image_profile.setImageResource(R.mipmap.ic_launcher);
                }
                else
                {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(toolbar_image_profile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("Settings").child("Settings-" + fuser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Settings settings = snapshot.getValue(Settings.class);
                receivedColor = Color.parseColor(settings.getReceivedColor());
                sentColor = Color.parseColor(settings.getSentColor());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
            }
        });
        received = findViewById(R.id.ChangeReceivedMessageColor);
        sent = findViewById(R.id.ChangeSendMessageColor);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case  R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.settings:
                startActivity(new Intent(SettingActivity.this,SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }

        return false;
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}