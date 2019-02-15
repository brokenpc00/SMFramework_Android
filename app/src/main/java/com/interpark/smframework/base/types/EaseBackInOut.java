package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseBackInOut extends ActionEase {
    public EaseBackInOut(IDirector director) {
        super(director);
    }

    public static EaseBackInOut create(IDirector director, ActionInterval action) {
        EaseBackInOut ease = new EaseBackInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseBackInOut clone() {
        if (_inner != null) {
            return EaseBackInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.backEaseInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseBackInOut.create(getDirector(), _inner.reverse());
    }
}
