package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseExponentialOut extends ActionEase {
    public EaseExponentialOut(IDirector director) {
        super(director);
    }

    public static EaseExponentialOut create(IDirector director, ActionInterval action) {
        EaseExponentialOut ease = new EaseExponentialOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseExponentialOut clone() {
        if (_inner != null) {
            return EaseExponentialOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.expoEaseOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseExponentialIn.create(getDirector(), _inner.reverse());
    }
}