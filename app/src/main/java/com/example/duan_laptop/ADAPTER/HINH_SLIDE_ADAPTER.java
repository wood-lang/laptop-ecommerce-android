package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.Target;
import com.example.duan_laptop.ChiTietLaptopActivity2;
import com.example.duan_laptop.MODEL.HINH_SLIDE_MODEL;
import com.example.duan_laptop.R;

import java.util.ArrayList;

public class HINH_SLIDE_ADAPTER extends RecyclerView.Adapter<HINH_SLIDE_ADAPTER.SlideViewHolder> {

    private Context context;
    private ArrayList<HINH_SLIDE_MODEL> list;

    public HINH_SLIDE_ADAPTER(Context context, ArrayList<HINH_SLIDE_MODEL> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anh, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        HINH_SLIDE_MODEL item = list.get(position);
        Glide.with(context)
                .load(item.getHinhchitiet())
                // Ép dùng hệ màu 32-bit (mặc định là 16-bit bị mờ)
                .format(DecodeFormat.PREFER_ARGB_8888)
                // Load kích thước gốc của ảnh từ server, không cho Android tự nén nhỏ lại
                .override(Target.SIZE_ORIGINAL)
                // CenterCrop đè lên scaleType của XML để chắc chắn lấp đầy
                .centerCrop()
                .into(holder.imgSlide);
        holder.imgSlide.setOnClickListener(v -> {
            if (context instanceof ChiTietLaptopActivity2) {
                // Gọi hàm Zoom ảnh bên Activity
                ((ChiTietLaptopActivity2) context).showZoomImageDialog(item.getHinhchitiet());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSlide;

        public SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSlide = itemView.findViewById(R.id.imghinh_slide);
        }
    }
}