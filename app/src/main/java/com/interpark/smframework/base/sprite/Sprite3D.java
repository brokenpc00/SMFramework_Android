package com.interpark.smframework.base.sprite;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ProgSprite3D;
import com.interpark.smframework.shader.ShaderManager.ProgramType;
import com.interpark.smframework.shader.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sprite3D extends DrawNode {
    protected ShortBuffer indexb;
    protected FloatBuffer colorb;
    protected FloatBuffer textureb;
    protected int numTriangles;

    public Sprite3D(IDirector director, float[] vertices, short[] indices, float[] colors) {//, float texCoord, int[] textureId) {
        this.director = director;
        this._contentSize.width = 1;
        this._contentSize.height = 1;
        this.cx = .5f;
        this.cy = .5f;

        setProgramType(ProgramType.Sprite3D);

        drawMode = GLES20.GL_TRIANGLES;
        numVertices = vertices.length/3;
        numTriangles = indices.length/3;

        initVertexBuffer(vertices);
        initIndexBuffer(indices);
        initColorBuffer(colors);
    }

    public Sprite3D(IDirector director, float[] vertices, float[] colors, float[] texCoord, Texture texture, short[] indices) {
        this.director = director;
        this._contentSize.width = 1;
        this._contentSize.height = 1;
        this.cx = .5f;
        this.cy = .5f;
        this.texture = texture;

        setProgramType(ProgramType.Sprite3D);

//		drawMode = GLES20.GL_TRIANGLE_STRIP;
//		numVertices = vertices.length/3;
        drawMode = GLES20.GL_TRIANGLES;
        numVertices = vertices.length/3;
        numTriangles = indices.length/3;

        initVertexBuffer(vertices);
        initColorBuffer(colors);
        initTextureBuffer(texCoord);
        initIndexBuffer(indices);
    }


    protected void initVertexBuffer(float[] vertices) {
        this.v = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(vertices);
        this.v.position(0);
    }

    protected void initIndexBuffer(short[] indices) {
        this.indexb = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        this.indexb.put(indices);
        this.indexb.position(0);
    }

    protected void initColorBuffer(float[] colors) {
        this.colorb = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.colorb.put(colors);
        this.colorb.position(0);
    }

    private void initTextureBuffer(float[] texCoord) {
        this.textureb = ByteBuffer.allocateDirect(texCoord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.textureb.put(texCoord);
        this.textureb.position(0);
    }


    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null && texture != null) {
            if (((ProgSprite3D)program).setDrawParam(texture, sMatrix, v, textureb, colorb)) {
                if (drawMode == GLES20.GL_TRIANGLE_STRIP) {
                    GLES20.glDrawArrays(drawMode, 0, numVertices);
                } else {
                    GLES20.glDrawElements(drawMode, numTriangles*3, GLES20.GL_UNSIGNED_SHORT, indexb);
                }
            }
        }
    }
}
