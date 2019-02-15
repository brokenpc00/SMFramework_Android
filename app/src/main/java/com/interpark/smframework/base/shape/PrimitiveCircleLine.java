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

public class PrimitiveCircleLine extends DrawNode {
    private static final int NUM_SEGMENT = 100;
    private static final float DEFAULT_THICKNESS = 20f;
    protected FloatBuffer uv;

    public PrimitiveCircleLine(IDirector director, Texture texture, float radius, float thickness, LineType type) {
        this.director = director;
        this.texture = texture;

        setProgramType(ShaderManager.ProgramType.Sprite);

        numVertices = (1+NUM_SEGMENT) * 2;
        drawMode = GLES20.GL_TRIANGLE_STRIP;

        if (thickness < 0) {
            thickness = DEFAULT_THICKNESS;
        }


        float inRadius  = radius - thickness/2;
        float outRadius = radius + thickness/2;
        int idx = 0;

        float[] vertices = new float[numVertices*2];
        float[] texCoord = new float[numVertices*2];

        float uu;
        if (type == LineType.Solid) {
            // 실선
            uu = 0.5f;
            for (int i = 0; i < NUM_SEGMENT; i++) {
                double r = i*2*Math.PI/NUM_SEGMENT;
                double ca = Math.cos(r);
                double sa = Math.sin(r);
                vertices[idx+0] = (float)(inRadius * ca);
                vertices[idx+1] = (float)(inRadius * sa);
                vertices[idx+2] = (float)(outRadius * ca);
                vertices[idx+3] = (float)(outRadius * sa);
                texCoord[idx+0] = uu;
                texCoord[idx+1] = 0;
                texCoord[idx+2] = uu;
                texCoord[idx+3] = 1;
                idx += 4;
            }
        } else {
            // 점선
            uu = 0;
            float ud = (float)(((2*radius*Math.PI)/NUM_SEGMENT)/thickness);
            for (int i = 0; i < NUM_SEGMENT; i++) {
                double r = i*2*Math.PI/NUM_SEGMENT;
                double ca = Math.cos(r);
                double sa = Math.sin(r);
                vertices[idx+0] = (float)(inRadius * ca);
                vertices[idx+1] = (float)(inRadius * sa);
                vertices[idx+2] = (float)(outRadius * ca);
                vertices[idx+3] = (float)(outRadius * sa);
                texCoord[idx+0] = uu;
                texCoord[idx+1] = 0;
                texCoord[idx+2] = uu;
                texCoord[idx+3] = 1;
                idx += 4;
                uu += ud*1.5f;
            }
        }

        vertices[idx+0] = vertices[0];
        vertices[idx+1] = vertices[1];
        vertices[idx+2] = vertices[2];
        vertices[idx+3] = vertices[3];
        texCoord[idx+0] = uu;
        texCoord[idx+1] = 0;
        texCoord[idx+2] = uu;
        texCoord[idx+3] = 1;


        this.v = ByteBuffer.allocateDirect(vertices.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(vertices);
        this.v.position(0);

        this.uv = ByteBuffer.allocateDirect(texCoord.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.put(texCoord);
        this.uv.position(0);

    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            if( ((ProgSprite)program).setDrawParam(texture, sMatrix, v, uv) ) {
                GLES20.glDrawArrays(drawMode, 0, numVertices);
            }
        }
    }

    protected void _drawPercent(float[] modelMatrix, int _start, int _end) {
        ShaderProgram program = useProgram();
        if (program != null) {
            if( ((ProgSprite)program).setDrawParam(texture, sMatrix, v, uv) ) {
                GLES20.glDrawArrays(drawMode, _start*2, (_end-_start)*2);
            }
        }
    }

//	public void drawPie(float x, float y, float pieRatio) {
//		Matrix.setIdentityM(sMatrix, 0);
//        Matrix.translateM(sMatrix, 0, x, y, 0);
//        int _numVertices = 2*(int)(pieRatio*(numVertices/2));
//        _numVertices %= numVertices;
//
//        _drawPercent(sMatrix, _numVertices);
//	}

    public void drawPie(float x, float y, float start, float end) {
        int _start = (int)(start*(numVertices/2));
        int _end   = (int)(end  *(numVertices/2));
        if (_start == _end) {
            return;
        }
        if (_start > _end) {
            int t = _start;
            _start = _end;
            _end = t;
        }
        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        _drawPercent(sMatrix, _start, _end);
    }

    public void drawPieScaleXY(float x, float y, float start, float end, float scaleX, float scaleY) {
        int _start = (2*(int)(start*(numVertices/2)))%numVertices;
        int _end = (2*(int)(end*(numVertices/2)))%numVertices;
        if (_start == _end) {
            return;
        }
        if (_end > _start) {
            int t = _start;
            _start = _end;
            _end = t;
        }

        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.scaleM(sMatrix, 0, scaleX, scaleY, 1);

        _drawPercent(sMatrix, _start, _end);
    }
}
