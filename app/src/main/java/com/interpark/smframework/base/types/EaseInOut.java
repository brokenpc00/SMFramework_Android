package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseInOut extends EaseRateAction {
    public EaseInOut(IDirector director) {
        super(director);
    }

    public static EaseInOut create(IDirector director, ActionInterval action, float rate) {
        EaseInOut ease = new EaseInOut(director);

        if (ease != null && ease.initWithAction(action, rate)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseInOut clone() {
        if (_inner != null) {
            return EaseInOut.create(getDirector(), _inner.clone(), _rate);
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.easeInOut(time, _rate));
    }

    @Override
    public ActionEase reverse() {
        return EaseInOut.create(getDirector(), _inner.reverse(), 1.0f/_rate);
    }
}