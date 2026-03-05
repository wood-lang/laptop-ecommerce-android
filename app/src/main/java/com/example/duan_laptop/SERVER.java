package com.example.duan_laptop;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.FRAGMENT.Favorite_fragment;
import com.example.duan_laptop.FRAGMENT.HOME_FRAGMENT;
import com.example.duan_laptop.MODEL.USER;
import com.example.duan_laptop.MODEL.laptop_model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SERVER {
    // ================= 1. CONFIG URL =================
    public static String server_add = "https://microclimatically-pricklier-mariyah.ngrok-free.dev/web_laptop_duan/";
    public static String url_check_status = server_add + "check_status_donhang.php";
    public static String url_update_status = server_add + "update_status_donhang.php";

    public static String server_img = server_add + "images/";
    public static String server_slide = server_add + "slide/";
    public static String server_logo = server_add + "logo/";

    // URL AUTH & USER
    public static String url_register = server_add + "register.php";
    public static String url_gui_otp = server_add + "gui_otp.php";
    public static String url_xac_nhan_otp = server_add + "xac_nhan_otp.php";
    public static String url_getKhachhang = server_add + "getKhachhang.php";
    public  static String url_edit_delete_danhgia = SERVER.server_add + "edit_delete_danhgia.php";
    public  static String url_send_danhgia = SERVER.server_add + "send_danhgia.php";
    public  static String upload_edit_danhgia = SERVER.server_add + "upload_edit_danhgia.php";
    public  static String url_update_token = SERVER.server_add + "update_token.php";
    public static String url_get_thongbao = server_add + "get_thongbao.php";
    public static String url_delete_thongbao = server_add + "delete_thongbao.php";

    // URL GIỎ HÀNG
    public static String url_get_gio_hang = server_add + "get_gio_hang.php";
    public static String url_them_gio_hang = server_add + "them_gio_hang.php";
    public static String url_xoa_sp_gio_hang = server_add + "xoa_san_pham_gio_hang.php";
    public static String url_update_so_luong_gio_hang = server_add + "update_so_luong_gio_hang.php";

    // URL SẢN PHẨM & CỬA HÀNG
    public static String url_getHang = server_add + "getHang.php";
    public static String url_getLaptop = server_add + "getLaptop.php";
    public static String url_getQuangcao = server_add + "getQuangcao.php";

    // URL THANH TOÁN & ĐƠN HÀNG
    public static String url_thanhtoan = server_add + "thanhtoan_muangay.php";
    public static String url_get_lich_su_giaodich = server_add + "get_lich_su_giao_dich.php";
    public static String url_get_chitiet_donhang = server_add + "get_chitiet_donhang.php";
    public static String url_mua_lai = server_add + "mua_lai.php";
    public static final String url_xoa_lichsu = server_add + "xoa_lichsu_donhang.php";
    public static String url_get_laptop_detail = server_add + "get_laptop_detail.php";


    // URL YÊU THÍCH
    public static String url_addFavorite = server_add + "addFavorite.php";
    public static String url_removeFavorite = server_add + "removeFavorite.php";
    public static String url_getFavorite = server_add + "getFavorite.php";

    // URL CHAT & AI
    public static String url_chat_ai = server_add + "chat_ai.php";
    public static String url_get_history = server_add + "get_chat_history.php";
    public static String url_save_chat = server_add + "save_chat.php";
    public static String url_login_google = server_add + "login_google.php";


    // ================= 2. GLOBAL DATA =================
    public static USER user = new USER();
    public static ArrayList<Integer> favoriteList = new ArrayList<>();

    // ================= 3. XỬ LÝ BADGE (HIỆN SỐ ĐỎ) =================
    public interface BadgeListener {
        void onBadgeChange(int count);
    }

    private static BadgeListener favoriteBadgeListener;
    private static BadgeListener cartBadgeListener;

    public static void setFavoriteBadgeListener(BadgeListener listener) {
        favoriteBadgeListener = listener;
    }

    public static void setCartBadgeListener(BadgeListener listener) {
        cartBadgeListener = listener;
    }

    public static void updateFavoriteBadge() {
        if (favoriteBadgeListener != null) {
            favoriteBadgeListener.onBadgeChange(favoriteList.size());
        }
    }

    public static void updateCartBadge() {
        if (cartBadgeListener != null) {
            cartBadgeListener.onBadgeChange(Cart_manager.manggiohang.size());
        }
    }

    // ================= 4. XỬ LÝ LOGIC FAVORITE =================
    public static void addFavoriteToList(int MaLaptop) {
        if (!favoriteList.contains(MaLaptop)) {
            favoriteList.add(MaLaptop);
            updateFavoriteBadge();
        }
    }

    public static void removeFavoriteFromList(int MaLaptop) {
        if (favoriteList.contains(MaLaptop)) {
            favoriteList.remove(Integer.valueOf(MaLaptop));
            updateFavoriteBadge();
        }
    }

    public static boolean isFavorite(int MaLaptop) {
        return favoriteList.contains(MaLaptop);
    }

    // ================= 5. ĐỒNG BỘ GIỮA CÁC FRAGMENT =================
    private static Favorite_fragment favoriteFragment;
    private static HOME_FRAGMENT homeFragment;

    public static void setFavoriteFragment(Favorite_fragment fragment) {
        favoriteFragment = fragment;
    }

    public static void setHomeFragment(HOME_FRAGMENT fragment) {
        homeFragment = fragment;
    }

    public static void updateFavoriteFragment(laptop_model laptop, boolean added) {
        if (added) {
            addFavoriteToList(laptop.MaLapTop);
        } else {
            removeFavoriteFromList(laptop.MaLapTop);
            updateHomeFavorite(laptop);
        }

        if (favoriteFragment != null && favoriteFragment.isVisible()) {
            if (added) {
                favoriteFragment.addFavoriteLaptop(laptop);
            } else {
                favoriteFragment.removeFavoriteLaptop(laptop);
            }
        }
    }

    public static void updateHomeFavorite(laptop_model lap) {
        if (homeFragment != null && homeFragment.isVisible()) {
            homeFragment.updateFavoriteUI(lap.MaLapTop);
        }
    }

    // ================= 6. AUTO LOGIN & AUTH (MỚI) =================
    // Lưu thông tin khi đăng nhập thành công
    public static void saveLogin(Context c, String sdt, String pass,String DiaChi) {
        SharedPreferences sp = c.getSharedPreferences("login_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("sdt", sdt);
        editor.putString("pass", pass);
        editor.putString("DIACHI",DiaChi);
        editor.putBoolean("isLogged", true);
        editor.apply();
    }

    // Kiểm tra đã đăng nhập chưa
    public static boolean isLogged(Context c) {
        return c.getSharedPreferences("login_pref", Context.MODE_PRIVATE).getBoolean("isLogged", false);
    }

    // Lấy SĐT đã lưu cho Auto Login
    public static String getSavedSDT(Context c) {
        return c.getSharedPreferences("login_pref", Context.MODE_PRIVATE).getString("sdt", "");
    }

    // Lấy Pass đã lưu cho Auto Login
    public static String getSavedPass(Context c) {
        return c.getSharedPreferences("login_pref", Context.MODE_PRIVATE).getString("pass", "");
    }

    // Đăng xuất sạch sẽ
    public static void logout(Context c) {
        // 1. Xóa SharedPreferences
        c.getSharedPreferences("login_pref", Context.MODE_PRIVATE).edit().clear().apply();

        // 2. Reset dữ liệu RAM
        user = new USER();
        favoriteList.clear();
        Cart_manager.clearCart();

        // 3. Reset UI
        updateFavoriteBadge();
        updateCartBadge();
    }
    public static String getSafeSDT() {
        if (user != null && user.SDT != null) {
            return user.SDT;
        }
        return "";
    }
    public static void loadFavoriteFromServer(Context context) {
        if (user == null || getSavedSDT(context).isEmpty()) return;

        StringRequest request = new StringRequest(Request.Method.POST, url_getFavorite,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        favoriteList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            favoriteList.add(obj.getInt("MaLaptop"));
                        }
                        // Thông báo để MainActivity vẽ lại Badge
                        updateFavoriteBadge();
                    } catch (Exception e) { e.printStackTrace(); }
                }, error -> Log.e("SERVER", "Lỗi load favorite badge")) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("SDT", getSavedSDT(context));
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }
    // 1. Tạo Interface
    public interface FavoriteChangeListener {
        void onFavoriteChanged();
    }

    // 2. Biến chứa người lắng nghe
    public static FavoriteChangeListener favoriteListener;

    // 3. Hàm để MainActivity đăng ký lắng nghe
    public static void setFavoriteListener(FavoriteChangeListener listener) {
        favoriteListener = listener;
    }
    // 4. Hàm để Adapter gọi khi có thay đổi (Bấm tim / Xóa tim)
    public static void notifyFavoriteChanged() {
        if (favoriteListener != null) {
            favoriteListener.onFavoriteChanged();
        }
    }
}