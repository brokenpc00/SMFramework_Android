package com.interpark.smframework.base.transition;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.EaseCubicActionOut;
import com.interpark.smframework.base.types.EaseSineOut;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.base.types.MoveTo;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;
import com.interpark.smframework.view.SMSolidRectView;

public class SlideOutToRight extends BaseSceneTransition {
    public SlideOutToRight(IDirector director) {
        super(director);
    }

    public static SlideOutToRight create(IDirector director, float t, SMScene inScene) {
        SlideOutToRight scene = new SlideOutToRight(director);
        if (scene!=null && scene.initWithDuration(t, inScene)) {
            return scene;
        }

        return null;
    }

    @Override
    public FiniteTimeAction getInAction() {
        _inScene.setPosition(-getDirector().getWinSize().width * 0.3f+getDirector().getWinSize().width/2f, getDirector().getWinSize().height/2f);
        TransformAction action = TransformAction.create(getDirector());
        action.toPositionX(getDirector().getWinSize().width/2).setTimeValue(_duration, 0);
        return action;
    }

    @Override
    public FiniteTimeAction getOutAction() {
        TransformAction action = TransformAction.create(getDirector());
        action.toPositionX(getDirector().getWinSize().width+getDirector().getWinSize().width/2).setTweenFunc(tweenfunc.TweenType.Sine_EaseOut).setTimeValue(_duration, 0);
        return action;
    }


    @Override
    protected void draw(final Mat4 m, int flags) {
        if (isDimLayerEnable() && _lastProgress > 0 && _dimLayer==null) {
            _dimLayer = new SMSolidRectView(getDirector());
            _dimLayer.setContentSize(new Size(getDirector().getWidth(), getDirector().getHeight()));
            _dimLayer.setAnchorPoint(Vec2.MIDDLE);
            _dimLayer.setPosition(new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2));
            _dimLayer.setColor(Color4F.TRANSPARENT);
        }

        if (_isInSceneOnTop) {
            // new scene entered!!
            _outScene.visit(m, flags);

            if (_lastProgress>0.0f && _lastProgress<1.0f && _dimLayer!=null) {
                    float alpha = 0.4f*_lastProgress;
                _dimLayer.setColor(new Color4F(0, 0, 0, alpha));
                _dimLayer.visit(m, flags);
            }

            _inScene.visit(m, flags);

        } else {
            // top scene exist
                float minusScale = 0.6f*_lastProgress;
                _inScene.setScale(1.6f-minusScale);
            _inScene.visit(m, flags);

            if (_lastProgress>0.0f && _lastProgress<1.0f && _dimLayer!=null) {
                _dimLayer.setColor(new Color4F(0, 0, 0, 0.4f * (1.0f-_lastProgress)));
                _dimLayer.visit(m, flags);
        }
            _outScene.visit(m, flags);
    }
    }

    @Override
    protected boolean isNewSceneEnter() {
        return false;
    }

    @Override
    protected void sceneOrder() {
        _isInSceneOnTop = false;
    }
}
