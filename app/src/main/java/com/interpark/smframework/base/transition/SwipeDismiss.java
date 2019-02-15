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
    public void render(float a) {
        _lastProgress = -_outScene.getY() / (getDirector().getWinSize().height+getDirector().getWinSize().height/2);
        BaseTransitionRender(a);
    }

    @Override
    protected boolean isNewSceneEnter() {
        return false;
    }

}
