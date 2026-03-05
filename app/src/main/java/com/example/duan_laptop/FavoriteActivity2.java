package com.example.duan_laptop;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.LAPTOP_ADAPTER;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.databinding.ActivityFavorite2Binding;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoriteActivity2 extends AppCompatActivity {

    private ActivityFavorite2Binding binding;
    private ArrayList<laptop_model> arrFavorite = new ArrayList<>();
    private LAPTOP_ADAPTER adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavorite2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        loadFavoriteFromServer();
    }

    private void setupRecyclerView(){
        adapter = new LAPTOP_ADAPTER(arrFavorite, this, new LAPTOP_ADAPTER.OnFavoriteClickListener() {
            @Override
            public void onFavoriteAdded(laptop_model laptop) {}
            @Override
            public void onFavoriteRemoved(laptop_model laptop) {
                arrFavorite.remove(laptop);
                adapter.notifyDataSetChanged();
            }
        });

        binding.rcvFavorite.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rcvFavorite.setAdapter(adapter);
    }

    private void loadFavoriteFromServer(){
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_getFavorite,
                response -> {
                    arrFavorite.clear();
                    try {
                        JSONArray array = new JSONArray(response);
                        for(int i=0;i<array.length();i++){
                            JSONObject obj = array.getJSONObject(i);
                            laptop_model laptop = new laptop_model();
                            laptop.MaLapTop = Integer.parseInt(String.valueOf(obj.getInt("MaLaptop")));
                            laptop.TenLapTop = obj.getString("TenLaptop");
                            laptop.Gia = obj.getInt("Gia");
                            laptop.HinhAnh = obj.getString("HinhAnh");
                            laptop.HangSX = obj.getString("HangSX");
                            laptop.isFavorite = true;
                            arrFavorite.add(laptop);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e){
                        Log.e("FAVORITE_JSON_ERROR", e.getMessage());
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FAVORITE_ERROR", error.getMessage());
                    Toast.makeText(this, "Lỗi tải dữ liệu favorite", Toast.LENGTH_SHORT).show();
                }
        ){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> map = new HashMap<>();
                if(SERVER.user != null && SERVER.user.SDT != null){
                    map.put("SDT", SERVER.user.SDT);
                } else {
                    map.put("SDT", "");
                }
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
