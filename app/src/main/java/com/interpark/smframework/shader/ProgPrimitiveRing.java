package com.interpark.smframework.shader;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class ProgPrimitiveRing extends ProgPrimitiveCircle {
    private static final String NAME_THICKNESS = "thickness";

    private int uniformThickness;

    @Override
    public void complete() {
        super.complete();
        uniformThickness = GLES20.glGetUniformLocation(programId, NAME_THICKNESS);
    }

    public boolean setDrawParam(float[] modelMatrix, FloatBuffer v, FloatBuffer uv, float radius, float thickness, float border) {
        if (super.setDrawParam(modelMatrix, v, uv, radius, border)) {
            GLES20.glUniform1f(uniformThickness, thickness);
            return true;
        }
        return false;
    }
}
