package com.interpark.smframework.base;

import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.transition.SlideInToLeft;
import com.interpark.smframework.base.transition.SlideInToTop;
import com.interpark.smframework.base.transition.SlideOutToBottom;
import com.interpark.smframework.base.transition.SlideOutToRight;
import com.interpark.smframework.base.transition.TransitionScene;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.BackPressable;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMScene extends SMView implements BackPressable {

    public static final int STATE_CREATING = -1;
    /**
     * 활성 상태
     * - SMScene stack의 최상단
     * */
    public static final int STATE_ACTIVATE = 0;

    /**
     * 비활성 상태
     * - SMScene stack의 최상단 상태가 아님 (비활성 상태)
     */
    public static final int STATE_PAUSED = 1;

    /**
     * 시작하고 있는 중
     * - 시작 애니메이션 동작중
     */
    public static final int STATE_SATRTING = 2;

    /**
     * 끝내고 있는중
     * - 종료 애니매이션 동작중
     */
    public static final int STATE_FINISHING = 3;

    /**
     * 상단으로 새로운 scene이 올라오고 있음
     */
    public static final int STATE_PAUSING = 4;

    /**
     * 상단의 scene이 제거되고 있음
     */
    public static final int STATE_RESUMING = 5;

    private int mState = STATE_CREATING;


    public enum SwipeType {
        NONE,
        MENU,
        BACK,
        DISMISS
    }

    public enum Transition {
        IN,
        OUT,
        PAUSE,
        RESUME,
        SWIPE_IN,
        SWIPE_OUT
    }

//    public static SMScene create(IDirector director) {
//        SMScene scene = new SMScene(director);
//        if (scene!=null) {
//            scene.init();
//        }
//        return scene;
//    }
//
//    public static SMScene createWithSize(IDirector director, Size size) {
//        SMScene scene = new SMScene(director);
//        if (scene!=null) {
//            scene.initWithSize(size);
//        }
//
//        return scene;
//    }

//    public SMScene(IDirector director) {
//        super(director);
//        setAnchorPoint(new Vec2(0.5f, 0.5f));
//
//    }

//    public static SMScene create(IDirector director) {
//        SMScene scene = new SMScene(director);
//        if (scene!=null) {
//            scene.init();
//        }
//
//        return scene;
//    }

    public static SMScene createWithSize(IDirector director, Size size) {
        SMScene scene = new SMScene(director);
        if (scene!=null) {
            scene.initWithSize(size);
        }

        return scene;
    }

    @Override
    protected boolean init() {
        Size size = new Size(getDirector().getWidth(), getDirector().getHeight());
        return initWithSize(size);
    }

    protected boolean initWithSize(Size size) {
        setContentSize(size);
        return true;
    }

    public static SMScene create(IDirector director) {
        SMScene scene = new SMScene(director);
        if (scene!=null) {
            scene.initWithSceneParams(null, SwipeType.NONE);
        }

        return scene;
    }

    public static SMScene create(IDirector director, SceneParams params, SwipeType type) {
        SMScene scene = new SMScene(director);
        if (scene!=null) {
            scene.initWithSceneParams(params, type);
        }

        return scene;
    }


//    @Override
//    protected boolean init() {
//        Size size = new Size(0, 0);
//        return initWithSize(size);
//    }

    // Intent하고 같은거.. 직접 구현..
    private SceneParams _sceneResult;
    private SceneParams _sceneParam;

    protected SMView _rootView;
    private SwipeType _swipeType;

    protected boolean initWithSceneParams(SceneParams params, SwipeType type) {
        Size size = new Size(getDirector().getWidth(), getDirector().getHeight());

        setContentSize(size);
        _rootView = new SMView(getDirector(), 0, 0, size.width, size.height);
        super.addChild(_rootView);
        _swipeType = type;
        if (params!=null) {
            _sceneParam = params;
        }

        if (init()) {
            setCascadeAlphaEnable(true);
            return true;
        }

        return false;
    }

    public SMScene(IDirector director) {
        super(director);
//        setAnchorPoint(new Vec2(0.0f, 0.0f));
//        setPosition(0, 0);
        setAnchorPoint(Vec2.MIDDLE);
        setPosition(new Vec2(director.getWinSize().width/2, director.getWinSize().height/2));
        setContentSize(director.getWidth(), director.getHeight());
        _sceneParam = null;
    }

    public SMScene(IDirector director, SceneParams params) {
        super(director);
//        setAnchorPoint(new Vec2(0.0f, 0.0f));
//        setPosition(0, 0);
        setAnchorPoint(Vec2.MIDDLE);
        setPosition(new Vec2(director.getWinSize().width/2, director.getWinSize().height/2));
        setContentSize(director.getWidth(), director.getHeight());
//        setBounds(0, 0, director.getWidth(), director.getHeight());
        _sceneParam = params;
    }

    public SMView getRootView() {return _rootView;}
    public SwipeType getSwipeType() {return _swipeType;}


    @Override
    public void addChild(SMView child) {
        _rootView.addChild(child);
    }

    @Override
    public void addChild(SMView child, int localZOrder) {
        _rootView.addChild(child, localZOrder);
    }

    @Override
    public void addChild(SMView child, int localZOrder, int tag) {
        _rootView.addChild(child, localZOrder, tag);
    }

    @Override
    public SMView getChildByTag(int tag) {
        return _rootView.getChild(tag);
    }

    @Override
    public void removeChild(SMView child, boolean cleanup) {
        _rootView.removeChild(child, cleanup);
    }

    @Override
    public void removeChildByTag(int tag, boolean cleanup) {
        _rootView.removeChildByTag(tag, cleanup);
    }

    // 쓸일이 별로 없을거임.
    protected void setRootView(SMView newRootView) {
        if (newRootView==null || newRootView==_rootView) {
            return;
        }

        super.removeChild(_rootView);
        super.addChild(newRootView, 0, 0);
        _rootView = newRootView;
    }


    public void setSceneResult(SceneParams result) {
        _sceneResult = result;
    }

    public void onSceneResult(SceneParams result) {}

    public void onSceneResult(SMScene fromScene, SceneParams result) {}

    public void onTransitionProgress(final Transition t, final int tag, final float progress) {}

    public void onTransitionStart(final Transition t, final int tag) {}

    public void onTransitionComplete(final Transition t, final int tag) {}

    public boolean canSwipe(final Vec2 point, final SwipeType type) {return true;}

    public void onExitBackground() {}
    public void onEnterForground() {}

    public void SMSceneOnEnter() {
        setState(STATE_ACTIVATE);
        SMViewOnEnter();
    }

    @Override
    public void onEnter() {
        setState(STATE_ACTIVATE);
        super.onEnter();
    }

    public void SMSceneOnExit() {
        if (mState == STATE_FINISHING) {
//            getDirector().popScene(this);
        } else {
            setState(STATE_PAUSING);
//            onPause();
            SMViewOnExit();
        }
    }

    @Override
    public void onExit() {
        if (mState == STATE_FINISHING) {
//            getDirector().popScene(this);
        } else {
            setState(STATE_PAUSING);
            onPause();
            super.onExit();
        }
    }

    public void startScene(SMScene scene) {
        TransitionScene transition = null;

        switch (scene.getSwipeType()) {
            case MENU:
            {

            }
            break;
            case NONE:
            case BACK:
            {
                transition = SlideInToLeft.create(getDirector(), AppConst.SceneTransitionTime.NORMAL, scene);
            }
            break;
            case DISMISS:
            {
                transition = SlideInToTop.create(getDirector(), AppConst.SceneTransitionTime.NORMAL, scene);
            }
            break;
        }

        if (transition!=null) {
            getDirector().pushScene(transition);
        }
    }

    public void finishScene() {
        finishScene(null);
    }

    public void finishScene(SceneParams params) {
        SMScene scene = (SMScene) getDirector().getPreviousScene();
        if (scene==null) {
            return;
        }

        setSceneResult(params);

        TransitionScene transition = null;

        switch (_swipeType) {
            case MENU:
            {
                assert (false);
                // 메뉴 타입은 마지막이라 종료할 수가 없음.
            }
            break;
            case NONE:
            case BACK:
            {
                transition = SlideOutToRight.create(getDirector(), AppConst.SceneTransitionTime.NORMAL, scene);
            }
            break;
            case DISMISS:
            {
                transition = SlideOutToBottom.create(getDirector(), AppConst.SceneTransitionTime.NORMAL, scene);
            }
            break;
        }

        transition.setTag(getTag());
        getDirector().popSceneWithTransition(transition);
    }

//    @Override
//    public void onInitView() {
//        setPosition(0, 0);
//        setContentSize(getDirector().getWidth(), getDirector().getHeight());
//
////        setBounds(0, 0, getDirector().getWidth(), getDirector().getHeight());
//    }

    /**
     * Scene의 활성 상태
     * @return true 일때만 이벤트를 받아들일수 있다.
     */
    public boolean isActivate() {
        return (isInitialized() && mState == STATE_ACTIVATE);
    }

    public void setState(int state) {
        mState = state;
    }

    public int getState() {
        return mState;
    }

    @Override
    public int dispatchTouchEvent(MotionEvent ev) {
        if (isActivate()) {
            return super.dispatchTouchEvent(ev);
        }
        return SMView.TOUCH_FALSE;
    }

    /**
     * Back press 이벤트 처리
     * @return true : scene 안에서 이벤트 처리 했음.
     *         false : Activity에서 이벤트 처리해야한다.
     */
    @Override
    public boolean onBackPressed() {
        if (isInitialized() && mState == STATE_ACTIVATE) {
            return sceneFinish();
        }
        return false;
    }

    public boolean sceneFinish() {
        return _director.sceneFinish(this, getSceneResult());
    }


    @Override
    protected void updateChildren() {
        if (mState != STATE_PAUSED) {
            super.updateChildren();
        }
    }

    public SceneParams getSceneResult() {
        return _sceneResult;
    }

    public SceneParams getSceneParams() {
        return _sceneParam;
    }

//    @Override
//    public void showComplete() {
//        super.showComplete();
//        setState(STATE_ACTIVATE);
//    }

//    @Override
//    public void hideComplete() {
//        super.hideComplete();
//
//        if (mState == STATE_FINISHING) {
//            getDirector().popScene(this);
//        } else {
//            setState(STATE_PAUSED);
//            onPause();
//        }
//    }

    @Override
    public void show() {
        super.show();
        // scene 보임, 메뉴 처리등.
    }
    @Override
    public void show(long durationMillis, long delayMillis) {
        super.show(durationMillis, delayMillis);
        // scene 보임
    }

    @Override
    public void hide() {
        super.hide();
        // scene 안 보임
    }

    @Override
    public void hide(long durationMillis, long delayMillis) {
        super.hide(durationMillis, delayMillis);
        // scene 안 보임
    }

//    @Override
//    public void onActionBarClick(SMView view) {
//    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    }
}
