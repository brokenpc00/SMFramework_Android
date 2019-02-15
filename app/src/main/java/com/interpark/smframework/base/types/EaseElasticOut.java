package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;

public class EaseElasticOut extends EaseElastic {
    public EaseElasticOut(IDirector director) {
        super(director);
    }

    public static EaseElasticOut create(IDirector director, ActionInterval action) {
        return EaseElasticOut.create(director, action, 0.3f);
    }

    public static EaseElasticOut create(IDirector director, ActionInterval action, float period) {
        EaseElasticOut ease = new EaseElasticOut(director);

        if (ease != null && ease.initWithAction(action, period)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseElasticOut clone() {
        if (_inner != null) {
            return EaseElasticOut.create(getDirector(), _inner.clone(), _period);
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.elasticEaseOut(time, _period));
    }

    @Override
    public EaseElastic reverse() {
        return EaseElasticIn.create(getDirector(), _inner.reverse(), _period);
    }
}
