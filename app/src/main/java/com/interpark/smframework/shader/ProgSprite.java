package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

// 제일 기본 sprite
public class ProgSprite extends ProgPrimitive {
    private static final String NAME_TEXTURE = "inputImageTexture";
    private static final String NAME_TEXTURECOORD = "inputTextureCoordinate";

    private int uniformTexture;
    private int attrTextureCoordinate;

    @Override
    public void complete() {
        super.complete();
        uniformTexture = GLES20.glGetUniformLocation(programId, NAME_TEXTURE);
        attrTextureCoordinate = GLES20.glGetAttribLocation(programId, NAME_TEXTURECOORD);
    }

    @Override
    public void bind() {
        super.bind();
        GLES20.glUniform1i(uniformTexture, 0);
        GLES20.glEnableVertexAttribArray(attrTextureCoordinate);
    }

    @Override
    public void unbind() {
        super.unbind();
        GLES20.glDisableVertexAttribArray(attrTextureCoordinate);
    }

    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv) {
        if (director.bindTexture(texture)) {
            super.setDrawParam(modelMatrix, v);
            GLES20.glVertexAttribPointer(attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv);
            return true;
        }
        return false;
    }
}
