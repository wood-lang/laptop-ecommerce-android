package com.example.duan_laptop.MODEL;

public class GIOHANG_MODEL {
     String id;
    String name,image;
     double price;
     int quantity;

    public GIOHANG_MODEL(String image,String id, String name, double price, int quantity) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public GIOHANG_MODEL() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
