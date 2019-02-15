package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class RepeatForever extends ActionInterval {
    public RepeatForever(IDirector director) {
        super(director);
    }

    public static RepeatForever create(IDirector director, ActionInterval action) {
        RepeatForever ret = new RepeatForever(director);
        if (ret!=null && ret.initWithAction(action)) {
            return ret;
        }

        return null;
    }

    public void setInnserAction(ActionInterval action) {
        if (_innerAction!=action) {
            _innerAction = action;
        }
    }

    protected boolean initWithAction(ActionInterval action) {
        if (action==null) {
            return false;
        }

        _innerAction = action;
        return true;
    }

    public ActionInterval getInnerAction() {
        return _innerAction;
    }

    @Override
    public RepeatForever clone() {
        return RepeatForever.create(getDirector(), _innerAction.clone());
    }

    @Override
    public RepeatForever reverse() {
        return RepeatForever.create(getDirector(), _innerAction.reverse());
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _innerAction.startWithTarget(target);
    }

    @Override
    public void step(float dt) {
        _innerAction.step(dt);

        if (_innerAction.isDone() && _innerAction.getDuration()>0) {
            float diff = _innerAction.getElapsed() - _innerAction.getDuration();
            if (diff > _innerAction.getDuration()) {
                diff = diff % _innerAction.getDuration();
            }

            _innerAction.startWithTarget(_target);

            // um.... so strange...
            _innerAction.step(0.0f);
            _innerAction.step(diff);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    protected ActionInterval _innerAction = null;
}
