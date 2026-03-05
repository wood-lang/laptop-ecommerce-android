package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ChiTietLaptopActivity2;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LAPTOP_ADAPTER extends RecyclerView.Adapter<LAPTOP_ADAPTER.LaptopViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteAdded(laptop_model laptop);
        void onFavoriteRemoved(laptop_model laptop);
    }

    private final Context context;
    private java.util.List<laptop_model> list;
    private final OnFavoriteClickListener listener;

    public LAPTOP_ADAPTER(java.util.List<laptop_model> list, Context context, OnFavoriteClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LaptopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_laptop, parent, false);
        return new LaptopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LaptopViewHolder holder, int position) {
        laptop_model lap = list.get(position);

        // Hiển thị tên (cắt ngắn nếu quá dài)
        holder.tvTen.setText(lap.TenLapTop.length() > 25 ? lap.TenLapTop.substring(0, 25) + "..." : lap.TenLapTop);

        // Hiển thị giá
        holder.tvGia.setText(NumberFormat.getInstance().format(lap.Gia) + " VNĐ");

        // Load ảnh
        //Lấy link từ DB và dọn dẹp khoảng trắng
        String linkTuDB = lap.HinhAnh.trim();
        String finalUrl = "";

        //LOGIC KIỂM TRA (Sửa lỗi dính 2 link)
        // Nếu link đã có sẵn chữ "http" (tức là link cào từ Web) thì KHÔNG cộng thêm SERVER.server_img
        if (linkTuDB.startsWith("http")) {
            finalUrl = linkTuDB;
        } else {
            // Chỉ cộng SERVER.server_img khi ảnh đó là file tự lưu trên localhost
            finalUrl = SERVER.server_img + linkTuDB;
        }

        // 3.ĐỂ SOI LINK CUỐI CÙNG TRƯỚC KHI ĐƯA VÀO PICASSO
        Log.d("SoiLink", "Link ảnh cuối cùng để load: [" + finalUrl + "]");

        // 4. Load ảnh bằng cái biến finalUrl đã được xử lý chuẩn
        Picasso.get()
                .load(finalUrl)
                .placeholder(R.drawable.no_image) // Ảnh chờ lúc đang tải
                .error(R.drawable.no_image)       // Ảnh lỗi nếu tải thất bại
                .fit().centerCrop()
                .into(holder.imgLaptop);

        // Kiểm tra trạng thái yêu thích
        boolean isFav = false;
        try {
            // Giữ nguyên logic ép kiểu của bồ
            isFav = SERVER.isFavorite(Integer.parseInt(String.valueOf(lap.MaLapTop)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Đổi icon tim
        holder.imgFavorite.setImageResource(
                isFav ? R.drawable.ic_baseline_favorite_24
                        : R.drawable.ic_baseline_favorite_border_24
        );

        // Click item -> Chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietLaptopActivity2.class);
            intent.putExtra("laptop", (Serializable) lap);
            context.startActivity(intent);
        });

        // Click tim (favorite)
        holder.imgFavorite.setOnClickListener(v -> {
            // Check lại trạng thái hiện tại để gọi hàm đúng
            boolean currentFav = SERVER.isFavorite(lap.MaLapTop);
            if (currentFav) removeFavorite(lap, holder.getBindingAdapterPosition());
            else addFavorite(lap, holder.getBindingAdapterPosition());
        });
    }

    public void updateList(ArrayList<laptop_model> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    private void addFavorite(laptop_model lap, int pos){
        if(SERVER.user == null) {
            Toast.makeText(context,"Bạn cần đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, SERVER.url_addFavorite,
                response -> {
                    // 1. Đổi trạng thái hiển thị tim đỏ
                    lap.isFavorite = true;

                    // 2. Cập nhật vào list chung trong SERVER
                    SERVER.addFavoriteToList(lap.MaLapTop);

                    // 3. Cập nhật giao diện Item này
                    notifyItemChanged(pos);

                    // 4. [MỚI] BÁO CHO MAIN ACTIVITY CẬP NHẬT BADGE SỐ ĐỎ NGAY LẬP TỨC
                    SERVER.notifyFavoriteChanged();

                    // 5. Gọi fragment cập nhật list nếu đang mở
                    SERVER.updateFavoriteFragment(lap, true);

                    if(listener != null) listener.onFavoriteAdded(lap);
                },
                error -> Toast.makeText(context,"Lỗi mạng", Toast.LENGTH_SHORT).show()
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> map = new HashMap<>();
                map.put("SDT", SERVER.user.SDT != null ? SERVER.user.SDT : SERVER.user.Email); // Fix nhẹ: Lấy cả Email nếu SDT rỗng
                map.put("MaLaptop", String.valueOf(lap.MaLapTop));
                return map;
            }
        };
        Volley.newRequestQueue(context).add(req);
    }

    private void removeFavorite(laptop_model lap, int pos){
        if(SERVER.user == null) return;

        StringRequest req = new StringRequest(Request.Method.POST, SERVER.url_removeFavorite,
                response -> {
                    // 1. Đổi trạng thái hiển thị tim trắng
                    lap.isFavorite = false;

                    // 2. Xóa khỏi list chung trong SERVER
                    SERVER.removeFavoriteFromList(lap.MaLapTop);

                    // 3. Cập nhật giao diện Item này
                    notifyItemChanged(pos);

                    // 4. [MỚI] BÁO CHO MAIN ACTIVITY GIẢM BADGE SỐ ĐỎ NGAY LẬP TỨC
                    SERVER.notifyFavoriteChanged();

                    // 5. Gọi fragment cập nhật list
                    SERVER.updateFavoriteFragment(lap, false);

                    if(listener != null) listener.onFavoriteRemoved(lap);
                },
                error -> Toast.makeText(context,"Lỗi mạng", Toast.LENGTH_SHORT).show()
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> map = new HashMap<>();
                map.put("SDT", SERVER.user.SDT != null ? SERVER.user.SDT : SERVER.user.Email); // Fix nhẹ
                map.put("MaLaptop", String.valueOf(lap.MaLapTop));
                return map;
            }
        };
        Volley.newRequestQueue(context).add(req);
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class LaptopViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLaptop, imgFavorite;
        TextView tvTen, tvGia;
        public LaptopViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLaptop = itemView.findViewById(R.id.imgLapTop);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvTen = itemView.findViewById(R.id.tvTenLaptop);
            tvGia = itemView.findViewById(R.id.tvGia);
        }
    }
}