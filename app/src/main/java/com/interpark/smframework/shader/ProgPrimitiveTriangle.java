package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.util.Vec2;

import java.nio.FloatBuffer;

public class ProgPrimitiveTriangle extends ProgPrimitive {
    private static final String NAME_TEXTURECOORD = "inputTextureCoordinate";

    private static final String NAME_P0 = "u_p0";
    private static final String NAME_P1 = "u_p1";
    private static final String NAME_P2 = "u_p2";
    private static final String NAME_AAWIDTH = "u_aaWidth";

    private int attrTextureCoordinate;
    private int uniformP0;
    private int uniformP1;
    private int uniformP2;
    private int uniformAAWidth;

    @Override
    public void complete() {
        super.complete();
        attrTextureCoordinate = GLES20.glGetAttribLocation(programId, NAME_TEXTURECOORD);
        uniformP0 = GLES20.glGetUniformLocation(programId, NAME_P0);
        uniformP1 = GLES20.glGetUniformLocation(programId, NAME_P1);
        uniformP2 = GLES20.glGetUniformLocation(programId, NAME_P2);
        uniformAAWidth = GLES20.glGetUniformLocation(programId, NAME_AAWIDTH);
    }

    @Override
    public void bind() {
        super.bind();
        GLES20.glEnableVertexAttribArray(attrTextureCoordinate);
    }

    @Override
    public void unbind() {
        super.unbind();
        GLES20.glDisableVertexAttribArray(attrTextureCoordinate);
    }

    public boolean setDrawParam(float[] modelMatrix, FloatBuffer v, FloatBuffer uv, Vec2 p0, Vec2 p1, Vec2 p2, float aaWidth) {

        if (super.setDrawParam(modelMatrix, v)) {
            GLES20.glVertexAttribPointer(attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv); //00 10, 01 11
            GLES20.glUniform2f(uniformP0, p0.x, p0.y);
            GLES20.glUniform2f(uniformP1, p1.x, p1.y);
            GLES20.glUniform2f(uniformP2, p2.x, p2.y);
            GLES20.glUniform1f(uniformAAWidth, aaWidth);
            return true;
        }

        return false;
    }

}
