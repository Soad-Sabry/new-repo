package com.example.tmdb.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Map;

public class MessageModel implements Parcelable {

    private String msg;
    private String dateMsg;
    private String senderId;
    private String receiverID;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public MessageModel(String msg) {
        this.msg=msg;
    }


    public String getMsg() {

        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDateMsg() {

        return dateMsg;
    }

    public void setDateMsg(String dateMsg) {
        this.dateMsg = dateMsg;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public MessageModel(Map<String, Object> json)
    {
        this.msg = json.get("msg").toString();
        this.dateMsg = json.get("dateMsg").toString();
        this.senderId = json.get("senderId").toString();
        this.receiverID = json.get("receiverID").toString();
    }

    public MessageModel(String msg, String dateMsg, String senderId, String receiverID,String time) {
        this.msg = msg;
        this.dateMsg = dateMsg;
        this.senderId = senderId;
        this.receiverID = receiverID;
        this.time=time;

    }

    public MessageModel() {
        this.msg = msg;
        this.dateMsg = dateMsg;
        this.senderId = senderId;
        this.receiverID = receiverID;
    }

    protected MessageModel(Parcel in) {
        msg = in.readString();
        dateMsg = in.readString();
        senderId = in.readString();
        receiverID = in.readString();
        time=in.readString();
    }

    public static final Parcelable.Creator<MessageModel> CREATOR = new Parcelable.Creator<MessageModel>() {
        @Override
        public MessageModel createFromParcel(Parcel in) {
            return new MessageModel(in);
        }

        @Override
        public MessageModel[] newArray(int size) {
            return new MessageModel[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(msg);
        dest.writeString(dateMsg);
        dest.writeString(senderId);
        dest.writeString(receiverID);
        dest.writeString(time);
    }

}
