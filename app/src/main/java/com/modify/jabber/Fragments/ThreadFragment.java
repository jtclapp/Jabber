package com.modify.jabber.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modify.jabber.Adapter.ProfileAdapter;
import com.modify.jabber.Adapter.ThreadAdapter;
import com.modify.jabber.CreatingPostActivity;
import com.modify.jabber.R;
import com.modify.jabber.model.ProfileMedia;
import com.modify.jabber.model.Thread;

import java.util.ArrayList;
import java.util.List;

public class ThreadFragment extends Fragment {
    DatabaseReference reference;
    FirebaseUser fuser;
    List<Thread> mThread;
    ThreadAdapter threadAdapter;
    RecyclerView recyclerView;
    FloatingActionButton threadButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_thread, container, false);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recycler_view_Thread);
        threadButton = view.findViewById(R.id.AddThread);

        threadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent start = new Intent(getContext(), CreatingPostActivity.class);
//                startActivity(start);
            }
        });
        readThreads();
        return view;
    }
    public void readThreads()
    {
        mThread = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Thread");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                mThread.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(count == 1000)
                    {
                        break;
                    }
                    Thread thread = dataSnapshot.getValue(Thread.class);
                    mThread.add(thread);
                    count++;
                }
                if(mThread.isEmpty())
                {
                    mThread.add(createStandIn());
                    threadAdapter = new ThreadAdapter(getContext(),mThread);
                    recyclerView.setAdapter(threadAdapter);
                }
                else {
                    threadAdapter = new ThreadAdapter(getContext(),mThread);
                    recyclerView.setAdapter(threadAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private Thread createStandIn()
    {
        return new Thread("",fuser.getUid(),"","","","");
    }
}