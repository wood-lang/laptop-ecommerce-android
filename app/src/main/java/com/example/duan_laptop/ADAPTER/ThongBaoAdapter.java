package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan_laptop.MODEL.ThongBao;
import com.example.duan_laptop.R;

import java.util.ArrayList;

public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ViewHolder> {

    Context context;
    ArrayList<ThongBao> list;

    public ThongBaoAdapter(Context context, ArrayList<ThongBao> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp giao diện item_thongbao.xml
        View v = LayoutInflater.from(context).inflate(R.layout.item_thongbao, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThongBao tb = list.get(position);

        // Đổ dữ liệu vào các TextView
        holder.tvTieuDe.setText(tb.TieuDe);
        holder.tvNoiDung.setText(tb.NoiDung);
        holder.tvNgay.setText(tb.NgayTao);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTieuDe, tvNoiDung, tvNgay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng ID trong item_thongbao.xml
            tvTieuDe = itemView.findViewById(R.id.tvTieuDeTB);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDungTB);
            tvNgay = itemView.findViewById(R.id.tvNgayTB);
        }
    }
}