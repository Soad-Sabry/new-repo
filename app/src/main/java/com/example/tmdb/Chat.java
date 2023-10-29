package com.example.tmdb;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmdb.Adapter.MessageAdapter;
import com.example.tmdb.Model.MessageModel;
import com.example.tmdb.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Chat extends AppCompatActivity {

    FirebaseFirestore firestore;
    User userReceiver;
    ImageButton send;
    EditText message;
    List<MessageModel> messageModelList=new ArrayList<>();
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    ActionBar actionBar ;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        actionBar= getSupportActionBar();
        //Intent intent = getIntent();
        send=findViewById(R.id.sendIB);
        message=findViewById(R.id.messageET);

        firestore = FirebaseFirestore.getInstance();
        userReceiver =  getIntent().getParcelableExtra("user model");
        Log.d(TAG,"userRecevier"+userReceiver.getId());

        assert actionBar != null;
        actionBar.setTitle(userReceiver.getUsername());
        recyclerView=findViewById(R.id.rc_msg);
        messageAdapter=new MessageAdapter(Chat.this,messageModelList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Chat.this));
        recyclerView.setAdapter(messageAdapter);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage(message.getText().toString());
                message.setText("");

            }
        });
        getMessage();

    }

    public void getMessage(){
        if (userReceiver!= null) {

            firestore.collection("users")
                    .document(UserID.id).collection("chat").document(userReceiver.getId())
                    .collection("message").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            messageModelList.clear();

                            assert value != null;
                            //   QueryDocumentSnapshot doc;
                            for (QueryDocumentSnapshot doc : value) {
                                //MessageModel c = doc.toObject(MessageModel.class);
                                //  messageList.add(c);
                                messageModelList.add(new MessageModel(
                                        doc.getData().get("msg").toString(),
                                        doc.getData().get("dateMsg").toString(),
                                        doc.getData().get("senderId").toString(),
                                        doc.getData().get("receiverID").toString(),
                                        doc.getData().get("time").toString()));
                                Log.d(TAG,"docc "+ doc.getData().get("msg").toString());


                            }
                            messageModelList.sort(new Comparator<MessageModel>() {
                                @Override
                                public int compare(MessageModel m1, MessageModel m2) {
                                    String time1 = m1.getTime();
                                    String time2 = m2.getTime();
                                    return time1.compareTo(time2);
                                }
                            });
                            messageAdapter.notifyDataSetChanged();


                            Toast.makeText(Chat.this, "message count" + messageModelList.size(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "message count " + messageModelList.size());
                        }
                    });
        }else {
            Toast.makeText(this, "not recevier", Toast.LENGTH_SHORT).show();
        }

    }

    public void AddMessage(String msg,String first, String second,String time){

        MessageModel messageModel = new MessageModel(msg,time,UserID.id,userReceiver.getId(),time);
        CollectionReference dbChat = firestore.collection("users");

        dbChat.document(first).collection("chat").document(second)
                .collection("message")
                .add(messageModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // messageModelList.add(messageModel);
                        //messageAdapter.notifyDataSetChanged();


                        Log.d(TAG,"mmmmmmm"+msg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"excep"+e.toString());

                    }
                });


    }
    public void SendMessage(String msg){
        String time = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            time = getCurrentTime();
        }

        AddMessage(msg, UserID.id, userReceiver.getId(),time.toString());
        AddMessage(msg,  userReceiver.getId(),UserID.id,time.toString());


    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getCurrentTime() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm a");
        return currentTime.format(formatter);
    }

}