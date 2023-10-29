package com.example.tmdb.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmdb.Adapter.PostAdapter;
import com.example.tmdb.Adapter.StoryAdapter;
import com.example.tmdb.Model.Post;
import com.example.tmdb.Model.Story;
import com.example.tmdb.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story>storyList;
    private List<String> followingList;

    ProgressBar progress_circular;



    public HomeFragment() {
        // Required empty public constructor
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mlinearLayoutManager=new LinearLayoutManager(getContext());
        mlinearLayoutManager.setReverseLayout(true);
        mlinearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mlinearLayoutManager);
        postList=new ArrayList<>();
        postAdapter=new PostAdapter(getContext(),postList);
        recyclerView.setAdapter(postAdapter);

        recyclerView_story=view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1=new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerView_story.setLayoutManager(linearLayoutManager1);
        storyList=new ArrayList<>();
        storyAdapter=new StoryAdapter(getContext(),storyList);
        recyclerView_story.setAdapter(storyAdapter);
        progress_circular = view.findViewById(R.id.progress_circular);

        checkFollowing();


        return view;
    }
    private void checkFollowing(){
        followingList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                readPosts();
                readStory();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readPosts(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Post post = snapshot1.getValue(Post.class);
                    for (String id : followingList){
                        if (post.getPublisher().equals(id)){
                            postList.add(post);
                        }
                    }
                }

                postAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readStory(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timeCurrent=System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("",0,0,"",FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    for (String id: followingList){
                        int countStory=0;
                        Story story=null;
                        for (DataSnapshot dataSnapshot: snapshot.child(id).getChildren()){
                            story=dataSnapshot.getValue(Story.class);
                            if (timeCurrent>story.getTimestart() && timeCurrent<story.getTimeend()){
                                countStory++;
                            }
                        }
                        if(countStory>0){
                           storyList.add(story);
                        }
                    }
                    storyAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}