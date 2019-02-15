package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class EaseElastic extends ActionEase {
    public EaseElastic(IDirector director) {
        super(director);
    }

    public float getPeriod() {return _period;}

    public void setPeriod(float period) {_period = period;}

    @Override
    public boolean initWithAction(ActionInterval action) {
        return initWithAction(action, 0.3f);
    }

    protected boolean initWithAction(ActionInterval action, float period) {
        if (super.initWithAction(action)) {
            _period = period;

            return true;
        }
        return false;
    }

    protected float _period;
}