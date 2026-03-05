package com.example.duan_laptop;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.MODEL.Cart_item;
import com.example.duan_laptop.MODEL.laptop_model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cart_manager {
    // Đảm bảo dòng này có chữ "public static" để các Activity khác truy cập được
    public static ArrayList<Cart_item> manggiohang = new ArrayList<>();

    public interface CartListener {
        void onCartChanged();
    }

    private static List<CartListener> listeners = new ArrayList<>();

    public static void addListener(CartListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(CartListener listener) {
        listeners.remove(listener);
    }

    public static void notifyListeners() {
        for (CartListener listener : listeners) {
            listener.onCartChanged();
        }
    }

    public static void addToCart(laptop_model laptop, int soLuong) {
        boolean exists = false;
        for (Cart_item item : manggiohang) {
            if (item.laptop.MaLapTop == laptop.MaLapTop) {
                item.SoLuong += soLuong;
                // Đảm bảo mang theo thông tin tồn kho mới nhất
                item.laptop.SoLuong = laptop.SoLuong;
                exists = true;
                break;
            }
        }
        if (!exists) {
            Cart_item newItem = new Cart_item(laptop, soLuong);
            manggiohang.add(newItem);
        }
        for (CartListener listener : listeners) {
            if (listener != null) {
                listener.onCartChanged(); // Kích hoạt hàm updateBadge() ở các nơi
            }
        }
        notifyListeners();
    }

    public static void removeFromCart(int position) {
        if (position >= 0 && position < manggiohang.size()) {
            manggiohang.remove(position);
            notifyListeners();
        }
    }

    public static void updateQuantity(int position, int newQuantity) {
        if (position >= 0 && position < manggiohang.size()) {
            manggiohang.get(position).SoLuong = newQuantity;
            notifyListeners();
        }
    }

    public static long getTotalPrice() {
        long total = 0;
        for (Cart_item item : manggiohang) {
            total += (long) item.laptop.Gia * item.SoLuong;
        }
        return total;
    }

    public static int getTotalQuantity() {
        int total = 0;
        if (manggiohang != null) {
            for (Cart_item item : manggiohang) {
                total += item.SoLuong;
            }
        }
        return total;
    }

    public static void clearCart() {
        manggiohang.clear();
        notifyListeners();
    }
    public static void loadCartFromServer(Context context) {
        if (SERVER.user == null) return;

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_get_gio_hang,
                response -> {
                    try {
                        manggiohang.clear();
                        JSONArray jsonArray = new JSONArray(response.trim());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            Log.d("CHECK_DATA", "JSON nhận về: " + obj.toString());
                            laptop_model laptop = new laptop_model();
                            laptop.MaLapTop = obj.getInt("MaLaptop");
                            laptop.TenLapTop = obj.getString("TenLaptop");
                            laptop.Gia = obj.getInt("Gia");
                            laptop.HinhAnh = obj.getString("HinhAnh");
                            laptop.SoLuong = obj.getInt("TonKho"); // Lấy cái TonKho từ PHP gán vào số lượng của laptop
                            Log.d("DEBUG_ASSIGN", "SP: " + laptop.TenLapTop + " | TonKho: " + laptop.SoLuong);
                            int soLuong = obj.getInt("SoLuong");
                            manggiohang.add(new Cart_item(laptop, soLuong));
                        }
                        // Quan trọng: Thông báo để Badge cập nhật
                        notifyListeners();
                    } catch (Exception e) { e.printStackTrace(); }
                }, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("sdt", SERVER.user.SDT);
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }
}