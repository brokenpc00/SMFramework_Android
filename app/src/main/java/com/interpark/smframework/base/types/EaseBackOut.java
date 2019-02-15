package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseBackOut extends ActionEase {
    public EaseBackOut(IDirector director) {
        super(director);
    }

    public static EaseBackOut create(IDirector director, ActionInterval action) {
        EaseBackOut ease = new EaseBackOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseBackOut clone() {
        if (_inner != null) {
            return EaseBackOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.backEaseOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseBackIn.create(getDirector(), _inner.reverse());
    }
}