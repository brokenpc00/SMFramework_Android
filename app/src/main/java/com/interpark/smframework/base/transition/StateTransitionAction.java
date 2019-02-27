package com.interpark.smframework.base.transition;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.ActionInterval;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.view.SMButton;

public class StateTransitionAction extends ActionInterval {
    public StateTransitionAction(IDirector director) {
        super(director);
    }

    public static StateTransitionAction create(IDirector director, SMView.STATE toState) {
        StateTransitionAction aciton = new StateTransitionAction(director);

        if (aciton!=null && aciton.initWithDuration(0)) {
            aciton._toState = toState;
        }

        return aciton;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);

        int tag;
        if (_toState==SMView.STATE.PRESSED) {
            tag = AppConst.TAG.ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL;
        } else {
            tag = AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS;
        }

        ActionInterval action = (ActionInterval)target.getActionByTag(tag);
        if (action!=null) {
            target.stopAction(action);
            float run = action.getElapsed() / action.getDuration();
            if (run<1) {
                _firstTick = false;
                _elapsed = getDuration() * (1-run);
            }
        }
    }

    @Override
    public void update(float t) {
        if (_target!=null) {
            SMButton btn = (SMButton)_target;
            if (btn!=null) {
                ((SMButton)_target).onUpdateStateTransition(_toState, _toState==SMView.STATE.PRESSED?t:1-t);
            }
        }

    }

    private SMView.STATE _toState;
}
