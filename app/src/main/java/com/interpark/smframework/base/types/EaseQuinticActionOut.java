package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuinticActionOut extends ActionEase {
    public EaseQuinticActionOut(IDirector director) {
        super(director);
    }

    public static EaseQuinticActionOut create(IDirector director, ActionInterval action) {
        EaseQuinticActionOut ease = new EaseQuinticActionOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuinticActionOut clone() {
        if (_inner != null) {
            return EaseQuinticActionOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quintEaseOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuinticActionIn.create(getDirector(), _inner.reverse());
    }
}


