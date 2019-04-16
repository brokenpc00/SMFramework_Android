package com.interpark.smframework;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.interpark.smframework.base.SMScene;

// GLSurface view를 만들때 마다 director가 한개씩 생성...

public class SMSurfaceView extends GLSurfaceView {
    private SMDirector mDirector;
    FragmentActivity mActivity = null;

    public SMSurfaceView(FragmentActivity activity) {
        this(activity, false);
    }

    public SMSurfaceView(FragmentActivity activity, boolean transulant) {
        super(activity, null);
        mActivity = activity;
        init(activity, transulant);
    }
    private void init(FragmentActivity activity, boolean transulant) {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);

        if (transulant) {
            getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }


        mDirector = new SMDirector(activity, this);

        setRenderer(mDirector);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onResume() {
        mDirector.onResume();

        // onResume이 들어오면 renderer에 전달
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        // onPause가 들어오면 renderer에 전달
        mDirector.onPause();
    }


    public SMDirector getDirector() {
        return mDirector;
    }

    public void startSMFrameWorkScene(SMScene scene) {
        if (mDirector==null) {
            mDirector = new SMDirector(mActivity, this);

            setRenderer(mDirector);
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        }
        mDirector.runWithScene(scene);
//        mDirector.setRootScene(scene);
    }

    private boolean _softkeyShown = false;
    private boolean _multiTouch = true;

    public boolean _isSoftKeyShown() {
        return _softkeyShown;
    }
    public void setSoftKeyShow(boolean softkeyShown) {
        this._softkeyShown = softkeyShown;
    }

    public boolean isMultiTouchEnable() {return _multiTouch;}
    public void setMultiTouchEnable(boolean enable) {
        _multiTouch = enable;
    }

//    private void handleTouchesBegin(int num, int id, float x, float y) {
//
//    }
//
//    private void handleTouchesMove(int num, int id, float x, float y) {
//
//    }
//
//    @Override
//    public boolean onTouchEvent(final MotionEvent pMotionEvent) {
//        final int pointerNumber = pMotionEvent.getPointerCount();
//        final int[] ids = new int[pointerNumber];
//        final float[] xs = new float[pointerNumber];
//        final float[] ys = new float[pointerNumber];
//
//        for (int i = 0; i < pointerNumber; i++) {
//            ids[i] = pMotionEvent.getPointerId(i);
//            xs[i] = pMotionEvent.getX(i);
//            ys[i] = pMotionEvent.getY(i);
//        }
//
//        switch (pMotionEvent.getAction() & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_POINTER_DOWN:
//                final int indexPointerDown = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//                if (!_multiTouch && indexPointerDown != 0) {
//                    break;
//                }
//                final int idPointerDown = pMotionEvent.getPointerId(indexPointerDown);
//                final float xPointerDown = pMotionEvent.getX(indexPointerDown);
//                final float yPointerDown = pMotionEvent.getY(indexPointerDown);
//
//                this.queueEvent(new Runnable() {
//                    @Override
//                    public void run() {
////                        handle touch down
//                    }
//                });
//                break;
//
//            case MotionEvent.ACTION_DOWN:
//                // there are only one finger on the screen
//                final int idDown = pMotionEvent.getPointerId(0);
//                final float xDown = xs[0];
//                final float yDown = ys[0];
//
//                this.queueEvent(new Runnable() {
//                    @Override
//                    public void run() {
////                        handle touch down
//                    }
//                });
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                if (!_multiTouch) {
//                    // handle only touch with id == 0
//                    for (int i = 0; i < pointerNumber; i++) {
//                        if (ids[i] == 0) {
//                            final int[] idsMove = new int[]{0};
//                            final float[] xsMove = new float[]{xs[i]};
//                            final float[] ysMove = new float[]{ys[i]};
//                            this.queueEvent(new Runnable() {
//                                @Override
//                                public void run() {
//                                    // handle touch move
//                                }
//                            });
//                            break;
//                        }
//                    }
//                } else {
//                    this.queueEvent(new Runnable() {
//                        @Override
//                        public void run() {
////                            handle touch move
//                        }
//                    });
//                }
//                break;
//
//            case MotionEvent.ACTION_POINTER_UP:
//                final int indexPointUp = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//                if (!_multiTouch && indexPointUp != 0) {
//                    break;
//                }
//                final int idPointerUp = pMotionEvent.getPointerId(indexPointUp);
//                final float xPointerUp = pMotionEvent.getX(indexPointUp);
//                final float yPointerUp = pMotionEvent.getY(indexPointUp);
//
//                this.queueEvent(new Runnable() {
//                    @Override
//                    public void run() {
//                        // handle touch up
//                    }
//                });
//                break;
//
//            case MotionEvent.ACTION_UP:
//                // there are only one finger on the screen
//                final int idUp = pMotionEvent.getPointerId(0);
//                final float xUp = xs[0];
//                final float yUp = ys[0];
//
//                this.queueEvent(new Runnable() {
//                    @Override
//                    public void run() {
//                        // handle touch up
//                    }
//                });
//                break;
//
//            case MotionEvent.ACTION_CANCEL:
//                if (!_multiTouch) {
//                    // handle only touch with id == 0
//                    for (int i = 0; i < pointerNumber; i++) {
//                        if (ids[i] == 0) {
//                            final int[] idsCancel = new int[]{0};
//                            final float[] xsCancel = new float[]{xs[i]};
//                            final float[] ysCancel = new float[]{ys[i]};
//                            this.queueEvent(new Runnable() {
//                                @Override
//                                public void run() {
//                                    // handle touch cancel
//                                }
//                            });
//                            break;
//                        }
//                    }
//                } else {
//                    this.queueEvent(new Runnable() {
//                        @Override
//                        public void run() {
//                            // handle touch cancel
//                        }
//                    });
//                }
//                break;
//        }
//
//        return true;
//    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // android os로 부터 전달받은 renderer Touch를 Queue에 넣는다

        mDirector.addTouchEvent(event);
        return true;
    }

    public boolean onBackPressed() {
        boolean ret = mDirector.onBackPressd();
        if (!ret) {
            mDirector = null;
        }
        return ret;
    }

    public void onSaveInstanceState(Bundle outState) {
//        getDirector().onSaveInstanceState(outState);
    }
}
