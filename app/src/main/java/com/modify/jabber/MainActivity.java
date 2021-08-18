package com.modify.jabber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modify.jabber.Fragments.ChatFragment;
import com.modify.jabber.Fragments.MenuFragment;
import com.modify.jabber.Fragments.ProfileFragment;
import com.modify.jabber.Fragments.ThreadFragment;
import com.modify.jabber.Fragments.UserFragment;
import com.modify.jabber.model.User;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends MenuActivity {
    private static final int REQUEST = 112;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    boolean isMenuFragmentLoaded;
    Fragment menuFragment;
    TextView title;
    ImageView menuButton;
    CircleImageView profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAddlayout(R.layout.activity_main);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        title = findViewById(R.id.title_top);
        profile_image = findViewById(R.id.profile_image);
        menuButton = findViewById(R.id.menu_icon);
        isMenuFragmentLoaded = false;
        if(!isConnected())
        {
            firebaseUser = null;
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Please connect to internet to use Jabber.");
            builder.setIcon(R.mipmap.ic_launcher_symbol);
            builder.setNegativeButton("Cancel",null);
            builder.show();
        }
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
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET};
            if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) MainActivity.this, PERMISSIONS, REQUEST);
            } else {
                //do here
            }
        }
        if (reference == null || firebaseUser == null) {
            startActivity(new Intent(getApplicationContext(), StartActivity.class));
        }
        Intent intent = getIntent();
        int sendToFragment = intent.getIntExtra("viewFragment", 3);
        sendToFragment(sendToFragment);
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        // Ctrl + O

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                }
            }
        }
    }
    private boolean hasPermissions(Context context, String... permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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
        menuFragment = fm.findFragmentById(R.id.container);
        if(menuFragment == null){
            menuFragment = new MenuFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);
            fragmentTransaction.add(R.id.container,menuFragment);
            fragmentTransaction.commit();
        }
        isMenuFragmentLoaded = true;
    }
    public void sendToFragment(int position)
    {
        final ViewPager viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatFragment(), "Chats");
        viewPagerAdapter.addFragment(new UserFragment(), "Users");
        viewPagerAdapter.addFragment(new ThreadFragment(), "Threads");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(position);
    }
    public boolean isConnected()
    {
        boolean result;
        try {
            @SuppressLint({"NewApi", "LocalSuppress"}) ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            result = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        }
        catch (Exception e)
        {
            result = false;
        }
        return result;
    }
    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

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