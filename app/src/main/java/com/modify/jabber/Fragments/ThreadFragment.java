package com.modify.jabber.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.modify.jabber.Adapter.ThreadAdapter;
import com.modify.jabber.R;

public class ThreadFragment extends Fragment {
    DatabaseReference reference;
    FirebaseUser fuser;
    ThreadAdapter threadAdapter;
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_thread, container, false);
    }
}