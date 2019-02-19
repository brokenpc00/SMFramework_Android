package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Vec3;

public class RotateTo extends ActionInterval {
    public RotateTo(IDirector director) {
        super(director);
    }

    public static RotateTo create(IDirector director, float duration, float dstAngleX, float dstAngleY) {
        RotateTo rotateTo = new RotateTo(director);
        if (rotateTo!=null && rotateTo.initWithDuration(duration, dstAngleX, dstAngleY)) {
            return rotateTo;
        }

        return null;
    }

    public static RotateTo create(IDirector director, float durtion, float dstAngle) {
        RotateTo rotateTo = new RotateTo(director);
        if (rotateTo!=null && rotateTo.initWithDuration(durtion, dstAngle, dstAngle)) {
            return rotateTo;
        }

        return null;
    }

    public static RotateTo create(IDirector director, float duratoin, Vec3 dstAngle3D) {
        RotateTo rotateTo = new RotateTo(director);
        if (rotateTo!=null && rotateTo.initWithDuration(duratoin, dstAngle3D)) {
            return rotateTo;
        }

        return null;
    }

    @Override
    public RotateTo clone() {
        RotateTo a = new RotateTo(getDirector());
        if (_is3D) {
            a.initWithDuration(_duration, _dstAngle);
        } else {
            a.initWithDuration(_duration, _dstAngle.x, _dstAngle.y);
        }

        return a;
    }

    @Override
    public RotateTo reverse() {
        assert (false);
        // no reverse
        return null;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);

        if (_is3D) {
            _startAngle = _target.getRotation3D();
        } else {
            _startAngle.x = _target.getRotationSkewX();
            _startAngle.y = _target.getRotationSkewY();
        }

        _startAngle.x = calculateAnglesStart(_startAngle.x);
        _diffAngle.x = calculateAnglesDiff(_startAngle.x, _dstAngle.x);

        _startAngle.y = calculateAnglesStart(_startAngle.y);
        _diffAngle.y = calculateAnglesDiff(_startAngle.y, _dstAngle.y);

        _startAngle.z = calculateAnglesStart(_startAngle.z);
        _diffAngle.z = calculateAnglesDiff(_startAngle.z, _dstAngle.z);
    }

    @Override
    public void update(float time) {
        if (_target!=null) {
            if (_is3D) {
                _target.setRotation3D(new Vec3(_startAngle.x+_diffAngle.x*time, _startAngle.y+_diffAngle.y*time, _startAngle.z+_diffAngle.z*time));
            } else {
                _target.setRotationSkewX(_startAngle.x + _diffAngle.x*time);
                _target.setRotationSkewY(_startAngle.y + _diffAngle.y*time);
            }
        }
    }

    protected boolean initWithDuration(float duration, float dstAngleX, float dstAngleY) {
        if (super.initWithDuration(duration)) {
            _dstAngle.x = dstAngleX;
            _dstAngle.y = dstAngleY;
            return true;
        }

        return false;
    }

    protected boolean initWithDuration(float duration, Vec3 dstAngle3D) {
        if (super.initWithDuration(duration)) {
            _dstAngle = new Vec3(dstAngle3D);
            _is3D = true;
            return true;
        }

        return false;
    }

    protected float calculateAnglesStart(float start) {
        if (start>0) {
            start = start % 360.0f;
        } else {
            start = start % -360.0f;
        }

        return start;
    }

    protected float calculateAnglesDiff(float start, float dst) {
        float diff = dst - start;
        if (diff > 180.0f) {
            diff -= 360.0f;
        }
        if (diff < -180.0f) {
            diff += 360.0f;
        }

        return diff;
    }


    protected boolean _is3D = false;
    protected Vec3 _dstAngle = new Vec3(0, 0, 0);
    protected Vec3 _startAngle = new Vec3(0, 0, 0);
    protected Vec3 _diffAngle = new Vec3(0, 0, 0);
}
