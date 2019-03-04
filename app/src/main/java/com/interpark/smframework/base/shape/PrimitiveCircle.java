package com.interpark.smframework.base.shape;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.shader.ProgPrimitiveCircle;
import com.interpark.smframework.shader.ProgPrimitiveRing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PrimitiveCircle extends DrawNode {
    protected FloatBuffer uv;

    private float mRadius, mThickness, mBorder;

    public PrimitiveCircle(IDirector director) {
        this.director = director;
        drawMode = GLES20.GL_TRIANGLE_STRIP;
        numVertices = 4;

        final float[] v = {
                -1, -1,
                1, -1,
                -1,  1,
                1,  1,
        };

        this.v = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(v);
        this.v.position(0);

        this.uv = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.put(_texCoordConst[Quadrant_ALL]);
        this.uv.position(0);

        setProgramType(ShaderManager.ProgramType.PrimitiveRing);
    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            switch (program.getType()) {
                default:
                case PrimitiveCircle:
                    if( ((ProgPrimitiveCircle)program).setDrawParam(sMatrix, v, uv, mRadius, mBorder)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                    break;
                case PrimitiveRing:
                    if( ((ProgPrimitiveRing)program).setDrawParam(sMatrix, v, uv, mRadius, mThickness, mBorder)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                    break;
            }
        }
    }

    public void drawRing(float x, float y, float radius, float thickness) {
        drawRing(x, y, radius, thickness, 1.5f);
    }

    public void drawRing(float x, float y, float radius, float thickness, float border) {
        setProgramType(ShaderManager.ProgramType.PrimitiveRing);
        mRadius = radius;
        mThickness = thickness;
        mBorder = border;
        drawScale(x, y, radius);
    }

    public void drawCircle(float x, float y, float radius) {
        drawCircle(x, y, radius, 1.5f);
    }

    public void drawCircle(float x, float y, float radius, float border) {
        setProgramType(ShaderManager.ProgramType.PrimitiveCircle);
        mRadius = radius;
        mBorder = border;
        drawScale(x, y, radius);
    }

    public void drawRingRotateY(float x, float y, float radius, float thickness, float border, float rotateY) {
        setProgramType(ShaderManager.ProgramType.PrimitiveRing);
        mRadius = radius;
        mThickness = thickness;
        mBorder = border;
        drawScaleXYRotateY(x, y, -radius, radius, rotateY);
    }

    public void drawCircleRotateY(float x, float y, float radius, float border, float rotateY) {
        setProgramType(ShaderManager.ProgramType.PrimitiveCircle);
        mRadius = radius;
        mBorder = border;
        drawScaleXYRotateY(x, y, -radius, radius, rotateY);
    }
}
