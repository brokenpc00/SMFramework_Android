package com.interpark.smframework.view;

import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.base.UIContainerView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.BitmapTexture;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMImageView extends UIContainerView {

    public static final int GRAVITY_LEFT = 1;
    public static final int GRAVITY_RIGHT = 1<<1;
    public static final int GRAVITY_CENTER_HORIZONTAL = GRAVITY_LEFT | GRAVITY_RIGHT;
    public static final int GRAVITY_TOP = 1<<2;
    public static final int GRAVITY_BOTTOM = 1<<3;
    public static final int GRAVITY_CENTER_VERTICAL = GRAVITY_TOP | GRAVITY_BOTTOM;


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
    private ScaleType _scaleType = ScaleType.FIT_CENTER;

    protected void setClipping(boolean cliping) {

        if (_clipping==cliping) return;

        _clipping = cliping;
        if (_sprite!=null) {
            registerUpdate(FLAG_CONTENT_SIZE);
        }
    }
    private boolean _clipping = false;

    private static final long FLAG_CONTENT_SIZE = 1;
    private static final int ACTION_TAG_SHOW = AppConst.TAG.USER+1;
    private static final int ACTION_TAG_DIM = AppConst.TAG.USER+2;

    public void setGravity(int gravity) {
        setGravity(gravity, true);
    }
    public void setGravity(int gravity, boolean immediate) {
            _gravity = gravity;
            registerUpdate(FLAG_CONTENT_SIZE);
//        if (_gravity!=gravity) {
//            if (immediate) {
//            } else {
//
//            }
//        }
        }
// float scale
//    if (immediate ) {
//        _realScale = _newScale = scale;
//        _scaleX = _scaleY = _scaleZ = scale * _animScale;
//    } else {
//        if (_newScale==scale) {
//            return;
//        }
//
//        _newScale = scale;
//        scheduleSmoothUpdate(VIEWFLAG_SCALE);
//    }

    private int _gravity = 0;
    public void setMaxAreaRatio(float ratio) {_maxAreaRatio = ratio;}
    private float _maxAreaRatio = 0;
    private boolean _isDownloadImageView = false;
    private float _imageScale = 1f;
    private float _spriteScaleX = 1f, _spriteScaleY = 1f;
    private Vec2 _spritePosition = new Vec2(Vec2.ZERO);
    private Size _spriteSize = new Size(Size.ZERO);
    private Rect _imageRect = new Rect();

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

    private void updateData() {
        _imageRect = new Rect(new Vec2(0, 0), new Size(_sprite.getWidth(), _sprite.getHeight()));
        computeContentSize();
    }

    public void setSprite(DrawNode sprite) {
        setSprite(sprite, false);
    }
    public void setSprite(DrawNode sprite, boolean fitSize) {
        if (_sprite!=sprite) {
            if (_sprite!=null) {
                _sprite.releaseResources();
            }

            if (sprite!=null) {
        _sprite = sprite;
                _imageRect = new Rect(new Vec2(0, 0), _sprite.getContentSize());

                registerUpdate(FLAG_CONTENT_SIZE);
        } else {
                _sprite = null;
            }
        }

        if (fitSize && _sprite!=null) {
            _imageScale = 1;
            setContentSize(_sprite.getContentSize());
        }

        if (_onSpriteSetCallback!=null) {
            _onSpriteSetCallback.onSpriteSetCallback(this, _sprite);
        }
    }

    public interface OnSpriteSetCallback {
        public void onSpriteSetCallback(SMImageView view, DrawNode sprite);
        }
    private OnSpriteSetCallback _onSpriteSetCallback = null;
    public void setOnSpriteSetCallback(OnSpriteSetCallback callback) {_onSpriteSetCallback = callback;}

    public interface OnSpriteLoadedCallback {
        public DrawNode onSpriteLoadedCallback(SMImageView view, DrawNode sprite);
    }
    private OnSpriteLoadedCallback _onSpriteLoadedCallback = null;
    public void setOnSpriteLoadedCallback(OnSpriteLoadedCallback callback) {_onSpriteLoadedCallback = callback;}

    @Override
    public void setContentSize(Size size) {
        super.setContentSize(size);
        registerUpdate(FLAG_CONTENT_SIZE);
    }

    @Override
    public void setContentSize(float width, float height) {
        super.setContentSize(new Size(width, height));
        registerUpdate(FLAG_CONTENT_SIZE);
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
        if (_scaleType != scaleType) {
            _scaleType = scaleType;
            registerUpdate(FLAG_CONTENT_SIZE);
        }
    }

    @Override
    public void onUpdateOnVisit() {
        if (isUpdate(FLAG_CONTENT_SIZE)) {
            computeContentSize();
            unregisterUpdate(FLAG_CONTENT_SIZE);
        }
    }

    @Override
    protected void draw(float a) {
        if (_sprite == null)
            return;

//        float x = mContentsBounds.left + _sprite.getCX()*mScaleX;
//        float y = mContentsBounds.top + _sprite.getCY()*mScaleY;

        drawImage(_spritePosition.x, _spritePosition.y, _spriteSize.width*_imageScale, _spriteSize.height*_imageScale, a);
    }

    protected void drawImage(float x, float y, float scaleX, float scaleY, float a) {

        setRenderColor(a);

        _sprite.drawScaleXY(x, y, scaleX, scaleY);
    }

    public void computeContentSize() {
        if (_sprite==null) return;

        final Size vsize =  new Size(_uiContainer.getContentSize());

        if (vsize.width<=0 || vsize.height<=0) return;

        Size ssize = new Size(_imageRect.size);
        if (ssize.width<=0 || ssize.height<=0) return;

        float scaleX=1, scaleY=1;

        switch (_scaleType) {
            case CENTER:
            {
                scaleX = scaleY = 1;
            }
            break;
            case CENTER_INSIDE:
            {
                scaleX = scaleY = Math.min(1, Math.min(vsize.width/ssize.width, vsize.height/ssize.height));
            }
            break;
            case CENTER_CROP:
            {
                scaleX = scaleY = Math.max(vsize.width/ssize.width, vsize.height/ssize.height);
            }
            break;
            case FIT_XY:
            {
                scaleX = vsize.width/ssize.width;
                scaleY = vsize.height/ssize.height;
            }
            break;
            default:
            {
                scaleX = scaleY = Math.min(vsize.width/ssize.width, vsize.height/ssize.height);
            }
                break;
        }

        float sw = ssize.width * scaleX;
        float sh = ssize.height *scaleY;

        Vec2 origin = new Vec2(vsize.width/2-sw/2, vsize.height/2-sh/2);


        if (_gravity>0) {
            if ((_gravity & GRAVITY_CENTER_HORIZONTAL) > 0) {
                if ((_gravity & GRAVITY_LEFT) > 0 && (_gravity & GRAVITY_RIGHT)==0) {
                    // attach left
                    origin.x = 0;
                } else if ((_gravity & GRAVITY_RIGHT)>0 && (_gravity&GRAVITY_LEFT)==0) {
                    // attach right
                    origin.x = vsize.width - sw;
                }
            }

            if ((_gravity&GRAVITY_CENTER_VERTICAL)>0) {
                if ((_gravity & GRAVITY_TOP) > 0 && (_gravity & GRAVITY_BOTTOM)==0) {
                    // attach top
                    origin.y = 0;
                } else if ((_gravity & GRAVITY_BOTTOM)>0 && (_gravity&GRAVITY_TOP)==0) {
                    // attach bottom
                    origin.y = vsize.height - sh;
                }
            }
    }


        if (_clipping && (sw > vsize.width || sh > vsize.height)) {

        } else {

    }


        float w = _imageRect.size.width * scaleX;
        float h = _imageRect.size.height * scaleY;

        float x = origin.x;
        float y = origin.y;

        if (_maxAreaRatio>0 && _maxAreaRatio<1) {
            float ratio = (w*h) / (_contentSize.width*_contentSize.height);
            if (ratio>_maxAreaRatio) {
                float newScale = _maxAreaRatio / ratio;
                scaleX *= newScale;
                scaleY *= newScale;
            }
    }

        _spritePosition.set(x, y);
        _spriteSize.set(scaleX, scaleY);
        }

    private Vec2 _realSpritePosition = new Vec2(0, 0);
    private Vec2 _newSpritePosition = new Vec2(0, 0);
    private Vec2 _animSpritePosition = new Vec2(0, 0);
    private Size _animSpriteSize = new Size(0, 0);

    private Size _realSpriteSize = new Size(0, 0);
    private Size _newSpriteSize = new Size(0, 0);
    private Vec2 _newAnimSpritePosition = new Vec2(0, 0);
    private Size _newAnimSpriteSize = new Size(0, 0);

    public float getContentsScaleX() {
        return _spriteScaleX;
    }

    public float getContentsScaleY() {
        return _spriteScaleY;
    }

    public float convertContentsXtoViewX(float x) {
        return x*_spriteScaleX + _spritePosition.x;
    }

    public float convertContentsYtoViewY(float y) {
        return y*_spriteScaleY + _spritePosition.y;
    }

    public float convertContentsScaleXtoViewScaleX(float scaleX) {
        return scaleX*_spriteScaleX;
    }

    public float convertContentsScaleYtoViewScaleY(float scaleY) {
        return scaleY*_spriteScaleY;
    }

    public float convertViewXtoContentsX(float x) {
        return (x - _spritePosition.x)/_spriteScaleY;
    }

    public float convertViewYtoContentsY(float y) {
        return (y - _spritePosition.y)/_spriteScaleY;
    }

    public float convertViewScaleXtoContentsScaleX(float scaleX) {
        return scaleX/_spriteScaleX;
    }

    public float convertViewScaleYtoContentsScaleY(float scaleY) {
        return scaleY/_spriteScaleY;
    }


    public void setImageScale(float imageScale) {
        _imageScale = imageScale;
    }

}
