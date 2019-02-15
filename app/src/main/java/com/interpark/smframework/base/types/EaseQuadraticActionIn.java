package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuadraticActionIn extends ActionEase {
    public EaseQuadraticActionIn(IDirector director) {
        super(director);
    }

    public static EaseQuadraticActionIn create(IDirector director, ActionInterval action) {
        EaseQuadraticActionIn ease = new EaseQuadraticActionIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuadraticActionIn clone() {
        if (_inner != null) {
            return EaseQuadraticActionIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quadraticIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuadraticActionOut.create(getDirector(), _inner.reverse());
    }
}
