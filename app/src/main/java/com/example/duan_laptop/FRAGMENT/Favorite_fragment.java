package com.example.duan_laptop.FRAGMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.LAPTOP_ADAPTER;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.SERVER;
import com.example.duan_laptop.databinding.ActivityFavorite2Binding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Favorite_fragment extends Fragment {

    private ActivityFavorite2Binding binding;
    public ArrayList<laptop_model> arrFavorite = new ArrayList<>();
    private LAPTOP_ADAPTER adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        binding = ActivityFavorite2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();

        // Đăng ký fragment với SERVER để các màn hình khác có thể gửi dữ liệu về đây
        SERVER.setFavoriteFragment(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Mỗi khi quay lại tab này, tải lại từ server để đồng bộ mới nhất
        loadFavoriteFromServer();
    }

    private void setupRecyclerView(){
        adapter = new LAPTOP_ADAPTER(arrFavorite, getContext(), new LAPTOP_ADAPTER.OnFavoriteClickListener() {
            @Override
            public void onFavoriteAdded(laptop_model laptop) {
                // Logic thêm đã có trong SERVER.updateFavoriteFragment
            }

            @Override
            public void onFavoriteRemoved(laptop_model laptop) {
                // Khi nhấn bỏ thích ngay tại trang Favorite
                removeFavoriteLaptop(laptop);
            }
        });
        binding.rcvFavorite.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rcvFavorite.setAdapter(adapter);
    }

    public void addFavoriteLaptop(laptop_model lap){
        // Kiểm tra xem đã có trong list chưa dựa trên mã laptop
        boolean isExist = false;
        for (laptop_model item : arrFavorite) {
            if (item.MaLapTop == lap.MaLapTop) {
                isExist = true;
                break;
            }
        }
        if(!isExist){
            lap.isFavorite = true;
            arrFavorite.add(0, lap); // Thêm lên đầu danh sách cho đẹp
            adapter.notifyDataSetChanged();
            updateEmptyView();
        }
    }

    public void removeFavoriteLaptop(laptop_model lap){
        laptop_model toRemove = null;
        for (laptop_model item : arrFavorite) {
            if (item.MaLapTop == lap.MaLapTop) {
                toRemove = item;
                break;
            }
        }

        if(toRemove != null){
            arrFavorite.remove(toRemove);
            // Cập nhật trạng thái trong SERVER.favoriteList (để Badge ở MainActivity giảm số)
            SERVER.removeFavoriteFromList(lap.MaLapTop);
            //Để cái số đỏ ở menu giảm xuống ngay lập tức
            SERVER.notifyFavoriteChanged();
            adapter.notifyDataSetChanged();
            updateEmptyView();
        }
    }

    private void updateEmptyView() {
        if (arrFavorite.isEmpty()) {
            binding.rcvFavorite.setVisibility(View.GONE);
            // Nếu bồ có TextView báo trống thì hiện ở đây
        } else {
            binding.rcvFavorite.setVisibility(View.VISIBLE);
        }
    }

    private void loadFavoriteFromServer(){
        if(SERVER.user == null) return;
        String idUser = (SERVER.user.SDT != null && !SERVER.user.SDT.isEmpty())
                ? SERVER.user.SDT : SERVER.user.Email;

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_getFavorite,
                response -> {
                    try{
                        arrFavorite.clear();
                        JSONArray array = new JSONArray(response);
                        for(int i=0; i<array.length(); i++){
                            JSONObject obj = array.getJSONObject(i);
                            laptop_model lap = new laptop_model();
                            lap.MaLapTop = obj.getInt("MaLaptop");
                            lap.TenLapTop = obj.getString("TenLaptop");
                            lap.Gia = obj.getInt("Gia");
                            lap.HinhAnh = obj.getString("HinhAnh");
                            lap.HinhChiTiet = obj.optString("HinhChiTiet", "");
                            lap.MoTa = obj.optString("MoTa", "Đang cập nhật");
                            lap.CPU = obj.optString("CPU", "");
                            lap.HangSX = obj.getString("HangSX");
                            lap.SoLuongBan = obj.optInt("SoLuongBan", 0);
                            lap.isFavorite = true;

                            // Đồng bộ với Badge của MainActivity
                            SERVER.addFavoriteToList(lap.MaLapTop);

                            arrFavorite.add(lap);
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                        SERVER.notifyFavoriteChanged();
                    }catch(JSONException e){
                        Log.e("FAV_JSON", e.getMessage());
                    }
                },
                error -> Log.e("FAV_ERROR", "Lỗi tải dữ liệu")
        ){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> map = new HashMap<>();
                map.put("SDT", idUser);
                return map;
            }
        };
        // Tắt cache để luôn tải mới nhất
        request.setShouldCache(false);
        if (getContext() != null) Volley.newRequestQueue(getContext()).add(request);
    }
}