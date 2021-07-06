 package com.modify.jabber.Fragments;

 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.MimeTypeMap;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;

 import androidx.annotation.NonNull;
 import androidx.fragment.app.Fragment;
 import androidx.fragment.app.FragmentManager;
 import androidx.fragment.app.FragmentTransaction;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;

 import com.bumptech.glide.Glide;
 import com.google.android.gms.tasks.Continuation;
 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.OnFailureListener;
 import com.google.android.gms.tasks.Task;
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
 import com.modify.jabber.CreatingPostActivity;
 import com.modify.jabber.MessageActivity;
 import com.modify.jabber.R;
 import com.modify.jabber.model.ProfileMedia;
 import com.modify.jabber.model.User;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;

 import de.hdodenhof.circleimageview.CircleImageView;

 import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username,bio;
    DatabaseReference reference;
    FirebaseUser fuser;
    StorageReference storageReference;
    ProfileAdapter profileAdapter;
    RecyclerView recyclerView;
    List<ProfileMedia> mprofile;
    ImageButton create;
    LinearLayoutManager linearLayoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        bio = view.findViewById(R.id.ProfileBio);
        username = view.findViewById(R.id.username);
        recyclerView = view.findViewById(R.id.recycler_view_Profile);
        create = view.findViewById(R.id.CreatePost);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL() == null)
                {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                }
                else {
                    if (user.getImageURL().equals("default")) {
                        image_profile.setImageResource(R.mipmap.ic_launcher);
                        bio.setText(user.getBio());
                    } else {
                        //Picasso.get().load(user.getImageURL()).fit().centerInside().rotate(270).into(image_profile);
                        if(getActivity() != null) {
                            Glide.with(getActivity()).load(user.getImageURL()).centerCrop().into(image_profile);
                        }
                        bio.setText(user.getBio());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CreatingPostActivity.class));
            }
        });
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