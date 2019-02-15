package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;

public class MoveTo extends MoveBy {
    public MoveTo(IDirector director) {
        super(director);
    }

    public static MoveTo create(IDirector director, float duration, final Vec2 position) {
        return create(director, duration, new Vec3(position.x, position.y, 0));
    }

    public static MoveTo create(IDirector director, float duration, final Vec3 position3D) {
        MoveTo ret = new MoveTo(director);
        if (ret!=null && ret.initWithDuration(duration, position3D)) {
            return ret;
        }

        return null;
    }

    @Override
    public MoveTo clone() {
        return MoveTo.create(getDirector(), _duration, _endPosition);
    }

    @Override
    public MoveTo reverse() {
        assert (false);
        // no reverse
        return null;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _positionDelta.set(_endPosition.minus(target.getPosition3D()));
    }

    protected boolean initWithDuration(float duration, final Vec2 position) {
        return initWithDuration(duration, new Vec3(position.x, position.y, 0));
    }

    protected boolean initWithDuration(float duration, final Vec3 position3D) {
        boolean ret = false;
        if (super.initWithDuration(duration)) {
            _endPosition.set(position3D);
            ret = true;
        }

        return ret;
    }

    protected Vec3 _endPosition = new Vec3(0, 0, 0);
}
