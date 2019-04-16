package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

public class ProgGaussianBlur extends ProgSprite {
    public static final float DEFAULT_TEXEL_SPACING_MULTIPLIER = 1.0f;

    private static final String NAME_TEXEL_WIDTH = "texelWidthOffset";
    private static final String NAME_TEXEL_HEIGHT = "texelHeightOffset";

    private int uniformTexelWidth;
    private int uniformTexelHeight;

    private int mProgramIndex = 0;
    private float mTexelSpacingMultiplier = DEFAULT_TEXEL_SPACING_MULTIPLIER;

    @Override
    public void complete() {
        super.complete();
        uniformTexelWidth = GLES20.glGetUniformLocation(programId, NAME_TEXEL_WIDTH);
        uniformTexelHeight = GLES20.glGetUniformLocation(programId, NAME_TEXEL_HEIGHT);
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
}
