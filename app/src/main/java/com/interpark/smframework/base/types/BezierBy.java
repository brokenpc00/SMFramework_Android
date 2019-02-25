package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Vec2;

public class BezierBy extends ActionInterval {
    public BezierBy(IDirector director) {
        super(director);
    }

    public class ccBezierConfig {
        public ccBezierConfig() {}
        public void set(ccBezierConfig c) {
            this.endPosition.set(c.endPosition);
            this.controlPoint_1.set(c.controlPoint_1);
            this.controlPoint_2.set(c.controlPoint_2);
        }

        public Vec2 endPosition = new Vec2(0, 0);
        public Vec2 controlPoint_1 = new Vec2(0, 0);
        public Vec2 controlPoint_2 = new Vec2(0, 0);
    }

    public static BezierBy create(IDirector director, float t, final ccBezierConfig c) {
        BezierBy bezierBy = new BezierBy(director);
        bezierBy.initWithDuration(t, c);
        return bezierBy;
    }

    @Override
    public BezierBy clone() {
        return BezierBy.create(getDirector(), _duration, _config);
    }

    @Override
    public BezierBy reverse() {
        ccBezierConfig r = new ccBezierConfig();

        r.endPosition.set(-_config.endPosition.x, -_config.endPosition.y);
        r.controlPoint_1.set(_config.controlPoint_2.add(new Vec2(-_config.endPosition.x, -_config.endPosition.y)));
        r.controlPoint_2.set(_config.controlPoint_1.add(new Vec2(-_config.endPosition.x, -_config.endPosition.y)));

        BezierBy action = BezierBy.create(getDirector(), _duration, r);
        return action;
    }

    public static float bezierat( float a, float b, float c, float d, float t )
    {
        return (float)(Math.pow(1-t,3) * a + 3*t*(Math.pow(1-t,2))*b + 3*Math.pow(t,2)*(1-t)*c + Math.pow(t,3)*d );
    }

    @Override
    public void update(float t) {
        if (_target!=null) {
            float xa = 0;
            float xb = _config.controlPoint_1.x;
            float xc = _config.controlPoint_2.x;
            float xd = _config.endPosition.x;

            float ya = 0;
            float yb = _config.controlPoint_1.y;
            float yc = _config.controlPoint_2.y;
            float yd = _config.endPosition.y;

            float x = bezierat(xa, xb, xc, xd, t);
            float y = bezierat(ya, yb, yc, yd, t);

            Vec2 currentPos = new Vec2(_target.getPosition());
            Vec2 diff = new Vec2(currentPos.minus(_previousPosition));
            _startPosition.addLocal(diff);

            Vec2 newPos = new Vec2(_startPosition.add(new Vec2(x,y)));
            _target.setPosition(newPos);

            _previousPosition.set(newPos);
        }
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        _previousPosition.set(_startPosition.set(target.getPosition()));
    }

    protected boolean initWithDuration(float t, final ccBezierConfig c) {
        super.initWithDuration(t);
        _config.set(c);

        return true;
    }

    protected ccBezierConfig _config = new ccBezierConfig();
    protected Vec2 _startPosition = new Vec2(0, 0);
    protected Vec2 _previousPosition = new Vec2(0, 0);
}
