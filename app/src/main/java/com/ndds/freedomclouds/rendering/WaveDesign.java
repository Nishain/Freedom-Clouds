package com.ndds.freedomclouds.rendering;

public class WaveDesign extends Shape {
    private final int coverage = 30;
    private final float outlineOffset;
    private final float[] twoDimensionalIncrement;
    private final float[] originalVertexData = new float[(coverage + 40) * 180 * 18];
    private final float[] currentVertexData = new float[originalVertexData.length];
    private final float[] sinRatio = new float[180];
    private final float zRadius;

    private void add3DCoordinate(int index, int xyRotationAngle, int zRotationAngle, double lengthFromCenter, float zAmplitude, float zWidth) {
        originalVertexData[index] = (float) (Math.sin(Math.toRadians(xyRotationAngle)) * lengthFromCenter);
        originalVertexData[index + 1] = (float) (Math.cos(Math.toRadians(xyRotationAngle)) * lengthFromCenter);
        float frequencyMultiplier = 20;
        originalVertexData[index + 2] = (float) ((Math.cos(Math.toRadians(zRotationAngle)) * zWidth) + (Math.sin(Math.toRadians(xyRotationAngle * frequencyMultiplier))* zAmplitude));
    }

    private void addEnd(boolean direction, int angleOffset) {
        int index = direction ? (coverage + 20) * 180 * 18 : 0;
        int index2 = direction ? (coverage + 20) * 180 * 12 : 0;
        float modifiedZRadius = (zRadius * 0.4f) + outlineOffset;
        float aptitude = (modifiedZRadius - outlineOffset) - (zRadius * 0.1f);
        float change = (modifiedZRadius - outlineOffset) / 20f;
        if (!direction) modifiedZRadius = outlineOffset;
        int startAngle = direction ? coverage + 20 + angleOffset : angleOffset;

        for (int i = startAngle; i < (startAngle + 20); i++) {
            for (int j = 0; j < 180; j++) {
                double twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j)) * modifiedZRadius;
                double twoDimensionalLengthOffset = Math.sin(Math.toRadians(j)) * (modifiedZRadius - outlineOffset);
                twoDimensionalIncrement[index2] = (float) (Math.sin(Math.toRadians(i)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[index2 + 1] = (float) (Math.cos(Math.toRadians(i)) * twoDimensionalLengthOffset);
                add3DCoordinate(index, i, j, twoDimensionalLength, aptitude, modifiedZRadius);

                float nextRadius = modifiedZRadius + (direction ? -change : change);
                twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j)) * nextRadius;
                twoDimensionalLengthOffset = Math.sin(Math.toRadians(j)) * (nextRadius - outlineOffset);
                twoDimensionalIncrement[index2 + 2] = (float) (Math.sin(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[index2 + 3] = (float) (Math.cos(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                add3DCoordinate(index + 3, i + 1, j, twoDimensionalLength, aptitude, nextRadius);

                twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j + 1)) * modifiedZRadius;
                twoDimensionalLengthOffset = Math.sin(Math.toRadians(j + 1)) * (modifiedZRadius - outlineOffset);
                twoDimensionalIncrement[index2 + 4] = (float) (Math.sin(Math.toRadians(i)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[index2 + 5] = (float) (Math.cos(Math.toRadians(i)) * twoDimensionalLengthOffset);
                add3DCoordinate(index + 6, i, j + 1, twoDimensionalLength, aptitude, modifiedZRadius);

                twoDimensionalIncrement[index2 + 6] = (float) (Math.sin(Math.toRadians(i)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[index2 + 7] = (float) (Math.cos(Math.toRadians(i)) * twoDimensionalLengthOffset);
                add3DCoordinate(index + 9, i, j + 1, twoDimensionalLength, aptitude, modifiedZRadius);

                twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j + 1)) * nextRadius;
                twoDimensionalLengthOffset = Math.sin(Math.toRadians(j + 1)) * (nextRadius - outlineOffset);
                twoDimensionalIncrement[index2 + 8] = (float) (Math.sin(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[index2 + 9] = (float) (Math.cos(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                add3DCoordinate(index + 12, i + 1, j + 1, twoDimensionalLength, aptitude, nextRadius);

                twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j)) * nextRadius;
                twoDimensionalLengthOffset = Math.sin(Math.toRadians(j)) * (nextRadius - outlineOffset);
                twoDimensionalIncrement[index2 + 10] = (float) (Math.sin(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[index2 + 11] = (float) (Math.cos(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                add3DCoordinate(index + 15, i + 1, j, twoDimensionalLength, aptitude, nextRadius);

                index += 18;
                index2 += 12;
            }
            if (direction) modifiedZRadius -= change;
            else modifiedZRadius += change;
        }
    }

    public void drawWithShapeMutation(int angleX, double brightnessFactor) {
        int index2 = 0;
        final float factor = sinRatio[angleX > 0 ? angleX % 180 : -angleX % 180];
        for (int i = 0; i < originalVertexData.length; i += 3) {
            currentVertexData[i] = originalVertexData[i] + factor * twoDimensionalIncrement[index2];
            currentVertexData[i + 1] = originalVertexData[i + 1] + factor * twoDimensionalIncrement[index2 + 1];
            index2 += 2;
        }
        vertexBuffer = buildBuffer(currentVertexData);
        draw(brightnessFactor);
    }

    WaveDesign(int program, float zRadius, int angleOffset, float outlineOffset) {
        mProgram = program;
        this.outlineOffset = outlineOffset;
        this.zRadius = zRadius;
        twoDimensionalIncrement = new float[(coverage + 40) * 180 * 12];

        float modifiedZRadius = (zRadius * 0.4f) + outlineOffset;
        float aptitude = (modifiedZRadius - outlineOffset) - (zRadius * 0.1f);

        addEnd(false, angleOffset);
        int threeDIndex = 20 * 180 * 18;
        int twoDIndex = 20 * 180 * 12;
        int limit = coverage + angleOffset + 20;
        for (int i = (angleOffset + 20); i < limit; i++) {
            for (int j = 0; j < 180; j++) {
                double twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j)) * modifiedZRadius;
                double twoDimensionalLengthOffset = Math.sin(Math.toRadians(j)) * (modifiedZRadius - outlineOffset);
                twoDimensionalIncrement[twoDIndex] = (float) (Math.sin(Math.toRadians(i)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[twoDIndex + 1] = (float) (Math.cos(Math.toRadians(i)) * twoDimensionalLengthOffset);
                add3DCoordinate(threeDIndex, i, j, twoDimensionalLength, aptitude, modifiedZRadius);

                twoDimensionalIncrement[twoDIndex + 2] = (float) (Math.sin(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[twoDIndex + 3] = (float) (Math.cos(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                add3DCoordinate(threeDIndex + 3, i + 1, j, twoDimensionalLength, aptitude, modifiedZRadius);

                twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j + 1)) * modifiedZRadius;
                twoDimensionalLengthOffset = Math.sin(Math.toRadians(j + 1)) * (modifiedZRadius - outlineOffset);
                twoDimensionalIncrement[twoDIndex + 4] = (float) (Math.sin(Math.toRadians(i)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[twoDIndex + 5] = (float) (Math.cos(Math.toRadians(i)) * twoDimensionalLengthOffset);
                add3DCoordinate(threeDIndex + 6, i, j + 1, twoDimensionalLength, aptitude, modifiedZRadius);

                twoDimensionalIncrement[twoDIndex + 6] = (float) (Math.sin(Math.toRadians(i)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[twoDIndex + 7] = (float) (Math.cos(Math.toRadians(i)) * twoDimensionalLengthOffset);
                add3DCoordinate(threeDIndex + 9, i, j + 1, twoDimensionalLength, aptitude, modifiedZRadius);

                twoDimensionalIncrement[twoDIndex + 8] = (float) (Math.sin(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[twoDIndex + 9] = (float) (Math.cos(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                add3DCoordinate(threeDIndex + 12, i + 1, j + 1, twoDimensionalLength, aptitude, modifiedZRadius);

                twoDimensionalLength = 0.5f + Math.sin(Math.toRadians(j)) * modifiedZRadius;
                twoDimensionalLengthOffset = Math.sin(Math.toRadians(j)) * (modifiedZRadius - outlineOffset);
                twoDimensionalIncrement[twoDIndex + 10] = (float) (Math.sin(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                twoDimensionalIncrement[twoDIndex + 11] = (float) (Math.cos(Math.toRadians(i + 1)) * twoDimensionalLengthOffset);
                add3DCoordinate(threeDIndex + 15, i + 1, j, twoDimensionalLength, aptitude, modifiedZRadius);

                threeDIndex += 18;
                twoDIndex += 12;
            }
        }
        addEnd(true, angleOffset);
        System.arraycopy(originalVertexData, 0, currentVertexData, 0, originalVertexData.length);

        for (int i = 0; i < 180; i++) {
            sinRatio[i] = (float) Math.sin(Math.toRadians(i));
        }
    }
}
