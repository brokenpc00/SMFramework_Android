package com.interpark.app.scene.StickerLayer;

import android.graphics.Shader;
import android.view.MotionEvent;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.GridSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.shader.ShaderNode;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.view.SMRectView;
import com.interpark.smframework.view.SMZoomView;
import com.interpark.smframework.view.Sticker.Sticker;
import com.interpark.smframework.view.Sticker.StickerCanvasView;
import com.interpark.smframework.view.Sticker.StickerControlView;

import java.util.ArrayList;

public class StickerLayer extends SMView implements StickerCanvasView.StickerCanvasListener, StickerControlView.StickerControlListener {
    public StickerLayer(IDirector director) {
        super(director);
    }

    public static StickerLayer create(IDirector director, Size contentSize) {
        StickerLayer layer = new StickerLayer(director);
        layer._contentSize.set(contentSize);
        layer.init();
        return layer;
    }

    public static StickerLayer create(IDirector director, Sprite sprite, Size contentSize) {
        StickerLayer layer = new StickerLayer(director);
        layer._contentSize.set(contentSize);
        layer.initWithSprite(sprite);
        return layer;
    }

    @Override
    protected boolean init() {
        if (!super.init()) {
            return false;
        }

//        Size s = getDirector().getWinSize();
        Size s = getContentSize();
//        s.set(s.width, s.height-AppConst.SIZE.BOTTOM_MENU_HEIGHT);
//        setContentSize(s);

        // sticker boad
        _zoomView = SMZoomView.create(getDirector());
        _zoomView.setContentSize(s);
        _zoomView.setPadding(20.0f);
        addChild(_zoomView);

        // stricker controller
        _controlView = StickerControlView.create(getDirector());
        _controlView.setContentSize(s);
        _controlView.setStickerListener(this);
        addChild(_controlView);

        // sticker container
        _contentView = SMView.create(getDirector());
        _contentView.setBackgroundColor(new Color4F(1, 1, 1, 0.6f));
        if (_gridSprite!=null) {
            // can set fatvalue
            _contentView.setContentSize(_gridSprite.getContentSize());
        } else {
            // normal sprite
            _contentView.setContentSize(s);
        }
        _zoomView.setContentView(_contentView);

        if (_gridSprite!=null) {
            _bgImageView = SMImageView.create(getDirector(),  _gridSprite);
            _bgImageView.setAnchorPoint(Vec2.MIDDLE);
            _bgImageView.setContentSize(_gridSprite.getContentSize());
            _bgImageView.setPosition(_contentView.getContentSize().divide(2));
        } else {
            _bgImageView = SMImageView.create(getDirector());
            _bgImageView.setAnchorPoint(Vec2.MIDDLE);
            _bgImageView.setContentSize(s);
            _bgImageView.setPosition(s.divide(2));
        }

        _bgImageView.setBackgroundColor(new Color4F(1, 0, 0, 0.4f));

        _contentView.addChild(_bgImageView);

        // bgimage guide
        SMRectView rect = SMRectView.create(getDirector());
        if (_gridSprite!=null) {
            rect.setContentSize(_gridSprite.getContentSize());
        } else {
            rect.setContentSize(s);
        }
        rect.setLineWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2);
        rect.setColor(Color4F.XDBDCDF);
        _bgImageView.addChild(rect);

        // sticker canvas
        _canvasView = StickerCanvasView.create(getDirector());
        _canvasView.setContentSize(s);
        _canvasView.setAnchorPoint(Vec2.MIDDLE);
        _canvasView.setPosition(_contentView.getContentSize().divide(2));
        _canvasView.setStickerListener(this);
        _contentView.addChild(_canvasView);

        return true;
    }

    public boolean initWithSprite(Sprite sprite) {
        if (sprite!=null) {
            _gridSprite = GridSprite.create(getDirector(), sprite);
        }

        return init();
    }

    public void setStickerListener(StickerCanvasView.StickerCanvasListener canvasListener, StickerControlView.StickerControlListener controlListener) {
        _canvasListener = canvasListener;
        _controlListener = controlListener;
    }

    // trash animation
    public void startGeineRemove(SMView view) {
        if (view instanceof StickerItemView) {
            StickerItemView sticker = (StickerItemView)view;

            Sprite sprite = (Sprite)sticker.getSprite();
            if (sprite==null) {
                return;
            }

            _controlView.startGeineRemove(view);
            _canvasView.removeChildWithGenieAction(sticker, sprite, new Vec2(0.0f, 1.0f), 0.5f, 0.01f);
        }
    }

    public void addSticker(SMView sticker) {
        if (sticker!=null) {
            _canvasView.addChild(sticker);
        }
    }

    public void addStickerAboveAt(SMView sticker, SMView aboveAt) {
        if (sticker!=null) {
            _canvasView.addChild(sticker);
            reorderStickerAboveAt(sticker, aboveAt);
        }
    }

    public void reorderStickerAboveAt(SMView sticker, SMView aboveAt) {
        if (aboveAt!=null) {
            _canvasView.aboveView(sticker, aboveAt);
        } else {
            _canvasView.sendChildToBack(sticker);
        }
    }

    public void removeSticker(SMView sticker) {
        if (sticker!=null) {
            _canvasView.removeChild(sticker);
        }
    }

    public void removeStickerWithFadeOut(SMView sticker, final float duration, final float delay) {
        if (sticker!=null) {
            _canvasView.removeChildWithFadeOut(sticker, duration, delay);
        }
    }

    public void removeAllStickerWithFly() {
        ArrayList<SMView> children = _canvasView.getChildren();

        Vec2 pt1 = new Vec2(_contentSize.divide(2).toVec());

        for (SMView child : children) {
            if (!(child instanceof StickerItemView)) {
                continue;
            }

            StickerItemView sticker = (StickerItemView)child;
            Vec2 pt2 = sticker.getPosition();

            double radians = Math.atan2(pt2.y-pt1.y , pt2.x-pt1.x);
            float degrees = (float)SMView.toDegrees(radians);

            // kaos~~
            if (degrees>90 && degrees<120) {
                degrees += SMView.randomFloat(0.0f, 0.3f) * 100.0f;
            }
            if (degrees<90 && degrees>60) {
                degrees -= SMView.randomFloat(0.0f, 0.3f) * 100.0f;
            }

            _canvasView.removeChildWithFly(sticker, degrees, SMView.randomFloat(0.7f, 0.8f) * 10000.0f);
        }
    }

    public void removeAllSticker() {
        ArrayList<SMView> children = _contentView.getChildren();

        int size = children.size();
        for (int i=size-1; i>=0; i--) {
            if (children.get(i) instanceof StickerItemView) {
                StickerItemView sticker = (StickerItemView)children.get(i);
                removeSticker(sticker);
            }
        }
    }

    public SMImageView getBgImageView() {
        return _bgImageView;
    }

    public StickerCanvasView getCanvas() {
        return _canvasView;
    }

    public StickerControlView getControl() {
        return _controlView;
    }

    public SMZoomView getZoomView() {
        return _zoomView;
    }

    public SMView getContentView() {
        return _contentView;
    }

    public void setZoomStatus(final float panX, final float panY, final float zoomScale, final float duration) {
        _zoomView.setZoomWithAnimation(panX, panY, zoomScale, duration);
    }

    public void cancelTouch() {
        cancel();
    }

    @Override
    public boolean containsPoint(final Vec2 point) {
        return this.containsPoint(point.x, point.y);
    }

    @Override
    public boolean containsPoint(final float x, final float y) {
        return true;
    }

    @Override
    public int dispatchTouchEvent(MotionEvent event, SMView view, boolean checkBounds) {
        return super.dispatchTouchEvent(event, view, false);
    }




        @Override
    public void onStickerMenuClick(SMView sticker, int menuId) {
            if (_controlListener!=null) {
                _controlListener.onStickerMenuClick(sticker, menuId);
            }
    }

    @Override
    public void onStickerTouch(SMView view, int action) {
        if (_canvasListener!=null) {
            _canvasListener.onStickerTouch(view, action);
        }
    }

    @Override
    public void onStickerSelected(SMView view, final boolean selected) {
        _controlView.linkStickerView(selected?view:null);

        if (_canvasListener!=null) {
            _canvasListener.onStickerSelected(view, selected);
        }
    }

    @Override
    public void onStickerDoubleClicked(SMView view, final Vec2 worldPoint) {
        _zoomView.performDoubleClick(worldPoint);

        if (_canvasListener!=null) {
            _canvasListener.onStickerDoubleClicked(view, worldPoint);
        }
    }

    @Override
    public void onStickerRemoveBegin(SMView view) {
        if (view!=null && view==_canvasView.getSelectedSticker()) {
            _controlView.linkStickerView(null);
        }

        if (_canvasListener!=null) {
            _canvasListener.onStickerRemoveBegin(view);
        }
    }

    @Override
    public void onStickerRemoveEnd(SMView view) {
        if (view!=null && view==_canvasView.getSelectedSticker()) {
            _controlView.linkStickerView(null);
        }

        if (_canvasListener!=null) {
            _canvasListener.onStickerRemoveEnd(view);
        }
    }

    public float getZoomScale() {
        return _zoomView.getZoom();
    }

    private SMView _contentView = null;
    private SMZoomView _zoomView = null;
    private StickerCanvasView _canvasView = null;
    private StickerControlView _controlView = null;
    private SMImageView _bgImageView = null;
    private StickerCanvasView.StickerCanvasListener _canvasListener = null;
    private StickerControlView.StickerControlListener _controlListener = null;
    private GridSprite _gridSprite = null;
}
