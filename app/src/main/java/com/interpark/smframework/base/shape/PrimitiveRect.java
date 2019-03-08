package com.interpark.smframework.base.shape;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.shader.ProgPrimitive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PrimitiveRect extends DrawNode {
    public PrimitiveRect(IDirector director, float w, float h, float cx, float cy) {
        this(director, w, h, cx, cy, true);
    }

    public PrimitiveRect(IDirector director, float w, float h, float cx, float cy, boolean fill) {
        setProgramType(ShaderManager.ProgramType.Primitive);
        if (fill) {
            initRect(director, w, h, cx, cy);
        } else {
            initRectHollow(director, w, h, cx, cy);
        }
    }

    protected void initRectHollow(IDirector director,float w, float h, float cx, float cy) {
        this.director = director;
        this._contentSize.width = w;
        this._contentSize.height = h;
        this.cx = cx;
        this.cy = cy;
        initVertexHollowQuad();
    }

    protected void initVertexHollowQuad() {
        final float[] v = {
                -cx,    -cy,
                -cx+_contentSize.width, -cy,
                -cx+_contentSize.width, -cy+_contentSize.height,
                -cx,    -cy+_contentSize.height,
                -cx,    -cy,
        };

        drawMode = GLES20.GL_LINE_STRIP;
        numVertices = 5;

        this.v = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(v);
        this.v.position(0);
    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            if( ((ProgPrimitive)program).setDrawParam(sMatrix, v) ) {
                GLES20.glDrawArrays(drawMode, 0, numVertices);
            }
        }
    }
}
