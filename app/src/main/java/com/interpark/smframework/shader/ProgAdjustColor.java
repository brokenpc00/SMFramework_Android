package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.base.texture.Texture;

import java.nio.FloatBuffer;

public class ProgAdjustColor extends ProgSprite {
    private static final String NAME_BRIGHTNESS = "brightness";
    private static final String NAME_CONTRAST   = "contrast";
    private static final String NAME_SATURATE 	= "saturate";
    private static final String NAME_TEMPRATURE = "temperature";

    private int uniformBrightness;
    private int uniformContrast;
    private int uniformSaturate;
    private int uniformTemperature;

    @Override
    public void complete() {
        super.complete();
        uniformBrightness 	= GLES20.glGetUniformLocation(programId, NAME_BRIGHTNESS);
        uniformContrast		= GLES20.glGetUniformLocation(programId, NAME_CONTRAST);
        uniformSaturate 	= GLES20.glGetUniformLocation(programId, NAME_SATURATE);
        uniformTemperature 	= GLES20.glGetUniformLocation(programId, NAME_TEMPRATURE);
    }

    public boolean setDrawParam(Texture texture, float[] modelMatrix, FloatBuffer v, FloatBuffer uv,
                                float brightness, float contrast, float saturate, float temperature) {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {

            temperature = temperature < 5000f ? 0.0004f * (temperature-5000.0f) : 0.00006f * (temperature-5000.0f);

            GLES20.glUniform1f(uniformBrightness, brightness);
            GLES20.glUniform1f(uniformContrast, contrast);
            GLES20.glUniform1f(uniformSaturate, 	saturate);
            GLES20.glUniform1f(uniformTemperature,	temperature);
            return true;
        }
        return false;
    }
}
