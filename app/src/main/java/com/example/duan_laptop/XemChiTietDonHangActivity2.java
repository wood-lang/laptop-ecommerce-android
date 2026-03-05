package com.example.duan_laptop;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.ChiTietDonHangAdapter;
import com.example.duan_laptop.ADAPTER.SelectedMediaAdapter;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.MODEL.laptop_model;
import com.r0adkll.slidr.Slidr;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.gotev.uploadservice.data.UploadInfo;
import net.gotev.uploadservice.network.ServerResponse;
import net.gotev.uploadservice.observer.request.RequestObserverDelegate;
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

public class XemChiTietDonHangActivity2 extends AppCompatActivity {
    Button btnHuyDon, btnMuaLai;
    Button btnDaNhanHang;
    RecyclerView rcvChiTiet;
    ArrayList<laptop_model> listSP;
    ChiTietDonHangAdapter adapter;
    int maHD = 0;
    int trangThai = 0;

    // UI Tracking
    ImageView step1, step2, step3, step4;
    View line1, line2, line3;
    LinearLayout layoutTracking;
    TextView tvHuy;
    // KHAI BÁO BIẾN CHO PHẦN ĐÁNH GIÁ
    ArrayList<Uri> listImages = new ArrayList<>();
    ArrayList<Uri> listVideos = new ArrayList<>();
    SelectedMediaAdapter imageAdapter;
    SelectedMediaAdapter videoAdapter;

    // LAUNCHER CHỌN NHIỀU ẢNH
    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                            listImages.add(uri);
                        }
                    } else if (result.getData().getData() != null) {
                        listImages.add(result.getData().getData());
                    }
                    if (imageAdapter != null) imageAdapter.notifyDataSetChanged();
                }
            }
    );

    // LAUNCHER CHỌN NHIỀU VIDEO
    ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                            listVideos.add(uri);
                        }
                    } else if (result.getData().getData() != null) {
                        listVideos.add(result.getData().getData());
                    }
                    if (videoAdapter != null) videoAdapter.notifyDataSetChanged();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_chi_tiet_don_hang2);
        Slidr.attach(this);

        // Ánh xạ View
        Toolbar toolbar = findViewById(R.id.toolbarCTDH);
        btnHuyDon = findViewById(R.id.btnHuyDonHang);
        btnMuaLai = findViewById(R.id.btnMuaLai);
        step1 = findViewById(R.id.imgStep1);
        step2 = findViewById(R.id.imgStep2);
        step3 = findViewById(R.id.imgStep3);
        step4 = findViewById(R.id.imgStep4);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
        layoutTracking = findViewById(R.id.layoutTrangThai);
        tvHuy = findViewById(R.id.tvTrangThaiHuy);
        rcvChiTiet = findViewById(R.id.rcvChiTietDonHang);
        btnDaNhanHang = findViewById(R.id.btnDaNhanHang);
        btnDaNhanHang.setOnClickListener(v -> confirmDaNhanHang());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent i = getIntent();
        maHD = i.getIntExtra("mahd", 0);
        trangThai = i.getIntExtra("trangthai", 0);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết đơn hàng #" + maHD);
        }

        setupButtonVisibility(trangThai);
        updateTrackingUI(trangThai);

        btnHuyDon.setOnClickListener(v -> showDialogConfirmHuy());
        btnMuaLai.setOnClickListener(v -> xuLyMuaLai());

        rcvChiTiet.setLayoutManager(new LinearLayoutManager(this));
        listSP = new ArrayList<>();

        adapter = new ChiTietDonHangAdapter(this, listSP, trangThai, laptop -> showDialogDanhGia(laptop));
        rcvChiTiet.setAdapter(adapter);

        loadChiTietDonHang();
    }
    private void confirmDaNhanHang() {
        new CustomDialogHelper(this).showWarning(
                "Xác nhận nhận hàng",
                "Bạn xác nhận đã nhận đủ hàng và muốn hoàn tất đơn hàng này chứ?",
                new CustomDialogHelper.DialogActionListener() {
                    @Override
                    public void onPositiveClick() {
                        xuLyCapNhatThanhCong();
                    }
                }
        );
    }

    private void xuLyCapNhatThanhCong() {
        String url = SERVER.url_update_status;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        new CustomDialogHelper(this).showSuccess(
                                "Hoàn tất!",
                                "Cảm ơn bồ! Chúc bồ có một ngày vui vẻ :)))).",
                                v -> {
                                    finish(); // Đóng trang để quay lại lịch sử
                                }
                        );
                    } else {
                        Toast.makeText(this, "Lỗi: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mahd", String.valueOf(maHD));
                params.put("trangthai", "3"); // GỬI SỐ 3 ĐỂ PHP CỘNG SỐ LƯỢNG BÁN
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void setupButtonVisibility(int status) {
        // 1. Mặc định ẩn tất cả đi để tránh bị chồng chéo
        btnHuyDon.setVisibility(View.GONE);
        btnMuaLai.setVisibility(View.GONE);
        btnDaNhanHang.setVisibility(View.GONE);

        if (status == 0) {
            // CHỜ DUYỆT: Khách có quyền Hủy đơn
            btnHuyDon.setVisibility(View.VISIBLE);

        } else if (status == 2) {
            // ĐANG GIAO: Hiện nút "Đã nhận hàng" để khách chốt đơn
            btnDaNhanHang.setVisibility(View.VISIBLE);

        } else if (status == 3 || status == 4) {
            // THÀNH CÔNG hoặc ĐÃ HỦY: Hiện nút "Mua lại" để khách đặt tiếp
            btnMuaLai.setVisibility(View.VISIBLE);

        } else {
        }
    }

    private void updateTrackingUI(int status) {
        if (status == 4) {
            layoutTracking.setVisibility(View.GONE);
            tvHuy.setVisibility(View.VISIBLE);
            return;
        }

        tvHuy.setVisibility(View.GONE);
        layoutTracking.setVisibility(View.VISIBLE);

        int colorGreen = android.graphics.Color.parseColor("#4CAF50");
        step1.setImageResource(R.drawable.ic_check_circle_green);

        if (status >= 1) {
            line1.setBackgroundColor(colorGreen);
            step2.setImageResource(R.drawable.ic_check_circle_green);
        }
        if (status >= 2) {
            line2.setBackgroundColor(colorGreen);
            step3.setImageResource(R.drawable.ic_check_circle_green);
        }
        if (status >= 3) {
            line3.setBackgroundColor(colorGreen);
            step4.setImageResource(R.drawable.ic_check_circle_green);
        }
    }

    private void showDialogConfirmHuy() {
        CustomDialogHelper dialogHelper = new CustomDialogHelper(this);
        dialogHelper.showWarning(
                "Xác nhận hủy đơn",
                "Bạn có chắc chắn muốn hủy đơn hàng này không?",
                new CustomDialogHelper.DialogActionListener() {
                    @Override
                    public void onPositiveClick() {
                        xuLyHuyDonHang(); // Gọi hàm hủy nếu bấm Đồng ý
                    }
                }
        );
    }

    private void xuLyMuaLai() {
        if (SERVER.user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_mua_lai,
                response -> {
                    String res = response.trim();
                    if (res.equals("success_deleted") || res.equals("success_kept")) {
                        Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                        goToCart();
                    } else {
                        Toast.makeText(this, "Lỗi Server: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mahd", String.valueOf(maHD));
                params.put("sdt", SERVER.user.SDT);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void goToCart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_cart", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void xuLyHuyDonHang() {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_update_status,
                response -> {
                    CustomDialogHelper dialogHelper = new CustomDialogHelper(this);
                    if (response.trim().equals("success")) {
                        dialogHelper.showSuccess("Đã hủy đơn", "Đơn hàng của bạn đã được hủy thành công!", v -> finish());
                    } else if (response.trim().equals("fail")) {
                        dialogHelper.showError("Không thể hủy", "Đơn hàng đã được duyệt hoặc đang giao, không thể hủy lúc này!");
                    } else {
                        Toast.makeText(this, "Lỗi: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mahd", String.valueOf(maHD));
                params.put("trangthai", "4");
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void loadChiTietDonHang() {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_get_chitiet_donhang,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        listSP.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            laptop_model sp = new laptop_model();

                            // 1. Dữ liệu laptop gốc (Để truyền đi trang Chi tiết sản phẩm)
                            sp.MaLapTop = obj.optInt("MaLaptop");
                            sp.TenLapTop = obj.optString("TenLaptop");
                            sp.HangSX = obj.optString("HangSX");
                            sp.CPU = obj.optString("CPU");
                            sp.MoTa = obj.optString("MoTa");
                            sp.HinhAnh = obj.optString("HinhAnh");
                            sp.HinhChiTiet = obj.optString("HinhChiTiet", "");

                            // ĐÂY LÀ SỐ TỔNG ĐÃ BÁN (Ví dụ: 8300) lấy từ cột 'SoLuongBan' trong bảng laptop
                            sp.SoLuongBan = obj.optInt("SoLuongBan");

                            // 2. Dữ liệu mua trong đơn hàng này (Để hiện "x 1", "x 2" ở danh sách)
                            sp.Gia = obj.optInt("Gia"); // Giá tại thời điểm mua

                            // Lấy đúng Alias 'SoLuongKhachMua' từ file PHP
                            int soLuongTrongDon = obj.optInt("SoLuongKhachMua",1);

                            // Gán vào biến tempSoLuongMua
                            sp.tempSoLuongMua = soLuongTrongDon;
                            Log.d("DEBUG_DATA", "Laptop: " + sp.TenLapTop + " | SoLuongMua: " + soLuongTrongDon);

                            listSP.add(sp);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("mahd", String.valueOf(maHD));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void showDialogDanhGia(laptop_model laptop) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_danh_gia, null);
        builder.setView(v);

        RatingBar rb = v.findViewById(R.id.rbDanhGia);
        EditText edt = v.findViewById(R.id.edtNoiDungDG);
        Button btnGui = v.findViewById(R.id.btnGuiDG);
        Button btnChonAnh = v.findViewById(R.id.btnChonAnh);
        Button btnChonVideo = v.findViewById(R.id.btnChonVideo);

        RecyclerView rcvImg = v.findViewById(R.id.rcvSelectedImages);
        RecyclerView rcvVid = v.findViewById(R.id.rcvSelectedVideos);

        listImages.clear();
        listVideos.clear();

        imageAdapter = new SelectedMediaAdapter(this, listImages, false, position -> {
            listImages.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, listImages.size());
        });
        rcvImg.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvImg.setAdapter(imageAdapter);

        videoAdapter = new SelectedMediaAdapter(this, listVideos, true, position -> {
            listVideos.remove(position);
            videoAdapter.notifyItemRemoved(position);
            videoAdapter.notifyItemRangeChanged(position, listVideos.size());
        });
        rcvVid.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvVid.setAdapter(videoAdapter);

        AlertDialog dialog = builder.create();

        btnChonAnh.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh (có thể chọn nhiều)"));
        });

        btnChonVideo.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            videoPickerLauncher.launch(Intent.createChooser(intent, "Chọn video (có thể chọn nhiều)"));
        });

        btnGui.setOnClickListener(view -> {
            String nd = edt.getText().toString().trim();
            if (nd.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadDanhGiaMultipart(laptop.MaLapTop, nd, (int) rb.getRating());
            dialog.dismiss();
        });

        dialog.show();
    }

    private String getPathFromUri(Uri uri, boolean isVideo) {
        if (uri == null) return null;
        try {
            String extension = isVideo ? ".mp4" : ".jpg";
            // Dùng nanoTime() để tạo tên file độc nhất vô nhị
            File file = new File(getCacheDir(), "upload_" + System.nanoTime() + extension);

            java.io.InputStream is = getContentResolver().openInputStream(uri);
            java.io.FileOutputStream os = new java.io.FileOutputStream(file);

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();

            Log.d("DEBUG_FILE", "Đã tạo file tạm: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("DEBUG_FILE", "Lỗi copy file: " + e.getMessage());
            return null;
        }
    }

    private void uploadDanhGiaMultipart(int maLaptop, String noiDung, int soSao) {
        try {
            MultipartUploadRequest request = new MultipartUploadRequest(this, SERVER.url_send_danhgia);

            // Gắn TẤT CẢ VIDEO
            for (int i = 0; i < listVideos.size(); i++) {
                String vidPath = getPathFromUri(listVideos.get(i), true); // THÊM true
                if (vidPath != null) request.addFileToUpload(vidPath, "video_files[]");
            }

            // Gắn TẤT CẢ ẢNH
            for (int i = 0; i < listImages.size(); i++) {
                String imgPath = getPathFromUri(listImages.get(i), false); // THÊM false
                if (imgPath != null) request.addFileToUpload(imgPath, "image_files[]");
            }

            request.addParameter("ma_laptop", String.valueOf(maLaptop))
                    .addParameter("sdt", SERVER.user.SDT)
                    .addParameter("ten_kh", SERVER.user.TenKH != null ? SERVER.user.TenKH : "Khách hàng")
                    .addParameter("noi_dung", noiDung)
                    .addParameter("so_sao", String.valueOf(soSao))
                    .setMethod("POST");

            net.gotev.uploadservice.observer.request.RequestObserver observer =
                    new net.gotev.uploadservice.observer.request.RequestObserver(this, this, new RequestObserverDelegate() {
                        @Override public void onCompletedWhileNotObserving() {}
                        @Override public void onProgress(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}

                        @Override
                        public void onSuccess(@NonNull Context context, @NonNull UploadInfo uploadInfo, @NonNull ServerResponse serverResponse) {
                            new CustomDialogHelper(XemChiTietDonHangActivity2.this).showSuccess(
                                    "Tuyệt vời!",
                                    "Đánh giá của bạn đã được gửi thành công.",
                                    null
                            );
                        }

                        @Override
                        public void onError(@NonNull Context context, @NonNull UploadInfo uploadInfo, @NonNull Throwable exception) {
                            Toast.makeText(context, "Lỗi: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override public void onCompleted(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}
                        public void onCancelled(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}
                    });

            observer.subscribe(request);
            request.startUpload();
            Toast.makeText(this, "Đang gửi đánh giá...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("UPLOAD_ERROR", e.getMessage());
        }
    }
}