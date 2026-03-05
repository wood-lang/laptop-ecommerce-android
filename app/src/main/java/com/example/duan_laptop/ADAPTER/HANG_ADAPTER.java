package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan_laptop.LaptopTheoHangActivity2;
import com.example.duan_laptop.MODEL.HANG_MODEL;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HANG_ADAPTER extends RecyclerView.Adapter<HANG_ADAPTER.HangViewHolder> {

    Context context;
    ArrayList<HANG_MODEL> arrHang;

    public HANG_ADAPTER(Context context, ArrayList<HANG_MODEL> arrHang) {
        this.context = context;
        this.arrHang = arrHang;
    }

    @NonNull
    @Override
    public HangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hang, parent, false);
        return new HangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HangViewHolder holder, int position) {
        HANG_MODEL hang = arrHang.get(position);

        // Load logo hãng
        Picasso.get()
                .load(SERVER.server_logo + hang.logo)
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(holder.imgHang);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LaptopTheoHangActivity2.class);

            // Gửi mật thư: Truyền đúng cái mã hãng mà khách vừa bấm
            intent.putExtra("MACHUDE", hang.masx);

            // Gửi mật thư: Báo cho trang kia biết là KHÔNG lọc theo bán chạy hay đồ mới
            intent.putExtra("LOAILAPTOP", "");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return arrHang.size();
    }

    static class HangViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHang;
        public HangViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHang = itemView.findViewById(R.id.imgHang);
        }
    }
}
