package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseExponentialIn extends ActionEase {
    public EaseExponentialIn(IDirector director) {
        super(director);
    }

    public static EaseExponentialIn create(IDirector director, ActionInterval action) {
        EaseExponentialIn ease = new EaseExponentialIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseExponentialIn clone() {
        if (_inner != null) {
            return EaseExponentialIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.expoEaseIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseExponentialOut.create(getDirector(), _inner.reverse());
    }
}
