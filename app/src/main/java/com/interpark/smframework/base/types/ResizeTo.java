package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Size;

public class ResizeTo extends ActionInterval {
    public ResizeTo(IDirector director) {
        super(director);
    }

    public static ResizeTo create(IDirector director, float duration, final Size filnal_size) {
        ResizeTo ret = new ResizeTo(director);
        ret.initWithDuration(duration, filnal_size);
        return ret;
    }

    @Override
    public ResizeTo clone() {
        ResizeTo a = new ResizeTo(getDirector());
        a.initWithDuration(_duration, _finalSize);
        return a;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);

        _initialSize.set(target.getContentSize());
        _sizeDelta.set(_finalSize.minus(_initialSize));
    }

    @Override
    public void update(float t) {
        if (_target!=null) {
            Size newSize = new Size(_initialSize.add(_sizeDelta.multiply(t)));
            _target.setContentSize(newSize);
        }
    }

    protected boolean initWithDuration(float duration, final Size final_size) {
        super.initWithDuration(duration);
        _finalSize.set(final_size);
        return true;
    }

    protected Size _initialSize = new Size(0, 0);
    protected Size _finalSize = new Size(0, 0);
    protected Size _sizeDelta = new Size(0, 0);
}