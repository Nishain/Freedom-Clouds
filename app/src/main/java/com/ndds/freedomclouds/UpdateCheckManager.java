package com.ndds.freedomclouds;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.ndds.freedomclouds.common.ActionButton;
import com.ndds.freedomclouds.common.InsetSafeLinearLayout;
import com.ndds.freedomclouds.common.SheetAlert;

public class UpdateCheckManager extends InsetSafeLinearLayout implements InstallStateUpdatedListener {
    private MainActivity mainActivity;
    private ActivityResultLauncher<IntentSenderRequest> activityLauncher;
    private AppUpdateManager appUpdateManager;
    private TextView progressText;
    private CircularProgressIndicator progressPercentage;
    private boolean isDownloading = false;

    public UpdateCheckManager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        appUpdateManager = AppUpdateManagerFactory.create(mainActivity);
        activityLauncher = mainActivity.registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        prepareProgressIndicators();
                        registerListener();
                    }
                });
        setVisibility(GONE);
    }

    private void registerListener() {
        isDownloading = true;
        appUpdateManager.registerListener(this);
    }

    private void unregisterListener() {
        isDownloading = false;
        appUpdateManager.unregisterListener(this);
    }

    private void prepareProgressIndicators() {
        setVisibility(VISIBLE);
        setVisibility(VISIBLE);
        progressText = findViewById(R.id.updateProgressText);
        progressPercentage = findViewById(R.id.updateProgressCircularIndicator);
        progressText.setText("0%");
        progressPercentage.setProgress(0);
    }

    public void pauseUIUpdate() {
        if (isDownloading) appUpdateManager.unregisterListener(this);
    }

    public  void resumeUIUpdate() {
        if (isDownloading) appUpdateManager.registerListener(this);
    }

    private void promptForUpdate(AppUpdateInfo appUpdateInfo, SheetAlert dialog) {
        dialog.dismiss();
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activityLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                        .setAllowAssetPackDeletion(true)
                        .build()
        );
    }

    private void alertToRestart() {
        ViewGroup dialogView = (ViewGroup) mainActivity.getLayoutInflater().inflate(R.layout.update_available_dialog, null);
        SheetAlert sheetAlert = new SheetAlert(dialogView, mainActivity, SheetAlert.Position.TOP);
        sheetAlert.show(false);

        Button dismiss = dialogView.findViewById(R.id.dismiss);
        Button confirm = dialogView.findViewById(R.id.confirm);
        ((TextView) dialogView.findViewById(R.id.updateDialogHeader))
                .setText("Update is ready! need to restart");
        confirm.setText("Restart");

        dismiss.setOnClickListener(v -> sheetAlert.dismiss());
        confirm.setOnClickListener(v -> {
            sheetAlert.dismiss();
            appUpdateManager.completeUpdate();
        });
    }

    public void checkUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADING) {
                prepareProgressIndicators();
                registerListener();
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                alertToRestart();
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                ViewGroup dialogView = (ViewGroup) mainActivity.getLayoutInflater().inflate(R.layout.update_available_dialog, null);
                SheetAlert sheetAlert = new SheetAlert(dialogView, mainActivity, SheetAlert.Position.TOP);

                dialogView.findViewById(R.id.dismiss).setOnClickListener(v -> sheetAlert.dismiss());
                dialogView.findViewById(R.id.confirm).setOnClickListener(v -> promptForUpdate(appUpdateInfo, sheetAlert));
                mainActivity.setTaskAfterSecurityUnlock(() -> {
                    sheetAlert.show(false);
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStateUpdate(@NonNull InstallState installState) {
        switch (installState.installStatus()) {
            case InstallStatus.DOWNLOADED:
                unregisterListener();
                alertToRestart();
                break;
            case InstallStatus.DOWNLOADING:
                int percentage = 0;
                if (installState.bytesDownloaded() > 0) {
                    percentage = (int) ((installState.bytesDownloaded() / (float) installState.totalBytesToDownload()) * 100);
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressPercentage.setProgress(percentage, true);
                    } else progressPercentage.setProgress(percentage);
                    progressText.setText(percentage + "%");
                } catch (Exception e) {
                    unregisterListener();
                }
                break;
            case InstallStatus.PENDING:
                break;
            default:
                setVisibility(GONE);
                unregisterListener();
        }
    }
}
