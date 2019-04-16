package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

public class ProgEdgeGlow extends ProgSprite {
    private static final String NAME_TEXTURE_WIDTH = "inputTextureWidth";
    private static final String NAME_TEXTURE_HEIGHT = "inputTextureHeight";
    private static final String NAME_SCAN_POSITION = "scanPosition";

    private int uniformTextureWidth;
    private int uniformTextureHeight;
    private int uniformScanPosition;

    @Override
    public void complete() {
        super.complete();
        uniformTextureWidth = GLES20.glGetUniformLocation(programId, NAME_TEXTURE_WIDTH);
        uniformTextureHeight = GLES20.glGetUniformLocation(programId, NAME_TEXTURE_HEIGHT);
        uniformScanPosition = GLES20.glGetUniformLocation(programId, NAME_SCAN_POSITION);
    }

    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv, float scanPosition) {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            GLES20.glUniform1f(uniformTextureWidth, texture.getWidth());
            GLES20.glUniform1f(uniformTextureHeight, texture.getHeight());
            GLES20.glUniform1f(uniformScanPosition, scanPosition);
            return true;
        }
        return false;
    }
}
