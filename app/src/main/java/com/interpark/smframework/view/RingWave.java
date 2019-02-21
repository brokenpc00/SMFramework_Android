package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.ActionInterval;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayTime;
import com.interpark.smframework.base.types.EaseOut;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class RingWave extends SMView {
    public RingWave(IDirector director) {
        super(director);
    }

    public static RingWave show(IDirector director, SMView parent, float x, float y, float size, float duration, float delay) {
        return show(director, parent, x, y, size, duration, delay, null);
    }
    public static RingWave show(IDirector director, SMView parent, float x, float y, float size, float duration, float delay, Color4F color) {
        RingWave wave = new RingWave(director);
        if (wave.initWithParam(size, duration, delay, color)) {
            if (parent!=null) {
                parent.addChild(wave);
                wave.setPosition(new Vec2(x, y));
            }
        }

        return wave;
    }

    public void setWaveColor(Color4F color) {
        if (color!=null && _circle!=null) {
            _circle.setLineColor(color);
        }
    }

    protected boolean initWithParam(float size, float duration, float delay, Color4F color) {
        setAnchorPoint(new Vec2(Vec2.MIDDLE));

        _circle = SMCircleView.create(getDirector());

        addChild(_circle);

        _circle.setAnchorPoint(Vec2.MIDDLE);
        _circle.setPosition(Vec2.ZERO);

        if (color!=null) {
            _circle.setLineColor(color);
        }
        _circle.setAlpha(0);

        Action action = null;
        EaseOut wave = EaseOut.create(getDirector(), WaveCircleActionCreate(getDirector(), duration, _circle, size), 2.0f);
        if (delay>0) {
            action = Sequence.create(getDirector(), DelayTime.create(getDirector(), delay), wave, null);
        } else {
            action = wave;
        }

        runAction(action);
        return true;
    }

    private WaveCircleAction WaveCircleActionCreate(IDirector director, float duration, SMShapeView shape, float size) {
        WaveCircleAction action = new WaveCircleAction(director);
        action.initWithDuration(duration);
        action._shape = shape;
        action._size = size;
        return action;
    }

    public class WaveCircleAction extends ActionInterval {
        public WaveCircleAction(IDirector director) {
            super(director);
        }

        @Override
        public void update(float t) {
            float r1 = (float)(_size * Math.sin(t * M_PI/2));
            float r2 = (float)(_size * (1.0f - Math.cos(t * M_PI/2)));

            float d = r1 - r2;
            float a = (float)(Math.sin(t*M_PI));

            _shape.setAlpha(0.7f * a);
            _shape.setContentSize(new Size(r1, r2));
            _shape.setLineWidth(d / 4.0f);

            if (t >= 1) {
                _target.removeFromParentAndCleanup(true);
            }
        }

        private SMShapeView _shape = null;
        private float _size;
    }

    protected SMCircleView _circle;

}
