package com.example.duan_laptop;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.HELPER.CustomDialogHelper;

import java.util.HashMap;
import java.util.Map;

public class QuenMatKhauActivity2 extends AppCompatActivity {

    EditText edtEmail, edtOTP, edtPassMoi;
    Button btnGuiOTP, btnXacNhan;
    LinearLayout layoutReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau2);

        edtEmail = findViewById(R.id.edtEmailQuenMK);
        edtOTP = findViewById(R.id.edtOTP);
        edtPassMoi = findViewById(R.id.edtPassMoi);
        btnGuiOTP = findViewById(R.id.btnGuiOTP);
        btnXacNhan = findViewById(R.id.btnXacNhanDoiMK);
        layoutReset = findViewById(R.id.layoutReset);

        btnGuiOTP.setOnClickListener(v -> guiYeuCauOTP());
        btnXacNhan.setOnClickListener(v -> xacNhanDoiMK());
    }

    private void guiYeuCauOTP() {
        String email = edtEmail.getText().toString().trim();
        if(email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuiOTP.setEnabled(false);
        btnGuiOTP.setText("Đang gửi...");

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_gui_otp,
                response -> {
                    btnGuiOTP.setEnabled(true);
                    btnGuiOTP.setText("Gửi lại OTP");
                    String result = response.trim();
                    if (result.equals("success")) {
                        Toast.makeText(this, "Mã OTP đã gửi đến Gmail của bạn!", Toast.LENGTH_LONG).show();
                        layoutReset.setVisibility(View.VISIBLE);
                        edtEmail.setEnabled(false);
                    } else {
                        Toast.makeText(this, "Lỗi: " + result, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    btnGuiOTP.setEnabled(true);
                    btnGuiOTP.setText("Gửi OTP");
                    Toast.makeText(this, "Lỗi mạng hoặc Server phản hồi chậm", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        // CHỐNG GỬI 2 LẦN: Chờ 30s và không thử lại (0 retries)
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }

    private void xacNhanDoiMK() {
        String email = edtEmail.getText().toString().trim();
        String otp = edtOTP.getText().toString().trim();
        String passMoi = edtPassMoi.getText().toString().trim();

        if (otp.isEmpty() || passMoi.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã OTP và mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_xac_nhan_otp,
                response -> {
                    String result = response.trim();
                    CustomDialogHelper dialogHelper = new CustomDialogHelper(QuenMatKhauActivity2.this);

                    if (result.equals("success")) {
                        dialogHelper.showSuccess("Thành công", "Mật khẩu của bạn đã được cập nhật an toàn.", v -> finish());
                    } else if (result.equals("wrong_otp")) {
                        dialogHelper.showError("Lỗi xác thực", "Mã OTP không chính xác, vui lòng kiểm tra lại.");
                    } else {
                        Toast.makeText(this, "Lỗi: " + result, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("otp", otp);
                params.put("new_pass", passMoi);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}