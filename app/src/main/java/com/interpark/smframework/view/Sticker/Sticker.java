package com.interpark.smframework.view.Sticker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.GridSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

import static com.interpark.smframework.view.Sticker.Sticker.ControlType.NONE;

public class Sticker extends RemovableSticker {

    private static float FAT_STEP_VALUE = 0.2f;

    public Sticker(IDirector director) {
        super(director);
    }

    protected boolean init() {
        setAnchorPoint(Vec2.MIDDLE);
        return super.init();
    }

    public enum ControlType {
        NONE,
        FAN,
        DELETE,
        UNPACK,
        PACK,
    }

    public static Sticker create(IDirector director) {
        return create(director, 0, 0, 0, 0);
    }
    public static Sticker create(IDirector director, float x, float y, float width, float height) {
        return create(director, 0, x, y, width, height, 0, 0);
    }
    public static Sticker create(IDirector director, int tag, float x, float y, float width, float height, float anchorX, float anchorY) {
        Sticker sticker = new Sticker(director);
        sticker.setAnchorPoint(new Vec2(anchorX, anchorY));
        sticker.setContentSize(new Size(width, height));
        sticker.setPosition(new Vec2(x, y));
        sticker.init();
        return sticker;
    }

    public boolean isRemoved() {
        return getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null;
    }

    public void setControlType(ControlType controlType) {
        _controlType = controlType;
    }

    public ControlType getControlType() {return _controlType;}


    @Override
    public void onImageLoadComplete(Sprite sprite, int tag, boolean direct) {
        if (sprite!=null) {
            GridSprite gridSprite = GridSprite.create(getDirector(), sprite);
            if (gridSprite!=null) {
                // always true
                setSprite(gridSprite);

                if (getContentSize().width==0 && getContentSize().height==0) {
                    setContentSize(getSprite().getWidth(), getSprite().getHeight());
                }

                if (_listener!=null) {
                    _listener.onSpriteLoadedCallback(this, gridSprite);
                }
            } else {
                setSprite(sprite);
                if (_listener!=null) {
                    _listener.onSpriteLoadedCallback(this, sprite);
                }
            }
        }
    }

    public void setFatValue(final float value) {
        _fatValue = value;
        if (_fatValue!=0) {
            if (_sprite instanceof GridSprite) {
                Size size = new Size(_sprite.getTexture().getWidth(), _sprite.getTexture().getHeight());
                ((GridSprite)_sprite).grow(size.width/2, size.height/2, value, FAT_STEP_VALUE, size.width);
            }
        }
    }

    public float getFatValue() {return _fatValue;}

    public interface OnSpriteLoadedCallback {
        public void onSpriteLoadedCallback(Sticker sender, Sprite sprite);
    }
    private OnSpriteLoadedCallback _listener = null;

    public void setOnSpriteLoadedCallback(OnSpriteLoadedCallback l) {
        _listener = l;
    }

    @Override
    public Sticker clone() {
        if (getSprite()!=null) {
            Sprite newSprite = new Sprite(getDirector(), getSprite().getTexture(), getSprite().getTexture().getWidth()/2, getSprite().getTexture().getHeight()/2);

            Sticker newSticker = Sticker.create(getDirector());
            newSticker.setSprite(newSprite);
            newSticker.setPosition(getPosition());
            newSticker.setScale(getScale());
            newSticker.setRotation(getRotation());

            return newSticker;
        }

        return null;
    }

    // anchor point is must be middle
    @Override
    public void setAnchorPoint(Vec2 v) {
        super.setAnchorPoint(Vec2.MIDDLE);
    }
    @Override
    public void setAnchorPoint(float anchorX, float anchorY) {
        this.setAnchorPoint(new Vec2(anchorX, anchorY));
    }

    private ControlType _controlType = NONE;
    private float _fatValue = 0;
}
