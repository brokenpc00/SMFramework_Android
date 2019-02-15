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
                (tx+_w)/tw, (ty   )/th,
                (tx   )/tw, (ty+_h)/th,
                (tx+_w)/tw, (ty+_h)/th,
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
                    float cx = (tx + .5f*_w)/tw;
                    float cy = (ty + .5f*_h)/th;
                    float radius = .5f * Math.min(_w/tw, _h/th);
                    float border = 2.0f / tw;

                    if (((ProgSpriteCircle)program).setDrawParam(texture, sMatrix, v, uv, cx, cy, radius, border)) {
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
                -cx,         -cy+_h,
                -cx+_w*div1, -cy,
                -cx+_w*div1, -cy+_h,
                -cx+_w*div1, -cy,
                -cx+_w*div1, -cy+_h,
                -cx+_w*div2, -cy,
                -cx+_w*div2, -cy+_h,
                -cx+_w*div2, -cy,
                -cx+_w*div2, -cy+_h,
                -cx+_w,      -cy,
                -cx+_w,      -cy+_h,
        };
        final float[] uv = {
                (tx        )/tw, (ty   )/th,
                (tx        )/tw, (ty+_h)/th,
                (tx+_w*div1)/tw, (ty   )/th,
                (tx+_w*div1)/tw, (ty+_h)/th,
                (tx+_w*div1)/tw, (ty   )/th,
                (tx+_w*div1)/tw, (ty+_h)/th,
                (tx+_w*div2)/tw, (ty   )/th,
                (tx+_w*div2)/tw, (ty+_h)/th,
                (tx+_w*div2)/tw, (ty   )/th,
                (tx+_w*div2)/tw, (ty+_h)/th,
                (tx+_w     )/tw, (ty   )/th,
                (tx+_w     )/tw, (ty+_h)/th,
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
    /*
        private static final int NUM_SEGMENT = 50;

        public void convertCircle() {

            extra_numVertices = NUM_SEGMENT+2;
            extra_drawMode = GLES20.GL_TRIANGLE_FAN;

            float[] vertices = new float[extra_numVertices*2];
            float[] texCoord = new float[extra_numVertices*2];
            float tw = texture.getWidth();
            float th = texture.getHeight();
            float tx0 = tx + tw/2;
            float ty0 = ty + th/2;
            float radius = Math.min(tw, th)/2f;

            vertices[0] = 0; // 원 중앙 x
            vertices[1] = 0; // 원 중앙 y
            texCoord[0] = tx0/tw;
            texCoord[1] = ty0/th;

            int i = 0;
            int idx = 0;
            for (; i < NUM_SEGMENT; i++) {
                double rad = 2*i*Math.PI/NUM_SEGMENT;
                float rx = radius*(float)Math.cos(rad);
                float ry = radius*(float)Math.sin(rad);
                idx = (i+1)*2;
                vertices[idx]   = rx;
                vertices[idx+1] = ry;
                texCoord[idx]   = (tx0 + rx)/tw;
                texCoord[idx+1] = (ty0 + ry)/th;
            }
            // 마지막 점은 첫번쨰 점으로
            idx = (i+1)*2;
            vertices[idx]   = vertices[2];
            vertices[idx+1] = vertices[3];
            texCoord[idx]   = texCoord[2];
            texCoord[idx+1] = texCoord[3];

            extra_v = ByteBuffer.allocateDirect(vertices.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            extra_v.put(vertices);
            extra_v.position(0);

            extra_uv = ByteBuffer.allocateDirect(texCoord.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            extra_uv.put(texCoord);
            extra_uv.position(0);
        }

        public void drawCircle(float x, float y, float scale, float angle) {
            Matrix.setIdentityM(sMatrix, 0);
            Matrix.translateM(sMatrix, 0, x, y, 0);
            Matrix.scaleM(sMatrix, 0, scale, scale, 1);
            Matrix.rotateM(sMatrix, 0, angle, 0, 0, 1);

            _extra_draw(sMatrix);

        }
    */
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

    public void drawStretchHorizontal(float x, float y, float width, float scale) {

        Matrix.setIdentityM(sMatrix, 0);
        Matrix.translateM(sMatrix, 0, x, y, 0);
        Matrix.scaleM(sMatrix, 0, -scale, scale, 1);

        if(extra_v != null) {
            if (extra_numVertices == 12) {

                if (width < _w) width = _w;
                cx = width * (extra_divX1+(extra_divX2-extra_divX1)/2);
                float l = _w*extra_divX1;
                float r = _w*(1-extra_divX2);
                float space = (width - _w)/2;

                float[] v = {
                        -cx,               -cy,
                        -cx,               -cy+_h,
                        -cx+l,             -cy,
                        -cx+l,             -cy+_h,
                        -cx+l+space,       -cy,
                        -cx+l+space,       -cy+_h,
                        -cx+width-r-space, -cy,
                        -cx+width-r-space, -cy+_h,
                        -cx+width-r,       -cy,
                        -cx+width-r,       -cy+_h,
                        -cx+width,         -cy,
                        -cx+width,         -cy+_h,
                };
                extra_v.put(v);
                extra_v.position(0);

            } else {
                float space = (width > _w)?width-_w:0;

                extra_v.put(2*4, -cx+_w*extra_divX1+space);
                extra_v.put(2*5, -cx+_w*extra_divX1+space);
                extra_v.put(2*6, -cx+_w+space);
                extra_v.put(2*7, -cx+_w+space);
                extra_v.position(0);
            }
            _extra_draw(sMatrix);
        } else {
            _draw(sMatrix);
        }

    }


    public void changeStertchXY(float width, float height, float divX, float divY) {

        final float[] xpoint = { 0, _w*divX, width-_w*(1f-divX), width };
        final float[] xdiv   = { tx, tx+divX*_w,  tx+divX*_w, tx+_w };
        final float[] ypoint = { 0, _h*divY, height-_h*(1f-divY), height };
        final float[] ydiv   = { ty, ty+divY*_h,  ty+divY*_h, ty+_h };

        drawMode = GLES20.GL_TRIANGLES;
        numVertices = 9*2*3;

        float[] v = new float[numVertices*2];
        float[] uv = new float[numVertices*2];
        int vidx = 0;
        int uidx = 0;

        for( int i = 0; i < 3; i++ )
        {
            float x1, x2, y1, y2, u1, u2, v1, v2;
            x1 = (float)xpoint[ i   ];
            x2 = (float)xpoint[ i+1 ];
            u1 = xdiv[ i   ]/tw;
            u2 = xdiv[ i+1 ]/tw;
            for( int j = 0; j < 3; j++ )
            {
                y1 = (float)ypoint[ j   ];
                y2 = (float)ypoint[ j+1 ];
                v1 = ydiv[ j   ]/th;
                v2 = ydiv[ j+1 ]/th;

                v[vidx++] = x1;	v[vidx++] = y1;	uv[uidx++] = u1; uv[uidx++] = v1;
                v[vidx++] = x2;	v[vidx++] = y1;	uv[uidx++] = u2; uv[uidx++] = v1;
                v[vidx++] = x2;	v[vidx++] = y2;	uv[uidx++] = u2; uv[uidx++] = v2;

                v[vidx++] = x1;	v[vidx++] = y1;	uv[uidx++] = u1; uv[uidx++] = v1;
                v[vidx++] = x1;	v[vidx++] = y2;	uv[uidx++] = u1; uv[uidx++] = v2;
                v[vidx++] = x2;	v[vidx++] = y2; uv[uidx++] = u2; uv[uidx++] = v2;
            }
        }

        ByteBuffer byteBuf;

        this.v.clear();
        byteBuf = ByteBuffer.allocateDirect(v.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        this.v = byteBuf.asFloatBuffer();
        this.v.put(v);
        this.v.position(0);

        byteBuf = ByteBuffer.allocateDirect(uv.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        this.uv.clear();
        this.uv = byteBuf.asFloatBuffer();
        this.uv.put(uv);
        this.uv.position(0);
    }


    // for Texture Packer
    public void changeStertch3Seg(float width, float divX1, float divX2) {

        float gap = (width - _w)/2;
        float center = (divX2-divX1)*_w;
        final float[] xpoint = {
                0,
                _w*divX1,
                _w*divX1+gap,
                _w*divX1+gap+center,
                _w*divX1+gap+center+gap,
                width
        };
        final float[] xdiv = {
                tx,
                tx+divX1*_w,
                tx+divX1*_w,
                tx+divX2*_w,
                tx+divX2*_w,
                tx+_w
        };

        drawMode = GLES20.GL_TRIANGLE_STRIP;
        numVertices = 12;

        cx = width/2;
        cy = _h/2;

        float[] v = new float[numVertices*2];
        float[] uv = new float[numVertices*2];

        int index = 0;
        for( int i = 0; i < 6; i++ ) {
            v[index+0] = v[index+2] = xpoint[i]-width/2;
            v[index+1] = -_h/2;
            v[index+3] =  _h/2;

            uv[index+0] = uv[index+2] = xdiv[i]/tw;
            uv[index+1] = ty/th;
            uv[index+3] = (ty+_h)/th;

            index += 4;
        }

        ByteBuffer byteBuf;

        this.v.clear();
        byteBuf = ByteBuffer.allocateDirect(v.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        this.v = byteBuf.asFloatBuffer();
        this.v.put(v);
        this.v.position(0);

        byteBuf = ByteBuffer.allocateDirect(uv.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        this.uv.clear();
        this.uv = byteBuf.asFloatBuffer();
        this.uv.put(uv);
        this.uv.position(0);
    }
}
