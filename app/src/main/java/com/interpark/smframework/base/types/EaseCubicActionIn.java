package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseCubicActionIn extends ActionEase {
    public EaseCubicActionIn(IDirector director) {
        super(director);
    }

    public static EaseCubicActionIn create(IDirector director, ActionInterval action) {
        EaseCubicActionIn ease = new EaseCubicActionIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseCubicActionIn clone() {
        if (_inner != null) {
            return EaseCubicActionIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.cubicEaseIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseCubicActionOut.create(getDirector(), _inner.reverse());
    }
}
