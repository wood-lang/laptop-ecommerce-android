package com.example.duan_laptop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class Mydialog {
    Context context;

    public Mydialog(Context context) {
        this.context = context;
    }
    public void ShowMessage2button(String type,String title, String message, String buttonText1, String buttonText2){
        Integer color=R.color.white;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.custom_dialog_1, null);
        builder.setView(view);
        TextView tvtittle = view.findViewById(R.id.tvtittle);
        TextView tvmessege = view.findViewById(R.id.tvmessege);
        ImageView img_cancel = view.findViewById(R.id.img_cancel);
        Button btnok = view.findViewById(R.id.btnok);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow()!=null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            tvtittle.setText(title);
            tvmessege.setText(message);
            //thiet lap kieu thong bao : error,succes
            if (type=="error"){
                color= R.color.red;
            }else if (type == "success"){
                color=R.color.green;
            } else if (type.equals("warning")) {
                color = R.color.yellow;
            }
            tvtittle.setBackgroundColor(ContextCompat.getColor(context,color));
            btnok.setBackgroundColor(ContextCompat.getColor(context,color));
            btnok.setTextColor(ContextCompat.getColor(context,R.color.white));
            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }



    }

}
