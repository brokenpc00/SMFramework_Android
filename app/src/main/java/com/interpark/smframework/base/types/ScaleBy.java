package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class ScaleBy extends ScaleTo {
    public ScaleBy(IDirector director) {
        super(director);
    }

    public static ScaleBy create(IDirector director, float duration, float s) {
        ScaleBy scaleBy = new ScaleBy(director);
        scaleBy.initWithDuration(duration, s);
        return scaleBy;
    }
    public static ScaleBy create(IDirector director, float duration, float sx, float sy) {
        ScaleBy scaleBy = new ScaleBy(director);
        scaleBy.initWithDuration(duration, sx, sy);
        return scaleBy;
    }
    public static ScaleBy create(IDirector director, float duration, float sx, float sy, float sz) {
        ScaleBy scaleBy = new ScaleBy(director);
        scaleBy.initWithDuration(duration, sx, sy, sz);
        return scaleBy;
    }

//    protected boolean initWithDuration(float duration, float s) {
//
//    }
//    protected boolean initWithDuration(float duration, float sx, float sy) {
//
//    }
//    protected boolean initWithDuration(float duration, float sx, float sy, float sz) {
//
//    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _deltaX = _startScaleX * _endScaleX - _startScaleX;
        _deltaY = _startScaleY * _endScaleY - _startScaleY;
        _deltaZ = _startScaleZ * _endScaleZ - _startScaleZ;
    }

    @Override
    public ScaleBy clone() {
        return ScaleBy.create(getDirector(), _duration, _endScaleX, _endScaleY, _endScaleZ);
    }
    @Override
    public ScaleBy reverse() {
        return ScaleBy.create(getDirector(), _duration, 1.0f/_endScaleX, 1.0f/_endScaleY, 1.0f/_endScaleZ);
    }
}
