package com.ndds.freedomclouds;

public interface MainActivityCallback {
    void openCurtain(Boolean isNewUser);
    void updateDate(int year, int month, int dayOfMonth);
    void onAmbientLightResponsivenessChanged(boolean isResponsive);
    void onAmbientBrightnessChanged(boolean isBright);
    boolean isAmbientLightResponsive();
}
