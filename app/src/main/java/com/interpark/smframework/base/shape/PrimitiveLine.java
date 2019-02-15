package com.interpark.smframework.base.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.shader.ProgPrimitive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PrimitiveLine extends DrawNode {
    private static float[] sVertices = new float[2*2];

    public PrimitiveLine(IDirector director) {
        this.director = director;
        setProgramType(ShaderManager.ProgramType.Primitive);
        drawMode = GLES20.GL_LINES;
        numVertices = 2;
        this.v = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.position(0);
    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            if( ((ProgPrimitive)program).setDrawParam(sMatrix, v) ) {;
                GLES20.glDrawArrays(drawMode, 0, numVertices);
            }
        }
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        sVertices[0] = 0;
        sVertices[1] = 0;
        sVertices[2] = (x2-x1);
        sVertices[3] = (y2-y1);
        v.put(sVertices);
        v.position(0);

        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x1, y1, 0);

        _draw(sMatrix);
    }
}
