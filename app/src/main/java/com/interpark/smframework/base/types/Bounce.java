package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.AppConst;

import static com.interpark.smframework.util.tweenfunc.M_PI;
import static com.interpark.smframework.util.tweenfunc.M_PI_2;

public class Bounce extends ActionInterval {
    public Bounce(IDirector director) {
        super(director);
    }

    public static Bounce create(IDirector director, float duration, final float bounceRate, int bounceCount) {
        return create(director, duration, bounceRate, bounceCount, 1.0f);
    }
    public static Bounce create(IDirector director, float duration, final float bounceRate, int bounceCount, float maxScale) {
        return create(director, duration, bounceRate, bounceCount, maxScale, null);
    }
    public static Bounce create(IDirector director, float duration, final float bounceRate, int bounceCount, float maxScale, SMView target) {
        Bounce action = new Bounce(director);
        if (action!=null && action.initWithDuration(duration)) {
            action._bounceRate = bounceRate;
            action._bounceCount = bounceCount;
            action._maxScale = maxScale;
            action._view = target;
        }

        return action;
    }

    @Override
    public void startWithTarget(SMView view) {
        super.startWithTarget(view);
        if (view!=null) {
            _view = view;
        }
    }

    @Override
    public void update(float dt) {
        float a = (float)Math.cos(dt * M_PI_2);
        float s = (float)(a * _bounceRate * Math.abs(Math.sin(dt * _bounceCount * M_PI)));
        _target.setScale((1.0f+s) * _maxScale);
    }

    protected float _bounceRate = 0.0f;
    protected int _bounceCount  = 0;
    protected float _maxScale = 1.0f;
    protected SMView _view = null;
}
