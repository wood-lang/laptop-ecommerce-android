package com.example.duan_laptop.FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.duan_laptop.ADAPTER.Cart_adapter;
import com.example.duan_laptop.Cart_manager;
import com.example.duan_laptop.HELPER.CustomDialogHelper;
import com.example.duan_laptop.MODEL.Cart_item;
import com.example.duan_laptop.MODEL.laptop_model;
import com.example.duan_laptop.SERVER;
import com.example.duan_laptop.ThanhToanActivity2;
import com.example.duan_laptop.databinding.FragmentCartLayoutBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Cart_fragment extends Fragment implements Cart_manager.CartListener {

    private FragmentCartLayoutBinding binding;
    private Cart_adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Cart_adapter(getContext(), Cart_manager.manggiohang);
        binding.recyclerCart.setAdapter(adapter);

        binding.btnThanhToan.setOnClickListener(v -> {
            if (!Cart_manager.manggiohang.isEmpty()) {
                startActivity(new Intent(getContext(), ThanhToanActivity2.class));
            } else {
                new CustomDialogHelper(requireContext()).showError("Giỏ hàng trống", "Bạn chưa chọn chiếc laptop nào cả. Hãy dạo quanh shop thêm nhé!");
            }
        });

        // Load từ Server để đồng bộ số lượng mới nhất
        Cart_manager.loadCartFromServer(getContext());
        // Trong onCreateView hoặc onViewCreated của Cart_fragment.java
        Cart_manager.addListener(new Cart_manager.CartListener() {
            @Override
            public void onCartChanged() {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) adapter.notifyDataSetChanged();
                        updateUI();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Cart_manager.addListener(this);
        updateUI();
    }

    @Override
    public void onStop() {
        super.onStop();
        Cart_manager.removeListener(this);
    }


    @Override
    public void onCartChanged() {
        if (isAdded()) updateUI();
    }

    private void updateUI() {
        if (binding != null) {
            long total = Cart_manager.getTotalPrice();
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvTongTien.setText("Tổng tiền: " + format.format(total));

            if (Cart_manager.manggiohang.isEmpty()) {
                binding.tvEmptyCart.setVisibility(View.VISIBLE);
                binding.recyclerCart.setVisibility(View.GONE);
            } else {
                binding.tvEmptyCart.setVisibility(View.GONE);
                binding.recyclerCart.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}