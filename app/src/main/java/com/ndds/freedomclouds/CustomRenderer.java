package com.ndds.freedomclouds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class CustomRenderer implements GLSurfaceView.Renderer {
    public volatile float mAngleX;
    public volatile float mAngleY;
    Context context;
    private Circle textureCircle2;
    private Circle outlineCircle1;
    private Circle outlineCircle2;
    private int program;
    private Cylinder cylinder;
    OpenGLScreen surfaceView;
    private Cylinder cylinder2;
    Bitmap[] emblemImages;
    int emblemCount;
    private String[] emblemTypes;

    public float getAngleX() {
        return mAngleX;
    }

    public float getAngleY() {
        return mAngleY;
    }

    CustomRenderer(Context context, OpenGLScreen surfaceView, ArrayList<Bitmap> bitmaps, String[] emblemTypes) {
        this.emblemTypes = emblemTypes;
        if (bitmaps != null) {
            emblemImages = bitmaps.toArray(new Bitmap[0]);
            emblemCount = emblemImages.length;
            textures = new int[emblemCount + 2];
        }
        this.surfaceView = surfaceView;
        this.context = context;
    }

    CustomRenderer(Context context, OpenGLScreen surfaceView) {
        this.surfaceView = surfaceView;
        this.context = context;
    }

    public void setAngleX(float angleX) {
        mAngleX = angleX;
    }

    public void setAngleY(float angleY) {
        mAngleY = angleY;
    }

    public static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 aTexPosition;" +
                    "varying vec2 vTexPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vTexPosition = aTexPosition;" +
                    "}";

    public final static String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "uniform sampler2D uTexture2;" +
                    "varying vec2 vTexPosition;" +
                    "uniform float textured;" +
                    "uniform float textureMix;" +
                    "uniform vec4 vColor;" +
//                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor * mix(vec4(1.0), mix(texture2D(uTexture, vTexPosition),texture2D(uTexture2, vTexPosition),textureMix), textured);" +//texture2D(uTexture, vTexPosition);,  vColor;
                    "}";

    private Circle textureCircle1;

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private int[] textures;

    private Bitmap flipImage(Bitmap bitmap) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    void wrapInsideOuterCircle(Bitmap image, Canvas canvas, Paint paint, Bitmap background) {
        int offset = (int) (background.getHeight() * 0.08f);
        Bitmap newImage = Bitmap.createScaledBitmap(image, background.getWidth() - (offset * 2), background.getHeight() - (offset * 2), false);
        canvas.drawBitmap(newImage, offset, offset, paint);
    }

    Bitmap getImage(int resourceCode) {
        Bitmap photo = BitmapFactory.decodeResource(context.getResources(), resourceCode);
        photo = flipImage(photo);
        return photo;
    }

    private void bindPicture(Bitmap picture, int index) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[index]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, picture, 0);
    }

    private float[] getRadialCoordinate(Random r,float _radius) {
        float radius = _radius * 0.95f;
        int trialX, trialY;
        double trialRadius;
        do {
            trialX = r.nextInt((int) radius);
            trialY = r.nextInt((int) radius) * (r.nextBoolean() ? -1 : 1);
            trialRadius = Math.sqrt((trialX ^ 2) * (trialY ^ 2));
        } while (trialRadius >= radius);
        return new float[] { trialX + _radius, trialY + _radius };
    }

    private boolean isLineIntercepting(float[][] lineA, float[][] lineB){
        double gradientA = (lineA[1][1] - lineA[0][1]) / (lineA[1][0] - lineA[0][0]);
        double gradientB = (lineB[1][1] - lineB[0][1]) / (lineB[1][0] - lineB[0][0]);


        double coefficient =  gradientA - gradientB;
        double constant = lineB[0][1] - lineA[0][1] - (gradientB * lineB[0][0]) + (gradientA * lineA[0][0]);
        if (coefficient == 0 ) return false; // parallel lines

        double interceptingX = constant / coefficient;
        return (Math.min(lineA[1][0], lineA[0][0]) < interceptingX &&
                Math.max(lineA[1][0], lineA[0][0]) > interceptingX
        ) && (Math.min(lineB[1][0], lineB[0][0]) < interceptingX &&
                Math.max(lineB[1][0], lineB[0][0]) > interceptingX
        );
    }

    static class DebugCircle {
        float startX, startY;
        DebugCircle(float[] data) {
            this.startX = data[0];
            this.startY = data[1];
        }

    }
    static class LineCoordinate {
        float[] startCoordinate, endCoordinate;
        boolean startConsumed = false, endConsumed = false;
        boolean debug = false;

        public LineCoordinate(float[] startCoordinate, float[] endCoordinate) {
            this.startCoordinate = startCoordinate;
            this.endCoordinate = endCoordinate;
        }

        public LineCoordinate consumeStart(){
            startConsumed = true;
            return this;
        }

        public LineCoordinate closeLine(){
            startConsumed = true;
            endConsumed = true;
            debug = true;
            return this;
        }

        boolean hasOpenNode() {
            return (!endConsumed) || (!startConsumed);
        }

        public float[] getOpenNode(){
            if(!endConsumed) return endCoordinate;
            if(!startConsumed) return  startCoordinate;
            return null;
        }

        public float[] getClosedNode() {
            if(startConsumed) return  startCoordinate;
            if(endConsumed) return endCoordinate;
            return null;
        }

        public int getOpenNodeIndex(){
            if(!startConsumed) return 0;
            if(!endConsumed) return 1;
            return -1;
        }

        public float[] getEdgeCoordinate(int index){
            return index == 0 ? startCoordinate : endCoordinate;
        }

        public void consumeEdgeAt(int index){
            if(index == 0)
                startConsumed = true;
            else
                endConsumed = true;
        }

        public float[][] get() {
            return new float[][] { startCoordinate, endCoordinate };
        }
    }
    private boolean isLineEdgeJoined(LineCoordinate a, LineCoordinate b) {
        return a.endCoordinate == b.endCoordinate || a.endCoordinate == b.startCoordinate
                || b.startCoordinate == a.startCoordinate;
    }
    private boolean checkIfInterceptingAnyLine(ArrayList<LineCoordinate> lineCollection, LineCoordinate testLine) {
        for(LineCoordinate line : lineCollection) {
             if(isLineIntercepting(testLine.get(), line.get())) return true;
        }
        return false;
    }
    private Bitmap drawRandomEmblem(int width,int height) {
        Paint paint = new Paint();
        Bitmap resultantImage = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultantImage);
        int radius = height / 2;
        Random r = new Random();
        canvas.drawColor(Color.RED);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        ArrayList<LineCoordinate> lineDB = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LineCoordinate proposedLine;

            if(i == 0) {
                proposedLine = new LineCoordinate(
                        getRadialCoordinate(r, radius),
                        getRadialCoordinate(r, radius)
                );
                lineDB.add(proposedLine);
            } else {
                float[] startingPoint,endPoint;
                int limit = 0;

                outerLoop:while(limit < 100){
                    limit++;
                    int randomPoint1 = r.nextInt(lineDB.size());
                    int randomAxis = r.nextInt(2);

                    startingPoint = lineDB.get(randomPoint1).getEdgeCoordinate(randomAxis);
                    endPoint = getRadialCoordinate(r,radius);
                    proposedLine = new LineCoordinate(startingPoint, endPoint).consumeStart();

                    if(checkIfInterceptingAnyLine(lineDB, proposedLine)) continue;

                    lineDB.add(proposedLine);
                    lineDB.get(randomPoint1).consumeEdgeAt(randomAxis);
                    break;
                }
            }
        }
        ArrayList<DebugCircle> debugCircles = new ArrayList<>();
        ArrayList<DebugCircle> coveredCircles = new ArrayList<>();
        ArrayList<LineCoordinate> closingLines = new ArrayList<>();

        for(LineCoordinate openLine : lineDB) {
            if(!openLine.hasOpenNode()) continue;
            ArrayList<LineCoordinate> shortList = new ArrayList<>();
            shortList.addAll(lineDB);
            shortList.addAll(closingLines);
            while (!shortList.isEmpty()) {
                LineCoordinate trialLine = shortList.get(r.nextInt(shortList.size()));
                if(openLine.startCoordinate.equals(trialLine.startCoordinate) || openLine.endCoordinate.equals(trialLine.endCoordinate)) {
                    shortList.remove(trialLine);
                    continue;
                }

                int edgeIndex = 0;
                LineCoordinate proposedLine = new LineCoordinate(openLine.getOpenNode(), trialLine.getEdgeCoordinate(edgeIndex)).closeLine();
                if(checkIfInterceptingAnyLine(shortList, proposedLine)) {
                    edgeIndex = 1;
                    proposedLine = new LineCoordinate(openLine.getOpenNode(), trialLine.getEdgeCoordinate(edgeIndex)).closeLine();
                    if(checkIfInterceptingAnyLine(shortList, proposedLine)) {
                        shortList.remove(trialLine);
                        continue;
                    }
                }
                openLine.consumeEdgeAt(edgeIndex);
                closingLines.add(proposedLine);
                break;
            }
        }
        lineDB.addAll(closingLines);


        for(LineCoordinate line : lineDB) {
                if(line.debug) paint.setColor(Color.YELLOW);
                else paint.setColor(Color.WHITE);
                canvas.drawLine(line.startCoordinate[0],line.startCoordinate[1],line.endCoordinate[0],line.endCoordinate[1], paint);
        }
        Paint debugPaint = new Paint();
        debugPaint.setStyle(Paint.Style.FILL);
        debugPaint.setColor(Color.GREEN);

        return resultantImage;
    }
    private void loadTextures() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glGenTextures(textures.length, textures, 0);

        Bitmap background = BitmapFactory.decodeResource(surfaceView.activity.getResources(), R.drawable.emblem_background);
        Paint paint = new Paint();
        Bitmap resultantMutatedImage = background.copy(background.getConfig(), true);
        Canvas canvas = new Canvas(resultantMutatedImage);
        Bitmap backSide = getImage(R.drawable.emblem_back);
        wrapInsideOuterCircle(getImage(R.drawable.emblem_glow), canvas, paint, background);

        bindPicture(backSide, 0);
        bindPicture(resultantMutatedImage, 1);

        for (int i = 0; i < emblemImages.length; i++) {
            wrapInsideOuterCircle(emblemImages[i], canvas, paint, background);
            bindPicture(flipImage(resultantMutatedImage), i + 2);
        }

        float outlineOffset = 0.0125f;
        program = GLES20.glCreateProgram();
        textureCircle1 = new Circle(-0.0625f, program);
        textureCircle2 = new Circle(0.0625f, program);
        cylinder = new Cylinder(0.0625f, program, .5f);
        outlineCircle1 = new Circle(-0.0625f - outlineOffset, program, .5f + outlineOffset);
        outlineCircle2 = new Circle(0.0625f + outlineOffset, program, .5f + outlineOffset);
        cylinder2 = new Cylinder(0.0625f + outlineOffset, program, .5f + outlineOffset);
        cylinder2.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        loadTextures();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        surfaceView.requestRender();
        surfaceView.activity.hideEmblemLoader();
    }

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] rotationMatrix = new float[16];
    private final float[] rotationMatrixY = new float[16];
    private final float[] outlineTranslator = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    float blendFactor = 0.0f;
    float brightnessFactor = 1f;
    int doGlow = 0;
    float quickSpinAngle = 0;
    int currentN = -1;

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        float[] scratch = new float[16];


        // Create a rotation transformation for the triangle
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(rotationMatrixY, 0);
        Matrix.setIdentityM(outlineTranslator, 0);
        if (quickSpinAngle > 0) {
            quickSpinAngle -= 10;
            mAngleX += 10;
            if (quickSpinAngle <= 0) {
                doGlow = -1;
            }
        }
        if (doGlow != 0) {
            if ((doGlow == 1 && blendFactor < 1.0f) || (doGlow == -1 && blendFactor > 0.0f))
                blendFactor += (doGlow * 0.05f);
            else {
                mAngleY = 0;
                quickSpinAngle = (360 * 3) - (mAngleX % 360);
                quickSpinAngle *= doGlow;

                doGlow = 0;
                if ((quickSpinAngle < 0) && !surfaceView.autoRotate) {
                    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    surfaceView.requestRender();
                }
            }
        }
        if (surfaceView.resetYMovement) {
            if(Math.abs(mAngleY) > 10) {
                if (mAngleY > 0)
                    mAngleY -= 10;
                else
                    mAngleY += 10;
            }else{
                mAngleY = 0;
                surfaceView.resetYMovement = false;
                if(!surfaceView.autoRotate){
                    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    surfaceView.requestRender();
                }
            }

        }
        if (surfaceView.autoRotate && quickSpinAngle < 1) {
            if ((Math.abs(mAngleX) % 360) > 10) {
                if (mAngleX > 0)
                    mAngleX -= 10;
                else
                    mAngleX += 10;
            } else {
                mAngleX = mAngleX - (mAngleX % 360);
                mAngleY = 0;
                surfaceView.autoRotate = false;
                if(!surfaceView.resetYMovement){
                    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    surfaceView.requestRender();
                }
            }
        }

        Matrix.rotateM(rotationMatrix, 0, mAngleX, 0, 1.0f, 0);
        Matrix.rotateM(rotationMatrixY, 0, mAngleY, 1.0f, 0, 0);
        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);
        Matrix.multiplyMM(scratch, 0, scratch, 0, rotationMatrixY, 0);
        int vPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);
        int n = Math.abs((int) (mAngleX / 360));
        int k = (int) ((Math.abs(mAngleX) + 90) / 360);
        if (currentN != k % emblemCount) {
            surfaceView.activity.setEmblemTypeText(emblemTypes[k % emblemCount]);
            currentN = k % emblemCount;
        }
        if (quickSpinAngle > 0 || doGlow != 0)
            textureCircle1.draw(textures[2 + Math.abs(n % emblemCount)], textures[1], blendFactor, brightnessFactor);
        else
            textureCircle1.draw(textures[2 + Math.abs(n % emblemCount)], textures[2 + Math.abs((n + 1) % emblemCount)], calculateTransitionFadeFactor(Math.abs(mAngleX) % 360), brightnessFactor);
        cylinder.draw(brightnessFactor);
        textureCircle2.draw(textures[0], -99, 0, brightnessFactor);
        Matrix.translateM(outlineTranslator, 0, 0, 0, 0.5f);
        Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        //Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);
        outlineCircle1.draw(brightnessFactor);
        cylinder2.draw(brightnessFactor);
        outlineCircle2.draw(brightnessFactor);

    }

    private float calculateTransitionFadeFactor(float angle) {
        return angle > 270 ? Math.min((angle - 270) / 90, 1.0f) : 0.0f;
    }
}
