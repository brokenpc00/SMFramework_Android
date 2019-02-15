package com.interpark.smframework.base.transition;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.EaseCubicActionOut;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.MoveTo;
import com.interpark.smframework.util.Vec2;

public class SlideInToTop extends BaseSceneTransition {
    public SlideInToTop(IDirector director) {
        super(director);
    }

    public static SlideInToTop create(IDirector director, float t, SMScene inScene) {
        SlideInToTop scene = new SlideInToTop(director);
        if (scene!=null && scene.initWithDuration(t, inScene)) {
            return scene;
        }

        return null;
    }

    @Override
    public FiniteTimeAction getInAction() {
//        _inScene.setPosition(0, getDirector().getWinSize().height);
//        return EaseCubicActionOut.create(getDirector(), MoveTo.create(getDirector(), _duration, new Vec2(0, 0)));
        _inScene.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height+getDirector().getWinSize().height/2);
        return EaseCubicActionOut.create(getDirector(), MoveTo.create(getDirector(), _duration, new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2)));
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
