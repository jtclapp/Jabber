package com.modify.jabber.Adapter;

import android.content.Context;
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
import com.modify.jabber.R;
import com.modify.jabber.model.Chat;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        public static  final int MSG_TYPE_LEFT = 0;
        public static  final int MSG_TYPE_RIGHT = 1;

        private Context mContext;
        private List<Chat> mChat;
        private String imageurl;

        FirebaseUser fuser;

        public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl){
            this.mChat = mChat;
            this.mContext = mContext;
            this.imageurl = imageurl;
        }

        @NonNull
        @Override
        public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == MSG_TYPE_RIGHT) {
                view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

            Chat chat = mChat.get(position);
            String type = chat.getType();

            if(type.equals("text")) {
                holder.show_message.setVisibility(View.VISIBLE);
                holder.image_text.setVisibility(View.GONE);
                holder.show_message.setText(chat.getMessage());
            }
            if(type.equals("image"))
            {
                holder.show_message.setVisibility(View.GONE);
                holder.image_text.setVisibility(View.VISIBLE);
                //Picasso.get().load(chat.getMessage()).into(holder.image_text);
                Glide.with(mContext).load(chat.getMessage()).centerCrop().into(holder.image_text);
            }

            if (imageurl.equals("default")){
                holder.profile_image.setImageResource(R.mipmap.ic_launcher);
            }
            else
            {
                Glide.with(mContext).load(imageurl).centerCrop().into(holder.profile_image);
            }

            if (position == (mChat.size() - 1)){
                if (chat.isIsSeen()){
                    holder.txt_seen.setText("Seen");
                } else {
                    holder.txt_seen.setText("Delivered");
                }
            } else {
                holder.txt_seen.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return mChat.size();
        }

        public  class ViewHolder extends RecyclerView.ViewHolder{

            public TextView show_message;
            public ImageView image_text;
            public CircleImageView profile_image;
            public TextView txt_seen;

            public ViewHolder(View itemView) {
                super(itemView);

                show_message = itemView.findViewById(R.id.show_message);
                profile_image = itemView.findViewById(R.id.profile_image);
                txt_seen = itemView.findViewById(R.id.txt_seen);
                image_text = itemView.findViewById(R.id.messageImage);
            }
        }

        @Override
        public int getItemViewType(int position) {
            fuser = FirebaseAuth.getInstance().getCurrentUser();
            if (mChat.get(position).getSender().equals(fuser.getUid())){
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
}