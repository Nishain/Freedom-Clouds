package com.ndds.freedomclouds;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
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

public class MainActivity extends AppCompatActivity implements MainActivityCallback {

    private OpenGLScreen openGLScreen;
    private Handler idleHandler;
    private ObjectAnimator rotateHintAnimator;
    SharedPreferences sharedPreferences;
    private BackgroundImage backgroundImage;
    private boolean isBright = true;
    public SoundClip audio;
    private Settings settings;
    public QuotesMaker quotesMaker;

    private void initOpenGL(int tag){
        ViewGroup view = (ViewGroup)getLayoutInflater().inflate(R.layout.opengl_layout, null);

        (view.getChildAt(0)).setTag(String.valueOf(tag));
        ((ViewGroup)findViewById(R.id.root)).addView(view, 0);
        ArrayList<Bitmap> bitmaps;
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
        backgroundImage = findViewById(R.id.wood_image);
        setupLightSensor();
    }

    @Override
    public void finish() {
        audio.release();
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
                R.id.startDateTxt
        });

        findViewById(R.id.emblem_loading_indicator).setVisibility(View.GONE);
        sharedPreferences = getSharedPreferences("configuration",MODE_PRIVATE);
        settings = new Settings(this, sharedPreferences);
        audio = new SoundClip(this);
        if(sharedPreferences.contains(START_TIME)){
            String[] dateComponents = sharedPreferences.getString(START_TIME,"").split("/");
            int year = Integer.parseInt(dateComponents[0]);
            int month = Integer.parseInt(dateComponents[1]);
            int dayOfMonth = Integer.parseInt(dateComponents[2]);
            showDate(year, month, dayOfMonth);
        }
        quotesMaker = new QuotesMaker(this);
        new CurtainAnimation(this, sharedPreferences).startAnimate();

        View v = findViewById(R.id.rotate_hint);
        rotateHintAnimator = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        rotateHintAnimator.addListener(new HintAnimationHandler(v));

        idleHandler = new Handler();
        idleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!rotateHintAnimator.isStarted())
                    rotateHintAnimator.start();
            }
        },5000);
    }

    private void setupLightSensor() {
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Implement a listener to receive updates
        SensorEventListener listener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                boolean newIsBright = event.values[0] > 15;
                if (isBright != newIsBright) {
                    audio.playSound(R.raw.light_switch);
                    isBright = newIsBright;
                    openGLScreen.setBrightnessFactor(isBright ? 1 : 0.5f);
                    backgroundImage.switchLight(isBright);
                }
            }
        };

        // Register the listener with the light sensor -- choosing
        // one of the SensorManager.SENSOR_DELAY_* constants.
        mSensorManager.registerListener(
                listener, mLightSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void showCalender(){
        Calendar calendar =  Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,settings,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Select a starting date");
        datePickerDialog.show();
    }

    public void glow(View v){
        openGLScreen.glow();
        audio.playSound(R.raw.magical);
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
        settings.show();
    }

    @SuppressLint("DefaultLocale")
    public Boolean showDate(int year, int month, int dayOfMonth) {
        Calendar editedCalender = Calendar.getInstance();
        editedCalender.set(year,month,dayOfMonth);
        Calendar currentCalender = Calendar.getInstance();
        int days = (int) ((currentCalender.getTime().getTime() - editedCalender.getTime().getTime()) / (1000 * 60 * 60 * 24));
        if(days < 0){
            Toast.makeText(this, "Can only enter a date on or before today's date", Toast.LENGTH_LONG).show();
            return true;
        }
        ((ProgressBar) findViewById(R.id.weekDaysProgress)).setProgress(days % 7);
        ProgressBar daysInMonthProgress = findViewById(R.id.monthDaysProgress);
        daysInMonthProgress.setProgress(days % 30);
        //weeksCompletedCount
        ((TextView)findViewById(R.id.dayCount)).setText(String.format("%d Days Week %d", days,days / 7));
        ((TextView)findViewById(R.id.startDateTxt)).setText(String.format("Since %d/%d/%d",dayOfMonth,month+1,year));
        ((TextView)findViewById(R.id.remaining_7_days_txt)).setText(String.format("%s %s",getString(R.string.days_completed_in_week), days % 7+"/7"));
        ((TextView)findViewById(R.id.remaining_30_days_txt)).setText(String.format("%s %s",getString(R.string._30_days_progress), days % 30+"/30"));
        return false;
    }

    @SuppressLint("DefaultLocale")
    public void updateDate(int year, int month, int dayOfMonth){
        if(showDate(year, month, dayOfMonth)) return;
        sharedPreferences.edit().putString(START_TIME,String.format("%d/%d/%d",year,month,dayOfMonth)).apply();
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
        audio.playSound(R.raw.paper_flip);
    }

    @Override
    public void openCurtain(Boolean isNewUser) {
        if(isNewUser) {
            ((TextView) findViewById(R.id.emblemType)).setText("The Gift");
            initOpenGL(2);
            showInstruction();
        } else {
            initOpenGL(1);
            quotesMaker.generateRandomQuote();
        }
    }
}
