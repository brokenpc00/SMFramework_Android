package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class DelayTime extends ActionInterval {
    public DelayTime(IDirector director){
        super(director);
    }

    public static DelayTime create(IDirector director, float t) {
        DelayTime action = new DelayTime(director);

        if (action!=null && action.initWithDuration(t)) {
            return action;
        }

        return null;
    }

    @Override
    public void update(float t) {
        // nothing to do...
        return;
    }

    @Override
    public DelayTime reverse() {
        return DelayTime.create(getDirector(), _duration);
    }

    @Override
    public DelayTime clone() {
        return DelayTime.create(getDirector(), _duration);
    }

}
