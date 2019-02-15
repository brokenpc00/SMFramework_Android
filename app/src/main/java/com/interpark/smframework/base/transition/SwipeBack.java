package com.interpark.smframework.base.transition;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.base.types.SEL_SCHEDULE;

import java.security.AlgorithmConstraints;

public class SwipeBack extends BaseSceneTransition {
    public SwipeBack(IDirector director) {
        super(director);
    }

    public static SwipeBack create(IDirector director, SMScene scene) {
        SwipeBack t = new SwipeBack(director);
        if (t!=null && t.initWithDuration(0, scene)) {
            return t;
        }

        return null;
    }

    private SEL_SCHEDULE cancelFunc = new SEL_SCHEDULE() {
        @Override
        public void onFunc(float t) {
            cancelNewScene(t);
        }
    };

    public void cancel() {

        // 나가려던 scene이 다시 들어와야함.
        _isCanceled = true;

        _outScene.setVisible(true);
        _outScene.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2);
        _outScene.setScale(1.0f);
        _outScene.setRotation(0.0f);

        _inScene.setVisible(false);
        _inScene.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2);
        _inScene.setScale(1.0f);
        _inScene.setRotation(0.0f);

        schedule(cancelFunc);
    }

    @Override
    public void onEnter() {
        TransitionSceneOnEnter();
        // event dispatcher enable
        getDirector().setTouchEventDispatcherEnable(true);
    }

    @Override
    public void onExit() {
        SMSceneOnExit();

        // event dispatcher enable
        getDirector().setTouchEventDispatcherEnable(true);

        if (_isCanceled) {
            _inScene.onExit();
        } else {
            _outScene.onExit();
        }
    }

    @Override
    public void render(float a) {
        _lastProgress = _outScene.getX() / (getDirector().getWinSize().width+getDirector().getWinSize().width/2);
//        Log.i("SWIPEBACK", "[[[[[ last progress : " + _lastProgress);
        super.render(a);
    }

    @Override
    public void sceneOrder() {
        _isInSceneOnTop = false;
    }

    @Override
    protected boolean isNewSceneEnter() {
        return false;
    }

    protected void cancelNewScene(float dt) {
        unschedule(cancelFunc);

        getDirector().replaceScene(_inScene);
        getDirector().pushScene(_outScene);

        _inScene.setVisible(true);
    }

    protected boolean _isCanceled;
}
