package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class DelayBaseAction extends ActionInterval {
    public DelayBaseAction(IDirector director) {
        super(director);
    }

    @Override
    public void update(float t) {
        float time = t * getDuration();
        if (time < _delay) {
            return;
        }

        if (!_started) {
            _started = true;
            _ended = false;
            onStart();
        }

        float tt = (time - _delay) / _duration;
        if (_reverse) {
            tt = 1 - tt;
        }
        onUpdate(tt);

        if (t >= 1.0f && !_ended) {
            onEnd();
            _ended = true;
        }
    }

    public void onStart() {}
    public void onEnd() {}
    public void onUpdate(float t) {}

    public void setReverse() {
        _reverse = true;
    }

    public void setTimeValue(float duration, float delay) {
        _duration = duration;
        _delay = delay;
        setDuration(duration+delay);
        _started = false;
    }

    protected float _duration;
    protected float _delay;


    private boolean _started;
    private boolean _ended;
    private boolean _reverse;
}
