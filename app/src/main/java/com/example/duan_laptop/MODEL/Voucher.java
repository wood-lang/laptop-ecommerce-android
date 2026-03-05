package com.example.duan_laptop.MODEL;

public class Voucher {
    public int id;
    public String MaVoucher;
    public String TenVoucher;
    public int GiamGia;

    public Voucher(int id, String maVoucher, String tenVoucher, int giamGia) {
        this.id = id;
        MaVoucher = maVoucher;
        TenVoucher = tenVoucher;
        GiamGia = giamGia;
    }

    // Để hiển thị lên Spinner hoặc Dialog cho đẹp
    @Override
    public String toString() {
        return MaVoucher + " (-" + GiamGia + "%)";
    }
}
