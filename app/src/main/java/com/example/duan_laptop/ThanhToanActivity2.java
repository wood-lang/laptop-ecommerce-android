package com.example.duan_laptop;

import android.content.Intent;
import android.graphics.Color; // Thêm thư viện màu
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.MODEL.Cart_item;
import com.example.duan_laptop.MODEL.Voucher;
import com.example.duan_laptop.databinding.ActivityThanhToan2Binding;
import com.r0adkll.slidr.Slidr;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThanhToanActivity2 extends AppCompatActivity {
    ActivityThanhToan2Binding binding;
    int hinhThucTT = 0; // 0: COD, 1: Banking
    long tongTienHang = 0; // Tổng tiền gốc của giỏ hàng
    int phanTramGiam = 0;
    ArrayList<Voucher> listVoucher;
    long tienGiamGia = 0; // Lưu số tiền được giảm để gửi lên Server
    long tongTienSauGiam = 0; // Lưu tổng tiền cuối cùng
    private Handler handlerCheckPayment;
    private Runnable runnableCheckPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThanhToan2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Slidr.attach(this);
        // 1. Lấy tổng tiền hàng gốc ban đầu
        tongTienHang = Cart_manager.getTotalPrice();
        // Mặc định nếu không giảm thì tiền sau giảm = tiền gốc
        tongTienSauGiam = tongTienHang;

        loadInfo();

        // 2. Tính toán và hiển thị giao diện tiền ngay lần đầu mở
        hienThiThanhToan();

        binding.btnXacNhan.setOnClickListener(v -> xuLyDatHang());
        binding.btnTroVe.setOnClickListener(v -> finish());

        listVoucher = new ArrayList<>();
        loadVoucherFromServer();
        binding.tvChonVoucher.setOnClickListener(v -> showDialogChonVoucher());
    }

    private void loadVoucherFromServer() {
        String url = SERVER.server_add + "get_voucher.php";
        JsonArrayRequest request = new JsonArrayRequest(url, response -> {
            listVoucher.clear();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject o = response.getJSONObject(i);
                    listVoucher.add(new Voucher(
                            o.getInt("id"),
                            o.getString("MaVoucher"),
                            o.getString("TenVoucher"),
                            o.getInt("GiamGia")
                    ));
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, error -> {});
        Volley.newRequestQueue(this).add(request);
    }

    // Hàm hiện Dialog chọn Voucher
    private void showDialogChonVoucher() {
        if (listVoucher.size() == 0) {
            Toast.makeText(this, "Không có mã giảm giá nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo mảng tên để hiện lên Dialog
        String[] tenVoucher = new String[listVoucher.size()];
        for (int i = 0; i < listVoucher.size(); i++) {
            tenVoucher[i] = listVoucher.get(i).TenVoucher + " (Giảm " + listVoucher.get(i).GiamGia + "%)";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn mã giảm giá");
        builder.setItems(tenVoucher, (dialog, which) -> {
            // Khi người dùng chọn 1 dòng
            Voucher v = listVoucher.get(which);

            // Cập nhật giao diện nút chọn
            binding.tvChonVoucher.setText(v.MaVoucher + " (-" + v.GiamGia + "%)");
            binding.tvChonVoucher.setTextColor(Color.parseColor("#3F51B5")); // Đổi màu xanh cho đẹp

            // Cập nhật phần trăm giảm
            phanTramGiam = v.GiamGia;

            // QUAN TRỌNG: Gọi hàm tính lại tiền ngay lập tức
            hienThiThanhToan();

            Toast.makeText(this, "Đã áp dụng mã: " + v.MaVoucher, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
    private void hienThiThanhToan() {
        DecimalFormat df = new DecimalFormat("###,###,###");

        // 1. Tính toán logic
        tienGiamGia = (tongTienHang * phanTramGiam) / 100;
        tongTienSauGiam = tongTienHang - tienGiamGia;

        // 2. Hiển thị lên giao diện
        // Tạm tính (Giá gốc)
        binding.tvTamTinh.setText(df.format(tongTienHang) + " đ");

        // Tiền giảm
        if (phanTramGiam > 0) {
            binding.tvTienGiam.setText("-" + df.format(tienGiamGia) + " đ");
            binding.tvTienGiam.setTextColor(Color.RED); // Màu đỏ cho nổi
        } else {
            binding.tvTienGiam.setText("0 đ");
            binding.tvTienGiam.setTextColor(Color.BLACK);
        }

        // Tổng thanh toán (Giá cuối)
        binding.tvTongTienThanhToan.setText(df.format(tongTienSauGiam) + " đ");
    }

    private void loadInfo() {
        if (SERVER.user != null) {
            binding.edTenNguoiNhan.setText(SERVER.user.TenKH != null ? SERVER.user.TenKH : "");
            binding.edSDT.setText(SERVER.user.SDT);
            binding.edDiaChi.setText(SERVER.user.DiaChi);
        }
        // Phần set text tổng tiền cũ này có thể ẩn đi vì hàm hienThiThanhToan đã lo rồi
        // binding.tvTongTienTT.setText("Tổng tiền: " + NumberFormat.getInstance().format(tongTienHang) + " đ");
    }

    private void xuLyDatHang() {
        // 1. Validate thông tin
        String ten = binding.edTenNguoiNhan.getText().toString().trim();
        String sdt = binding.edSDT.getText().toString().trim();
        String diachi = binding.edDiaChi.getText().toString().trim();

        if (ten.isEmpty() || sdt.isEmpty() || diachi.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin nhận hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra hình thức thanh toán
        if (binding.radioGroupPayment.getCheckedRadioButtonId() == R.id.rbBanking) {
            hinhThucTT = 1; // Chuyển khoản
        } else {
            hinhThucTT = 0; // COD
        }

        // 3. Tạo JSON chi tiết
        JSONArray jsonArray = new JSONArray();
        for (Cart_item item : Cart_manager.manggiohang) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("ma_laptop", item.laptop.MaLapTop);
                obj.put("so_luong", item.SoLuong);
                obj.put("don_gia", item.laptop.Gia);
                obj.put("ten_laptop", item.laptop.TenLapTop);
                jsonArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String strChiTiet = jsonArray.toString();
        // CHẶN SPAM: Khóa nút ngay khi bấm
        binding.btnXacNhan.setEnabled(false);
        binding.btnXacNhan.setText("Đang xử lý...");
        // 4. Gửi Request lên Server
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_thanhtoan,
                response -> {
                    binding.btnXacNhan.setEnabled(true); // Mở lại nút
                    binding.btnXacNhan.setText("Xác nhận");
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getString("status").equals("success")) {
                            int maDonHang = jsonObject.getInt("ma_don_hang");

                            // QUAN TRỌNG: Lấy số tiền ĐÃ GIẢM để tạo QR Code
                            long tienCanThanhToan = tongTienSauGiam;

                            if (hinhThucTT == 1) {
                                // Banking -> Hiện QR với số tiền ĐÃ GIẢM
                                openPayOSPayment(maDonHang, tienCanThanhToan);
                            } else {
                                // COD -> Xong luôn
                                hoanTatDatHang();
                            }
                        } else {
                            String message = jsonObject.optString("message", "Có lỗi xảy ra khi đặt hàng!");

                            CustomDialogHelper dialogHelper = new CustomDialogHelper(this);
                            dialogHelper.showError(
                                    "Hết sản phẩm!",
                                    message // PHP sẽ trả về ví dụ: "Sản phẩm Laptop Dell hiện tại không đủ hàng!"
                            );
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (response.trim().equals("success")) {
                            hoanTatDatHang();
                        } else {
                            Toast.makeText(this, "Lỗi phản hồi: " + response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error ->{
                    binding.btnXacNhan.setEnabled(true);
                    binding.btnXacNhan.setText("Xác nhận");
                    Toast.makeText(this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("ten_nguoi_nhan", ten);
                map.put("sdt", sdt);
                map.put("dia_chi", diachi);
                map.put("user_id", SERVER.user != null ? SERVER.user.SDT : "");

                // --- GỬI ĐÚNG SỐ TIỀN KHÁCH PHẢI TRẢ ---
                long tienCanThanhToan = (tongTienSauGiam > 0) ? tongTienSauGiam : tongTienHang;
                map.put("tong_tien", String.valueOf(tienCanThanhToan));

                // --- GỬI SỐ TIỀN ĐƯỢC GIẢM ---
                map.put("tien_giam", String.valueOf(tienGiamGia));

                map.put("chi_tiet_don_hang", strChiTiet);
                map.put("hinh_thuc", String.valueOf(hinhThucTT));
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
    private void openPayOSPayment(int maDonHang, long tongTien) {
        String urlCreateLink = SERVER.server_add + "create_payos_link.php";

        StringRequest request = new StringRequest(Request.Method.POST, urlCreateLink,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            String checkoutUrl = jsonObject.getString("checkoutUrl");

                            // 1. Mở trình duyệt hoặc hiện QR từ link này
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                            startActivity(intent);

                            // 2. Bắt đầu vòng lặp "canh" tiền về
                            batDauCheckThanhToan(maDonHang, null);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Lỗi kết nối PayOS", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("orderCode", String.valueOf(maDonHang));
                params.put("amount", String.valueOf(tongTien));
                params.put("description", "Thanh toan DH" + maDonHang);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void showDialogQR(int maHD, long soTien) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_qr_code, null);
        builder.setView(view);

        ImageView imgQR = view.findViewById(R.id.imgQR);
        Button btnDaThanhToan = view.findViewById(R.id.btnDaThanhToan);
        TextView tvNoiDung = view.findViewById(R.id.tvNoiDungCK);

        String BANK_ID = "MB";
        String ACCOUNT_NO = "0334888888";
        String TEMPLATE = "compact2";
        String CONTENT = "TT DH" + maHD;

        // URL QR Code (Dùng số tiền thực tế truyền vào)
        String urlQR = "https://img.vietqr.io/image/" + BANK_ID + "-" + ACCOUNT_NO + "-" + TEMPLATE + ".png?amount=" + soTien + "&addInfo=" + CONTENT;

        Picasso.get().load(urlQR).into(imgQR);
        tvNoiDung.setText("Nội dung: " + CONTENT);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnDaThanhToan.setOnClickListener(v -> {
            dialog.dismiss();
            hoanTatDatHang();
        });

        dialog.show();
    }
//    private void openMoMoApp(long soTien, int maHD) {
//        String phoneAdmin = "0868021320"; // Số MoMo của bồ
//        String nameAdmin = "Chủ Shop Laptop";
//        String note = "Thanh toan don hang DH" + maHD;
//
//        // Cấu trúc dữ liệu DeepLink P2P MoMo
//        String data = "2|0|" + phoneAdmin + "|" + nameAdmin + "||0|0|" + soTien + "|" + note + "|transfer_p2p";
//        String base64Data = android.util.Base64.encodeToString(data.getBytes(), android.util.Base64.DEFAULT);
//
//        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(android.net.Uri.parse("momoportal://com.mservice.momo/payment?data=" + base64Data));
//            startActivity(intent);
//
//            // Hiện thông báo chờ và bắt đầu quét trạng thái từ Server
//            showDialogDoiThanhToan(maHD);
//        } catch (Exception e) {
//            Toast.makeText(this, "Vui lòng cài đặt ứng dụng MoMo!", Toast.LENGTH_SHORT).show();
//        }
//    }
private void batDauCheckThanhToan(int maHD, AlertDialog dialog) {
    handlerCheckPayment = new Handler(Looper.getMainLooper());
    runnableCheckPayment = new Runnable() {
        @Override
        public void run() {
            // Gọi file check_status.php qua link NGROK
            String url = SERVER.url_check_status + "?mahd=" + maHD;
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    response -> {
                        if (response.trim().equals("1")) { // Nếu TrangThai trong DB đã là 1
                            huyCheckThanhToan();
                            if (dialog != null) dialog.dismiss();
                            hoanTatDatHang(); // Hiện pháo hoa chúc mừng!
                        }
                    }, error -> {}
            );
            Volley.newRequestQueue(ThanhToanActivity2.this).add(request);
            handlerCheckPayment.postDelayed(this, 3000); // 3 giây check 1 lần
        }
    };
    handlerCheckPayment.post(runnableCheckPayment);
}

    private void huyCheckThanhToan() {
        if (handlerCheckPayment != null) handlerCheckPayment.removeCallbacks(runnableCheckPayment);
    }

    private void hoanTatDatHang() {
        // 1. Dọn dẹp giỏ hàng ngay lập tức
        Cart_manager.clearCart();
        CustomDialogHelper dialogHelper = new CustomDialogHelper(this);
        dialogHelper.showOrderSuccess(
                "Chốt đơn thành công! 🎉",
                "Cảm ơn bạn đã tin tưởng ủng hộ. Đơn hàng đang được đóng gói và sẽ giao sớm nhất!",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 3. Chỉ khi nào khách bấm nút "Về trang chủ" trên Dialog thì app mới chuyển trang
                        Intent intent = new Intent(ThanhToanActivity2.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        huyCheckThanhToan(); // Dừng ngay vòng lặp khi đóng trang
    }
    // ==============================================================
    // HÀM BẮT SỰ KIỆN KHI TRÌNH DUYỆT ĐÁ DEEP LINK VỀ LẠI APP
    // ==============================================================
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật Intent mới nhất

        Uri data = intent.getData();
        if (data != null && "duan_laptop".equals(data.getScheme()) && "payment".equals(data.getHost())) {
            String path = data.getPath();

            if ("/success".equals(path)) {
                // Nếu trình duyệt báo về là success -> Nổ pháo hoa luôn!
                huyCheckThanhToan(); // Dừng vòng lặp chạy ngầm lại
                hoanTatDatHang();    // Gọi hàm chốt đơn, hiện Dialog custom

            } else if ("/cancel".equals(path)) {
                // Nếu khách bấm hủy
                huyCheckThanhToan();
                // Mở lại nút bấm để khách có thể đổi ý sang COD hoặc thử lại
                binding.btnXacNhan.setEnabled(true);
                binding.btnXacNhan.setText("Xác nhận đặt hàng lại");

                new CustomDialogHelper(this).showError(
                        "Thanh toán bị hủy",
                        "Đơn hàng cũ đã được hủy tự động để đảm bảo an toàn. Bồ có thể bấm xác nhận lại để thanh toán bằng cách khác nhé!"
                );
            }
        }
    }
}