package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.util.Vec2;

import java.nio.FloatBuffer;

public class ProgGeineEffect2 extends ProgSprite {
    private static final String NAME_ANCHOR =  "u_anchor";
    private static final String NAME_PROGRESS = "u_progress";

    private int uniformAnchor;
    private int uniformProgress;

    @Override
    public void complete() {
        super.complete();
        uniformAnchor = GLES20.glGetUniformLocation(programId, NAME_ANCHOR);
        uniformProgress = GLES20.glGetUniformLocation(programId, NAME_PROGRESS);
    }

    @Override
    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv) {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            return true;
        }
        return false;
    }

    public void setGeineValue(Vec2 anchor, float progress) {
        GLES20.glUniform2f(uniformAnchor, anchor.x, anchor.y);
        GLES20.glUniform1f(uniformProgress, progress);
    }
}
