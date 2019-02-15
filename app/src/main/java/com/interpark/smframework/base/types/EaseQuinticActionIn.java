package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuinticActionIn extends ActionEase {
    public EaseQuinticActionIn(IDirector director) {
        super(director);
    }

    public static EaseQuinticActionIn create(IDirector director, ActionInterval action) {
        EaseQuinticActionIn ease = new EaseQuinticActionIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuinticActionIn clone() {
        if (_inner != null) {
            return EaseQuinticActionIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quintEaseIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuinticActionOut.create(getDirector(), _inner.reverse());
    }
}
