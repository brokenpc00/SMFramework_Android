package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuinticActionInOut extends ActionEase {
    public EaseQuinticActionInOut(IDirector director) {
        super(director);
    }

    public static EaseQuinticActionInOut create(IDirector director, ActionInterval action) {
        EaseQuinticActionInOut ease = new EaseQuinticActionInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuinticActionInOut clone() {
        if (_inner != null) {
            return EaseQuinticActionInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quintEaseInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuinticActionInOut.create(getDirector(), _inner.reverse());
    }
}

