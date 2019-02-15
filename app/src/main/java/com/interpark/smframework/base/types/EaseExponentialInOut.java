package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseExponentialInOut extends ActionEase {
    public EaseExponentialInOut(IDirector director) {
        super(director);
    }

    public static EaseExponentialInOut create(IDirector director, ActionInterval action) {
        EaseExponentialInOut ease = new EaseExponentialInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseExponentialInOut clone() {
        if (_inner != null) {
            return EaseExponentialInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.expoEaseInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseExponentialInOut.create(getDirector(), _inner.reverse());
    }
}