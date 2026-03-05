package com.example.duan_laptop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.ChatAdapter;
import com.example.duan_laptop.MODEL.ChatMessage;
import com.example.duan_laptop.MODEL.ChatSegment;
import com.r0adkll.slidr.Slidr;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity2 extends AppCompatActivity {
    RecyclerView rcvChat;
    EditText edtMessage;
    ImageButton btnSend;
    ArrayList<ChatMessage> listChat;
    ChatAdapter adapter;

    // Launcher giọng nói giữ nguyên
    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        edtMessage.setText(matches.get(0));
                        btnSend.performClick();
                    }
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);
        Slidr.attach(this);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 1. Đổi nền thanh tiêu đề thành Trắng
            actionBar.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));

            // 2. Đổi chữ thành "Trợ lý AI" màu Đen Xám, in đậm
            actionBar.setTitle(android.text.Html.fromHtml("<font color='#111827'><b>✨ Trợ lý AI</b></font>"));

            // 3. Hiện nút mũi tên Back
            actionBar.setDisplayHomeAsUpEnabled(true);

            // 4. Đổi màu nút Back sang Đen (Không có hàm này là nút Back bị tàng hình đó)
            android.graphics.drawable.Drawable upArrow = androidx.core.content.ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            if (upArrow != null) {
                upArrow.setColorFilter(android.graphics.Color.parseColor("#111827"), android.graphics.PorterDuff.Mode.SRC_ATOP);
                actionBar.setHomeAsUpIndicator(upArrow);
            }

            // 5. Thêm đổ bóng mờ ảo (Elevation) cắt ngang nội dung chat
            actionBar.setElevation(12f);
        }

        // 6. "Tẩy trắng" luôn thanh trạng thái trên cùng (chỗ hiển thị Pin, Giờ, Wifi)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(android.graphics.Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        rcvChat = findViewById(R.id.rcvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        Toolbar toolbar = findViewById(R.id.toolbarChat);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        listChat = new ArrayList<>();
        adapter = new ChatAdapter(listChat);
        rcvChat.setLayoutManager(new LinearLayoutManager(this));
        rcvChat.setAdapter(adapter);

        loadHistory(SERVER.user.SDT);

        btnSend.setOnClickListener(v -> {
            String question = edtMessage.getText().toString().trim();
            if (question.isEmpty()) return;

            // Tạo tin nhắn người dùng (chỉ có text)
            listChat.add(ChatMessage.createTextMessage(question, true));
            adapter.notifyItemInserted(listChat.size() - 1);
            rcvChat.scrollToPosition(listChat.size() - 1);
            edtMessage.setText("");

            // Lưu và hỏi AI. Lưu ý: Database hiện tại của bồ chỉ lưu được 1 ảnh mỗi tin nhắn.
            // Nếu muốn lưu chuẩn cấu trúc mới thì phải sửa DB. Tạm thời lưu text gốc.
            saveChatToDB(SERVER.user.SDT, question, 1, null);
            askAI(question);
        });

        findViewById(R.id.btnVoice).setOnClickListener(v -> {
            // Intent giọng nói giữ nguyên
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
            try { voiceLauncher.launch(intent); } catch (Exception e) {
                Toast.makeText(this, "Thiết bị không hỗ trợ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  TÁCH PHẢN HỒI AI THÀNH CÁC ĐOẠN ---
    private ArrayList<ChatSegment> parseAiReplyToSegments(String rawReply) {
        ArrayList<ChatSegment> segments = new ArrayList<>();
        // Tách chuỗi thành từng dòng dựa vào ký tự xuống dòng \n
        String[] lines = rawReply.split("\n");
        Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\s*\\]");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue; // Bỏ qua dòng trống

            Matcher matcher = pattern.matcher(line);
            String imgUrl = null;
            String textContent = line;

            // Tìm xem dòng này có ảnh không
            if (matcher.find()) {
                String fileName = matcher.group(1).trim();
                if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".png")) {
                    imgUrl = SERVER.server_img + fileName;
                    // Xóa tag ảnh khỏi dòng text để hiển thị cho đẹp
                    textContent = line.replace("[" + matcher.group(1) + "]", "").trim();
                }
            }
            // Tạo một đoạn (Segment) cho dòng này
            segments.add(new ChatSegment(textContent, imgUrl));
        }
        return segments;
    }

    private void askAI(String question) {
        // Tin nhắn chờ
        ChatMessage waitingMsg = ChatMessage.createTextMessage("Đang tìm thông tin...", false);
        listChat.add(waitingMsg);
        adapter.notifyItemInserted(listChat.size() - 1);
        rcvChat.scrollToPosition(listChat.size() - 1);

        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_chat_ai,
                response -> {
                    try {
                        int pos = listChat.indexOf(waitingMsg);
                        if (pos != -1) { listChat.remove(pos); adapter.notifyItemRemoved(pos); }

                        JSONObject jsonObject = new JSONObject(response);
                        String rawReply = jsonObject.getString("reply");

                        // --- DÙNG HÀM PARSE MỚI ---
                        ArrayList<ChatSegment> segments = parseAiReplyToSegments(rawReply);

                        // Tạo tin nhắn với danh sách các đoạn đã tách
                        listChat.add(new ChatMessage(false, segments));
                        adapter.notifyItemInserted(listChat.size() - 1);
                        rcvChat.scrollToPosition(listChat.size() - 1);

                        // Lưu vào DB: Tạm thời lưu text gốc và ảnh đầu tiên tìm thấy (nếu có)
                        String firstImg = null;
                        for(ChatSegment seg : segments) {
                            if(seg.getImageUrl() != null) { firstImg = seg.getImageUrl(); break; }
                        }
                        saveChatToDB(SERVER.user.SDT, rawReply, 0, firstImg);

                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> {
                    int pos = listChat.indexOf(waitingMsg);
                    if (pos != -1) { listChat.remove(pos); adapter.notifyItemRemoved(pos); }
                }
        ) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("question", question);
                params.put("user_id", SERVER.user.SDT);
                return params;
            }
            // Các hàm override xử lý UTF-8 giữ nguyên
            @Override protected com.android.volley.Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try { String utf8String = new String(response.data, "UTF-8"); return com.android.volley.Response.success(utf8String, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)); } catch (Exception e) { return com.android.volley.Response.error(new com.android.volley.ParseError(e)); }
            }
            @Override public String getBodyContentType() { return "application/x-www-form-urlencoded; charset=UTF-8"; }
        };
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(30000, 0, 1f));
        Volley.newRequestQueue(this).add(request);
    }

    // Hàm loadHistory cần sửa lại để tương thích với cấu trúc mới.
    // LƯU Ý: Vì DB cũ chỉ lưu 1 message string và 1 image url, nên khi load lại
    // nó sẽ hiển thị dạng: 1 đoạn text dài + 1 ảnh ở cuối cùng.
    // Chỉ những tin nhắn mới chat với AI sau khi sửa code này mới hiện đẹp kiểu xen kẽ.
    private void loadHistory(String userId) {
        String url = SERVER.url_get_history + "?user_id=" + userId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        listChat.clear();

                        // Tin nhắn chào mừng mặc định
                        listChat.add(ChatMessage.createTextMessage("Chào bạn!\nMình là trợ lí AI!\nMình có thể giúp gì bạn?", false));

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String msgText = obj.getString("message");
                            boolean isUser = obj.getBoolean("is_user");
                            String imgUrl = null;

                            // Lấy URL ảnh nếu CSDL có lưu (dành cho tin nhắn cũ)
                            if (!obj.isNull("image_url")) {
                                String temp = obj.getString("image_url");
                                if (!temp.equals("null") && !temp.trim().isEmpty()) {
                                    imgUrl = temp;
                                }
                            }

                            // ĐÂY LÀ CHÌA KHÓA: Phải phân tích lại cái msgText để xóa bỏ thẻ [ảnh]
                            ArrayList<ChatSegment> segments;
                            if (!isUser) {
                                // Nếu là AI nói -> Bóc tách text và ảnh (nó sẽ tự xóa cái [laptop.jpg])
                                segments = parseAiReplyToSegments(msgText);

                                // Nếu DB cũ có lưu 1 cái imgUrl riêng lẻ, mà lúc parse không thấy, thì nhét thêm vào cuối
                                if (imgUrl != null && !imgUrl.isEmpty()) {
                                    boolean hasImage = false;
                                    for (ChatSegment seg : segments) {
                                        if (seg.getImageUrl() != null && !seg.getImageUrl().isEmpty()) {
                                            hasImage = true; break;
                                        }
                                    }
                                    if (!hasImage) {
                                        segments.add(new ChatSegment("", imgUrl));
                                    }
                                }
                            } else {
                                // Nếu là khách hàng nhắn -> Giữ nguyên, không cần bóc tách
                                segments = new ArrayList<>();
                                segments.add(new ChatSegment(msgText, imgUrl));
                            }

                            // Đóng gói thành ChatMessage và nhét vào List
                            listChat.add(new ChatMessage(isUser, segments));
                        }

                        adapter.notifyDataSetChanged();
                        if (!listChat.isEmpty()) rcvChat.scrollToPosition(listChat.size() - 1);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("DEBUG_HISTORY", "Lỗi load lịch sử: " + e.getMessage());
                    }
                }, error -> { }
        ) {
            // Override UTF-8 giữ nguyên
            @Override protected com.android.volley.Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return com.android.volley.Response.success(utf8String, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.ParseError(e));
                }
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void saveChatToDB(String userId, String msg, int isUser, String img) {
        StringRequest request = new StringRequest(Request.Method.POST, SERVER.url_save_chat, response -> {}, error -> {}) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId); params.put("message", msg); params.put("is_user", String.valueOf(isUser)); params.put("image_url", img != null ? img : ""); return params;
            }
            @Override public String getBodyContentType() { return "application/x-www-form-urlencoded; charset=UTF-8"; }
            @Override protected String getParamsEncoding() { return "UTF-8"; }
        };
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(5000, com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES, com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }
}