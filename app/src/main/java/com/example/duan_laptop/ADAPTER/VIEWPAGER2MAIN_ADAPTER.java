package com.example.duan_laptop.ADAPTER;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class VIEWPAGER2MAIN_ADAPTER extends FragmentStateAdapter {

    private ArrayList<Fragment> fragmentList;

    public VIEWPAGER2MAIN_ADAPTER(
            @NonNull FragmentActivity fragmentActivity,
            ArrayList<Fragment> fragmentList
    ) {
        super(fragmentActivity);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
