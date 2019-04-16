package com.interpark.smframework.base;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.shader.ShaderManager.ProgramType;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.view.SMImageView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class DrawNode implements Cloneable {
    protected static float[] sMatrix = new float[16];

    public static float[][] _texCoordConst =  new float[][] {
            {  0,  0,   1,  0,   0,  1,   1,  1 }, // ALL
            {  0,  0,  .5f,  0,   0,  1,  .5f,  1 }, // LEFT_HALF
            { .5f,  0,   1,  0,  .5f,  1,   1,  1 }, // RIGHT_HALF
            {  0, .5f,   1, .5f,   0,  1,   1,  1 }, // TOP_HALF
            {  0,  0,   1,  0,   0, .5f,   1, .5f }, // BOTTOM_HALF
            {  0, .5f,  .5f, .5f,   0,  1,  .5f,  1 }, // LEFT_TOP
            {  0,  0,  .5f,  0,   0, .5f,  .5f, .5f }, // LEFT_BOTTOM
            { .5f, .5f,   1, .5f,  .5f,  1,   1,  1 }, // RIGHT_TOP
            { .5f,  0,   1,  0,  .5f, .5f,   1, .5f }, // RIGHT_BOTTOM
    };

    public static final int Quadrant_ALL = 0;
    public static final int Quadrant_LEFT_HALF = 1;
    public static final int Quadrant_RIGHT_HALF = 2;
    public static final int Quadrant_TOP_HALF = 3;
    public static final int Quadrant_BOTTOM_HALF = 4;
    public static final int Quadrant_LEFT_TOP = 5;
    public static final int Quadrant_LEFT_BOTTOM = 6;
    public static final int Quadrant_RIGHT_TOP =  7;
    public static final int Quadrant_RIGHT_BOTTOM = 8;


    protected Texture texture = null;
    protected Size _contentSize = new Size(Size.ZERO);
    protected float cx;
    protected float cy;
    protected FloatBuffer v;
    protected int numVertices;
    protected int drawMode = GLES20.GL_TRIANGLE_STRIP;

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

    public Size getContentSize() {return _contentSize;}

    public float getWidth() {
        return _contentSize.width;
    }

    public float getHeight() {
        return _contentSize.height;
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
        this._contentSize = new Size(w, h);
        this.cx = cx;
        this.cy = cy;
        initVertexQuad();
    }

    protected void initVertexQuad() {
        final float[] v = {
                -cx,    -cy,
                -cx+_contentSize.width, -cy,
                -cx,    -cy+_contentSize.height,
                -cx+_contentSize.width, -cy+_contentSize.height,
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

    public void setOpacity(int opacity) {
        if (opacity>0xff) {
            opacity = 0xff;
        }

        if (opacity<0) {
            opacity = 0;
        }

        _color.a = (float) opacity/255.0f;
    }

    public void setAlpha(float alpha) {
        _color.a = alpha;
    }

    public void setColor(final Color4F color) {
        _setColor = true;
        _color.set(color);
    }

    protected boolean _setColor = false;
    protected Color4F _color = new Color4F(1, 1, 1, 0);
}
