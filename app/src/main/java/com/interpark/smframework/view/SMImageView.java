package com.interpark.smframework.view;

import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.base._UIContainerView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.BitmapTexture;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMImageView extends _UIContainerView {
    public enum ScaleType {
        /**
         * 가운데 정렬
         */
        CENTER,
        /**
         * 가운데 안쪽으로 정렬
         */
        CENTER_INSIDE,
        /**
         * 이미지가 View
         */
        CENTER_CROP,
        /**
         * View 크기에 맞춤 (비율무시)
         */
        FIT_XY,
        /**
         * View 크기에 맞추고 가운데 정렬(비율유지, 기본값)
         */
        FIT_CENTER,
    }

    protected boolean _iconVisible = true;
    protected DrawNode _sprite = null;
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    private float mImageScale = 1f;
    private float mScaleX, mScaleY;
    private final RectF mContentsBounds;
//    private Color4F _spriteColor = new Color4F(0, 0, 0, 0);

    public static SMImageView create(IDirector director, String assetName) {
        SMImageView imageView = new SMImageView(director, assetName);
        if (imageView!=null) {
            if (imageView.getContentSize().width==0 && imageView.getContentSize().height==0) {
                imageView.setContentSize(imageView.getSprite().getWidth(), imageView.getSprite().getHeight());
            }
        }
        return imageView;
    }
    public static SMImageView create(IDirector director, String assetName, float x, float y, float width, float height) {
        SMImageView view = new SMImageView(director, assetName);
        if (view!=null) {
            view.setContentSize(new Size(width, height));
            view.setPosition(x, y);
            view.setAnchorPoint(Vec2.ZERO);
        }
        return view;
    }

    public SMImageView (IDirector director) {
        super(director);
        mContentsBounds = new RectF();
    }

    public SMImageView(IDirector director, String assetName) {
        this(director);
        BitmapSprite sprite = BitmapSprite.createFromAsset(getDirector(), assetName, true, null);
        setSprite(sprite);
    }

    public SMImageView (IDirector director, DrawNode sprite) {
        this (director);
        setSprite(sprite);
    }

//    public SMImageView(IDirector director, Texture texture, float cx, float cy) {
//        this(director);
//        Sprite sprite = new Sprite(director, texture, cx, cy);
//        setSprite(sprite, true);
//    }

    public SMImageView(IDirector director, Texture texture) {
        this(director);
        Sprite sprite = new Sprite(director, texture, 0, 0);
        setSprite(sprite, true);
    }


    public SMImageView(IDirector director, float x, float y, float width, float height) {
        this(director, x, y, width, height, 0, 0);
    }

    public SMImageView(IDirector director, float x, float y, float width, float height, float anchorX, float anchorY) {
        this(director);
        setPosition(x, y);
        setContentSize(new Size(width, height));
    }


//    public SMImageView(IDirector director, float x, float y, float width, float height, float cx, float cy) {
//        this(director);
//        setPosition(x, y);
//        setContentSize(width, height);
////        setBounds(x, y, width, height, cx, cy);
//    }


    public void setSprite(DrawNode sprite) {
        setSprite(sprite, false);
    }

    public void setSprite(DrawNode sprite, boolean fitBounds) {
        _sprite = sprite;
        if (fitBounds) {
            fitSpriteBounds();
        } else {
            computeContentsBounds();
        }
    }

//    @Override
//    public void setBounds(float x, float y, float width, float height, float cx, float cy) {
//        super.setBounds(x, y, width, height, cx, cy);
//        computeContentsBounds();
//    }

    @Override
    public void setContentSize(Size size) {
        super.setContentSize(size);
        computeContentsBounds();
    }

    @Override
    public void setContentSize(float width, float height) {
        super.setContentSize(new Size(width, height));
        computeContentsBounds();
    }

    public DrawNode getSprite() {
        return _sprite;
    }

    public void fitSpriteBounds() {
        if (_sprite != null) {
            setPosition(getX(), getY());
            setContentSize(new Size(_sprite.getWidth(), _sprite.getHeight()));
        }
    }

    public void setScaleType(ScaleType scaleType) {
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            computeContentsBounds();
        }
    }

    @Override
    protected void render(float a) {
        if (_sprite == null)
            return;

        float x = mContentsBounds.left + _sprite.getCX()*mScaleX;
        float y = mContentsBounds.top + _sprite.getCY()*mScaleY;

        drawImage(x, y, mScaleX*mImageScale, mScaleY*mImageScale, a);
    }

    protected void drawImage(float x, float y, float scaleX, float scaleY, float a) {

        if (_tintColor == null) {
            _director.setColor(a, a, a, a);
        } else {
            _director.setColor(a*_tintColor[0], a*_tintColor[1], a*_tintColor[2], a*_tintColor[3]);
        }
        _sprite.drawScaleXY(x, y, scaleX, scaleY);
    }

    public void computeContentsBounds() {
        final float vw = _contentSize.width;
        final float vh = _contentSize.height;
//        final float vcx = getCX();
//        final float vcy = getCY();
        final float vcx = 0;
        final float vcy = 0;
        final float sw, sh;

        if (_sprite != null) {
            sw = _sprite.getWidth();
            sh = _sprite.getHeight();
        } else {
            sw = vw;
            sh = vh;
        }

        switch (mScaleType) {
            case CENTER:
            {
                mScaleX = mScaleY = 1;
            }
            break;
            case CENTER_INSIDE:
            {
                mScaleX = mScaleY = Math.min(1, Math.min(vw/sw, vh/sh));
            }
            break;
            case CENTER_CROP:
            {
                mScaleX = mScaleY = Math.max(vw/sw, vh/sh);
            }
            break;
            case FIT_XY:
            {
                mScaleX = vw/sw;
                mScaleY = vh/sh;
            }
            break;
            default:
            case FIT_CENTER:
                mScaleX = mScaleY = Math.min(vw/sw, vh/sh);
                break;
        }

        float x = (vw-sw*mScaleX)/2-vcx;
        float y = (vh-sh*mScaleY)/2-vcy;
        mContentsBounds.set(
                x,
                y,
                x + sw*mScaleX,
                y + sh*mScaleY);
    }

    public float getContentsScaleX() {
        return mScaleX;
    }

    public float getContentsScaleY() {
        return mScaleY;
    }

    public void getContentsBounds(RectF bounds) {
        if (bounds != null) {
            bounds.set(mContentsBounds);
        }
    }

    public float convertContentsXtoViewX(float x) {
        return x*mScaleX + mContentsBounds.left;
    }

    public float convertContentsYtoViewY(float y) {
        return y*mScaleY + mContentsBounds.top;
    }

    public float convertContentsScaleXtoViewScaleX(float scaleX) {
        return scaleX*mScaleX;
    }

    public float convertContentsScaleYtoViewScaleY(float scaleY) {
        return scaleY*mScaleY;
    }

    public float convertViewXtoContentsX(float x) {
        return (x - mContentsBounds.left)/mScaleX;
    }

    public float convertViewYtoContentsY(float y) {
        return (y - mContentsBounds.top)/mScaleY;
    }

    public float convertViewScaleXtoContentsScaleX(float scaleX) {
        return scaleX/mScaleX;
    }

    public float convertViewScaleYtoContentsScaleY(float scaleY) {
        return scaleY/mScaleY;
    }


    public void setImageScale(float imageScale) {
        mImageScale = imageScale;
    }

    private boolean mScissorTest = false;
    public void setScissorTest(boolean scissorTest) {
        mScissorTest = scissorTest;
    }

    @Override
    public void renderFrame(float alpha) {
        if (mScissorTest) {
            enableScissorTest(true);
            super.renderFrame(alpha);
            enableScissorTest(false);
        } else {
            super.renderFrame(alpha);
        }
    }

//    @Override
//    public void updateTintColor() {
//        _spriteColor.set(new Color4F(_tintColor));
//    }
}
