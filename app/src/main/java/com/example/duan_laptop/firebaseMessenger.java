package com.example.duan_laptop;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class firebaseMessenger extends FirebaseMessagingService {
    public static  String TAG = "FCM_CHANNEL";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        if (message.getNotification()!=null){
            //hien thi thong bao
            ShowmMessage(message.getNotification().getTitle(),message.getNotification().getBody());
        }
    }

    private void ShowmMessage(String title, String body) {
        //nhan vao thong bao hien man hinh notifi
        Intent intent = new Intent(this, notifiActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,111,intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //hop thoai thong bao phia tren
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"FCM_CHANNEL")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        //kiem tra phien ban
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(TAG, "duan_laptop", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(TAG);
            //DEFAULT_ALL sẽ áp dụng các thiết lập mặc định về âm thanh, rung và đèn nháy của điện thoại.
            builder.setDefaults(Notification.DEFAULT_ALL);
            //giúp thông báo có khả năng "nhảy ra" (heads-up) thành một biểu ngữ ở cạnh trên màn hình thay vì chỉ hiện icon im lặng trên thanh trạng thái
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            //Nếu người dùng bật chế độ "Không làm phiền" (Do Not Disturb) nhưng cho phép nhận tin nhắn, thì thông báo này vẫn sẽ được hiển thị
            builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);

    }
        notificationManager.notify(111,builder.build());
}}
