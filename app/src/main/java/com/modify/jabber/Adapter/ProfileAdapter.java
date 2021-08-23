package com.modify.jabber.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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
import com.modify.jabber.CreatingPostActivity;
import com.modify.jabber.CreatingProfileActivity;
import com.modify.jabber.MessageActivity;
import com.modify.jabber.R;
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>
{
    private Context mContext;
    private List<ProfileMedia> profileMediaList;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private FirebaseUser fuser;

    public ProfileAdapter(Context mContext, List<ProfileMedia> profileMediaList) {
        this.mContext = mContext;
        this.profileMediaList = profileMediaList;
    }
    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ProfileAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {

        ProfileMedia profileMedia = profileMediaList.get(position);
        String type = profileMedia.getType();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(profileMedia.getSender());

        if (position == profileMediaList.size() - 1) {
            holder.bold_username.setVisibility(View.VISIBLE);
            holder.image_profile.setVisibility(View.VISIBLE);
            holder.bio.setVisibility(View.VISIBLE);

            if(!fuser.getUid().equals(profileMedia.getSender()))
            {
                holder.editProfile.setVisibility(View.INVISIBLE);
                holder.message.setVisibility(View.VISIBLE);
            } else if(fuser.getUid().equals(profileMedia.getSender())) {
                holder.editProfile.setVisibility(View.VISIBLE);
                holder.message.setVisibility(View.GONE);
            }
            storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");
            reference = FirebaseDatabase.getInstance().getReference("Users").child(profileMedia.getSender());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    holder.bold_username.setText(user.getUsername());
                    if (user.getImageURL() == null) {
                        holder.image_profile.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        if (user.getImageURL().equals("default")) {
                            holder.image_profile.setImageResource(R.mipmap.ic_launcher);
                            holder.bio.setText(user.getBio());
                        } else {
                            if (mContext.getApplicationContext() != null) {
                                Glide.with(mContext.getApplicationContext()).load(user.getImageURL()).centerCrop().into(holder.image_profile);
                            }
                            holder.bio.setText(user.getBio());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            holder.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent start = new Intent(mContext, MessageActivity.class);
                    start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    start.putExtra("userid", profileMedia.getSender());
                    mContext.startActivity(start);
                }
            });
        }
        // Add all the posts that the users posted
        if(!profileMedia.getId().equals("")) {
            holder.show_date.setVisibility(View.VISIBLE);
            holder.username_image.setVisibility(View.VISIBLE);
            holder.username.setVisibility(View.VISIBLE);

            if(profileMedia.getSender().equals(fuser.getUid())) {
                holder.menu.setVisibility(View.VISIBLE);
                holder.menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(mContext, holder.menu);
                        popupMenu.inflate(R.menu.postmenu);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            popupMenu.setForceShowIcon(true);
                        }
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.delete:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Delete Post");
                                        builder.setIcon(R.mipmap.ic_launcher_symbol_round);
                                        builder.setMessage("Are you sure you want to delete this post?");
                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        // Delete the post from Realtime database and Firebase storage
                                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                                        if (type.equals("image")) {
                                                            if(profileMedia.getImage1() != null) {
                                                                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileMedia.getImage1());
                                                                storageReference.delete();
                                                            }
                                                            if(profileMedia.getImage2() != null) {
                                                                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileMedia.getImage2());
                                                                storageReference.delete();
                                                            }
                                                            if(profileMedia.getImage3() != null) {
                                                                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileMedia.getImage3());
                                                                storageReference.delete();
                                                            }
                                                        }
                                                        ref.child(profileMedia.getId()).removeValue();
                                                    }
                                                });
                                        builder.setNegativeButton("No",null);
                                        builder.show();
                                        return true;
                                    case R.id.edit:
                                        // Send the user to the edit page...
                                        Intent start = new Intent(mContext, CreatingPostActivity.class);
                                        start.putExtra("EditPost", profileMedia);
                                        mContext.startActivity(start);
                                        return true;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user.getUsername() != null;
                holder.username.setText(user.getUsername());
                if (user.getImageURL() == null) {
                    holder.username_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    if (user.getImageURL().equals("default")) {
                        holder.username_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(mContext.getApplicationContext()).load(user.getImageURL()).centerCrop().into(holder.username_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (type.equals("text")) {
            holder.spacing.setVisibility(View.VISIBLE);
            holder.show_message.setVisibility(View.VISIBLE);
            holder.viewPager.setVisibility(View.GONE);
            holder.photoCount.setVisibility(View.GONE);
            holder.show_message.setText(profileMedia.getCaption());
        }
        if (type.equals("image")) {
            if (profileMedia.getCaption().equals("")) {
                holder.show_message.setVisibility(View.GONE);
            } else {
                holder.show_message.setVisibility(View.VISIBLE);
            }
            holder.viewPager.setVisibility(View.VISIBLE);
            holder.photoCount.setVisibility(View.VISIBLE);
            holder.spacing.setVisibility(View.GONE);
            holder.show_message.setText(profileMedia.getCaption());

            if(profileMedia.getImage1() != null) {
                holder.imageIDs.add(profileMedia.getImage1());
            }
            if(profileMedia.getImage2() != null) {
                holder.imageIDs.add(profileMedia.getImage2());
            }
            if(profileMedia.getImage3() != null) {
                holder.imageIDs.add(profileMedia.getImage3());
            }
            holder.imageAdapter = new ImageAdapter(mContext,holder.imageIDs);
            holder.viewPager.setAdapter(holder.imageAdapter);
            if(holder.imageIDs.size() > 0)
            {
                holder.photoCount.setText(holder.viewPager.getCurrentItem() + 1 + "/" + holder.imageIDs.size() + "");
            }
        }
        holder.show_date.setText(profileMedia.getDate());
        }
    }
    @Override
    public int getItemCount() {
        return profileMediaList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, show_date, spacing, editProfile, photoCount;
        public ImageView menu;
        public CircleImageView username_image, image_profile;
        public TextView username, bold_username, bio;
        ImageButton message;
        ViewPager viewPager;
        ImageAdapter imageAdapter;
        List<String> imageIDs;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIDs = new ArrayList<>();
            viewPager = itemView.findViewById(R.id.ViewPostImages);
            imageAdapter = new ImageAdapter(mContext,imageIDs);
            photoCount = itemView.findViewById(R.id.ViewPostPhotoCount);
            spacing = itemView.findViewById(R.id.spacing2);
            show_message = itemView.findViewById(R.id.typed_post);
            show_date = itemView.findViewById(R.id.post_date);
            username = itemView.findViewById(R.id.YourUsername);
            username_image = itemView.findViewById(R.id.Your_profile_image);
            menu = itemView.findViewById(R.id.postMenu);
            editProfile = itemView.findViewById(R.id.edit_profile);
            bio = itemView.findViewById(R.id.ProfileBio);
            bold_username = itemView.findViewById(R.id.username);
            image_profile = itemView.findViewById(R.id.profile_image);
            message = itemView.findViewById(R.id.ViewChatButton);

            editProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext.getApplicationContext(), CreatingProfileActivity.class));
                }
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }
                @Override
                public void onPageSelected(int position) {
                    photoCount.setText(viewPager.getCurrentItem() + 1 + "/" + imageIDs.size() + "");
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }
}