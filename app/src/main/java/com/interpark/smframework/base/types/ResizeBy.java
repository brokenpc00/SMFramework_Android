package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Size;

public class ResizeBy extends ActionInterval {
    public ResizeBy(IDirector director) {
        super(director);
    }

    public static ResizeBy create(IDirector director, float duration, final Size deltaSize) {
        ResizeBy ret = new ResizeBy(director);
        ret.initWithDuration(duration, deltaSize);
        return ret;
    }

    @Override
    public ResizeBy clone() {
        ResizeBy a = new ResizeBy(getDirector());
        a.initWithDuration(_duration, _sizeDelta);
        return a;

    }

    @Override
    public ResizeBy reverse() {
        Size newSize = new Size(-_sizeDelta.width, -_sizeDelta.height);
        return ResizeBy.create(getDirector(), _duration, newSize);
    }

    @Override
    public void update(float t) {
        if (_target!=null) {
            _target.setContentSize(_startSize.add(_sizeDelta.multiply(t)));
        }
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _previousSize.set(_startSize.set(target.getContentSize()));
    }

    protected boolean initWithDuration(float duration, final Size deltaSize) {
        super.initWithDuration(duration);
        _sizeDelta.set(deltaSize);
        return true;
    }

    protected Size _sizeDelta = new Size(0, 0);
    protected Size _startSize = new Size(0, 0);
    protected Size _previousSize = new Size(0, 0);
}
