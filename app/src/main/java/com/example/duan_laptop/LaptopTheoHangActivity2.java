package com.example.duan_laptop;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.LAPTOP_ADAPTER;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.databinding.ActivityLaptopTheoHang2Binding;
import com.r0adkll.slidr.Slidr;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LaptopTheoHangActivity2 extends AppCompatActivity {

    ActivityLaptopTheoHang2Binding binding;
    ArrayList<laptop_model> arrLaptop = new ArrayList<>();
    LAPTOP_ADAPTER adapter;

    String masx = "";

    // === CÁC BIẾN CHO LOAD MORE ===
    int page = 1;
    boolean isLoading = false;
    boolean isLastPage = false;
    ProgressBar progressBar;
    ArrayList<laptop_model> arrLaptopBackup = new ArrayList<>();
    String loaiLaptop = "";
    String tuKhoaSearch;
    String sortOrder = ""; // Chứa chữ "ASC" (Tăng) hoặc "DESC" (Giảm)
    String priceFilter = ""; // Chứa mảng giá (VD: "1,2" -> Dưới 10tr và 10-20tr)
    String cpuFilter = ""; // Chứa mảng CPU (VD: "Core i5,Core i7")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLaptopTheoHang2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Slidr.attach(this);
        // Lấy mã sản xuất từ màn hình trước gửi sang
        masx = getIntent().getStringExtra("MACHUDE");
        if (masx == null) masx = "0";
        loaiLaptop = getIntent().getStringExtra("LOAILAPTOP");
        if (loaiLaptop == null) loaiLaptop = "";
        tuKhoaSearch = getIntent().getStringExtra("TUKHOA_SEARCH");

        // Ánh xạ ProgressBar từ layout (để ngoài ViewBinding cho gọn)
        progressBar = findViewById(R.id.progressBarLoadMore);

        // Khởi tạo Adapter
        adapter = new LAPTOP_ADAPTER(arrLaptop, this, new LAPTOP_ADAPTER.OnFavoriteClickListener() {
            @Override
            public void onFavoriteAdded(laptop_model laptop) {
                SERVER.addFavoriteToList(laptop.MaLapTop);
            }

            @Override
            public void onFavoriteRemoved(laptop_model laptop) {
                SERVER.removeFavoriteFromList(laptop.MaLapTop);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rcvlaptopTheoChude.setLayoutManager(gridLayoutManager);
        binding.rcvlaptopTheoChude.addItemDecoration(new SpacesItemDecoration(6)); // Thêm khoảng cách cho đẹp
        binding.rcvlaptopTheoChude.setAdapter(adapter);

        // === BẮT SỰ KIỆN VUỐT XUỐNG ĐÁY ===
        binding.rcvlaptopTheoChude.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // Nếu khách đang vuốt lên (kéo nội dung xuống)
                    int visibleItemCount = gridLayoutManager.getChildCount();
                    int totalItemCount = gridLayoutManager.getItemCount();
                    int pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && !isLastPage && binding.searchviewLaptop.getQuery().toString().isEmpty()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            page++;
                            loadLaptopTheoHang(page);
                        }
                    }
                }
            }
        });
        // Thiết lập kéo để làm mới
        binding.swipeRefreshTheoHang.setColorSchemeResources(android.R.color.holo_red_dark, android.R.color.holo_blue_dark,android.R.color.holo_green_dark);
        binding.swipeRefreshTheoHang.setOnRefreshListener(() -> {
            page = 1;
            isLastPage = false;
            arrLaptop.clear();
            arrLaptopBackup.clear();

            loadLaptopTheoHang(page);

            // Tắt vòng xoay sau khi load xong (hoặc sau 1.5s)
            new android.os.Handler().postDelayed(() ->
                    binding.swipeRefreshTheoHang.setRefreshing(false), 1500);
        });

        // Load trang đầu tiên
        loadLaptopTheoHang(page);
        binding.searchviewLaptop.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.searchviewLaptop.clearFocus(); // Chỉ cần ẩn bàn phím
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Search tới đâu lọc tới đó trên máy luôn
                String query = newText.toLowerCase().trim();
                ArrayList<laptop_model> filteredList = new ArrayList<>();

                for (laptop_model item : arrLaptopBackup) {
                    if (item.TenLapTop.toLowerCase().contains(query)) {
                        filteredList.add(item);
                    }
                }

                // Cập nhật lại danh sách hiển thị
                arrLaptop.clear();
                arrLaptop.addAll(filteredList);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Đóng màn hình này lại, tự động lùi về HOME_FRAGMENT
        });
        binding.btnFilter.setOnClickListener(v -> {
            showFilterBottomSheet();
        });

    }
    private void loadLaptopTheoHang(int pageToLoad) {
        // Nếu đang gõ search thì tạm dừng load trang mới từ server để tránh loạn danh sách
        if (!binding.searchviewLaptop.getQuery().toString().isEmpty()) return;
        isLoading = true;
        if (pageToLoad > 1) {
            progressBar.setVisibility(View.VISIBLE); // Bật vòng xoay nếu đang load trang 2, 3...
        }

        StringRequest request = new StringRequest(StringRequest.Method.POST, SERVER.url_getLaptop,
                response -> {
                    progressBar.setVisibility(View.GONE); // Tắt vòng xoay
                    try {
                        JSONArray arr = new JSONArray(response);

                        // Nếu server trả về mảng rỗng -> Báo hiệu đã hết data
                        if (arr.length() == 0) {
                            isLastPage = true;
                        } else {
                            // Không dùng clear() ở đây nữa để nối tiếp danh sách
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                laptop_model lap = new laptop_model();
                                lap.MaLapTop = obj.optInt("MaLaptop", 0);
                                lap.TenLapTop = obj.optString("TenLaptop", "Đang cập nhật");
                                lap.Gia = obj.optInt("Gia", 0);
                                lap.HinhAnh = obj.optString("HinhAnh", "");
                                lap.HangSX = obj.optString("HangSX", "Khác");
                                lap.HinhChiTiet = obj.optString("HinhChiTiet", "");
                                lap.MoTa = obj.optString("MoTa", "");
                                lap.CPU = obj.optString("CPU", "");
                                lap.SoLuongBan = obj.optInt("SoLuongBan", 0);
                                lap.isFavorite = obj.optInt("isFavorite", 0) == 1;

                                arrLaptop.add(lap); // Nối vào đuôi mảng
                                arrLaptopBackup.add(lap); // Nối vào mảng dự phòng để search
                            }
                            adapter.notifyDataSetChanged(); // Cập nhật giao diện
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isLoading = false; // Mở khóa
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    isLoading = false; // Mở khóa
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> map = new HashMap<>();
                map.put("masx", masx);
                map.put("laptopgi", loaiLaptop);
                map.put("SDT", SERVER.getSafeSDT());
                map.put("page", String.valueOf(pageToLoad)); // Gửi số trang cho PHP
                if (tuKhoaSearch != null && !tuKhoaSearch.isEmpty()) {
                    map.put("tuKhoa", tuKhoaSearch);
                }
                if (!sortOrder.isEmpty()) map.put("sortOrder", sortOrder);
                if (!priceFilter.isEmpty()) map.put("priceFilter", priceFilter);
                if (!cpuFilter.isEmpty()) map.put("cpuFilter", cpuFilter);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
    private void showFilterBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        // Nạp cái giao diện XML bồ vừa tạo lúc nãy
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_filter, null);
        bottomSheetDialog.setContentView(view);

        // Ánh xạ View trong bảng lọc
        android.widget.RadioGroup rgSort = view.findViewById(R.id.rgSort);
        android.widget.RadioButton rbGiaTang = view.findViewById(R.id.rbGiaTang);
        android.widget.RadioButton rbGiaGiam = view.findViewById(R.id.rbGiaGiam);

        android.widget.CheckBox cbGia1 = view.findViewById(R.id.cbGia1);
        android.widget.CheckBox cbGia2 = view.findViewById(R.id.cbGia2);
        android.widget.CheckBox cbGia3 = view.findViewById(R.id.cbGia3);

        android.widget.CheckBox cbCpuI3 = view.findViewById(R.id.cbCpuI3);
        android.widget.CheckBox cbCpuI5 = view.findViewById(R.id.cbCpuI5);
        android.widget.CheckBox cbCpuI7 = view.findViewById(R.id.cbCpuI7);
        android.widget.CheckBox cbCpuRyzen = view.findViewById(R.id.cbCpuRyzen);

        android.widget.Button btnBoLoc = view.findViewById(R.id.btnBoLoc);
        android.widget.Button btnApDungLoc = view.findViewById(R.id.btnApDungLoc);
        // 1. Phục hồi trí nhớ Sắp xếp
        if (sortOrder.equals("ASC")) rbGiaTang.setChecked(true);
        else if (sortOrder.equals("DESC")) rbGiaGiam.setChecked(true);

        // 2. Phục hồi trí nhớ Mức giá
        if (priceFilter.contains("1")) cbGia1.setChecked(true);
        if (priceFilter.contains("2")) cbGia2.setChecked(true);
        if (priceFilter.contains("3")) cbGia3.setChecked(true);

        // 3. Phục hồi trí nhớ CPU
        if (cpuFilter.contains("Core i3")) cbCpuI3.setChecked(true);
        if (cpuFilter.contains("Core i5")) cbCpuI5.setChecked(true);
        if (cpuFilter.contains("Core i7")) cbCpuI7.setChecked(true);
        if (cpuFilter.contains("Ryzen")) cbCpuRyzen.setChecked(true);

        // --- Bắt sự kiện nút THIẾT LẬP LẠI (Xóa trắng) ---
        btnBoLoc.setOnClickListener(v -> {
            rgSort.clearCheck();
            cbGia1.setChecked(false); cbGia2.setChecked(false); cbGia3.setChecked(false);
            cbCpuI3.setChecked(false); cbCpuI5.setChecked(false); cbCpuI7.setChecked(false); cbCpuRyzen.setChecked(false);
        });

        // --- Bắt sự kiện nút ÁP DỤNG ---
        btnApDungLoc.setOnClickListener(v -> {
            // 1. Lấy Sắp xếp
            if (rbGiaTang.isChecked()) sortOrder = "ASC";
            else if (rbGiaGiam.isChecked()) sortOrder = "DESC";
            else sortOrder = "";

            // 2. Lấy Mức giá (Nối các lựa chọn bằng dấu phẩy)
            ArrayList<String> arrGia = new ArrayList<>();
            if (cbGia1.isChecked()) arrGia.add("1"); // 1 là Dưới 10 triệu
            if (cbGia2.isChecked()) arrGia.add("2"); // 2 là 10 - 20 triệu
            if (cbGia3.isChecked()) arrGia.add("3"); // 3 là Trên 20 triệu
            priceFilter = android.text.TextUtils.join(",", arrGia); // Ví dụ ra chuỗi: "1,2"

            // 3. Lấy CPU (Nối bằng dấu phẩy)
            ArrayList<String> arrCpu = new ArrayList<>();
            if (cbCpuI3.isChecked()) arrCpu.add("Core i3");
            if (cbCpuI5.isChecked()) arrCpu.add("Core i5");
            if (cbCpuI7.isChecked()) arrCpu.add("Core i7");
            if (cbCpuRyzen.isChecked()) arrCpu.add("Ryzen");
            cpuFilter = android.text.TextUtils.join(",", arrCpu); // Ví dụ ra chuỗi: "Core i5,Core i7"

            bottomSheetDialog.dismiss(); // Đóng bảng lọc

            // 4. Reset danh sách để tải lại từ trang 1 với bộ lọc mới
            page = 1;
            arrLaptop.clear();
            arrLaptopBackup.clear();
            adapter.notifyDataSetChanged();
            isLastPage = false;

            // Gọi lại API tải dữ liệu
            loadLaptopTheoHang(page);
        });

        bottomSheetDialog.show();
    }
}