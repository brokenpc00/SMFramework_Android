package com.interpark.smframework.base.sprite;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ProgGeineEffect;
import com.interpark.smframework.shader.ProgGeineEffect2;
import com.interpark.smframework.shader.ProgSprite;
import com.interpark.smframework.shader.ShaderManager.ProgramType;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.util.Vec2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static com.interpark.smframework.SMDirector.getDirector;

public class GridSprite extends Sprite {
    public static final int DEFAULT_GRID_SIZE = 10;


    private ShortBuffer mIndices;
    private int mNumVertices;
    private int mBufferSize;
    private int mNumFaces;

    protected float mGridSize;
    protected int mNumCol;
    protected int mNumRow;
    protected float[] vertices;

    public int getNumCols() {
        return mNumCol;
    }

    public int getNumRows() {
        return mNumRow;
    }

    public float[] getVertexBuffer() {
        return vertices;
    }

    public int getNumFaces() {
        return mNumFaces;
    }

    public ShortBuffer getIndices() {
        return mIndices;
    }

    public static GridSprite create(IDirector director, Sprite sprite) {
        if (sprite instanceof GridSprite) {
            return (GridSprite)sprite;
        } else {
            Texture texture = sprite.getTexture();
//            return new GridSprite(director, texture, sprite.getWidth()/2, sprite.getHeight()/2, DEFAULT_GRID_SIZE);
            return new GridSprite(director, texture, 0, 0, DEFAULT_GRID_SIZE);
        }
    }

    public GridSprite(IDirector director, Texture texture, float cx, float cy, int gridSize) {
        super(director);

        final float tw = texture.getWidth();
        final float th = texture.getHeight();

        initRect(director, tw, th, cx, cy);
        setProgramType(ProgramType.Sprite);

        this.tx = 0;
        this.ty = 0;
        this.tw = tw;
        this.th = th;
        this.texture = texture;
        mGridSize = Math.max(10, gridSize);

        initTextureCoordQuard();

        texture.incRefCount();
    }

    @Override
    protected void initTextureCoordQuard() {

        drawMode = GLES20.GL_TRIANGLES;

        mNumCol = (int)Math.ceil(_contentSize.width / mGridSize);
        mNumRow = (int)Math.ceil(_contentSize.height / mGridSize);
        mNumVertices = (mNumCol+1) * (mNumRow+1);
        mBufferSize = mNumVertices * 2;

        // create vertex & texture buffer
        vertices = new float[mBufferSize];
        float[] texcoord = new float[mBufferSize];

        int idx = 0;
        float xx, yy, uu, vv;

        for (int y = 0; y <= mNumRow; y++) {
            if (y == mNumRow) {
                yy = _contentSize.height;
                vv = _contentSize.height/th;
            } else {
                yy = mGridSize * y;
                vv = (float)y * mGridSize /  _contentSize.height;
            }
            for (int x = 0; x <= mNumCol; x++) {
                if (x == mNumCol) {
                    xx = _contentSize.width;
                    uu = _contentSize.width/tw;
                } else {
                    xx = mGridSize * x;
                    uu = (float)x * mGridSize / _contentSize.width;
                }
                vertices[idx  ] = xx-cx; // x coord
                vertices[idx+1] = yy-cy; // y coord
                texcoord[idx  ] = uu;
                texcoord[idx+1] = vv;

                idx += 2;
            }
        }

        int size = mBufferSize * Float.SIZE / Byte.SIZE;
        v = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        v.put(vertices);
        v.position(0);

        uv = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        uv.put(texcoord);
        uv.position(0);

        // crete index buffer
        int numQuads = mNumCol * mNumRow;
        mNumFaces = numQuads * 2;

        size = mNumFaces * 3 * 2 * Short.SIZE / Byte.SIZE;
        mIndices = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();

        int vi = 0;	// vertex index
        short ll, lr, ul, ur;

        for(int index = 0; index < numQuads; index++) {
            int rowNum = index / mNumCol;
            int colNum = index % mNumCol;
            ll = (short)(rowNum * (mNumCol+1) + colNum);
            lr = (short)(ll + 1);
            ul = (short)((rowNum + 1) * (mNumCol+1) + colNum);
            ur = (short)(ul + 1);
            QuadToTrianglesWindCCWSet(mIndices, vi, ul, ur, ll, lr);
            vi += 6;
        }
        mIndices.position(0);
    }

    @Override
    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null && texture != null) {
            switch (program.getType()) {
                default:
                case Sprite:
                    if (((ProgSprite)program).setDrawParam(texture, sMatrix, v, uv)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a);
                        }

                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumFaces*3, GLES20.GL_UNSIGNED_SHORT, mIndices);
                    }
                    break;
                case GeineEffect:
                    if (((ProgGeineEffect)program).setDrawParam(texture, sMatrix, v, uv)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a);
                        }
                        ((ProgGeineEffect)program).setGeineValue(mGenieMinimize, mGenieBend, mGenieSide);
                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumFaces*3, GLES20.GL_UNSIGNED_SHORT, mIndices);
                    }
                    break;
                case GeineEffect2:
                    if (((ProgGeineEffect2)program).setDrawParam(texture, sMatrix, v, uv)) {
                        if (_setColor) {
                            getDirector().setColor(_color.r, _color.g, _color.b, _color.a);
                        }
                        ((ProgGeineEffect2)program).setGeineValue(_genieAnchor, _genieProgress);
                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumFaces*3, GLES20.GL_UNSIGNED_SHORT, mIndices);
                    }
                    break;
            }
        }
    }

    static void QuadToTrianglesWindCCWSet(ShortBuffer vertex, int pos, short ul, short ur, short ll, short lr) {

        vertex.position(pos);
        vertex.put(lr);
        vertex.put(ul);
        vertex.put(ll);
        vertex.put(lr);
        vertex.put(ur);
        vertex.put(ul);
    }

    private Vec2 _genieAnchor = new Vec2();
    private float _genieProgress = 0;

    public void setGenieAnchor(final Vec2 v) {
        _genieAnchor.set(v);
    }
    public void setGenieProgress(final float f) {
        _genieProgress = f;
    }

    private float mGenieMinimize = 0;
    private float mGenieBend = 0;
    private float mGenieSide = 0;
    public void setGeineValue(float minimize, float bend, float side) {
        mGenieMinimize = minimize;
        mGenieBend = bend;
        mGenieSide = side;
    }

    //	private static final float GROWSTEP = 0.2f;
    // http://www.codeproject.com/Articles/182242/Transforming-Images-for-Fun-A-Local-Grid-based-Ima
    public void grow(float px, float py, float value, float step, float radius) {

        float sx, sy, dx, dy;
        float r;
        int idx;
        float growStep = Math.abs(value*step);

        idx = 0;
        for (int y = 0; y <= mNumRow; y++) {
            if (y == mNumRow) {
                sy = _contentSize.height-cy;
            } else {
                sy = mGridSize * y-cy;
            }
            for (int x = 0; x <= mNumCol; x++) {
                if (x == mNumCol) {
                    sx = _contentSize.width-cx;
                } else {
                    sx = mGridSize * x-cx;
                }

                dx = sx - px;
                dy = sy - py;
                r = (float)Math.sqrt(dx*dx+dy*dy)/radius;
                r = (float)Math.pow(r, growStep);

                if (value > 0 && r > .001) {
                    vertices[idx  ] = px + dx/r;
                    vertices[idx+1] = py + dy/r;
                } else if (value < 0 && r > .001) {
                    vertices[idx  ] = px + dx*(r);
                    vertices[idx+1] = py + dy*(r);
                } else {
                    vertices[idx  ] = sx;
                    vertices[idx+1] = sy;
                }
                idx += 2;
            }
        }

        v.put(vertices);
        v.position(0);
    }
}
