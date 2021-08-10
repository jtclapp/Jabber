package com.modify.jabber.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.modify.jabber.R;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder>
{
    private Context mContext;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private FirebaseUser fuser;
    public ThreadAdapter(Context mContext) {
        this.mContext = mContext;
    }
    @NonNull
    @Override
    public ThreadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ThreadAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
