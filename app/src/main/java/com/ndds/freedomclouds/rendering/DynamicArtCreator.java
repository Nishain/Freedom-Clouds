package com.ndds.freedomclouds.rendering;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Random;

public class DynamicArtCreator {

    private final static String[] ColorThemes = new String[] {
            // first color - background color
            // second color - outline colors rest are content colors
            // color theme correspond to OrnamentRenderer.ORDER
            "#ffd966,#853b0b,#be9002,#c65911,#7f6000,#f4b183",
            "#01de64,#385723,#feff00,#a9d18e,#e2f0d9,#fee699",
            "#ff9c9b,#c00000,#86a7fa,#fee699,#ff5050,#ff9900,#ffd966,#f10373,#f78253",
            "#04aff0,#203864,#6476fc,#86a7fa,#003399,#deebf7,#8faadc",
            "#ffffff,#595959,#86a7fa,#bfbfbf,#8f84fc,#7c7c7c,#deebf7,#adb9ca,#ffd966",
            "#df8af6,#7030a0,#2dff8b,#9900ff,#ff3399,#04aff0,#86a7fa,#cc02ff,#b4c7e7,#cc99ff",
            "#ff9933,#a40221,#feff00,#ff0200,#ff6600,#fee699,#ff7c80,#ffcc01,#c00000"

    };

    private final int width, height;

    public DynamicArtCreator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Bitmap[] getBackgroundCovers() {
        Paint paint = new Paint();
        Paint paint2 = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.BLACK);

        Bitmap[] covers = new Bitmap[ColorThemes.length];
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        int radius = height / 2;
        for (int i = 0; i < ColorThemes.length; i++) {
            int backgroundColor = Color.parseColor(ColorThemes[i].split(",")[0]);
            paint2.setColor(backgroundColor);
            canvas.drawCircle(radius, radius, radius, paint);
            canvas.drawCircle(radius, radius, radius - 5, paint2);
            covers[i] = image.copy(Bitmap.Config.ARGB_8888, false);
        }
        return covers;
    }

    private void drawShape(Canvas canvas, float width, Random random, Paint paint, Paint paint2, int fillColor) {
        Path path = new Path();
        Path path2 = new Path();
        float[] currentCoordinates = new float[] { 0 , 0 };
        final float radius = width / 2;
        do {
            currentCoordinates[0] = random.nextFloat() * width;
            currentCoordinates[1] = random.nextFloat() * width;
        } while (Math.sqrt(Math.pow(currentCoordinates[0] - radius, 2) + Math.pow(currentCoordinates[1] - radius, 2)) > radius);


        path.moveTo(currentCoordinates[0], currentCoordinates[1]);
        path2.moveTo(width - currentCoordinates[0], currentCoordinates[1]);
        for (int i = 0; i < 10; i++) {
            float[] coordinate = getRadialCoordinate2(random, width, currentCoordinates);

            float[] coordinate2 = getRadialCoordinate2(random, width, currentCoordinates);
            path.quadTo(coordinate[0], coordinate[1], coordinate2[0], coordinate2[1]);
            path2.quadTo(width - coordinate[0], coordinate[1], width - coordinate2[0], coordinate2[1]);
        }
        path.close();
        path2.close();
        path.setFillType(Path.FillType.WINDING);
        path2.setFillType(Path.FillType.WINDING);
        paint2.setColor(fillColor);
        canvas.drawPath(path2, paint);
        canvas.drawPath(path, paint);
        canvas.drawPath(path2, paint2);
        canvas.drawPath(path, paint2);
    }

    private boolean doColorsSimilarToEachOther(int[] colors) {
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < i; j++) {
                if (colors[i] == colors[j]) return true;
            }
        }
        return false;
    }

    public Bitmap createPoster() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Bitmap[] emblemImages = new Bitmap[7];
        int emblemSize = bitmap.getWidth() / 3;
        DynamicArtCreator artCreator = new DynamicArtCreator(500, 500);
        int angle = 300;

        for (int i = 0; i < emblemImages.length ; i++) {
            emblemImages[i] = Bitmap.createScaledBitmap(artCreator.drawRandomEmblem(i, 10), emblemSize, emblemSize, true);
            Matrix rotateMatrix = new Matrix();
            float radius = emblemSize * 0.75f;
            rotateMatrix.setTranslate(
                    (float) (((bitmap.getWidth() - emblemSize) / 2) - (Math.cos(Math.toRadians(angle)) * radius)),
                    (float) (radius - (Math.sin(Math.toRadians(angle)) * radius))
            );
            angle += 40;
            canvas.drawBitmap(emblemImages[i], rotateMatrix, null);
        }
        return bitmap;
    }

    private float[] getRadialCoordinate2(Random r, float size, float[] currentCoordinates) {
        float newCoordinateX, newCoordinateY;

        double distanceFromCenter;
        final float FACTOR = 0.3f;
        final float radius = size / 2;
        do {
            float ranVal = r.nextFloat();
            if (ranVal < 0.1) ranVal = 0.1f;
            float diff = (ranVal * size * FACTOR * 2) - (size * FACTOR);
            newCoordinateX =  currentCoordinates[0] + diff;
            ranVal = r.nextFloat();
            if (ranVal < 0.1) ranVal = 0.1f;
            diff = (ranVal * size * FACTOR * 2) - (size * FACTOR);
            newCoordinateY =  currentCoordinates[1] + diff;
            distanceFromCenter = Math.sqrt(Math.pow(radius - newCoordinateX, 2) + Math.pow(radius - newCoordinateY, 2));
        } while (distanceFromCenter >= (radius - (0.1f * size)));
        currentCoordinates[0] = newCoordinateX;
        currentCoordinates[1] = newCoordinateY;
        return new float[] { newCoordinateX, newCoordinateY };
    }

    public Bitmap drawRandomEmblem(int emblemIndex, float strokeWidth) {
        Paint paint = new Paint(), paint2 = new Paint();
        Bitmap resultantImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultantImage);
        Random random = new Random();
        String[] hexColors = ColorThemes[emblemIndex].split(",");

        int[] colorField = new int[hexColors.length];
        for (int i = 0; i < hexColors.length; i++) {
            colorField[i] = Color.parseColor(hexColors[i]);
        }

        int radius = height / 2;
        paint.setAntiAlias(true);
        paint2.setAntiAlias(true);
        paint.setColor(colorField[0]);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(radius, radius, radius - 5, paint);
        paint.setColor(colorField[1]);

        paint.setStrokeWidth(strokeWidth);
        paint2.set(paint);
        paint2.setStyle(Paint.Style.FILL);

        int[] randomColors;
        do {
            randomColors = new int[] {
                    colorField[2 + random.nextInt(colorField.length - 2)],
                    colorField[2 + random.nextInt(colorField.length - 2)],
                    colorField[2 + random.nextInt(colorField.length - 2)],
                    colorField[2 + random.nextInt(colorField.length - 2)]
            };
        } while (doColorsSimilarToEachOther(randomColors));

        drawShape(canvas, width, random, paint, paint2, randomColors[0]);
        drawShape(canvas, width, random, paint, paint2, randomColors[1]);
        drawShape(canvas, width, random, paint, paint2, randomColors[2]);
        drawShape(canvas, width, random, paint, paint2, randomColors[3]);

        return resultantImage;
    }
}
