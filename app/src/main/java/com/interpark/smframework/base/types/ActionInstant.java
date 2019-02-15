package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class ActionInstant extends FiniteTimeAction {
    public ActionInstant(IDirector director) {
        super(director);
    }

    private boolean _done;
    @Override
    public ActionInstant clone() {
        return null;
    }

    @Override
    public ActionInstant reverse() {
        return null;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
    }

    @Override
    public boolean isDone() {
        return _done;
    }

    @Override
    public void step(float dt) {
        float updateDt = 1;
        update(updateDt);
    }

    @Override
    public void update(float t) {
        _done = true;
    }

}
