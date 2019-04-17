package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

// 제일 기본 3D sprite

public class ProgSprite3D extends ShaderProgram {
    private static final String NAME_MODEL = "model";
    private static final String NAME_PROJECTION = "projection";
    private static final String NAME_POSITION = "position";
    private static final String NAME_VERTEXCOLOR = "vertexColor";
    private static final String NAME_COLOR = "inputColor";

    private static final String NAME_TEXTURE = "inputImageTexture";
    private static final String NAME_TEXTURECOORD = "inputTextureCoordinate";


    private int uniformTexture;
    private int uniformModelMatrix;
    private int uniformProjectionMatrix;
    private int attrPosition;
    private int attrTextureCoordinate;
    private int attrVertexColor;
    private int uniformColor;

    @Override
    public void complete() {
        uniformTexture = GLES20.glGetUniformLocation(programId, NAME_TEXTURE);
        uniformModelMatrix = GLES20.glGetUniformLocation(programId, NAME_MODEL);
        uniformProjectionMatrix = GLES20.glGetUniformLocation(programId, NAME_PROJECTION);
        uniformColor = GLES20.glGetUniformLocation(programId, NAME_COLOR);

        attrVertexColor = GLES20.glGetAttribLocation(programId, NAME_VERTEXCOLOR);
        attrPosition = GLES20.glGetAttribLocation(programId, NAME_POSITION);
        attrTextureCoordinate = GLES20.glGetAttribLocation(programId, NAME_TEXTURECOORD);
    }

    @Override
    public void bind() {
        GLES20.glUseProgram(programId);
        GLES20.glUniformMatrix4fv(uniformProjectionMatrix, 1, false, director.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0);
        GLES20.glUniform1i(uniformTexture, 0);
        GLES20.glEnableVertexAttribArray(attrPosition);
        GLES20.glEnableVertexAttribArray(attrTextureCoordinate);
        GLES20.glEnableVertexAttribArray(attrVertexColor);

    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(attrPosition);
        GLES20.glDisableVertexAttribArray(attrVertexColor);
        GLES20.glDisableVertexAttribArray(attrTextureCoordinate);
    }

    @Override
    public void setMatrix(float[] matrix) {
        GLES20.glUniformMatrix4fv(uniformProjectionMatrix, 1, false, director.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m, 0);
    }

    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv, FloatBuffer c) {
        if (director.bindTexture(texture)) {
            GLES20.glVertexAttribPointer(attrPosition, 3, GLES20.GL_FLOAT, false, 0, v);
            GLES20.glVertexAttribPointer(attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv);
            GLES20.glVertexAttribPointer(attrVertexColor, 4, GLES20.GL_FLOAT, false, 0, c);
            GLES20.glUniformMatrix4fv(uniformModelMatrix, 1, false, modelMatrix, 0);
            GLES20.glUniform4fv(uniformColor, 1, director.getColor(), 0);
            return true;
        }
        return false;
    }
}
