package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class EaseRateAction extends ActionEase {
    public EaseRateAction(IDirector director) {
        super(director);
    }

    public static EaseRateAction create(IDirector director, ActionInterval action, float rate) {
        EaseRateAction ease = new EaseRateAction(director);
        if (ease!=null && ease.initWithAction(action, rate)) {
            return ease;
        }
        return null;
    }

    public void setRate(float rate) {_rate = rate;}

    public float getRate() {return _rate;}

    protected boolean initWithAction(ActionInterval action, float rate) {
        if (super.initWithAction(action)) {
            _rate = rate;
            return true;
        }

        return false;
    }

    protected float _rate;
}

