package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseQuarticActionIn extends ActionEase {
    public EaseQuarticActionIn(IDirector director) {
        super(director);
    }

    public static EaseQuarticActionIn create(IDirector director, ActionInterval action) {
        EaseQuarticActionIn ease = new EaseQuarticActionIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseQuarticActionIn clone() {
        if (_inner != null) {
            return EaseQuarticActionIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.quartEaseIn(time));
    }

    @Override
    public ActionEase reverse() {
        return EaseQuarticActionOut.create(getDirector(), _inner.reverse());
    }
}
