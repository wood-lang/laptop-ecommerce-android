package com.example.duan_laptop.HELPER;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.duan_laptop.R;

public class CustomDialogHelper {

    private Context context;
    private Dialog dialog;
    private LottieAnimationView lottieView;
    private TextView tvTitle, tvMessage;
    private MaterialButton btnOk, btnCancel;

    // Các thành phần trang trí mới
    private MaterialCardView cardHalo1, cardHalo2;
    private View viewTopStrip, viewTitleUnderline;

    public CustomDialogHelper(Context context) {
        this.context = context;
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom_layout);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        lottieView = dialog.findViewById(R.id.lottieAnimation);
        tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        btnOk = dialog.findViewById(R.id.btnDialogOk);
        btnCancel = dialog.findViewById(R.id.btnDialogCancel);

        // Ánh xạ đồ trang trí
        cardHalo1 = dialog.findViewById(R.id.cardHalo1);
        cardHalo2 = dialog.findViewById(R.id.cardHalo2);
        viewTopStrip = dialog.findViewById(R.id.viewTopStrip);
        viewTitleUnderline = dialog.findViewById(R.id.viewTitleUnderline);
    }

    public void showSuccess(String title, String message, View.OnClickListener okClickListener) {
        setupTheme(title, message, R.raw.anim_success, "#4CAF50"); // XANH LÁ
        btnCancel.setVisibility(View.GONE);
        btnOk.setText("TUYỆT VỜI");
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (okClickListener != null) okClickListener.onClick(v);
        });
        dialog.show();
    }

    public void showError(String title, String message) {
        setupTheme(title, message, R.raw.anim_error, "#F44336"); // ĐỎ CẢNH BÁO
        btnCancel.setVisibility(View.GONE);
        btnOk.setText("ĐÃ HIỂU");
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public interface DialogActionListener {
        void onPositiveClick();
        default void onNegativeClick(){}
    }

    public void showWarning(String title, String message, DialogActionListener listener) {
        setupTheme(title, message, R.raw.anim_warning, "#FF9800"); // CAM TƯƠI
        btnCancel.setVisibility(View.VISIBLE);
        btnOk.setText("ĐỒNG Ý");

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onNegativeClick(); // Báo cho Fragment biết để trượt item về
        });
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onPositiveClick();
        });
        dialog.show();
    }

    public void showOrderSuccess(String title, String message, View.OnClickListener okClickListener) {
        setupTheme(title, message, R.raw.anim_order_success, "#4CAF50");
        btnCancel.setVisibility(View.GONE);
        btnOk.setText("VỀ TRANG CHỦ");
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (okClickListener != null) okClickListener.onClick(v);
        });
        dialog.show();
    }

    private void setupTheme(String title, String message, int lottieResId, String colorHex) {
        tvTitle.setText(title);
        tvMessage.setText(message);
        lottieView.setAnimation(lottieResId);
        lottieView.playAnimation();

        int mainColor = Color.parseColor(colorHex);

        // 1. Đổi màu Nút, Thanh đỉnh, và Gạch chân
        btnOk.setBackgroundTintList(ColorStateList.valueOf(mainColor));
        viewTopStrip.setBackgroundColor(mainColor);
        viewTitleUnderline.setBackgroundColor(mainColor);

        // 2. Thuật toán tạo 2 vòng Hào quang (Halo) trong suốt
        // Alpha: 25 (~10% độ mờ) cho vòng ngoài cùng
        int halo1Color = Color.argb(25, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));
        // Alpha: 50 (~20% độ mờ) cho vòng bên trong
        int halo2Color = Color.argb(50, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));

        cardHalo1.setCardBackgroundColor(halo1Color);
        cardHalo2.setCardBackgroundColor(halo2Color);
    }
}