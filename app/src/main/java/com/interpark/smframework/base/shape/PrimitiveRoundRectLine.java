package com.interpark.smframework.base.shape;

import android.opengl.GLES20;

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

public class PrimitiveRoundRectLine extends DrawNode {
    private static final int CONER_SEGMENT = 10;
    private static final int NUM_VERTICES = (CONER_SEGMENT+1)*2*4+2;
    protected FloatBuffer uv;

    private float[] vertices;
    private float[] texcoord;
    private LineType lineType;
    private float thickness;

    public PrimitiveRoundRectLine(IDirector director, Texture texture, float thickness, LineType lineType) {
        this.director = director;
        this.texture = texture;

        setProgramType(ShaderManager.ProgramType.Sprite);

        numVertices = NUM_VERTICES;
        drawMode = GLES20.GL_TRIANGLE_STRIP;
        this.lineType = lineType;
        this.thickness = thickness;

        vertices = new float[NUM_VERTICES*2];
        texcoord = new float[NUM_VERTICES*2];

        this.v = ByteBuffer.allocateDirect(vertices.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.position(0);

        this.uv = ByteBuffer.allocateDirect(texcoord.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.position(0);

        for (int i = 0; i < NUM_VERTICES*2; i += 4) {
            texcoord[i+0] = .5f;
            texcoord[i+1] = 0f;
            texcoord[i+2] = .5f;
            texcoord[i+3] = 1f;
        }
        this.uv.put(texcoord);
        this.uv.position(0);
    }

    public void setSize(float width, float height, float conerRadius) {

        float inR = conerRadius - thickness/2f;
        float outR = conerRadius + thickness/2f;
        float w = width/2-conerRadius;
        float h = height/2-conerRadius;
        float textureRoundLength = (float)(0.25*2*conerRadius*Math.PI)/thickness;
        float textureWidthLength = (float)(width-2*conerRadius)/thickness;
        float textureHeightLength = (float)(height-2*conerRadius)/thickness;
        float stepRoundLength = textureRoundLength / CONER_SEGMENT;

        int index = 0;
        float tu = 0;
        for (int i = 0; i <= CONER_SEGMENT; i++) {
            double rad = i*Math.PI*0.5/CONER_SEGMENT;
            float ca = (float)Math.cos(rad);
            float sa = (float)Math.sin(rad);

            float inA = inR*ca;
            float inB = inR*sa;
            float outA = outR*ca;
            float outB = outR*sa;

            // left-top
            index = i*4;
            vertices[index+0] = -w-inA;
            vertices[index+1] = -h-inB;
            vertices[index+2] = -w-outA;
            vertices[index+3] = -h-outB;
            if (lineType == LineType.Dash) {
                tu = i*stepRoundLength;
                texcoord[index+0] = texcoord[index+2] = tu;
            }

            // right-top
            index += (CONER_SEGMENT+1)*4;
            vertices[index+0] = +w+inB;
            vertices[index+1] = -h-inA;
            vertices[index+2] = +w+outB;
            vertices[index+3] = -h-outA;
            if (lineType == LineType.Dash) {
                tu += textureWidthLength+textureRoundLength;
                texcoord[index+0] = texcoord[index+2] = tu;
            }

            // right-bottom
            index += (CONER_SEGMENT+1)*4;
            vertices[index+0] = +w+inA;
            vertices[index+1] = +h+inB;
            vertices[index+2] = +w+outA;
            vertices[index+3] = +h+outB;
            if (lineType == LineType.Dash) {
                tu += textureHeightLength+textureRoundLength;
                texcoord[index+0] = texcoord[index+2] = tu;
            }

            // left_bottom
            index += (CONER_SEGMENT+1)*4;
            vertices[index+0] = -w-inB;
            vertices[index+1] = +h+inA;
            vertices[index+2] = -w-outB;
            vertices[index+3] = +h+outA;
            if (lineType == LineType.Dash) {
                tu += textureWidthLength+textureRoundLength;
                texcoord[index+0] = texcoord[index+2] = tu;
            }
        }
        index += 4;
        vertices[index+0] = vertices[0];
        vertices[index+1] = vertices[1];
        vertices[index+2] = vertices[2];
        vertices[index+3] = vertices[3];
        if (lineType == LineType.Dash) {
            tu += textureHeightLength;
            texcoord[index+0] = texcoord[index+2] = tu;
        }

        this.v.put(vertices);
        this.v.position(0);
        if (lineType == LineType.Dash) {
            this.uv.put(texcoord);
            this.uv.position(0);
        }
    }


    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {
            if( ((ProgSprite)program).setDrawParam(texture, sMatrix, v, uv) ) {
                GLES20.glDrawArrays(drawMode, 0, numVertices);
            }
        }
    }
}
