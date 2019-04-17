package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;

import java.nio.FloatBuffer;

public class ProgCameraPreview extends ShaderProgram {
    private static final String NAME_MODEL = "model";
    private static final String NAME_PROJECTION = "projection";
    private static final String NAME_POSITION = "position";

    private static final String NAME_TEXTURE = "inputImageTexture";
    private static final String NAME_TEXTURE2 = "inputImageTexture2";
    private static final String NAME_TEXTURECOORD = "inputTextureCoordinate";

    private int uniformTexture;
    private int uniformTexture2;
    private int uniformModelMatrix;
    private int uniformProjectionMatrix;

    private int attrPosition;
    private int attrTextureCoordinate;

    @Override
    public void complete() {
        uniformTexture = GLES20.glGetUniformLocation(programId, NAME_TEXTURE);
        uniformTexture2 = GLES20.glGetUniformLocation(programId, NAME_TEXTURE2);
        uniformModelMatrix = GLES20.glGetUniformLocation(programId, NAME_MODEL);
        uniformProjectionMatrix = GLES20.glGetUniformLocation(programId, NAME_PROJECTION);

        attrPosition = GLES20.glGetAttribLocation(programId, NAME_POSITION);
        attrTextureCoordinate = GLES20.glGetAttribLocation(programId, NAME_TEXTURECOORD);
    }

    @Override
    public void bind() {
        GLES20.glUseProgram(programId);
        GLES20.glUniformMatrix4fv(uniformProjectionMatrix, 1, false, director.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0);
        GLES20.glUniform1i(uniformTexture, 0);
        GLES20.glUniform1i(uniformTexture2, 1);
        GLES20.glEnableVertexAttribArray(attrPosition);
        GLES20.glEnableVertexAttribArray(attrTextureCoordinate);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(attrPosition);
        GLES20.glDisableVertexAttribArray(attrTextureCoordinate);
    }

    @Override
    public void setMatrix(float[] matrix) {
        GLES20.glUniformMatrix4fv(uniformProjectionMatrix, 1, false, matrix, 0);
    }

    public boolean setDrawParam(float[] modelMatrix, FloatBuffer v, FloatBuffer uv) {
        GLES20.glVertexAttribPointer(attrPosition, 2, GLES20.GL_FLOAT, false, 0, v);
        GLES20.glVertexAttribPointer(attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv);
        GLES20.glUniformMatrix4fv(uniformModelMatrix, 1, false, modelMatrix, 0);

        GLES20.glUniform1i(uniformTexture, 0);
        GLES20.glUniform1i(uniformTexture2, 1);

        return true;
    }
}
