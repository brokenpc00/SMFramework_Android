package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class FadeTo extends ActionInterval {
    public FadeTo(IDirector director) {
        super(director);
    }

    public static FadeTo create(IDirector director, float duration, float alpha) {
        FadeTo fadeTo = new FadeTo(director);
        fadeTo.initWithDuration(duration, alpha);
        return fadeTo;
    }

    @Override
    public FadeTo clone() {
        return FadeTo.create(getDirector(), _duration, _toAlpha);
    }

    @Override
    public FadeTo reverse() {
        return null;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        if (target!=null) {
            _fromAlpha = target.getAlpha();
        }
    }

    @Override
    public void update(float t) {
        if (_target!=null) {
            float deltaAlpha = _toAlpha - _fromAlpha;
            _target.setTintAlpha(_fromAlpha + deltaAlpha*t);
        }
    }

    protected boolean initWithDuration(float duration, float alpha) {
        super.initWithDuration(duration);
        _toAlpha = alpha;
        return true;
    }

    protected float _toAlpha = 1.0f;
    protected float _fromAlpha = 1.0f;
}
