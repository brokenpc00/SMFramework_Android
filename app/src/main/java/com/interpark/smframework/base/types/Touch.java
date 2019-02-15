package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.Vec2;

public class Touch extends Ref {
    public Touch(IDirector director) {
        super(director);
    }

    public enum DispatchMode {
        ALL_AT_ONCE,
        ONE_BY_ONE,
    }

    private int _id = 0;
    private boolean _startPointCaptured = false;
    private Vec2 _startPoint = new Vec2(0, 0);
    private Vec2 _point = new Vec2(0, 0);
    private Vec2 _prevPoint = new Vec2(0, 0);
    private float _curForce = 0.0f;
    private float _maxForce = 0.0f;

    public void setTouchInfo(int id, float x, float y) {
        setTouchInfo(id, x, y, 0, 0);
    }

    public void setTouchInfo(int id, float x, float y, float force, float maxForce) {
        _id = id;
        _prevPoint.set(_point.x, _point.y);
        _point.x = x;
        _point.y = y;
        _curForce = force;
        _maxForce = maxForce;
        if (!_startPointCaptured) {
            _startPoint.set(_point.x, _point.y);
            _startPointCaptured = true;
            _prevPoint.set(_point.x, _point.y);
        }
    }

    public int getID() {return _id;}


    public Vec2 getLocationInView() {
        return _point;
    }

    public Vec2 getPreviousLocationInView() {
        return _prevPoint;
    }

    public Vec2 getStartLocationInView() {
        return _startPoint;
    }

    public Vec2 getLocation() {
        // need convert to gl
        return _point;
    }

    public Vec2 getPreviousLocation() {
        // need converg to gl
        return _prevPoint;
    }

    public Vec2 getStartLocation() {
        // need convert to gl
        return _startPoint;
    }

    public Vec2 getDelta() {
        return new Vec2(_point.x - _prevPoint.x, _point.y - _prevPoint.y);
    }

    public float getCurrentForce() {
        return _curForce;
    }

    public float getMaxForce() {
        return _maxForce;
    }
}
