package com.example.duan_laptop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.FRAGMENT.Account_fragment;
import com.example.duan_laptop.FRAGMENT.Cart_fragment;
import com.example.duan_laptop.FRAGMENT.Favorite_fragment;
import com.example.duan_laptop.FRAGMENT.HOME_FRAGMENT;
import com.example.duan_laptop.databinding.ActivityMainBinding;
import com.google.android.material.badge.BadgeDrawable;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Cart_manager.CartListener {

    ActivityMainBinding binding;
    private TextView tvBadgeCount;
    MainViewPagerAdapter adapter;
    // Bộ thu sóng nghe ngóng xem có thông báo FCM nào rớt xuống không
    private android.content.BroadcastReceiver thongBaoReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateThongBaoBadge(); // Nghe tiếng ting ting là tự động load lại số lượng!
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 102);
            }
        }

        // 2. TẠO KÊNH THÔNG BÁO FCM NGAY KHI MỞ APP
        createNotificationChannelFCM();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // 1. Tạo Notification Channel trước
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "upload_channel_id",
                    "Upload Service",
                    android.app.NotificationManager.IMPORTANCE_LOW
            );
            android.app.NotificationManager manager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
// 2. Khởi tạo UploadServiceConfig
        new Thread(() -> {
            net.gotev.uploadservice.UploadServiceConfig.initialize(
                    getApplication(),
                    "upload_channel_id",
                    false
            );
        }).start();
        if (SERVER.user != null) {
            Cart_manager.loadCartFromServer(this);
//            SERVER.loadFavoriteFromServer(this);
        }

        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.drawer_open, R.string.drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        loadUserDataToHeader();
        setupBottomNav();

        // 2. Mặc định mở Home hoặc Cart theo Intent
//        if (savedInstanceState == null) {
//            handleIntent(getIntent());
//            if (getSupportFragmentManager().findFragmentById(R.id.frameLayout) == null) {
//                loadFragment(new HOME_FRAGMENT());
//            }
//        }
        SERVER.setFavoriteListener(new SERVER.FavoriteChangeListener() {
            @Override
            public void onFavoriteChanged() {
                // Chạy hàm cập nhật badge
                updateFavoriteBadge();
            }
        });

        // Gọi lần đầu khi mở app
        updateFavoriteBadge();

//        // Lắng nghe Favorite Badge
//        SERVER.setFavoriteBadgeListener(count -> {
//            if (binding.bottomNavView != null) {
//                BadgeDrawable badge = binding.bottomNavView.getOrCreateBadge(R.id.mnuFavorite);
//                badge.setVisible(count > 0);
//                if (count > 0) badge.setNumber(count);
//            }
//        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_VIDEO}, 101);
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            }
        }
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.getBooleanExtra("open_cart", false)) {
                // Nếu có lệnh mở giỏ hàng thì mở Cart luôn
                binding.viewPager2.setCurrentItem(1, false);
            } else {
                // Nếu không có gì thì mới mở Home mặc định
                binding.viewPager2.setCurrentItem(0, false);
            }
        }
        updateFavoriteBadge();
        layVaGuiTokenFCM();
    }
    // Hàm tạo kênh thông báo riêng cho Đơn hàng
    private void createNotificationChannelFCM() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "Kenh_Thong_Bao_Laptop", // ID phải khớp với file PHP và MyFirebaseService
                    "Thông báo Đơn Hàng",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh nhận thông báo khi có đơn hàng mới");
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private void layVaGuiTokenFCM() {
        // Chỉ gửi nếu đã đăng nhập
        if (SERVER.user == null) return;

        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_TOKEN", "Lấy Token thất bại", task.getException());
                        return;
                    }
                    // Lấy Token thành công
                    String token = task.getResult();
                    Log.d("FCM_TOKEN", "Token hiện tại: " + token);

                    // Bắn lên Server PHP
                    guiTokenLenPHP(token);
                });
    }

    private void guiTokenLenPHP(String token) {
        Log.d("FCM_TOKEN", "Đang gửi đi - SDT: " + (SERVER.user.SDT != null ? SERVER.user.SDT : "rỗng")
                + " | Email: " + (SERVER.user.Email != null ? SERVER.user.Email : "rỗng"));
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_update_token,
                response -> Log.d("FCM_TOKEN", "Kết quả lưu Token lên PHP: " + response),
                error -> Log.e("FCM_TOKEN", "Lỗi mạng khi lưu Token")
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", token);
                map.put("sdt", SERVER.user.SDT != null ? SERVER.user.SDT : "");
                map.put("email", SERVER.user.Email != null ? SERVER.user.Email : "");
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        MenuItem itemCart = menu.findItem(R.id.menu_cart);
        View actionView = itemCart.getActionView();
        if (actionView != null) {
            // Phải ánh xạ lại tvBadgeCount mỗi khi nạp Menu
            tvBadgeCount = actionView.findViewById(R.id.tv_cart_count_badge);
            actionView.setOnClickListener(v -> {
                binding.viewPager2.setCurrentItem(1, true);
            });
        }

        // Gọi hàm update ngay lập tức sau khi ánh xạ xong
        updateBadge();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_cart) {
            binding.viewPager2.setCurrentItem(1, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // ------------------------------------------------------------------

    private void updateBadge() {
        int total = Cart_manager.getTotalQuantity();

        // Cập nhật Toolbar
        if (tvBadgeCount != null) {
            if (total > 0) {
                tvBadgeCount.setVisibility(View.VISIBLE);
                tvBadgeCount.setText(String.valueOf(total));
            } else {
                tvBadgeCount.setVisibility(View.GONE);
            }
        }

        // Cập nhật BottomNav
        if (binding.bottomNavView != null) {
            BadgeDrawable bottomBadge = binding.bottomNavView.getOrCreateBadge(R.id.mnCart);
            if (total > 0) {
                bottomBadge.setVisible(true);
                bottomBadge.setNumber(total);
                bottomBadge.setBackgroundColor(getColor(R.color.red));
                bottomBadge.setBadgeTextColor(getColor(R.color.white));
            } else {
                bottomBadge.setVisible(false);
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        Cart_manager.addListener(this); // Lắng nghe để cập nhật Badge
        updateBadge();
//        if (SERVER.user != null) {
//            SERVER.loadFavoriteFromServer(this);
//        }
        updateFavoriteBadge();
        updateThongBaoBadge(); // Mỗi lần mở App là đếm số thông báo

        // Bật ăng-ten thu sóng real-time
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            //context dùng để tránh những app khác bắt được sóng
            registerReceiver(thongBaoReceiver, new IntentFilter("CO_THONG_BAO_MOI"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(thongBaoReceiver, new IntentFilter("CO_THONG_BAO_MOI"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Cart_manager.removeListener(this);
        unregisterReceiver(thongBaoReceiver);//tắt thu sóng đỡ tốn pin
    }

    @Override
    public void onCartChanged() {
        runOnUiThread(this::updateBadge);
    }

    private void setupBottomNav() {
        // 1. Khởi tạo Adapter và gắn vào ViewPager2
        adapter = new MainViewPagerAdapter(this);
        binding.viewPager2.setAdapter(adapter);

        // 2. Tắt hiệu ứng vuốt quá đà (cái bóng mờ ở mép màn hình) cho đẹp
        binding.viewPager2.setOffscreenPageLimit(4);

        // 3. Khi bấm BottomNav -> Chuyển trang ViewPager2
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.mnHome) binding.viewPager2.setCurrentItem(0, true);
            else if (id == R.id.mnCart) binding.viewPager2.setCurrentItem(1, true);
            else if (id == R.id.mnuFavorite) binding.viewPager2.setCurrentItem(2, true);
            else if (id == R.id.mnuThongBao) binding.viewPager2.setCurrentItem(3, true);
            else if (id == R.id.mnuAccount) binding.viewPager2.setCurrentItem(4, true);
            return true;
        });

        // 4. Khi vuốt ViewPager2 -> Đổi sáng icon trên BottomNav
        binding.viewPager2.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0: binding.bottomNavView.getMenu().findItem(R.id.mnHome).setChecked(true); break;
                    case 1: binding.bottomNavView.getMenu().findItem(R.id.mnCart).setChecked(true); break;
                    case 2: binding.bottomNavView.getMenu().findItem(R.id.mnuFavorite).setChecked(true); break;
                    case 3: binding.bottomNavView.getMenu().findItem(R.id.mnuThongBao).setChecked(true); break;
                    case 4: binding.bottomNavView.getMenu().findItem(R.id.mnuAccount).setChecked(true); break;
                }
            }
        });
    }

//    private void loadFragment(Fragment fragment) {
//        Log.d("DEBUG_FRAGMENT", "Loading: " + fragment.getClass().getSimpleName());
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.frameLayout, fragment)
//                .commit();
//        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            binding.drawerLayout.closeDrawer(GravityCompat.START);
//        }
//    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("open_cart", false)) {
            binding.viewPager2.setCurrentItem(1, false);
            binding.bottomNavView.setSelectedItemId(R.id.mnCart);
        }
    }

    private void loadUserDataToHeader() {
        if (SERVER.user != null) {
            View header = binding.navViewDrawer.getHeaderView(0);
            TextView tvEmail = header.findViewById(R.id.textView2);
            if (tvEmail != null) tvEmail.setText(SERVER.user.getEmail());
            ImageView imgAvatarHeader = header.findViewById(R.id.imageView);
            if (imgAvatarHeader != null && SERVER.user.Avatar != null && !SERVER.user.Avatar.isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(SERVER.user.Avatar)
                        .circleCrop()
                        .into(imgAvatarHeader);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Cart_manager.removeListener(this);
    }
    // Trong MainActivity.java

    public void updateFavoriteBadge() {
        if (SERVER.user == null) return;

        // Lấy ID User
        String idUser = (SERVER.user.SDT != null && !SERVER.user.SDT.isEmpty())
                ? SERVER.user.SDT : SERVER.user.Email;

        // Log URL để debug nếu cần
        String url = SERVER.url_getFavorite + "?user_id=" + idUser;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        int count = jsonArray.length();

                        // Sử dụng binding thay vì findViewById
                        if (binding.bottomNavView != null) {
                            // Xóa badge cũ để tránh lỗi lưu đè
                            binding.bottomNavView.removeBadge(R.id.mnuFavorite);

                            if (count > 0) {
                                // Tạo badge mới
                                BadgeDrawable badge = binding.bottomNavView.getOrCreateBadge(R.id.mnuFavorite);
                                badge.setVisible(true);
                                badge.setNumber(count);
                                badge.setBackgroundColor(getColor(R.color.red));
                                badge.setBadgeTextColor(getColor(R.color.white));
                            }
                            // Ép vẽ lại menu
                            binding.bottomNavView.invalidate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {}
        );
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }
    // Tạo một cái Badge toàn cục cho Toolbar
    private com.google.android.material.badge.BadgeDrawable badgeThongBaoToolbar;

    //Hàm gọi API đếm thông báo và hiện cục màu đỏ
    @androidx.annotation.OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
    public void updateThongBaoBadge() {
        if (SERVER.user == null) return;
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.POST, SERVER.url_get_thongbao,
                response -> {
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                        int count = jsonArray.length(); // Đếm tổng số thông báo
                        // CẬP NHẬT SỐ ĐẾM Ở BOTTOM NAVIGATION
                        if (binding.bottomNavView != null) {
                            com.google.android.material.badge.BadgeDrawable bottomBadge = binding.bottomNavView.getOrCreateBadge(R.id.mnuThongBao);
                            bottomBadge.setVisible(count > 0);
                            if (count > 0) {
                                bottomBadge.setNumber(count);
                                bottomBadge.setBackgroundColor(getColor(R.color.red));
                                bottomBadge.setBadgeTextColor(getColor(R.color.white));
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {}
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("sdt", SERVER.user.SDT);
                return map;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }
}


//        mydialog = new Mydialog(MainActivity.this);
//        //xin quyền cho firebaseMessenger
//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
//         if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
//             ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
//         }
//        }
//        REQUEST_PERMISION();
//        //show ra cai hop thoai thong bao lon
//        binding.dialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showdialogcustom();
//                showdialogcustom2();
//                mydialog.ShowMessage2button("error","Title","Message","OK","Cancel");
//            }
//
//            private void showdialog() {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("Thong bao");
//                builder.setIcon(R.drawable.ic_baseline_notifications_24);
//                builder.setMessage("day la thong bao");
//                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
////                        dialogInterface.cancel();
//                        finish();
//
//                    }
//                });
//                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        finish();
//                    }
//                });
//                builder.create().show();
//            }
//        });
//
//    private void REQUEST_PERMISION() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 111);
//        }
//    }
//    private void showdialogcustom2() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        View view = getLayoutInflater().inflate(R.layout.custom_dialog_2, (ConstraintLayout) findViewById(R.id.contraintlayouroot_2));
//        TextView tvtittle2 = view.findViewById(R.id.tvtittle2);
//        TextView tvmessege2 = view.findViewById(R.id.tvmessege2);
//        ImageView img_cancel2 = view.findViewById(R.id.img_cancel2);
//        Button btnok2 = view.findViewById(R.id.btnok_2);
//
//        tvtittle2.setText("Thong bao");
//        tvmessege2.setText("day la thong bao");
//        dialog.setView(view);
//        AlertDialog alertDialog = dialog.create();
//        if (alertDialog.getWindow()!=null){
//            //backgroud cua contrainroot thiet lap la trong suot
//            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }
//        tvtittle2.setText("Title of dialog custom 2");
//        tvmessege2.setText("content of dialog custom 2");
//        btnok2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.cancel();
//
//            }
//        });
//        dialog.show();
//
//    }
//
//
//    private void showdialogcustom() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        View view = getLayoutInflater().inflate(R.layout.custom_dialog_1,(ConstraintLayout)findViewById(R.id.dialog_layout_root));
//        TextView tvtittle = view.findViewById(R.id.tvtittle);
//        TextView tvmessege = view.findViewById(R.id.tvmessege);
//        ImageView img_cancel = view.findViewById(R.id.img_cancel);
//        Button btnok = view.findViewById(R.id.btnok);
//
//        tvtittle.setText("Thong bao");
//        tvmessege.setText("day la thong bao");
//
//        builder.setView(view);
//        Dialog dialog = builder.create();
//
//        img_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.cancel();
//            }
//        });
//        btnok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.cancel();
//            }
//        });
//        builder.show();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            Toast.makeText(this, "khong duoc", Toast.LENGTH_SHORT).show();
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
