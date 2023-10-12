package com.ndds.freedomclouds;

public interface MainActivityCallback {
    void openCurtain(Boolean isNewUser);
    void updateDate(int year, int month, int dayOfMonth);
    void onAmbientLightResponsivenessChanged(boolean isResponsive);
    void onAmbientBrightnessChanged(double brightnessFactor);
    void setTaskAfterSecurityUnlock(PasscodeShield.TaskAfterUnlock task);
    void shouldDrawDynamicEmblem(boolean enable);
    void onUpdateEventTitle(String title);
}
