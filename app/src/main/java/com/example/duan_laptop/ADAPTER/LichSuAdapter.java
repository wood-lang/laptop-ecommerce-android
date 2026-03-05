package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan_laptop.MODEL.LichSuModel;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;

public class LichSuAdapter extends RecyclerView.Adapter<LichSuAdapter.ViewHolder> {

    Context context;
    ArrayList<LichSuModel> list;

    public LichSuAdapter(Context c, ArrayList<LichSuModel> l) {
        context = c;
        list = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_lichsu, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        LichSuModel m = list.get(i);

        // Vì đơn hàng có thể chứa nhiều laptop khác nhau, hiển thị Mã là chuẩn nhất
        h.tvTen.setText(m.TenLapTop);

        //  NGÀY ĐẶT
        h.tvNgay.setText("Ngày đặt: " + m.NgayLap);

        //  TỔNG TIỀN (Lấy TongTien từ Model, không phải DonGia)
        h.tvGia.setText("Tổng thanh toán: " + NumberFormat.getInstance().format(m.TongTien) + " đ");

        // SỐ LƯỢNG
        if (m.SoLuong > 1) {
            // Nếu đơn có nhiều món -> Hiện "Macbook... và 2 món khác"
            h.tvSoLuong.setText("Và " + (m.SoLuong - 1) + " sản phẩm khác");
        } else {
            h.tvSoLuong.setText("Số lượng: 1");
        }

        //XỬ LÝ TRẠNG THÁI
        // Cần đảm bảo Model có biến TrangThai (int)
        String trangThaiText = "";
        int color = Color.BLACK;

        switch (m.TrangThai) {
            case 0:
                trangThaiText = "Chờ xác nhận";
                color = Color.parseColor("#FF9800"); // Cam
                break;
            case 1:
                trangThaiText = "Đang đóng gói";
                color = Color.parseColor("#2196F3"); // Xanh
                break;
            case 2:
                trangThaiText = "Đang giao";
                color = Color.parseColor("#2196F3"); // Xanh
                break;
            case 3:
                trangThaiText = "Giao thành công";
                color = Color.parseColor("#4CAF50"); // Xanh lá
                break;
            case 4:
                trangThaiText = "ĐÃ HỦY";
                color = Color.RED; // Đỏ
                break;
        }

        h.tvTrangThai.setText(trangThaiText);
        h.tvTrangThai.setTextColor(color);
        if (m.TrangThai == 4) {
            h.tvTrangThai.setTypeface(null, Typeface.BOLD);
        } else {
            h.tvTrangThai.setTypeface(null, Typeface.NORMAL);
        }

        // 6. HÌNH ẢNH
        if (m.HinhAnh != null && !m.HinhAnh.isEmpty()) {
            String linkAnh = m.HinhAnh;
            if (!linkAnh.startsWith("http")) {
                linkAnh = SERVER.server_img + linkAnh;
            }

            Picasso.get().load(linkAnh)
                    .placeholder(R.drawable.no_image)
                    .into(h.img);
        } else {
            h.img.setImageResource(R.drawable.no_image);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTen, tvNgay, tvSoLuong, tvGia, tvTrangThai; // Thêm tvTrangThai
        LinearLayout layoutImages;

        ViewHolder(View v) {
            super(v);
            layoutImages = itemView.findViewById(R.id.layoutImages);
            tvNgay = v.findViewById(R.id.tvNgayMua);
            tvSoLuong = v.findViewById(R.id.tvSoLuongDH);
            tvGia = v.findViewById(R.id.tvGiaDH);
            tvTrangThai = v.findViewById(R.id.tvTrangThai);
        }
    }
}