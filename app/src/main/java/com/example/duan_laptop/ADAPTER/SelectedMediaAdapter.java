package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan_laptop.R;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class SelectedMediaAdapter extends RecyclerView.Adapter<SelectedMediaAdapter.ViewHolder> {

    Context context;
    ArrayList<Uri> uriList;
    boolean isVideo; // Biến này để biết là đang hiển thị ảnh hay video
    OnMediaRemoveListener listener;

    // Interface để báo cho Activity biết khi nút X được bấm
    public interface OnMediaRemoveListener {
        void onRemove(int position);
    }

    public SelectedMediaAdapter(Context context, ArrayList<Uri> uriList, boolean isVideo, OnMediaRemoveListener listener) {
        this.context = context;
        this.uriList = uriList;
        this.isVideo = isVideo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_selected_media, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = uriList.get(position);
        String urlString = uri.toString();

        if (isVideo) {
            // LUÔN BẬT ICON PLAY CHO VIDEO
            holder.imgPlay.setVisibility(View.VISIBLE);

            // KIỂM TRA NGUỒN VIDEO
            if (urlString.startsWith("http")) {
                // 1. VIDEO TỪ SERVER: Dùng VideoView để tự nó buffer ra hình!
                holder.imgThumbnail.setVisibility(View.GONE); // Ẩn ImageView đi
                holder.videoMedia.setVisibility(View.VISIBLE); // Hiện VideoView lên

                holder.videoMedia.setVideoURI(uri);

                // MẸO CỰC HAY: Đợi video sẵn sàng thì tua đến mili-giây thứ 100 để nó kẹt lại và hiện cái hình con mèo lên!
                holder.videoMedia.setOnPreparedListener(mp -> {
                    holder.videoMedia.seekTo(100);
                });

            } else {
                // 2. VIDEO TỪ ĐIỆN THOẠI: Glide load thumbnail rất nhanh nên cứ xài Glide
                holder.imgThumbnail.setVisibility(View.VISIBLE);
                holder.videoMedia.setVisibility(View.GONE);

                Glide.with(context)
                        .load(uri)
                        .error(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#424242")))
                        .into(holder.imgThumbnail);
            }
        } else {
            // ẨN ICON PLAY VÀ VIDEOVIEW NẾU LÀ ẢNH
            holder.imgPlay.setVisibility(View.GONE);
            holder.videoMedia.setVisibility(View.GONE);
            holder.imgThumbnail.setVisibility(View.VISIBLE);

            // XỬ LÝ ẢNH
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.no_image)
                    .into(holder.imgThumbnail);
        }

        // Sự kiện xóa
        holder.btnRemove.setOnClickListener(v -> {
            listener.onRemove(position);
        });
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, imgPlay;
        ImageButton btnRemove;
        VideoView videoMedia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgMediaThumbnail);
            imgPlay = itemView.findViewById(R.id.imgPlayIcon);
            btnRemove = itemView.findViewById(R.id.btnRemoveMedia);
            videoMedia = itemView.findViewById(R.id.videoMediaThumbnail);
        }
    }
}