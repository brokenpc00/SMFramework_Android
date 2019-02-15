package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseCubicActionInOut extends ActionEase {
    public EaseCubicActionInOut(IDirector director) {
        super(director);
    }

    public static EaseCubicActionInOut create(IDirector director, ActionInterval action) {
        EaseCubicActionInOut ease = new EaseCubicActionInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseCubicActionInOut clone() {
        if (_inner != null) {
            return EaseCubicActionInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.cubicEaseInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseCubicActionInOut.create(getDirector(), _inner.reverse());
    }
}
