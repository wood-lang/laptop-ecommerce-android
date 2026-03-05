package com.example.duan_laptop.MODEL;

import java.io.Serializable;

public class DonHang implements Serializable {
    public int MaHD;

    public String NgayLap;
    public String TenLapTop;

    public long TongTien;
    public String HinhAnh;
    public int SoLuong;
    public int TrangThai;
    public long DonGia;
    public String DanhSachAnh;
    public String DanhSachSoLuong;

    public DonHang(int maHD, String ngayLap, String tenLapTop, long tongTien, String hinhAnh, int soLuong, int trangThai, long donGia, String danhSachAnh, String danhSachSoLuong) {
        MaHD = maHD;
        NgayLap = ngayLap;
        TenLapTop = tenLapTop;
        TongTien = tongTien;
        HinhAnh = hinhAnh;
        SoLuong = soLuong;
        TrangThai = trangThai;
        DonGia = donGia;
        DanhSachAnh = danhSachAnh;
        DanhSachSoLuong = danhSachSoLuong;
    }

    public String getDanhSachAnh() {
        return DanhSachAnh;
    }

    public void setDanhSachAnh(String danhSachAnh) {
        DanhSachAnh = danhSachAnh;
    }

    public int getMaHD() {
        return MaHD;
    }

    public void setMaHD(int maHD) {
        MaHD = maHD;
    }

    public String getNgayLap() {
        return NgayLap;
    }

    public void setNgayLap(String ngayLap) {
        NgayLap = ngayLap;
    }

    public String getTenLapTop() {
        return TenLapTop;
    }

    public void setTenLapTop(String tenLapTop) {
        TenLapTop = tenLapTop;
    }

    public long getTongTien() {
        return TongTien;
    }

    public void setTongTien(long tongTien) {
        TongTien = tongTien;
    }

    public String getHinhAnh() {
        return HinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        HinhAnh = hinhAnh;
    }

    public int getSoLuong() {
        return SoLuong;
    }

    public void setSoLuong(int soLuong) {
        SoLuong = soLuong;
    }

    public int getTrangThai() {
        return TrangThai;
    }

    public void setTrangThai(int trangThai) {
        TrangThai = trangThai;
    }

    public long getDonGia() {
        return DonGia;
    }

    public void setDonGia(long donGia) {
        DonGia = donGia;
    }

    // Hàm hỗ trợ lấy text trạng thái (để dùng trong Adapter)
    public String getTrangThaiText() {
        switch (TrangThai) {
            case 0: return "Chờ xác nhận";
            case 1: return "Đang đóng gói";
            case 2: return "Đang giao hàng";
            case 3: return "Giao thành công";
            case 4: return "ĐÃ HỦY";
            default: return "Đang xử lý";
        }
    }
}