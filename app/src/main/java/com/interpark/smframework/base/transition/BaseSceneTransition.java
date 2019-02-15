package com.interpark.smframework.base.transition;

import android.telecom.Call;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.types.CallFunc;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayBaseAction;
import com.interpark.smframework.base.types.DelayTime;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMSolidRectView;

public class BaseSceneTransition extends TransitionScene {
    public BaseSceneTransition(IDirector director) {
        super(director);
    }

    public static float DEFAULT_DELAY_TIME = 0.1f;

    public SMScene getOutScene() {
        return _outScene;
    }

    public void BaseTransitionRender(float a) {
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
    public void render(float a) {
//        super.render(a);

        BaseTransitionRender(a);
    }


    // protected
    @Override
    public void onEnter() {
        super.onEnter();

        FiniteTimeAction in = getInAction();
        FiniteTimeAction out = getOutAction();

        if (in==null) {
            in = DelayTime.create(getDirector(), _duration);
        }

        if (_isInSceneOnTop) {
           Sequence seq = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), in, CallFunc.create(getDirector(), new PERFORM_SEL() {
                @Override
                public void onFunc() {
                    finish();
                }
            }), null);
           _inScene.runAction(seq);
           if (out!=null) {
               Sequence seq2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), out, null);
               _outScene.runAction(seq2);
           }

           runAction(Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), new ProgressUpdater(getDirector(), _duration), null));
        } else {
            Sequence seq = Sequence.create(getDirector(), in, CallFunc.create(getDirector(), new PERFORM_SEL() {
                @Override
                public void onFunc() {
                    finish();
                }
            }), null);
            _inScene.runAction(seq);

            // 타이밍이 안 맞아 하얀게 자꾸 보여서 delay 줌...
            if (out!=null) {
                Sequence seq2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), out, null);
                _outScene.runAction(seq2);
            }


//            if (out!=null) {
//                _outScene.runAction(out);
//            }

            runAction(new ProgressUpdater(getDirector(), _duration));
        }

        if (!isNewSceneEnter()) {
            SMScene outScene = _outScene;
            SMScene inScene = _inScene;
            inScene.onSceneResult(outScene, outScene.getSceneParams());
        }

        (_outScene).onTransitionStart(SMScene.Transition.OUT, getTag());
        (_inScene).onTransitionStart(SMScene.Transition.IN, getTag());
    }

    @Override
    public void onExit() {
        super.onExit();
    }

    protected FiniteTimeAction getInAction() {return null;}

    protected FiniteTimeAction getOutAction() {return null;}

    protected boolean isDimLayerEnable() {return true;}

    protected void updateProgress(final float progress) {
        if (_lastProgress!=progress) {
            if (_isInSceneOnTop) {
                if (_inScene!=null) {
                    _inScene.onTransitionProgress(SMScene.Transition.IN, getTag(), progress);
                }
                if (_outScene!=null) {
                    _outScene.onTransitionProgress(SMScene.Transition.PAUSE, getTag(), progress);
                }
            } else {
                if (_inScene!=null) {
                    _inScene.onTransitionProgress(SMScene.Transition.RESUME, getTag(), progress);
                }
                if (_outScene!=null) {
                    _outScene.onTransitionProgress(SMScene.Transition.OUT, getTag(), progress);
                }
            }
            _lastProgress = progress;
        }
    }

    protected void updateComplete() {
        _outScene.onTransitionComplete(SMScene.Transition.OUT, getTag());
        _inScene.onTransitionComplete(SMScene.Transition.IN, getTag());
    }

    protected boolean isNewSceneEnter() {return false;}



    protected SMSolidRectView _dimLayer = null;

    public class ProgressUpdater extends DelayBaseAction {

        public ProgressUpdater(IDirector director, float t) {
            super(director);
            setTimeValue(t, 0);
        }

        @Override
        public void onUpdate(float t) {
            ((BaseSceneTransition)_target).updateProgress(t);
        }

        @Override
        public void onEnd() {
            ((BaseSceneTransition)_target).updateComplete();
        }
    }
}
