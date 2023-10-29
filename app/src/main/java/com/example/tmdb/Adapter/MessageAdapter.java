package com.example.tmdb.Adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmdb.Model.MessageModel;
import com.example.tmdb.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    MessageModel messageModel;
    private List<MessageModel> messageList;
    int ITEM_SEND=1;
    int ITEM_RECIEVE=2;

    Context context;

    public MessageAdapter(Context context,List<MessageModel> messageList) {
        this.messageList = messageList;
        this.context=context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==ITEM_SEND){
            Log.d(TAG, "sender layout"+ true);

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.senderchatlayout, parent, false);
            return new SenderViewHolder(view);


        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recieverchatlayout, parent, false);

            return new RecieverViewHolder(view);

        }



    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        messageModel=messageList.get(position);
        if(holder.getClass()==SenderViewHolder.class)
        {
            SenderViewHolder viewHolder=(SenderViewHolder)holder;
            viewHolder.textSender.setText(messageModel.getMsg());
            viewHolder.timeofmessage.setText(messageModel.getTime());
        }
        else
        {
            RecieverViewHolder viewHolder=(RecieverViewHolder)holder;
            viewHolder.textReceiver.setText(messageModel.getMsg());
            viewHolder.timeofmessage.setText(messageModel.getTime());
        }

        // holder.textSender.setText(messageModel.getMsg());

    }
/*
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
              messageModel=messageList.get(position);

        holder.textSender.setText(messageModel.getMsg());

       // holder.textMessage.setText("Message: " + message.getMsg());
        Log.d(TAG,"message adapter"+messageModel.getMsg());
    //    holder.textTimestamp.setText("Timestamp: " + message.getDateMsg());



    }

 */

    @Override
    public int getItemViewType(int position) {
        messageModel=messageList.get(position);
         if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModel.getSenderId()))
        //if (UserID.id.equals(messageModel.getSenderId()))
        {
            return  ITEM_SEND;
        }
        else
        {
            return ITEM_RECIEVE;
        }
    }

    @Override
    public int getItemCount() {
        if (messageList==null){
            Toast.makeText(context, "not found msg", Toast.LENGTH_SHORT).show();
            return 0;
        }else {

            return messageList.size();

        }
    }
/*
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textReceiver;
        TextView textMessage;
        TextView textTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textSender = itemView.findViewById(R.id.sendermessage);
          //  textMessage = itemView.findViewById(R.id.text_message);
          //  textTimestamp = itemView.findViewById(R.id.timeofmessage);
        }
    }

 */

    class SenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView textSender;

        TextView textViewmessaage;
        TextView timeofmessage;


        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textSender=itemView.findViewById(R.id.sendermessage);
            timeofmessage=itemView.findViewById(R.id.timeofmessage);
        }
    }

    class RecieverViewHolder extends RecyclerView.ViewHolder
    {
        TextView textReceiver;
        //   TextView textViewmessaage;
        TextView timeofmessage;


        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            textReceiver=itemView.findViewById(R.id.receviermessage);
            timeofmessage=itemView.findViewById(R.id.timeofmessage);
        }
    }

}