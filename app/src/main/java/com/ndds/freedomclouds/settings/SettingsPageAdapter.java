package com.ndds.freedomclouds.settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SettingsPageAdapter extends FragmentStateAdapter {
    private final SettingsPage[] settingsTabs;

    public SettingsPageAdapter(@NonNull FragmentActivity fragmentActivity, SettingsPage[] settingsTabs) {
        super(fragmentActivity);
        this.settingsTabs = settingsTabs;
    }

    public String getTitle(int position) {
        return settingsTabs[position].title;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new SettingsFragment(settingsTabs[position]);
    }

    @Override
    public int getItemCount() {
        return settingsTabs.length;
    }
}
