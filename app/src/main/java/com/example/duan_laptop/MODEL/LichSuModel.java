package com.example.duan_laptop.MODEL;

public class LichSuModel {
    // 1. Thêm biến TrangThai để biết đơn Hủy hay chưa
    public int MaHD;
    public int TrangThai;
    public String NgayLap;

    // 2. Đổi sang long cho an toàn với tiền tỉ
    public long TongTien;

    // Các thông tin phụ (để hiển thị tượng trưng 1 sản phẩm)
    public String TenLapTop;
    public String HinhAnh;
    public int SoLuong;
    public int DonGia;
    public LichSuModel(int maHD, String ten, String hinh, String ngay, int trangThai, long tongTien) {
        this.MaHD = maHD;
        this.TenLapTop = ten;
        this.HinhAnh = hinh;
        this.NgayLap = ngay;
        this.TrangThai = trangThai;
        this.TongTien = tongTien;
    }
}