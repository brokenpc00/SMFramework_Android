package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.types.Color4F;

public class LoadingSprite extends SMImageView {
    public LoadingSprite(IDirector director) {
        super(director);
    }

    private static final float NUM_TICK = 12.0f;
    private static final float TICK_TIME = 0.1f;
    private static final float DELAY_TIME = 0.2f;
    private static final float FADE_TIME = 0.1f;

    public static LoadingSprite createWithTexture(IDirector director) {
        return createWithTexture(director, null);
    }
    public static LoadingSprite createWithTexture(IDirector director, Texture texture) {
        LoadingSprite loadingSprite = new LoadingSprite(director);


        if (texture!=null) {
            Sprite sprite = new Sprite(director, texture, 0, 0);
            loadingSprite.setSprite(sprite);
        } else {
            BitmapSprite sprite = BitmapSprite.createFromAsset(director, "images/loading_spinner_white.png", true, null);
            loadingSprite.setSprite(sprite);
        }

        loadingSprite._start = NUM_TICK*SMView.randomFloat(0, 1.0f);
        loadingSprite.setColor(Color4F.XEEEFF1);

        return loadingSprite;
    }


    public static LoadingSprite createWithFile(IDirector director) {
        return createWithFile(director, "");
    }
    public static LoadingSprite createWithFile(IDirector director, final String imageFileName) {
        LoadingSprite loadingSprite = new LoadingSprite(director);
        String fileName = imageFileName;
        if (fileName.equals("")) {
            fileName = "images/loading_spinner_white.png";
        }
        BitmapSprite sprite = BitmapSprite.createFromAsset(director, fileName, true, null);
        loadingSprite.setSprite(sprite);
        loadingSprite._start = NUM_TICK*SMView.randomFloat(0, 1.0f);
        loadingSprite.setColor(Color4F.XEEEFF1);

        return loadingSprite;
    }

    @Override
    public void setVisible(final boolean visible) {
        if (_visible!=visible) {
            _visible = visible;
            if (_visible) {
                _transformUpdated = _transformDirty = _inverseDirty = true;
                _visibleTime = _director.getGlobalTime();
            }
        }
    }

    @Override
    public void draw(float alpha) {
        // calc spend time
        float t = _director.getGlobalTime() - _visibleTime;

        if (t<DELAY_TIME) {
            return;
        }

        t = (t-DELAY_TIME) / FADE_TIME;
        float newAlpha = 1.0f;
        if (t<1) {
            newAlpha *= t;
        }

        if (getAlpha()!=newAlpha) {
            setAlpha(newAlpha);
        }


        float time = _director.getGlobalTime() + _start;

        // 흐른 시간만큼 돌린다.
        float tick = (time/TICK_TIME)%NUM_TICK;
        float angle = tick*360.0f/NUM_TICK;

        if (getRotation()!=angle) {
            setRotation(angle);
        }

        super.draw(alpha);
    }

    private float _start = 0.0f;
    private float _visibleTime = 0.0f;
}
