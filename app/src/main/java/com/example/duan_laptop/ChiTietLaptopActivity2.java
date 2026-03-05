package com.example.duan_laptop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.DanhGiaAdapter;
import com.example.duan_laptop.ADAPTER.HINH_SLIDE_ADAPTER;
import com.example.duan_laptop.ADAPTER.SelectedMediaAdapter;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.MODEL.DanhGia;
import com.example.duan_laptop.MODEL.HINH_SLIDE_MODEL;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.databinding.ActivityChiTietLaptop2Binding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.r0adkll.slidr.Slidr;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.gotev.uploadservice.data.UploadInfo;
import net.gotev.uploadservice.network.ServerResponse;
import net.gotev.uploadservice.observer.request.RequestObserverDelegate;
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

@androidx.annotation.OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
public class ChiTietLaptopActivity2 extends AppCompatActivity {


    ActivityChiTietLaptop2Binding binding;
    private TextView tvBadgeCount;
    // --- KHAI BÁO BỘ ĐẾM SLIDER ---
    private Handler sliderHandler = new Handler(android.os.Looper.getMainLooper());
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding.viewpagerChitietlaptop.getAdapter() != null) {
                int currentItem = binding.viewpagerChitietlaptop.getCurrentItem();
                int totalItems = binding.viewpagerChitietlaptop.getAdapter().getItemCount();
                if (currentItem < totalItems - 1) {
                    binding.viewpagerChitietlaptop.setCurrentItem(currentItem + 1);
                } else {
                    binding.viewpagerChitietlaptop.setCurrentItem(0); // Quay lại đầu
                }
            }
        }
    };
    laptop_model laptopModel;
    Cart_manager.CartListener cartListener;

    DanhGiaAdapter adapterDanhGia;
    ArrayList<DanhGia> mangDanhGia;
    ArrayList<Uri> listImages = new ArrayList<>();
    ArrayList<Uri> listVideos = new ArrayList<>();
    SelectedMediaAdapter imageAdapter;
    SelectedMediaAdapter videoAdapter;

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        // Trường hợp khách chọn NHIỀU ảnh
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                            if (!listImages.contains(uri)) {
                                listImages.add(uri);
                            }
                        }
                    } else if (result.getData().getData() != null) {
                        // Trường hợp khách chỉ chọn 1 ảnh
                        listImages.add(result.getData().getData());
                    }
                    // Cập nhật lại RecyclerView
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
                            if (!listVideos.contains(uri)) {
                                listVideos.add(uri);
                            }
                        }
                    } else if (result.getData().getData() != null) {
                        listVideos.add(result.getData().getData());
                    }
                    // Cập nhật lại RecyclerView
                    if (videoAdapter != null) videoAdapter.notifyDataSetChanged();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChiTietLaptop2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Slidr.attach(this);
        setSupportActionBar(binding.toolbarct);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết sản phẩm");
        }
        binding.toolbarct.setNavigationOnClickListener(v -> finish());

        laptopModel = (laptop_model) getIntent().getSerializableExtra("laptop");
        if (laptopModel != null) {
            loadChiTiet();
            // Gọi hàm này để lấy số lượng "sống" từ Database
            getLatestLaptopData(laptopModel.MaLapTop);
        }
        if (laptopModel == null) { finish(); return; }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, 1001);
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            }
        }
        setupReviewRecyclerView();
        loadChiTiet();
        new Handler().postDelayed(() ->{
            loadSpinnerSoLuong();
            loadSlide();
            loadDanhGiaFromServer();
        },500);
        binding.btnthemGioHang.setOnClickListener(v -> themVaoGioHangServer(false));
        binding.btnmuangay.setOnClickListener(v -> themVaoGioHangServer(true));
        binding.tvTenlaptopChitiet.setOnClickListener(v -> showDialogDanhGia());
    }
    private void getLatestLaptopData(int maLaptop) {
        String url = SERVER.url_get_laptop_detail + "?malaptop=" + maLaptop;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        // 1. Cập nhật các con số "nhạy cảm"
                        int soLuongBanMoi = obj.getInt("SoLuongBan");
                        int tonKhoMoi = obj.getInt("SoLuong");
                        long giaMoi = obj.getLong("Gia");

                        // 2. Cập nhật dữ liệu vào object laptopModel hiện tại
                        laptopModel.SoLuongBan = soLuongBanMoi;
                        laptopModel.SoLuong = tonKhoMoi;
                        laptopModel.Gia = (int) giaMoi;
                        laptopModel.MoTa = obj.getString("MoTa");
                        laptopModel.CPU = obj.getString("CPU");
                        laptopModel.HangSX = obj.getString("HangSX");
                        laptopModel.HinhChiTiet = obj.getString("HinhChiTiet");

                        // 3. Đổ ngược lại lên giao diện (Đây là lúc bồ thấy dữ liệu đầy đủ)
                        binding.tvSoluongbanChitiet.setText("Đã bán: " + soLuongBanMoi);
                        binding.tvGiaChitiet.setText(java.text.NumberFormat.getNumberInstance().format(giaMoi) + " đ");
                        binding.tvMotachitiet.setText(laptopModel.MoTa);
                        binding.tvCPUChitiet.setText(laptopModel.CPU);
                        binding.tvHangChitiet.setText(laptopModel.HangSX);
                        loadSpinnerSoLuong();

                        loadSlide();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("API_ERROR", "Lỗi đồng bộ dữ liệu")
        );
        Volley.newRequestQueue(this).add(request);
    }
    private void uploadDanhGiaMultipart(String noiDung, int soSao) {
        try {
            // 1. Khởi tạo Request
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

            // Gắn các thông số chữ
            request.addParameter("ma_laptop", String.valueOf(laptopModel.MaLapTop))
                    .addParameter("sdt", SERVER.user.SDT)
                    .addParameter("ten_kh", SERVER.user.TenKH != null ? SERVER.user.TenKH : "Khách hàng")
                    .addParameter("noi_dung", noiDung)
                    .addParameter("so_sao", String.valueOf(soSao))
                    .setMethod("POST")
                    .setNotificationConfig((context, uploadId) -> {
                        return new net.gotev.uploadservice.data.UploadNotificationConfig(
                                "upload_channel_id",
                                false,
                                new net.gotev.uploadservice.data.UploadNotificationStatusConfig("Đang tải lên...", "Vui lòng chờ"),
                                new net.gotev.uploadservice.data.UploadNotificationStatusConfig("Thành công!", "Đánh giá đã gửi"),
                                new net.gotev.uploadservice.data.UploadNotificationStatusConfig("Lỗi!", "Tải lên thất bại"),
                                new net.gotev.uploadservice.data.UploadNotificationStatusConfig("Đã hủy", "Đã dừng upload")
                        );
                    });

            net.gotev.uploadservice.observer.request.RequestObserver observer =
                    new net.gotev.uploadservice.observer.request.RequestObserver(this, this, new RequestObserverDelegate() {
                        @Override public void onCompletedWhileNotObserving() {}

                        @Override
                        public void onProgress(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}

                        @Override
                        public void onSuccess(@NonNull Context context, @NonNull UploadInfo uploadInfo, @NonNull ServerResponse serverResponse) {
                            CustomDialogHelper dialogHelper = new CustomDialogHelper(ChiTietLaptopActivity2.this);
                            dialogHelper.showSuccess("Tuyệt vời!", "Đánh giá của bạn đã được gửi thành công.", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    loadDanhGiaFromServer(); // Load lại dữ liệu
                                }
                            });
                        }

                        @Override
                        public void onError(@NonNull Context context, @NonNull UploadInfo uploadInfo, @NonNull Throwable exception) {
                            Toast.makeText(context, "Lỗi: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override public void onCompleted(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}
                         public void onCancelled(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}
                    });

            // 4. Đăng ký observer và bắt đầu chạy
            observer.subscribe(request);
            request.startUpload();

            Toast.makeText(this, "Đang gửi đánh giá...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("UPLOAD_ERROR", e.getMessage());
        }
    }

    private void showDialogDanhGia() {
        if (SERVER.user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_danh_gia, null);
        builder.setView(v);

        RatingBar rb = v.findViewById(R.id.rbDanhGia);
        EditText edt = v.findViewById(R.id.edtNoiDungDG);
        Button btnGui = v.findViewById(R.id.btnGuiDG);
        Button btnChonAnh = v.findViewById(R.id.btnChonAnh);
        Button btnChonVideo = v.findViewById(R.id.btnChonVideo);

        // Ánh xạ 2 cái RecyclerView
        androidx.recyclerview.widget.RecyclerView rcvImg = v.findViewById(R.id.rcvSelectedImages);
        androidx.recyclerview.widget.RecyclerView rcvVid = v.findViewById(R.id.rcvSelectedVideos);

        // Reset list mỗi lần mở bảng
        listImages.clear();
        listVideos.clear();

        // 1. Cài đặt Adapter cho ẢNH
        imageAdapter = new SelectedMediaAdapter(this, listImages, false, position -> {
            listImages.remove(position); // Xóa khỏi danh sách khi bấm X
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, listImages.size());
        });
        rcvImg.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvImg.setAdapter(imageAdapter);

        // 2. Cài đặt Adapter cho VIDEO
        videoAdapter = new SelectedMediaAdapter(this, listVideos, true, position -> {
            listVideos.remove(position); // Xóa khỏi danh sách khi bấm X
            videoAdapter.notifyItemRemoved(position);
            videoAdapter.notifyItemRangeChanged(position, listVideos.size());
        });
        rcvVid.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvVid.setAdapter(videoAdapter);

        AlertDialog dialog = builder.create();

        // 3. Sự kiện bấm nút "Thêm Ảnh" (Bật chế độ chọn nhiều)
        btnChonAnh.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // CHÌA KHÓA LÀ ĐÂY
            imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh (có thể chọn nhiều)"));
        });

        // 4. Sự kiện bấm nút "Thêm Video" (Bật chế độ chọn nhiều)
        btnChonVideo.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // CHÌA KHÓA LÀ ĐÂY
            videoPickerLauncher.launch(Intent.createChooser(intent, "Chọn video (có thể chọn nhiều)"));
        });

        // 5. Nút Gửi Đánh Giá
        btnGui.setOnClickListener(view -> {
            String nd = edt.getText().toString().trim();
            if (nd.isEmpty()) {
                Toast.makeText(this, "Hãy nhập nội dung!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Truyền nội dung và số sao vào hàm gửi
            uploadDanhGiaMultipart(nd, (int) rb.getRating());
            dialog.dismiss();
        });

        dialog.show();
    }

    // Hàm này sẽ copy video từ thư viện vào bộ nhớ tạm của App để lấy đường dẫn thật
    // Tách biệt tên file và không bao giờ bị trùng
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

    private void setupReviewRecyclerView() {
        mangDanhGia = new ArrayList<>();
        adapterDanhGia = new DanhGiaAdapter(this, mangDanhGia, new DanhGiaAdapter.OnReviewActionListener() {
            @Override public void onEdit(DanhGia dg) {
                showDialogEditDanhGia(dg);
            }
            @Override public void onDelete(DanhGia dg) { confirmDelete(dg); }
        });
        binding.rcvDanhGia.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvDanhGia.setAdapter(adapterDanhGia);
    }
    // Hiện Dialog cho khách sửa nội dung
    // HÀM HIỂN THỊ DIALOG SỬA ĐÁNH GIÁ (Update mới cho RecyclerView)
    private void showDialogEditDanhGia(DanhGia dg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_danh_gia, null);
        builder.setView(v);

        RatingBar rb = v.findViewById(R.id.rbDanhGia);
        EditText edt = v.findViewById(R.id.edtNoiDungDG);
        Button btnGui = v.findViewById(R.id.btnGuiDG);
        Button btnChonAnh = v.findViewById(R.id.btnChonAnh);
        Button btnChonVideo = v.findViewById(R.id.btnChonVideo);

        // Ánh xạ 2 cái RecyclerView
        androidx.recyclerview.widget.RecyclerView rcvImg = v.findViewById(R.id.rcvSelectedImages);
        androidx.recyclerview.widget.RecyclerView rcvVid = v.findViewById(R.id.rcvSelectedVideos);

        // Reset dữ liệu
        listImages.clear();
        listVideos.clear();

        // Gắn dữ liệu cũ
        rb.setRating(dg.SoSao);
        edt.setText(dg.NoiDung);
        btnGui.setText("Cập nhật");

        // Load ảnh cũ vào danh sách (Cắt chuỗi bằng dấu ;)
        if (dg.HinhAnh != null && !dg.HinhAnh.isEmpty() && !dg.HinhAnh.equals("null")) {
            String[] arrImg = dg.HinhAnh.split(";");
            for (String img : arrImg) {
                if (!img.trim().isEmpty()) {
                    listImages.add(Uri.parse(SERVER.server_add + img.trim()));
                }
            }
        }

        // Load video cũ vào danh sách (Cắt chuỗi bằng dấu ;)
        if (dg.Video != null && !dg.Video.isEmpty() && !dg.Video.equals("null")) {
            String[] arrVid = dg.Video.split(";");
            for (String vid : arrVid) {
                if (!vid.trim().isEmpty()) {
                    listVideos.add(Uri.parse(SERVER.server_add + vid.trim()));
                }
            }
        }

        // Cài đặt Adapter cho ẢNH
        imageAdapter = new SelectedMediaAdapter(this, listImages, false, position -> {

            listImages.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, listImages.size());
        });
        rcvImg.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvImg.setAdapter(imageAdapter);

        // Cài đặt Adapter cho VIDEO
        videoAdapter = new SelectedMediaAdapter(this, listVideos, true, position -> {
            listVideos.remove(position);
            videoAdapter.notifyItemRemoved(position);
            videoAdapter.notifyItemRangeChanged(position, listVideos.size());
        });
        rcvVid.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvVid.setAdapter(videoAdapter);

        AlertDialog dialog = builder.create();

        // Bấm chọn ảnh mới
        btnChonAnh.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
        });

        // Bấm chọn video mới
        btnChonVideo.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            videoPickerLauncher.launch(Intent.createChooser(intent, "Chọn video"));
        });

        // Bấm nút Cập nhật
        btnGui.setOnClickListener(view -> {
            String noiDungMoi = edt.getText().toString().trim();
            if (noiDungMoi.isEmpty()) return;
            editDanhGiaMultipart(dg.MaDG, noiDungMoi, (int) rb.getRating());
            dialog.dismiss();
        });

        dialog.show();
    }

    // HÀM ĐẨY DỮ LIỆU SỬA LÊN SERVER (Gửi nhiều file)
    // HÀM ĐẨY DỮ LIỆU SỬA LÊN SERVER
    private void editDanhGiaMultipart(int maDG, String noiDungMoi, int soSaoMoi) {
        try {
            MultipartUploadRequest request = new MultipartUploadRequest(this, SERVER.upload_edit_danhgia);

            // 1. Xử lý ẢNH: Tách riêng ảnh CŨ giữ lại và ảnh MỚI
            StringBuilder oldImagesToKeep = new StringBuilder();
            for (Uri uri : listImages) {
                String url = uri.toString();
                if (url.startsWith("http")) {
                    // Nếu là link http (ảnh cũ), cắt bỏ tên miền để lấy đường dẫn gốc lưu vào DB
                    String relativePath = url.replace(SERVER.server_add, "");
                    if (oldImagesToKeep.length() > 0) oldImagesToKeep.append(";");
                    oldImagesToKeep.append(relativePath);
                } else {
                    // Nếu là Uri điện thoại (ảnh mới), đính kèm vào file để upload
                    String imgPath = getPathFromUri(uri, false);
                    if (imgPath != null) request.addFileToUpload(imgPath, "image_files[]");
                }
            }

            // 2. Xử lý VIDEO: Tách riêng video CŨ giữ lại và video MỚI
            StringBuilder oldVideosToKeep = new StringBuilder();
            for (Uri uri : listVideos) {
                String url = uri.toString();
                if (url.startsWith("http")) {
                    String relativePath = url.replace(SERVER.server_add, "");
                    if (oldVideosToKeep.length() > 0) oldVideosToKeep.append(";");
                    oldVideosToKeep.append(relativePath);
                } else {
                    String vidPath = getPathFromUri(uri, true);
                    if (vidPath != null) request.addFileToUpload(vidPath, "video_files[]");
                }
            }

            // 3. Đóng gói gửi lên Server
            request.addParameter("ma_dg", String.valueOf(maDG))
                    .addParameter("sdt", SERVER.user.SDT)
                    .addParameter("noi_dung", noiDungMoi)
                    .addParameter("so_sao", String.valueOf(soSaoMoi))
                    .addParameter("old_images", oldImagesToKeep.toString()) // Gửi mảng ảnh cũ giữ lại
                    .addParameter("old_videos", oldVideosToKeep.toString()) // Gửi mảng video cũ giữ lại
                    .setMethod("POST");

            net.gotev.uploadservice.observer.request.RequestObserver observer =
                    new net.gotev.uploadservice.observer.request.RequestObserver(this, this, new RequestObserverDelegate() {
                        @Override public void onCompletedWhileNotObserving() {}
                        @Override public void onProgress(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}
                        @Override public void onSuccess(@NonNull Context context, @NonNull UploadInfo uploadInfo, @NonNull ServerResponse serverResponse) {
                            CustomDialogHelper dialogHelper = new CustomDialogHelper(ChiTietLaptopActivity2.this);
                            dialogHelper.showSuccess("Tuyệt vời!", "Đánh giá của bạn đã được gửi thành công.", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    loadDanhGiaFromServer(); // Load lại dữ liệu
                                }
                            });
                        }
                        @Override public void onError(@NonNull Context context, @NonNull UploadInfo uploadInfo, @NonNull Throwable exception) {
                            Toast.makeText(context, "Lỗi cập nhật: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onCompleted(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}
                        public void onCancelled(@NonNull Context context, @NonNull UploadInfo uploadInfo) {}
                    });

            observer.subscribe(request);
            request.startUpload();
            Toast.makeText(this, "Đang cập nhật...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("UPLOAD_ERROR", e.getMessage());
        }
    }

    private void loadDanhGiaFromServer() {
        String url = SERVER.server_add + "get_danhgia.php?ma_laptop=" + laptopModel.MaLapTop;
        Volley.newRequestQueue(this).add(new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangDanhGia.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            mangDanhGia.add(new DanhGia(
                                    obj.getInt("MaDG"), obj.getString("SDT"), obj.getString("TenKH"),
                                    obj.getString("NoiDung"), obj.getInt("SoSao"), obj.getString("NgayDanhGia"),
                                    obj.getString("HinhAnh"), obj.optString("Video", "")
                            ));
                        }
                        adapterDanhGia.notifyDataSetChanged();
                        binding.scrollChiTiet.postDelayed(() -> {
                            binding.scrollChiTiet.scrollTo(0, 0);
                        }, 100);
                    } catch (Exception e) { e.printStackTrace(); }
                }, error -> {}));
    }

//    //đoạn nén ảnh trong pickerLauncher
//    private String encodeImageToBase64(Bitmap bitmap) {
//        // Chạy trong một biến local hoặc luồng riêng để không treo UI
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream); // Giảm xuống 60% cho nhẹ
//        byte[] byteArray = outputStream.toByteArray();
//        return Base64.encodeToString(byteArray, Base64.DEFAULT);
//    }

    private void loadChiTiet() {
        binding.tvTenlaptopChitiet.setText(laptopModel.TenLapTop);
        binding.tvGiaChitiet.setText(NumberFormat.getNumberInstance().format(laptopModel.Gia) + " đ");
        binding.tvSoluongbanChitiet.setText("Đã bán: " + laptopModel.SoLuongBan);
        binding.tvMotachitiet.setText(laptopModel.MoTa);
        binding.tvCPUChitiet.setText(laptopModel.CPU);
        binding.tvHangChitiet.setText(laptopModel.HangSX);
    }

    private void loadSpinnerSoLuong() {
        ArrayList<String> arr = new ArrayList<>();
        int soLuongTonKho = laptopModel.SoLuong; // Lấy tồn kho thật

        if (soLuongTonKho <= 0) {
            binding.edtSpinnerSlmua.setText("Hết");
            binding.edtSpinnerSlmua.setEnabled(false);
            binding.btnthemGioHang.setEnabled(false);
            binding.btnmuangay.setEnabled(false);
            binding.btnthemGioHang.setText("Hết hàng");
            binding.btnmuangay.setText("Hết hàng");
        } else {
            // Giới hạn menu chọn thả xuống tối đa 50 cái cho gọn
            int maxMua = Math.min(soLuongTonKho, 50);
            for (int i = 1; i <= maxMua; i++) {
                arr.add(String.valueOf(i));
            }
            binding.edtSpinnerSlmua.setEnabled(true);
            // Đặt mặc định là 1 (chữ false để nó không tự xổ menu ra)
            binding.edtSpinnerSlmua.setText("1", false);
        }

        // Nạp dữ liệu vào cái ô tam giác
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, arr);
        binding.edtSpinnerSlmua.setAdapter(adapter);
    }

    private void loadSlide() {
        ArrayList<HINH_SLIDE_MODEL> list = new ArrayList<>();

        // 1. Kiểm tra kỹ HinhChiTiet
        if (laptopModel.HinhChiTiet != null && !laptopModel.HinhChiTiet.trim().isEmpty()) {
            String[] arrHinh = laptopModel.HinhChiTiet.split(";");
            for (String h : arrHinh) {
                if (!h.trim().isEmpty()) {
                    // Nếu link ảnh chỉ là tên file (vd: "laptop1.jpg"), hãy ghép thêm đường dẫn server vào
                    if (!h.contains("http")) {
                        list.add(new HINH_SLIDE_MODEL(SERVER.server_img + h.trim()));
                    } else {
                        list.add(new HINH_SLIDE_MODEL(h.trim()));
                    }
                }
            }
        }
        // Nếu sau khi tách chuỗi mà list vẫn rỗng, hãy lấy ảnh đại diện HinhAnh
        if (list.isEmpty()) {
            if (laptopModel.HinhAnh != null && !laptopModel.HinhAnh.isEmpty()) {
                // Kiểm tra xem HinhAnh đã full link chưa
                String fullLink = laptopModel.HinhAnh.contains("http") ? laptopModel.HinhAnh : SERVER.server_img + laptopModel.HinhAnh;
                list.add(new HINH_SLIDE_MODEL(fullLink));
            }
        }

        // Log ra để kiểm tra
        android.util.Log.d("DEBUG_SLIDE", "Số lượng ảnh slide: " + list.size());

        // 3. Set Adapter
        HINH_SLIDE_ADAPTER adapter = new HINH_SLIDE_ADAPTER(this, list);
        binding.viewpagerChitietlaptop.setAdapter(adapter);
        binding.circleIndicator.setViewPager(binding.viewpagerChitietlaptop);
        // --- KÍCH HOẠT TỰ ĐỘNG TRƯỢT ---
        binding.viewpagerChitietlaptop.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000); // 3 giây trượt 1 lần
            }
        });
    }

    private void themVaoGioHangServer(boolean isMuaNgay) {
        if (SERVER.user == null) { Toast.makeText(this, "Đăng nhập ngay", Toast.LENGTH_SHORT).show(); return; }

        // --- 1. LẤY SỐ LƯỢNG VÀ KIỂM TRA LỖI BỎ TRỐNG ---
        String textSl = binding.edtSpinnerSlmua.getText().toString().trim();
        if (textSl.isEmpty() || textSl.equals("0")) {
            Toast.makeText(this, "Vui lòng nhập số lượng hợp lệ!", Toast.LENGTH_SHORT).show();
            binding.edtSpinnerSlmua.setText("1", false); // Tự trả về 1
            return;
        }

        // --- 2. ÉP KIỂU AN TOÀN VÀ CHUYỂN THÀNH FINAL ---
        int tempSoLuong = 0; // Dùng biến tạm để lấy số
        try {
            tempSoLuong = Integer.parseInt(textSl);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một biến final chốt cứng số lượng lại để truyền vào Volley
        final int soLuong = tempSoLuong;

        // --- 3. KIỂM TRA TỒN KHO TRƯỚC KHI THÊM ---
        if (soLuong > laptopModel.SoLuong) {
            Toast.makeText(this, "Kho chỉ còn " + laptopModel.SoLuong + " sản phẩm!", Toast.LENGTH_SHORT).show();
            // Ép tự động hiện số max luôn cho khách đỡ mất công gõ lại
            binding.edtSpinnerSlmua.setText(String.valueOf(laptopModel.SoLuong), false);
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_them_gio_hang,
                response -> {
                    if (response.trim().equals("success")) {
                        Cart_manager.addToCart(laptopModel, soLuong);
                        invalidateOptionsMenu();
                        if (isMuaNgay) {
                            Cart_manager.loadCartFromServer(this);
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("open_cart", true);
                            startActivity(intent);
                            finish();
                        } else {
                            // Nếu bấm Thêm Giỏ Hàng -> BẬT HIỆU ỨNG BAY! 🚀
                            View cartIcon = findViewById(R.id.menu_cart);
                            if (cartIcon != null) {
                                animateAddToCart(binding.viewpagerChitietlaptop, cartIcon);
                            } else {
                                invalidateOptionsMenu();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {}) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maLaptop", String.valueOf(laptopModel.MaLapTop));
                p.put("sdt", SERVER.user.SDT);
                p.put("soLuong", String.valueOf(soLuong)); // Truyền đúng số lượng xịn vào đây
                p.put("tenLaptop", laptopModel.TenLapTop);
                p.put("gia", String.valueOf(laptopModel.Gia));
                p.put("hinhAnh", laptopModel.HinhAnh);
                return p;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void confirmDelete(DanhGia dg) {
        CustomDialogHelper dialogHelper = new CustomDialogHelper(this);
        dialogHelper.showWarning(
                "Xác nhận xóa",
                "Bạn có chắc chắn muốn xóa đánh giá này không? Hành động này không thể hoàn tác.",
                new CustomDialogHelper.DialogActionListener() {
                    @Override
                    public void onPositiveClick() {
                        deleteDanhGiaServer(dg.MaDG); // Gọi API xóa trên Server
                    }
                }
        );
    }

    private void deleteDanhGiaServer(int maDG) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_edit_delete_danhgia,
                response -> loadDanhGiaFromServer(), error -> {}) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("action", "delete"); p.put("ma_dg", String.valueOf(maDG));
                p.put("sdt", SERVER.user.SDT); return p;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
    private void animateAddToCart(android.view.View productView, android.view.View cartIcon) {
        // 1. Lấy tọa độ
        int[] startLoc = new int[2];
        productView.getLocationInWindow(startLoc);
        int[] endLoc = new int[2];
        cartIcon.getLocationInWindow(endLoc);

        float startX = startLoc[0];
        float startY = startLoc[1];
        float endX = endLoc[0] + (cartIcon.getWidth() / 2f) - (productView.getWidth() / 2f);
        float endY = endLoc[1] + (cartIcon.getHeight() / 2f) - (productView.getHeight() / 2f);

        // 2. Tạo Ảnh bóng ma
        android.widget.ImageView cloneImg = new android.widget.ImageView(this);
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(productView.getWidth(), productView.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        productView.draw(canvas);
        cloneImg.setImageBitmap(bitmap);

        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(productView.getWidth(), productView.getHeight());
        cloneImg.setLayoutParams(params);
        cloneImg.setX(startX);
        cloneImg.setY(startY);

        android.view.ViewGroup root = (android.view.ViewGroup) getWindow().getDecorView().getRootView();
        root.addView(cloneImg);

        // 3. Vẽ quỹ đạo (Giảm độ cao vồng lên để đường bay gọn hơn)
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(startX, startY);
        float controlX = startX + (endX - startX) / 2f;
        float controlY = Math.min(startY, endY) - 250f; // GIẢM ĐỘ CAO VỒNG (Ví dụ từ 450 xuống 250)
        path.quadTo(controlX, controlY, endX, endY);

        // 4. Thiết lập Animation (BỎ XOAY, CHỈ GIỮ LẠI BAY & THU NHỎ)
        android.animation.ObjectAnimator pathAnim = android.animation.ObjectAnimator.ofFloat(cloneImg, android.view.View.X, android.view.View.Y, path);
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(cloneImg, android.view.View.SCALE_X, 1f, 0.1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(cloneImg, android.view.View.SCALE_Y, 1f, 0.1f);
        // Mờ dần hoàn toàn ở cuối
        android.animation.ObjectAnimator alpha = android.animation.ObjectAnimator.ofFloat(cloneImg, android.view.View.ALPHA, 1f, 0f);

        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(pathAnim, scaleX, scaleY, alpha); // Đã bỏ 'rotate'
        animatorSet.setDuration(500); // Giảm thời gian bay xuống 500ms cho cảm giác nhanh, gọn

        // Sử dụng Interpolator chuẩn Material Design cho chuyển động mượt mà
        // (Bắt đầu nhanh, kết thúc chậm dần khi vào giỏ)
        animatorSet.setInterpolator(new androidx.interpolator.view.animation.FastOutSlowInInterpolator());

        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                root.removeView(cloneImg);

                // 5. HIỆU ỨNG GIỎ HÀNG CHUYÊN NGHIỆP (POP NHANH GỌN)
                // Phóng to nhẹ lên 1.2 lần thật nhanh (100ms)
                cartIcon.animate()
                        .scaleX(1.2f).scaleY(1.2f)
                        .setDuration(100)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator()) // Phóng nhanh lúc đầu
                        .withEndAction(() -> {
                            // Thu nhỏ về lại kích thước gốc thật nhanh (100ms)
                            cartIcon.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .setInterpolator(new android.view.animation.AccelerateInterpolator()) // Thu nhanh lúc cuối
                                    .start();
                            updateBadge();
                            invalidateOptionsMenu(); // Cập nhật số Badge
                        }).start();
            }
        });

        animatorSet.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);

        // Lấy MenuItem giỏ hàng
        MenuItem itemCart = menu.findItem(R.id.menu_cart);

        // Lấy cái Layout xịn (layout_cart_badge)
        View actionView = itemCart.getActionView();

        if (actionView != null) {
            tvBadgeCount = actionView.findViewById(R.id.tv_cart_count_badge);

            // Bắt sự kiện click vào cả cái cụm đó
            actionView.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("open_cart", true);
                startActivity(intent);
            });
        }

        updateBadge(); // Gọi hàm này để nó tự điền số vào tvBadgeCount
        return true;
    }

    // Hàm cập nhật số hiển thị trên cục đỏ
    private void updateBadge() {
        int total = Cart_manager.getTotalQuantity();
        if (tvBadgeCount != null) {
            if (total > 0) {
                tvBadgeCount.setVisibility(View.VISIBLE);
                tvBadgeCount.setText(String.valueOf(total));
            } else {
                tvBadgeCount.setVisibility(View.GONE);
            }
        }
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cart) {
            startActivity(new Intent(this, MainActivity.class).putExtra("open_cart", true));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cartListener = new Cart_manager.CartListener() {
            @Override
            public void onCartChanged() {
                runOnUiThread(() -> {
                    updateBadge(); // Cập nhật cái số màu đỏ
                    invalidateOptionsMenu(); // Vẽ lại menu
                });
            }
        };
        Cart_manager.addListener(cartListener);
        updateBadge(); // Gọi lần đầu để chắc chắn có số khi vừa vào trang
    }
    @Override protected void onStop() { super.onStop(); Cart_manager.removeListener(cartListener); }
    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable); // Tạm dừng khi ẩn app
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000); // Chạy lại khi mở app
    }
    // --- HÀM MỞ ẢNH ZOOM TOÀN MÀN HÌNH (Dùng PhotoView) ---
    public void showZoomImageDialog(String imageUrl) {
        // 1. Khởi tạo Dialog toàn màn hình, không có thanh tiêu đề
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        // 2. Nạp giao diện từ file XML vừa tạo ở Bước 2
        dialog.setContentView(R.layout.dialog_zoom_image);

        // 3. Ánh xạ các view trong dialog
        com.github.chrisbanes.photoview.PhotoView photoView = dialog.findViewById(R.id.photo_view_zoom);
        ImageButton btnClose = dialog.findViewById(R.id.btn_close_zoom);

        // 4. Load ảnh vào PhotoView dùng Picasso
        com.squareup.picasso.Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.no_image) // Ảnh chờ nếu mạng lag
                .into(photoView);

        // 5. Sự kiện bấm nút X thì đóng dialog
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // (Tùy chọn) Bấm vào chính cái ảnh cũng đóng luôn cho tiện
        photoView.setOnClickListener(v-> dialog.dismiss());

        // 6. Hiện dialog lên
        dialog.show();
    }
}