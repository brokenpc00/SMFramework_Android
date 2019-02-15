package com.interpark.smframework.base.shape;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ProgSprite;
import com.interpark.smframework.base.shape.ShapeConstant.LineType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PrimitiveLinearLine extends DrawNode {
    private static final float DEFAULT_THICKNESS = 20f;
    protected FloatBuffer uv;
    private float thickness;
    private ShapeConstant.LineType type;

    static float[] sVertices = new float[4*2];
    static float[] sTexCoord = new float[4*2];

    public PrimitiveLinearLine(IDirector director, Texture texture, float thickness, LineType type) {
        this.director = director;
        this.texture = texture;
        this.type = type;

        setProgramType(ShaderManager.ProgramType.Sprite);

        numVertices = 4;
        drawMode = GLES20.GL_TRIANGLE_STRIP;

        if (thickness <= 0) {
            this.thickness = DEFAULT_THICKNESS;
        } else {
            this.thickness = thickness;
        }

        this.v = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        sVertices[0] = 0;
        sVertices[1] = -thickness/2f;
        sVertices[2] =  thickness;
        sVertices[3] = -thickness/2f;
        sVertices[4] = 0;
        sVertices[5] = +thickness/2f;
        sVertices[6] =  thickness;
        sVertices[7] = +thickness/2f;
        v.put(sVertices).position(0);

        sTexCoord[0] = 0;
        sTexCoord[1] = 0;
        sTexCoord[2] = .5f;
        sTexCoord[3] = 0;
        sTexCoord[4] = 0;
        sTexCoord[5] = 1;
        sTexCoord[6] = .5f;
        sTexCoord[7] = 1;
        uv.put(sTexCoord).position(0);
    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            if( ((ProgSprite)program).setDrawParam(texture, sMatrix, v, uv) ) {
                GLES20.glDrawArrays(drawMode, 0, numVertices);
            }
        }
    }

    public void drawLine(float x1, float y1, float x2, float y2) {

        float dx = x2-x1;
        float dy = y2-y1;
        float width = (float)Math.sqrt(dx*dx+dy*dy);
        float angle = (float)Math.toDegrees(Math.atan2(dy, dx));

        sVertices[2] = width;
        sVertices[6] = width;
        v.put(sVertices).position(0);

        if (type == LineType.Dash) {
            sTexCoord[2] = width/thickness;
            sTexCoord[6] = width/thickness;
            uv.put(sTexCoord).position(0);
        }


        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x1, y1, 0);
        Matrix.rotateM(sMatrix, 0, angle, 0, 0, 1);

        _draw(sMatrix);

    }
}
