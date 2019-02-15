package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuadraticActionOut extends ActionEase {
    public EaseQuadraticActionOut(IDirector director) {
        super(director);
    }

    public static EaseQuadraticActionOut create(IDirector director, ActionInterval action) {
        EaseQuadraticActionOut ease = new EaseQuadraticActionOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuadraticActionOut clone() {
        if (_inner != null) {
            return EaseQuadraticActionOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quadraticOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuadraticActionIn.create(getDirector(), _inner.reverse());
    }
}