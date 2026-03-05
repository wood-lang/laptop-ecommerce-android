package com.example.duan_laptop.FRAGMENT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.ThongBaoAdapter;
import com.example.duan_laptop.MODEL.ThongBao;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThongBao_fragment extends Fragment {

    RecyclerView rcvThongBao;
    ThongBaoAdapter adapter;
    ArrayList<ThongBao> mangThongBao;

    // --- BỘ THU SÓNG: Lắng nghe khi có thông báo rớt xuống ---
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Khi Firebase bắn thông báo, nó sẽ kích hoạt hàm này -> Gọi load lại ngay!
            loadThongBao();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Bồ nhớ tự tạo một file layout tên là fragment_thongbao_layout.xml
        // Trong đó chứa duy nhất 1 cái RecyclerView tên là rcvThongBao nhé
        return inflater.inflate(R.layout.fragment_thongbao_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rcvThongBao = view.findViewById(R.id.rcvThongBao);
        mangThongBao = new ArrayList<>();
        adapter = new ThongBaoAdapter(getContext(), mangThongBao);
        rcvThongBao.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvThongBao.setAdapter(adapter);
        // TẠO HIỆU ỨNG VUỐT TRÁI ĐỂ XÓA THÔNG BÁO (CÓ NỀN ĐỎ + THÙNG RÁC)
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback swipeCallback =
                new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        ThongBao tbBiVuot = mangThongBao.get(position);
                            // HIỆN CUSTOM DIALOG XÁC NHẬN
                        com.example.duan_laptop.HELPER.CustomDialogHelper dialogHelper = new com.example.duan_laptop.HELPER.CustomDialogHelper(requireContext());
                        dialogHelper.showWarning(
                                "Xóa thông báo",
                                "Bồ có chắc muốn xóa thông báo này không?",
                                new com.example.duan_laptop.HELPER.CustomDialogHelper.DialogActionListener() {
                                    @Override
                                    public void onPositiveClick() {
                                        // Nếu chọn Xóa -> Gọi API xóa trên Server
                                        xoaThongBaoTrenServer(tbBiVuot.MaTB, position);
                                    }
                                    @Override
                                    public void onNegativeClick() {
                                        // Nếu chọn Không -> Ép item trượt ngược về chỗ cũ
                                        adapter.notifyItemChanged(position);
                                    }
                                }
                        );
                    }

                    // Vẽ nền đỏ và thùng rác khi vuốt
                    @Override
                    public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        View itemView = viewHolder.itemView;
                        if (dX < 0) { // Đang vuốt sang trái
                            android.graphics.drawable.ColorDrawable background = new android.graphics.drawable.ColorDrawable(android.graphics.Color.RED);
                            background.setBounds(itemView.getRight() + ((int) dX) - 20, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                            background.draw(c);

                            android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);
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
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                };

        // Gắn hiệu ứng vuốt vào RecyclerView
        new androidx.recyclerview.widget.ItemTouchHelper(swipeCallback).attachToRecyclerView(rcvThongBao);
    }

    private void loadThongBao() {
        if (SERVER.user == null) return;

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_get_thongbao,
                response -> {
                    try {
                        mangThongBao.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            mangThongBao.add(new ThongBao(
                                    obj.getInt("MaTB"),
                                    obj.getString("TieuDe"),
                                    obj.getString("NoiDung"),
                                    obj.getString("NgayTao")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) { e.printStackTrace(); }
                }, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("sdt", SERVER.user.SDT); // Lấy thông báo của user này
                return p;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadThongBao(); // Mở tab lên là lấy dữ liệu mới

        // Bật bộ thu sóng khi mở Tab
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiver, new IntentFilter("CO_THONG_BAO_MOI"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(requireContext(), receiver, new IntentFilter("CO_THONG_BAO_MOI"), ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Tắt bộ thu sóng cho đỡ hao pin
        requireContext().unregisterReceiver(receiver);
    }
    // HÀM XÓA TRÊN SERVER VÀ ÉP MAIN ACTIVITY CẬP NHẬT LẠI SỐ ĐẾM
    private void xoaThongBaoTrenServer(int maTB, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_delete_thongbao,
                response -> {
                    if (response.trim().equals("success")) {
                        // 1. Xóa khỏi danh sách trên App
                        mangThongBao.remove(position);
                        adapter.notifyItemRemoved(position);

                        // 2. CHIÊU THỨC QUAN TRỌNG: Ép MainActivity chạy lại hàm đếm số!
                        if (getActivity() instanceof com.example.duan_laptop.MainActivity) {
                            ((com.example.duan_laptop.MainActivity) getActivity()).updateThongBaoBadge();
                        }

                        Toast.makeText(getContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position); // Bật item trở lại nếu lỗi
                    }
                }, error -> {
            Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(position);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maTB", String.valueOf(maTB));
                return p;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }
}