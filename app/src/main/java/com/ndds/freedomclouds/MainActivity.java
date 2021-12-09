package com.ndds.freedomclouds;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    private OpenGLScreen openGLScreen;
    private Handler idleHandler;
    private ObjectAnimator rotateHintAnimator;
    private long startTime = 0;
    SharedPreferences sharedPreferences;
    private AlertDialog settingsAlert;

    private void initOpenGL(){
        openGLScreen = findViewById(R.id.gl_screen);
        openGLScreen.activity = MainActivity.this;
        openGLScreen.setIdleHandler(idleHandler,rotateHintAnimator);
        openGLScreen.getHolder().setFormat(PixelFormat.TRANSPARENT);
        openGLScreen.setZOrderOnTop(true);
    }
    private static final String START_TIME = "startTime";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0,0);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("configuration",MODE_PRIVATE);
        if(sharedPreferences.contains(START_TIME)){
            String[] dateComponents = sharedPreferences.getString(START_TIME,"").split("/");
            int year = Integer.parseInt(dateComponents[0]);
            int month = Integer.parseInt(dateComponents[1]);
            int dayOfMonth = Integer.parseInt(dateComponents[2]);
            updateDate(year,month,dayOfMonth,false);
        }
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
                ((ViewGroup)findViewById(R.id.root)).addView(getLayoutInflater().inflate(R.layout.opengl_layout,null),
                        0);
                initOpenGL();
                if(!sharedPreferences.contains(START_TIME))
                    showInstruction();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.splash_overlay).startAnimation(slideUpAnimation);

//        View dashboard = getLayoutInflater().inflate(R.layout.dashboard,null);
//
//        ((ViewGroup)findViewById(R.id.root)).addView(dashboard, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
//        findViewById(R.id.interface_container).setZ();
        View v = findViewById(R.id.rotate_hint);
        rotateHintAnimator = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        rotateHintAnimator.setRepeatMode(ValueAnimator.REVERSE);
        rotateHintAnimator.setRepeatCount(7);
        rotateHintAnimator.setDuration(500);
        idleHandler = new Handler();

        rotateHintAnimator.start();
    }
    public void showCalender(){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Select a starting date");
        datePickerDialog.show();
    }
    public void glow(View v){
        openGLScreen.glow();
    }
    public void showSettings(View v){
        ViewGroup settingsView = (ViewGroup)getLayoutInflater().inflate(R.layout.settings,null);

        settingsView.findViewById(R.id.settings_change_date).setOnClickListener(this);
        settingsView.findViewById(R.id.settings_reset_today).setOnClickListener(this);
        settingsView.findViewById(R.id.settings_developer_notice).setOnClickListener(this);
        settingsView.findViewById(R.id.reset_memory).setOnClickListener(this);
        settingsAlert = new AlertDialog.Builder(this).setView(settingsView).create();
        settingsAlert.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        updateDate(year,month,dayOfMonth,true);
    }

    @SuppressLint("DefaultLocale")
    private void updateDate(int year, int month, int dayOfMonth,boolean doSave){
        Calendar settedCalender = Calendar.getInstance();
        settedCalender.set(year,month,dayOfMonth);
        Calendar currentCalender = Calendar.getInstance();
        int days = (int) ((currentCalender.getTime().getTime() - settedCalender.getTime().getTime()) / (1000 * 60 * 60 * 24));
        ((ProgressBar) findViewById(R.id.weekDaysProgress)).setProgress(days % 7);
        ProgressBar daysInMonthProgress = findViewById(R.id.monthDaysProgress);
        daysInMonthProgress.setProgress(days % 30);
        //weeksCompletedCount
        ((TextView)findViewById(R.id.dayCount)).setText(String.format("%d Days", days));
        ((TextView)findViewById(R.id.weeksCompletedCount)).setText(String.format("weeks Completed  %d", days / 7));
        ((TextView)findViewById(R.id.remaining_7_days_txt)).setText(String.format("%s %s",getString(R.string.days_completed_in_week), days % 7+"/7"));
        ((TextView)findViewById(R.id.remaining_30_days_txt)).setText(String.format("%s %s",getString(R.string._30_days_progress), days % 30+"/30"));
        startTime = settedCalender.getTime().getTime();
        if(doSave)
            sharedPreferences.edit().putString(START_TIME,String.format("%d/%d/%d",year,month,dayOfMonth)).apply();
    }
    @Override
    public void onClick(View v) {
        settingsAlert.dismiss();
        switch (v.getId()){
            case R.id.settings_change_date:
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.settings_reset_today:
                Calendar now = Calendar.getInstance();
                updateDate(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH),true);
                break;
            case R.id.settings_developer_notice:
                ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.developer_note,null);
                AlertDialog noteAlert = new AlertDialog.Builder(this).setView(viewGroup).create();
                viewGroup.findViewById(R.id.developer_note_ok).setOnClickListener(v1 -> noteAlert.dismiss());
                noteAlert.show();
                break;
            case  R.id.reset_memory:
                sharedPreferences.edit().remove(START_TIME).apply();
                Toast.makeText(this, "will be effected on next launch", Toast.LENGTH_SHORT).show();
        }
    }
    private void showInstruction(){
        ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.introduction,null);

        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(viewGroup).setCancelable(false).create();
        viewGroup.findViewById(R.id.introduction_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                showCalender();
            }
        });

        alertDialog.show();
    }

}
