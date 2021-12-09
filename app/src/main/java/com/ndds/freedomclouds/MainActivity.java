package com.ndds.freedomclouds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private OpenGLScreen openGLScreen;
    private Handler idleHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(0,0);
        setContentView(R.layout.activity_main);
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this,R.anim.push_up_anim);
        View splashOverlay = findViewById(R.id.splash_overlay);
        splashOverlay.setVisibility(View.VISIBLE);

        slideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashOverlay.setVisibility(View.GONE);
                openGLScreen.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.splash_overlay).startAnimation(slideUpAnimation);
        openGLScreen = findViewById(R.id.gl_screen);
        openGLScreen.activity = this;
        openGLScreen.setVisibility(View.INVISIBLE);
//        View dashboard = getLayoutInflater().inflate(R.layout.dashboard,null);
//
//        ((ViewGroup)findViewById(R.id.root)).addView(dashboard, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
//        findViewById(R.id.interface_container).setZ();
        View v = findViewById(R.id.rotate_hint);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setRepeatCount(7);
        objectAnimator.setDuration(500);
        idleHandler = new Handler();
        openGLScreen.setIdleHandler(idleHandler,objectAnimator);
        openGLScreen.getHolder().setFormat(PixelFormat.TRANSPARENT);
        openGLScreen.setZOrderOnTop(true);
        objectAnimator.start();
    }
    public void showCalender(View v){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
//        DialogFragment newFragment = new TimePickerFragment();
//        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    public void glow(View v){
        openGLScreen.glow();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar settedCalender = Calendar.getInstance();
        settedCalender.set(year,month,dayOfMonth);
        Calendar currentCalender = Calendar.getInstance();
        int days = (int) ((currentCalender.getTime().getTime() - settedCalender.getTime().getTime()) / (1000 * 60 * 60 * 24));
        ((ProgressBar) findViewById(R.id.weekDaysProgress)).setProgress(days % 7);
        ProgressBar daysInMonthProgress = findViewById(R.id.monthDaysProgress);
        daysInMonthProgress.setProgress(days % 30);
        //weeksCompletedCount
        ((TextView)findViewById(R.id.weeksCompletedCount)).setText(String.format("weeks Completed  %d", days / 7));
        Toast.makeText(this, "Days "+days, Toast.LENGTH_SHORT).show();
    }
}
