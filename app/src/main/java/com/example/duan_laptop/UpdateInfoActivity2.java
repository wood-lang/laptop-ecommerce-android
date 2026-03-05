package com.example.duan_laptop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.HELPER.CustomDialogHelper;

import java.util.HashMap;
import java.util.Map;

public class UpdateInfoActivity2 extends AppCompatActivity {

    EditText edtSDT, edtDiaChi, edtTen;
    Button btnXacNhan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info2);
        edtTen = findViewById(R.id.edtUpdateTenKH);
        edtSDT = findViewById(R.id.edtSdtUpdate);
        edtDiaChi = findViewById(R.id.edtDiaChiUpdate);
        btnXacNhan = findViewById(R.id.btnXacNhanUpdate);
        String tenTuGoogle = getIntent().getStringExtra("TEN_GOOGLE");
        String sdtCu = getIntent().getStringExtra("SDT");
        String diaChiCu = getIntent().getStringExtra("DIACHI");

        if (tenTuGoogle != null) edtTen.setText(tenTuGoogle);
        if (sdtCu != null) edtSDT.setText(sdtCu);
        if (diaChiCu != null) edtDiaChi.setText(diaChiCu);
        if (tenTuGoogle != null && !tenTuGoogle.isEmpty()) {
            edtTen.setText(tenTuGoogle);
        }
        btnXacNhan.setOnClickListener(v -> capNhatServer());
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SERVER.user.getSDT() == null || SERVER.user.getSDT().isEmpty()) {
                    // TRƯỜNG HỢP 1: Khách mới đăng nhập Google, chưa có SĐT -> Bắt buộc ở lại
                    new CustomDialogHelper(UpdateInfoActivity2.this).showError(
                            "Cảnh báo",
                            "Bạn bắt buộc phải cập nhật số điện thoại và địa chỉ trước khi mua sắm!"
                    );
                } else {
                    // TRƯỜNG HỢP 2: Khách đã có thông tin, chỉ vào để sửa -> Cho phép quay lại
                    finish();
                }
            }
        });
    }

    private void capNhatServer() {
        String sdt = edtSDT.getText().toString().trim();
        String diachi = edtDiaChi.getText().toString().trim();
        String ten = edtTen.getText().toString().trim();

        if (sdt.isEmpty() || diachi.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.server_add + "update_info.php",
                response -> {
                    if (response.trim().equals("success")) {
                        // Cập nhật thành công -> Lưu lại vào biến toàn cục và0 máy
                        SERVER.user.TenKH = ten;
                        SERVER.user.SDT = sdt;
                        SERVER.user.DiaChi = diachi;

                        // Quan trọng: Lưu lại Login mới với đầy đủ thông tin
                        SERVER.saveLogin(this, SERVER.user.Email, "google_login", diachi);

                        new CustomDialogHelper(this).showSuccess("Hoàn tất", "Hồ sơ của bạn đã được cập nhật thành công!", v -> {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        });
                    } else {
                        Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Lỗi mạng", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tenkh", ten);
                params.put("email", SERVER.user.Email); // Lấy email từ biến toàn cục
                params.put("sdt", sdt);
                params.put("diachi", diachi);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}