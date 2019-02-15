package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseSineIn extends EaseRateAction {
    public EaseSineIn(IDirector director) {
        super(director);
    }

    public static EaseSineIn create(IDirector director, ActionInterval action) {
        EaseSineIn ease = new EaseSineIn(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseSineIn clone() {
        if (_inner != null) {
            return EaseSineIn.create(getDirector(), _inner.clone());
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.sineEaseIn(time));
    }

    @Override
    public EaseRateAction reverse() {
        return EaseSineOut.create(getDirector(), _inner.reverse());
    }
}