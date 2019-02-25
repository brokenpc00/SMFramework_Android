package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Vec2;

public class BezierTo extends BezierBy {
    public BezierTo(IDirector director) {
        super(director);
    }

    public static BezierTo create(IDirector director, float t, final ccBezierConfig c) {
        BezierTo bezierTo = new BezierTo(director);
        bezierTo.initWithDuration(t, c);
        return bezierTo;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _config.controlPoint_1.set(_toConfig.controlPoint_1.minus(_startPosition));
        _config.controlPoint_2.set(_toConfig.controlPoint_2.minus(_startPosition));
        _config.endPosition.set(_toConfig.endPosition.minus(_startPosition));
    }

    @Override
    public BezierTo clone() {
        return BezierTo.create(getDirector(), _duration, _toConfig);
    }

    @Override
    public BezierTo reverse() {
        return null;
    }

    protected boolean initWithDuration(float t, final ccBezierConfig c) {
        super.initWithDuration(t);
        _toConfig.set(c);

        return true;
    }

    protected ccBezierConfig _toConfig = new ccBezierConfig();


}
