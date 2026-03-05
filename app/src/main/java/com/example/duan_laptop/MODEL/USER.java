package com.example.duan_laptop.MODEL;

public class USER {

    public String TenKH,SDT,Password;
    public String DiaChi;
    public String Email;
    public String Avatar;

    public USER(String tenKH, String SDT, String password, String diaChi, String email, String avatar) {
        TenKH = tenKH;
        this.SDT = SDT;
        Password = password;
        DiaChi = diaChi;
        Email = email;
        Avatar = avatar;
    }

    public USER() {

    }

    public String getTenKH() {
        return TenKH;
    }

    public String getSDT() {
        return SDT;
    }


    public String getDiaChi() {
        return DiaChi;
    }

    public String getEmail() {
        return Email;
    }
}
