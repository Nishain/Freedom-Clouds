package com.ndds.freedomclouds.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    final SettingsPage controller;

    public SettingsFragment(SettingsPage controller) {
        this.controller = controller;
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root != null)
            root.requestLayout();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup content = (ViewGroup) inflater.inflate(controller.layoutId, container, false);
        controller.setUpContent(content);
        return content;
    }
}
