package com.example.duan_laptop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.MODEL.USER;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);

        // Đợi 2 giây để user thấy logo rồi mới check login
        new Handler().postDelayed(() -> {
            if (SERVER.isLogged(this)) {
                autoLogin();
            } else {
                startActivity(new Intent(SplashActivity2.this, Login_laptopActivity2.class));
                finish();
            }
        }, 2000);
    }

    private void autoLogin() {
        // Lấy thông tin đã lưu
        String savedUser = SERVER.getSavedSDT(this); // Cái này có thể là SĐT hoặc Email
        String savedPass = SERVER.getSavedPass(this);

        // KIỂM TRA: Đây là Google hay Login thường?
        if (savedPass.equals("google_login")) {
            // === TRƯỜNG HỢP 1: AUTO LOGIN GOOGLE ===
            autoLoginGoogle(savedUser);
        } else {
            // === TRƯỜNG HỢP 2: AUTO LOGIN THƯỜNG (SĐT) ===
            autoLoginNormal(savedUser, savedPass);
        }
    }

    // Hàm dành riêng cho Login thường
    private void autoLoginNormal(String sdt, String pass) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_getKhachhang,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            // Gán dữ liệu
                            SERVER.user.SDT = sdt;
                            SERVER.user.Password = pass;
                            SERVER.user.TenKH = jsonObject.optString("TenKH");
                            SERVER.user.Email = jsonObject.optString("Email");
                            SERVER.user.DiaChi = jsonObject.optString("DiaChi", "");

                            startActivity(new Intent(SplashActivity2.this, MainActivity.class));
                            finish();
                        } else {
                            // Login thất bại -> Ra màn hình đăng nhập
                            goToLogin();
                        }
                    } catch (Exception e) { goToLogin(); }
                },
                error -> goToLogin()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tendangnhap", sdt);
                params.put("matkhau", pass);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // Hàm dành riêng cho Google
    // Trong SplashActivity2.java

    private void autoLoginGoogle(String email) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_login_google,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            // 1. Lấy dữ liệu từ Server về
                            String sdt = json.optString("SDT", "");
                            String diachi = json.optString("DiaChi", "");
                            String ten = json.optString("TenKH", "");

                            // 2. Lưu vào biến toàn cục SERVER.user
                            SERVER.user.Email = email;
                            SERVER.user.TenKH = ten;
                            SERVER.user.SDT = sdt;
                            SERVER.user.DiaChi = diachi;
                            SERVER.user.Password = "google_login"; // Đánh dấu là user Google

                            // 3. KIỂM TRA QUAN TRỌNG: Đã có SĐT và Địa chỉ chưa?
                            if (sdt.trim().isEmpty() || diachi.trim().isEmpty()) {
                                // --- TH1: Chưa có -> Đá sang màn hình Cập nhật ---
                                Intent intent = new Intent(SplashActivity2.this, UpdateInfoActivity2.class);
                                startActivity(intent);
                            } else {
                                // --- TH2: Đã có đủ -> Vào thẳng Main mua hàng ---
                                Intent intent = new Intent(SplashActivity2.this, MainActivity.class);
                                startActivity(intent);
                            }
                            finish(); // Đóng Splash lại

                        } else {
                            // Lỗi dữ liệu -> Về đăng nhập
                            goToLogin();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        goToLogin();
                    }
                },
                error -> {
                    // Lỗi mạng -> Về đăng nhập
                    goToLogin();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                // Gửi tên/ảnh rỗng cũng được vì đây là bước check lại user đã tồn tại
                params.put("ten", "");
                params.put("anh", "");
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    // Hàm phụ để chuyển trang cho gọn
    private void goToLogin() {
        startActivity(new Intent(SplashActivity2.this, Login_laptopActivity2.class));
        finish();
    }
}