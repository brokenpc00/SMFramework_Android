package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseBounceIn extends ActionEase {
    public EaseBounceIn(IDirector director) {
        super(director);
    }

    public static EaseBounceIn create(IDirector director, ActionInterval action) {
        EaseBounceIn ease = new EaseBounceIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseBounceIn clone() {
        if (_inner != null) {
            return EaseBounceIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.bounceEaseIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseBounceOut.create(getDirector(), _inner.reverse());
    }
}