package com.interpark.smframework.base.transition;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.EaseSineOut;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.MoveTo;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMSolidRectView;

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
    public void render(float a) {
        if (isDimLayerEnable() && _lastProgress > 0 && _dimLayer==null) {
            _dimLayer = new SMSolidRectView(getDirector());
            _dimLayer.setContentSize(new Size(getDirector().getWidth(), getDirector().getHeight()));
            _dimLayer.setAnchorPoint(new Vec2(0.5f, 0.5f));
            _dimLayer.setPosition(new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2));
            _dimLayer.setBackgroundColor(new Color4F(0, 0, 0, 0));
        }

        if (_isInSceneOnTop) {
            // new scene entered!!
            _director.pushProjectionMatrix();
            {
                _outScene.transformMatrix(_director.getProjectionMatrix());
                _director.updateProjectionMatrix();
                _outScene.renderFrame(a);
            }
            _director.popProjectionMatrix();

            if (_lastProgress>0.0f && _lastProgress<1.0f && _dimLayer!=null) {
                _director.pushProjectionMatrix();
                {
                    _dimLayer.transformMatrix(_director.getProjectionMatrix());
                    _director.updateProjectionMatrix();
                    float alpha = 0.4f*_lastProgress;
                    _dimLayer.setBackgroundColor(new Color4F(0, 0, 0, alpha));
                    _dimLayer.renderFrame(a);
                }
                _director.popProjectionMatrix();


            }

            _director.pushProjectionMatrix();
            {
                _inScene.transformMatrix(_director.getProjectionMatrix());
                _director.updateProjectionMatrix();
                _inScene.renderFrame(a);
            }
            _director.popProjectionMatrix();

        } else {
            // top scene exist
            _director.pushProjectionMatrix();
            {
                _inScene.transformMatrix(_director.getProjectionMatrix());
                float minusScale = 0.6f*_lastProgress;
                _inScene.setScale(1.6f-minusScale);
                _director.updateProjectionMatrix();
                _inScene.renderFrame(a);
            }
            _director.popProjectionMatrix();

            if (_lastProgress>0.0f && _lastProgress<1.0f && _dimLayer!=null) {
                _dimLayer.setAlpha(0.4f * (1.0f-_lastProgress));
                _director.pushProjectionMatrix();
                {
                    _dimLayer.transformMatrix(_director.getProjectionMatrix());
                    _director.updateProjectionMatrix();
                    _dimLayer.renderFrame(a);
                }
                _director.popProjectionMatrix();
            }
            _director.pushProjectionMatrix();
            {
                _outScene.transformMatrix(_director.getProjectionMatrix());
                _director.updateProjectionMatrix();
                _outScene.renderFrame(a);
            }
            _director.popProjectionMatrix();
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
