package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

public class ProgGeineEffect extends ProgSprite {
    private static final String NAME_WIDTH =  "width";
    private static final String NAME_HEIGHT = "height";
    private static final String NAME_MINIMIZE = "minimize";
    private static final String NAME_BEND = "bend";
    private static final String NAME_SIDE = "side";

    private int uniformWidth;
    private int uniformHeight;
    private int uniformMinimize;
    private int uniformBend;
    private int uniformSide;

    @Override
    public void complete() {
        super.complete();
        uniformWidth = GLES20.glGetUniformLocation(programId, NAME_WIDTH);
        uniformHeight = GLES20.glGetUniformLocation(programId, NAME_HEIGHT);
        uniformMinimize = GLES20.glGetUniformLocation(programId, NAME_MINIMIZE);
        uniformBend = GLES20.glGetUniformLocation(programId, NAME_BEND);
        uniformSide = GLES20.glGetUniformLocation(programId, NAME_SIDE);
    }

    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv) {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            GLES20.glUniform1f(uniformWidth, texture.getWidth());
            GLES20.glUniform1f(uniformHeight, texture.getHeight());
            return true;
        }
        return false;
    }

    public void setGeineValue(float minimize, float bend, float side) {
        GLES20.glUniform1f(uniformMinimize, minimize);
        GLES20.glUniform1f(uniformBend, bend);
        GLES20.glUniform1f(uniformSide, side);
    }
}
