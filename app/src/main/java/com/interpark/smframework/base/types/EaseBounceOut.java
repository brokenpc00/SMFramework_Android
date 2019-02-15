package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseBounceOut extends ActionEase {
    public EaseBounceOut(IDirector director) {
        super(director);
    }

    public static EaseBounceOut create(IDirector director, ActionInterval action) {
        EaseBounceOut ease = new EaseBounceOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseBounceOut clone() {
        if (_inner != null) {
            return EaseBounceOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.bounceEaseOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseBounceIn.create(getDirector(), _inner.reverse());
    }
}