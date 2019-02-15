package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import static com.interpark.smframework.util.tweenfunc.M_PI;

public class ScaleSine extends ActionInterval {
    public ScaleSine(IDirector director) {
        super(director);
    }

    public static ScaleSine create(IDirector director, float duration, float maxScale) {
        ScaleSine action = new ScaleSine(director);
        if (action!=null && action.initWithDuration(duration)) {
            action._deltaScale = maxScale - 1.0f;
        }

        return action;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);
        if (target!=null) {
            _baseScale = target.getScale();
        }
    }

    @Override
    public void update(float t) {
        _target.setScale(_baseScale*(1.0f + _deltaScale * (float)Math.sin(t * M_PI)));
    }

    protected float _deltaScale;
    protected float _baseScale;
}
