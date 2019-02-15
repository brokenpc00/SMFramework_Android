package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseIn extends EaseRateAction {
    public EaseIn(IDirector director) {
        super(director);
    }

    public static EaseIn create(IDirector director, ActionInterval action, float rate) {
        EaseIn ease = new EaseIn(director);

        if (ease != null && ease.initWithAction(action, rate)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseIn clone() {
        if (_inner != null) {
            return EaseIn.create(getDirector(), _inner.clone(), _rate);
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.easeIn(time, _rate));
    }

    @Override
    public ActionEase reverse() {
        return EaseIn.create(getDirector(), _inner.reverse(), 1.0f/_rate);
    }
}