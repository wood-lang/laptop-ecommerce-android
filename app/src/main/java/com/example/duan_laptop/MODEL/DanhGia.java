package com.example.duan_laptop.MODEL;

public class DanhGia {
    public int MaDG;
    public String SDT; // Thêm SDT để so sánh chủ sở hữu
    public String TenKH;
    public String NoiDung;
    public int SoSao;
    public String NgayDanhGia;
    public String HinhAnh;
    public String Video;

    public DanhGia(int maDG, String SDT, String tenKH, String noiDung, int soSao, String ngayDanhGia, String hinhAnh, String video) {
        MaDG = maDG;
        this.SDT = SDT;
        TenKH = tenKH;
        NoiDung = noiDung;
        SoSao = soSao;
        NgayDanhGia = ngayDanhGia;
        HinhAnh = hinhAnh;
        Video = video;
    }

    public DanhGia() {
    }
}