package com.modify.jabber.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.modify.jabber.R;
import com.modify.jabber.model.Chat;
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

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        ProfileMedia profileMedia = profileMediaList.get(position);
        String type = profileMedia.getType();

        if(type.equals("text"))
        {
            holder.show_message.setVisibility(View.VISIBLE);
            holder.image_text.setVisibility(View.GONE);
            holder.show_message.setText(profileMedia.getMessage());
        }

        if(type.equals("image"))
        {
            holder.show_message.setVisibility(View.GONE);
            holder.image_text.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(profileMedia.getMessage()).rotate(270).into(holder.image_text);
        }
    }

    @Override
    public int getItemCount() {
        return profileMediaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public ImageView image_text;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            show_message = itemView.findViewById(R.id.show_message);
//            image_text = itemView.findViewById(R.id.messageImage);
        }
    }
}
