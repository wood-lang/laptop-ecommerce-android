package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan_laptop.MODEL.DonHang;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import com.example.duan_laptop.XemChiTietDonHangActivity2;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DonHangAdapter extends RecyclerView.Adapter<DonHangAdapter.ViewHolder> {
    Context context;
    ArrayList<DonHang> list;

    public DonHangAdapter(Context context, ArrayList<DonHang> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lichsu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonHang item = list.get(position);

        //  Hiển thị thông tin cơ bản
        holder.txtMaHD.setText("Đơn hàng #" + item.MaHD);
        holder.txtNgay.setText(item.NgayLap);
        holder.txtSoLuong.setText("Tổng " + item.SoLuong + " sản phẩm");

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        holder.txtGia.setText(decimalFormat.format(item.TongTien) + " Đ");

        //  Xử lý danh sách ảnh nằm ngang (Dùng chuỗi DanhSachAnh từ PHP)
        holder.layoutImages.removeAllViews(); // Xóa ảnh cũ khi cuộn
        String dsAnh = item.getDanhSachAnh();
        String dsSoLuong = item.DanhSachSoLuong;

        if (dsAnh != null && !dsAnh.isEmpty()) {
            String[] mangAnh = dsAnh.split(",");
            String[] mangSL = (dsSoLuong != null) ? dsSoLuong.split(",") : null;
            for (int i = 0; i < mangAnh.length; i++) {
                // Tạo FrameLayout để chứa Ảnh + Badge
                android.widget.FrameLayout frameContainer = new android.widget.FrameLayout(context);
                int size = (int) (70 * context.getResources().getDisplayMetrics().density);
                android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(size, size);
                layoutParams.setMargins(0, 0, 20, 0);
                frameContainer.setLayoutParams(layoutParams);

                // Tạo ImageView
                ImageView img = new ImageView(context);
                img.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String linkAnh = mangAnh[i].trim();
                // Kiểm tra nếu chưa có http thì mới nối SERVER.server_img
                if (!linkAnh.startsWith("http")) {
                    linkAnh = SERVER.server_img + linkAnh;
                }

                Picasso.get().load(linkAnh)
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(img);
                frameContainer.addView(img);

                // Thêm Badge nếu số lượng món đó > 1
                if (mangSL != null && i < mangSL.length) {
                    try {
                        String slStr = mangSL[i].trim();
                        if (!slStr.isEmpty()) {
                            int slMon = Integer.parseInt(mangSL[i].trim());
                            if (slMon > 1) {
                                TextView tvBadge = new TextView(context);
                                android.widget.FrameLayout.LayoutParams badgeParams = new android.widget.FrameLayout.LayoutParams(
                                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
                                badgeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
                                tvBadge.setLayoutParams(badgeParams);
                                tvBadge.setText("x" + slMon);
                                tvBadge.setTextColor(Color.WHITE);
                                tvBadge.setTextSize(10);
                                tvBadge.setPadding(8, 2, 8, 2);
                                tvBadge.setBackgroundColor(Color.parseColor("#CC000000")); // Nền đen mờ
                                frameContainer.addView(tvBadge);
                            }
                        }
                } catch (NumberFormatException e) {
                        e.printStackTrace(); // Nếu lỗi số thì bỏ qua, không làm chết App
                    }
                }

                holder.layoutImages.addView(frameContainer);
            }
        }

        // Xử lý trạng thái
        setupStatus(holder.tvTrangThai, item.TrangThai);
        // Khóa triệt để các View con
        holder.layoutImages.setClickable(false);
        holder.layoutImages.setFocusable(false);
        if (holder.scrollImages != null) {
            holder.scrollImages.setClickable(false);
            holder.scrollImages.setFocusable(false);
            // Quan trọng: Trên một số máy Android, ScrollView vẫn chiếm quyền chạm, dùng dòng này:
            holder.scrollImages.setOnTouchListener((v, event) -> false);
        }

        //  Click xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Log.d("CLICK_CHECK", "Đang bấm vào đơn: " + item.MaHD);
            Intent intent = new Intent(context, XemChiTietDonHangActivity2.class);
            intent.putExtra("mahd", item.MaHD);
            intent.putExtra("trangthai", item.TrangThai);
            context.startActivity(intent);
        });

    }

    private void setupStatus(TextView tv, int status) {
        switch (status) {
            case 0:
                tv.setText("CHỜ XÁC NHẬN");
                tv.setTextColor(Color.parseColor("#FF9800"));
                break;
            case 1:
            case 2:
                tv.setText("ĐANG GIAO");
                tv.setTextColor(Color.parseColor("#2196F3"));
                break;
            case 3:
                tv.setText("THÀNH CÔNG");
                tv.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case 4:
                tv.setText("ĐÃ HỦY");
                tv.setTextColor(Color.RED);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMaHD, txtNgay, txtSoLuong, txtGia, tvTrangThai;
        LinearLayout layoutImages; // layout chứa đống ảnh nhỏ
        HorizontalScrollView scrollImages;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMaHD = itemView.findViewById(R.id.tvMaDonHang);
            txtNgay = itemView.findViewById(R.id.tvNgayMua);
            txtSoLuong = itemView.findViewById(R.id.tvSoLuongDH);
            txtGia = itemView.findViewById(R.id.tvGiaDH);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            layoutImages = itemView.findViewById(R.id.layoutImages);
            scrollImages = itemView.findViewById(R.id.scrollImages);
        }
    }
}