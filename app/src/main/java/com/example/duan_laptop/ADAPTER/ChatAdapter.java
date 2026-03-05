package com.example.duan_laptop.ADAPTER;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.duan_laptop.MODEL.ChatMessage;
import com.example.duan_laptop.R;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private ArrayList<ChatMessage> list;

    public ChatAdapter(ArrayList<ChatMessage> list) { this.list = list; }

    @NonNull @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = list.get(position);

        // 1. Căn lề và màu nền cho cái hộp tin nhắn
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.boxMessage.getLayoutParams();
        if (message.isUser()) {
            params.gravity = Gravity.END;
            holder.boxMessage.setBackgroundResource(R.drawable.bg_message_user);
        } else {
            params.gravity = Gravity.START;
            holder.boxMessage.setBackgroundResource(R.drawable.bg_message_ai);
        }
        holder.boxMessage.setLayoutParams(params);

        // 2. Setup RecyclerView con để hiển thị các đoạn nội dung
        holder.rcvSegments.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        // Gọi Adapter con
        SegmentAdapter segmentAdapter = new SegmentAdapter(message.getSegments(), message.isUser());
        holder.rcvSegments.setAdapter(segmentAdapter);
    }

    @Override public int getItemCount() { return list.size(); }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMsgContainer, boxMessage;
        RecyclerView rcvSegments;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMsgContainer = itemView.findViewById(R.id.layoutMsgContainer);
            boxMessage = itemView.findViewById(R.id.boxMessage);
            rcvSegments = itemView.findViewById(R.id.rcvSegments);
        }
    }
}