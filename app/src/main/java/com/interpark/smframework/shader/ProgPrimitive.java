package com.interpark.smframework.shader;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

// 제일 기본 shader

public class ProgPrimitive extends ShaderProgram {
    private static final String NAME_MODEL = "model";
    private static final String NAME_PROJECTION = "projection";
    private static final String NAME_POSITION = "position";
    private static final String NAME_COLOR = "inputColor";

    private int uniformModelMatrix;
    private int uniformProjectionMatrix;
    private int uniformColor;
    private int attrPosition;

    @Override
    public void complete() {
        uniformModelMatrix = GLES20.glGetUniformLocation(programId, NAME_MODEL);
        uniformProjectionMatrix = GLES20.glGetUniformLocation(programId, NAME_PROJECTION);
        uniformColor = GLES20.glGetUniformLocation(programId, NAME_COLOR);

        attrPosition = GLES20.glGetAttribLocation(programId, NAME_POSITION);
    }

    @Override
    public void bind() {
        GLES20.glUseProgram(programId);
        GLES20.glUniformMatrix4fv(uniformProjectionMatrix, 1, false, director.getProjectionMatrix(), 0);
        GLES20.glEnableVertexAttribArray(attrPosition);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(attrPosition);
    }

    @Override
    public void setMatrix(float[] matrix) {
        GLES20.glUniformMatrix4fv(uniformProjectionMatrix, 1, false, matrix, 0);
    }

    public boolean setDrawParam(float[] modelMatrix, FloatBuffer v) {
        GLES20.glVertexAttribPointer(attrPosition, 2, GLES20.GL_FLOAT, false, 0, v);
        GLES20.glUniformMatrix4fv(uniformModelMatrix, 1, false, modelMatrix, 0);
        GLES20.glUniform4fv(uniformColor, 1, director.getColor(), 0);
        return true;
    }
}
