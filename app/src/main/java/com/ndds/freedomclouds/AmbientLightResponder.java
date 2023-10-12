package com.ndds.freedomclouds;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AmbientLightResponder implements SensorEventListener {
    private final SensorManager sensorManager;
    public final static String AMBIENT_LIGHT_RESPONSIVE = "swapAmbientResponsiveness";
    MainActivity activity;
    private static final float threshold = 20;
    private boolean isAmbientLightResponsive;
    private boolean previousIsBright = true;
    private final Sensor lightSensor;

    public void setAmbientResponsiveness(boolean isResponsive) {
        isAmbientLightResponsive = isResponsive;
        if(isAmbientLightResponsive) start();
        else {
            activity.onAmbientBrightnessChanged(1.0f);
            release();
        }
    }

    public void resumeSensor() {
        if (isAmbientLightResponsive) start();
    }

    private void start() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    AmbientLightResponder(MainActivity activity, SharedPreferences sharedPreferences) {
        this.activity = activity;
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        isAmbientLightResponsive = sharedPreferences.getBoolean(AMBIENT_LIGHT_RESPONSIVE, true);
        if (isAmbientLightResponsive) start();
    }

    public void release() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean isBright = event.values[0] > threshold;
        if(!isBright || !previousIsBright) {
            double factor = event.values[0] > threshold ? 1.0 : (event.values[0] / threshold * 0.5) + 0.5;
            activity.onAmbientBrightnessChanged(factor);
        }
        previousIsBright = isBright;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
