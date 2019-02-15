package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class Repeat extends ActionInterval {
    public Repeat(IDirector director) {
        super(director);
    }

    public static Repeat create(IDirector director, FiniteTimeAction action, int times) {
        Repeat repeat = new Repeat(director);
        if (repeat!=null && repeat.initWithActioin(action, times)) {
            return repeat;
        }

        return null;
    }

    public void setInnerAction(FiniteTimeAction action) {
        if (_innerAction!=action) {
            _innerAction = action;
        }
    }

    public FiniteTimeAction getInnerAction() {
        return _innerAction;
    }

    @Override
    public Repeat clone() {
        return Repeat.create(getDirector(), _innerAction.clone(), _times);
    }

    @Override
    public Repeat reverse() {
        return Repeat.create(getDirector(), _innerAction.reverse(), _times);
    }

    @Override
    public void startWithTarget(SMView target) {
        _total = 0;
        _nextDt = _innerAction.getDuration() / _duration;
        super.startWithTarget(target);
        _innerAction.startWithTarget(target);
    }

    @Override
    public void stop() {
        _innerAction.stop();
        super.stop();
    }

    @Override
    public void update(float dt) {
        if (dt > _nextDt) {
            while (dt >= _nextDt && _total < _times) {
                _innerAction.update(1.0f);
                _total++;

                _innerAction.stop();
                _innerAction.startWithTarget(_target);
                _nextDt = _innerAction.getDuration() / _duration * (_total+1);
            }

            if (Math.abs(dt - 1.0f) < EPSILON && _total < _times) {
                _innerAction.update(1.0f);
                _total++;
            }

            if (!_actionInstant) {
                if (_total==_times) {
                    _innerAction.stop();
                } else {
                    _innerAction.update(dt - (_nextDt - _innerAction.getDuration()/_duration));
                }
            }
        } else {
            _innerAction.update(dt*_times % 1.0f);
        }
    }

    @Override
    public boolean isDone() {
        return _total==_times;
    }

    protected boolean initWithActioin(FiniteTimeAction a, int times) {
        float d = a.getDuration() * times;
        if (a!=null && super.initWithDuration(d)) {
            _times = times;
            _innerAction = a;

            _actionInstant = a.getClass()==ActionInstant.class;

            _total = 0;
            return true;
        }

        return false;
    }

    protected int _times;
    protected int _total;
    protected float _nextDt;
    protected boolean _actionInstant;

    protected FiniteTimeAction _innerAction = null;
}
