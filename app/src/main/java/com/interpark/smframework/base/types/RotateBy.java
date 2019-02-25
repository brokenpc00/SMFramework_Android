package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Vec3;

public class RotateBy extends ActionInterval {
    public RotateBy(IDirector director) {
        super(director);
    }

    public static RotateBy create(IDirector director, float duration, float deltaAngle) {
        RotateBy rotate = new RotateBy(director);
        rotate.initWithDuration(duration, deltaAngle);
        return rotate;
    }

    public static RotateBy create(IDirector director, float duration, float deltaAngleZ_X, float deltaAngleZ_Y) {
        RotateBy rotate = new RotateBy(director);
        rotate.initWithDuration(duration, deltaAngleZ_X, deltaAngleZ_X);
        return rotate;
    }

    public static RotateBy create(IDirector director, float duration, final Vec3 deltaAngle3D) {
        RotateBy rotate = new RotateBy(director);
        rotate.initWithDuration(duration, deltaAngle3D);
        return rotate;
    }

    @Override
    public RotateBy clone() {
        RotateBy a = new RotateBy(getDirector());
        if (_is3D) {
            a.initWithDuration(_duration, _deltaAngle);
        } else {
            a.initWithDuration(_duration, _deltaAngle.x, _deltaAngle.y);
        }
        return a;
    }

    @Override
    public RotateBy reverse() {
        if (_is3D) {
            Vec3 v = new Vec3(-_deltaAngle.x, -_deltaAngle.y, -_deltaAngle.z);
            return RotateBy.create(getDirector(), _duration, v);
        } else {
            return RotateBy.create(getDirector(), _duration, -_deltaAngle.x, -_deltaAngle.y);
        }
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        if (_is3D) {
            _startAngle = target.getPosition3D();
        } else {
            _startAngle.x = target.getRotationSkewX();
            _startAngle.y = target.getRotationSkewY();
        }
    }

    @Override
    public void update(float t) {
        if (_target!=null) {
            if (_is3D) {
                Vec3 v = new Vec3(_startAngle.x + _deltaAngle.x*t, _startAngle.y + _deltaAngle.y*t, _startAngle.z + _deltaAngle.z*t);
                _target.setRotation3D(v);
            } else {
                if (_startAngle.x==_startAngle.y && _deltaAngle.x==_deltaAngle.y) {
                    _target.setRotation(_startAngle.x + _deltaAngle.x*t);
                } else {
                    _target.setRotationSkewX(_startAngle.x + _deltaAngle.x*t);
                    _target.setRotationSkewY(_startAngle.y + _deltaAngle.y*t);
                }
            }
        }
    }

    protected boolean initWithDuration(float duration, float deltaAngle) {
        super.initWithDuration(duration);
        _deltaAngle.x = _deltaAngle.y = deltaAngle;
        return true;
    }
    protected boolean initWithDuration(float duration, float deltaAngleZ_X, float deltaAngleZ_Y) {
        super.initWithDuration(duration);
        _deltaAngle.x = deltaAngleZ_X;
        _deltaAngle.y = deltaAngleZ_Y;
        return true;
    }
    protected boolean initWithDuration(float duration, final Vec3 deltaAngle3D) {
        super.initWithDuration(duration);
        _deltaAngle.set(deltaAngle3D);
        _is3D = true;
        return true;
    }

    protected boolean _is3D = false;
    protected Vec3 _deltaAngle = new Vec3(0, 0, 0);
    protected Vec3 _startAngle = new Vec3(0, 0, 0);
}
