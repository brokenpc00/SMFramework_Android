package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuarticActionInOut extends ActionEase {
    public EaseQuarticActionInOut(IDirector director) {
        super(director);
    }

    public static EaseQuarticActionInOut create(IDirector director, ActionInterval action) {
        EaseQuarticActionInOut ease = new EaseQuarticActionInOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuarticActionInOut clone() {
        if (_inner != null) {
            return EaseQuarticActionInOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quartEaseInOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuarticActionInOut.create(getDirector(), _inner.reverse());
    }
}