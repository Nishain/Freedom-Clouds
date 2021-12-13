package com.ndds.freedomclouds;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    private OpenGLScreen openGLScreen;
    private Handler idleHandler;
    private ObjectAnimator rotateHintAnimator;
    private long startTime = 0;
    SharedPreferences sharedPreferences;
    private AlertDialog settingsAlert;
    private SoundPool soundPool;

    private void initOpenGL(int tag){
        ViewGroup view = (ViewGroup)getLayoutInflater().inflate(R.layout.opengl_layout, null);

        (view.getChildAt(0)).setTag(String.valueOf(tag));
        ((ViewGroup)findViewById(R.id.root)).addView(view, 0);
        ArrayList<Bitmap> bitmaps = null;
        openGLScreen = findViewById(R.id.gl_screen);
        if(String.valueOf(tag).equals("1")) {
            bitmaps = new ArrayList<>();
            AssetManager assetManager = getResources().getAssets();

            String[] emblemTypes = null;

            try {
                    String[] fileList = assetManager.list("borderless");
                for (String file : Objects.requireNonNull(fileList)) {
                    if(file.equals("names.txt"))
                        emblemTypes = new BufferedReader(new InputStreamReader(assetManager.open("borderless/"+file))).readLine().split(",");
                    else
                        bitmaps.add(BitmapFactory.decodeStream(assetManager.open("borderless/"+file)));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            openGLScreen.initRenderer(this,bitmaps,emblemTypes);
        }else
            openGLScreen.initRenderer(this);
        openGLScreen.activity = MainActivity.this;
        openGLScreen.setIdleHandler(idleHandler,rotateHintAnimator);
        openGLScreen.getHolder().setFormat(PixelFormat.TRANSPARENT);
        openGLScreen.setZOrderOnTop(true);
    }
    private void initSoundPool(){
        AudioAttributes attributes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(1)
                    .build();
        }else {
            soundPool = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        }
    }
    public void playSound(int id){
        int soundId = soundPool.load(this,id, 1);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(soundId, 1, 1, 1, 0, 1));
    }

    @Override
    public void finish() {
        soundPool.release();
        super.finish();
    }

    private static final String START_TIME = "startTime";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0,0);
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.emblemType)).setTypeface(Typeface.createFromAsset(getAssets(),"fonts/bananaFont.ttf"));
        initSoundPool();
        findViewById(R.id.emblem_loading_indicator).setVisibility(View.GONE);
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

                if(!sharedPreferences.contains(START_TIME)) {
                    ((TextView) findViewById(R.id.emblemType)).setText("The Gift");
                    initOpenGL(2);
                    showInstruction();
                }else
                    initOpenGL(1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.splash_overlay).startAnimation(slideUpAnimation);
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
    public void setEmblemTypeText(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.emblemType)).setText(text+" Emblem");
            }
        });
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
        if(days < 0){
            Toast.makeText(this, "Can only enter a date on or before today's date", Toast.LENGTH_LONG).show();
            return;
        }
        ((ProgressBar) findViewById(R.id.weekDaysProgress)).setProgress(days % 7);
        ProgressBar daysInMonthProgress = findViewById(R.id.monthDaysProgress);
        daysInMonthProgress.setProgress(days % 30);
        //weeksCompletedCount
        ((TextView)findViewById(R.id.dayCount)).setText(String.format("%d Days Week %d", days,days / 7));
        ((TextView)findViewById(R.id.startDateTxt)).setText(String.format("Since %d/%d/%d",dayOfMonth,month+1,year));
        ((TextView)findViewById(R.id.remaining_7_days_txt)).setText(String.format("%s %s",getString(R.string.days_completed_in_week), days % 7+"/7"));
        ((TextView)findViewById(R.id.remaining_30_days_txt)).setText(String.format("%s %s",getString(R.string._30_days_progress), days % 30+"/30"));
        startTime = settedCalender.getTime().getTime();
        if(doSave)
            sharedPreferences.edit().putString(START_TIME,String.format("%d/%d/%d",year,month,dayOfMonth)).apply();
    }
    @Override
    public void onClick(View v) {
        if(v.getId() != R.id.reset_memory)
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
                playSound(R.raw.paper_flip);
                break;
            case  R.id.reset_memory:
                if(((Button)v).getText().toString().contains("Are you sure ?")){
                    settingsAlert.dismiss();
                    sharedPreferences.edit().remove(START_TIME).apply();
                    Toast.makeText(this, "will be effect on next app launch", Toast.LENGTH_SHORT).show();
                    return;
                }
                v.setBackgroundResource(R.drawable.round_beauty_orange);
                ((Button)v).setText("Are you sure ? (yes)");
        }
    }
    public void unwrapGift(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.emblem_loading_indicator).setVisibility(View.VISIBLE);
                ((ViewGroup)findViewById(R.id.root)).removeViewAt(0);

                initOpenGL(1);
            }
        });

    }
    public void hideEmblemLoader(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.emblem_loading_indicator).setVisibility(View.GONE);
            }
        });
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
        playSound(R.raw.paper_flip);
    }

}
