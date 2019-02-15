package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseElasticIn extends EaseElastic {
    public EaseElasticIn(IDirector director) {
        super(director);
    }

    public static EaseElasticIn create(IDirector director, ActionInterval action) {
        return EaseElasticIn.create(director, action, 0.3f);
    }

    public static EaseElasticIn create(IDirector director, ActionInterval action, float period) {
        EaseElasticIn ease = new EaseElasticIn(director);

        if (ease != null && ease.initWithAction(action, period)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseElasticIn clone() {
        if (_inner != null) {
            return EaseElasticIn.create(getDirector(), _inner.clone(), _period);
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.elasticEaseIn(time, _period));
    }

    @Override
    public EaseElastic reverse() {
        return EaseElasticOut.create(getDirector(), _inner.reverse(), _period);
    }
}