package com.interpark.smframework.base.transition;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.EaseCubicActionOut;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.MoveTo;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;
import com.interpark.smframework.view.SMSolidRectView;

public class SlideInToLeft extends BaseSceneTransition {
    public SlideInToLeft(IDirector director) {
        super(director);
    }

    public static SlideInToLeft create(IDirector director, float t, SMScene inScene) {
        SlideInToLeft scene = new SlideInToLeft(director);
        if (scene!=null && scene.initWithDuration(t, inScene)) {

            return scene;
        }

        return null;
    }

    @Override
    public FiniteTimeAction getInAction() {
        _inScene.setPosition(getDirector().getWinSize().width+getDirector().getWinSize().width/2, getDirector().getWinSize().height/2);

        TransformAction action = TransformAction.create(getDirector());
        action.toPositionX(getDirector().getWidth()/2).setTweenFunc(tweenfunc.TweenType.Cubic_EaseOut).setTimeValue(_duration, 0);
        return action;
//        return EaseCubicActionOut.create(getDirector(), MoveTo.create(getDirector(), _duration, new Vec2(0, 0)));
//        return EaseCubicActionOut.create(getDirector(), MoveTo.create(getDirector(), _duration, new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2)));
    }


    @Override
    public FiniteTimeAction getOutAction() {
//        return MoveTo.create(getDirector(), _duration, new Vec2(-getDirector().getWinSize().width * 0.2f, 0));
        TransformAction action = TransformAction.create(getDirector());
        action.toPositionX(-getDirector().getWinSize().width * 0.3f+getDirector().getWinSize().width/2).setTimeValue(_duration, 0);
        return action;
//        return MoveTo.create(getDirector(), _duration, new Vec2(-getDirector().getWinSize().width * 0.2f+getDirector().getWinSize().width/2, getDirector().getWinSize().height/2));
    }

    @Override
    protected boolean isNewSceneEnter() {
        return true;
    }

    @Override
    protected void sceneOrder() {
        _isInSceneOnTop = true;
    }
}
