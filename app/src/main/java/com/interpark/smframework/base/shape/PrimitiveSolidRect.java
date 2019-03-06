package com.interpark.smframework.base.shape;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ProgPrimitiveSolidRect;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PrimitiveSolidRect extends DrawNode {
    protected FloatBuffer uv;

    private float mRound, mAAWidth, mWidth, mHeight;

    public PrimitiveSolidRect(IDirector director) {
        this.director = director;
        drawMode = GLES20.GL_TRIANGLE_STRIP;
        numVertices = 4;

        final float[] v = {
                -1f, -1f,
                1f, -1f,
                -1f,  1f,
                1f,  1f,
        };


        this.v = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(v);
        this.v.position(0);

        this.uv = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.put(_texCoordConst[Quadrant_ALL]);
        this.uv.position(0);

        setProgramType(ShaderManager.ProgramType.PrimitiveSolidRect);
    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            switch (program.getType()) {
                default:
                case PrimitiveSolidRect:
                    if( ((ProgPrimitiveSolidRect)program).setDrawParam(sMatrix, v, uv, mWidth, mHeight, mRound, mAAWidth)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                    break;
            }
        }
    }

    public void drawRect(float x, float y, float width, float height, float round, float aaWidth) {
        setProgramType(ShaderManager.ProgramType.PrimitiveSolidRect);
        mWidth = width;
        mHeight = height;
        mRound = round;
        mAAWidth = aaWidth;
        drawScale(x, y, Math.max(width, height));
    }
}
