package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.util.Vec2;

import java.nio.FloatBuffer;

public class ProgPrimitiveRing extends ProgPrimitiveCircle {
    private static final String NAME_THICKNESS = "thickness";

    private int uniformThickness;

    @Override
    public void complete() {
        super.complete();
        uniformThickness = GLES20.glGetUniformLocation(programId, NAME_THICKNESS);
    }

    public boolean setDrawParam(float[] modelMatrix, FloatBuffer v, FloatBuffer uv, float radius, float thickness, float aaWidth, Vec2 anchor) {
        if (super.setDrawParam(modelMatrix, v, uv, radius, aaWidth, anchor)) {
            GLES20.glUniform1f(uniformThickness, thickness);
            return true;
        }
        return false;
    }
}
