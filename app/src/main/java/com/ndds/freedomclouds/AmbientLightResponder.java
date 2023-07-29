package com.ndds.freedomclouds;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AmbientLightResponder implements SensorEventListener {
    MainActivity activity;
    private boolean isBright;
    private boolean isAmbientLightResponsive = true;

    public void isAmbientLightResponsive(Boolean isResponsive) {
        isAmbientLightResponsive = isResponsive;
    }

    public boolean isAmbientLightResponsive() {
        return isAmbientLightResponsive;
    }


    AmbientLightResponder(MainActivity activity) {
        this.activity = activity;
        SensorManager mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        Sensor mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        mSensorManager.registerListener(
                this, mLightSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean newIsBright = event.values[0] > 15;
        if (isAmbientLightResponsive && isBright != newIsBright) {
            activity.audio.playSound(R.raw.light_switch);
            isBright = newIsBright;
            activity.onAmbientBrightnessChanged(isBright);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
