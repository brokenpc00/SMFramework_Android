package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseCircleActionIn extends ActionEase {
    public EaseCircleActionIn(IDirector director) {
        super(director);
    }

    public static EaseCircleActionIn create(IDirector director, ActionInterval action) {
        EaseCircleActionIn ease = new EaseCircleActionIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseCircleActionIn clone() {
        if (_inner != null) {
            return EaseCircleActionIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.circEaseIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseCircleActionOut.create(getDirector(), _inner.reverse());
    }
}
