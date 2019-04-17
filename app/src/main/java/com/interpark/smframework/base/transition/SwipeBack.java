package com.interpark.smframework.base.transition;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.Mat4;
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

    @Override
    protected void updateProgress(final float progress) {
        if (_lastProgress!=progress) {
            if (_isInSceneOnTop) {
                if (_inScene!=null) {
                    _inScene.onTransitionProgress(Transition.SWIPE_IN, getTag(), progress);
                }
                if (_outScene!=null) {
                    _outScene.onTransitionProgress(Transition.PAUSE, getTag(), progress);
                }
            } else {
                if (_inScene!=null) {
                    _inScene.onTransitionProgress(Transition.RESUME, getTag(), progress);
                }
                if (_outScene!=null) {
                    _outScene.onTransitionProgress(Transition.SWIPE_OUT, getTag(), progress);
                }
            }
            _lastProgress = progress;
        }
    }

    @Override
    protected void draw(final Mat4 m, int flags) {
        float progress = (_outScene.getPositionX()-getDirector().getWinSize().width/2) / getDirector().getWinSize().width;
        updateProgress(progress);

        if (_menuDrawContainer!=null) {
            // Todo... If you need another menu
        }
        super.draw(m, flags);
    }


    private SEL_SCHEDULE cancelFunc = new SEL_SCHEDULE() {
        @Override
        public void scheduleSelector(float t) {
            cancelNewScene(t);
        }
    };

    public void cancel() {

        // comback outScene.
        _isCanceled = true;

        _outScene.setVisible(true);
        _outScene.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2);
        _outScene.setScale(1.0f);
        _outScene.setRotation(0.0f);
        _outScene.onEnterTransitionDidFinish();

        _inScene.setVisible(false);
        _inScene.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2);
        _inScene.setScale(1.0f);
        _inScene.setRotation(0.0f);

        schedule(cancelFunc);
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


    @Override
    public void onEnter() {
        TransitionSceneOnEnter();
        // event dispatcher enable
        getDirector().setTouchEventDispatcherEnable(true);


        // start swipe back~
        if (_isInSceneOnTop) {
            _outScene.onTransitionStart(Transition.PAUSE, getTag());
            _inScene.onTransitionStart(Transition.SWIPE_IN, getTag());
        } else {
            _outScene.onTransitionStart(Transition.SWIPE_OUT, getTag());
            _inScene.onTransitionStart(Transition.RESUME, getTag());
        }

        boolean inMenu = _inScene.isMainMenuEnable();
        boolean outMenu = _outScene.isMainMenuEnable();

        if (outMenu) {
            if (inMenu) {
                _menuDrawType = MenuDrawType.OO;
            } else {
                _menuDrawType = MenuDrawType.OX;
            }
        } else {
            if (inMenu) {
                _menuDrawType = MenuDrawType.XO;
            } else {
                _menuDrawType = MenuDrawType.XX;
            }
        }

        if (_menuDrawContainer!=null) {
            // Todo... If you need another menu
        }
    }

    @Override
    public void onExit() {
        SMSceneOnExit();

        // event dispatcher enable
        getDirector().setTouchEventDispatcherEnable(true);

        if (_isCanceled) {
            if (_isInSceneOnTop) {
                _inScene.onTransitionComplete(Transition.SWIPE_OUT, getTag());
                _outScene.onTransitionComplete(Transition.RESUME, getTag());
                _outScene.onEnterTransitionDidFinish();
            } else {
                _outScene.onTransitionComplete(Transition.RESUME, getTag());
                _inScene.onTransitionComplete(Transition.SWIPE_OUT, getTag());
                _outScene.onEnterTransitionDidFinish();
            }
            _inScene.onExit();
        } else {
            if (_isInSceneOnTop) {
                _outScene.onTransitionComplete(Transition.PAUSE, getTag());
                _inScene.onTransitionComplete(Transition.SWIPE_IN, getTag());
            } else {
                _outScene.onTransitionComplete(Transition.SWIPE_OUT, getTag());
                _inScene.onTransitionComplete(Transition.RESUME, getTag());
        }
            _outScene.onExit();
    }

        if (_menuDrawContainer!=null) {
            // Todo... If you need another menu
    }
    }


    @Override
    public void sceneOrder() {
        _isInSceneOnTop = false;
    }

}
