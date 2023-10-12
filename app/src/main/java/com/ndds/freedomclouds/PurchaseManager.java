package com.ndds.freedomclouds;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.ndds.freedomclouds.common.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PurchaseManager {
    public interface OnLocalTextAvailable {
        void onPriceAvailable(String formattedPrice);
    }

    public interface OnPurchaseComplete {
        void onComplete();
    }

    private final MainActivity activity;
    private final SharedPreferences sharedPreferences;
    private OnPurchaseComplete onPurchaseComplete = null;
    public static final String PURCHASE_STATE = "purchaseState";
    private final BillingClient billingClient;

    private void acknowledgePaymentToGooglePlay(Purchase purchase) {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                billingClient.endConnection();
                return;
            }
            sharedPreferences.edit().putBoolean(PURCHASE_STATE, true).apply();
            billingClient.endConnection();
            if (onPurchaseComplete != null) {
                activity.runOnUiThread(() -> onPurchaseComplete.onComplete());
            }
        };

        AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    public PurchaseManager(MainActivity activity, SharedPreferences sharedPreferences) {
        this.activity = activity;

        final List<Integer> IGNORABLE_RESPONSES = Arrays.asList(
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
                BillingClient.BillingResponseCode.USER_CANCELED
        );

        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                Purchase purchase = purchases.get(0);
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    acknowledgePaymentToGooglePlay(purchase);
                    return;
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    return;
                }
            } else if (IGNORABLE_RESPONSES.contains(billingResult.getResponseCode())) {
                return;
            }
            showToast("Sorry something went wrong");
        };

        billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        this.sharedPreferences = sharedPreferences;
    }

    private void showToast(String message) {
        activity.runOnUiThread(() -> {
            Message.show(activity, message);
        });
    }

    private void getPurchaseInfo(ProductDetailsResponseListener listener) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult connectionStatus) {
                if (connectionStatus.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    billingClient.endConnection();
                    return;
                }
                ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
                productList.add(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("generated_arts")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build());
                QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build();
                billingClient.queryProductDetailsAsync(queryProductDetailsParams, listener);
            }
        });
    }

    public void getPriceString(OnLocalTextAvailable onLocalTextAvailable) {
        getPurchaseInfo((billingResult, list) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
            ProductDetails.OneTimePurchaseOfferDetails details = list.get(0).getOneTimePurchaseOfferDetails();
            if (details != null) activity.runOnUiThread(() -> {
                onLocalTextAvailable.onPriceAvailable(details.getFormattedPrice());
            });
            billingClient.endConnection();
        });
    }

    public void restorePurchaseStatus(OnPurchaseComplete onPurchaseDetected) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult connectionStatus) {
                if (connectionStatus.getResponseCode() != BillingClient.BillingResponseCode.OK)
                    return;
                billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(), (purchaseResponse, purchases) -> {
                    if (purchaseResponse.getResponseCode() != BillingClient.BillingResponseCode.OK)
                        return;
                    if (purchases.size() > 0) {
                        Purchase purchase = purchases.get(0);
                        boolean isPurchased = purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED;
                        sharedPreferences.edit().putBoolean(PURCHASE_STATE, isPurchased).apply();
                        if (isPurchased) {
                            if (!purchase.isAcknowledged()) acknowledgePaymentToGooglePlay(purchase);
                            else billingClient.endConnection();
                            activity.runOnUiThread(onPurchaseDetected::onComplete);
                            return;
                        }
                    }
                    showToast("Sorry, no purchase was made");
                });
            }
        });

    }

    public void promptPurchaseBill(OnPurchaseComplete onPurchaseComplete) {
        this.onPurchaseComplete = onPurchaseComplete;
        getPurchaseInfo((billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                ArrayList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                productDetailsParamsList.add(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(list.get(0))
                                .build()
                );
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();
                billingClient.launchBillingFlow(activity, billingFlowParams);
            }
        });
    }
}
