package com.ndds.freedomclouds;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GiftRenderer extends CustomRenderer implements GLSurfaceView.Renderer {
    public volatile float mAngle;
    Context context;
    private Circle mSquare2;
    private Circle mSquare1Outline;
    private Circle mSquare2Outline;
    private int program;
    private Cylinder cylinder;
    OpenGLScreen surfaceView;
    private Cylinder cylinder2;

    public float getAngleX() {
        return mAngle;
    }

    GiftRenderer(Context context, OpenGLScreen surfaceView) {
        super(context, surfaceView);
        this.surfaceView = surfaceView;
        this.context = context;
    }

    public void setAngleX(float angle) {
        mAngle = angle;
    }

    private Circle mSquare;


    private int textures[] = new int[2];

    private void bindPicture(Bitmap picture, int index) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[index]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, picture, 0);
    }

    private void loadTextures() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glGenTextures(2, textures, 0);

        Bitmap emblem1 = getImage(R.drawable.present_front);

        Bitmap emblemBack = getImage(R.drawable.present_back);

        bindPicture(emblemBack, 0);
        bindPicture(emblem1, 1);

        float outlineOffset = 0.0125f;
        program = GLES20.glCreateProgram();
        mSquare = new Circle(-0.0625f, program);
        mSquare2 = new Circle(0.0625f, program);
        cylinder = new Cylinder(0.0625f, program, .5f);
        cylinder.color = new float[]{1.0f, 0.0f, 0.0f, 1.0f};
        mSquare1Outline = new Circle(-0.0625f - outlineOffset, program, .5f + outlineOffset);
        mSquare2Outline = new Circle(0.0625f + outlineOffset, program, .5f + outlineOffset);
        cylinder2 = new Cylinder(0.0625f + outlineOffset, program, .5f + outlineOffset);
        cylinder2.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);


    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        loadTextures();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        surfaceView.requestRender();
    }

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    private float[] outlineTranslator = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    boolean isUnwrap = false;

    @Override
    public void onDrawFrame(GL10 gl) {
//        GLES10.glClearDepth(1.0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        float[] scratch = new float[16];
        if (surfaceView.autoRotate) {
            if ((Math.abs(mAngle) % (360 * 3)) > 5) {
                if (mAngle > 0)
                    mAngle -= 5;
                else
                    mAngle += 5;
            } else {
                mAngle = mAngle - (Math.abs(mAngle) % 360);
                surfaceView.autoRotate = false;
                surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                surfaceView.requestRender();
            }

        }

        // Create a rotation transformation for the triangle
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(outlineTranslator, 0);

        Matrix.rotateM(rotationMatrix, 0, mAngle, 0, 1.0f, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);
        int vPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);
        cylinder.draw(scratch);
        mSquare.draw(scratch, textures[1], -99, 0);
        mSquare2.draw(scratch, textures[0], -99, 0);
        Matrix.translateM(outlineTranslator, 0, 0, 0, 0.5f);
        Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        //Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);
        mSquare1Outline.draw(scratch);
        cylinder2.draw(scratch);
        mSquare2Outline.draw(scratch);
        if (Math.abs(mAngle) > 715 && !isUnwrap) {
            isUnwrap = true;
            surfaceView.activity.unwrapGift();

        }

    }

}
