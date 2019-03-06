package com.interpark.smframework.base.shape;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.shader.ProgPrimitiveCircle;
import com.interpark.smframework.shader.ProgPrimitiveRing;
import com.interpark.smframework.util.Vec2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PrimitiveCircle extends DrawNode {
    protected FloatBuffer uv;

    private float _radius, _thickness, _aaWidth;
    private Vec2 _anchor = new Vec2(Vec2.MIDDLE);

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
                    if( ((ProgPrimitiveCircle)program).setDrawParam(sMatrix, v, uv, _radius, _aaWidth, _anchor)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                    break;
                case PrimitiveRing:
                    if( ((ProgPrimitiveRing)program).setDrawParam(sMatrix, v, uv, _radius, _thickness, _aaWidth, _anchor)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                    break;
            }
        }
    }

    public void drawRing(float x, float y, float radius, float thickness) {
        drawRing(x, y, radius, thickness, 1.5f);
    }

    public void drawRing(float x, float y, float radius, float thickness, float aaWidth, Vec2 anchor) {
        _anchor.set(anchor);
        drawRing(x, y, radius, thickness, aaWidth);
    }

    public void drawRing(float x, float y, float radius, float thickness, float aaWidth) {
        setProgramType(ShaderManager.ProgramType.PrimitiveRing);
        _radius = radius;
        _thickness = thickness;
        _aaWidth = aaWidth;
        drawScale(x, y, radius);
    }

    public void drawCircle(float x, float y, float radius) {
        drawCircle(x, y, radius, 1.5f);
    }

    public void drawCircle(float x, float y, float radius, float aaWidth, Vec2 anchor) {
        _anchor.set(anchor);
        drawCircle(x, y, radius, aaWidth);
    }
    public void drawCircle(float x, float y, float radius, float aaWidth) {
        setProgramType(ShaderManager.ProgramType.PrimitiveCircle);
        _radius = radius;
        _aaWidth = aaWidth;
        drawScale(x, y, radius);
    }

    public void drawRingRotateY(float x, float y, float radius, float thickness, float aaWidth, float rotateY) {
        setProgramType(ShaderManager.ProgramType.PrimitiveRing);
        _radius = radius;
        _thickness = thickness;
        _aaWidth = aaWidth;
        drawScaleXYRotateY(x, y, -radius, radius, rotateY);
    }

    public void drawCircleRotateY(float x, float y, float radius, float aaWidth, float rotateY) {
        setProgramType(ShaderManager.ProgramType.PrimitiveCircle);
        _radius = radius;
        _aaWidth = aaWidth;
        drawScaleXYRotateY(x, y, -radius, radius, rotateY);
    }
}
