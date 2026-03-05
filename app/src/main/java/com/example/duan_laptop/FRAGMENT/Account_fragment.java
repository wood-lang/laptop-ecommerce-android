package com.example.duan_laptop.FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.LichSuDonHangActivity;
import com.example.duan_laptop.Login_laptopActivity2;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import com.example.duan_laptop.databinding.FragmentAccountLayoutBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Account_fragment extends Fragment {

    private FragmentAccountLayoutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (SERVER.user != null && SERVER.user.SDT.equals("0868021320")) {
            binding.btnQuanLyKho.setVisibility(View.VISIBLE);
            binding.lineQuanLy.setVisibility(View.VISIBLE); // Hiện đường kẻ ngang phía trên nút
        }

        binding.btnQuanLyKho.setOnClickListener(v -> showDialogNhapHang());
        // Hiển thị thông tin User
        loadUserInfo();

        // Cài đặt sự kiện nút bấm
        setupEvents();
    }

    private void loadUserInfo() {
        // Kiểm tra nếu chưa đăng nhập
        if (SERVER.user == null) {
            binding.tvTenKH.setText("Khách");
            binding.tvSDT.setText("Chưa đăng nhập");
            binding.tvEmail.setText("---");
            binding.tvDiaChi.setText("---");

            // Ẩn nút lịch sử nếu chưa đăng nhập (tuỳ chọn)
            // binding.btnLichSuGiaoDich.setVisibility(View.GONE);
            // Set ảnh mặc định nếu chưa đăng nhập (nếu có id ảnh)
            // binding.imgAvatar.setImageResource(R.drawable.ic_user);

            return;
        }


        // Đổ dữ liệu nếu đã đăng nhập
        binding.tvTenKH.setText(SERVER.user.getTenKH()); // Dùng getter cho an toàn
        binding.tvSDT.setText("SĐT: " + SERVER.user.getSDT());
        binding.tvEmail.setText("Email: " + SERVER.user.getEmail());
        binding.tvDiaChi.setText("Địa chỉ: " + SERVER.user.getDiaChi());
        // Đưa load ảnh ra ngoài if luôn cho gọn
        com.bumptech.glide.Glide.with(requireContext())
                .load(SERVER.user.Avatar) // Glide tự hiểu nếu cái này null/rỗng nó sẽ vào .error()
                .placeholder(R.drawable.ic_user) // Ảnh hiện ra trong lúc đang chờ load
                .error(R.drawable.icons8_person)       // Ảnh hiện ra nếu link ảnh bị lỗi hoặc không có ảnh
                .circleCrop()
                .into(binding.imgAvatar);
    }

    private void setupEvents() {
        binding.btnEditProfile.setOnClickListener(v -> {
            if (SERVER.user != null) {
                Intent intent = new Intent(getActivity(), com.example.duan_laptop.UpdateInfoActivity2.class);

                // Truyền dữ liệu hiện tại sang để bên kia hiện sẵn vào ô nhập
                intent.putExtra("TEN_GOOGLE", SERVER.user.getTenKH());
                intent.putExtra("SDT", SERVER.user.getSDT());
                intent.putExtra("EMAIL", SERVER.user.getEmail());
                intent.putExtra("DIACHI", SERVER.user.getDiaChi());

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để sửa thông tin", Toast.LENGTH_SHORT).show();
            }
        });
        // --- Nút xem Lịch sử giao dịch ---
        binding.btnLichSuGiaoDich.setOnClickListener(v -> {
            if (SERVER.user != null) {
                Intent intent = new Intent(getActivity(), LichSuDonHangActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            }
        });
        // --- Nút Đăng xuất ---
        binding.btnLogout.setOnClickListener(v -> {
            CustomDialogHelper dialogHelper = new CustomDialogHelper(requireContext());
            dialogHelper.showWarning("Đăng xuất", "Bạn có chắc chắn muốn thoát tài khoản không?",
                    new CustomDialogHelper.DialogActionListener() {
                        @Override
                        public void onPositiveClick() {
                            // Khách bấm "Đồng ý" thì mới chạy code đăng xuất
                            SERVER.logout(requireContext());
                            SERVER.user = null;
                            SERVER.favoriteList.clear();
                            SERVER.updateFavoriteBadge();
                            SERVER.updateCartBadge();
                            Intent intent = new Intent(getActivity(), Login_laptopActivity2.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo(); // Mỗi khi quay lại tab Account, nó sẽ vẽ lại thông tin mới nhất
    }
    private void nhapHangVaoKho(int maLaptop, int soLuongNhap) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.server_add + "nhap_hang.php",
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("ma_laptop", String.valueOf(maLaptop));
                params.put("so_luong", String.valueOf(soLuongNhap));
                params.put("sdt_admin", SERVER.user.SDT); // Gửi SĐT Admin để PHP check quyền
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }
    private void showDialogNhapHang() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("NHẬP HÀNG VÀO KHO");

        // Tạo View nhập liệu nhanh bằng Code hoặc dùng file XML riêng
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etMaLaptop = new EditText(getContext());
        etMaLaptop.setHint("Nhập mã Laptop (MaLaptop)");
        layout.addView(etMaLaptop);

        final EditText etSoLuong = new EditText(getContext());
        etSoLuong.setHint("Số lượng nhập thêm");
        etSoLuong.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etSoLuong);

        builder.setView(layout);

        builder.setPositiveButton("Nhập hàng", (dialog, which) -> {
            String ma = etMaLaptop.getText().toString().trim();
            String sl = etSoLuong.getText().toString().trim();

            if (!ma.isEmpty() && !sl.isEmpty()) {
                nhapHangVaoKho(Integer.parseInt(ma), Integer.parseInt(sl));
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đủ tin", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

}