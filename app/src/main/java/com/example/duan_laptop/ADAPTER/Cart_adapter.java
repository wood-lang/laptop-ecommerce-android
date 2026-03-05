package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.Cart_manager;
import com.example.duan_laptop.HELPER.CustomDialogHelper; // THÊM IMPORT NÀY VÀO
import com.example.duan_laptop.MODEL.Cart_item;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cart_adapter extends RecyclerView.Adapter<Cart_adapter.CartViewHolder> {

    Context context;
    ArrayList<Cart_item> list;

    public Cart_adapter(Context context, ArrayList<Cart_item> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart_item item = list.get(position);

        if (item.laptop != null) {
            holder.txtTen.setText(item.laptop.TenLapTop);
            long tongGiaItem = (long) item.laptop.Gia * item.SoLuong;
            holder.txtGia.setText(NumberFormat.getNumberInstance().format(tongGiaItem) + " đ");
            String linkAnh = item.laptop.HinhAnh;
            // Kiểm tra xem chuỗi ảnh có chứa chữ "http" chưa
            if (linkAnh != null && !linkAnh.startsWith("http")) {
                // Nếu chưa có (hàng tự nhập) -> Ghép thêm link server vào
                linkAnh = SERVER.server_img + linkAnh;
            }

            Picasso.get()
                    .load(linkAnh)
                    .placeholder(R.drawable.no_image)
                    .into(holder.imgCart);
        }

        holder.txtSoLuong.setText(String.valueOf(item.SoLuong));

        // Nút Trừ
        holder.btnTru.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (item.SoLuong > 1) {
                updateQuantityOnServer(item.laptop.MaLapTop, item.SoLuong - 1, pos);
            }
        });

        // Nút Cộng
        holder.btnCong.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            Cart_item itemHienTai = list.get(pos); // Lấy object mới nhất từ list

            Log.d("DEBUG_CART", "Mua: " + itemHienTai.SoLuong + " | Kho: " + itemHienTai.laptop.SoLuong);

            if (itemHienTai.SoLuong < itemHienTai.laptop.SoLuong) {
                updateQuantityOnServer(itemHienTai.laptop.MaLapTop, itemHienTai.SoLuong + 1, pos);
            } else {
                Toast.makeText(context, "Kho chỉ còn " + itemHienTai.laptop.SoLuong + " máy!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnXoa.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();

            // Khởi tạo CustomDialogHelper
            CustomDialogHelper dialogHelper = new CustomDialogHelper(context);

            // Gọi hàm showWarning (Dialog màu Cam)
            dialogHelper.showWarning(
                    "Xóa sản phẩm",
                    "Bồ có chắc muốn bỏ sản phẩm này khỏi giỏ hàng không?",
                    new CustomDialogHelper.DialogActionListener() {
                        @Override
                        public void onPositiveClick() {
                            // Gọi hàm xóa trên server
                            xoaSanPhamTrenServer(item.laptop.MaLapTop, pos);
                        }
                    }
            );
        });
    }

    private void updateQuantityOnServer(int maLaptop, int soLuongMoi, int position) {
        if (SERVER.user == null) return;
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_update_so_luong_gio_hang,
                response -> {
                    if (response.trim().equals("success")) {
                        Cart_manager.updateQuantity(position, soLuongMoi);
                        notifyItemChanged(position);
                    }
                }, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maLaptop", String.valueOf(maLaptop));
                p.put("sdt", SERVER.user.SDT);
                p.put("soLuong", String.valueOf(soLuongMoi));
                return p;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    private void xoaSanPhamTrenServer(int maLaptop, int position) {
        if (SERVER.user == null) return;
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_xoa_sp_gio_hang,
                response -> {
                    if (response.trim().equals("success")) {
                        Cart_manager.removeFromCart(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                    }
                }, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maLaptop", String.valueOf(maLaptop));
                p.put("sdt", SERVER.user.SDT);
                return p;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCart;
        TextView txtTen, txtGia, txtSoLuong;
        ImageButton btnTru, btnCong, btnXoa;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCart = itemView.findViewById(R.id.imgCart);
            txtTen = itemView.findViewById(R.id.txtTen);
            txtGia = itemView.findViewById(R.id.txtGia);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            btnTru = itemView.findViewById(R.id.btnTru);
            btnCong = itemView.findViewById(R.id.btnCong);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }
}