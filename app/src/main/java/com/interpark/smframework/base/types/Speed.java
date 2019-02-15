package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class Speed extends Action {
    public Speed(IDirector director) {
        super(director);
        _speed = 0.0f;
        _innerAction = null;
    }

    public static Speed create(IDirector director, ActionInterval action, float speed) {
        Speed ret = new Speed(director);
        if (ret!=null && ret.initWithAction(action, speed)) {
            return ret;
        }

        return ret;
    }

    @Override
    public Speed clone() {
        if (_innerAction!=null) {
            return Speed.create(_director, _innerAction.clone(), _speed);
        }
        return null;
    }

    @Override
    public void startWithTarget(SMView target) {
        if (target!=null && _innerAction!=null) {
            super.startWithTarget(target);
            _innerAction.startWithTarget(target);
        }
    }

    @Override
    public void step(float dt) {
        _innerAction.step(dt * _speed);
    }

    @Override
    public Speed reverse() {
        if (_innerAction!=null) {
            return Speed.create(_director, _innerAction.reverse(), _speed);
        }
        return null;
    }

    @Override
    public void stop() {
        if (_innerAction!=null) {
            _innerAction.stop();
        }
        super.stop();
    }

    @Override
    public boolean isDone() {
        return _innerAction.isDone();
    }

    public float getSpeed() {return _speed;}
    public void setSpeed(float speed) {
        _speed = speed;
    }

    public void setInnerAction(ActionInterval action) {
        if (_innerAction!=action) {
            _innerAction.stop();
            _innerAction = action;
        }
    }

    public ActionInterval getInnerAction() {return _innerAction;}

    protected boolean initWithAction(ActionInterval action, float speed) {
        assert (action!=null);

        if (action==null) {
            return false;
        }

        _innerAction = action;
        _speed = speed;
        return true;
    }

    protected float _speed = 0.0f;
    protected ActionInterval _innerAction;
}
