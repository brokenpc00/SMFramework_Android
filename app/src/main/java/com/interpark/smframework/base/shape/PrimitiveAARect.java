package com.interpark.smframework.base.shape;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.shader.ProgPrimitiveAARect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PrimitiveAARect extends DrawNode {
    protected FloatBuffer uv;

    private float mRound, mBorder, mWidth, mHeight;

    public PrimitiveAARect(IDirector director) {
        this.director = director;
        drawMode = GLES20.GL_TRIANGLE_STRIP;
        numVertices = 4;

        final float[] v = {
                -1f, -1f,
                1f, -1f,
                -1f,  1f,
                1f,  1f,
        };

        final float[] uv = {
                0,  0,
                1,  0,
                0,  1,
                1,  1,
        };

        this.v = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(v);
        this.v.position(0);

        this.uv = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.put(uv);
        this.uv.position(0);

        setProgramType(ShaderManager.ProgramType.PrimitiveCircle);
    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            switch (program.getType()) {
                default:
                case PrimitiveAARect:
                    if( ((ProgPrimitiveAARect)program).setDrawParam(sMatrix, v, uv, mWidth, mHeight, mRound, mBorder)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                    break;
            }
        }
    }

    public void drawRect(float x, float y, float width, float height, float round, float border) {
        setProgramType(ShaderManager.ProgramType.PrimitiveAARect);
        mWidth = width;
        mHeight = height;
        mRound = round;
        mBorder = border;
        drawScale(x, y, Math.max(width, height));
    }
}
