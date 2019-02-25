package com.interpark.smframework.base.types;

import android.support.v4.content.PermissionChecker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class ScaleTo extends ActionInterval {
    public ScaleTo(IDirector director) {
        super(director);
    }

    public static ScaleTo create(IDirector director, float duration, float s) {
        ScaleTo scaleTo = new ScaleTo(director);
        scaleTo.initWithDuration(duration, s);
        return scaleTo;
    }

    public static ScaleTo create(IDirector director, float duration, float sx, float sy) {
        ScaleTo scaleTo = new ScaleTo(director);
        scaleTo.initWithDuration(duration, sx, sy);
        return scaleTo;
    }

    public static ScaleTo create(IDirector director, float duration, float sx, float sy, float sz) {
        ScaleTo scaleTo = new ScaleTo(director);
        scaleTo.initWithDuration(duration, sx, sy, sz);
        return scaleTo;
    }

    @Override
    public ScaleTo clone() {
        return ScaleTo.create(getDirector(), _duration, _endScaleX, _endScaleY, _endScaleZ);
    }

    @Override
    public ScaleTo reverse() {
        return null;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _startScaleX = target.getScaleX();
        _startScaleY = target.getScaleY();
        _startScaleZ = target.getScaleZ();
        _deltaX = _endScaleX - _startScaleX;
        _deltaY = _endScaleY - _startScaleY;
        _deltaZ = _endScaleZ - _startScaleZ;
    }

    @Override
    public void update(float t) {
        if (_target!=null) {
            _target.setScaleX(_startScaleX + _deltaX*t);
            _target.setScaleY(_startScaleY + _deltaY*t);
            _target.setScaleZ(_startScaleZ + _deltaZ*t);
        }
    }

    protected boolean initWithDuration(float duration, float s) {
        super.initWithDuration(duration);
        _endScaleX = s;
        _endScaleY = s;
        _endScaleZ = s;
        return true;
    }

    protected boolean initWithDuration(float duration, float sx, float sy) {
        super.initWithDuration(duration);
        _endScaleX = sx;
        _endScaleY = sy;
        _endScaleZ = 1.0f;
        return true;
    }

    protected boolean initWithDuration(float duration, float sx, float sy, float sz) {
        _endScaleX = sx;
        _endScaleY = sy;
        _endScaleZ = sz;
        return true;
    }

    protected float _scaleX = 1.0f;
    protected float _scaleY = 1.0f;
    protected float _scaleZ = 1.0f;
    protected float _startScaleX = 1.0f;
    protected float _startScaleY = 1.0f;
    protected float _startScaleZ = 1.0f;
    protected float _endScaleX = 1.0f;
    protected float _endScaleY = 1.0f;
    protected float _endScaleZ = 1.0f;
    protected float _deltaX = 1.0f;
    protected float _deltaY = 1.0f;
    protected float _deltaZ = 1.0f;
}
