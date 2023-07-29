package com.ndds.freedomclouds;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cylinder extends Shape {
    public Cylinder(float depth, int program, float radius) {
        mProgram = program;
        float[] positions = new float[(360 * 18)];
        int j;
        for(int i = 0; i < 360; i += 1) {
            j =  18 * i;
            float distanceX = (float) (radius*Math.sin(i*Math.PI/180f));
            float distanceY = (float) (radius*Math.cos(i*Math.PI/180f));

            float nextDistanceX = (float) (radius*Math.sin((i + 1) * Math.PI/180f));
            float nextDistanceY = (float) (radius*Math.cos((i + 1) * Math.PI/180f));

            positions[j] = distanceX;
            positions[j + 1] = distanceY;
            positions[j + 2] = -depth;

            positions[j + 3] = distanceX;
            positions[j + 4] = distanceY;
            positions[j + 5] = depth;

            positions[j + 6] = nextDistanceX;
            positions[j + 7] = nextDistanceY;
            positions[j + 8] = depth;


            positions[j + 9] = distanceX;
            positions[j + 10] = distanceY;
            positions[j + 11] = -depth;

            positions[j + 12] = nextDistanceX;
            positions[j + 13] = nextDistanceY;
            positions[j + 14] = -depth;

            positions[j + 15] = nextDistanceX;
            positions[j + 16] = nextDistanceY;
            positions[j + 17] = depth;
        }

        // initialize vertex byte buffer for shape coordinates
        vertexBuffer = buildBuffer(positions);
    }
}
