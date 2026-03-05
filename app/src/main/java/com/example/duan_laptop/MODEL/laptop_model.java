package com.example.duan_laptop.MODEL;

import java.io.Serializable;

public class laptop_model implements Serializable {
   public int MaLoai,masx,MaKH,MaNCC,SoLuongBan,SoLuongMua,Gia;
    public int MaLapTop;
    public String TenLapTop;
    public String MoTa;
    public String HinhAnh;
    public String HinhChiTiet;
    public String HangSX;
    public String CPU;
    public String ngaycapnhat;
    public boolean isFavorite = false;
    public int tempSoLuongMua;
    public int SoLuong;

    public laptop_model(int maLoai, int masx, int maKH, int maNCC, int soLuongBan, int soLuongMua, int gia, int maLapTop, String tenLapTop, String moTa, String hinhAnh, String hinhChiTiet, String hangSX, String CPU, String ngaycapnhat, boolean isFavorite, int tempSoLuongMua, int soLuong) {
        MaLoai = maLoai;
        this.masx = masx;
        MaKH = maKH;
        MaNCC = maNCC;
        SoLuongBan = soLuongBan;
        SoLuongMua = soLuongMua;
        Gia = gia;
        MaLapTop = maLapTop;
        TenLapTop = tenLapTop;
        MoTa = moTa;
        HinhAnh = hinhAnh;
        HinhChiTiet = hinhChiTiet;
        HangSX = hangSX;
        this.CPU = CPU;
        this.ngaycapnhat = ngaycapnhat;
        this.isFavorite = isFavorite;
        this.tempSoLuongMua = tempSoLuongMua;
        SoLuong = soLuong;
    }
    public laptop_model() {
    }

}
