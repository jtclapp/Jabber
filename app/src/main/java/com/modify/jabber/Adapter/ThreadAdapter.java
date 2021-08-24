package com.modify.jabber.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.modify.jabber.R;
import com.modify.jabber.ViewSelectedThread;
import com.modify.jabber.model.Thread;

import java.util.List;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder>
{
    private Context mContext;
    private List<Thread> threadList;

    public ThreadAdapter(Context mContext, List<Thread> threadList) {
        this.mContext = mContext;
        this.threadList = threadList;
    }
    @NonNull
    @Override
    public ThreadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.thread_items, parent, false);
        return new ThreadAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ThreadAdapter.ViewHolder holder, int position) {
        Thread thread = threadList.get(position);
        if(!thread.getId().equals("")) {
            String title = thread.getTitle();
            holder.thread_title.setText(title);
        } else {
            holder.thread_title.setVisibility(View.GONE);
        }
        holder.thread_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(mContext, ViewSelectedThread.class);
                start.putExtra("selectedThread",thread.getId());
                mContext.startActivity(start);
            }
        });
    }
    @Override
    public int getItemCount() {
        return threadList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView thread_title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thread_title = itemView.findViewById(R.id.Title_threadItems);
        }
    }
}
