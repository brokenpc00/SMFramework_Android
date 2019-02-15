package com.interpark.smframework.base.types;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class ActionInterval extends FiniteTimeAction {

    public ActionInterval(IDirector director) {
        super(director);
    }

    protected float EPSILON = 0.0000001f;

    public float getElapsed() {
        return _elapsed;
    }

    public void setAmplitudeRate(float amp) { }
    public float getAmplitudeRate() {
        return 0;
    }

    @Override
    public boolean isDone() {
        return _done;
    }

    @Override
    public ActionInterval clone() {
        return null;
    }

    @Override
    public ActionInterval reverse() {
        return null;
    }

    @Override
    public void step(float dt) {
        if (_firstTick) {
            _firstTick = false;
            _elapsed = 0;
        } else {
            _elapsed += dt;
        }

        float updateDt = Math.max(0.0f, Math.min(1.0f, _elapsed/_duration));

        this.update(updateDt);

        _done = _elapsed >= _duration;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _elapsed = 0.0f;
        _firstTick = true;
        _done = false;
    }

    public boolean initWithDuration(float d) {
        _duration = d;
        _elapsed = 0;
        _firstTick = true;
        _done = false;

        return true;
    }

    protected float _elapsed;
    protected boolean _firstTick;
    protected boolean _done;
}
