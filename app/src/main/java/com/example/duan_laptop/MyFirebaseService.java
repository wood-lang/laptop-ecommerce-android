package com.example.duan_laptop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseService extends FirebaseMessagingService {

    // Hàm này tự động chạy để lấy Token
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "Mã Token của máy này là: " + token);
    }

    // Hàm này bắt thông báo khi App ĐANG MỞ
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "";
        String body = "";
        // Hoặc lấy từ Data (khi gửi từ PHP của bồ)
        if (message.getData().size() > 0) {
             title = message.getData().get("title");
             body = message.getData().get("body");

            // LOG ĐỂ KIỂM TRA (Bồ nhìn trong Logcat sẽ thấy nó hiện ra hay không)
            Log.d("FCM_DATA", "Nhận được: " + title + " - " + body);
        }

        if (title != null && !title.isEmpty()) {
            hienThiThongBao(title, body);
        }
    }

    // --- HÀM TỰ VẼ THÔNG BÁO ---
    private void hienThiThongBao(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "Kenh_Thong_Bao_Laptopv4";

        // 1. Từ Android 8.0 (Oreo) trở lên bắt buộc phải tạo Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Kenh_Thong_Bao_Laptopv4",
                    NotificationManager.IMPORTANCE_HIGH // Mức độ cao nhất để ép thông báo rớt xuống màn hình
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 2. Cài đặt hành động: Khi khách bấm vào thông báo sẽ mở MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Xây dựng hình dáng cái bảng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_noti)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(getResources().getColor(android.R.color.holo_blue_dark))
                .setAutoCancel(true) // Khách vuốt hoặc bấm vào là tự tắt
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Ép ưu tiên cao trên Android cũ
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        //Tạo ID ngẫu nhiên để các thông báo không bị đè lên nhau
        int notificationId = (int) System.currentTimeMillis();

        // Kích nổ tiếng "Ting"
        notificationManager.notify(notificationId, builder.build());
    }
}