package com.example.duan_laptop.FRAGMENT;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.HANG_ADAPTER;
import com.example.duan_laptop.ADAPTER.LAPTOP_ADAPTER;
import com.example.duan_laptop.ChatActivity2;
import com.example.duan_laptop.LaptopTheoHangActivity2;
import com.example.duan_laptop.MODEL.HANG_MODEL;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.R;
import com.example.duan_laptop.SERVER;
import com.example.duan_laptop.SpacesItemDecoration;
import com.example.duan_laptop.databinding.FragmentHomeLayoutBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HOME_FRAGMENT extends Fragment {
    FloatingActionButton fabMain, fabChatAI, fabMaps;
    View fabOverlay;
    boolean isMenuOpen = false;

    private FragmentHomeLayoutBinding binding;

    private final ArrayList<HANG_MODEL> arrHang = new ArrayList<>();
    private final ArrayList<laptop_model> arrLaptopMoi = new ArrayList<>();
    private final ArrayList<laptop_model> arrLaptopBanChay = new ArrayList<>();
    private final ArrayList<laptop_model> arrLaptopMoiBackup = new ArrayList<>();
    private final ArrayList<laptop_model> arrLaptopBanChayBackup = new ArrayList<>();

    private HANG_ADAPTER hangAdapter;
    private LAPTOP_ADAPTER laptopMoiAdapter;
    private LAPTOP_ADAPTER laptopBanChayAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeLayoutBinding.inflate(inflater, container, false);
        binding.rvChudeLaptop.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        binding.rcvlaptopBanChay.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        binding.rcvLaptopMoi.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        // FIX scroll giật + load trễ
        binding.rvChudeLaptop.setNestedScrollingEnabled(false);
        binding.rcvLaptopMoi.setNestedScrollingEnabled(false);
        binding.rcvlaptopBanChay.setNestedScrollingEnabled(false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabMain = view.findViewById(R.id.fabMain);
        fabChatAI = view.findViewById(R.id.fabChatAI);
        fabMaps = view.findViewById(R.id.fabMaps);
        fabOverlay = view.findViewById(R.id.fabOverlay);
        fabMain.setOnClickListener(v -> {
            if (!isMenuOpen) {
                showFabMenu();
            } else {
                closeFabMenu();
            }
        });

// Bấm ra ngoài khoảng đen để đóng menu
        fabOverlay.setOnClickListener(v -> closeFabMenu());
//        Sự kiện mở Google Maps Chỉ đường
        fabMaps.setOnClickListener(v -> {
            closeFabMenu();
            String lat = "10.77520768443988";
            String lng = "106.63348015533363";

            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(getContext(), "Máy bạn chưa cài Google Maps!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.fabChatAI.setOnClickListener(v -> {
            // Mở màn hình ChatActivity
            Intent intent = new Intent(getContext(), ChatActivity2.class);
            startActivity(intent);
        });

        SERVER.setHomeFragment(this);

        setupRecyclerView();
        loadAllData();
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadAllData(); // Khi kéo xuống thì gọi lại hàm này
        });
        binding.swipeRefresh.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN);
        // Bắt sự kiện bấm "Xem tất cả" cho Bán chạy
        binding.tvXemTatCaBanChay.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LaptopTheoHangActivity2.class);
            // Gửi mật thư: Không lọc theo hãng nào cả, nhưng lọc theo laptop bán chạy
            intent.putExtra("MACHUDE", "0");
            intent.putExtra("LOAILAPTOP", "laptopbanchay");
            startActivity(intent);
        });

// Bắt sự kiện bấm "Xem tất cả" cho Laptop Mới
        binding.tvXemTatCaMoi.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LaptopTheoHangActivity2.class);
            intent.putExtra("MACHUDE", "0");
            intent.putExtra("LOAILAPTOP", "laptopmoi");
            startActivity(intent);
        });
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu);
                MenuItem searchItem = menu.findItem(R.id.menu_search);
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint("Bạn tìm Laptop gì...?");

                // --- XỬ LÝ LỊCH SỬ KHI CLICK VÀO THANH SEARCH ---
                searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        // Khi người dùng chạm vào ô tìm kiếm (có tiêu điểm)
                        showSearchHistoryPopup(searchView);
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (!query.trim().isEmpty()) {
                            // LƯU LẠI LỊCH SỬ VÀO MÁY
                            luuLichSuSearch(query.trim());

                            searchView.clearFocus();
                            Intent intent = new Intent(getContext(), LaptopTheoHangActivity2.class);
                            intent.putExtra("TUKHOA_SEARCH", query.trim());
                            intent.putExtra("LOAILAPTOP", "search_server");
                            intent.putExtra("MACHUDE", "0");
                            startActivity(intent);
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return true;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner());
    }
    private void loadAllData() {
        // Bật vòng xoay nếu chưa quay (để khách biết là đang load)
        binding.swipeRefresh.setRefreshing(true);

        loadSlide();
        loadHang();
        loadLaptopMoi();
        loadLaptopBanChay();

        //Đợi 1 chút cho server trả về rồi mới tắt vòng xoay
        new android.os.Handler().postDelayed(() -> {
            if (binding.swipeRefresh != null) {
                binding.swipeRefresh.setRefreshing(false);
            }
        }, 1500);
    }
    // --- HÀM LƯU LỊCH SỬ VÀO MÁY ---
    private void luuLichSuSearch(String query) {
        android.content.SharedPreferences pref = requireActivity().getSharedPreferences("HISTORY_DATA", android.content.Context.MODE_PRIVATE);
        String list = pref.getString("search_list", "");

        // Tránh lưu trùng và giữ tối đa 5 từ khóa gần nhất
        if (!list.contains(query)) {
            pref.edit().putString("search_list", query + ";" + list).apply();
        }
    }

    // --- HÀM HIỆN POPUP LỊCH SỬ VÀ GỢI Ý ---
    private void showSearchHistoryPopup(SearchView searchView) {
        android.content.SharedPreferences pref = requireActivity().getSharedPreferences("HISTORY_DATA", android.content.Context.MODE_PRIVATE);
        String historyRaw = pref.getString("search_list", "");

        //Nếu chưa có lịch sử, mình hiện Gợi ý mặc định ---
        String[] displayList;
        if (historyRaw.isEmpty()) {
            displayList = new String[]{"🔍 Gaming", "🔍 Văn phòng", "🔍 Macbook", "🔍 Dell XPS"};
        } else {
            String[] arr = historyRaw.split(";");
            int count = Math.min(arr.length, 5);
            displayList = new String[count];
            for (int i = 0; i < count; i++) {
                displayList[i] =arr[i];
            }
        }

        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(requireContext(), searchView);
        for (String s : displayList) {
            popup.getMenu().add(s);
        }

        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString()
                    .replace("🔍 ", "")
                    .replace("🔍 ", "");
            searchView.setQuery(selected, true);
            return true;
        });
        popup.show();
    }
    private void showFabMenu() {
        isMenuOpen = true;
        fabChatAI.setVisibility(View.VISIBLE);
        fabMaps.setVisibility(View.VISIBLE);
        fabOverlay.setVisibility(View.VISIBLE);

        // Hiệu ứng xoay nút chính thành dấu X
        fabMain.animate().rotation(45f).setDuration(300).start();
    }

    private void closeFabMenu() {
        isMenuOpen = false;
        fabChatAI.setVisibility(View.GONE);
        fabMaps.setVisibility(View.GONE);
        fabOverlay.setVisibility(View.GONE);

        // Xoay nút chính về lại ban đầu
        fabMain.animate().rotation(0f).setDuration(300).start();
    }

    private void filterList(String text) {
        String query = text.toLowerCase().trim();

        // --- 1. Lọc danh sách Laptop Mới ---
        ArrayList<laptop_model> filteredMoi = new ArrayList<>();
        for (laptop_model item : arrLaptopMoiBackup) { // Lưu ý: Dùng danh sách Backup
            if (item.TenLapTop.toLowerCase().contains(query)) {
                filteredMoi.add(item);
            }
        }
        laptopMoiAdapter.updateList(filteredMoi);

        // Xử lý giao diện: Nếu không có kết quả thì ẩn tiêu đề và danh sách
        if (filteredMoi.isEmpty()) {
            binding.tvTieudeMoi.setVisibility(View.GONE); // ID tiêu đề Laptop Mới
            binding.rcvLaptopMoi.setVisibility(View.GONE);
        } else {
            binding.tvTieudeMoi.setVisibility(View.VISIBLE);
            binding.rcvLaptopMoi.setVisibility(View.VISIBLE);
        }

        // --- 2. Lọc danh sách Laptop Bán Chạy ---
        ArrayList<laptop_model> filteredBanChay = new ArrayList<>();
        for (laptop_model item : arrLaptopBanChayBackup) { // Lưu ý: Dùng danh sách Backup
            if (item.TenLapTop.toLowerCase().contains(query)) {
                filteredBanChay.add(item);
            }
        }
        laptopBanChayAdapter.updateList(filteredBanChay);

        // Xử lý giao diện: Nếu không có kết quả thì ẩn tiêu đề và danh sách
        if (filteredBanChay.isEmpty()) {
            binding.tvTieudeBanchay.setVisibility(View.GONE); // ID tiêu đề Bán chạy
            binding.rcvlaptopBanChay.setVisibility(View.GONE);
        } else {
            binding.tvTieudeBanchay.setVisibility(View.VISIBLE);
            binding.rcvlaptopBanChay.setVisibility(View.VISIBLE);
        }

        //  Nếu cả 2 đều trống thì hiện thông báo "Không tìm thấy"
        if (filteredMoi.isEmpty() && filteredBanChay.isEmpty()) {
            binding.tvKhongCoKetQua.setVisibility(View.VISIBLE);
        } else {
            binding.tvKhongCoKetQua.setVisibility(View.GONE);
        }
    }
    public void updateFavoriteUI(int maLaptop) {
        for (laptop_model l : arrLaptopMoi) {
            if (l.MaLapTop == maLaptop) {
                l.isFavorite = SERVER.isFavorite(maLaptop);
            }
        }
        for (laptop_model l : arrLaptopBanChay) {
            if (l.MaLapTop == maLaptop) {
                l.isFavorite = SERVER.isFavorite(maLaptop);
            }
        }

        laptopMoiAdapter.notifyDataSetChanged();
        laptopBanChayAdapter.notifyDataSetChanged();
    }


    private void setupRecyclerView() {

        // Hãng
        hangAdapter = new HANG_ADAPTER(requireContext(), arrHang);
        binding.rvChudeLaptop.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvChudeLaptop.setAdapter(hangAdapter);

        // Laptop mới
        laptopMoiAdapter = new LAPTOP_ADAPTER(arrLaptopMoi, getContext(),
                new LAPTOP_ADAPTER.OnFavoriteClickListener() {
                    @Override
                    public void onFavoriteAdded(laptop_model laptop) {
                        SERVER.addFavoriteToList(laptop.MaLapTop);
                        updateFavoriteUI(laptop.MaLapTop);
                    }

                    @Override
                    public void onFavoriteRemoved(laptop_model laptop) {
                        SERVER.removeFavoriteFromList(laptop.MaLapTop);
                        updateFavoriteUI(laptop.MaLapTop);
                    }
                });

        binding.rcvLaptopMoi.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rcvLaptopMoi.addItemDecoration(new SpacesItemDecoration(6));
        binding.rcvLaptopMoi.setAdapter(laptopMoiAdapter);

        // Laptop bán chạy
        laptopBanChayAdapter = new LAPTOP_ADAPTER(arrLaptopBanChay, getContext(),
                new LAPTOP_ADAPTER.OnFavoriteClickListener() {
                    @Override
                    public void onFavoriteAdded(laptop_model laptop) {
                        SERVER.addFavoriteToList(laptop.MaLapTop);
                        updateFavoriteUI(laptop.MaLapTop);
                    }

                    @Override
                    public void onFavoriteRemoved(laptop_model laptop) {
                        SERVER.removeFavoriteFromList(laptop.MaLapTop);
                        updateFavoriteUI(laptop.MaLapTop);
                    }
                });

        binding.rcvlaptopBanChay.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rcvlaptopBanChay.addItemDecoration(new SpacesItemDecoration(6));
        binding.rcvlaptopBanChay.setAdapter(laptopBanChayAdapter);

    }


    private void loadSlide() {
        JsonArrayRequest request = new JsonArrayRequest(
                SERVER.url_getQuangcao,
                response -> {
                    // Kiểm tra nếu Fragment không còn gắn với màn hình thì dừng lại
                    if (getContext() == null) {
                        return;
                    }
                    binding.viewflipper.removeAllViews();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            // Lúc này getContext() đảm bảo không null
                            ImageView img = new ImageView(getContext());

                            Picasso.get()
                                    .load(SERVER.server_slide + obj.getString("tenqc"))
                                    .placeholder(R.drawable.no_image)
                                    .into(img);
                            img.setScaleType(ImageView.ScaleType.FIT_XY);
                            binding.viewflipper.addView(img);
                        } catch (JSONException e) {
                            Log.e("SLIDE", e.getMessage());
                        }
                    }
                    binding.viewflipper.setFlipInterval(3000);
                    binding.viewflipper.startFlipping();
                },
                error -> Log.e("SLIDE_ERR", error.getMessage())
        );

        // Sử dụng requireContext() ở đây để đảm bảo request chỉ được gửi khi Fragment còn sống
        if (getContext() != null) {
            Volley.newRequestQueue(getContext()).add(request);
        }
    }

    private void loadHang() {
        JsonArrayRequest request = new JsonArrayRequest(
                SERVER.url_getHang,
                response -> {
                    arrHang.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            HANG_MODEL h = new HANG_MODEL();
                            h.masx = obj.getString("masx");
                            h.logo = obj.getString("logo");
                            arrHang.add(h);
                        } catch (JSONException e) {
                            Log.e("HANG", e.getMessage());
                        }
                    }
                    hangAdapter.notifyDataSetChanged();
                },
                error -> Log.e("HANG_ERR", error.getMessage())
        );
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void loadLaptopMoi() {
        loadLaptop("laptopmoi", arrLaptopMoi, laptopMoiAdapter);
    }

    private void loadLaptopBanChay() {
        loadLaptop("laptopbanchay", arrLaptopBanChay, laptopBanChayAdapter);
    }

    private void loadLaptop(String type, ArrayList<laptop_model> list, LAPTOP_ADAPTER adapter) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_getLaptop,
                response -> {
                    try {
                        list.clear();
                        // Clear backup cũ để tránh trùng lặp khi load lại
                        if (type.equals("laptopmoi")) arrLaptopMoiBackup.clear();
                        else arrLaptopBanChayBackup.clear();

                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            // l chính là laptop_model vừa lấy ra từ JSON
                            laptop_model item = parseLaptop(arr.getJSONObject(i));

                            list.add(item); // Thêm vào danh sách hiển thị

                            // Lưu vào danh sách Backup tương ứng để Search
                            if (type.equals("laptopmoi")) {
                                arrLaptopMoiBackup.add(item);
                            } else {
                                arrLaptopBanChayBackup.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("LAPTOP", e.getMessage());
                    }
                },
                error -> {
                    String errParams = (error.getMessage() != null) ? error.getMessage() : "Lỗi Server 500 (Không có nội dung)";
                    Log.e("LAPTOP_ERR", errParams);

                    // In thêm chi tiết nếu có
                    if (error.networkResponse != null) {
                        Log.e("LAPTOP_ERR_CODE", "Status Code: " + error.networkResponse.statusCode);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("laptopgi", type);
                map.put("masx", "0");
                return map;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private laptop_model parseLaptop(JSONObject row) throws JSONException {
        laptop_model l = new laptop_model();
        l.MaLapTop = row.getInt("MaLaptop");
        l.TenLapTop = row.getString("TenLaptop");
        l.Gia = row.getInt("Gia");
        l.HinhAnh = row.getString("HinhAnh");
        l.HinhChiTiet = row.getString("HinhChiTiet");
        l.SoLuong = row.getInt("SoLuong");
        if (row.has("MoTa")) {
            l.MoTa = row.getString("MoTa");
        } else {
            l.MoTa = "Đang cập nhật...";
        }

        if (row.has("HangSX")) {
            l.HangSX = row.getString("HangSX");
        } else {
            l.HangSX = "Khác";
        }

        if (row.has("CPU")) {
            l.CPU = row.getString("CPU");
        } else {
            l.CPU = "Đang cập nhật";
        }

        if (row.has("SoLuongBan")) {
            l.SoLuongBan = row.getInt("SoLuongBan");
        } else {
            l.SoLuongBan = 0;
        }
        l.isFavorite = SERVER.isFavorite(l.MaLapTop);
        return l;
    }
}
