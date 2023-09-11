package com.ndds.freedomclouds.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ndds.freedomclouds.MainActivity;
import com.ndds.freedomclouds.R;
import com.ndds.freedomclouds.common.SheetAlert;

public class Settings {
    final Bundle bundle;

    static class Bundle {
        final SharedPreferences sharedPreferences;
        final MainActivity activity;
        SheetAlert dialog;


        Bundle(SharedPreferences sharedPreferences, MainActivity activity) {
            this.sharedPreferences = sharedPreferences;
            this.activity = activity;
        }
    }


    public Settings(MainActivity activity, SharedPreferences sharedPreferences) {
        this.bundle = new Bundle(sharedPreferences, activity);
    }

    @SuppressLint("DefaultLocale")
    public void show() {
        MainActivity activity = bundle.activity;
        ViewGroup rootView = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.settings_root,null);
        SheetAlert sheetAlert = new SheetAlert(
                rootView,
                activity,
                SheetAlert.Position.TOP);
        bundle.dialog = sheetAlert;
        ViewPager2 viewPager = rootView.findViewById(R.id.settings_pager);
        TabLayout tabLayout = rootView.findViewById(R.id.settings_tabs);
        rootView.findViewById(R.id.settings_dismiss).setOnClickListener(v -> sheetAlert.dismiss());
        SettingsPageAdapter viewPagerAdapter = new SettingsPageAdapter(activity, new SettingsPage[]{
                new GeneralSettings(bundle),
                new NotificationSettings(bundle),
                new PasscodeSettings(bundle),
                new PurchaseSettings(bundle)
        });
        TextView titleHeadline = rootView.findViewById(R.id.settingsHeadline);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                titleHeadline.setText(viewPagerAdapter.getTitle(position));
            }
        });
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
        sheetAlert.show(true);

        activity.audio.playSound(R.raw.paper_flip);
    }
}
