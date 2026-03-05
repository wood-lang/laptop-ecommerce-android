package com.example.duan_laptop.MODEL;

import java.util.ArrayList;

public class ChatMessage {

    public boolean isUser; // true: người dùng, false: AI
    // THAY BẰNG DANH SÁCH CÁC ĐOẠN NỘI DUNG
    private ArrayList<ChatSegment> segments;

    // Constructor mới
    public ChatMessage(boolean isUser, ArrayList<ChatSegment> segments) {
        this.isUser = isUser;
        this.segments = segments;
    }

    // Constructor tiện ích để tạo nhanh tin nhắn chỉ có text (dùng cho tin nhắn chào mừng, tin nhắn chờ)
    public static ChatMessage createTextMessage(String text, boolean isUser) {
        ArrayList<ChatSegment> segs = new ArrayList<>();
        segs.add(new ChatSegment(text, null));
        return new ChatMessage(isUser, segs);
    }

    public boolean isUser() { return isUser; }
    public ArrayList<ChatSegment> getSegments() { return segments; }
}