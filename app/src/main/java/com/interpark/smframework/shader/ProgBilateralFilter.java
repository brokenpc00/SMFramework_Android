package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

public class ProgBilateralFilter extends ProgSprite {
    public static final float DEFAULT_TEXEL_SPACING_MULTIPLIER = 3f;
    public static final float DEFAULT_DISTANCE_NORMALIZE_FACTOR = 6f;

    private static final String NAME_DISTANCE_NORMALIZE_FACTOR = "distanceNormalizationFactor";
    private static final String NAME_TEXEL_WIDTH = "texelWidthOffset";
    private static final String NAME_TEXEL_HEIGHT = "texelHeightOffset";
    private static final String NAME_RESOLUTION = "u_resolution";
    private static final String NAME_CENTER = "u_center";
    private static final String NAME_RADIUS = "u_radius";
    private static final String NAME_BORDER = "u_aaWidth";

    private int uniformDistanceNormalizationFactor;
    private int uniformTexelWidth;
    private int uniformTexelHeight;

    private int uniformResolution;
    private int uniformCenter;
    private int uniformRadius;
    private int uniformBorder;

    private int mProgramIndex = 0;
    private float mTexelSpacingMultiplier = DEFAULT_TEXEL_SPACING_MULTIPLIER;
    private float mDistanceNotmalizeFactor = DEFAULT_DISTANCE_NORMALIZE_FACTOR;

    private float mFaceX;
    private float mFaceY;
    private float mFaceWidth;
    private float mFaceHeight;

    @Override
    public void complete() {
        super.complete();
        uniformDistanceNormalizationFactor = GLES20.glGetUniformLocation(programId, NAME_DISTANCE_NORMALIZE_FACTOR);
        uniformTexelWidth = GLES20.glGetUniformLocation(programId, NAME_TEXEL_WIDTH);
        uniformTexelHeight = GLES20.glGetUniformLocation(programId, NAME_TEXEL_HEIGHT);

        uniformResolution = GLES20.glGetUniformLocation(programId, NAME_RESOLUTION);
        uniformCenter = GLES20.glGetUniformLocation(programId, NAME_CENTER);
        uniformRadius = GLES20.glGetUniformLocation(programId, NAME_RADIUS);
        uniformBorder = GLES20.glGetUniformLocation(programId, NAME_BORDER);
    }

    @Override
    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv) {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            if (mProgramIndex == 0) {
                GLES20.glUniform1f(uniformTexelWidth, 0);
                GLES20.glUniform1f(uniformTexelHeight, mTexelSpacingMultiplier / texture.getHeight());
            } else {
                GLES20.glUniform1f(uniformTexelWidth, mTexelSpacingMultiplier / texture.getWidth());
                GLES20.glUniform1f(uniformTexelHeight, 0);
            }
            GLES20.glUniform1f(uniformDistanceNormalizationFactor, mDistanceNotmalizeFactor);

            GLES20.glUniform2f(uniformResolution, texture.getWidth(), texture.getHeight());
            GLES20.glUniform2f(uniformCenter, mFaceX+texture.getWidth()/2, mFaceY+texture.getHeight()/2);
            GLES20.glUniform2f(uniformRadius, mFaceWidth, mFaceHeight*1.2f);
            GLES20.glUniform1f(uniformBorder, mFaceWidth/2f);

            return true;
        }
        return false;
    }

    public void setProgramIndex(int index) {
        mProgramIndex = index;
    }

    public void setTexelSpacingMultiplier(float texelSpacingMultiplier) {
        mTexelSpacingMultiplier = texelSpacingMultiplier;
    }

    public void setDistanceNotmalizeFactor(float distanceNotmalizeFactor) {
        mDistanceNotmalizeFactor = distanceNotmalizeFactor;
    }

    public void setFaceDetectionValue(float faceX, float faceY, float faceWidth, float faceHeight) {
        mFaceX = faceX;
        mFaceY = faceY;
        mFaceWidth = faceWidth;
        mFaceHeight = faceHeight;
    }
}
