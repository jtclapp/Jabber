package com.modify.jabber.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modify.jabber.MainActivity;
import com.modify.jabber.R;
import com.modify.jabber.SettingActivity;
import com.modify.jabber.StartActivity;
import com.modify.jabber.model.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuFragment extends Fragment {
    CircleImageView profile_image;
    TextView username, logout;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    LinearLayout chat;
    LinearLayout profile;
    LinearLayout users;
    LinearLayout thread;
    LinearLayout settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_menu, container, false);
        username = rootView.findViewById(R.id.menu_username);
        profile_image = rootView.findViewById(R.id.menu_ProfileImage);
        chat = rootView.findViewById(R.id.ChatLayout);
        profile = rootView.findViewById(R.id.ProfileLayout);
        users = rootView.findViewById(R.id.UserLayout);
        thread = rootView.findViewById(R.id.TheardLayout);
        settings = rootView.findViewById(R.id.SettingsLayout);
        logout = rootView.findViewById(R.id.MenuLogout);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL() == null) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        if(getActivity() != null) {
                            Glide.with(getActivity()).load(user.getImageURL()).centerCrop().into(profile_image);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(rootView.getContext(), MainActivity.class);
                start.putExtra("viewFragment",0);
                startActivity(start);
            }
        });
        users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(rootView.getContext(), MainActivity.class);
                start.putExtra("viewFragment",1);
                startActivity(start);
            }
        });
        thread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(rootView.getContext(), MainActivity.class);
                start.putExtra("viewFragment",2);
                startActivity(start);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(rootView.getContext(), MainActivity.class);
                start.putExtra("viewFragment",3);
                startActivity(start);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(rootView.getContext(), SettingActivity.class);
                startActivity(start);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
               Intent start = new Intent(rootView.getContext(), StartActivity.class);
               start.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               startActivity(start);
            }
        });
        return rootView;
    }
}