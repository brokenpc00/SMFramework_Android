package com.interpark.smframework;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;

import com.android.volley.RequestQueue;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.shape.PrimitiveAARect;
import com.interpark.smframework.base.shape.PrimitiveCircle;
import com.interpark.smframework.base.shape.PrimitiveLine;
import com.interpark.smframework.base.shape.PrimitiveRect;
import com.interpark.smframework.base.sprite.CanvasSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.sprite.SpriteSet;
import com.interpark.smframework.base.texture.CanvasTexture;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.texture.TextureManager;
import com.interpark.smframework.base.transition.SwipeBack;
import com.interpark.smframework.base.transition.SwipeDismiss;
import com.interpark.smframework.base.transition.TransitionScene;
import com.interpark.smframework.base.types.ActionManager;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.BackPressable;
import com.interpark.smframework.util.OpenGlUtils;
import com.interpark.smframework.base.types.Scheduler;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.EdgeSwipeForDismiss;
import com.interpark.smframework.view.EdgeSwipeForDismiss.SWIPTE_DISMISS_UPDATE_CALLBCK;
import com.interpark.smframework.view.EdgeSwipeLayerForPushBack;
import com.interpark.smframework.view.EdgeSwipeLayerForPushBack.SWIPTE_BACK_UPDATE_CALLBCK;
import com.interpark.smframework.view.EdgeSwipeLayerForSideMenu;

import org.apache.http.cookie.SM;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.SocketHandler;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SMDirector implements IDirector, GLSurfaceView.Renderer {

    private static SMDirector _instance = null;

    public static SMDirector getDirector() {return _instance;}

    // 가로 기준 720px
    public static final int BASE_SCREEN_WIDTH = 720;

    private FragmentActivity mActivity;
    private boolean mInitialized = false;
    private int mWidth;
    private int mHeight;
    private int mDeviceWidth;
    private int mDeviceHeight;
    private int mDisplayRawWidth;
    private int mDisplayRawHeight;
    private float mDisplayAdjust = 1;
    private Thread mThreadOwner;
//    private PreviewSurfaceView mPreviewSurface;


    public static SharedLayer intToEnumForSharedLayer(int num) {
        switch (num) {
            case 0: return SharedLayer.BACKGROUND;
            case 1: return SharedLayer.LEFT_MENU;
            case 2: return SharedLayer.RIGHT_MENU;
            case 3: return SharedLayer.BETWEEN_MENU_AND_SCENE;
            case 4: return SharedLayer.BETWEEN_SCENE_AND_UI;
            case 5: return SharedLayer.UI;
            case 6: return SharedLayer.BETWEEN_UI_AND_POPUP;
            case 7: return SharedLayer.DIM;
            case 8: return SharedLayer.POPUP;
            default: return SharedLayer.BACKGROUND;
        }
    }

    public static int enumToIntForSharedLayer(SharedLayer layer) {
        switch (layer) {
            case BACKGROUND: return 0;
            case LEFT_MENU: return 1;
            case RIGHT_MENU: return 2;
            case BETWEEN_MENU_AND_SCENE: return 3;
            case BETWEEN_SCENE_AND_UI: return 4;
            case UI: return 5;
            case BETWEEN_UI_AND_POPUP: return 6;
            case DIM: return 7;
            case POPUP: return 8;
            default: return 0;
        }
    }

    @Override
    public void setSharedLayer(final SharedLayer layerId, SMView layer) {
        if (_sharedLayer[enumToIntForSharedLayer(layerId)]!=null) {
            _sharedLayer[enumToIntForSharedLayer(layerId)].onExitTransitionDidStart();
            _sharedLayer[enumToIntForSharedLayer(layerId)].onExit();
            _sharedLayer[enumToIntForSharedLayer(layerId)].cleanup();
        }

        _sharedLayer[enumToIntForSharedLayer(layerId)] = null;

        _sharedLayer[enumToIntForSharedLayer(layerId)] = layer;
        if (layer==null) {
            return;
        }

        _sharedLayer[enumToIntForSharedLayer(layerId)].onEnter();
        _sharedLayer[enumToIntForSharedLayer(layerId)].onEnterTransitionDidFinish();
    }

    @Override
    public SMView getSharedLayer(final SharedLayer layerId) {
        return _sharedLayer[enumToIntForSharedLayer(layerId)];
    }

    private SMView[] _sharedLayer = null;

    private float[] mColorBuffer = new float[4];
    private int mFrameBufferId = 0;
    private CanvasSprite mFrameBuffer;


    private SpriteSet mSpriteSet;
    private PrimitiveRect mPrimFillBox;
    private PrimitiveRect mPrimHollowBox;
    private PrimitiveLine mPrimLine;
    private PrimitiveCircle mPrimCircle;
    private PrimitiveAARect mPrimAARect;
    private ShaderManager mShaderManager;
    public ShaderManager getShaderManager() {return mShaderManager;}
    private TextureManager mTextureManager;

    private float[] mActiveMatrix = null;
    private float[] mFrameBufferMatrix;
    private final Stack<float[]> mMatrixStack = new Stack<float[]>();
    private final Queue<MotionEvent> _motionEventQueue = new LinkedList<MotionEvent>();
    private final Queue<Runnable> mRunOnDraw = new LinkedList<Runnable>();
    private final ArrayList<SMDirector.DelayedRunnable> mRunOnDrawDelayed = new ArrayList<SMDirector.DelayedRunnable>();

    protected SMScene _runningScene;
    protected SMScene _nextScene;
    protected boolean _sendCleanupToScene = false;
    private final Stack<SMScene> _scenesStack = new Stack<SMScene>();

    private boolean mTouckLock = false;
    private boolean mTimerUpdate = true;
    private long mCurrentTime = 0;

    private static final int MAX_MARIX_BUFFER = 32;
    private float[][] mMatrixBuffer;
    private int mMaxtrixBufferPointer = 0;

    protected Scheduler _scheduler = null;
    protected ActionManager _actionManager = null;

    public ActionManager getActionManager() {return _actionManager;}

    public Scheduler getScheduler() {return _scheduler;}

    // 나중에 action bar

    // 나중에 spiner progress view

    // 나중에 upload progress view

    private RequestQueue mRequestQueue;
    private boolean mRootSceneInitialized = false;

    private SMDirector.OrientationListener mOrientationListener;

    private class DelayedRunnable {
        Runnable action;
        long startTickCount;

        DelayedRunnable(Runnable action, long startTickCount) {
            this.action = action;
            this.startTickCount = startTickCount;
        }
    }

    public SMDirector(FragmentActivity activity) {
        mActivity = activity;

        _scheduler = new Scheduler(this);
        _actionManager = new ActionManager(this);
        _scheduler.scheduleUpdate(_actionManager, Scheduler.PRIORITY_SYSTEM, false);
        _lastUpdate = System.currentTimeMillis();


        mShaderManager = new ShaderManager();
        mTextureManager= new TextureManager(this);

        mWidth = BASE_SCREEN_WIDTH;
        mTimerUpdate = true;

        _lastTouchDownTime = getGlobalTime();
        _invalid = false;

        mMatrixBuffer = new float[MAX_MARIX_BUFFER][];
        for (int i = 0; i < MAX_MARIX_BUFFER; i++) {
            mMatrixBuffer[i] = new float[16];
        }

        // Texture Packer에서 얻어옴..
//        mSpriteSet = new SpriteSetCommon(this);

        // 나중에 네트웍 구현
//        mRequestQueue = Volley.newRequestQueue(mActivity, new OkHttpStack());

        // for texture packer... 좌표체계.. 바꿈.
//        mSpriteSet.get(SR.camera_guide_head).changeStertch3Seg(490, 0.266f, 1-0.266f);

        mOrientationListener = new SMDirector.OrientationListener(activity);

        _sharedLayer = new SMView[enumToIntForSharedLayer(SharedLayer.POPUP)+1];
        for (int i=0; i<enumToIntForSharedLayer(SharedLayer.POPUP)+1; i++) {
            _sharedLayer[i] = null;
        }

        _instance = this;

    }

    private float[] getObtainMatrix() {
        float[] matrix = mMatrixBuffer[mMaxtrixBufferPointer++];
        mMaxtrixBufferPointer %= MAX_MARIX_BUFFER;
        return matrix;
    }

    private void releaseObtainMatrix() {
        mMaxtrixBufferPointer = (mMaxtrixBufferPointer + MAX_MARIX_BUFFER - 1)%MAX_MARIX_BUFFER;
    }

    public boolean onBackPressd() {
        // 터치가 가능하면
        if (!mTouckLock) {

            // 아니면 하위 scene부터 view에게 전달함.
            final SMScene scene;
            synchronized (_scenesStack) {
                try {
                    // 맨 위에 scene에게 준다.
                    scene = getTopScene();
                } catch (ArrayIndexOutOfBoundsException e) {
                    return false;
                }
            }
//            if (!scene.isVisibleAnimation()) {
                boolean ret = scene.onBackPressed();
                if (ret == false && _scenesStack.size() == 1) {
                    // last scene
                    scene.onPause();
                    scene.onDestoryView();
                }
                return ret;
//            }
        }

        return true;
    }


    public void onResume() {
        mOrientationListener.enable();
        startSceneAnimation();
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                final SMScene scene;
                synchronized (_scenesStack) {
                    try {
                        scene = getTopScene();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return;
                    }
                }
                _deltaTime = 0;
                setTouchEventDispatcherEnable(true);
                if (scene.isInitialized()) {
                    scene.onResume();
                }

                for (int i=0; i<enumToIntForSharedLayer(SharedLayer.POPUP)+1; i++) {
                    if (_sharedLayer[i]!=null) {
                        if (_sharedLayer[i].isInitialized()) {
                            _sharedLayer[i].onResume();
                        }
                    }
                }

                mTextureManager.onResume();
            }
        });
    }

    public void onPause() {
        mOrientationListener.disable();
        stopSceneAnimation();
        final SMScene scene;
        setTouchEventDispatcherEnable(false);
        synchronized (_scenesStack) {
            try {
                scene = getTopScene();
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }
        }
        scene.onPause();

        for (int i=0; i<enumToIntForSharedLayer(SharedLayer.POPUP)+1; i++) {
            if (_sharedLayer[i]!=null) {
                _sharedLayer[i].onPause();
            }
        }
        releaseResources();
    }

    public void releaseResources() {
        mShaderManager.release(this);
        mTextureManager.onPause();
        mMatrixStack.clear();
    }

    @Override
    public boolean isGLThread() {
        return (Thread.currentThread() == mThreadOwner);
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        setColor(1,1,1,1);

        mThreadOwner = Thread.currentThread();
        setTouchEventDispatcherEnable(true);
        _instance = this;

    }

    private SMView _dimLayer = null;

    private EdgeSwipeLayerForSideMenu _menuSwipe = null;
    private EdgeSwipeLayerForPushBack _backSwipe = null;
    private EdgeSwipeForDismiss _dismissSwipe = null;
    public static SideMenu _sideMenu = null;
    private SMView _swipeLayer = null;

    private void beginProjectionMatrix() {
            mWidth = BASE_SCREEN_WIDTH;
        mHeight = mDeviceHeight * BASE_SCREEN_WIDTH / mDeviceWidth;
        mDisplayAdjust = (float)BASE_SCREEN_WIDTH / mDeviceWidth;

        GLES20.glViewport(0, 0, mDeviceWidth, mDeviceHeight);

            float[] m1 = new float[16];
            float[] m2 = new float[16];

            float w = getWidth();
            float h = getHeight();

            final float zNear = 0.01f*1000;
            final float zFar = 10000.0f*1000;
//			final float zNear = 0.1f;
//			final float zFar = 100000.0f;
            final float fov = 40.0f;
            final float ratio = w / h;
            final float dist = (float)(h / 2 / Math.tan(Math.toRadians(fov) / 2));

            mFrameBufferMatrix = getObtainMatrix();

            OpenGlUtils.getPerspectiveMatrix(m1, fov, ratio, zNear, zFar);
            OpenGlUtils.getLookAtMatrix(m2, 0, 0, dist, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(mFrameBufferMatrix, 0, m1, 0, m2, 0);
            Matrix.translateM(mFrameBufferMatrix, 0, -getWidth()/2f, getHeight()/2f, 0);
            Matrix.scaleM(mFrameBufferMatrix, 0, 1, -1, 1);

            mMaxtrixBufferPointer = 0;
            mMatrixStack.removeAllElements();
            setProjectionMatrix(mFrameBufferMatrix);
            pushProjectionMatrix();

    }

    private void endProjectionMatrix() {

    };

    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
        _instance = this;
        if (width > 0 && height > 0) {
            mDeviceWidth = width;
            mDeviceHeight = height;

            beginProjectionMatrix();

            startTimer();
            setTouchEventDispatcherEnable(true);

            // make menu swipe
            if (_sideMenu==null) {
                _sideMenu = SideMenu.GetSideMenu();
                _sideMenu.setSideMenuListener(null);
            }


            // menu swipe...
            if (_dimLayer==null) {
                // 좌측 메뉴 열릴 때 메인 scene을 덮어줄 dim layer
                _dimLayer = SMView.create(this, 0, 0, getWidth(), getHeight());
                _dimLayer.setBackgroundColor(0, 0, 0, 0);
                _dimLayer.setAlpha(0);
                _dimLayer.setVisible(false);
            }

            // set shared layer
            for (int i=0; i<enumToIntForSharedLayer(SharedLayer.POPUP)+1; i++) {
                if (_sharedLayer[i]!=null) {
                    continue;
                }

                SharedLayer layerId = intToEnumForSharedLayer(i);

                // 각 layer를 쓸거면 수정. 안쓰는 layer는 넘어감
                if (layerId==SharedLayer.RIGHT_MENU) {
                    continue;
                }

                if (layerId==SharedLayer.LEFT_MENU) {
                    setSharedLayer(layerId, _sideMenu);
                } else if (layerId==SharedLayer.DIM) {
                    setSharedLayer(layerId, _dimLayer);
                } else {
                    setSharedLayer(layerId, new SMView(this, 0, 0, getWidth(), getHeight()));
                }
            }

            // make dismiss swipe, back swipe
            // make edge swipe - dismiss and back
            // dismiss - swipe

            // back - swipe
            if (_backSwipe == null) {

                _backSwipe = EdgeSwipeLayerForPushBack.create(this, 0, 0, 0, getWidth(), getHeight());
                _backSwipe.setSwipeWidth(getWidth());
                _backSwipe.setEdgeWidth(AppConst.SIZE.EDGE_SWIPE_MENU);
                _backSwipe._swipeUpdateCallback = new SWIPTE_BACK_UPDATE_CALLBCK() {
                    @Override
                    public void Func(int a, float b) {
                        onEdgeBackUpdateCallback(a, b);
                    }
                };
                _backSwipe.onEnter();
                _backSwipe.onEnterTransitionDidFinish();
                _backSwipe.reset();

            }

            if (_dismissSwipe==null) {
                _dismissSwipe = EdgeSwipeForDismiss.create(this, 0, 0, 0, getWidth(), getHeight());
                _dismissSwipe.setSwipeWidth(getHeight());
                _dismissSwipe.setEdgeWidth(AppConst.SIZE.EDGE_SWIPE_TOP);
                _dismissSwipe._swipeUpdateCallback = new SWIPTE_DISMISS_UPDATE_CALLBCK() {
                    @Override
                    public void Func(int a, float b) {
                        onEdgeDismissUpdateCallback(a, b);
                    }
                };
                _dismissSwipe.onEnter();
                _dismissSwipe.onEnterTransitionDidFinish();
                _dismissSwipe.reset();
            }


            if (_menuSwipe==null) {
                _menuSwipe = EdgeSwipeLayerForSideMenu.create(getDirector(), 0, 0, 0, getWidth(), getHeight());
                _menuSwipe.setSwipeWidth(_sideMenu.getContentSize().width);
                _menuSwipe.setEdgeWidth(AppConst.SIZE.EDGE_SWIPE_MENU);
                _menuSwipe.setOnClickListener(new SMView.OnClickListener() {
                    @Override
                    public void onClick(SMView view) {
                        if (_sideMenu!=null && _sideMenu.getState()==SIDE_MENU_STATE.OPEN) {
                            float p1 = _sideMenu.getOpenPosition() + getWidth();
                            float p2 = _swipeLayer.getLastTouchLocation().x;
                            if (p2<p1) {
                                SideMenu.CloseMenu();
                            }
                        }
                    }
                });
            }

            _sideMenu.setSwipeLayer(_menuSwipe);
            _sideMenu._sideMenuUpdateCallback = new SideMenu.SIDE_MENU_UPDATE_CALLBACK() {
                @Override
                public void Func(SIDE_MENU_STATE state, float position) {
                    onSideMenuUpdateCallback(state, position);
                }
            };

            if (_swipeLayer==null) {
                _swipeLayer = SMView.create(this);
                _swipeLayer.addChild(_menuSwipe);
                _swipeLayer.onEnter();
                _swipeLayer.onEnterTransitionDidFinish();
            }

            if (mFrameBuffer == null) {
                mFrameBuffer = CanvasSprite.createCanvasSprite(this, mWidth, mHeight, "FRAME_BUFFER");
            }
        }
    }

    private SMView _touchMotionTarget = null;
    private float _lastTouchDownTime;

    public void onSideMenuUpdateCallback(SIDE_MENU_STATE state, float position) {
        float f = position / _sideMenu.getContentSize().width;
        if (f <= 0) {
            f = 0;
        } else if (f > 1) {
            f = 1;
        }

        if (f > 0) {
            if (!_dimLayer.isVisible()) {
                _dimLayer.setVisible(true);
            }
            _dimLayer.setContentSize(new Size(getWinSize().width-position, getWinSize().height));
            _dimLayer.setPositionX(position);
            _dimLayer.setAlpha(0.5f*f);
//            _dimLayer.setBackgroundColor(0, 0, 0, 0.5f*f);

        } else {
            if (_dimLayer.isVisible()) {
                _dimLayer.setVisible(false);
            }
        }

        SMScene runningScene = null;
        SMScene inScene = null;
        if (getRunningScene() instanceof  SMScene) {
            runningScene = getRunningScene();
            if (getRunningScene() instanceof TransitionScene) {
                TransitionScene transitionScene = (TransitionScene)getRunningScene();
                inScene = transitionScene.getInScene();
                inScene.setPositionX(getDirector().getWinSize().width/2);
                runningScene.setPositionX(getDirector().getWinSize().width/2);
                transitionScene.setPositionX(position+getDirector().getWinSize().width/2);
                return;
            }
        }

        if (runningScene!=null) {
            runningScene.setPositionX(position+getDirector().getWinSize().width/2);
        }

        if (inScene!=null) {
            runningScene.setPositionX(position+getDirector().getWinSize().width/2);
        }
    }

    public void onEdgeDismissUpdateCallback(int state, float position) {
        SwipeDismiss dismissScene = null;

        if (!(getRunningScene() instanceof SwipeDismiss)) {
            if (position > 0) {
                dismissScene = SwipeDismiss.create(this, getPreviousScene());
                popSceneWithTransition(dismissScene);
            }
        } else {
            dismissScene = (SwipeDismiss)getRunningScene();
            if (dismissScene!=null && !_dismissSwipe.isScrollTargeted()) {
                if (position<=0) {
                    dismissScene.cancel();
                    _dismissSwipe.reset();
                    // dismiss swipe scene(out scene)이 close 되었으면
                } else if (position>=_dismissSwipe.getContentSize().height) {
                    // dismiss swipe scene(out scene)이 끝까지 갔으면
                    dismissScene.finish();
                    _dismissSwipe.reset();
                } else {
                    // touch를 막아야
                    setTouchEventDispatcherEnable(false);
                }
            }
        }

        if (dismissScene!=null) {
            dismissScene.getOutScene().setPositionY(position+getDirector().getWinSize().height/2);
            float progress = (dismissScene.getOutScene().getPositionY() - getDirector().getWinSize().height/2) / getDirector().getWinSize().height;

            float minusScale = 0.6f * progress;
            float newScale = 1.6f - minusScale;
            dismissScene.getInScene().setScale(newScale);

        }
    }

    public void onEdgeBackUpdateCallback(int state, float position) {
        // EdgeSwipeBack에서 schedule update에 의해 호출되는 callback
        // 여기에서 director의 next scene이 transition scene임을 setting하고
        // Open또는 Close 되었을 때 callback을 호출하는 update를 unscheduled한다.
        SwipeBack backScene = null;

        if (!(getRunningScene() instanceof SwipeBack)) {
            if (position > 0) {
                backScene = SwipeBack.create(this, getPreviousScene());
                popSceneWithTransition(backScene);
            }
        } else {
            backScene = (SwipeBack)getRunningScene();
            if (backScene!=null && !_backSwipe.isScrollTargeted()) {
                if (position<=0) {
                    backScene.cancel();
                    _backSwipe.reset();
                    // back swipe scene(out scene)이 close 되었으면
                } else if (position>=_backSwipe.getContentSize().width) {
                    // back swipe scene(out scene)이 끝까지 갔으면
                    backScene.finish();
                    _backSwipe.reset();
                } else {
                    // touch를 막아야
                    setTouchEventDispatcherEnable(false);
                }
            }
        }

        if (backScene!=null) {
            backScene.getOutScene().setPositionX(position+getDirector().getWinSize().width/2);
            SMScene inScene = backScene.getInScene();
            if (inScene!=null) {
                inScene.setPositionX(0.3f*(-_backSwipe.getContentSize().width + position) + getDirector().getWinSize().width/2);
                float progress = backScene.getLastProgress();
                float minusScale = 0.6f * progress;
                inScene.setScale(1.6f-minusScale);
            }
        }
    }

    private void handleTouchEvent() {

        // motion event queue에서 하나씩 꺼내서 사용하자
        synchronized(_motionEventQueue) {

            // 있는거 다 내놔~
            while (!_motionEventQueue.isEmpty()) {
                final MotionEvent event = _motionEventQueue.poll();

                float nowTime = getGlobalTime();
                if (event==null) {
                    break;
                }

                int action = event.getAction();
                // 터치 다운과 업의 시간이 같을 경우 오류 방지
                if (action==MotionEvent.ACTION_DOWN) {
                    _lastTouchDownTime = nowTime;
                } else if (action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL) {
                    if (_lastTouchDownTime==nowTime) {
                        nowTime = _lastTouchDownTime + (1.0f / 60.0f);
                    }
                }

                Vec2 worldPoint = new Vec2(event.getX(0), event.getY(0));
                SMView touchLayer = null;
                SMView newTouchTarget = null;

                event.setLocation(event.getX()*mDisplayAdjust, event.getY()*mDisplayAdjust);
                int ret = SMView.TOUCH_FALSE;

                do {

                    // POPUP과 UI는 터치를 받는 레이어니까 POPUP부터 순서대로
                    touchLayer = getSharedLayer(SharedLayer.POPUP);
                    if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
                        ret = touchLayer.dispatchTouchEvent(event, touchLayer, false);
                    }
                    if (ret!=SMView.TOUCH_FALSE) {
                        newTouchTarget = touchLayer;
                        break;
                    }

                    touchLayer = getSharedLayer(SharedLayer.UI);
                    if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
                        ret = touchLayer.dispatchTouchEvent(event, touchLayer, false);
                    }
                    if (ret!=SMView.TOUCH_FALSE) {
                        newTouchTarget = touchLayer;
                        break;
                    }


                    // 그 다음 실행중인 Scene과 Transition Scene... 그리고 Swipe(BACK, MENU, DISMISS)에게 터치 전달
                    SMScene runningScene = null;
                    SMScene.SwipeType type = SMScene.SwipeType.NONE;
                    SwipeBack backScene = null;
                    SwipeDismiss dismissScene = null;

                    if (getRunningScene() instanceof TransitionScene) {

                        if (getRunningScene() instanceof SwipeDismiss) {
                            dismissScene = (SwipeDismiss)getRunningScene();
                        }

                        if (dismissScene!=null) {
                            type = SMScene.SwipeType.DISMISS;
                            runningScene = dismissScene.getOutScene();
                        } else {
                            if (getRunningScene() instanceof SwipeBack) {
                                backScene = (SwipeBack)getRunningScene();
                            }
                            if (backScene!=null) {
                                type = SMScene.SwipeType.BACK;
                                runningScene = backScene.getOutScene();
                            }
                        }

                    } else {
                        runningScene = getRunningScene();
                        type = runningScene.getSwipeType();
                    }


                    // 먼저 Swipe Layer에 터치 전달 Menu, Back, Dismiss 등
                    if (runningScene!=null) {
                        switch (type) {
                            case MENU:
                            {
                                if (_swipeLayer!=null && !(runningScene instanceof TransitionScene)) {
                                    if (action == MotionEvent.ACTION_DOWN && _menuSwipe.isScrollArea(worldPoint) && !runningScene.canSwipe(worldPoint, type)) {
                                        break;
                                    }
                                    int inRet = _swipeLayer.dispatchTouchEvent(event, _swipeLayer, false);
                                    if (inRet == SMView.TOUCH_INTERCEPT) {
                                        if (_touchMotionTarget!=null && _touchMotionTarget!=_sideMenu) {
                                            _touchMotionTarget.cancelTouchEvent(_touchMotionTarget, event);
                                            _touchMotionTarget = null;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                            case BACK:
                            {
                                if (_backSwipe!=null) {

                                    if (action==MotionEvent.ACTION_DOWN && _backSwipe.isScrollArea(worldPoint) && !runningScene.canSwipe(worldPoint, type)) {
                                        break;
                                    }

                                    int inRet = _backSwipe.dispatchTouchEvent(event, _backSwipe, false);
                                    if (inRet==SMView.TOUCH_INTERCEPT) {
                                        if (_touchMotionTarget!=null && _touchMotionTarget!=_backSwipe) {
                                            _touchMotionTarget.cancelTouchEvent(_touchMotionTarget, event);
                                            _touchMotionTarget = null;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                            case DISMISS:
                            {
                                if (_dismissSwipe!=null) {
                                    if (action == MotionEvent.ACTION_DOWN && !runningScene.canSwipe(worldPoint, type)) {
                                        break;
                                    }

                                    int inRet = _dismissSwipe.dispatchTouchEvent(event, _dismissSwipe, false);
                                    if (inRet==SMView.TOUCH_INTERCEPT) {
                                        if (_touchMotionTarget!=null && _touchMotionTarget!=_dismissSwipe) {
                                            _touchMotionTarget.cancelTouchEvent(_touchMotionTarget, event);
                                            _touchMotionTarget = null;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                            default:
                            {

                            }
                            break;
                        }
                    }

                    // 그 다음 실행 중인 Scene에 터치 전달
                    if (runningScene!=null) {
                        touchLayer = runningScene.getRootView();
                    }

                    // 메뉴가 닫혀 있을때는 그냥 Scene이 터치를 먹는다.
                    if (_sideMenu==null || _sideMenu.getState()==SIDE_MENU_STATE.CLOSE) {
                        // side menu가 닫혀 있을 때 대부분 여기...
                        if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
                            ret = touchLayer.dispatchTouchEvent(event, touchLayer, true);
                            newTouchTarget = touchLayer;

                            if (ret==SMView.TOUCH_FALSE && action==MotionEvent.ACTION_DOWN) {
                                Vec2 point = new Vec2(event.getX(0), event.getY(0));
                                if (touchLayer.containsPoint(point)) {
                                    ret = SMView.TOUCH_TRUE;
                                }
                            }
                        }
                        if (ret!=SMView.TOUCH_FALSE) {
                            newTouchTarget = touchLayer;
                            if (_swipeLayer!=null && ret==SMView.TOUCH_INTERCEPT && _menuSwipe.isScrollTargeted()) {
                                _swipeLayer.cancelTouchEvent(_swipeLayer, event);
                            }
                            break;
                        }
                    }

                    // 메뉴가 열려 있으면 Scene에 아닌 메뉴 레이어가 터치를 먹는다. (메뉴를 누르거나 아니면 dimLayer를 눌러서 메뉴를 닫히게 하거나...
                    if (_sideMenu==null || _sideMenu.getState()!=SIDE_MENU_STATE.CLOSE) {
                        // side menu가 열려 있을 때
                        touchLayer = getSharedLayer(SharedLayer.LEFT_MENU);
                        if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
                            ret = touchLayer.dispatchTouchEvent(event, touchLayer, false);
                            newTouchTarget = touchLayer;
                        }
                        if (ret!=SMView.TOUCH_FALSE) {
                            newTouchTarget = touchLayer;
                            break;
                        }
                    }

                } while (false);

                if (action==MotionEvent.ACTION_DOWN && newTouchTarget!=null) {
                    _touchMotionTarget = newTouchTarget;
                } else if (action==MotionEvent.ACTION_UP) {
                    _touchMotionTarget = null;
                }

                event.recycle();
            }
        }
    }


//    private void pollTouchEvent(SMScene scene) {
//        // onDrawFrame에서 강제로 호출 되는 곳... touch 판별을 여기서 한다
//        // touch event를 각 layer 별로 구분하여 전달 해 줌
//
////        if (scene != null && scene.isActivate()) {
//            // touch queue에 있는 event를 꺼내서 각 view에 전달한다.
//
//
//        // scene이건 view이건 무조건 받은 touch 분석
//
//            synchronized(_motionEventQueue) {
//                while (!_motionEventQueue.isEmpty()) {
//                    final MotionEvent event = _motionEventQueue.poll();
//
//                    float nowTime = getGlobalTime();
//
//                    int action = event.getAction();
//                    if (action==MotionEvent.ACTION_DOWN) {
//                        _lastTouchDownTime = nowTime;
//                    } else if (action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL) {
//                        if (_lastTouchDownTime==nowTime) {
//                            nowTime = _lastTouchDownTime + (1.0f / 60.0f);
//                        }
//                    }
//
//                    Vec2 worldPoint = new Vec2(event.getX(0), event.getY(0));
//                    SMView touchLayer = null;
//                    SMView newTouchTarget = null;
//
//                    event.setLocation(event.getX()*mDisplayAdjust, event.getY()*mDisplayAdjust);
//                    int ret = SMView.TOUCH_FALSE;
//
//                    do {
////                        if (mPopupView != null) {
////                            ret = mPopupView.dispatchTouchEvent(event, mPopupView, false);
////                        }
//
////                    BACKGROUND,
////                            LEFT_MENU,
////                            RIGHT_MENU,
////                            BETWEEN_MENU_AND_SCENE,
////                            // scene
////                            BETWEEN_SCENE_AND_UI,
////                            UI,
////                            BETWEEN_UI_AND_POPUP,
////                            DIM,
////                            POPUP,
//                        // shared layer 맨 위에서 부터
//                        // top most popup
//                        touchLayer = getSharedLayer(SharedLayer.POPUP);
//                        if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
//                            ret = touchLayer.dispatchTouchEvent(event, touchLayer, false);
//                        }
//                        if (ret!=SMView.TOUCH_FALSE) {
//                            newTouchTarget = touchLayer;
//                            break;
//                        }
//
//                        // ui
//                        touchLayer = getSharedLayer(SharedLayer.UI);
//                        if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
//                            ret = touchLayer.dispatchTouchEvent(event, touchLayer, false);
//                        }
//                        if (ret!=SMView.TOUCH_FALSE) {
//                            newTouchTarget = touchLayer;
//                            break;
//                        }
//
//                        SMScene runningScene = null;
//                        SMScene.SwipeType type = SMScene.SwipeType.NONE;
//                        SwipeBack backScene = null;
//                        SwipeDismiss dismissScene = null;
//
//                        if (getRunningScene() instanceof TransitionScene) {
//
//                            if (getRunningScene() instanceof SwipeDismiss) {
//                                dismissScene = (SwipeDismiss)getRunningScene();
//                            }
//
//                            if (dismissScene!=null) {
//                                type = SMScene.SwipeType.DISMISS;
//                                runningScene = dismissScene.getOutScene();
//                            } else {
//                                if (getRunningScene() instanceof SwipeBack) {
//                                    backScene = (SwipeBack)getRunningScene();
//                                }
//                                if (backScene!=null) {
//                                    type = SMScene.SwipeType.BACK;
//                                    runningScene = backScene.getOutScene();
//                                }
//                            }
//
//                        } else {
//                            runningScene = getRunningScene();
//                            type = runningScene.getSwipeType();
//                        }
//
//
//                        if (runningScene!=null) {
//                            switch (type) {
//                                case MENU:
//                                {
//                                    if (_swipeLayer!=null && !(runningScene instanceof TransitionScene)) {
//                                        if (action == MotionEvent.ACTION_DOWN && _menuSwipe.isScrollArea(worldPoint) && !runningScene.canSwipe(worldPoint, type)) {
//                                            break;
//                                        }
//                                        int inRet = _swipeLayer.dispatchTouchEvent(event, _swipeLayer, false);
//                                        if (inRet == SMView.TOUCH_INTERCEPT) {
//                                            if (_touchMotionTarget!=null && _touchMotionTarget!=_sideMenu) {
//                                                _touchMotionTarget.cancelTouchEvent(_touchMotionTarget, event);
//                                                _touchMotionTarget = null;
//                                            }
//                                        }
//                                        break;
//                                    }
//                                }
//                                break;
//                                case BACK:
//                                {
//                                    while (_backSwipe!=null) {
//
//                                        if (action==MotionEvent.ACTION_DOWN && _backSwipe.isScrollArea(worldPoint) && !runningScene.canSwipe(worldPoint, type)) {
//                                            break;
//                                        }
//
//                                        int inRet = _backSwipe.dispatchTouchEvent(event, _backSwipe, false);
//                                        if (inRet==SMView.TOUCH_INTERCEPT) {
//                                            if (_touchMotionTarget!=null && _touchMotionTarget!=_backSwipe) {
//                                                _touchMotionTarget.cancelTouchEvent(_touchMotionTarget, event);
//                                                _touchMotionTarget = null;
//                                            }
//                                        }
//                                        break;
//                                    }
//                                }
//                                break;
//                                case DISMISS:
//                                {
//                                    while (_dismissSwipe!=null) {
//                                        if (action == MotionEvent.ACTION_DOWN && !runningScene.canSwipe(worldPoint, type)) {
//                                            break;
//                                        }
//
//                                        int inRet = _dismissSwipe.dispatchTouchEvent(event, _dismissSwipe, false);
//                                        if (inRet==SMView.TOUCH_INTERCEPT) {
//                                            if (_touchMotionTarget!=null && _touchMotionTarget!=_dismissSwipe) {
//                                                _touchMotionTarget.cancelTouchEvent(_touchMotionTarget, event);
//                                                _touchMotionTarget = null;
//                                            }
//                                        }
//                                        break;
//                                    }
//                                }
//                                break;
//                                default:
//                                {
//
//                                }
//                                break;
//                            }
//                        }
//
//
//                        if (runningScene!=null) {
//                            touchLayer = runningScene.getRootView();
//                            // runningScene 자체에 해도 될 듯 한데...
////                            if (ret == SMView.TOUCH_FALSE) {
////                                ret = runningScene.dispatchTouchEvent(event, scene, true);
////                            }
//                        }
//
//                        if (_sideMenu==null || _sideMenu.getState()==SIDE_MENU_STATE.CLOSE) {
//                            // side menu가 닫혀 있을 때 대부분 여기...
//                            if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
//                                ret = touchLayer.dispatchTouchEvent(event, touchLayer, true);
//                                newTouchTarget = touchLayer;
//
//                                if (ret==SMView.TOUCH_FALSE && action==MotionEvent.ACTION_DOWN) {
//                                    Vec2 point = new Vec2(event.getX(0), event.getY(0));
//                                    if (touchLayer.containsPoint(point)) {
//                                        ret = SMView.TOUCH_TRUE;
//                                    }
//                                }
//                            }
//                            if (ret!=SMView.TOUCH_FALSE) {
//                                newTouchTarget = touchLayer;
//                                if (_swipeLayer!=null && ret==SMView.TOUCH_INTERCEPT && _menuSwipe.isScrollTargeted()) {
//                                    _swipeLayer.cancelTouchEvent(_swipeLayer, event);
//                                }
//                                break;
//                            }
//                        }
//
//                        if (_sideMenu==null || _sideMenu.getState()!=SIDE_MENU_STATE.CLOSE) {
//                            // side menu가 열려 있을 때
//                            touchLayer = getSharedLayer(SharedLayer.LEFT_MENU);
//                            if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
//                                ret = touchLayer.dispatchTouchEvent(event, touchLayer, false);
//                                newTouchTarget = touchLayer;
//                            }
//                            if (ret!=SMView.TOUCH_FALSE) {
//                                newTouchTarget = touchLayer;
//                                break;
//                            }
//                        }
//                    } while (false);
//
//
//                    if (action==MotionEvent.ACTION_DOWN && newTouchTarget!=null) {
//                        _touchMotionTarget = newTouchTarget;
//                    } else if (action==MotionEvent.ACTION_UP) {
//                        _touchMotionTarget = null;
//                    }
//
//                    event.recycle();
//                }
//            }
////        }
//    }

    public boolean _touchEventDispather = false;

    @Override
    public void setTouchEventDispatcherEnable(boolean enable) {
        _touchEventDispather = enable;
    }

    public boolean getTouchEventDispatcherEnable() {
        return _touchEventDispather;
    }

    public void addTouchEvent(final MotionEvent event) {

//        if (!_touchEventDispather) {
//            return;
//        }

        // Android os에서 전달 받은 Touch event를 touch queue에 넣는다

        final MotionEvent ev = MotionEvent.obtain(event);

        // progress view 부분 처리...s]
        synchronized (_motionEventQueue) {
            _motionEventQueue.add(ev);
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
    }

    private boolean _nextDeltaTimeZero = false;

    protected void setNextDeltaTimeZero(boolean flag) {
        _nextDeltaTimeZero = false;
    }

    public void startTimer() {
        mTimerUpdate = true;
        _lastUpdate = System.currentTimeMillis();
        Thread thread = Thread.currentThread();
        _threadId = thread.getId();
        _invalid = false;
    }

    public void stopTimer() {
        mTimerUpdate = false;
    }

//    private SMScene[] mScene = new SMScene[2];

    private float _deltaTime = 0;

    private float _globalTime = 0;

    public float getGlobalTime() {return _globalTime;}

    protected void calculateDeltaTime() {
        if (_nextDeltaTimeZero) {
            _deltaTime = 0;
            _nextDeltaTimeZero = false;
        } else {
            if (mTimerUpdate) {
                long time = System.currentTimeMillis();
                _deltaTime = (time - _lastUpdate)/1000.0f;
                mCurrentTime += time - _lastUpdate;
                _lastUpdate = time;
            }

            _deltaTime = Math.max(0, _deltaTime);
        }
    }

    @Override
    public void onDrawFrame(final GL10 unused) {

        bindTexture(null);

        calculateDeltaTime();
        _globalTime += _deltaTime;


        // schedule udpate 여기서 할것
        if (_scheduler!=null) {
            // update 전파
            _scheduler.update(_deltaTime);
        }


        synchronized (mRunOnDraw) {
            if (mRunOnDrawDelayed.size() > 0) {
                int count = mRunOnDrawDelayed.size();
                for (int i = count-1; i >= 0; i--) {
                    if (mCurrentTime >= mRunOnDrawDelayed.get(i).startTickCount) {
                        runOnDraw(mRunOnDrawDelayed.get(i).action);
                        mRunOnDrawDelayed.remove(i);
                    }
                }
            }
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.poll().run();
            }
        }

        if (_nextScene!=null) {
            setNextScene();
        }

        final CanvasTexture frameBuffer = (CanvasTexture)mFrameBuffer.getTexture();

        frameBuffer.setFrameBuffer(this, true);
        setFrameBufferId(frameBuffer.getId());

        GLES20.glClearColor(1, 1, 1, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);


//        pollTouchEvent(_runningScene);
        // touch event queue에서 꺼내서 실행
        handleTouchEvent();



        //////////////////////
        //
        // frame update !!!!!!
        //
        //////////////////////


        // shared layer 와 scene을 그리자

        if (!_invalid) {

            for (int i=0; i<enumToIntForSharedLayer(SharedLayer.POPUP)+1; i++) {
                SharedLayer layerId = intToEnumForSharedLayer(i);

                SMView drawLayer = _sharedLayer[i];
                if (drawLayer!=null) {
                    drawLayer.visit(1);
                }

                if (layerId==SharedLayer.BETWEEN_MENU_AND_SCENE) {
                    // running scene을 그리자
                    if (_runningScene!=null) {
                        _runningScene.visit(1);
                    }

                    if (!mRootSceneInitialized) {
                        mRootSceneInitialized = true;
                        _runningScene.onEnter();
                        _runningScene.onEnterTransitionDidFinish();
                    }
                }

            }
        }

        frameBuffer.setFrameBuffer(this, false);
        setColor(1,1,1,1);
        GLES20.glViewport(0, 0, getDeviceWidth(), getDeviceHeight());
        setProjectionMatrix(getFrameBufferMatrix());

        // Todo... modify this line... after test.
        GLES20.glClearColor(1, 1, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mFrameBuffer.drawScaleXY(0, getHeight(), 1, -1);

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }

        mInitialized = true;
    }

    @Override
    public FragmentActivity getActivity() {
        return mActivity;
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int  getHeight() {
        return mHeight;
    }


    @Override
    public int getDeviceWidth() {
        return mDeviceWidth;
    }

    @Override
    public int getDeviceHeight() {
        return mDeviceHeight;
    }

    @Override
    public float getDisplayAdjust() {
        return mDisplayAdjust;
    }

    @Override
    public float[] getColor() {
        return mColorBuffer;
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        mColorBuffer[0] = r;
        mColorBuffer[1] = g;
        mColorBuffer[2] = b;
        mColorBuffer[3] = a;
    }

    @Override
    public boolean bindTexture(Texture texture) {
        return mTextureManager.bindTexture(texture);
    }

    @Override
    public ShaderProgram useProgram(ShaderManager.ProgramType type) {
        ShaderProgram program = mShaderManager.getActiveProgram();
        if (program == null || program.getType() != type) {
            if (program != null) {
                program.unbind();
            }
            program = mShaderManager.useProgram(this, type);
            if (program == null) {
                return null;
            } else {
                program.bind();
            }
        }
        return program;
    }


    @Override
    public float[] getProjectionMatrix() {
        return mActiveMatrix;
    }

    @Override
    public void setProjectionMatrix(float[] matrix) {
        if (mActiveMatrix == null) {
            mActiveMatrix = matrix;
        } else {
            OpenGlUtils.copyMatrix(mActiveMatrix, matrix);
        }
        mShaderManager.setMatrix(matrix);
    }

    @Override
    public void pushProjectionMatrix() {
        float[] m = getObtainMatrix();
        OpenGlUtils.copyMatrix(m, mActiveMatrix);
        mMatrixStack.push(m);
    }

    @Override
    public void popProjectionMatrix() {
        if (mMatrixStack.size() > 0) {
            OpenGlUtils.copyMatrix(mActiveMatrix, mMatrixStack.pop());
            releaseObtainMatrix();
            mShaderManager.setMatrix(mActiveMatrix);
        }
    }

    @Override
    public void updateProjectionMatrix() {
        mShaderManager.setMatrix(mActiveMatrix);
    }

    @Override
    public long getTickCount() {
        return mCurrentTime;
    }

    @Override
    public void drawFillRect(float x, float y, float width, float height) {
        if (mPrimFillBox == null) {
            mPrimFillBox = new PrimitiveRect(this, 1, 1, 0, 0);
        }
        mPrimFillBox.drawScaleXY(x, y, width, height);
    }

    @Override
    public void drawRect(float x, float y, float width, float height, float lineWidth) {
        if (mPrimHollowBox == null) {
            mPrimHollowBox = new PrimitiveRect(this, 1, 1, 0, 0, false);
        }
        GLES20.glLineWidth(lineWidth);
        mPrimHollowBox.drawScaleXY(x, y, width, height);
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2, float lineWidth) {
        if (mPrimLine == null) {
            mPrimLine = new PrimitiveLine(this);
        }
        GLES20.glLineWidth(lineWidth);
        mPrimLine.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawCircle(float x, float y, float radius) {
        drawCircle(x, y, radius, 1.5f);
    }

    @Override
    public void drawCircle(float x, float y, float radius, float border) {
        if (mPrimCircle == null) {
            mPrimCircle = new PrimitiveCircle(this);
        }
        mPrimCircle.drawCircle(x, y, radius, border);
    }

    @Override
    public void drawRing(float x, float y, float radius, float thickness) {
        drawRing(x, y, radius, thickness, 1.5f);
    }

    @Override
    public void drawRing(float x, float y, float radius, float thickness, float border) {
        if (mPrimCircle == null) {
            mPrimCircle = new PrimitiveCircle(this);
        }
        mPrimCircle.drawRing(x, y, radius, thickness, border);
    }

    @Override
    public void drawAARect(float x, float y, float width, float height, float round) {
        drawAARect(x, y, width, height, round, 1f);
    }

    @Override
    public void drawAARect(float x, float y, float width, float height, float round, float border) {
        if (mPrimAARect == null) {
            mPrimAARect = new PrimitiveAARect(this);
        }
        mPrimAARect.drawRect(x, y, width, height, round, border);
    }

    @Override
    public SpriteSet getSpriteSet() {
        return mSpriteSet;
//        return null;
    }

    @Override
    public void runOnDraw(final Runnable action) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(action);
        }
    }

    @Override
    public void runOnDrawDelayed(final Runnable action, long delayTimeMillis) {
        synchronized (mRunOnDraw) {
            mRunOnDrawDelayed.add(0, new SMDirector.DelayedRunnable(action, mCurrentTime+delayTimeMillis));
        }
    }

    @Override
    public void removeOnDraw(Runnable targetAction) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.remove(targetAction);
            if (mRunOnDrawDelayed.size() > 0) {
                int count = mRunOnDrawDelayed.size();
                for (int i = count-1; i >= 0; i--) {
                    if (mRunOnDrawDelayed.get(i).action == targetAction) {
                        mRunOnDrawDelayed.remove(i);
                    }
                }
            }
        }
    }

    @Override
    public void removeOnDraw(Class<?> targetClass) {
        synchronized (mRunOnDraw) {
            Iterator<Runnable> iterator = mRunOnDraw.iterator();
            while (iterator.hasNext()) {
                Runnable runnable = iterator.next();
                if (runnable.getClass() == targetClass) {
                    iterator.remove();
                }
            }
            if (mRunOnDrawDelayed.size() > 0) {
                int count = mRunOnDrawDelayed.size();
                for (int i = count-1; i >= 0; i--) {
                    if (mRunOnDrawDelayed.get(i).action.getClass() == targetClass) {
                        mRunOnDrawDelayed.remove(i);
                    }
                }
            }
        }
    }

    @Override
    public boolean hasOnDraw(Runnable targetAction) {
        synchronized (mRunOnDraw) {
            return mRunOnDraw.contains(targetAction);
        }
    }

    @Override
    public boolean hasOnDraw(Class<?> targetClass) {
        synchronized (mRunOnDraw) {
            Iterator<Runnable> iterator = mRunOnDraw.iterator();
            while (iterator.hasNext()) {
                Runnable runnable = iterator.next();
                if (runnable.getClass() == targetClass) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void runOnUiThread(Runnable action) {
        ((FragmentActivity)mActivity).runOnUiThread(action);
    }

    @Override
    public SMScene getTopScene() {
        synchronized (_scenesStack) {
            final int numScene = _scenesStack.size();
            if (numScene > 0) {
                return _scenesStack.peek();
            }
            return null;
        }
    }

    private SMScene getSecondScene() {
        synchronized (_scenesStack) {
            final int numScene = _scenesStack.size();
            if (numScene > 1) {
                return _scenesStack.get(numScene-2);
            }
            return null;
        }
    }

    @Override
    public boolean sceneFinish(SMScene scene, final SceneParams params) {
        synchronized (_scenesStack) {
            final SMScene topScene = getTopScene();
            if (params == null || params.getPopStackCount() <= 1) {
                if (getSecondScene() == null) {
                    return false;
                }
            } else {
                if (_scenesStack.size()-params.getPopStackCount() < 0) {
                    return false;
                }
            }

            if (topScene == scene) {
                runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        SMScene secondScene = null;
                        synchronized (_scenesStack) {
                            if (params == null || params.getPopStackCount() <= 1) {
                                secondScene = getSecondScene();
                            } else {
                                int popCount = params.getPopStackCount();
                                params.setPopStackCount(1);
                                for (int i = 0; i < popCount; i++) {
                                    secondScene = getSecondScene();
                                    if (i < popCount-1) {
                                        // 즉시 제거;
                                        secondScene.onPause();
                                        secondScene.onDestoryView();
                                        _scenesStack.remove(secondScene);
                                    }
                                }
                            }
                        }
                        topScene.setState(SMScene.STATE_FINISHING);
//                        topScene.hide();
                        if (secondScene!=null) {
                            secondScene.setState(SMScene.STATE_RESUMING);
                            secondScene.setVisible(false);
                            secondScene.onSceneResult(topScene.getSceneResult());
                            secondScene.onResume();
//                            secondScene.show();
                            _runningScene = secondScene;
                        }
                    }
                });
            }
        }
        return true;
    }

    @Override
    public TextureManager getTextureManager() {
        return mTextureManager;
    }

    @Override
    public void showProgress(boolean show, RectF bounds) {
//        mProgressView.show(show, bounds);
    }

    @Override
    public void showUploadProgress(boolean show, int status, RectF bounds) {
//        mUploadProgress.show(show, status, bounds);
    }

    @Override
    public void setDisplayRawWidth(int displayRawWidth, int displayRawHeight) {
        mDisplayRawWidth = displayRawWidth;
        mDisplayRawHeight = displayRawHeight;

        mDeviceWidth = mDisplayRawWidth;
        mDeviceHeight = mDisplayRawHeight;

        mWidth = BASE_SCREEN_WIDTH;
        mHeight = mDisplayRawHeight * BASE_SCREEN_WIDTH / mDisplayRawWidth;

        mDisplayAdjust = (float)BASE_SCREEN_WIDTH / mDisplayRawWidth;

        beginProjectionMatrix();
    }

    @Override
    public int getDisplayRawWidth() {
        return mDisplayRawWidth;
    }

    @Override
    public int getDisplayRawHeight() {
        return mDisplayRawHeight;
    }

//    @Override
//    public void setPreviewSurfaceView(PreviewSurfaceView view) {
//        mPreviewSurface = view;
//    }

//    @Override
//    public PreviewSurfaceView getPreviewSurfaceView() {
//        return mPreviewSurface;
//    }

    @Override
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    @Override
    public int getFrameBufferId() {
        return mFrameBufferId;
    }

    @Override
    public void setFrameBufferId(int frameBufferId) {
        mFrameBufferId = frameBufferId;
    }

    @Override
    public Sprite getFrameBufferSprite() {
        return mFrameBuffer;
    }

    @Override
    public float[] getFrameBufferMatrix() {
        return mFrameBufferMatrix;
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {

                synchronized (_scenesStack) {
                    SMScene scene = getTopScene();
                    if (scene != null) {
                        scene.onActivityResult(requestCode, resultCode, data);
                    }
                }
            }
        });
    }

    private int mScreenOrientation = 0;

    @Override
    public int getScreenOrientation() {
        return ((mScreenOrientation + 45) / 90 * 90) % 360;
    }

    private class OrientationListener extends OrientationEventListener {

        public OrientationListener(Context context) {
            super(context);
        }

        public int roundOrientation(int orientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == ORIENTATION_UNKNOWN)
                return;

            mScreenOrientation = orientation;
        }
    }

    private boolean mScissorTestEnable = false;

    @Override
    public void enableScissorTest(boolean enable) {
        mScissorTestEnable = enable;
        if (enable) {
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        } else {
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        }
    }

    @Override
    public boolean isScissorTestEnabled() {
        return mScissorTestEnable;
    }


    @Override
    public void setSideMenuOpenPosition(float position) {
        SideMenu.GetSideMenu().setOpenPosition(position);
    }

    @Override
    public SIDE_MENU_STATE getSideMenuState() {
        return SideMenu.GetSideMenu().getState();
    }

    @Override
    public SMScene getRunningScene() {
        return _runningScene;
    }

    private long _threadId;
    public long getThreadId() {return _threadId;}

    private long _lastUpdate;

    @Override
    public void runWithScene(SMScene scene) {
        pushScene(scene);
        startSceneAnimation();

    }

    private boolean _invalid = true;

    @Override
    public void startSceneAnimation() {
        startTimer();
    }

    @Override
    public void stopSceneAnimation() {
        _invalid = true;
    }

    @Override
    public void replaceScene(SMScene scene) {
        if (_runningScene==null) {
            runWithScene(scene);
            return;
        }

        if (scene==_nextScene) {
            return;
        }

        if (_nextScene!=null) {
            if (_nextScene.isRunning()) {
                _nextScene.onExit();
            }

            _nextScene.cleanup();
            _nextScene = null;
        }

        int index = _scenesStack.size() - 1;
        _sendCleanupToScene = true;

        _scenesStack.set(index, scene);
        _nextScene = scene;
    }

    @Override
    public void pushScene(SMScene scene) {
        _sendCleanupToScene = false;

        _scenesStack.push(scene);
        _nextScene = scene;
    }

    @Override
    public void popScene() {
        _scenesStack.pop();

        int c = _scenesStack.size();
        if (c==0) {
            // app finish
            end();
        } else {
            _sendCleanupToScene = true;
            _nextScene = _scenesStack.get(c-1);
        }
    }

    public void end() {
        // app finish
    }

    @Override
    public void popToRootScene() {
        popToSceneStackLevel(1);
    }

    @Override
    public void popToSceneStackLevel(int level) {

        int c = _scenesStack.size();
        if (level==0) {
            // app finish.. ?? no more scene in stack
            end();
            return;
        }


        if (c>0 && level>=c) {
            level = c-1;
        }

        SMScene firstOnStackScene = _scenesStack.peek();
        if (firstOnStackScene==_runningScene) {
            _scenesStack.pop();
            --c;
        }


        while (c>level) {
            SMScene current = _scenesStack.peek();

            if (current.isRunning()) {
                current.onExit();
            }

            current.cleanup();

            _scenesStack.pop();
            --c;
        }

        _nextScene = _scenesStack.peek();
        _sendCleanupToScene = true;
    }

    public void setNextScene() {

        boolean runningIsTransition = _runningScene instanceof TransitionScene;
        boolean newIsTransition = _nextScene instanceof TransitionScene;

        if (!newIsTransition) {
            if (_runningScene!=null) {
                _runningScene.onExitTransitionDidStart();
                _runningScene.onExit();
            }

            if (_sendCleanupToScene && _runningScene!=null) {
                _runningScene.cleanup();
                _runningScene.setState(SMScene.STATE_FINISHING);
            }
        }

        if (_runningScene!=null) {
            _runningScene = null;
        }

        _runningScene = _nextScene;
        _nextScene = null;

        if ((!runningIsTransition) && _runningScene!=null) {
            _runningScene.onEnter();
            _runningScene.onEnterTransitionDidFinish();
        }
    }

    @Override
    public SMScene getPreviousScene() {
        int c = _scenesStack.size();
        if (c<=1) return null;

        return _scenesStack.get(c-2);
    }

    // pop_back -> remove last element like Java pop
    // back -> return last element like Java peek

    @Override
    public void popSceneWithTransition(SMScene scene) {

        assert (_runningScene!=null);

        // remvoe last element
        _scenesStack.pop();
        int c = _scenesStack.size();
        if (c==0) {
            // app finish
            end();
        } else {
            _sendCleanupToScene = true;
            _nextScene = scene;
        }
    }

    @Override
    public boolean isSendCleanupToScene() {
        return _sendCleanupToScene;
    }

    @Override
    public int getSceneStackCount() {
        return _scenesStack.size();
    }

    @Override
    public Size getWinSize() {
        return new Size(mWidth, mHeight);
    }

    protected boolean _paused = false;
    @Override
    public void paused() {
        if (_paused) {
            return;
        }


    }
    @Override
    public void resume() {

    }
    @Override
    public boolean isPaused()
    {
        return _paused;
    }

}
