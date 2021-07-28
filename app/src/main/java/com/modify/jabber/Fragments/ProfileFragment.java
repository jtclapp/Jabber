 package com.modify.jabber.Fragments;

 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.TextView;

 import androidx.annotation.NonNull;
 import androidx.fragment.app.Fragment;
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
 import com.google.firebase.storage.FirebaseStorage;
 import com.google.firebase.storage.StorageReference;
 import com.modify.jabber.Adapter.ProfileAdapter;
 import com.modify.jabber.CreatingPostActivity;
 import com.modify.jabber.CreatingProfileActivity;
 import com.modify.jabber.R;
 import com.modify.jabber.model.ProfileMedia;
 import com.modify.jabber.model.User;

 import java.util.ArrayList;
 import java.util.List;

 import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    DatabaseReference reference;
    FirebaseUser fuser;
    ProfileAdapter profileAdapter;
    RecyclerView recyclerView;
    List<ProfileMedia> mprofile;
    LinearLayoutManager linearLayoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_Profile);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        readPosts();
        return view;
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
                        if(fuser.getUid().equals(media.getSender()))
                        {
                            mprofile.add(media);
                        }
                }
                profileAdapter = new ProfileAdapter(getContext(),mprofile);
                recyclerView.setAdapter(profileAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}