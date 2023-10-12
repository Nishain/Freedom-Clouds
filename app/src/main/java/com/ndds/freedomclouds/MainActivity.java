package com.ndds.freedomclouds;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ndds.freedomclouds.common.Message;
import com.ndds.freedomclouds.notifications.NotificationHandler;
import com.ndds.freedomclouds.settings.PurchaseSettings;
import com.ndds.freedomclouds.settings.Settings;
import com.ndds.freedomclouds.settings.TextContentSettings;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements MainActivityCallback, DatePickerDialog.OnDateSetListener, DialogInterface.OnCancelListener {

    private OpenGLScreen openGLScreen;
    private Handler idleHandler;
    private ObjectAnimator rotateHintAnimator;
    SharedPreferences sharedPreferences;
    private BackgroundImage backgroundImage;
    public SoundClip audio;
    private Settings settings;
    public QuotesMaker quotesMaker;
    public AmbientLightResponder ambientLightResponder;
    private UpdateCheckManager updateCheckManager;
    private PasscodeShield passcodeShield;

    public void rebuildOpenGLScreen(boolean isDynamicDrawingEnabled) {
        ((ViewGroup)findViewById(R.id.emblemContainer)).removeViewAt(0);
        buildOpenGLScreen()
                .initOrnamentRenderer(isDynamicDrawingEnabled);
    }

    private OpenGLScreen buildOpenGLScreen() {
        findViewById(R.id.emblem_loading_indicator).setVisibility(View.VISIBLE);
        openGLScreen = new OpenGLScreen(this, sharedPreferences.getString(TextContentSettings.EVENT_DESCRIPTION, null));
        ((ViewGroup) findViewById(R.id.emblemContainer)).addView(openGLScreen, 0, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        openGLScreen.setIdleHandler(idleHandler, rotateHintAnimator);
        return openGLScreen;
    }

    private void checkForInAppUpdates() {
        updateCheckManager.checkUpdate();
    }

    private void setupIssuePrompt() {
        View issuePrompt = findViewById(R.id.issuePrompt);
        issuePrompt.setVisibility(View.GONE);
        if (NotificationHandler.didBootReceiverFailed(sharedPreferences, this)) {
            issuePrompt.setVisibility(View.VISIBLE);
            issuePrompt.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Issue in notifications")
                        .setCancelable(false)
                        .setMessage(R.string.bootReceiverFailNotice)
                        .setPositiveButton("Go to settings", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
                        .show();
                    issuePrompt.setVisibility(View.GONE);
            });
        }
    }

    private void onRelease(boolean isFinishing) {
        if(isFinishing) {
            audio.release();
        } else {
            audio.pauseAll();
        }
        if (ambientLightResponder != null) ambientLightResponder.release();
        updateCheckManager.pauseUIUpdate();
    }


    @Override
    protected void onDestroy() {
        onRelease(true);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        onRelease(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        audio.resumeAll();
        passcodeShield.show();
        updateCheckManager.resumeUIUpdate();
        if (ambientLightResponder != null) {
            ambientLightResponder.resumeSensor();
        }
        super.onResume();
    }

    private void applyTypeFace(int[] ids) {
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/CherrySwash-Regular.ttf");
        for (int id: ids) {
            ((TextView)findViewById(id)).setTypeface(typeface);
        }
    }

    public static final String START_TIME = "startTime";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        passcodeShield = new PasscodeShield(this, sharedPreferences);
        if(sharedPreferences.contains(START_TIME)){
            Calendar date = getDateComponents(sharedPreferences);
            showDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
        }
        quotesMaker = new QuotesMaker(this);
        new CurtainAnimation(this, sharedPreferences).showSplashScreen(passcodeShield);

        View v = findViewById(R.id.rotate_hint);
        rotateHintAnimator = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        rotateHintAnimator.addListener(new HintAnimationHandler(v));

        idleHandler = new Handler();
        idleHandler.postDelayed(() -> {
            if(!rotateHintAnimator.isStarted())
                rotateHintAnimator.start();
        },5000);
        updateCheckManager = findViewById(R.id.updateCheckManager);
        updateCheckManager.init(this);
        setupIssuePrompt();
    }

    public static Calendar getDateComponents(SharedPreferences sharedPreferences) {
        String[] dateComponents = sharedPreferences.getString(START_TIME, "").split("/");
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                Integer.parseInt(dateComponents[0]),
                Integer.parseInt(dateComponents[1]),
                Integer.parseInt(dateComponents[2])
        );
        return calendar;
    }

    public void showCalender(){
        Calendar calendar =  Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setOnCancelListener(this);
        datePickerDialog.setTitle("Select a starting date");
        datePickerDialog.show();
    }

    public void glow(View v){
        if (openGLScreen.isGiftRenderer()) return;
        openGLScreen.glow();
        audio.playSound(R.raw.magical);
    }

    public void setEmblemTypeText(String text){
        runOnUiThread(() -> {
            ((TextView)findViewById(R.id.emblemType)).setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.normalEmblemTypeTextSize));
            ((TextView)findViewById(R.id.emblemType)).setText(text+" Face");
        });
    }

    public void showSettings(View v){
        settings.show();
    }

    @SuppressLint("DefaultLocale")
    public Boolean showDate(int year, int month, int dayOfMonth) {
        Calendar startedDate = Calendar.getInstance();
        startedDate.set(year,month,dayOfMonth);
        int days = (int) ((Calendar.getInstance().getTimeInMillis() - startedDate.getTimeInMillis()) / (24 * 60 * 60 * 1000));
        if(days < 0){
            Message.show(this, "Can only enter a date on or before today's date");
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

    @Override
    public void onAmbientLightResponsivenessChanged(boolean isResponsive) {
        ambientLightResponder.setAmbientResponsiveness(isResponsive);
    }

    @Override
    public void onAmbientBrightnessChanged(double brightnessFactor) {
        openGLScreen.setBrightnessFactor(brightnessFactor);
        backgroundImage.changeBrightness(brightnessFactor);
    }

    @Override
    public void setTaskAfterSecurityUnlock(PasscodeShield.TaskAfterUnlock task) {
        passcodeShield.runTaskAfterUnlock(task);
    }

    @Override
    public void shouldDrawDynamicEmblem(boolean enable) {
        rebuildOpenGLScreen(enable);
    }

    @Override
    public void onUpdateEventTitle(String title) {
        openGLScreen.setBackTitle(title);
    }

    public void unwrapGift(){
        runOnUiThread(() -> {
            audio.pauseEmblemSound();
            rebuildOpenGLScreen(false);
        });
    }

    public void hideEmblemLoader(){
        runOnUiThread(() -> findViewById(R.id.emblem_loading_indicator).setVisibility(View.GONE));
    }

    private void showInstruction(){
        ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.introduction,null);

        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(viewGroup).setCancelable(false).create();
        Bitmap originalImage = BitmapFactory.decodeResource(getResources(),R.drawable.introduction_background);
        ((ImageView)viewGroup.findViewById(R.id.instruction_bg)).setImageBitmap(Bitmap.createScaledBitmap(originalImage,originalImage.getWidth()/4,originalImage.getHeight()/4,false));
        viewGroup.findViewById(R.id.introduction_ok).setOnClickListener(v -> {
            alertDialog.dismiss();
            showCalender();
        });
        alertDialog.show();
        audio.playSound(R.raw.paper_flip);
    }

    public void showDeveloperNote() {
        View view = findViewById(R.id.developerNoteOverlay);
        view.setPadding(
                (int) (getWindow().getDecorView().getWidth() * 0.25f),
                0,
                0,
                0
        );
        view.findViewById(R.id.closeDeveloperNote).setOnClickListener((View v) -> {
            openGLScreen.playShiftAnimation(false, view);
        });
        openGLScreen.playShiftAnimation(true, view);
    }

    @Override
    public void openCurtain(Boolean isNewUser) {
        if(isNewUser) {
            ((TextView) findViewById(R.id.emblemType)).setText("The Gift");
            findViewById(R.id.settings).setEnabled(false);
            buildOpenGLScreen().initGiftRenderer();
            showInstruction();
        } else {
            boolean shouldEnableDynamicArts = sharedPreferences.getBoolean(PurchaseManager.PURCHASE_STATE, false)
                    && sharedPreferences.getBoolean(PurchaseSettings.DYNAMIC_DRAWING_ENABLED, false);
            buildOpenGLScreen().initOrnamentRenderer(shouldEnableDynamicArts);
            checkForInAppUpdates();
            quotesMaker.generateRandomQuote();
        }
        backgroundImage = findViewById(R.id.backgroundHueController);
        backgroundImage.init(this);
        ambientLightResponder = new AmbientLightResponder(this, sharedPreferences);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        updateDate(year, month, dayOfMonth);
        findViewById(R.id.settings).setEnabled(true);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        showInstruction();
    }
}
