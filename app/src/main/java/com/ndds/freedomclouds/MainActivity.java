package com.ndds.freedomclouds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public Handler quoteHandler =  null;
    private MediaPlayer emblemRotateSound;

    class FiddleHintAnimationListener extends AnimatorListenerAdapter {
        ObjectAnimator moveAnimator;
        View view;

        FiddleHintAnimationListener(View v) {
            view = v;
        }
        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            if (moveAnimator != null)
                moveAnimator.cancel();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            moveAnimator = ObjectAnimator.ofFloat(view, "translationX", -20   , 20);
            moveAnimator.setRepeatCount(4);
            moveAnimator.setDuration(500);
            moveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            moveAnimator.setRepeatMode(ValueAnimator.REVERSE);
            moveAnimator.start();
            moveAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ObjectAnimator.ofFloat(view, "alpha", 1, 0).start();
                }
            });
        }
    }

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


    public void pauseEmblemSound(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emblemRotateSound.pause();
            }
        });
    }
    public void playEmblemSound(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emblemRotateSound.start();
            }
        });

    }
    private void initSoundPool(){
        AudioAttributes attributes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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

    private void applyTypeFace(int[] ids) {
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/CherrySwash-Regular.ttf");
        for (int id: ids) {
            ((TextView)findViewById(id)).setTypeface(typeface);
        }
    }

    static final String START_TIME = "startTime";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0,0);
        setContentView(R.layout.activity_main);

        applyTypeFace(new int[] {
                R.id.emblemType,
                R.id.remaining_7_days_txt,
                R.id.remaining_30_days_txt,
                R.id.startDateTxt,
//                R.id.dayCount,
        });

        initSoundPool();
        emblemRotateSound = MediaPlayer.create(this,R.raw.rotation);

        emblemRotateSound.setLooping(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            emblemRotateSound.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        }else
            emblemRotateSound.setAudioStreamType(AudioManager.STREAM_MUSIC);

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
                }else {
                    initOpenGL(1);
                    generateRandomQuote();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.splash_overlay).startAnimation(slideUpAnimation);
        View v = findViewById(R.id.rotate_hint);
        rotateHintAnimator = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        rotateHintAnimator.addListener(new FiddleHintAnimationListener(v));

        idleHandler = new Handler();
        idleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!rotateHintAnimator.isStarted())
                    rotateHintAnimator.start();
            }
        },5000);
    }
    public void showCalender(){
        Calendar calendar =  Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Select a starting date");
        datePickerDialog.show();
    }
    public void glow(View v){
        openGLScreen.glow();
        playSound(R.raw.magical);
    }
    public void setEmblemTypeText(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.emblemType)).setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.normalEmblemTypeTextSize));
                ((TextView)findViewById(R.id.emblemType)).setText(text+" Face");
            }
        });
    }
    public void showSettings(View v){
        ViewGroup settingsView = (ViewGroup)getLayoutInflater().inflate(R.layout.settings,null);

        settingsView.findViewById(R.id.settings_change_date).setOnClickListener(this);
        settingsView.findViewById(R.id.settings_reset_today).setOnClickListener(this);
        settingsView.findViewById(R.id.settings_developer_notice).setOnClickListener(this);
        settingsView.findViewById(R.id.reset_memory).setOnClickListener(this);
        settingsView.findViewById(R.id.restart_app).setOnClickListener(this);
        settingsView.findViewById(R.id.settings_dismiss).setOnClickListener(this);
        TextView privacyNotice = settingsView.findViewById(R.id.privacy_policy);
        privacyNotice.setText(new PrivacyPolicyText(this));
        privacyNotice.setMovementMethod(LinkMovementMethod.getInstance());

        settingsAlert = new AlertDialog.Builder(this, R.style.SettingsAlert).setView(settingsView).create();
        settingsAlert.show();
        playSound(R.raw.paper_flip);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        updateDate(year,month,dayOfMonth,true);
    }

    @SuppressLint("DefaultLocale")
    private void updateDate(int year, int month, int dayOfMonth,boolean doSave){
        Calendar editedCalender = Calendar.getInstance();
        editedCalender.set(year,month,dayOfMonth);
        Calendar currentCalender = Calendar.getInstance();
        int days = (int) ((currentCalender.getTime().getTime() - editedCalender.getTime().getTime()) / (1000 * 60 * 60 * 24));
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
        startTime = editedCalender.getTime().getTime();
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
                AssetManager assetManager = getAssets();
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View view = viewGroup.getChildAt(i);
                    if(view instanceof TextView && !(view instanceof Button))
                        ((TextView) view).setTypeface(Typeface.createFromAsset(assetManager,"fonts/CherrySwash-Regular.ttf"));
                }
                AlertDialog noteAlert = new AlertDialog.Builder(this).setView(viewGroup).create();
                viewGroup.findViewById(R.id.developer_note_ok).setOnClickListener(v1 -> noteAlert.dismiss());
                noteAlert.show();
                playSound(R.raw.paper_flip);
                break;
            case  R.id.reset_memory:
                if(((Button)v).getText().toString().contains("Are you sure ?")){
                    settingsAlert.dismiss();
                    sharedPreferences.edit().remove(START_TIME).apply();
                    Toast.makeText(this, "restarting the app", Toast.LENGTH_SHORT).show();
                    restartApp();
                    return;
                }
                v.setBackgroundResource(R.drawable.round_beauty_orange);
                ((Button)v).setText("Are you sure ? (yes)");
                break;
            case R.id.restart_app:
                restartApp();
                break;
            case  R.id.settings_dismiss:
                settingsAlert.dismiss();
        }
    }

    private void restartApp() {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        finish();
        startActivity(mainIntent);
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
    public void generateRandomQuote(){
        if(quoteHandler == null)
            quoteHandler = new Handler();
        else {
            quoteHandler.removeCallbacksAndMessages(null);
            quoteHandler = null;
        }
        quoteHandler.postDelayed(() -> runOnUiThread(() -> {
            ArrayList<String> quotesArrayList = new ArrayList<>();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("quotes.txt")));
                String quote = bufferedReader.readLine();
                while (quote != null){
                    quotesArrayList.add(quote);
                    quote = bufferedReader.readLine();
                }
                String[] quotes = quotesArrayList.toArray(new String[0]);
                String randomQuote = quotes[(int) (Math.random() * quotes.length)];

                ((TextView)findViewById(R.id.emblemType)).setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.quoteTextSize));
                ((TextView)findViewById(R.id.emblemType)).setText(randomQuote);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }),1500);
    }
    private void showInstruction(){
        ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.introduction,null);

        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(viewGroup).setCancelable(false).create();
        Bitmap originalImage = BitmapFactory.decodeResource(getResources(),R.drawable.introduction_background);
        ((ImageView)viewGroup.findViewById(R.id.instruction_bg)).setImageBitmap(Bitmap.createScaledBitmap(originalImage,originalImage.getWidth()/4,originalImage.getHeight()/4,false));
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
