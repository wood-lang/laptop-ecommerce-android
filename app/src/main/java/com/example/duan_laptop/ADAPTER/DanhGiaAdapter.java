package com.example.duan_laptop.ADAPTER;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan_laptop.MODEL.DanhGia;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;

import java.util.ArrayList;

public class DanhGiaAdapter extends RecyclerView.Adapter<DanhGiaAdapter.ViewHolder> {

    Context context;
    ArrayList<DanhGia> list;
    OnReviewActionListener listener;

    public interface OnReviewActionListener {
        void onEdit(DanhGia dg);
        void onDelete(DanhGia dg);
    }

    public DanhGiaAdapter(Context context, ArrayList<DanhGia> list, OnReviewActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_danh_gia, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DanhGia dg = list.get(position);

        holder.tvTen.setText(dg.TenKH);
        holder.tvNoiDung.setText(dg.NoiDung);
        holder.tvNgay.setText(dg.NgayDanhGia);
        holder.rb.setRating(dg.SoSao);

        // ================= XỬ LÝ NHIỀU HÌNH ẢNH =================
        holder.layoutImages.removeAllViews(); // Xóa view cũ tránh bị nhân đôi khi cuộn
        if (dg.HinhAnh != null && !dg.HinhAnh.isEmpty() && !dg.HinhAnh.equals("null")) {
            holder.scrollImages.setVisibility(View.VISIBLE);
            String[] arrImg = dg.HinhAnh.split(";"); // Băm chuỗi ra nhiều link

            for (String imgPath : arrImg) {
                if (imgPath.trim().isEmpty()) continue;

                // Tự động đẻ ra ImageView mới bằng code
                ImageView imageView = new ImageView(context);
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                        (int) (100 * context.getResources().getDisplayMetrics().density), // Rộng 100dp
                        (int) (100 * context.getResources().getDisplayMetrics().density)  // Cao 100dp
                );
                params.setMargins(0, 0, 20, 0); // Khoảng cách giữa các ảnh (marginRight = 20)
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Load ảnh bằng Glide
                String urlAnhDayDu = imgPath.startsWith("http") ? imgPath : SERVER.server_add + imgPath;
                Glide.with(context).load(urlAnhDayDu).into(imageView);

                // Nhét ảnh vào layout ngang
                holder.layoutImages.addView(imageView);
            }
        } else {
            holder.scrollImages.setVisibility(View.GONE);
        }

        // ================= XỬ LÝ NHIỀU VIDEO =================
        holder.layoutVideos.removeAllViews();
        if (dg.Video != null && !dg.Video.isEmpty() && !dg.Video.equals("null")) {
            holder.scrollVideos.setVisibility(View.VISIBLE);
            String[] arrVid = dg.Video.split(";");

            for (String vidPath : arrVid) {
                if (vidPath.trim().isEmpty()) continue;

                // 1. Khung FrameLayout bọc ngoài
                android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(context);
                android.widget.LinearLayout.LayoutParams frameParams = new android.widget.LinearLayout.LayoutParams(
                        (int) (120 * context.getResources().getDisplayMetrics().density),
                        (int) (120 * context.getResources().getDisplayMetrics().density)
                );
                frameParams.setMargins(0, 0, 20, 0);
                frameLayout.setLayoutParams(frameParams);

                // 2. Cái VideoView ở trong
                VideoView videoView = new VideoView(context);
                android.widget.FrameLayout.LayoutParams vidParams = new android.widget.FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                );
                videoView.setLayoutParams(vidParams);

                String urlVideoDayDu = vidPath.startsWith("http") ? vidPath : SERVER.server_add + vidPath;
                videoView.setVideoURI(android.net.Uri.parse(urlVideoDayDu));

                // 3. Icon Play đè lên trên Video
                ImageView playIcon = new ImageView(context);
                android.widget.FrameLayout.LayoutParams playParams = new android.widget.FrameLayout.LayoutParams(
                        (int) (40 * context.getResources().getDisplayMetrics().density),
                        (int) (40 * context.getResources().getDisplayMetrics().density)
                );
                playParams.gravity = android.view.Gravity.CENTER;
                playIcon.setLayoutParams(playParams);
                playIcon.setImageResource(android.R.drawable.ic_media_play);

                // Bắt sự kiện bấm vào Video thì phát và ẩn nút Play
                videoView.setOnClickListener(v -> {
                    if (!videoView.isPlaying()) {
                        videoView.start();
                        playIcon.setVisibility(View.GONE);
                    } else {
                        videoView.pause();
                        playIcon.setVisibility(View.VISIBLE);
                    }
                });

                // Khi video chạy hết thì hiện lại icon Play
                videoView.setOnCompletionListener(mp -> playIcon.setVisibility(View.VISIBLE));

                // Lắp ráp tụi nó lại nhét vào Frame
                frameLayout.addView(videoView);
                frameLayout.addView(playIcon);

                // Nhét cả khối Frame vào Layout ngang
                holder.layoutVideos.addView(frameLayout);
            }
        } else {
            holder.scrollVideos.setVisibility(View.GONE);
        }

        // ================= XỬ LÝ NÚT SỬA/XÓA =================
        if (SERVER.user != null && dg.SDT.equals(SERVER.user.SDT)) {
            holder.btnMenu.setVisibility(View.VISIBLE);
        } else {
            holder.btnMenu.setVisibility(View.GONE);
        }

        // TẠO MENU XỔ XUỐNG KHI BẤM NÚT 3 CHẤM
        holder.btnMenu.setOnClickListener(v -> {
            android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(context, holder.btnMenu);
            popupMenu.getMenu().add(android.view.Menu.NONE, 0, 0, "Sửa đánh giá");
            popupMenu.getMenu().add(android.view.Menu.NONE, 1, 1, "Xóa đánh giá");

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 0) {
                    listener.onEdit(dg); // Bấm Sửa
                } else if (menuItem.getItemId() == 1) {
                    listener.onDelete(dg); // Bấm Xóa
                }
                return true;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= CLASS VIEWHOLDER =================
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvNoiDung, tvNgay;
        RatingBar rb;
        ImageButton btnMenu;

        // Khai báo id mới của phần cuộn ngang
        android.widget.HorizontalScrollView scrollImages, scrollVideos;
        android.widget.LinearLayout layoutImages, layoutVideos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tvTenKHDG);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDungDG);
            tvNgay = itemView.findViewById(R.id.tvNgayDG);
            rb = itemView.findViewById(R.id.rbItem);
            btnMenu = itemView.findViewById(R.id.btnMenuDG);

            // Ánh xạ file XML
            scrollImages = itemView.findViewById(R.id.scrollImages);
            scrollVideos = itemView.findViewById(R.id.scrollVideos);
            layoutImages = itemView.findViewById(R.id.layoutImages);
            layoutVideos = itemView.findViewById(R.id.layoutVideos);
        }
    }
}