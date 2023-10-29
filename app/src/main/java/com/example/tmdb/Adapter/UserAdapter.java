package com.example.tmdb.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tmdb.MainActivity;
import com.example.tmdb.Model.User;
import com.example.tmdb.R;
import com.example.tmdb.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
private Context context;
private List<User>users=new ArrayList<>();
    private boolean isfragment;
private FirebaseUser firebaseUser;

    public UserAdapter(Context context, List<User> users,boolean isfragment) {
        this.context = context;
        this.users = users;
        this .isfragment=isfragment;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        final User user=users.get(position);
        holder.btn_follow.setVisibility(View.VISIBLE);
        holder.userName.setText(user.getUsername());
        holder.fullName.setText(user.getFullname());

        Glide.with(context).load(user.getImageurl()).into(holder.imageProfile);
        isFollowing(user.getId(),holder.btn_follow);
        if (user.getId().equals(firebaseUser.getUid())){
           holder.btn_follow.setVisibility(View.GONE);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isfragment) {


                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFERE", Context.MODE_PRIVATE).edit();
                    editor.putString("profiled", user.getId());
                    editor.apply();
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                }
                Intent intent=new Intent(context, MainActivity.class);
                intent.putExtra("publisherid",user.getId());
                context.startActivity(intent);


            }
        });
        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.btn_follow.getText().toString().equals("follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    addNotification(user.getId());

                  //  addNotification(user.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }

        });
    }


    @Override
    public int getItemCount() {
        int size;
        if (users != null) {
             size = users.size(); // Only call size() when the list is not null
        } else {
            // Handle the case where the list is null
            size=0;
        }
        return size;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
public TextView userName;
public TextView fullName;
private CircleImageView imageProfile;
public Button btn_follow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            fullName = itemView.findViewById(R.id.fullname);
            imageProfile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }
    private void addNotification (String userid){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Notification").child(userid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","Starting following you");
        hashMap.put("postid","");
        hashMap.put("ispost",false);
        reference.push().setValue(hashMap);

    }

    private  void  isFollowing(String userId,Button button){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference()
                .child("follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userId).exists()){
                    button.setText("following");
                }else {
                    button.setText("follow");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
