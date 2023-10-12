package com.ndds.freedomclouds.rendering;

import android.opengl.GLES20;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class Circle extends Shape {
    private FloatBuffer textureBuffer;

    public Circle(float depth,int program,float radius){
        mProgram = program;
        float[] positions = new float[(360 * 9)];
        int j;
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

        // initialize vertex byte buffer for shape coordinates
        vertexBuffer = buildBuffer(positions);
    }

    public Circle(float depth, int program) {
        mProgram = program;
        float[] positions = new float[(360 * 9)];
        float[] textureCoordinates = new float[360 * 6];
        int j, k;
        for(int i = 0; i < 360; i+= 1){
            j =  9 * i;
            k = i * 6;
            textureCoordinates[k] = 0.5f;
            textureCoordinates[k+1] = 0.5f;
            positions[j] = 0.0f;
            positions[j + 1] = 0.0f;
            positions[j + 2] = depth;

            float distanceX = (float) (0.5 * Math.sin(Math.toRadians(i)));
            float nextDistanceX = (float) (0.5 * Math.sin((Math.toRadians(i + 1))));

            float distanceY = (float) (0.5 * Math.cos(Math.toRadians(i)));
            float nextDistanceY = (float) (0.5 * Math.cos(Math.toRadians(i + 1)));

            positions[j + 3] = distanceX;
            positions[j + 4] = distanceY;
            positions[j + 5] = depth;
            textureCoordinates[k + 2] = distanceX + 0.5f;
            textureCoordinates[k + 3] = -distanceY + 0.5f;

            positions[j + 6] = nextDistanceX;
            positions[j + 7] = nextDistanceY;
            positions[j + 8] = depth;
            textureCoordinates[k + 4] = nextDistanceX + 0.5f;
            textureCoordinates[k + 5] = -nextDistanceY + 0.5f;
        }

        vertexBuffer = buildBuffer(positions);
        textureBuffer = buildBuffer(textureCoordinates);
    }

    public void draw(int texture1, int texture2, float blendFactor, double brightnessFactor) {
        // Add program to OpenGL ES environment
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(mProgram);
        GLES20.glDisable(GLES20.GL_BLEND);
        // get handle to vertex shader's vPosition member
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
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
        // 4 bytes per vertex
        int vertexStride = COORDINATES_PER_VERTEX * 4;
        GLES20.glVertexAttribPointer(positionHandle, COORDINATES_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        int isTextureHandle  = GLES20.glGetUniformLocation(mProgram,"textured");

        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        float[] blendedColor = new float[] {
                (float) (color[0] * brightnessFactor),
                (float) (color[1] * brightnessFactor),
                color[2], color[3]
        };

        GLES20.glUniform4fv(colorHandle, 1, blendedColor, 0);

        GLES20.glUniform1f(isTextureHandle,1.0f);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 360 * 3);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texturePositionHandle);

    }
}
