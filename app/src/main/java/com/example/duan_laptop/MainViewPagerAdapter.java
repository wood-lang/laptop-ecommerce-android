package com.example.duan_laptop;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.duan_laptop.FRAGMENT.Account_fragment;
import com.example.duan_laptop.FRAGMENT.Cart_fragment;
import com.example.duan_laptop.FRAGMENT.Favorite_fragment;
import com.example.duan_laptop.FRAGMENT.HOME_FRAGMENT;
import com.example.duan_laptop.FRAGMENT.ThongBao_fragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HOME_FRAGMENT();
            case 1:
                return new Cart_fragment();
            case 2:
                return new Favorite_fragment();
            case 3: return new ThongBao_fragment();
            case 4: return new Account_fragment();
            default:
                return new HOME_FRAGMENT();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // có 5 tab ở BottomNav
    }
}