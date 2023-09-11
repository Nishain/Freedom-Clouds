package com.ndds.freedomclouds;

import android.app.Activity;
import android.os.Handler;
import android.util.TypedValue;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QuotesMaker {
    Activity activity;
    QuotesMaker(Activity activity) {
        this.activity = activity;
    }

    private Handler quoteHandler;

    public void removeQuoteTimer() {
        if(quoteHandler != null) {
            quoteHandler.removeCallbacksAndMessages(null);
            quoteHandler = null;
        }
    }

    public void generateRandomQuote(){
        if (quoteHandler != null){
            quoteHandler.removeCallbacksAndMessages(null);
        }
        quoteHandler = new Handler();
        quoteHandler.postDelayed(() -> {
            ArrayList<String> quotesArrayList = new ArrayList<>();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(activity.getAssets().open("quotes.txt")));
                String quote = bufferedReader.readLine();
                while (quote != null){
                    quotesArrayList.add(quote);
                    quote = bufferedReader.readLine();
                }
                String[] quotes = quotesArrayList.toArray(new String[0]);
                String randomQuote = quotes[(int) (Math.random() * quotes.length)];

                ((TextView) activity.findViewById(R.id.emblemType)).setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.getResources().getDimensionPixelSize(R.dimen.quoteTextSize));
                ((TextView) activity.findViewById(R.id.emblemType)).setText(randomQuote);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 1500);
    }
}
