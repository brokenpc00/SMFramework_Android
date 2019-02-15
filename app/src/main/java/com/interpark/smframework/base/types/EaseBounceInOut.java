package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseBounceInOut extends ActionEase {
    public EaseBounceInOut(IDirector director) {
        super(director);
    }

    public static EaseBounceInOut create(IDirector director, ActionInterval action) {
        EaseBounceInOut ease = new EaseBounceInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseBounceInOut clone() {
        if (_inner != null) {
            return EaseBounceInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.bounceEaseInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseBounceInOut.create(getDirector(), _inner.reverse());
    }
}
