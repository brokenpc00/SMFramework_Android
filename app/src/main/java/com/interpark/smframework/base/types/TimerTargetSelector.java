package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.shader.ShaderManager;

public class TimerTargetSelector extends Timer {

    public TimerTargetSelector(IDirector director) {
        super(director);
    }

    public boolean initWithSelector(Scheduler scheduler, SEL_SCHEDULE selector, Ref target, float seconds, long repeat, float dealy) {
        _scheduler = scheduler;
        _target = target;
        _selector = selector;
        setupTimerWithInterval(seconds, repeat, dealy);
        return true;
    }

    public SEL_SCHEDULE getSelector() {return _selector;}

    @Override
    public void trigger(float dt) {
        if (_target!=null && _selector!=null) {
            _selector.scheduleSelector(dt);
//            _target.setSchdule(_selector);;
//            _target._schedule.scheduleSelector(dt);
        }
    }

    @Override
    public void cancel() {
        _scheduler.unschedule(_selector, _target);
    }

    protected Ref _target = null;
    protected SEL_SCHEDULE _selector = null;
}
