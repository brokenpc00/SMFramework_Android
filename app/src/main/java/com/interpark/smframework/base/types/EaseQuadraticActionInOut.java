package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuadraticActionInOut extends ActionEase {
    public EaseQuadraticActionInOut(IDirector director) {
        super(director);
    }

    public static EaseQuadraticActionInOut create(IDirector director, ActionInterval action) {
        EaseQuadraticActionInOut ease = new EaseQuadraticActionInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuadraticActionInOut clone() {
        if (_inner != null) {
            return EaseQuadraticActionInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quadraticInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuadraticActionInOut.create(getDirector(), _inner.reverse());
    }
}