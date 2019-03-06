package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.util.Vec2;

import java.nio.FloatBuffer;

public class ProgPrimitiveCircle extends ProgPrimitive {
    private static final String NAME_TEXTURECOORD = "inputTextureCoordinate";

    private static final String NAME_RADIUS = "radius";
    private static final String NAME_AAWIDTH = "aaWidth";
    private static final String NAME_ANCHOR = "anchor";

    private int attrTextureCoordinate;
    private int uniformRadius;
    private int uniformAAWidth;
    private int uniformAnchor;

    @Override
    public void complete() {
        super.complete();
        attrTextureCoordinate = GLES20.glGetAttribLocation(programId, NAME_TEXTURECOORD);
        uniformRadius = GLES20.glGetUniformLocation(programId, NAME_RADIUS);
        uniformAAWidth = GLES20.glGetUniformLocation(programId, NAME_AAWIDTH);
        uniformAnchor = GLES20.glGetUniformLocation(programId, NAME_ANCHOR);
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

    public boolean setDrawParam(float[] modelMatrix, FloatBuffer v, FloatBuffer uv, float radius, float aaWidth, Vec2 anchor) {
        if (super.setDrawParam(modelMatrix, v)) {
            GLES20.glVertexAttribPointer(attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv);
            GLES20.glUniform1f(uniformRadius, radius);
            GLES20.glUniform1f(uniformAAWidth, aaWidth);
            GLES20.glUniform2f(uniformAnchor, anchor.x, anchor.y);
            return true;
        }
        return false;
    }
}
