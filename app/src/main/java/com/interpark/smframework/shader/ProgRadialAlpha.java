package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

public class ProgRadialAlpha extends ProgSprite {

    private static final String NAME_DIMENSION  = "u_dimension";
    private static final String NAME_CENTER     = "u_center";
    private static final String NAME_RADIUS     = "u_radius";
    private static final String NAME_BORDER 	= "u_border";

    private int uniformDimension;
    private int uniformCenter;
    private int uniformRadius;
    private int uniformBorder;

    @Override
    public void complete() {
        super.complete();

        uniformDimension = GLES20.glGetUniformLocation(programId, NAME_DIMENSION);
        uniformCenter = GLES20.glGetUniformLocation(programId, NAME_CENTER);
        uniformRadius = GLES20.glGetUniformLocation(programId, NAME_RADIUS);
        uniformBorder = GLES20.glGetUniformLocation(programId, NAME_BORDER);
    }

    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv,
                                float cx, float cy, float radius, float border) {
        // TODO : 갤럭시S2의 presion bug로 인해 0.1곱함
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {

            GLES20.glUniform2f(uniformDimension, 0.1f*texture.getWidth(), 0.1f*texture.getHeight());
            GLES20.glUniform2f(uniformCenter, 0.1f*cx, 0.1f*cy);
            GLES20.glUniform1f(uniformRadius, 0.1f*radius);
            GLES20.glUniform1f(uniformBorder, 0.1f*border);

            return true;
        }
        return false;
    }

}
