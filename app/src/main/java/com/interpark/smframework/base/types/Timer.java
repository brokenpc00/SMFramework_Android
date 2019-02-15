package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class Timer {
    protected Timer(IDirector director) {
        _director = director;
    }

    protected Scheduler _scheduler = null;
    protected float _elapsed = -1;
    protected boolean _runForever = false;
    protected boolean _useDelay = false;
    protected long _timesExecuted = 0;
    protected long _repeat = 0;
    protected float _delay = 0.0f;
    protected float _interval = 0.0f;
    protected boolean _aborted = false;

    public void setupTimerWithInterval(float seconds, long repeat, float delay) {
        _elapsed = -1;
        _interval = seconds;
        _delay = delay;
        _useDelay = (_delay > 0.0f) ? true : false;
        _repeat = repeat;
        _runForever = (_repeat == Long.MAX_VALUE) ? true : false;
        _timesExecuted = 0;

    }

    public void setAborted() { _aborted = true; }
    public final boolean isAborted() { return _aborted; }
    public final boolean isExhausted() {
        return !_runForever && _timesExecuted > _repeat;
    };

    public void trigger(float dt) {};
    public void cancel() {};

    public void update(float dt) {
        if (_elapsed == -1)
        {
            _elapsed = 0;
            _timesExecuted = 0;
            return;
        }

        // accumulate elapsed time
        _elapsed += dt;

        // deal with delay
        if (_useDelay)
        {
            if (_elapsed < _delay)
            {
                return;
            }
            _timesExecuted += 1; // important to increment before call trigger
            trigger(_delay);
            _elapsed = _elapsed - _delay;
            _useDelay = false;
            // after delay, the rest time should compare with interval
            if (isExhausted())
            {    //unschedule timer
                cancel();
                return;
            }
        }

        // if _interval == 0, should trigger once every frame
        float interval = (_interval > 0) ? _interval : _elapsed;
        while ((_elapsed >= interval) && !_aborted)
        {
            _timesExecuted += 1; // important to increment before call trigger
            trigger(interval);
            _elapsed -= interval;

            if (isExhausted())
            {
                cancel();
                break;
            }

            if (_elapsed <= 0.f)
            {
                break;
            }
        }
    };

    protected IDirector _director;
}
