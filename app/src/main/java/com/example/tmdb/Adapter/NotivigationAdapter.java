package com.example.tmdb.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tmdb.Model.Notivigation;
import com.example.tmdb.Model.Post;
import com.example.tmdb.Model.User;
import com.example.tmdb.R;
import com.example.tmdb.fragment.PostDetailFragment;
import com.example.tmdb.fragment.ProfileFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotivigationAdapter extends RecyclerView.Adapter<NotivigationAdapter.ViewHolder> {
private Context mContext;

private List<Notivigation>mNotivigation;

    public NotivigationAdapter(Context mContext, List<Notivigation> mNotivigation) {
        this.mContext = mContext;
        this.mNotivigation = mNotivigation;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.notivigation_item,parent,false);
        return new NotivigationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notivigation notivigation=mNotivigation.get(position);
        holder.text.setText(notivigation.getText());

        getUserInfo(holder.image_profile,holder.username,notivigation.getUserid());

        if (notivigation.isIspost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image,notivigation.getPostid());
        }
        else {
            holder.post_image.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notivigation.isIspost()){
                    SharedPreferences.Editor editor= (SharedPreferences.Editor) mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE);
                    editor.putString("postid",notivigation.getPostid());
                    editor.apply();
                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,new PostDetailFragment());


                }else {

                    SharedPreferences.Editor editor= (SharedPreferences.Editor) mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE);
                    editor.putString("profileId",notivigation.getUserid());
                    editor.apply();
                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,new ProfileFragment()).commit();


                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mNotivigation.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        public ImageView image_profile, post_image;
        public TextView username, text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo(ImageView imageView,TextView username,String publisherid){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(publisherid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user= snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPostImage(ImageView imageView,String postid){

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post=snapshot.getValue(Post.class);
                Glide.with(mContext).load(post.getPostimage()).into(imageView);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
