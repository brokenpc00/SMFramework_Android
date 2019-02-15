package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseBackIn extends ActionEase {
    public EaseBackIn(IDirector director) {
        super(director);
    }

    public static EaseBackIn create(IDirector director, ActionInterval action) {
        EaseBackIn ease = new EaseBackIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseBackIn clone() {
        if (_inner != null) {
            return EaseBackIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.backEaseIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseBackOut.create(getDirector(), _inner.reverse());
    }
}