package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseCircleActionInOut extends ActionEase {
    public EaseCircleActionInOut(IDirector director) {
        super(director);
    }

    public static EaseCircleActionInOut create(IDirector director, ActionInterval action) {
        EaseCircleActionInOut ease = new EaseCircleActionInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseCircleActionInOut clone() {
        if (_inner != null) {
            return EaseCircleActionInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.circEaseInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseCircleActionInOut.create(getDirector(), _inner.reverse());
    }
}