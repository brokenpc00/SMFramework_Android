package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class FiniteTimeAction extends Action {
    public float getDuration() {
        return _duration;
    }

    public void setDuration(float duration) {
        _duration = duration;
    }

    @Override
    public FiniteTimeAction reverse() {
        return null;
    }

    @Override
    public FiniteTimeAction clone() {
        return null;
    }

    public FiniteTimeAction(IDirector director) {
        super(director);
        _duration = 0;
    }

    protected float _duration;
}
