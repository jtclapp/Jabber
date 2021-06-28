package com.modify.jabber.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modify.jabber.MainActivity;
import com.modify.jabber.R;
import com.modify.jabber.StartActivity;
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>
{
    private Context mContext;
    private List<ProfileMedia> profileMediaList;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

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

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                holder.username.setText(user.getUsername());
                if(user.getImageURL() == null)
                {
                    holder.username_image.setImageResource(R.mipmap.ic_launcher);
                }
                else {
                    if (user.getImageURL().equals("default")) {
                        holder.username_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Picasso.with(mContext).load(user.getImageURL()).fit().centerInside().rotate(270).into(holder.username_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ProfileMedia profileMedia = profileMediaList.get(position);
        String type = profileMedia.getType();
        if(type.equals("text"))
        {
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
            }
            holder.image_text.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(profileMedia.getMessage()).rotate(270).into(holder.image_text);
            holder.show_message.setText(profileMedia.getCaption());
        }
        holder.show_date.setText(profileMedia.getDate());
    }

    @Override
    public int getItemCount() {
        return profileMediaList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, show_date;
        public ImageView image_text;
        public CircleImageView username_image;
        public TextView username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.typed_post);
            image_text = itemView.findViewById(R.id.postImage);
            show_date = itemView.findViewById(R.id.post_date);
            username = itemView.findViewById(R.id.YourUsername);
            username_image = itemView.findViewById(R.id.Your_profile_image);
        }
    }
}
