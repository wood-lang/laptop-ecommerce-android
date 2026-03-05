package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.duan_laptop.ChiTietLaptopActivity2;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ChiTietDonHangAdapter extends RecyclerView.Adapter<ChiTietDonHangAdapter.ViewHolder> {
    Context context;
    ArrayList<laptop_model> listSP;
    int trangThaiDonHang; // 0: Chờ, 3: Thành công...
    OnDanhGiaClickListener listener;

    // Interface để gửi sự kiện click ra ngoài Activity
    public interface OnDanhGiaClickListener {
        void onDanhGiaClick(laptop_model laptop);
    }

    public ChiTietDonHangAdapter(Context context, ArrayList<laptop_model> listSP, int trangThaiDonHang, OnDanhGiaClickListener listener) {
        this.context = context;
        this.listSP = listSP;
        this.trangThaiDonHang = trangThaiDonHang;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chitiet_donhang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        laptop_model sp = listSP.get(position);
        holder.tvTen.setText(sp.TenLapTop);
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        holder.tvGia.setText(decimalFormat.format(sp.Gia) + " đ x " + sp.tempSoLuongMua); // SoLuongBan ở đây mình dùng tạm để lưu Số Lượng mua
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietLaptopActivity2.class);
            // Gửi đối tượng laptop qua trang Chi Tiết
            intent.putExtra("laptop", sp);
            context.startActivity(intent);
        });
        String urlAnh = sp.HinhAnh;
        if (!urlAnh.contains("http")) {
            urlAnh = SERVER.server_img + sp.HinhAnh; // Nối đường dẫn chứa ảnh
        }

        Glide.with(context)
                .load(urlAnh)
                .placeholder(R.drawable.no_image)
                .into(holder.imgLaptopCT);

        // LOGIC QUAN TRỌNG: Chỉ hiện nút đánh giá khi TrangThai == 3 (Giao thành công)
        if (trangThaiDonHang == 3) {
            holder.btnDanhGia.setVisibility(View.VISIBLE);
            holder.btnDanhGia.setOnClickListener(v -> listener.onDanhGiaClick(sp));
        } else {
            holder.btnDanhGia.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return listSP.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvGia;
        ImageView img;
        Button btnDanhGia;
        ImageView imgLaptopCT;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tvTenLaptopCT);
            tvGia = itemView.findViewById(R.id.tvGiaCT);
            btnDanhGia = itemView.findViewById(R.id.btnVietDanhGia);
            imgLaptopCT = itemView.findViewById(R.id.imgLaptopCT);
        }
    }
}