package com.example.duan_laptop.MODEL;

public class ChatSegment {
    private String textContent;
    private String imageUrl; // Ảnh đi kèm đoạn text này (có thể null)

    public ChatSegment(String textContent, String imageUrl) {
        this.textContent = textContent;
        this.imageUrl = imageUrl;
    }

    public String getTextContent() { return textContent; }
    public String getImageUrl() { return imageUrl; }
}