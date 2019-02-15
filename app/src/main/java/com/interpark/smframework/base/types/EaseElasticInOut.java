package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseElasticInOut extends EaseElastic {
    public EaseElasticInOut(IDirector director) {
        super(director);
    }

    public static EaseElasticInOut create(IDirector director, ActionInterval action) {
        return EaseElasticInOut.create(director, action, 0.3f);
    }

    public static EaseElasticInOut create(IDirector director, ActionInterval action, float period) {
        EaseElasticInOut ease = new EaseElasticInOut(director);

        if (ease != null && ease.initWithAction(action, period)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseElasticInOut clone() {
        if (_inner != null) {
            return EaseElasticInOut.create(getDirector(), _inner.clone(), _period);
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.elasticEaseInOut(time, _period));
    }

    @Override
    public EaseElastic reverse() {
        return EaseElasticInOut.create(getDirector(), _inner.reverse(), _period);
    }
}