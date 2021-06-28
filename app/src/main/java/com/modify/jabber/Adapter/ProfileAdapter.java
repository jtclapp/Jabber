package com.modify.jabber.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.modify.jabber.R;
import com.modify.jabber.model.ProfileMedia;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>
{
    private Context mContext;
    private List<ProfileMedia> profileMediaList;
    FirebaseUser fuser;

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

        holder.image_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"......." + position,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileMediaList.size();
    }
    public ProfileMedia getItem(int position)
    {
        return profileMediaList.get(position);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, show_date;
        public ImageView image_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.typed_post);
            image_text = itemView.findViewById(R.id.postImage);
            show_date = itemView.findViewById(R.id.post_date);
        }
    }
}
