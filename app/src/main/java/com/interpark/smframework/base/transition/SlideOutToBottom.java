package com.interpark.smframework.base.transition;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.EaseSineOut;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.base.types.MoveTo;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMSolidRectView;

import androidx.work.impl.model.DependencyDao_Impl;

public class SlideOutToBottom extends BaseSceneTransition {
    public SlideOutToBottom(IDirector director) {
        super(director);
    }

    public static SlideOutToBottom create(IDirector director, float t, SMScene inScene) {
        SlideOutToBottom scene = new SlideOutToBottom(director);
        if (scene!=null && scene.initWithDuration(t, inScene)) {
            return scene;
        }

        return null;
    }

    @Override
    protected void draw(final Mat4 m, int flags) {
        if (isDimLayerEnable() && _lastProgress > 0 && _dimLayer==null) {
            _dimLayer = new SMSolidRectView(getDirector());
            _dimLayer.setContentSize(new Size(getDirector().getWidth(), getDirector().getHeight()));
            _dimLayer.setAnchorPoint(new Vec2(0.5f, 0.5f));
            _dimLayer.setPosition(new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2));
            _dimLayer.setColor(Color4F.TRANSPARENT);
        }

        if (_isInSceneOnTop) {
            // new scene entered!!
            _outScene.visit(m, flags);
            if (_lastProgress>0.0f && _lastProgress<1.0f && _dimLayer!=null) {
                    float alpha = 0.4f*_lastProgress;
                _dimLayer.setColor(0, 0, 0, alpha);
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
    public FiniteTimeAction getOutAction() {
//        return EaseSineOut.create(getDirector(), MoveTo.create(getDirector(), _duration, new Vec2(0, getDirector().getWinSize().height)));
        return EaseSineOut.create(getDirector(), MoveTo.create(getDirector(), _duration, new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height+getDirector().getWinSize().height/2)));
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
