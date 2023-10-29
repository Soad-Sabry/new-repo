package com.example.tmdb.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmdb.Adapter.NotivigationAdapter;
import com.example.tmdb.Model.Notivigation;
import com.example.tmdb.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotivigationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotivigationAdapter notivigationAdapter;
    private List<Notivigation> notivigationList;

    public NotivigationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_notivigation, container, false);

        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        notivigationList=new ArrayList<>();
        notivigationAdapter=new NotivigationAdapter(getContext(),notivigationList);
        recyclerView.setAdapter(notivigationAdapter);

        readNotification();


        return view;
    }
    private void readNotification(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Notification").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               notivigationList.clear();
               for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                   Notivigation notivigation=dataSnapshot.getValue(Notivigation.class);
                   notivigationList.add(notivigation);
               }

                Collections.reverse(notivigationList);
               notivigationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}