package com.ndds.freedomclouds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class CustomRenderer implements GLSurfaceView.Renderer {
    public volatile float mAngle;
    Context context;
    private Circle textureCircle2;
    private Circle outlineCircle1;
    private Circle outlineCircle2;
    private int program;
    private Cylinder cylinder;
    OpenGLScreen surfaceView;
    private Cylinder cylinder2;

    public float getAngle() {
        return mAngle;
    }
    CustomRenderer(Context context,OpenGLScreen surfaceView){
        this.surfaceView = surfaceView;
        this.context = context;
    }

    public void setAngle(float angle) {
        mAngle = angle;
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
    public final static String vertexColorShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    public final static String fragmentColorShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private Circle textureCircle1;

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    private int textures[] = new int[7];
    private Bitmap flipImage(Bitmap bitmap){
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    Bitmap getImage(int resourceCode){
        Bitmap photo = BitmapFactory.decodeResource(context.getResources(),resourceCode);
        photo = flipImage(photo);
        return photo;
    }

    private void bindPicture(Bitmap picture, int index){
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[index]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, picture, 0);
    }
    private void loadTextures(){
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glGenTextures(6, textures, 0);

        Bitmap emblem1 = getImage(R.drawable.emblem_plain);
        Bitmap emblem2 = getImage(R.drawable.emblem_plain2);
        Bitmap emblem3 = getImage(R.drawable.emblem_plain3);
        Bitmap emblem4 = getImage(R.drawable.emblem_plain4);
        Bitmap emblem5 = getImage(R.drawable.emblem_plain5);
        Bitmap emblemBack = getImage(R.drawable.emblem_back);
        Bitmap glow = getImage(R.drawable.emble_glowm);

        bindPicture(emblemBack,0);
        bindPicture(glow,1);
        bindPicture(emblem1,2);
        bindPicture(emblem2,3);
        bindPicture(emblem3,4);
        bindPicture(emblem4,5);
        bindPicture(emblem5,6);
        float outlineOffset = 0.0125f;
        program = GLES20.glCreateProgram();
        textureCircle1 = new Circle(- 0.0625f,program);
        textureCircle2 = new Circle( 0.0625f,program);
        cylinder = new Cylinder(0.0625f,program,.5f );
        outlineCircle1 = new Circle(- 0.0625f - outlineOffset,program,.5f + outlineOffset);
        outlineCircle2 = new Circle( 0.0625f + outlineOffset,program,.5f + outlineOffset);
        cylinder2 = new Cylinder(0.0625f + outlineOffset,program,.5f + outlineOffset);
        cylinder2.color = new float[] {1.0f,1.0f,1.0f,1.0f};
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);


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
    float blendFactor = 0.0f;
    int doGlow = 0;
    float quickSpinAngle = 0;
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        float[] scratch = new float[16];


        // Create a rotation transformation for the triangle
        Matrix.setIdentityM(rotationMatrix,0);
        Matrix.setIdentityM(outlineTranslator,0);
        if(quickSpinAngle > 0){
            quickSpinAngle -= 10;
            mAngle+= 10;
            if(quickSpinAngle <= 0){
                doGlow = -1;
            }
        }
        if(doGlow != 0){
            if((doGlow == 1 && blendFactor < 1.0f) || (doGlow==-1 && blendFactor > 0.0f))
                    blendFactor += (doGlow * 0.05f);
            else{

                quickSpinAngle = (360 * 3) - (mAngle % 360);
                quickSpinAngle *= doGlow;

                doGlow = 0;
                if((quickSpinAngle < 0) && !surfaceView.autoRotate){
                    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    surfaceView.requestRender();
                }
            }
        }
        if(surfaceView.autoRotate && quickSpinAngle < 1){
                if ((Math.abs(mAngle) % (360 * 5)) > 10) {
                    if (mAngle > 0)
                        mAngle -= 10;
                    else
                        mAngle += 10;
                } else {
                    mAngle = mAngle - (Math.abs(mAngle) % (360 * 5));
                    surfaceView.autoRotate = false;
                    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    surfaceView.requestRender();
                }

        }
        Matrix.rotateM(rotationMatrix, 0, mAngle, 0,  1.0f, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);
        int vPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);
        int n = Math.abs((int) (mAngle/360));
        if(quickSpinAngle > 0 || doGlow!=0)
            textureCircle1.draw(scratch,textures[2 + Math.abs(n%5)],textures[1],blendFactor);
        else
            textureCircle1.draw(scratch,textures[2 + Math.abs(n%5)],textures[2 + Math.abs((n+ 1)%5)],calculateTransitionFadeFactor(Math.abs(mAngle) % 360));
        cylinder.draw(scratch);
        textureCircle2.draw(scratch,textures[0],-99,0);
        Matrix.translateM(outlineTranslator,0,0,0,0.5f);
        Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        //Matrix.multiplyMM(scratch, 0, outlineTranslator, 0, scratch, 0);
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, scratch, 0);
        outlineCircle1.draw(scratch);
        cylinder2.draw(scratch);
        outlineCircle2.draw(scratch);

    }
    private float calculateTransitionFadeFactor(float angle){
        return angle > 270 ? Math.min((angle - 270)/90,1.0f) : 0.0f;
    }
}
