package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseCubicActionOut extends ActionEase {
    public EaseCubicActionOut(IDirector director) {
        super(director);
    }

    public static EaseCubicActionOut create(IDirector director, ActionInterval action) {
        EaseCubicActionOut ease = new EaseCubicActionOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseCubicActionOut clone() {
        if (_inner != null) {
            return EaseCubicActionOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.cubicEaseOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseCubicActionIn.create(getDirector(), _inner.reverse());
    }
}