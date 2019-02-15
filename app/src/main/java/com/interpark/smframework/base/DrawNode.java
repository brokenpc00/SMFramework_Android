package com.interpark.smframework.base;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.shader.ShaderManager.ProgramType;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class DrawNode implements Cloneable {
    protected static float[] sMatrix = new float[16];

    protected Texture texture = null;
    protected float _w;
    protected float _h;
    protected float cx;
    protected float cy;
    protected FloatBuffer v;
    protected int numVertices;
    protected int drawMode;

    protected IDirector director;
    protected ProgramType programType;

    @Override
    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            // Does nothing
        }
        return obj;

    }


    public float getWidth() {
        return _w;
    }

    public float getHeight() {
        return _h;
    }

    public float getCX() {
        return cx;
    }

    public float getCY() {
        return cy;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public void setProgramType(ProgramType type) {
        programType = type;
    }

    protected ShaderProgram useProgram() {
        return director.useProgram(programType);
    }

    protected void initRect(IDirector director,float w, float h, float cx, float cy) {
        this.director = director;
        this._w = w;
        this._h = h;
        this.cx = cx;
        this.cy = cy;
        initVertexQuad();
    }

    protected void initVertexQuad() {
        final float[] v = {
                -cx,    -cy,
                -cx+_w, -cy,
                -cx,    -cy+_h,
                -cx+_w, -cy+_h,
        };

        drawMode = GLES20.GL_TRIANGLE_STRIP;
        numVertices = 4;

        this.v = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(v);
        this.v.position(0);
    }

    // virtual method
    protected abstract void _draw(float[] modelMatrix);

    public void draw(float x, float y) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);

        _draw(sMatrix);
    }

    public void drawScale(float x, float y, float scale) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.scaleM(sMatrix, 0, scale, scale, 1);

        _draw(sMatrix);
    }

    public void drawScaleXY(float x, float y, float scaleX, float scaleY) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.scaleM(sMatrix, 0, scaleX, scaleY, 1);

        _draw(sMatrix);
    }

    public void drawScaleXY2(float x, float y, float z, float scaleX, float scaleY) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, z);
        Matrix.scaleM(sMatrix, 0, scaleX, scaleY, 1);

        _draw(sMatrix);
    }

    public void drawScale(float x, float y, float z, float scale) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, z);
        Matrix.scaleM(sMatrix, 0, scale, scale, 1);

        _draw(sMatrix);
    }

    public void drawRotate(float x, float y, float angle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.rotateM(sMatrix, 0, angle, 0, 0, 1);

        _draw(sMatrix);
    }

    public void drawRotateX(float x, float y, float angle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.rotateM(sMatrix, 0, angle, 1, 0, 0);

        _draw(sMatrix);
    }

    public void drawScaleRotate(float x, float y, float scale, float angle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.scaleM(sMatrix, 0, scale, scale, 1);
        Matrix.rotateM(sMatrix, 0, angle, 0, 0, 1);

        _draw(sMatrix);
    }

    public void drawScaleRotateX(float x, float y, float scale, float angle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.scaleM(sMatrix, 0, scale, scale, 1);
        Matrix.rotateM(sMatrix, 0, angle, 1, 0, 0);

        _draw(sMatrix);
    }

    public void drawScaleXYRotate(float x, float y, float scaleX, float scaleY, float angle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.rotateM(sMatrix, 0, angle, 0, 0, 1);
        Matrix.scaleM(sMatrix, 0, scaleX, scaleY, 1);

        _draw(sMatrix);
    }

    public void drawScaleXYRotateY(float x, float y, float scaleX, float scaleY, float angle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.rotateM(sMatrix, 0, angle, 0, 1, 0);
        Matrix.scaleM(sMatrix, 0, scaleX, scaleY, 1);

        _draw(sMatrix);
    }

    public void drawRotateXYZ(float x, float y, float z, float xAngle, float yAngle, float zAngle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, z);
        Matrix.rotateM(sMatrix, 0, xAngle, 1, 1, 1);

        _draw(sMatrix);
    }

    public void drawRotateY(float x, float y, float angle) {
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.rotateM(sMatrix, 0, angle, 0, 1, 0);

        _draw(sMatrix);
    }

    public void drawMatrix(float[] matrix) {
        _draw(matrix);
    }

    public void releaseResources() {
        if (texture != null) {
            director.getTextureManager().removeTexture(texture);
            texture = null;
        }
    }
}
