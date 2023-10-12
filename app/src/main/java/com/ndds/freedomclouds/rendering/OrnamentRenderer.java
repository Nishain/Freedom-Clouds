package com.ndds.freedomclouds.rendering;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.View;

import com.ndds.freedomclouds.CenteredText;
import com.ndds.freedomclouds.OpenGLScreen;
import com.ndds.freedomclouds.R;

import java.io.IOException;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OrnamentRenderer implements GLSurfaceView.Renderer {
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
    private final boolean doGenerateDynamicArt;
    private boolean showingDefaultBackPlate = true;
    private static final float[] waveColor = getColor("#730517"),
            waveColorQuickRotate = getColor("#57f482");
    private Bitmap defaultBack, customBack;

    private static float[] getColor(String hexColor) {
        float[] color = new float[4];
        int intColor = Integer.parseInt(hexColor.substring(1), 16);
        color[3] = 1;
        color[0] = ((intColor) >> 16) / 255f;
        color[1] = ((intColor & 0xff00) >> 8) / 255f;
        color[2] = (intColor & 0xff) / 255f;
        return color;
    }

    public final static String[] ORDER = "Wood,Trees and Leaves,Flower,Water,Rock and Stones,Diamond,Fire"
            .split(",");
    private WaveDesign[] waveDesigns = new WaveDesign[4];
    private WaveDesign[] waveDesignsOutlines = new WaveDesign[4];

    public float getAngleX() {
        return mAngleX;
    }

    public float getAngleY() {
        return mAngleY;
    }

    public void updateBackPlate() {
        customBack = generateCustomWritingBitmap();
        if (surfaceView.backTitle == null) {
            showingDefaultBackPlate = true;
            bindPicture(defaultBack, 0);
        } else {
            showingDefaultBackPlate = false;
            bindPicture(customBack, 0);
        }
    }

    public OrnamentRenderer(Context context, OpenGLScreen surfaceView, boolean enableDynamicDrawing) {
        doGenerateDynamicArt = enableDynamicDrawing;
        if (!enableDynamicDrawing) {
            AssetManager assetManager = context.getResources().getAssets();
            try {
                String[] names = OrnamentRenderer.ORDER;
                emblemImages = new Bitmap[names.length];
                for (int i = 0; i < names.length; i++) {
                    emblemImages[i] = BitmapFactory.decodeStream(assetManager.open("borderless/" + names[i] + ".png"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        textures = new int[OrnamentRenderer.ORDER.length + 2];
        this.surfaceView = surfaceView;
        this.context = context;
    }

    OrnamentRenderer(Context context, OpenGLScreen surfaceView) {
        this.surfaceView = surfaceView;
        this.context = context;
        doGenerateDynamicArt = false;
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

    void wrapInsideOuterCircle(Bitmap image, Canvas canvas, Paint paint) {
        int offset = (int) (canvas.getHeight() * 0.08f);
        Bitmap newImage = Bitmap.createScaledBitmap(image, canvas.getWidth() - (offset * 2), canvas.getHeight() - (offset * 2), false);
        canvas.drawBitmap(newImage, offset, offset, paint);
    }

    Bitmap getImage(int resourceCode) {
        return BitmapFactory.decodeResource(context.getResources(), resourceCode);
    }

    private void bindPicture(Bitmap picture, int index) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[index]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, picture, 0);
    }

    public void shiftOrnament(boolean moveAway) {
        horizontalShiftDirection = moveAway ? -1 : 1;
    }

    public void setHorizontalShift(float value) {
        horizontalShift = value;
    }

    private Bitmap generateCustomWritingBitmap() {
        if (surfaceView.backTitle == null) return null;
        Bitmap background = BitmapFactory.decodeResource(surfaceView.activity.getResources(), R.drawable.emblem_background);
        Bitmap finalResult = background.copy(Bitmap.Config.ARGB_8888, true);
        Canvas backgroundCover = new Canvas(finalResult);
        Bitmap resultantMutatedImage = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(resultantMutatedImage);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        int backgroundColor = Color.parseColor("#ffd966");
        paint.setColor(backgroundColor);
        float radius = resultantMutatedImage.getHeight() / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(radius, radius, radius, paint);

        float sizeInPixel = surfaceView.activity.getResources().getDisplayMetrics().density * 60;
        new CenteredText(canvas, sizeInPixel, "#bc5611", backgroundColor)
                .drawText(surfaceView.backTitle);
        wrapInsideOuterCircle(resultantMutatedImage, backgroundCover, paint);
        return finalResult;
    }

    private void createDynamicImage(int index) {
        Bitmap background = BitmapFactory.decodeResource(surfaceView.activity.getResources(), R.drawable.emblem_background);
        Paint paint = new Paint();
        Bitmap resultantMutatedImage = background.copy(background.getConfig(), true);
        Canvas canvas = new Canvas(resultantMutatedImage);
        DynamicArtCreator artCreator = new DynamicArtCreator(canvas.getWidth(), canvas.getHeight());
        wrapInsideOuterCircle(artCreator.drawRandomEmblem(index - 2, 30), canvas, paint);
        bindPicture(resultantMutatedImage, index);
    }

    protected void loadTextures() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glGenTextures(textures.length, textures, 0);

        Bitmap background = BitmapFactory.decodeResource(surfaceView.activity.getResources(), R.drawable.emblem_background);
        Paint paint = new Paint();
        Bitmap resultantMutatedImage = background.copy(background.getConfig(), true);
        Canvas canvas = new Canvas(resultantMutatedImage);
        customBack = generateCustomWritingBitmap();
        defaultBack = getImage(R.drawable.emblem_back);
        bindPicture(defaultBack, 0);
        wrapInsideOuterCircle(getImage(R.drawable.emblem_glow), canvas, paint);
        bindPicture(resultantMutatedImage, 1);

        if (doGenerateDynamicArt) {
            emblemImages = new DynamicArtCreator(resultantMutatedImage.getWidth(), resultantMutatedImage.getHeight())
                    .getBackgroundCovers();
        }
        for (int i = 0; i < emblemImages.length; i++) {
            wrapInsideOuterCircle(emblemImages[i], canvas, paint);
            bindPicture(resultantMutatedImage, i + 2);
        }
        emblemCount = emblemImages.length;

        float outlineOffset = 0.0125f;
        program = GLES20.glCreateProgram();
        textureCircle1 = new Circle(-0.0625f, program);
        textureCircle2 = new Circle(0.0625f, program);
        waveDesigns = new WaveDesign[] {
                new WaveDesign(program,0.0625f, 30, 0),
                new WaveDesign(program, 0.0625f, 120, 0),
                new WaveDesign(program,0.0625f, 210, 0),
                new WaveDesign(program, 0.0625f, 300, 0)
        };

        waveDesignsOutlines = new WaveDesign[] {
                new WaveDesign(program,0.0625f, 30, outlineOffset),
                new WaveDesign(program, 0.0625f, 120, outlineOffset),
                new WaveDesign(program,0.0625f, 210, outlineOffset),
                new WaveDesign(program, 0.0625f, 300, outlineOffset)
        };
        cylinder = new Cylinder(0.0625f, program, .5f);
        cylinder.color = new float[]{ 1.0f, 0.64705882f, 0.0f, 1.0f };
        outlineCircle1 = new Circle(-0.0625f - outlineOffset, program, .5f + outlineOffset);
        outlineCircle2 = new Circle(0.0625f + outlineOffset, program, .5f + outlineOffset);
        cylinder2 = new Cylinder(0.0625f + outlineOffset, program, .5f + outlineOffset);

        int vertexShader = OrnamentRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                OrnamentRenderer.vertexShaderCode);
        int fragmentShader = OrnamentRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                OrnamentRenderer.fragmentShaderCode);

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program);
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
    private final float[] rotationMatrixX = new float[16];
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

    public float blendFactor = 0.0f;
    public double brightnessFactor = 1f;
    public int doGlow = 0;
    public int quickSpinAngle = 0;
    float horizontalShift = 0;
    int horizontalShiftDirection = 0;
    int currentN = -1;
    int previousRotationIndex = -1;

    private boolean getDirectionForClosestPolarity(float value) {
        float remainder = value % 360;
        if (value > 0)
            return remainder > 180;
        return remainder > -180;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -4f, -horizontalShift, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        float[] scratch = new float[16];


        // Create a rotation transformation for the triangle
        Matrix.setIdentityM(rotationMatrixX, 0);
        Matrix.setIdentityM(rotationMatrixY, 0);
        Matrix.setIdentityM(outlineTranslator, 0);
        if (quickSpinAngle > 0) {
            quickSpinAngle -= 10;
            mAngleX += 10;
            if (quickSpinAngle <= 0) {
                quickSpinAngle = 0;
                doGlow = -1;
            }
        }
        if (doGlow != 0) {
            if ((doGlow == 1 && blendFactor < 1.0f) || (doGlow == -1 && blendFactor > 0.0f))
                blendFactor += (doGlow * 0.05f);
            else {
                if (doGlow == 1) {
                    mAngleY = 0;
                    quickSpinAngle = (360 * 3) - (((int) mAngleX) % 360);
                } else {
                    if (Math.abs(mAngleX % 360) > 0) surfaceView.resetXAngle();
                    else {
                        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                        surfaceView.requestRender();
                    }
                }
                doGlow = 0;
            }
        }
        if (surfaceView.resetYMovement) {
            if(Math.abs(mAngleY) > 10) {
                if (mAngleY > 0)
                    mAngleY -= 10;
                else
                    mAngleY += 10;
            } else {
                mAngleY = 0;
                surfaceView.resetYMovement = false;
                if(!surfaceView.autoRotate){
                    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    surfaceView.requestRender();
                }
            }

        }
        if (surfaceView.autoRotate && quickSpinAngle == 0) {
            if ((Math.abs(mAngleX) % 360) > 10) {
                if (getDirectionForClosestPolarity(mAngleX))
                    mAngleX += 10;
                else mAngleX -= 10;
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
        int n = Math.abs((int) (mAngleX / 360));
        if (previousRotationIndex != n) {
            if (quickSpinAngle == 0) {
                previousRotationIndex = n;
                int index = 2 + Math.abs(n % emblemCount);
                if (surfaceView.backTitle != null && new Random().nextBoolean()) {
                    showingDefaultBackPlate = !showingDefaultBackPlate;
                    bindPicture(showingDefaultBackPlate ? defaultBack : customBack, 0);
                }
                if (doGenerateDynamicArt) createDynamicImage(index);
            }
        }
        Matrix.rotateM(rotationMatrixX, 0, mAngleX, 0, 1.0f, 0);
        Matrix.rotateM(rotationMatrixY, 0, mAngleY, 1.0f, 0, 0);
        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrixX, 0);
        Matrix.multiplyMM(scratch, 0, scratch, 0, rotationMatrixY, 0);
        int vPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);

        int k = (int) ((Math.abs(mAngleX) + 90) / 360);
        if (currentN != k % emblemCount) {
            surfaceView.activity.setEmblemTypeText(ORDER[k % emblemCount]);
            currentN = k % emblemCount;
        }
        if (quickSpinAngle > 0 || doGlow != 0)
            textureCircle1.draw(textures[2 + Math.abs(n % emblemCount)], textures[1], blendFactor, brightnessFactor);
        else
            textureCircle1.draw(textures[2 + Math.abs(n % emblemCount)], textures[2 + Math.abs((n + 1) % emblemCount)], calculateTransitionFadeFactor(Math.abs(mAngleX) % 360), brightnessFactor);
        cylinder.draw(brightnessFactor);
        textureCircle2.draw(textures[0], -99, 0, brightnessFactor);
        float[] calculatedWaveColor = new float[] {
                (waveColor[0] * (1 - blendFactor)) + (waveColorQuickRotate[0] * blendFactor),
                (waveColor[1] * (1 - blendFactor)) + (waveColorQuickRotate[1] * blendFactor),
                (waveColor[2] * (1 - blendFactor)) + (waveColorQuickRotate[2] * blendFactor),
                1
        };

        for (int i = 0; i < waveDesigns.length; i++) {
            waveDesigns[i].color = calculatedWaveColor;
            waveDesigns[i].drawWithShapeMutation((int) mAngleX, brightnessFactor);
        }
        Matrix.translateM(outlineTranslator, 0, 0, 0, 0.5f);
        Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        //Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);
        for (int i = 0; i < waveDesigns.length; i++) {
            waveDesignsOutlines[i].drawWithShapeMutation((int) mAngleX, brightnessFactor);
        }
        outlineCircle1.draw(brightnessFactor);
        cylinder2.draw(brightnessFactor);
        outlineCircle2.draw(brightnessFactor);
    }

    private float calculateTransitionFadeFactor(float angle) {
        return angle > 270 ? Math.min((angle - 270) / 90, 1.0f) : 0.0f;
    }
}
