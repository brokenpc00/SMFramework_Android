package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;

public class MoveBy extends ActionInterval {
    public MoveBy(IDirector director) {
        super(director);
    }

    public static MoveBy create(IDirector director, float duration, final Vec2 deltaPosition) {
        return create(director, duration, new Vec3(deltaPosition.x, deltaPosition.y, 0));
    }

    public static MoveBy create(IDirector director, float duration, final Vec3 deltaPosition3D) {
        MoveBy ret = new MoveBy(director);
        if (ret!=null && ret.initWithDuration(duration, deltaPosition3D)) {
            return ret;
        }

        return null;
    }

    @Override
    public MoveBy clone() {
        return MoveBy.create(getDirector(), _duration, _positionDelta);
    }

    @Override
    public MoveBy reverse() {
        return MoveBy.create(getDirector(), _duration, new Vec3(-_positionDelta.x, -_positionDelta.y, -_positionDelta.z));
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        Vec3 v3 = target.getPosition3D();
        _startPosition.set(v3);
        _previousPosition.set(v3);
    }

    @Override
    public void update(float time) {
        if (_target!=null) {
            Vec3 currentPos = new Vec3(_target.getPosition3D());
            Vec3 diff = new Vec3(currentPos.minus(_previousPosition));
            _startPosition.addLocal(diff);
            Vec3 newPos = new Vec3(_startPosition.add(_positionDelta.multiply(time)));
            _target.setPosition3D(newPos);
            _previousPosition.set(newPos);

//            _target.setPosition3D(_startPosition.add(_positionDelta.multiply(time)));
        }
    }

    protected boolean initWithDuration(float duration, final Vec2 deltaPosition) {
        return initWithDuration(duration, new Vec3(deltaPosition.x, deltaPosition.y, 0));
    }

    protected boolean initWithDuration(float duration, final Vec3 deltaPosition3D) {
        boolean ret = false;

        if (super.initWithDuration(duration)) {
            _positionDelta.set(deltaPosition3D);
            _is3D = true;
            ret = true;
        }

        return ret;
    }

    protected boolean _is3D = false;
    protected Vec3 _positionDelta = new Vec3(0, 0, 0);
    protected Vec3 _startPosition = new Vec3(0, 0, 0);
    protected Vec3 _previousPosition = new Vec3(0, 0, 0);
}
