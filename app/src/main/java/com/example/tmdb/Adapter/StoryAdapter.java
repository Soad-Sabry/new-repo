package com.example.tmdb.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tmdb.AddStoryActivity;
import com.example.tmdb.Model.Story;
import com.example.tmdb.Model.User;
import com.example.tmdb.R;
import com.example.tmdb.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private Context mContext;
    private List<Story> mStory;

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0){
            View view= LayoutInflater.from(mContext).inflate(R.layout.add_story_item,parent,false);
            return new StoryAdapter.ViewHolder(view);
        }else {
            View view= LayoutInflater.from(mContext).inflate(R.layout.story_item,parent,false);
            return new StoryAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Story story=mStory.get(position);
        userInfo(holder,story.getUserid(),position);
         if (holder.getAdapterPosition() !=0){
             seenStory(holder,story.getUserid());
         }
        if (holder.getAdapterPosition() ==0) {
            myStory(holder.add_story_text,holder.story_plus,false);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() ==0) {
                    myStory(holder.add_story_text,holder.story_photo,true);
                }else {

                    Intent intent=new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid",story.getUserid());
                    mContext.startActivity(intent);


                }
                }
        });

        }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
    public ImageView story_photo,story_plus,story_photo_seen;
    public TextView story_username,add_story_text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            story_photo=itemView.findViewById(R.id.story_photo);
            story_plus=itemView.findViewById(R.id.story_plus);
            story_photo_seen=itemView.findViewById(R.id.story_photo_seen);
            story_username=itemView.findViewById(R.id.story_username);
            add_story_text=itemView.findViewById(R.id.addstory_text);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0){
            return 0;
        }
        return 1;
    }

    private void userInfo(ViewHolder viewHolder,String userId,int pos){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(viewHolder.story_photo);
                if (pos !=0){
                    Glide.with(mContext).load(user.getImageurl()).into(viewHolder.story_photo_seen);
                    viewHolder.story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void myStory(TextView textView,ImageView imageView,boolean click){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                int count=0;
                long timecurrent=System.currentTimeMillis();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Story story=dataSnapshot.getValue(Story.class);
                    if (timecurrent>story.getTimestart() && timecurrent<story.getTimeend()){
                        count++;
                    }
                }
                if (click){
                    if (count>0)
                    {
                        AlertDialog alertDialog=new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO go to story
                                Intent intent=new Intent(mContext, StoryActivity.class);
                                intent.putExtra("userid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mContext.startActivity(intent);
                                dialog.dismiss();

                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(mContext, AddStoryActivity.class);
                                mContext.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                    else {
                        Intent intent=new Intent(mContext, AddStoryActivity.class);
                        mContext.startActivity(intent);

                    }


                }else {
                    if (count>0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    }else {
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void  seenStory(ViewHolder viewHolder,String userId){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story")
                .child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              int i=0;
              for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                  if (!dataSnapshot.child("views")
                          .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()
                  && System.currentTimeMillis()<dataSnapshot.getValue(Story.class).getTimeend()){
                      i++;
                  }
              }
              if (i>0){
                  viewHolder.story_photo.setVisibility(View.VISIBLE);
                  viewHolder.story_photo_seen.setVisibility(View.GONE);
              }else {
                  viewHolder.story_photo.setVisibility(View.GONE);
                  viewHolder.story_photo_seen.setVisibility(View.VISIBLE);

              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
