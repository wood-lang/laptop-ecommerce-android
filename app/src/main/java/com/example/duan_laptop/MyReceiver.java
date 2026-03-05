package com.example.duan_laptop;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

public class MyReceiver extends BroadcastReceiver {
    Mydialog mydialog;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action =intent.getAction();
        //bluetooth is On of Off
        if ((action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
            if (state==BluetoothAdapter.STATE_ON){
                //bluetooth is on
                mydialog.ShowMessage2button("success","Title","Message","OK","Cancel");
            }else if (state==BluetoothAdapter.STATE_OFF){
                //bluetooth is off
                mydialog.ShowMessage2button("error","Title","Message","OK","Cancel");
            }
        } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            Boolean isAirplanemod = intent.getBooleanExtra("state",false);
            if (isAirplanemod)
                mydialog.ShowMessage2button("success","Airplane Mode is On","Message","OK","Cancel");
            else
                mydialog.ShowMessage2button("success","Airplane Mode is Off","Message","OK","Cancel");
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnectedOrConnecting())
                mydialog.ShowMessage2button("success","WIFI","Wifi is On","OK","Cancel");
            else
                mydialog.ShowMessage2button("error","WIFI","Wifi is Off","OK","Cancel");

        } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            Bundle extras = intent.getExtras();
            if (extras!=null){
                //lay so dt ra
                String sdt = extras.getString(TelephonyManager.EXTRA_STATE);
                if (sdt.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                String phonenumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                mydialog.ShowMessage2button("success","PHONE",phonenumber+"danh sach","OK","Cancel");
            }
        }
    } else if (action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            String format = intent.getStringExtra("format");
            Bundle extras = intent.getExtras();
            String tinnhan = "";
            if (extras!=null){
                Object[] smsExtra = (Object[]) extras.get("pdus");
                for (int i = 0;i<smsExtra.length;i++){
                    SmsMessage mes = SmsMessage.createFromPdu((byte[]) smsExtra[i],format);
                    String body = mes.getMessageBody().toString();
                    String address = mes.getOriginatingAddress().toString();
                    tinnhan+= "tin nhan tu: "+address+"\n"+body;
            }
        }
           mydialog.ShowMessage2button("success","SMS",tinnhan,"OK","Cancel");
    }}}
