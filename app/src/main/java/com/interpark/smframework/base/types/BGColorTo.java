package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class BGColorTo extends ActionInterval {

    public BGColorTo(IDirector director) {
        super(director);
    }

    public static BGColorTo create(IDirector director, float duration, final Color4F color) {
        BGColorTo bgColorTo = new BGColorTo(director);
        if (bgColorTo!=null && bgColorTo.initWithDuration(duration)) {
            bgColorTo._toColor = new Color4F(color);
        }

        return bgColorTo;
    }

    @Override
    public void startWithTarget(SMView target) {
        if (target!=null) {
            super.startWithTarget(target);
            _startColor = new Color4F(target.getBackgroundColor());
            _deltaColor = _toColor.minus(_startColor);
        }
    }

    @Override
    public void update(float dt) {
        if (_target!=null) {
            Color4F color = _startColor.add(_deltaColor.multiply(dt));
            _target.setBackgroundColor(color);
        }
    }

    protected Color4F _startColor;
    protected Color4F _toColor;
    protected Color4F _deltaColor;
}
