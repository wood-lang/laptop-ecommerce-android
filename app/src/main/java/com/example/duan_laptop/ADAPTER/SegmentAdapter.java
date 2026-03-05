package com.example.duan_laptop.ADAPTER;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.duan_laptop.MODEL.ChatSegment;
import com.example.duan_laptop.R;
import java.util.ArrayList;

public class SegmentAdapter extends RecyclerView.Adapter<SegmentAdapter.SegmentViewHolder> {
    private ArrayList<ChatSegment> segments;
    private boolean isUser; // Để biết đổi màu chữ

    public SegmentAdapter(ArrayList<ChatSegment> segments, boolean isUser) {
        this.segments = segments;
        this.isUser = isUser;
    }

    @NonNull @Override
    public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_segment, parent, false);
        return new SegmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SegmentViewHolder holder, int position) {
        ChatSegment segment = segments.get(position);

        // 1. Hiển thị Text
        holder.tvSegmentText.setText(segment.getTextContent());
        holder.tvSegmentText.setTextColor(isUser ? Color.WHITE : Color.BLACK);

        // 2. Hiển thị Ảnh (nếu có)
        if (segment.getImageUrl() != null && !segment.getImageUrl().isEmpty()) {
            holder.imgSegment.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).load(segment.getImageUrl()).into(holder.imgSegment);
        } else {
            holder.imgSegment.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return segments.size(); }

    class SegmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvSegmentText;
        ImageView imgSegment;
        public SegmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSegmentText = itemView.findViewById(R.id.tvSegmentText);
            imgSegment = itemView.findViewById(R.id.imgSegment);
        }
    }
}