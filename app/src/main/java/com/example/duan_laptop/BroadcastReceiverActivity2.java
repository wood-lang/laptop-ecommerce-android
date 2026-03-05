package com.example.duan_laptop;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.TelephonyManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BroadcastReceiverActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_broadcast_receiver2);
        XinQuyen();
        MyReceiver myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(myReceiver,intentFilter);

    }

    private void XinQuyen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
            !=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                !=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},2);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                !=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG},3);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                !=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS},3);
        }
    }
}