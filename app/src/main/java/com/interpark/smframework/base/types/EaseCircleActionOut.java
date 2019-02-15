package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseCircleActionOut extends ActionEase {
    public EaseCircleActionOut(IDirector director) {
        super(director);
    }

    public static EaseCircleActionOut create(IDirector director, ActionInterval action) {
        EaseCircleActionOut ease = new EaseCircleActionOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseCircleActionOut clone() {
        if (_inner != null) {
            return EaseCircleActionOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.circEaseOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseCircleActionIn.create(getDirector(), _inner.reverse());
    }
}
