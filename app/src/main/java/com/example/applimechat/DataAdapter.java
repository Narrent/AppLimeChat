package com.example.applimechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MessageViewHolder> {
    private Context context;
    private ArrayList<Message> messages;
    private String deviceId;

    public DataAdapter(Context context, ArrayList<Message> messages, String deviceId) {
        this.context = context;
        this.messages = messages;
        this.deviceId = deviceId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageText.setText(message.getText());

        if (message.getDeviceId().equals(deviceId)) {
            holder.messageText.setBackgroundColor(context.getResources().getColor(R.color.blue));
        } else {
            holder.messageText.setBackgroundColor(context.getResources().getColor(R.color.gray));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }
    }
}