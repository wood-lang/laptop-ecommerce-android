package com.example.duan_laptop;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.DonHangAdapter;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.MODEL.DonHang;
import com.example.duan_laptop.databinding.ActivityLichSuDonHangBinding;
import com.r0adkll.slidr.Slidr;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LichSuDonHangActivity extends AppCompatActivity {
    ActivityLichSuDonHangBinding binding;

    RecyclerView rcvLichSu;
    DonHangAdapter adapter;
    ArrayList<DonHang> mangDonHang;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su_don_hang);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbarLS);
        rcvLichSu = findViewById(R.id.rcvLichSu);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        mangDonHang = new ArrayList<>();
        adapter = new DonHangAdapter(this, mangDonHang);
        rcvLichSu.setLayoutManager(new LinearLayoutManager(this));
        rcvLichSu.setAdapter(adapter);

        // CÀI ĐẶT HIỆU ỨNG VUỐT XÓA (CÓ NỀN ĐỎ + THÙNG RÁC)
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                DonHang donHangBiVuot = mangDonHang.get(position);
                if (donHangBiVuot.getTrangThai() == 3) {
                    // NẾU ĐÃ GIAO THÀNH CÔNG -> Cho hiện Dialog xác nhận xóa
                    new CustomDialogHelper(LichSuDonHangActivity.this).showWarning(
                            "Xóa lịch sử",
                            "Bạn có chắc chắn muốn xóa đơn hàng #" + donHangBiVuot.getMaHD() + " vĩnh viễn không?",
                            new CustomDialogHelper.DialogActionListener() {
                                @Override
                                public void onPositiveClick() {
                                    xoaDonHangTrenServer(donHangBiVuot.getMaHD(), position);
                                }
                            }
                    );
                    adapter.notifyItemChanged(position);
                } else {
                    // NẾU CHƯA GIAO XONG -> Không cho xóa, bắt item bật ngược lại và thông báo
                    adapter.notifyItemChanged(position);
                    Toast.makeText(LichSuDonHangActivity.this, "Chỉ có thể xóa đơn hàng đã giao thành công!", Toast.LENGTH_SHORT).show();
                }
            }

            // Hàm này dùng để vẽ nền đỏ và cái thùng rác khi ngón tay đang vuốt
            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                // 1. Lấy vị trí của item đang bị tác động
                int position = viewHolder.getAdapterPosition();

                // 2. Kiểm tra trạng thái: Nếu KHÔNG PHẢI trạng thái 3 (thành công) thì khóa dX = 0
                if (position != RecyclerView.NO_POSITION) {
                    DonHang item = mangDonHang.get(position);
                    if (item.getTrangThai() != 3) {
                        dX = 0; // Ép độ dài vuốt bằng 0 (khóa cứng, không cho kéo sang trái)
                    }
                }

                // 3. Gọi lại super với dX đã được xử lý
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                // Chỉ vẽ nền đỏ và icon nếu dX < 0 (nghĩa là chỉ khi dX khác 0 do lệnh ở trên)
                View itemView = viewHolder.itemView;
                if (dX < 0) {
                    android.graphics.drawable.ColorDrawable background = new android.graphics.drawable.ColorDrawable(android.graphics.Color.RED);
                    background.setBounds(itemView.getRight() + ((int) dX) - 20, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat.getDrawable(LichSuDonHangActivity.this, android.R.drawable.ic_menu_delete);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    }
                }
            }
        };

        // Gắn thao tác vuốt vào rcvLichSu
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rcvLichSu);
    }

    // Hàm này chạy mỗi khi màn hình hiện lên (kể cả khi quay lại từ màn hình Chi tiết)
    @Override
    protected void onResume() {
        super.onResume();
        loadLichSu(); // Gọi hàm load lại dữ liệu ở đây
    }

    private void loadLichSu() {
        // Lấy SDT từ User đã đăng nhập
        if (SERVER.user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }
        String sdt = SERVER.user.SDT;

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_get_lich_su_giaodich,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangDonHang.clear();
                        Log.d("KiemTraJSON", response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            int trangThai = obj.optInt("TrangThai", 0);
                            int tongSoLuong = 0;
                            String dsSoLuong = obj.optString("DanhSachSoLuong", "");
                            if (!dsSoLuong.isEmpty()) {
                                String[] mangSL = dsSoLuong.split(",");
                                for (String sl : mangSL) {
                                    try {
                                        tongSoLuong += Integer.parseInt(sl.trim());
                                    } catch (Exception ignored) {} // Lỗi ép kiểu thì bỏ qua
                                }
                            }
                            mangDonHang.add(new DonHang(
                                    obj.getInt("MaHD"),
                                    obj.getString("NgayLap"),
                                    "",
                                    obj.getLong("TongTien"),
                                    "",
                                    tongSoLuong,
                                    trangThai,
                                    0,
                                    obj.getString("DanhSachAnh"),
                                    obj.getString("DanhSachSoLuong")
                            ));
                        }
                        adapter.notifyDataSetChanged();

                        if(mangDonHang.size() == 0){
                            Toast.makeText(this, "Chưa có đơn hàng nào", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e("LoiJSON", e.getMessage());
                    }
                },
                error -> Toast.makeText(this, "Lỗi mạng", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("SDT", sdt); // Gửi SDT lên để server lọc đơn hàng
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void xoaDonHangTrenServer(int maHD, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_xoa_lichsu,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(LichSuDonHangActivity.this, "Đã xóa lịch sử giao dịch", Toast.LENGTH_SHORT).show();

                        // Xóa khỏi danh sách ảo trên App và báo cho Adapter biết
                        mangDonHang.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        Toast.makeText(LichSuDonHangActivity.this, "Xóa thất bại!", Toast.LENGTH_SHORT).show();
                        // Lỗi thì cho item bật ngược lại
                        adapter.notifyItemChanged(position);
                    }
                },
                error -> {
                    Toast.makeText(LichSuDonHangActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mahd", String.valueOf(maHD));
                return params;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }
}