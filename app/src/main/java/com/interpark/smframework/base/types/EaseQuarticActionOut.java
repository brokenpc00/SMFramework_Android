package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuarticActionOut extends ActionEase {
    public EaseQuarticActionOut(IDirector director) {
        super(director);
    }

    public static EaseQuarticActionOut create(IDirector director, ActionInterval action) {
        EaseQuarticActionOut ease = new EaseQuarticActionOut(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuarticActionOut clone() {
        if (_inner != null) {
            return EaseQuarticActionOut.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quartEaseOut(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuarticActionIn.create(getDirector(), _inner.reverse());
    }
}
