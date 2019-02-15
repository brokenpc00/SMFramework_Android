package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseSineInOut extends EaseRateAction {
    public EaseSineInOut(IDirector director) {
        super(director);
    }

    public static EaseSineInOut create(IDirector director, ActionInterval action) {
        EaseSineInOut ease = new EaseSineInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseSineInOut clone() {
        if (_inner != null) {
            return EaseSineInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.sineEaseInOut(time));
    }

    @Override
    public EaseRateAction reverse() {
        return EaseSineInOut.create(getDirector(), _inner.reverse());
    }
}
