package com.modify.jabber.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.modify.jabber.CreatingPostActivity;
import com.modify.jabber.R;
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>
{
    private Context mContext;
    private List<ProfileMedia> profileMediaList;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private StorageReference storageReference;

    public ProfileAdapter(Context mContext, List<ProfileMedia> profileMediaList) {
        this.mContext = mContext;
        this.profileMediaList = profileMediaList;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
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
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Do you want to edit or delete this post?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        final AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                        builder1.setMessage("Are you sure you want to delete?");
                        builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete the post from Realtime database and Firebase storage
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                if(type.equals("image"))
                                {
                                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileMedia.getMessage());
                                    storageReference.delete();
                                }
                                ref.child(profileMedia.getId()).removeValue();
                            }
                        });
                        builder1.setNegativeButton("No",null);
                        builder1.show();
                    }
                });
                builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the user to the edit page...
                        Intent start = new Intent(mContext, CreatingPostActivity.class);
                        start.putExtra("EditPost",profileMedia);
                        mContext.startActivity(start);
                    }
                });
                builder.show();
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user.getUsername() != null;
                holder.username.setText(user.getUsername());
                if(user.getImageURL() == null)
                {
                    holder.username_image.setImageResource(R.mipmap.ic_launcher);
                }
                else {
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
        if(type.equals("text"))
        {
            holder.spacing.setVisibility(View.VISIBLE);
            holder.show_message.setVisibility(View.VISIBLE);
            holder.image_text.setVisibility(View.GONE);
            holder.show_message.setText(profileMedia.getCaption());
        }

        if(type.equals("image"))
        {
            if(profileMedia.getCaption() == null)
            {
                holder.show_message.setVisibility(View.GONE);
            }
            else {
                holder.show_message.setVisibility(View.VISIBLE);
                holder.spacing.setVisibility(View.GONE);
            }
            holder.image_text.setVisibility(View.VISIBLE);
            //Picasso.get().load(profileMedia.getMessage()).rotate(270).into(holder.image_text);
            Glide.with(mContext).load(profileMedia.getMessage()).centerCrop().into(holder.image_text);
            holder.show_message.setText(profileMedia.getCaption());
        }
        holder.show_date.setText(profileMedia.getDate());
    }

    @Override
    public int getItemCount() {
        return profileMediaList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, show_date,spacing;
        public ImageView image_text, menu;
        public CircleImageView username_image;
        public TextView username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spacing = itemView.findViewById(R.id.spacing2);
            show_message = itemView.findViewById(R.id.typed_post);
            image_text = itemView.findViewById(R.id.postImage);
            show_date = itemView.findViewById(R.id.post_date);
            username = itemView.findViewById(R.id.YourUsername);
            username_image = itemView.findViewById(R.id.Your_profile_image);
            menu = itemView.findViewById(R.id.postMenu);
        }
    }
}
