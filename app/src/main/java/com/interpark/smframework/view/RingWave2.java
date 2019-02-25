package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.ActionInterval;
import com.interpark.smframework.base.types.DelayTime;
import com.interpark.smframework.base.types.FadeIn;
import com.interpark.smframework.base.types.RepeatForever;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;

public class RingWave2 extends SMView {
    public RingWave2(IDirector director) {
        super(director);
    }

    public static RingWave2 create(IDirector director, float minRadius, float maxRadius) {
        return create(director, minRadius, maxRadius, 0);
    }
    public static RingWave2 create(IDirector director, float minRadius, float maxRadius, float startDelay) {
        RingWave2 wave = new RingWave2(director);
        wave.initWithParam(minRadius, maxRadius, startDelay);
        return wave;
    }

    public void hide() {
        TransformAction action = TransformAction.create(getDirector());
        action.toAlpha(0).removeOnFinish();
        action.setTimeValue(0.5f, 0);
        runAction(action);
    }

    private boolean initWithParam(float minRadius, float maxRadius, float startDelay) {
        setAnchorPoint(Vec2.MIDDLE);

        _circle = SMCircleView.create(getDirector());
        _circle.setAnchorPoint(Vec2.MIDDLE);
        addChild(_circle);

        if (startDelay > 0) {
            Sequence action = Sequence.create(getDirector(), DelayTime.create(getDirector(), startDelay),
                                                                        RepeatForever.create(getDirector(), Sequence.create(getDirector(), WaveActionCreate(getDirector(), 0.6f, minRadius, maxRadius),
                                                                                                                                                    DelayTime.create(getDirector(), 0.1f), null
                                                                                                                            )), null
                                                );
            _circle.runAction(action);
        } else {
            RepeatForever action = RepeatForever.create(getDirector(), Sequence.create(getDirector(), WaveActionCreate(getDirector(), 0.6f, minRadius, maxRadius),
                                                                                                                DelayTime.create(getDirector(), 0.1f), null
                                                                                        )
                                                        );
            runAction(action);
        }

        runAction(FadeIn.create(getDirector(), 0.2f));
        return true;
    }

    protected SMCircleView _circle = null;

    public WaveAction WaveActionCreate(IDirector director, float duration, float minRadius, float maxRadius) {
        WaveAction action = new WaveAction(director);
        action.initWithDuration(duration);
        action._minRadius = minRadius;
        action._maxRadius = maxRadius;

        return action;
    }
    public class WaveAction extends ActionInterval {
        public WaveAction(IDirector director) {
            super(director);
        }

        @Override
        public void update(float t) {
            SMCircleView ring = (SMCircleView)_target;

            float d = _maxRadius - _minRadius;
            float outR = _minRadius + d * tweenfunc.cubicEaseOut(t);
            float inR = _minRadius + d * tweenfunc.cubicEaseIn(t);
            ring.setContentSize(new Size(outR*2, outR*2));
            ring.setLineWidth(outR-inR);

            ring.setTintAlpha(1.0f - tweenfunc.sineEaseIn(t));
        }

        private float _minRadius = 0;
        private float _maxRadius = 0;
    }
}
