package com.ndds.freedomclouds.settings;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.ndds.freedomclouds.PurchaseManager;
import com.ndds.freedomclouds.R;
import com.ndds.freedomclouds.common.ActionButton;
import com.ndds.freedomclouds.common.Message;
import com.ndds.freedomclouds.rendering.DynamicArtCreator;

public class PurchaseSettings extends SettingsPage {
    private boolean isDynamicDrawingEnabled = false;
    public final static String DYNAMIC_DRAWING_ENABLED = "dynamicDrawingEnabled";
    public PurchaseSettings(Settings.Bundle bundle) {
        super(bundle.activity, bundle.sharedPreferences, R.layout.settings_purchase, "Specials", bundle.dialog);
    }

    private PurchaseManager purchaseManager() {
        return new PurchaseManager(activity, sharedPreferences);
    }

    private void handlePurchase(ActionButton purchaseButton) {
        purchaseManager().promptPurchaseBill(() -> {
            Message.show(activity, "Thanks for the purchase!");
            preparePaidFeatures();
        });
    }

    private void preparePaidFeatures() {
        setListener(R.id.purchaseBtn, this::toggleDynamicDrawing);
        toggleDynamicDrawing(settingsView.findViewById(R.id.purchaseBtn));
        settingsView.findViewById(R.id.purchaseRestoreSection).setVisibility(View.GONE);
    }

    private void restorePurchase() {
        purchaseManager().restorePurchaseStatus(() -> {
            Message.show(activity, "Purchase restored");
            preparePaidFeatures();
            dialog.dismiss();
        });
    }

    private void toggleDynamicDrawing(ActionButton button) {
        isDynamicDrawingEnabled = !isDynamicDrawingEnabled;
        updateButtonState();
        activity.shouldDrawDynamicEmblem(isDynamicDrawingEnabled);
        sharedPreferences.edit().putBoolean(DYNAMIC_DRAWING_ENABLED, isDynamicDrawingEnabled).apply();
    }

    private void updateButtonState() {
        ActionButton button = settingsView.findViewById(R.id.purchaseBtn);
        button.setText(String.format("%s dynamic arts", isDynamicDrawingEnabled ? "Disable" : "Enable"));
        button.setColorTheme(isDynamicDrawingEnabled ? R.color.beautyOrange : R.color.beautyGreen);
    }

    private void preparePoster() {
        ImageView poster = settingsView.findViewById(R.id.purchasePosterImg);
        poster.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            Bitmap posterImage = new DynamicArtCreator(poster.getWidth(), poster.getHeight())
                    .createPoster();
            poster.setImageBitmap(posterImage);
        });
    }

    @Override
    void onCreate(ViewGroup settingsView) {
        preparePoster();
        boolean isPurchased = sharedPreferences.getBoolean(PurchaseManager.PURCHASE_STATE, false);
        if (isPurchased) {
            setListener(R.id.purchaseBtn, this::toggleDynamicDrawing);
            settingsView.findViewById(R.id.purchaseRestoreSection).setVisibility(View.GONE);
            isDynamicDrawingEnabled = sharedPreferences.getBoolean(DYNAMIC_DRAWING_ENABLED, false);
            updateButtonState();
        } else {
            Button purchaseButton = settingsView.findViewById(R.id.purchaseBtn);
            purchaseButton.setEnabled(false);
            purchaseManager().getPriceString(formattedPrice -> {
                purchaseButton.setText(formattedPrice);
                purchaseButton.setEnabled(true);
            });
            setListener(R.id.purchaseBtn, this::handlePurchase);
            setListener(R.id.restorePurchaseBtn, this::restorePurchase);
        }
    }
}
