package com.interpark.smframework.base.transition;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;

import javax.security.auth.login.LoginException;

public class SwipeDismiss extends SwipeBack {
    public SwipeDismiss(IDirector director) {
        super(director);
    }

    public static SwipeDismiss create(IDirector director, SMScene scene) {
        SwipeDismiss t = new SwipeDismiss(director);
        if (t!=null && t.initWithDuration(0, scene)) {
            return t;
        }

        return null;
    }

    @Override
    protected void draw(float a) {
        _lastProgress = -(_outScene.getY()-getDirector().getWinSize().height/2) / getDirector().getWinSize().height;

        if (_menuDrawContainer!=null) {
            // Todo... If you need another menu
        }

        BaseTransitionDraw(a);
    }

    @Override
    protected boolean isNewSceneEnter() {
        return false;
    }

}
