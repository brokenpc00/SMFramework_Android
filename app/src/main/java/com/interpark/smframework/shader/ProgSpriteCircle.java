package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

public class ProgSpriteCircle extends ProgSprite {
    private static final String NAME_RADIUS = "radius";
    private static final String NAME_AAWIDTH = "aaWidth";
    private static final String NAME_ASPECT_RATIO = "aspectRatio";
    private static final String NAME_TEXTURE_CENTER = "textureCenter";

    private int uniformTextureCenter;
    private int uniformAspectRatio;
    private int uniformRadius;
    private int uniformAAWidth;

    @Override
    public void complete() {
        super.complete();
        uniformTextureCenter = GLES20.glGetUniformLocation(programId, NAME_TEXTURE_CENTER);
        uniformAspectRatio = GLES20.glGetUniformLocation(programId, NAME_ASPECT_RATIO);
        uniformRadius = GLES20.glGetUniformLocation(programId, NAME_RADIUS);
        uniformAAWidth = GLES20.glGetUniformLocation(programId, NAME_AAWIDTH);
    }

    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv, float cx, float cy, float radius, float aaWidth) {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            GLES20.glUniform2f(uniformTextureCenter, cx, cy);
            if (texture.getWidth() < texture.getHeight()) {
                GLES20.glUniform2f(uniformAspectRatio, (float)texture.getWidth()/texture.getHeight(), 1f );
            } else {
                GLES20.glUniform2f(uniformAspectRatio, 1f, (float)texture.getHeight()/texture.getWidth() );
            }
            GLES20.glUniform1f(uniformRadius, radius);
            GLES20.glUniform1f(uniformAAWidth, aaWidth);
            return true;
        }
        return false;
    }
}
