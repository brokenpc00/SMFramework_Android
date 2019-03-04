package com.interpark.smframework.base.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ProgPrimitive;
import com.interpark.smframework.shader.ProgPrimitiveTriangle;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.util.Vec2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PrimitiveTriangle extends DrawNode {
    public PrimitiveTriangle(IDirector director, float width, float height) {
        this.director = director;
        _w = width;
        _h = height;

        final float[] v = { 0, 0,
                            _w, 0,
                            0, _h,
                            _w, _h
        };
//        final float[] v = {
//                0, 0,
//                1, 0,
//                0, 1,
//                1, 1
//        };

        drawMode = GLES20.GL_TRIANGLE_STRIP;
        numVertices = 4;

        this.v = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(v);
        this.v.position(0);


        this.uv = ByteBuffer.allocateDirect(_texCoordConst[Quadrant_ALL].length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.put(_texCoordConst[Quadrant_ALL]);
        this.uv.position(0);

        setProgramType(ShaderManager.ProgramType.PrimitiveTriangle);
    }

    protected FloatBuffer uv;

    private Vec2 _p0 = new Vec2(Vec2.ZERO);
    private Vec2 _p1 = new Vec2(Vec2.ZERO);
    private Vec2 _p2 = new Vec2(Vec2.ZERO);
    private float _aaWidth = 0.015f;

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program!=null) {
            switch (program.getType()) {
                default:
                case PrimitiveTriangle:
                {
                    if (((ProgPrimitiveTriangle)program).setDrawParam(sMatrix, v, uv, _p0, _p1, _p2, _aaWidth)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                }
                break;
            }
        }
    }

    public void drawTrinalge(Vec2 p0, Vec2 p1, Vec2 p2, float aaWidth) {

        setProgramType(ShaderManager.ProgramType.PrimitiveTriangle);
        _p0.set(p0.x/_w, p0.y/_h);
        _p1.set(p1.x/_w, p1.y/_h);
        _p2.set(p2.x/_w, p1.y/_h);
        _aaWidth = aaWidth;

        draw(0, 0);
    }

}
