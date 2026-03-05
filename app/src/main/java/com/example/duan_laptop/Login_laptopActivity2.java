package com.example.duan_laptop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.MODEL.USER;
import com.example.duan_laptop.databinding.ActivityLoginLaptop2Binding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login_laptopActivity2 extends AppCompatActivity {

    ActivityLoginLaptop2Binding binding;

    // Biến cho Captcha
    int ketQuaDung = 0;
    boolean canHienCaptcha = false;
    private com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient;
    // Launcher nhận kết quả từ Google

    androidx.activity.result.ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    android.content.Intent data = result.getData();
                    com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> task =
                            com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
            }
    );
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginLaptop2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso =
                new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail() // Yêu cầu lấy Email
                        .requestProfile()//lấy avatar
                        .build();

        mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);
        // --- BẮT SỰ KIỆN BẤM NÚT GOOGLE ---
        binding.btnGoogle.setOnClickListener(v -> {
            // Đăng xuất Google cũ ra trước để cho chọn tài khoản mới (nếu muốn)
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
        // AUTO LOGIN (Nếu đã lưu thì vào thẳng Main)
        if (SERVER.isLogged(this)) {
            // ... code auto login ...
        }
        // 1. Xử lý hiện mật khẩu
        binding.chkShowPass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            if (binding.edtPassword.getText() != null) {
                binding.edtPassword.setSelection(binding.edtPassword.getText().length());
            }
        });

        // 2. Sự kiện Đăng nhập
        binding.btnLogin.setOnClickListener(v -> doLogin(v));

        // 3. Sự kiện Đăng ký
        binding.txtRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity2.class)));

        // 4. Sự kiện QUÊN MẬT KHẨU
        binding.txtForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, QuenMatKhauActivity2.class));
        });
    }

    private void taoCauHoiCaptcha() {
        int a = (int) (Math.random() * 10);
        int b = (int) (Math.random() * 10);
        ketQuaDung = a + b;

        binding.tvCauHoiCaptcha.setText("Vui lòng tính: " + a + " + " + b + " = ?");
        binding.layoutCaptcha.setVisibility(View.VISIBLE);
        canHienCaptcha = true;
    }

    private void doLogin(View v) {
        String sdt = binding.edtPhone.getText().toString().trim();
        String pass = binding.edtPassword.getText().toString().trim();

        CustomDialogHelper dialogHelper = new CustomDialogHelper(this);

        if (sdt.isEmpty() || pass.isEmpty()) {
            dialogHelper.showError("Thiếu thông tin", "Bồ quên nhập số điện thoại hoặc mật khẩu kìa!");
            return;
        }

        // Kiểm tra Captcha nếu đang hiện
        if (canHienCaptcha) {
            String traLoi = binding.edtTraLoiCaptcha.getText().toString().trim();
            if (traLoi.isEmpty() || Integer.parseInt(traLoi) != ketQuaDung) {
                dialogHelper.showError("Sai Captcha", "Kết quả phép tính chưa đúng. Vui lòng tính lại nhé!");
                taoCauHoiCaptcha(); // Đổi câu hỏi khác
                return;
            }
        }

        login(sdt, pass, v);
    }

    private void login(String sdt, String pass, View v) {
        StringRequest req = new StringRequest(
                Request.Method.POST,
                SERVER.url_getKhachhang,
                res -> {
                    android.util.Log.d("DEBUG_LOGIN", "Dữ liệu PHP trả về: " + res);
                    try {
                        JSONObject json = new JSONObject(res.trim());
                        boolean isSuccess = json.optBoolean("success", false);
                        String status = json.optString("status", "");
                        String message = json.optString("message", "");

                        if (isSuccess) {
                            // Reset lại biến Captcha khi thành công
                            canHienCaptcha = false;
                            binding.layoutCaptcha.setVisibility(View.GONE);
                            USER u = new USER();
                            u.SDT = sdt;
                            u.TenKH = json.optString("TenKH", "Chưa có tên");
                            u.Email = json.optString("Email", "Chưa có email");
                            u.DiaChi = json.optString("DiaChi", "Chưa cập nhật địa chỉ");
                            u.Avatar = json.optString("Avatar", "");
                            SERVER.user = u;
                            SERVER.saveLogin(Login_laptopActivity2.this, sdt, pass, u.DiaChi);

                            CustomDialogHelper dialogHelper = new CustomDialogHelper(Login_laptopActivity2.this);
                            dialogHelper.showSuccess("Đăng nhập thành công!", "Chào mừng " + u.TenKH + " quay trở lại!", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Khách bấm chữ "Tuyệt vời" xong thì mới chuyển sang trang chủ
                                    startActivity(new Intent(Login_laptopActivity2.this, MainActivity.class));
                                    finish();
                                }
                            });
                        } else {
                            // --- XỬ LÝ CÁC TRƯỜNG HỢP THẤT BẠI ---
                            CustomDialogHelper dialogHelper = new CustomDialogHelper(Login_laptopActivity2.this);

                            if (status.equals("account_locked") || status.equals("locked_now")) {
                                dialogHelper.showError("Tài khoản bị khóa!", message + "\nVui lòng chọn 'Quên mật khẩu' để xác thực OTP và mở khóa.");
                            }
                            else if (status.equals("show_captcha")) {
                                Toast.makeText(Login_laptopActivity2.this, message, Toast.LENGTH_LONG).show();
                                taoCauHoiCaptcha();
                            }
                            else {
                                // Sai mật khẩu bình thường
                                dialogHelper.showError("Đăng nhập thất bại", message);
                            }
                        }

                    } catch (Exception e) {
                        Log.e("LOGIN_ERROR", "Lỗi parse JSON: " + e.getMessage());
                        Snackbar.make(v, "Lỗi dữ liệu server", Snackbar.LENGTH_LONG).show();
                    }
                    // Khi đăng nhập thành công
                },
                err -> Snackbar.make(v, "Lỗi kết nối mạng", Snackbar.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("tendangnhap", sdt);
                m.put("matkhau", pass);
                return m;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    @SuppressWarnings("deprecation")
    private void handleSignInResult(com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> completedTask) {
        try {
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = completedTask.getResult(com.google.android.gms.common.api.ApiException.class);

            // Lấy thông tin từ Google thành công
            String email = account.getEmail();
            String ten = account.getDisplayName();
            String anh ="";
            if (account.getPhotoUrl() != null) {
                anh = account.getPhotoUrl().toString();
                Log.d("DEBUG_AVATAR", "Link ảnh lấy được: " + anh);
                anh = anh.replace("s96-c", "s400-c"); // Hack lấy ảnh HD siêu nét
            }else {
                // HIỆN THÔNG BÁO LỖI NẾU GOOGLE KHÔNG TRẢ ẢNH
                Log.e("DEBUG_AVATAR", "Google trả về NULL cho PhotoUrl");
            }

            // Gửi lên PHP để kiểm tra (Đăng ký hoặc Đăng nhập)
            loginWithGooglePHP(email, ten, anh);

        } catch (com.google.android.gms.common.api.ApiException e) {
            android.widget.Toast.makeText(this, "Lỗi đăng nhập Google: " + e.getStatusCode(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void loginWithGooglePHP(String email, String ten, String anh) {
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.POST,SERVER.url_login_google,
                response -> {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.getBoolean("success")) {
                            // Lưu thông tin User
                            USER u = new USER();
                            u.Email = email;
                            u.TenKH = json.optString("TenKH", ten);
                            u.SDT = json.optString("SDT", "");
                            u.DiaChi = json.optString("DiaChi", "");
                            u.Avatar = json.optString("Avatar", "");

                            SERVER.user = u;
                            if (u.SDT.isEmpty()) {
                                // 1. Nếu SĐT rỗng (Khách mới hoặc chưa update) -> Sang trang Update
                                // Lưu tạm email để Login sau
                                SERVER.saveLogin(this, u.Email, "google_login", "");

                                Intent intent = new Intent(this, UpdateInfoActivity2.class);
                                intent.putExtra("TEN_GOOGLE", u.TenKH);
                                startActivity(intent);
                                finish();
                            } else {
                                // 2. Nếu đã có SĐT (Khách cũ) -> Vào thẳng Main
                                SERVER.saveLogin(this, u.Email, "google_login", u.DiaChi);
                                CustomDialogHelper dialogHelper = new CustomDialogHelper(Login_laptopActivity2.this);
                                dialogHelper.showSuccess("Đăng nhập thành công!", "Chào mừng " + u.TenKH + " quay trở lại!", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Bấm chữ Tuyệt vời xong mới nhảy sang Trang chủ
                                        startActivity(new Intent(Login_laptopActivity2.this, MainActivity.class));
                                        finish();
                                    }
                                });
                            }
                        } else {
                            android.widget.Toast.makeText(this, "Lỗi Server: " + json.getString("message"), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> android.widget.Toast.makeText(this, "Lỗi mạng", android.widget.Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("email", email);
                params.put("ten", ten);
                params.put("anh", anh);
                return params;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }
}