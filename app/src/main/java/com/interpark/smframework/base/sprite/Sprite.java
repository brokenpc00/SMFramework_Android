package com.interpark.smframework.base.sprite;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ProgSprite;
import com.interpark.smframework.shader.ProgSpriteCircle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Sprite extends DrawNode {
    protected float tx;
    protected float ty;
    protected float tw;
    protected float th;
    protected FloatBuffer uv;

    protected float extra_divX1;
    protected float extra_divX2;
    protected FloatBuffer extra_v;
    protected FloatBuffer extra_uv;
    protected int extra_numVertices;
    protected int extra_drawMode = GLES20.GL_TRIANGLE_STRIP;


    public Sprite(IDirector director) {
        this.director = director;
    }

    public Sprite(IDirector director, float w, float h, float cx, float cy, int tx, int ty, Texture texture) {
        initRect(director, w, h, cx, cy);
        setProgramType(ShaderManager.ProgramType.Sprite);

        this.tx = tx;
        this.ty = ty;
        this.tw = texture.getWidth();
        this.th = texture.getHeight();
        this.texture = texture;

        initTextureCoordQuard();

        texture.incRefCount();
    }

    public Sprite(IDirector director, Texture texture, float cx, float cy) {
        final float tw = texture.getWidth();
        final float th = texture.getHeight();

        initRect(director, tw, th, cx, cy);
        setProgramType(ShaderManager.ProgramType.Sprite);

        this.tx = 0;
        this.ty = 0;
        this.tw = tw;
        this.th = th;
        this.texture = texture;

        initTextureCoordQuard();

        texture.incRefCount();
    }

    protected void initTextureCoordQuard() {
        final float[] uv = {
                (tx   )/tw, (ty   )/th,
                (tx+_contentSize.width)/tw, (ty   )/th,
                (tx   )/tw, (ty+_contentSize.height)/th,
                (tx+_contentSize.width)/tw, (ty+_contentSize.height)/th,
        };

        this.uv = ByteBuffer.allocateDirect(uv.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.put(uv);
        this.uv.position(0);
    }

    protected void _draw(float[] modelMatrix) {

        ShaderProgram program = useProgram();
        if (program != null && texture != null) {
            switch (program.getType()) {
                default:
                case Sprite:
                    // 나중에 구현
//                case Bilateral:
//                case GeineEffect:
//                case GaussianBlur:
                    if (((ProgSprite)program).setDrawParam(texture, sMatrix, v, uv)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                    break;
                case SpriteCircle:
                {
                    float cx = (tx + .5f*_contentSize.width)/tw;
                    float cy = (ty + .5f*_contentSize.height)/th;
                    float radius = .5f * Math.min(_contentSize.width/tw, _contentSize.height/th);
                    float aaWidth = 2.0f / tw;

                    if (((ProgSpriteCircle)program).setDrawParam(texture, sMatrix, v, uv, cx, cy, radius, aaWidth)) {
                        GLES20.glDrawArrays(drawMode, 0, numVertices);
                    }
                }
                break;
            }
        }
    }

    public void removeTexture() {
        if (texture != null) {
            if (director.getTextureManager().removeTexture(texture)) {
                texture = null;
            }
        }
    }

    public void convertHotizontalResizable(float div1, float div2) {

        final float[] v = {
                -cx,         -cy,
                -cx,         -cy+_contentSize.height,
                -cx+_contentSize.width*div1, -cy,
                -cx+_contentSize.width*div1, -cy+_contentSize.height,
                -cx+_contentSize.width*div1, -cy,
                -cx+_contentSize.width*div1, -cy+_contentSize.height,
                -cx+_contentSize.width*div2, -cy,
                -cx+_contentSize.width*div2, -cy+_contentSize.height,
                -cx+_contentSize.width*div2, -cy,
                -cx+_contentSize.width*div2, -cy+_contentSize.height,
                -cx+_contentSize.width,      -cy,
                -cx+_contentSize.width,      -cy+_contentSize.height,
        };
        final float[] uv = {
                (tx        )/tw, (ty   )/th,
                (tx        )/tw, (ty+_contentSize.height)/th,
                (tx+_contentSize.width*div1)/tw, (ty   )/th,
                (tx+_contentSize.width*div1)/tw, (ty+_contentSize.height)/th,
                (tx+_contentSize.width*div1)/tw, (ty   )/th,
                (tx+_contentSize.width*div1)/tw, (ty+_contentSize.height)/th,
                (tx+_contentSize.width*div2)/tw, (ty   )/th,
                (tx+_contentSize.width*div2)/tw, (ty+_contentSize.height)/th,
                (tx+_contentSize.width*div2)/tw, (ty   )/th,
                (tx+_contentSize.width*div2)/tw, (ty+_contentSize.height)/th,
                (tx+_contentSize.width     )/tw, (ty   )/th,
                (tx+_contentSize.width     )/tw, (ty+_contentSize.height)/th,
        };

        extra_numVertices = 12;
        extra_divX1 = div1;
        extra_divX2 = div2;

        ByteBuffer byteBuf;

        byteBuf = ByteBuffer.allocateDirect(v.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        extra_v = byteBuf.asFloatBuffer();
        extra_v.put(v);
        extra_v.position(0);


        byteBuf = ByteBuffer.allocateDirect(uv.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        extra_uv = byteBuf.asFloatBuffer();
        extra_uv.put(uv);
        extra_uv.position(0);
    }

    protected void _extra_draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null && texture != null) {
            if (program.getType() == ShaderManager.ProgramType.Sprite) {
                if (((ProgSprite)program).setDrawParam(texture, sMatrix, extra_v, extra_uv)) {
                    GLES20.glDrawArrays(extra_drawMode, 0, extra_numVertices);
                }
            }
        }
    }

}
