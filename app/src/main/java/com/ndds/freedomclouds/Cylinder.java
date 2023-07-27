package com.ndds.freedomclouds;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cylinder {
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f,  0.5f, 0.0f }; // top right

    private int mProgram = 0;

    public Cylinder(float depth, int program, float radius) {
        mProgram = program;
        float[] pos=new float[(360 * 18)];

        int j = 0;
        //pos = new float[]{-0.5f, 0.0f,0.0f,0.5f, 0.0f,0.0f,0.0f, 0.5f,0.0f};
        for(int i=0;i<360;i+=1){
            j =  18 * i;

            pos[j] = (float) (radius*Math.sin(i*Math.PI/180f));
            pos[j + 1] = (float) (radius*Math.cos(i*Math.PI/180f));
            pos[j + 2] = -depth;

            pos[j + 3] = (float) (radius*Math.sin(i*Math.PI/180f));
            pos[j + 4] = (float) (radius*Math.cos(i*Math.PI/180f));
            pos[j + 5] = depth;

            pos[j + 6] = (float) (radius*Math.sin((i+1)*Math.PI/180f));
            pos[j + 7] = (float) (radius*Math.cos((i+1)*Math.PI/180f));
            pos[j + 8] = depth;


            pos[j + 9] = (float) (radius*Math.sin(i*Math.PI/180f));
            pos[j + 10] = (float) (radius*Math.cos(i*Math.PI/180f));
            pos[j + 11] = -depth;

            pos[j + 12] = (float) (radius*Math.sin((i+1)*Math.PI/180f));
            pos[j + 13] = (float) (radius*Math.cos((i+1)*Math.PI/180f));
            pos[j + 14] = -depth;

            pos[j + 15] = (float) (radius*Math.sin((i+1)*Math.PI/180f));
            pos[j + 16] = (float) (radius*Math.cos((i + 1)*Math.PI/180f));
            pos[j + 17] = depth;

        }
        squareCoords = pos;


        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        int vertexShader = CustomRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                CustomRenderer.vertexShaderCode);
        int fragmentShader = CustomRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                CustomRenderer.fragmentShaderCode);

        // create empty OpenGL ES Program
//        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }
    private int positionHandle;
    private int colorHandle;

    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private int vPMatrixHandle;
    float[] color = { 1.0f, 0.64705882f, 0.0f, 1.0f };
    float[] brightnessBlendColor = { 1f, 1f, 1f, 1.0f };
    public void draw(float[] mvpMatrix, float brightnessFactor) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        int isTextureHandle  = GLES20.glGetUniformLocation(mProgram,"textured");
        GLES20.glUniform1f(isTextureHandle,0.0f);
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");


        int colorHandle2 = GLES20.glGetUniformLocation(mProgram, "vColor2");
        brightnessBlendColor[0] = brightnessFactor;
        brightnessBlendColor[1] = brightnessFactor;
        GLES20.glUniform4fv(colorHandle2, 1, brightnessBlendColor, 0);

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (360 * 6));

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);

    }

}
