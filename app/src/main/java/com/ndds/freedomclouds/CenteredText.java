package com.ndds.freedomclouds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;

public class CenteredText {
    private final Canvas canvas;
    private final float textSize;
    private final Paint paint;
    private final float radius;
    private final int backgroundColor, textColor;

    public CenteredText(Canvas canvas, float textSize, String textColorString, int backgroundColor) {
        this.backgroundColor = backgroundColor;
        textColor = Color.parseColor(textColorString);
        this.canvas = canvas;
        radius = canvas.getHeight() / 2f;
        this.textSize = textSize;
        paint = new Paint();
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(textColor);
    }

    public void drawText(String text) {
        int prevLineCount;
        int newLineCount = calculateContent(text, canvas.getHeight() * 0.3f + textSize).length;
        Row[] result;
        do  {
            prevLineCount = newLineCount;
            result = calculateContent(text, (radius - (prevLineCount * textSize / 2f) + textSize));
            newLineCount = result.length;
        } while (newLineCount > prevLineCount);

        float startY = radius - ((textSize * result.length) / 2f) + textSize;
        ArrayList<float[]> decorationPositions = new ArrayList<>();
        for (int i = 0; i < result.length; i++) {
            float remainingSpace =  result[i].availableSpace - (paint.measureText(result[i].text) / 2);
            if (remainingSpace > (radius * 0.07f)) {
                decorationPositions.add(new float[] {
                        radius - result[i].availableSpace,
                        startY + ((i - 0.5f) * textSize)
                });
            }
            canvas.drawText(result[i].text,  radius - (paint.measureText(result[i].text) / 2), startY + (i * textSize), paint);
        }
        if (decorationPositions.size() > 0) {
            float sweepAngle = 40;
            float decorationCenterY = decorationPositions.get(decorationPositions.size() / 2)[1];
            float decorationCenterX = decorationPositions.get(decorationPositions.size() / 2)[0];
            float startAngle = 180 - (float) Math.toDegrees(Math.asin((decorationCenterY - radius) / radius)) -(sweepAngle / 2);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(30);
            int offsetFromEdge = (int) (radius * 0.1f);
            canvas.drawArc(
                    new RectF(offsetFromEdge, offsetFromEdge ,canvas.getHeight() - offsetFromEdge ,canvas.getHeight() - offsetFromEdge),
                    startAngle,
                    sweepAngle,
                    false,
                    paint
            );

            float outerCircleRadius = offsetFromEdge * 0.75f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(backgroundColor);
            canvas.drawCircle(decorationCenterX, decorationCenterY, outerCircleRadius, paint);
            canvas.drawCircle(canvas.getHeight() - decorationCenterX, decorationCenterY, outerCircleRadius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(textColor);
            canvas.drawCircle(decorationCenterX, decorationCenterY, outerCircleRadius, paint);
            canvas.drawCircle(canvas.getHeight() - decorationCenterX, decorationCenterY, outerCircleRadius, paint);
            paint.setStyle(Paint.Style.FILL);

            // small inner circle
            canvas.drawCircle(decorationCenterX, decorationCenterY, outerCircleRadius / 2f, paint);
            canvas.drawCircle(canvas.getHeight() - decorationCenterX, decorationCenterY, outerCircleRadius / 2f, paint);
        }
    }

    private int getPossibleWholeWordTextLength(String text, float maxLength, Paint paint) {
        char[] sequence = text.toCharArray();
        int markedIndex = 0;
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == ' ') {
                if (paint.measureText(text, 0, i) > maxLength) return markedIndex;
                markedIndex = i;
            }
        }
        return paint.measureText(text) > maxLength ? markedIndex : sequence.length;
    }

    private static class Row {
        final String text;
        final float availableSpace;

        private Row(String text, float availableSpace) {
            this.text = text;
            this.availableSpace = availableSpace;
        }
    }

    private Row[] calculateContent(String text, float YLevel) {
        ArrayList<Row> data = new ArrayList<>();
        int textCovered = 0;
        do {
            int startTextIndex = textCovered;
            float maxTextSpanRadius = (float) (Math.sqrt(Math.pow(radius, 2) - Math.pow(radius - YLevel, 2))) * 0.9f;
            String remainingText = text.substring(startTextIndex);
            int numOfCharacters = getPossibleWholeWordTextLength(remainingText, maxTextSpanRadius * 2, paint);
            if (numOfCharacters == 0) break;
            data.add(new Row(remainingText.substring(0, numOfCharacters), maxTextSpanRadius));
            textCovered += numOfCharacters;
            YLevel += textSize;
        } while (textCovered < text.length());

        return data.toArray(new Row[0]);
    }
}
