package com.example.water_logger;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HistoryViewPagerAdapter extends FragmentStateAdapter {

    public HistoryViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new HistoryWeekFragment();
        }
        return new HistoryDayFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}