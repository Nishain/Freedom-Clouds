package com.ndds.freedomclouds;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Circle {

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
    public Circle(float depth,int program,float radius){
        mProgram = program;
        float[] positions = new float[(360 * 9)];
        int j = 0;
        for(int i=0;i<360;i+=1){
            j =  9 * i;
            positions[j] = 0.0f;
            positions[j + 1] = 0.0f;
            positions[j + 2] = depth;

            positions[j + 3] = (float) (radius*Math.sin(i*Math.PI/180f));
            positions[j + 4] = (float) (radius*Math.cos(i*Math.PI/180f));
            positions[j + 5] = depth;

            positions[j + 6] = (float) (radius*Math.sin((i+1)*Math.PI/180f));
            positions[j + 7] = (float) (radius*Math.cos((i+1)*Math.PI/180f));
            positions[j + 8] = depth;
        }
        squareCoords = positions;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }
    public Circle(float depth, int program) {
        mProgram = program;
        float[] pos=new float[(360 * 9)];
        float[] textureCood = new float[360 * 6];
        int j = 0;
        int k = 0;
        for(int i=0;i<360;i+=1){
            j =  9 * i;
            k = i * 6;
            textureCood[k] = 0.5f;
            textureCood[k+1] = 0.5f;
            pos[j] = 0.0f;
            pos[j + 1] = 0.0f;
            pos[j + 2] = depth;

            pos[j + 3] = (float) (0.5*Math.sin(i*Math.PI/180f));
            pos[j + 4] = (float) (0.5*Math.cos(i*Math.PI/180f));
            pos[j + 5] = depth;
            textureCood[k + 2] = (float) ((0.5*Math.sin(i*Math.PI/180f)) + 0.5f);
            textureCood[k + 3] = (float) ((0.5*Math.cos(i*Math.PI/180f)) + 0.5f);

            pos[j + 6] = (float) (0.5*Math.sin((i+1)*Math.PI/180f));
            pos[j + 7] = (float) (0.5*Math.cos((i+1)*Math.PI/180f));
            pos[j + 8] = depth;
            textureCood[k + 4] = (float) ((0.5*Math.sin((i+1)*Math.PI/180f)) + 0.5f);
            textureCood[k + 5] = (float) ((0.5*Math.cos((i+1)*Math.PI/180f)) + 0.5f);
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

        bb = ByteBuffer.allocateDirect(textureCood.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textureBuffer = bb.asFloatBuffer();
        textureBuffer.put(textureCood);
        textureBuffer.position(0);


        int vertexShader = CustomRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                CustomRenderer.vertexShaderCode);
        int fragmentShader = CustomRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                CustomRenderer.fragmentShaderCode);

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }
    private int positionHandle;
    private int colorHandle;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    float[] color = { 1.0f, 1.0f, 1.0f, 1.0f };

    public void draw(float brightnessFactor) {
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

        // Set color for drawing the triangle
        float[] blendedColor = new float[] {
                color[0] * brightnessFactor,
                color[1] * brightnessFactor,
                color[2], color[3]
        };
        GLES20.glUniform4fv(colorHandle, 1, blendedColor, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (360 * 6));

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
    public void draw(int texture1, int texture2, float blendFactor, float brightnessFactor) {
        // Add program to OpenGL ES environment
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(mProgram);
        GLES20.glDisable(GLES20.GL_BLEND);
        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int textureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        int textureHandle2 = GLES20.glGetUniformLocation(mProgram, "uTexture2");
        int texturePositionHandle = GLES20.glGetAttribLocation(mProgram, "aTexPosition");

        GLES20.glVertexAttribPointer(texturePositionHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(texturePositionHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1);
        GLES20.glUniform1i(textureHandle, 0);
        int textureMixHandle  = GLES20.glGetUniformLocation(mProgram,"textureMix");
        if(texture2 != -99) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2);
            GLES20.glUniform1i(textureHandle2, 1);
            GLES20.glUniform1f(textureMixHandle,blendFactor);
        }else{
            GLES20.glUniform1f(textureMixHandle,0.0f);
        }

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        int isTextureHandle  = GLES20.glGetUniformLocation(mProgram,"textured");

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        float[] blendedColor = new float[] {
                color[0] * brightnessFactor,
                color[1] * brightnessFactor,
                color[2], color[3]
        };

        GLES20.glUniform4fv(colorHandle, 1, blendedColor, 0);

        GLES20.glUniform1f(isTextureHandle,1.0f);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);
        // get handle to fragment shader's vColor member
        //colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        //GLES20.glUniform4fv(colorHandle, 1, color, 0);


        // get handle to shape's transformation matrix
//        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
//
//        // Pass the projection and view transformation to the shader
//        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);
        // Draw the triangle

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 360 * 3);//(360/20) * 3
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
//                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texturePositionHandle);

    }
}
