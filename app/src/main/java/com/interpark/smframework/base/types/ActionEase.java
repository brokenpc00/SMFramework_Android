package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.tweenfunc;

public class ActionEase extends ActionInterval {
    public ActionEase(IDirector director) {
        super(director);
    }

    public ActionInterval getInnerAction() {
        return _inner;
    }

    @Override
    public void startWithTarget(SMView target) {
        if (target!=null && _inner!=null) {
            super.startWithTarget(target);
            _inner.startWithTarget(_target);
        }
    }

    @Override
    public void stop() {
        if (_inner!=null) {
            _inner.stop();
        }

        super.stop();
    }

    @Override
    public void update(float time) {
        _inner.update(time);
    }

    public boolean initWithAction(ActionInterval action) {
        if (action==null) {
            return false;
        }

        if (super.initWithDuration(action.getDuration())) {
            _inner = action;

            return true;
        }

        return false;
    }

    protected ActionInterval _inner = null;
}
