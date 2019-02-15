package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseOut extends EaseRateAction {
    public EaseOut(IDirector director) {
        super(director);
    }

    public static EaseOut create(IDirector director, ActionInterval action, float rate) {
        EaseOut ease = new EaseOut(director);

        if (ease != null && ease.initWithAction(action, rate)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseOut clone() {
        if (_inner != null) {
            return EaseOut.create(getDirector(), _inner.clone(), _rate);
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.easeOut(time, _rate));
    }

    @Override
    public ActionEase reverse() {
        return EaseOut.create(getDirector(), _inner.reverse(), 1.0f/_rate);
    }
}