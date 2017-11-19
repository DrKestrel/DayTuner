package com.netherbench.daytuner;

import android.app.Notification;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 6/26/2016.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder>{

    public static class MyViewHolder extends RecyclerView.ViewHolder  {
        CardView cv;
        TextView textNotification;
        EditText editTextNotification;
        Switch switchActive;


        MyViewHolder(View itemView){
            super (itemView);
            cv = (CardView)itemView.findViewById(R.id.notification_card);
            this.switchActive = (Switch) itemView.findViewById(R.id.notification_switch);
            this.textNotification = (TextView)itemView.findViewById(R.id.notification_textView);
            this.editTextNotification = (EditText) itemView.findViewById(R.id.notification_editText);
        }
    }
    List<NotificationData> notifications;

    NotificationAdapter(List<NotificationData> notifications){
        this.notifications= notifications;
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        holder.textNotification.setText(notifications.get(listPosition).notification_text);
        holder.editTextNotification.setText(notifications.get(listPosition).notification_time);
        holder.switchActive.setChecked(notifications.get(listPosition).notification_active);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

}