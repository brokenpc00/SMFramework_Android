package com.interpark.smframework.base.transition;

import android.telecom.Call;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMView;
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

    public void BaseTransitionDraw(float a) {
        if (isDimLayerEnable() && _lastProgress > 0 && _dimLayer==null) {
            _dimLayer = new SMSolidRectView(getDirector());
            _dimLayer.setContentSize(new Size(getDirector().getWidth(), getDirector().getHeight()));
            _dimLayer.setAnchorPoint(new Vec2(0.5f, 0.5f));
            _dimLayer.setPosition(new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2));
            _dimLayer.setColor(0, 0, 0, 0);
        }

        if (_isInSceneOnTop) {
            // new scene entered!!
            _outScene.visit(a);

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.OX) {
                _menuDrawContainer.setVisible(true);
                _menuDrawContainer.visit(a);
            }


            if (_lastProgress>0.0f && _lastProgress<1.0f && _dimLayer!=null) {
                    float alpha = 0.4f*_lastProgress;
                _dimLayer.setColor(new Color4F(0,0, 0, alpha));
                _dimLayer.visit(a);
                }

            _inScene.visit(a);

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.XO) {
                _menuDrawContainer.setVisible(true);
                _menuDrawContainer.visit(a);
            }
        } else {
            // top scene exist
            _inScene.visit(a);

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.XO) {
                _menuDrawContainer.setVisible(true);
                _menuDrawContainer.visit(a);
            }

            if (_lastProgress>0.0f && _lastProgress<1.0f && _dimLayer!=null) {
                _dimLayer.setColor(new Color4F(0,0, 0, 0.4f * (1.0f-_lastProgress)));
                _dimLayer.visit(a);
            }

            _outScene.visit(a);

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.OX) {
                _menuDrawContainer.setVisible(true);
                _menuDrawContainer.visit(a);
            }
        }
    }

    @Override
    protected void draw(float a) {
        BaseTransitionDraw(a);
    }


    // protected
    @Override
    public void onEnter() {
        super.onEnter();

//        setBackgroundColor(new Color4F(1, 0, 0, 0.3f));

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

        if (_menuDrawType == MenuDrawType.OX || _menuDrawType == MenuDrawType.XO) {
//            _menuDrawContainer = SMView.create(getDirector());
            // Todo... make If you need another menu
        }

        FiniteTimeAction in = getInAction();
        FiniteTimeAction out = getOutAction();

        if (in==null) {
            in = DelayTime.create(getDirector(), _duration);
        }

        if (_isInSceneOnTop) {
           Sequence seq = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), in, null);
           _inScene.runAction(seq);
           if (out!=null) {
               Sequence seq2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), out, null);
               _outScene.runAction(seq2);
           }

           runAction(Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), new ProgressUpdater(getDirector(), _duration), CallFunc.create(getDirector(), new PERFORM_SEL() {
                @Override
               public void performSelector() {
                    finish();
                }
           }), null));
        } else {
//            Sequence seq = Sequence.create(getDirector(), in, null);
            _inScene.runAction(in);

            // 타이밍이 안 맞아 하얀게 자꾸 보여서 delay 줌...
            if (out!=null) {
                Sequence seq2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), out, null);
                _outScene.runAction(seq2);
            }

            // progress for transition scene
//            runAction(new ProgressUpdater(getDirector(), _duration));
            runAction(Sequence.create(getDirector(), new ProgressUpdater(getDirector(), _duration), CallFunc.create(getDirector(), new PERFORM_SEL() {
                @Override
                public void performSelector() {
                    finish();
                }
            }), null));
        }

        if (!isNewSceneEnter()) {
            SMScene outScene = _outScene;
            SMScene inScene = _inScene;
            inScene.onSceneResult(outScene, outScene.getSceneParams());
        }

        if (_isInSceneOnTop) {
            _outScene.onTransitionStart(Transition.PAUSE, getTag());
            _inScene.onTransitionStart(Transition.IN, getTag());
        } else {
            _outScene.onTransitionStart(Transition.OUT, getTag());
            _inScene.onTransitionStart(Transition.RESUME, getTag());
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        // event dispatcher enable
        getDirector().setTouchEventDispatcherEnable(true);

        if (_isInSceneOnTop) {
            _outScene.onTransitionComplete(Transition.PAUSE, getTag());
            _inScene.onTransitionComplete(Transition.SWIPE_IN, getTag());
        } else {
            _outScene.onTransitionComplete(Transition.SWIPE_OUT, getTag());
            _inScene.onTransitionComplete(Transition.RESUME, getTag());
    }
        _outScene.onExit();
        _inScene.onEnterTransitionDidFinish();

        if (_menuDrawContainer!=null) {
            // Todo... If you need another menu
        }
    }
//    public void onExit() {
//        super.onExit();
//
//        if (_menuDrawContainer!=null) {
//            // Todo... If you need another menu
//        }
//    }

    protected FiniteTimeAction getInAction() {return null;}

    protected FiniteTimeAction getOutAction() {return null;}

    protected boolean isDimLayerEnable() {return true;}

    protected void updateProgress(final float progress) {
        if (_lastProgress!=progress) {
            if (_isInSceneOnTop) {
                if (_inScene!=null) {
                    _inScene.onTransitionProgress(Transition.IN, getTag(), progress);
                }
                if (_outScene!=null) {
                    _outScene.onTransitionProgress(Transition.PAUSE, getTag(), progress);
                }
            } else {
                if (_inScene!=null) {
                    _inScene.onTransitionProgress(Transition.RESUME, getTag(), progress);
                }
                if (_outScene!=null) {
                    _outScene.onTransitionProgress(Transition.OUT, getTag(), progress);
                }
            }
            _lastProgress = progress;
        }
    }

    protected void updateComplete() {
        if (_isInSceneOnTop) {
            _outScene.onTransitionComplete(Transition.PAUSE, getTag());
            _inScene.onTransitionComplete(Transition.IN, getTag());
        } else {
            _outScene.onTransitionComplete(Transition.OUT, getTag());
            _inScene.onTransitionComplete(Transition.RESUME, getTag());
        }
//        _inScene.onEnter();
        _inScene.onEnterTransitionDidFinish();
    }

    protected boolean isNewSceneEnter() {return false;}

    protected enum MenuDrawType {
        OO,
        OX,
        XO,
        XX
    }
    protected MenuDrawType _menuDrawType = MenuDrawType.OO;


    protected SMView _menuDrawContainer = null;

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
