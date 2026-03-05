package com.example.duan_laptop;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.databinding.ActivityRegister2Binding;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity2 extends AppCompatActivity {

    ActivityRegister2Binding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegister2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String sdt = binding.edtPhone.getText().toString().trim();
        String pass = binding.edtPassword.getText().toString().trim();
        String ten = binding.edtName.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String dc = binding.edtDiachi.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào cơ bản
        if (sdt.isEmpty() || pass.isEmpty() || ten.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sdt.length() < 10) {
            binding.edtPhone.setError("Số điện thoại không hợp lệ");
            return;
        }

        binding.btnRegister.setEnabled(false); // Chống bấm nhiều lần

        StringRequest req = new StringRequest(
                Request.Method.POST,
                SERVER.url_register,
                res -> {
                    binding.btnRegister.setEnabled(true);
                    try {
                        JSONObject json = new JSONObject(res.trim());
                        CustomDialogHelper dialogHelper = new CustomDialogHelper(RegisterActivity2.this);

                        if (json.getBoolean("success")) {
                            dialogHelper.showSuccess("Tuyệt vời!", "Đăng ký tài khoản thành công. Đăng nhập ngay thôi!", v -> finish());
                        } else {
                            dialogHelper.showError("Đăng ký thất bại", json.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar.make(binding.getRoot(), "Lỗi phản hồi từ Server", Snackbar.LENGTH_LONG).show();
                    }
                },
                err -> {
                    binding.btnRegister.setEnabled(true);
                    Snackbar.make(binding.getRoot(), "Lỗi kết nối mạng", Snackbar.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("sdt", sdt);
                m.put("password", pass);
                m.put("tenkh", ten);
                m.put("email", email);
                m.put("diachi", dc);
                return m;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }
}