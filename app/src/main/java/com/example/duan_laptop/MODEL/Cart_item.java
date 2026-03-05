package com.example.duan_laptop.MODEL;

import java.io.Serializable;

public class Cart_item implements Serializable {
    public int MaGioHang;
    public int MaLapTop;
    public String SDT;
    public int SoLuong;
    public String NgayThem;

    public laptop_model laptop;
    public long GiaTong;

    public Cart_item() {
    }

    public Cart_item(laptop_model laptop, int soLuong) {
        this.laptop = laptop;
        this.SoLuong = soLuong;

        // Tự động gán các thông tin phụ từ laptop sang
        if (laptop != null) {
            this.MaLapTop = laptop.MaLapTop;
            this.updateGiaTong(); // Tính luôn giá tổng = Giá * Số lượng
        }
    }

    public Cart_item(int maGioHang, int maLapTop, String SDT, int soLuong, String ngayThem, laptop_model laptop, long giaTong) {
        MaGioHang = maGioHang;
        MaLapTop = maLapTop;
        this.SDT = SDT;
        SoLuong = soLuong;
        NgayThem = ngayThem;
        this.laptop = laptop;
        GiaTong = giaTong;
    }

    public void updateGiaTong() {
        if (this.laptop != null) {
            this.GiaTong = (long) this.SoLuong * this.laptop.Gia;
        }
    }
}