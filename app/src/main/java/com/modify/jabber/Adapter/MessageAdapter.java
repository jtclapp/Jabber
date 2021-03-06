package com.modify.jabber.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.modify.jabber.R;
import com.modify.jabber.model.Chat;
import com.modify.jabber.model.Settings;
import com.modify.jabber.model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        public static  final int MSG_TYPE_LEFT = 0;
        public static  final int MSG_TYPE_RIGHT = 1;

        private Context mContext;
        private List<Chat> mChat;
        private String imageurl;
        private String yourImageurl;
        private DatabaseReference reference,databaseReference;
        FirebaseUser fuser;

        public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl, String yourImageurl){
            this.mChat = mChat;
            this.mContext = mContext;
            this.imageurl = imageurl;
            this.yourImageurl = yourImageurl;
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
        public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            Chat chat = mChat.get(position);
            String type = chat.getType();

            fuser = FirebaseAuth.getInstance().getCurrentUser();
            databaseReference = FirebaseDatabase.getInstance().getReference("Settings").child("Settings:" + fuser.getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Settings settings = snapshot.getValue(Settings.class);
                    GradientDrawable backgroundGradient;
                    String receivedColor,sentColor,receivedTextColor,sentTextColor;
                    receivedColor = settings.getReceivedColor();
                    sentColor = settings.getSentColor();
                    receivedTextColor = settings.getReceivedTextColor();
                    sentTextColor = settings.getSentTextColor();

                    if(getItemViewType(position) == MSG_TYPE_RIGHT)
                    {
                        backgroundGradient = (GradientDrawable)holder.image_text.getBackground();
                        backgroundGradient.setColor(Color.parseColor(sentColor));

                        holder.show_message.setTextColor(Color.parseColor(sentTextColor));
                        backgroundGradient = (GradientDrawable)holder.show_message.getBackground();
                        backgroundGradient.setColor(Color.parseColor(sentColor));
                    } else {
                        backgroundGradient = (GradientDrawable)holder.image_text.getBackground();
                        backgroundGradient.setColor(Color.parseColor(receivedColor));

                        holder.show_message.setTextColor(Color.parseColor(receivedTextColor));
                        backgroundGradient = (GradientDrawable)holder.show_message.getBackground();
                        backgroundGradient.setColor(Color.parseColor(receivedColor));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mContext,"Failed",Toast.LENGTH_SHORT).show();
                }
            });
            if(position >= 1)
            {
                Chat chat1 = mChat.get(position - 1);
                if(chat.getDate().equals(chat1.getDate()))
                {
                    holder.txt_date.setVisibility(View.GONE);
                }
                else {
                    holder.txt_date.setVisibility(View.VISIBLE);
                    holder.txt_date.setText(chat.getDate());
                }
            }
            else {
                holder.txt_date.setVisibility(View.VISIBLE);
                holder.txt_date.setText(chat.getDate());
            }
            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                        if (user.getImageURL().equals("default")) {
                            holder.your_profile_image.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Glide.with(mContext).load(yourImageurl).centerCrop().into(holder.your_profile_image);
                        }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            if(type.equals("text")) {
                holder.show_message.setVisibility(View.VISIBLE);
                holder.image_text.setVisibility(View.GONE);
                holder.show_message.setText(chat.getMessage());
                params.addRule(RelativeLayout.BELOW,R.id.show_message);
                params.addRule(RelativeLayout.START_OF,R.id.Your_profile_image);
                params.addRule(RelativeLayout.ALIGN_END,R.id.show_message);
                holder.txt_seen.setLayoutParams(params);
            }
            if(type.equals("image"))
            {
                holder.show_message.setVisibility(View.GONE);
                holder.image_text.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(chat.getMessage()).centerCrop().into(holder.image_text);
                params.addRule(RelativeLayout.BELOW,R.id.messageImage);
                params.addRule(RelativeLayout.START_OF,R.id.Your_profile_image);
                params.addRule(RelativeLayout.ALIGN_END,R.id.messageImage );
                holder.txt_seen.setLayoutParams(params);
            }
            if (imageurl.equals("default")){
                holder.profile_image.setImageResource(R.mipmap.ic_launcher);
            }
            else
            {
                Glide.with(mContext).load(imageurl).centerCrop().into(holder.profile_image);
            }
            if (position == mChat.size()-1){
                if (mChat.get(position).isIsseen()){
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

            public TextView show_message,txt_seen,txt_date;
            public ImageView image_text;
            public CircleImageView profile_image,your_profile_image;

            public ViewHolder(View itemView) {
                super(itemView);

                show_message = itemView.findViewById(R.id.show_message);
                profile_image = itemView.findViewById(R.id.profile_image);
                txt_seen = itemView.findViewById(R.id.txt_seen);
                image_text = itemView.findViewById(R.id.messageImage);
                your_profile_image = itemView.findViewById(R.id.Your_profile_image);
                txt_date = itemView.findViewById(R.id.message_date);
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
        public void filterList(ArrayList<Chat> filteredList)
        {
            mChat = filteredList;
            notifyDataSetChanged();
        }
}