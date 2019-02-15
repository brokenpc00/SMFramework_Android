package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseSineOut extends EaseRateAction {
    public EaseSineOut(IDirector director) {
        super(director);
    }

    public static EaseSineOut create(IDirector director, ActionInterval action) {
        EaseSineOut ease = new EaseSineOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseSineOut clone() {
        if (_inner != null) {
            return EaseSineOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.sineEaseOut(time));
    }

    @Override
    public EaseRateAction reverse() {
        return EaseSineIn.create(getDirector(), _inner.reverse());
    }
}