package com.interpark.smframework.base;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Matrix4f;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.NativeImageProcess.ImageProcessing;
import com.interpark.smframework.base.sprite.CanvasSprite;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.ActionManager;
import com.interpark.smframework.base.types.BGColorTo;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.Ref;
import com.interpark.smframework.base.types.Scheduler;
import com.interpark.smframework.base.types.SEL_SCHEDULE;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;
import com.interpark.smframework.view.ViewConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;


public class SMView extends Ref {
    // graphics matrix
    protected static Matrix sMatrix = new Matrix();

    // 부모뷰
    protected SMView _parent = null;

    public static final long FLAGS_TRANSFORM_DIRTY = 1;
    public static final long FLAGS_CONTENT_SIZE_DIRTY = 1 << 1;
    public static final long FLAGS_RENDER_AS_3D = 1 << 3;
    public static final long FLAGS_DIRTY_MASK = FLAGS_TRANSFORM_DIRTY | FLAGS_CONTENT_SIZE_DIRTY;

    // 자식뷰
    protected ArrayList<SMView> _children = new ArrayList<>();

    public ArrayList<SMView> getChildren() { return _children; }

    protected int _tag = -1;
    public void setTag(int tag) {_tag = tag;}
    public final  int getTag() {return _tag;}

    protected String _name = "";
    public void setName(String name) {
        _name = name;
        _hashOfName = _name.hashCode();
    }
    public final String getName() {return _name;}

    protected int _hashOfName = 0;

    public void setLocalZOrder(int zOrder) {
        if (getLocalZOrder()==zOrder) {
            return;
        }

        _setLocalZOrder(zOrder);
        if (_parent!=null) {
            _parent.reorderChild(this, zOrder);
        }

//            _eventDispatcher.setDirtyForNode(this);
    }

    public void reorderChild(SMView child, int zOrder) {
        assert (child!=null);
        _reorderChildDirty = true;
        child.updateOrderOfArrival();
        child._setLocalZOrder(zOrder);
    }

    public int getLocalZOrder() {return _localZOrder;}

    public void _setLocalZOrder(int z) {
        _localZOrderAndArrival = ((long)(z) << 32) | (_localZOrderAndArrival & 0xffffffff);
        _localZOrder = z;
    }

    protected static int s_globalOrderOfArrival = 0;

    public void updateOrderOfArrival() {
        _localZOrderAndArrival = (_localZOrderAndArrival & 0xffffffff00000000L) | (++s_globalOrderOfArrival);
    }

    public void setGlobalZOrder(float globalZOrder) {
        if (_globalZOrder != globalZOrder)
        {
            _globalZOrder = globalZOrder;
//            _eventDispatcher.setDirtyForNode(this);
        }
    }

    protected int _localZOrder = 0;
    protected long _localZOrderAndArrival = 0;
    protected float _globalZOrder = 0;
    protected boolean _running = false;
    protected boolean _visible = true;


    protected static int __attachedNodeCount = 0;
    public int getAttachedNodeCount() {return __attachedNodeCount;}

    public final boolean isRunning() {
        return _running;
    }

    public void SMViewOnEnter() {
        if (!_running) {
            ++__attachedNodeCount;
        }

        _isTransitionFinished = false;

        for (final SMView child : _children) {
            child.SMViewOnEnter();
        }

        this.onResume();
        _running = true;
    }

    public void onEnter() {
        if (!_running) {
            ++__attachedNodeCount;
        }

        _isTransitionFinished = false;

        for (final SMView child : _children) {
            child.onEnter();
        }

        this.onResume();
        _running = true;
    }

    public void onEnterTransitionDidFinish() {
        _isTransitionFinished = true;

        for (final SMView child : _children) {
            child.onEnterTransitionDidFinish();
        }
    }

    public void onExitTransitionDidStart() {
        for (final SMView child : _children) {
            child.onExitTransitionDidStart();
        }
    }

    public void SMViewOnExit() {
        if (_running) {
            --__attachedNodeCount;
        }

        this.onPause();

        _running = false;

        for (final SMView child : _children) {
            child.SMViewOnExit();
        }
    }

    public void onExit() {
        if (_running) {
            --__attachedNodeCount;
        }

        this.onPause();

        _running = false;

        for (final SMView child : _children) {
            child.onExit();
        }
    }

    public void cleanup() {
        for (final SMView child : _children) {
            child.cleanup();
        }

        releaseGLResources();

        if (_smoothFlags > 0) {
            _smoothFlags = 0;
            _newAnimOffset = new Vec3(0, 0, 0);
            _animOffset = new Vec3(0, 0, 0);
            _newAnimScale = _newScale = 1.0f;

            _position.x = _realPosition.x = _newPosition.x;
            _position.y = _realPosition.y = _newPosition.y;
            _positionZ = _realPosition.z = _newPosition.z;

            _scaleX = _scaleY = _scaleZ = _realScale = _newScale;

            _transformUpdated = true;

        }
    }


    // touch motion event target
    protected SMView _touchMotionTarget = null;

    // tag
    private Object mTag;

    protected boolean _ignoreTouchBounds = false;

    public void setIgnoreTouchBounds(boolean ignore) {_ignoreTouchBounds = ignore;}

    // touch
    public static interface OnTouchListener {
        public int onTouch(SMView view, MotionEvent event);
    }
    protected OnTouchListener mOnTouchListener;

    // click
    public static interface OnClickListener {
        public void onClick(SMView view);
    }
    protected OnClickListener mOnClickListener;

    // double click
    public static interface OnDoubleClickListener {
        public void onDoubleClick(SMView view);
    }
    protected OnDoubleClickListener mOnDoubleClickListener;


    // long click
    public static interface OnLongClickListener {
        public void onLongClick(SMView view);
    }
    private OnLongClickListener mOnLongClickListener;


    // state change
    public static interface OnStateChangeListener {
        public void onStateChange(SMView view, STATE state);
    }
    private OnStateChangeListener mOnStateChangeListener;

    public enum STATE {
        NORMAL,
        PRESSED,
        MAX,
    }


    protected static final long TIME_PRESSED_TO_NORMAL = 100;
    protected static final long TIME_NORMAL_TO_PRESSED = 50;
    protected static final float DISABLE_ALPHA_VALUE = 0.5f;
    public static final float DEFAULT_PUSHDOWN_HEIGHT = 10f;

    protected boolean mStateChangeAni = false;
    protected long	mStateChangeTime;
    protected float mStateAlpha = 0;


    public STATE _pressState = STATE.NORMAL;

    protected void onStateChangePressToNormal(MotionEvent event) {}
    protected void onStateChangeNormalToPress(MotionEvent event) {}

    private void stateChangePressToNormal(MotionEvent event) {
        if (_pressState==STATE.PRESSED) {
            setState(STATE.NORMAL);

            // for Override
            onStateChangePressToNormal(event);

            // for listener
            if (mOnStateChangeListener != null) {
                if (_eventTargetStateChange!=null) {
                    mOnStateChangeListener.onStateChange(_eventTargetStateChange, _pressState);
                } else {
                    mOnStateChangeListener.onStateChange(this, _pressState);
                }
            }


        }
    }


    private void stateChangeNormalToPress(MotionEvent event) {
        if (_pressState==STATE.NORMAL) {
            setState(STATE.PRESSED);

            // for Override
            onStateChangeNormalToPress(event);

            // for listener
            if (mOnStateChangeListener != null) {
                if (_eventTargetStateChange!=null) {
                    mOnStateChangeListener.onStateChange(_eventTargetStateChange, _pressState);
                } else {
                    mOnStateChangeListener.onStateChange(this, _pressState);
                }
            }
        }
    }


    private int mId;

    // alpha
    protected float _realAlpha = 1.0f;
    protected float _newAlpha = 1.0f;

    // position
    protected Vec3 _realPosition = new Vec3(0, 0, 0);
    protected Vec3 _newPosition = new Vec3(0, 0, 0);

    // scale
    protected float _realScale = 1.0f;
    protected float _newScale = 1.0f;

    // content Size
    protected Size _newContentSize = new Size(0, 0);

    // rotating
    protected Vec3 _realRotation = new Vec3(0, 0, 0);
    protected Vec3 _newRotation = new Vec3(0, 0, 0);

    // color
    protected Color4F _newColor= new Color4F(0, 0, 0, 0);

    // for animation
    protected float _animAlpha = 1.0f;
    protected float _newAnimAlpha = 1.0f;
    protected Vec3 _animOffset = new Vec3(0, 0, 0);
    protected Vec3 _newAnimOffset = new Vec3(0, 0, 0);
    protected float _animScale = 1.0f;
    protected float _newAnimScale = 1.0f;
    protected Vec3 _animRotation = new Vec3(0, 0, 0);
    protected Vec3 _newAnimRotation = new Vec3(0, 0, 0);

    protected float _rotationX = 0.0f;
    protected float _rotationY = 0.0f;
    protected float _rotationZ_X = 0.0f;
    protected float _rotationZ_Y = 0.0f;

    protected float _alpha = 1.0f;
    protected float _scaleX = 1.0f;
    protected float _scaleY = 1.0f;
    protected float _scaleZ = 1.0f;

    protected Vec2 _position = new Vec2(0, 0);
    protected float _positionZ = 0.0f;

    protected Vec2 _anchorPoint = new Vec2(0, 0);
    protected Vec2 _anchorPointInPoints = new Vec2(0, 0);

    // ????
    protected Vec2 _normalizePosition = new Vec2(0, 0);

    protected Size _contentSize = new Size(0, 0);
    protected boolean _contentSizeDirty = true;

    protected float[] _bgColor, _newBgColor, _tintColor, _realTintColor;


    public static final int VISIBLE = View.VISIBLE;
    public static final int INVISIBLE = View.INVISIBLE;

    public static final long ANIM_TIME_SHOW = 200;
    public static final long ANIM_TIME_HIDE = 150;

    private long mMotionEventTime;
    protected boolean _touchHasFirstClicked = false;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsPressed = false;
    protected boolean _cancelIfTouchOutside = false;


    private boolean mIsEnable = true;
    private boolean mClickable = false;
    private boolean mLongClickable = false;
    private boolean mDoubleClickable = false;

    private float mScaledDoubleTouchSlope;

    public static SMView create(IDirector director) {
        return create(director, 0, 0, 0, 0, 0);
    }

    public static SMView create(IDirector director, float x, float y, float width, float height) {
        return create(director, 0, x, y, width, height, 0, 0);
    }

    public static SMView create(IDirector director, int tag, float x, float y, float width, float height) {
        return create(director, tag, x, y, width, height, 0, 0);
    }

    public static SMView create(IDirector director, int tag, float x, float y, float width, float height, float anchorX, float anchorY) {
        SMView view = new SMView(director);

        if (view!=null) {
            view.setPosition(x, y);
            view.setContentSize(new Size(width, height));
            view.setAnchorPoint(new Vec2(anchorX, anchorY));
            if (view.init()) {
                view.setTag(tag);
                return view;
            }
        }

        return view;
    }

    // constructor
    public SMView(IDirector director) {
        super(director);
//        setPosition(0, 0);
//        setContentSize(0, 0);
//        setAnchorPoint(new Vec2(0, 0));
        _updateFlags = 0;
        _actionManager = _director.getActionManager();
        _scheduler = _director.getScheduler();
        mScaledDoubleTouchSlope = ViewConfig.getScaledDoubleTouchSlop(director);
        setCascadeAlphaEnable(true);
    }

    protected boolean init() {
        return true;
    }

    public SMView(IDirector director, int id) {
        this(director);
        setId(id);
    }

    public SMView(IDirector director, float x, float y, float width, float height) {
        this(director);
        setPosition(x, y);
        setContentSize(width, height);
    }

    public void setId(int id) {
        mId = id;
    }

    public Context getContext() {
        return _director.getContext();
    }

    public void setContentSize(Size size) {
        setContentSize(size, true);
    }

    public void setContentSize(float width, float height) {
        setContentSize(new Size(width, height), true);
    }

    public void setContentSize(Size size, boolean immediate) {

        if (!size.equals(_contentSize)) {
            if (immediate) {
                _contentSize = new Size(size);

                _anchorPointInPoints.set(_contentSize.width*_anchorPoint.x, _contentSize.height*_anchorPoint.y);
            } else {
                _newContentSize = size;
                scheduleSmoothUpdate(VIEWFLAG_CONTENT_SIZE);
            }
        }
    }

    public Size getContentSize() {
        return _contentSize;
    }

    public void setWidth(float width) {
        _contentSize.width = width;
    }

    public void setHeight(float height) {
        _contentSize.height = height;
    }

    public boolean isInitialized() {
        return true;
    }

    public int getId() {
        return mId;
    }

    public SMView getParent() {
        return _parent;
    }

    public Vec3 getPosition3D() {return new Vec3(_position.x, _position.y, _positionZ);}

    public Vec2 getPosition() {return new Vec2(_position);}

    public float getPositionX() {return _position.x;}

    public float getPositionY() {return _position.y;}

    public float getPositionZ() {return _positionZ;}

    public float getX() {
        return _position.x;
    }

    public float getY() {
        return _position.y;
    }

    public float getNewX() {
        return _newPosition.x;
    }

    public float getNewY() {
        return _newPosition.y;
    }

    public float getZ() {
        return _positionZ;
    }

    public Vec2 getWolrdPosition() {
        return getWolrdPosition(getPosition());
    }

    public Vec2 getWolrdPosition(final Vec2 localPos) {
        Vec2 pos = new Vec2(localPos);

        SMView parent = getParent();
        while (parent!=null) {
            pos.addLocal(parent.getAnchorPointInPoints());
            parent = parent.getParent();
        }

        return new Vec2(pos);
    }

    public float getScaleX() {
        return _scaleX;
    }

    public float getScaleY() {
        return _scaleY;
    }

    public float getScaleZ() {
        return _scaleZ;
    }

    public float getScale() {
        return _scaleX;
    }

    public float getLeft() {
        return _position.x;
    }

    public float getTop() {
        return _position.y;
    }

    public float getRight() {
        return _position.x + _contentSize.width;
    }

    public float getBottom() {
        return _position.y + _contentSize.height;
    }

    public void setAlpha(float alpha) {setAlpha(alpha, true);}

    public void setScaleX(float scale) {
        if (_scaleX==scale) return;
        _scaleX = scale;
        _transformUpdated = _transformDirty =_inverseDirty = true;
    }
    public void setScaleY(float scale) {
        if (_scaleY==scale) return;
        _scaleY = scale;
        _transformUpdated = _transformDirty =_inverseDirty = true;
    }
    public void setScaleZ(float scale) {
        if (_scaleZ==scale) return;
        _scaleZ = scale;
        _transformUpdated = _transformDirty =_inverseDirty = true;
    }

    public void setScale(float scale) {
        setScale(scale, true);
    }

    public float getRotationX() {return _rotationX;}

    public float getRotationY() {return _rotationY;}

    public float getRotationZ() {return _rotationZ_X;}

    public float getRotationSkewX() {return _rotationZ_X;}

    public float getRotationSkewY() {return _rotationZ_Y;}

    public float getRotation() {return _rotationZ_X;}
    public Vec3 getRotation3D() {return new Vec3(_rotationX, _rotationY, _rotationZ_X);}

    public void setRotationSkewX(float rotationX) {
        if (_rotationZ_X==rotationX) {
            return;
        }

        _rotationZ_X = rotationX;
    }

    public void setRotationSkewY(float rotationY) {
        if (_rotationZ_Y==rotationY) {
            return;
        }

        _rotationZ_Y = rotationY;
    }

    public void setRotation(float rotate) {
        setRotation(rotate, true);
    }
    public void setRotation(float rotate, boolean immediate) {
        setRotationZ(rotate, immediate);
    }

    public void setRotationX(float rotate) {
        setRotationX(rotate, true);
    }

    public void setRotationX(float rotate, boolean immediate) {
        if (immediate) {
            _realRotation.x = _newRotation.x = rotate;
            Vec3 rotate3D = new Vec3(_realRotation.x+_animRotation.x, _realRotation.y+_animRotation.y, _realRotation.z+_animRotation.z);
            if (_rotationX==rotate3D.x && _rotationY==rotate3D.y && _rotationZ_X==rotate3D.z) {
                return;
            }

            _rotationX = rotate3D.x;
            _rotationY = rotate3D.y;
            _rotationZ_X = _rotationZ_Y = rotate3D.z;
        } else {
            if (_newRotation.x==rotate) {
                return;
            }

            _newRotation.z = rotate;

            scheduleSmoothUpdate(VIEWFLAG_ROTATE);
        }
    }

    public void setRotationY(float rotationY) {
        setRotationY(rotationY, true);
    }

    public void setRotationY(float rotationY, boolean immediate) {
        if (immediate) {
            _realRotation.y = _newRotation.y = rotationY;
            Vec3 rotate3D = new Vec3(_realRotation.x+_animRotation.x, _realRotation.y+_animRotation.y, _realRotation.z+_animRotation.z);
            if (_rotationX==rotate3D.x && _rotationY==rotate3D.y && _rotationZ_X==rotate3D.z) {
                return;
            }

            _rotationX = rotate3D.x;
            _rotationY = rotate3D.y;
            _rotationZ_X = _rotationZ_Y = rotate3D.z;

        } else {
            if (_newRotation.y==rotationY) {
                return;
            }

            _newRotation.y = rotationY;

            scheduleSmoothUpdate(VIEWFLAG_ROTATE);
        }
    }

    public void setRotationZ(float rotateZ) {
        setRotationZ(rotateZ, true);
    }

    public void setRotationZ(float rotateZ, boolean immediate) {
        if (immediate) {
            _realRotation.z = _newPosition.z = rotateZ;
            float rotation = rotateZ + _animRotation.z;
            if (_rotationZ_X!=rotation) {
                _rotationZ_X = _rotationZ_Y = rotation;
            _transformUpdated = _transformDirty = _inverseDirty = true;
            }
        } else {
            if (_newRotation.z==rotateZ) {
                return;
            }

            _newRotation.z = rotateZ;

            scheduleSmoothUpdate(VIEWFLAG_ROTATE);
        }
    }

    public void setRotation3D(Vec3 rotate) {
        setRotation3D(rotate, true);
    }

    public void setRotation3D(Vec3 rotate, boolean immediate) {
        if (immediate) {
            _realRotation = new Vec3(rotate.x, rotate.y, rotate.z);
            _newRotation = new Vec3(rotate.x, rotate.y, rotate.z);

            Vec3 rotate3D = new Vec3(_realRotation.x*_animRotation.x, _realRotation.y*_animRotation.y, _realRotation.z*_animRotation.z);
            if (_rotationX==rotate3D.x && _rotationY==rotate3D.y && _rotationZ_X==rotate3D.z) {
                return;
            }

            _rotationX = rotate3D.x;
            _rotationY = rotate3D.y;
            _rotationZ_X = _rotationZ_Y = rotate3D.z;
        } else {
            if (_newRotation.equals(rotate)) {
                return;
            }

            _newRotation = new Vec3(rotate.x, rotate.y, rotate.z);

            scheduleSmoothUpdate(VIEWFLAG_ROTATE);
        }
    }

    public void setAlpha(float alpha, boolean immediate) {

        if (immediate ) {
            _realAlpha = _newAlpha = alpha;
            _bgColor[3] = _alpha = alpha * _animAlpha;

        } else {
            if (_newAlpha==alpha) {
                return;
            }

            _newAlpha = alpha;
            scheduleSmoothUpdate(VIEWFLAG_COLOR);
        }
    }

    public void setScale(float scale, boolean immediate) {

        if (immediate ) {
            _realScale = _newScale = scale;
            _scaleX = _scaleY = _scaleZ = scale * _animScale;
        } else {
            if (_newScale==scale) {
                return;
            }

            _newScale = scale;
            scheduleSmoothUpdate(VIEWFLAG_SCALE);
        }
    }


//    public void setX(float x) {
//
//        _newPosition.x = _position.x = x;
//    }
//
//    public void setX(float x, boolean immediate) {
//        _newPosition.x = x;
//        if (immediate) {
//            _position.x = x;
//        }
//    }


    public void setY(float y) {
        _newPosition.y = _position.y = y;
    }

    public void setPosition(Vec2 pos) {
        this.setPosition(pos.x, pos.y, true);
    }

    public void setPosition(Vec2 pos, boolean immediate) {
        setPosition(pos.x, pos.y, immediate);
    }

    public void setPosition(float x, float y) {
        setPosition(x, y, true);
    }

    public void setPositionX(float x) {
        setPositionX(x, true);
    }

    public void setPositionY(float y) {
        setPositionY(y, true);
    }

    public void setPositionZ(float z) {
        setPositionZ(z, true);
    }

    public void setPosition3D(Vec3 pos) {
        setPosition3D(pos, true);
    }


    public void setPositionX(float x, boolean immediate) {
        if (immediate) {
            if (_position.x != x+_animOffset.x) {
                _position.x = x+_animOffset.x;
                _realPosition.x = _newPosition.x = x;
            }
        } else {
            if (_newPosition.x==x) {
                return;
            }
            _newPosition.x = x;
            scheduleSmoothUpdate(VIEWFLAG_POSITION);
        }
    }

    public void setPositionY(float y, boolean immediate) {
        if (immediate) {
            if (_position.y != y+_animOffset.y) {
                _position.y = y+_animOffset.y;
                _realPosition.y = _newPosition.y = y;
            }
        } else {
            if (_newPosition.y==y) {
                return;
            }
            _newPosition.y = y;
            scheduleSmoothUpdate(VIEWFLAG_POSITION);
        }
    }

    public void setPositionZ(float z, boolean immediate) {
        if (immediate) {
            if (_positionZ==z) {
                return;
            }
            _positionZ = z;

            _realPosition.z = _newPosition.z = z;
        } else {
            if (_newPosition.z == z) {
                return;
            }

            _newPosition.z = z;
            scheduleSmoothUpdate(VIEWFLAG_POSITION);
        }
    }


    public void setPosition(float x, float y, boolean immediate) {
        setPositionX(x, immediate);
        setPositionY(y, immediate);
    }

    public void setPosition3D(Vec3 pos, boolean immediate) {
        if (immediate) {
            if (_positionZ!=pos.z+_animOffset.z) {
                _positionZ = pos.z+_animOffset.z;
            }

            _position.x = pos.x+_animOffset.x;
            _position.y = pos.y+_animOffset.y;
        } else {
            setPositionX(pos.x, false);
            setPositionY(pos.y, false);
            setPositionZ(pos.z, false);
        }
    }

    public void setZ(float z, boolean immediate) {
        _newPosition.z = z;
        if (immediate) {
            _positionZ = z;
        }
    }


    public void setY(float y, boolean immediate) {
        _position.y = y;
        _newPosition.y = y;
        if (immediate) {
            _position.y = y;
        }
    }


    public void setPosition(float x, float y, float z, boolean immediate) {
        _newPosition.x = x;
        _newPosition.y = y;
        _newPosition.z = z;
        if (immediate) {
            _position.x = x;
            _position.y = y;
            _positionZ = z;
        }
    }


    public Vec2 getAnchorPointInPoints() {
        return new Vec2(_anchorPointInPoints);
    }

    public Vec2 getAnchorPoint() {
        return new Vec2(_anchorPoint);
    }

    public void setAnchorPoint(float anchorX, float anchorY) {
        setAnchorPoint(new Vec2(anchorX, anchorY));
    }

    public void setAnchorPoint(Vec2 point) {
//        setAnchorPoint(point, true);
        if (!point.equals(_anchorPoint)) {
            _anchorPoint.set(point);
            _anchorPointInPoints.set(_contentSize.width*_anchorPoint.x, _contentSize.height*_anchorPoint.y);
            _transformUpdated = true;
        }
    }

//    public void setAnchorPoint(Vec2 point, boolean immediate) {
//        if (immediate) {
//            if (!point.equals(_anchorPoint)) {
//                _anchorPoint = point;
//                _anchorPointInPoints.set(_contentSize.width*_anchorPoint.x, _contentSize.height*_anchorPoint.y);
//            }
//
//        } else {
//
//        }
//
//    }

    public void setAnimOffset(Vec2 pos) {
        setAnimOffset(pos, false);
    }

    public void setAnimOffset(Vec2 pos, boolean immediate) {
        if (_newAnimOffset.x != pos.x || _newAnimOffset.y != pos.y) {
            _newAnimOffset.x = pos.x;
            _newAnimOffset.y = pos.y;


            scheduleSmoothUpdate(VIEWFLAG_ANIM_OFFSET);
        }

        if (immediate) {
            _animOffset.x = _newAnimOffset.x;
            _animOffset.y = _newAnimOffset.y;
            _animOffset.z = _newAnimOffset.z;
        }
    }

    public void setAnimOffset3D(Vec3 pos) {
        setAnimOffset3D(pos, false);
    }


    public void setAnimOffset3D(Vec3 pos, boolean immediate) {
        if (!_newAnimOffset.equals(pos)) {
            _newAnimOffset.x = pos.x;
            _newAnimOffset.y = pos.y;
            _newAnimOffset.z = pos.z;

            scheduleSmoothUpdate(VIEWFLAG_ANIM_OFFSET);
        }
        if (immediate) {
            _animOffset.x = _newAnimOffset.x;
            _animOffset.y = _newAnimOffset.y;
            _animOffset.z = _newAnimOffset.z;
        }
    }


    public void setAnimAlpha(float alpha) {setAnimAlpha(alpha, true);}

    public void setAnimAlpha(float alpha, boolean immediate) {
        if (_newAnimAlpha!=alpha) {
            _newAnimAlpha = alpha;
            scheduleSmoothUpdate(VIEWFLAG_ANIM_COLOR);
        }

        if (immediate) {
            _animAlpha = _newAnimAlpha;
        }
    }

    public void setAnimScale(float scale) {
        setAnimScale(scale, false);
    }

    public void setAnimScale(float scale, boolean immediate) {
        if (_newAnimScale!=scale) {
            _newAnimScale = scale;
            scheduleSmoothUpdate(VIEWFLAG_ANIM_SCALE);
        }

        if (immediate) {
            _animScale = _newAnimScale;
        }
    }

    public void setAnimRotate(float rotate) {
        setAnimRotate(rotate, false);
    }

    public void setAnimRotate(float rotate, boolean immediate) {
        if (_newAnimRotation.z != rotate) {
            _newAnimRotation.z = rotate;

            scheduleSmoothUpdate(VIEWFLAG_ANIM_ROTATE);
        }

        if (immediate) {
            _animRotation.x = _newAnimRotation.x;
            _animRotation.y = _newAnimRotation.y;
            _animRotation.z = _newAnimRotation.z;
        }
    }

    public void setAnimRotate3D(Vec3 rotate) {
        setAnimRotate3D(rotate, false);
    }

    public void setAnimRotate3D(Vec3 rotate, boolean immediate) {
        if (!_newAnimRotation.equals(rotate)) {
            _newAnimRotation.x = rotate.x;
            _newAnimRotation.y = rotate.y;
            _newAnimRotation.z = rotate.z;

            scheduleSmoothUpdate(VIEWFLAG_ANIM_ROTATE);
        }

        if (immediate) {
            _animRotation.x = _newAnimRotation.x;
            _animRotation.y = _newAnimRotation.y;
            _animRotation.z = _newAnimRotation.z;
        }
    }

    public void setLeft(float x) {
        _newPosition.x = _position.x = x;
    }

    public void setRight(float x) {
        _newPosition.x = _position.x = x - _contentSize.width;
    }

    public void setTop(float y) {
        _newPosition.y = _position.y = y;
    }

    public void setBottom(float y) {
        _newPosition.y = _position.y = y - _contentSize.height;
    }

    public void setLeft(float x, boolean immediate) {
        _newPosition.x = x;
        if (immediate) {
            _position.x = _newPosition.x;
        }
    }

    public void setRight(float x, boolean immediate) {
        _newPosition.x = x - _contentSize.width;
        if (immediate) {
            _position.x = _newPosition.x;
        }
    }

    public void setTop(float y, boolean immediate) {
        _newPosition.y = y;
        if (immediate) {
            _position.y = _newPosition.y;
        }
    }

    public void setBottom(float y, boolean immediate) {
        _newPosition.y = y - _contentSize.height;
        if (immediate) {
            _position.y = _newPosition.y;
        }
    }

    public float getOriginX() {
        return _position.x - _anchorPointInPoints.x;
    }

    public float getOriginY() {
        return _position.y - _anchorPointInPoints.y;
    }

    public float getScreenX() {

        float x = 0;
        if (_parent!=null) {
            x = _parent.getScreenX();
            return x + getOriginX()*_parent.getScale();
        }

        return x + getOriginX();
    }

    public Vec2 convertToWorldSpace(final Vec2 nodePos) {

        return new Vec2(getScreenX(nodePos.x), getScreenY(nodePos.y));
    }

    public Vec2 convertToNodeSpace(final Vec2 worldPos) {
        float baseX = getScreenX();
        float baseY = getScreenY();

        float posX = worldPos.x - baseX;
        float posY = worldPos.y - baseY;

        return new Vec2(posX, posY);
    }

    public float getScreenX(float posX) {
        float x = 0;
        if (_parent != null) {
            x = _parent.getScreenX();
            return x + _parent.getScale()*posX;
        }
        return x + posX;
    }

    public float getScreenY() {
        float y = 0;
        if (_parent!=null) {
            y = _parent.getScreenY();
            return y + getOriginY()*_parent.getScale();
        }

        return y + getOriginY();
    }

    public float getScreenY(float posY) {
        float y = 0;
        if (_parent != null) {
            y = _parent.getScreenY();
            return y + _parent.getScale()*posY;
        }
        return y + posY;
    }

    public float getScreenScale() {
        float scale = 1;
        if (_parent != null) {
            scale = _parent.getScreenScale();
        }
        return scale * _scaleX;
    }

    public float getScreenAngleX() {
        float angleX = 0;
        if (_parent != null) {
            angleX = _parent.getScreenAngleX();
        }
        return angleX + _rotationX;
    }

    public float getScreenAngleY() {
        float angleY = 0;
        if (_parent != null) {
            angleY = _parent.getScreenAngleY();
        }
        return angleY + _rotationY;
    }

    public float getScreenAngleZ() {
        float angleX = 0;
        if (_parent != null) {
            angleX = _parent.getScreenAngleZ();
        }
        return angleX + _rotationZ_X;
    }

    public float getAlpha() {
        return _alpha;
    }

    public void setCancelIfTouchOutside(boolean cancelIfTouchOutside) {
        _cancelIfTouchOutside = cancelIfTouchOutside;
    }

    private float mPivotX=0, mPivotY=0;
    public void setPivot(float pivotX, float pivotY) {
        mPivotX = pivotX;
        mPivotY = pivotY;
    }

    public float getPivotX() {
        return mPivotX;
    }

    public float getPivotY() {
        return mPivotY;
    }

    // View Transform Matrix
    public void transformMatrix(final float[] matrix) {
        if (matrix != null) {

            float x = _position.x;
            float y = _position.y;
            float z = _positionZ;

            // 현재 x, y, z를 입력하고
            android.opengl.Matrix.translateM(matrix, 0, x, y, z);
            // scale
            if (_scaleX!=1.0f || _scaleY!=1.0f || _scaleZ!=1.0f) {
                android.opengl.Matrix.scaleM(matrix, 0, _scaleX, _scaleY, _scaleZ);
            }

            // rotate
            if (_rotationX != 0) {
                android.opengl.Matrix.rotateM(matrix, 0, _rotationX, 1, 0, 0);
            }
            if (_rotationY != 0) {
                android.opengl.Matrix.rotateM(matrix, 0, _rotationY, 0, 1, 0);
            }
            if (_rotationZ_X != 0) {
                android.opengl.Matrix.rotateM(matrix, 0, _rotationZ_X, 0, 0, 1);
            }


//            if (!_anchorPointInPoints.equals(Vec2.ZERO)) {
//                matrix[12] += matrix[0] * -_anchorPointInPoints.x + matrix[4]*-_anchorPointInPoints.y + matrix[8]*-_anchorPointInPoints.z;
//                matrix[13] += matrix[1] * -_anchorPointInPoints.x + matrix[5]*-_anchorPointInPoints.y + matrix[9]*-_anchorPointInPoints.z;
//                matrix[14] += matrix[2] * -_anchorPointInPoints.x + matrix[6]*-_anchorPointInPoints.y + matrix[10]*-_anchorPointInPoints.z;
//                // m[12 + mi] += m[mi] * x + m[4 + mi] * y + m[8 + mi] * z;
//            }
            // 아래와 동일함.

            // anchor point를 적용한 길이 x -> 0~width, y -> 1~height
            android.opengl.Matrix.translateM(matrix, 0, -_anchorPointInPoints.x, -_anchorPointInPoints.y, 0);
        }
    }




    public void setVisible(boolean visible) {
        if (_visible!=visible) {
            _visible = visible;
            if (_visible) {
                _transformUpdated = _transformDirty = _inverseDirty = true;
        }
    }

            }
    public boolean isVisible() {
        return _visible;
    }

    private boolean _enabled = true;
    public boolean isEnabled() {
        return _enabled;
    }
    public void setEnabled(boolean enabled) {
        if (_enabled == enabled)
            return;
        _enabled = enabled;
    }


    // touch event

    public void setOnTouchListener(OnTouchListener listener) {
        setOnTouchListener(listener, null);
    }

    public void setOnTouchListener(OnTouchListener listener, SMView eventTarget) {
        mOnTouchListener = listener;
        _eventTargetTouch = eventTarget;

        if (listener!=null) {
            setTouchMask(TOUCH_MASK_TOUCH);
        } else {
            clearTouchMask(TOUCH_MASK_TOUCH);
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        setOnClickListener(listener, null);
    }

    public void setOnClickListener(OnClickListener listener, SMView eventTarget) {
        if (!isClickable()) {
            setClickable(true);
        }
        mOnClickListener = listener;
        _eventTargetClick = eventTarget;

        if (listener!=null) {
            setTouchMask(TOUCH_MASK_CLICK);
        } else {
            clearTouchMask(TOUCH_MASK_CLICK);
        }
    }

    public void setOnDoubleClickListener(OnDoubleClickListener listener) {
        setOnDoubleClickListener(listener, null);
    }

    public void setOnDoubleClickListener(OnDoubleClickListener listener, SMView eventTarget) {
        if (!isClickable()) {
            setDoubleClickable(true);
        }
        mOnDoubleClickListener = listener;
        _eventTargetDoubleClick = eventTarget;

        if (listener!=null) {
            setTouchMask(TOUCH_MASK_DOUBLECLICK);
        } else {
            clearTouchMask(TOUCH_MASK_DOUBLECLICK);
        }
    }

    public void setOnLongClickListener(OnLongClickListener listener) {

    }
    public void setOnLongClickListener(OnLongClickListener listener, SMView eventTarget) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnLongClickListener = listener;
        _eventTargetLongPress = eventTarget;

        if (listener!=null) {
            setTouchMask(TOUCH_MASK_LONGCLICK);
        } else {
            clearTouchMask(TOUCH_MASK_LONGCLICK);
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        setOnStateChangeListener(listener, null);
    }
    public void setOnStateChangeListener(OnStateChangeListener listener, SMView eventTarget) {
        mOnStateChangeListener = listener;
        _eventTargetStateChange = eventTarget;
    }

    public boolean isClickable() {
        return mClickable;
    }

    public void setClickable(boolean clickable) {
        mClickable = clickable;
    }

    public boolean isDoubleClickable() {
        return mDoubleClickable;
    }


    public void setDoubleClickable(boolean doubleClickable) {
        if (doubleClickable) {
            setClickable(true);
        }
        mDoubleClickable = doubleClickable;
    }

    public boolean isLongClickable() {
        return mLongClickable;
    }

    public void setLongClickable(boolean longClickable) {
        if (longClickable) {
            setClickable(true);
        }
        mLongClickable = longClickable;
    }

    // click 판정시 호출 됨.
    protected void performClick() {
        if (mOnClickListener != null) {
            if (_eventTargetClick!=null) {
                mOnClickListener.onClick(_eventTargetClick);
            } else {
                mOnClickListener.onClick(this);
            }

        }

    }

    // doublic click 판정시 호출 됨.
    protected void performDoubleClick(Vec2 worldPoint) {
        if (mOnDoubleClickListener != null) {
            if (_eventTargetDoubleClick!=null) {
                mOnDoubleClickListener.onDoubleClick(_eventTargetDoubleClick);
            } else {
                mOnDoubleClickListener.onDoubleClick(this);
            }
        }
    }

    // lonck lick 판정시 호출 됨.
    protected void performLongClick() {
        if (mOnLongClickListener != null) {
            if (_eventTargetLongPress!=null) {
                mOnLongClickListener.onLongClick(_eventTargetLongPress);
            } else {
                mOnLongClickListener.onLongClick(this);
            }
        }
        if (mLongClickable) {
            // for vibrator
//            ((Vibrator)_director.getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(25);
        }
    }



    // child & parent

    private void addChildHelper(SMView child, int localZOrder, int tag, final String name, boolean setTag) {
        boolean ownNode = false;
        for (SMView parent = getParent(); parent!=null; parent = parent.getParent()) {
            if (parent==child) {
                ownNode = true;
                break;
            }
        }

        if (ownNode) {
            assert (false);
        }

        if (_children == null) {
            _children = new ArrayList<SMView>();
        }

        if (_children.isEmpty()) {
            this.childrenAlloc();
        }

        this.insertChild(child, localZOrder);

        if (setTag) {
            child.setTag(tag);
        } else {
            child.setName(name);
        }

        child._parent = this;

        child.updateOrderOfArrival();

        if (_running) {
            child.onEnter();
            if (_isTransitionFinished) {
                child.onEnterTransitionDidFinish();
            }
        }
    }

    public void addChild(SMView child, int localZOrder, int tag) {
        assert (child!=null);
        assert (child.getParent()!=null);
        addChildHelper(child, localZOrder, tag, "", true);
    }

    public void addChild(SMView child, int localZOrder, final String name) {
        assert (child!=null);
        assert (child.getParent()!=null);
        addChildHelper(child, localZOrder, -1, name, false);
    }

    public void addChild(SMView child) {
        this.addChild(child, child.getLocalZOrder(), child._name);
    }

    public void addChild(SMView child, int zOrder) {
        this.addChild(child, zOrder, child._name);
    }

    protected void childrenAlloc() {
        _children.ensureCapacity(4);
    }

    protected boolean _transformUpdated = true;
    protected boolean _reorderChildDirty = false;
    protected boolean _isTransitionFinished = false;

    protected void insertChild(SMView child, int z) {
        _transformUpdated = true;
        _reorderChildDirty = true;
        _children.add(child);
        child._setLocalZOrder(z);
        child.onAddToParent(this);
    }

    public void removeFromParent() {
        if (getParent()!=null) {
            SMView view = getParent();
            if (view!=null && view._touchMotionTarget==this) {
                view._touchMotionTarget = null;
            }
        }
        this.removeFromParentAndCleanup(true);
    }

    public void removeFromParentAndCleanup(boolean cleanup) {
        if (_parent!=null) {
            _parent.removeChild(this, cleanup);
        }
    }

    public void removeChild(SMView child) {
        this.removeChild(child, true);
    }

    public void removeChild(SMView child, boolean cleanup) {
        if (_children.isEmpty()) {
            return;
        }

        int index = _children.indexOf(child);
        if (index!=-1) {
            detachChild(child, index, cleanup);
        }

        if (child!=null && child==_touchMotionTarget) {
            _touchMotionTarget = null;
        }
    }

    public SMView getChildByTag(int tag) {
        assert (tag!=-1);

        for (final SMView child : _children) {
            if (child!=null && child._tag==tag) {
                return child;
            }
        }

        return null;
    }

    public void removeChildByTag(int tag) {
        removeChildByTag(tag, true);
    }

    public void removeChildByTag(int tag, boolean cleanup) {
        SMView child = this.getChildByTag(tag);
        if (child!=null) {
            this.removeChild(child, cleanup);
        }
    }

    public final SMView getChildByName(final String name) {
        assert (!name.isEmpty());

        int hash = name.hashCode();

        for (final SMView child : _children) {
            if (child._hashOfName==hash && child._name.equalsIgnoreCase(name)) {
                return child;
            }
        }

        return null;
    }

    public void removeChildByName(final String name, boolean cleanup) {
        assert (name!="");

        SMView child = this.getChildByName(name);
        if (child!=null) {
            this.removeChild(child, cleanup);
        }
    }

    public void detachChild(SMView child, int childIndex, boolean doCleanup) {
        // 실제로 삭
        if (_running) {
            child.onExitTransitionDidStart();
            child.onExit();
        }

        if (doCleanup) {
            child.cleanup();
        }

        child._parent = null;
        _children.remove(childIndex);
        child.onRemoveFromParent(this);

        if (_internal_current_update_child_ != null && _internal_current_update_child_ == child) {
            _internal_this_child_removed_ = true;
        }
    }

    public void removeAllChildren() {
        this.removeAllChildrenWithCleanup(true);
    }

    protected void onAddToParent(SMView parent) {
    }

    protected void onRemoveFromParent(SMView parent) {
    };


    private boolean _internal_this_child_removed_ = false;

    public void removeAllChildrenWithCleanup(boolean cleanup) {
        for (final SMView child : _children) {
            if (_running) {
                child.onExitTransitionDidStart();
                child.onExit();
            }

            if (cleanup) {
                child.cleanup();
            }

            child.onRemoveFromParent(this);
            child._parent = null;

        }

        _children.clear();
        _touchMotionTarget = null;
    }

    public boolean isContainsChidren(SMView child) {
        if (_children != null) {
            return _children.contains(child);
        }
        return false;
    }

    public int getChildrenCount() {
        return _children == null ? 0 : _children.size();
    }

    public int getChildCount() {
        return _children == null ? 0 : _children.size();
    }

    public SMView getChild(int index) {
        if (_children == null || getChildCount() <= index) {
            //throw new ArrayIndexOutOfBoundsException(index);
            return null;
        }
        return _children.get(index);
    }


    // position & size

    public boolean containsPoint(final Vec2 point) {
        return containsPoint(point.x, point.y);
    }

    public boolean containsPoint(final float x, final float y) {
        return !(x < 0 || y < 0 || x > _contentSize.width || y > _contentSize.height);
    }


    protected boolean _ignoreAnchorPointForPosition = false;

    protected boolean _usingNormalizedPosition = false;

    protected boolean _normalizedPositionDirty = false;

    protected float _skewX = 0.0f;

    protected float _skewY = 0.0f;

    protected boolean _transformDirty = true;

    protected boolean _inverseDirty = true;


    protected Matrix4f _modelViewTransform;

    protected Matrix4f _transform;

    protected Matrix4f _inverse;

    protected Matrix4f[] _additionalTransform = null;



//    public final Matrix getNodeToParentTransform() {
//        if (_transformDirty) {
//            float x = _position.x;
//            float y = _position.y;
//            float z = _positionZ;
//
//            if (_ignoreAnchorPointForPosition) {
//                x += _anchorPointInPoints.x;
//                y += _anchorPointInPoints.y;
//            }
//
//            boolean needSkewMatrix = (_skewX > 0 || _skewY > 0);
//
//            Matrix4f translation = new Matrix4f();
//            translation.loadTranslate(x, y, z);
//
////            _transform.loadRotate();
//
//
//
//
//
//
//        }
//    }

    private final float[] sMapPoint = new float[2];
    protected boolean pointInView(final float x, final float y) {
        if (_scaleX == 0)
            return false;

        sMatrix.reset();
        sMatrix.postTranslate(-_position.x, -_position.y);
        if (_scaleX != 1.0f || _scaleY!=1.0f) {
            sMatrix.postScale(1/_scaleX, 1/_scaleY);
        }
        if (_rotationZ_X != 0) {
            sMatrix.postRotate(-_rotationZ_X);
        }
        sMapPoint[0] = x;
        sMapPoint[1] = y;
        sMatrix.mapPoints(sMapPoint);
        float ptX = sMapPoint[0];
        float ptY = sMapPoint[1];

        return containsPoint(ptX, ptY);
    }





    // dispath touch event
    public static final int TOUCH_FALSE = 0;
    public static final int TOUCH_TRUE = 1;
    public static final int TOUCH_INTERCEPT = 2;

//    public Vec2 getTouchPointInView(Vec2 pos) {
//        sMatrix.reset();
//        sMatrix.postTranslate(-getX()+_anchorPointInPoints.x, -getY()+_anchorPointInPoints.y);
//        if (_scale != 1) {
//            sMatrix.postScale(1/_scale, 1/_scale);
//        }
//        if (_rotationZ_X != 0) {
//            sMatrix.postRotate(-_rotationZ_X);
//        }
//
//        MotionEvent ev = MotionEvent.obtain(event);
//        ev.transform(sMatrix);
//    }

    public int dispatchTouchEvent(MotionEvent event, SMView view, boolean checkBounds)
    {
        sMatrix.reset();
        sMatrix.postTranslate(-view.getX()+view._anchorPointInPoints.x, -view.getY()+view._anchorPointInPoints.y);
        if (view._scaleX != 1.0f || view._scaleY!=1.0f) {
            sMatrix.postScale(1/view._scaleX, 1/view._scaleY);
        }
        if (view._rotationZ_X != 0) {
            sMatrix.postRotate(-view._rotationZ_X);
        }

        MotionEvent ev = MotionEvent.obtain(event);
        ev.transform(sMatrix);


        int action = ev.getAction();
        Vec2 point = new Vec2(ev.getX(0), ev.getY(0));
        boolean isContain = view.containsPoint(point);

        view._touchPrevPosition.set(view._touchCurrentPosition);
        view._touchCurrentPosition.set(point);
        if (!view._startPointCaptured) {
            view._startPointCaptured = true;
            view._touchStartPosition.set(_touchCurrentPosition);
            view._touchPrevPosition.set(_touchCurrentPosition);
        }

        if (ev.getAction()==MotionEvent.ACTION_CANCEL || ev.getAction()==MotionEvent.ACTION_UP || ev.getAction()==MotionEvent.ACTION_POINTER_UP) {
            view._startPointCaptured = false;
        }

        _lastTouchLocation.set(point.x, point.y);


        if (action==MotionEvent.ACTION_DOWN) {
            view._touchStartPosition.set(point.x, point.y);
            view._touchStartTime = _director.getGlobalTime();
        } else {
            view._touchLastPosition.set(point.x, point.y);
        }

        if (view._cancelIfTouchOutside && action == MotionEvent.ACTION_DOWN && !isContain) {
            view.cancel();
        }

        int ret = TOUCH_FALSE;
        if (!checkBounds || isContain || view == _touchMotionTarget) {
            ret = view.dispatchTouchEvent(ev);
        } else if (view._ignoreTouchBounds && action == MotionEvent.ACTION_DOWN) {
            DispatchChildrenRet touchRet = view.dispatchChildren(ev, ret);
            ret = touchRet.retI;
//            view.dispatchChildren(ev);
        }
        ev.recycle();
        return ret;
    }


    protected boolean _startPointCaptured = false;
    protected Vec2 _touchPrevPosition = new Vec2(0, 0);
    protected Vec2 _touchCurrentPosition = new Vec2(0, 0);
    protected Vec2 _touchStartPosition = new Vec2(0, 0);
    protected float _touchStartTime = 0.0f;
    protected Vec2 _touchLastPosition = new Vec2(0, 0);


    public int dispatchTouchEvent(MotionEvent event) {
        // touch event를 통해 click, doubleclick을 구분한다 long-Press는 update frame에서 상태 값으로 체크한다
        if (!isEnabled()) {
            return TOUCH_TRUE;
        }

        if (mOnTouchListener != null) {
            int ret = TOUCH_FALSE;
            if (_eventTargetTouch!=null) {
                ret = mOnTouchListener.onTouch(_eventTargetTouch, event);
            } else {
                ret = mOnTouchListener.onTouch(this, event);
            }

            if (ret != TOUCH_FALSE) {
                return ret;
            }
        }


        final int action = event.getAction();


        if (_touchMotionTarget != null) {
            // motion target이 있으면 (내가 처리할...)
            if (action == MotionEvent.ACTION_DOWN) {
                _touchMotionTarget = null;
            } else {
                final int ret = dispatchTouchEvent(event, _touchMotionTarget, false);
                if (_touchMotionTarget.mClickable) {
                    // touch 가능 상태이냐???

                    if (action == MotionEvent.ACTION_UP) {
                        // action up일 때
                        long time = Math.abs(_director.getTickCount()-_touchMotionTarget.mMotionEventTime);
                        // touch에 걸린시간이 TA_TIMEOUT 보다 작으면(약 0.3초)
                        if (time < ViewConfig.TAP_TIMEOUT) {
                            // 유효한 클릭으로 처리
                            if (_touchMotionTarget.mDoubleClickable) {
                                // DoubleClick 가능한 상태
                                if (_touchMotionTarget._touchHasFirstClicked) {
                                    _touchMotionTarget._touchHasFirstClicked = false;

                                    float dx = event.getX() - _touchMotionTarget.mInitialTouchX;
                                    float dy = event.getY() - _touchMotionTarget.mInitialTouchY;
                                    float slope = (float)Math.sqrt(dx*dx+dy*dy);
                                    if (slope < mScaledDoubleTouchSlope) {
                                        // doublie click로 판정
                                        _touchMotionTarget.performDoubleClick(new Vec2(event.getX(), event.getY()));
                                    }
                                } else {
                                    // 두번째 클릭 실패..
                                    _touchMotionTarget._touchHasFirstClicked = true;
                                    _touchMotionTarget.mMotionEventTime = _director.getTickCount();
                                }
                            } else {
                                // Click만 가능한 상태면 바로 Click (Doubletap 불가)
                                _touchMotionTarget._touchHasFirstClicked = false;
                                if (ret == TOUCH_FALSE) {
                                    // 일반 click로 판정
                                    _touchMotionTarget.performClick();
                                }
                            }
                        } else {
                            // 클릭 시간 타임 아웃... 무효처리
                            _touchMotionTarget._touchHasFirstClicked = false;
                        }
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        // 움직이다가 내 영역을 벗어났다면 무효처리
                        if (!_touchMotionTarget.pointInView(event.getX(), event.getY())) {
                            _touchMotionTarget._touchHasFirstClicked = false;
                        }
                    }
                }

                // action up을 위에서 처리하지 못했으면 무효처리.. (타임 아웃 등)
                if (action == MotionEvent.ACTION_CANCEL ||
                        action == MotionEvent.ACTION_UP) {
                    _touchMotionTarget.mIsPressed = false;
                    _touchMotionTarget.stateChangePressToNormal(event);
                    _touchMotionTarget = null;
                }
                if (ret == TOUCH_INTERCEPT) {
                    return TOUCH_INTERCEPT;
                }
                return TOUCH_TRUE;
            }
        } else {
            // motion target이 없으면 (내가 처리할...) 하위 (자식 뷰)에서 처리한다.
            if (action == MotionEvent.ACTION_DOWN) {
                DispatchChildrenRet touchRet = dispatchChildren(event, 0);
                if (touchRet.retB) {
                    return touchRet.retI;
                }
            }
        }

        if (isClickable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    return TOUCH_FALSE;
                default:
                    return TOUCH_TRUE;
            }
        }

        return TOUCH_FALSE;
    }

    protected int onTouch(MotionEvent event) {
        if (mOnTouchListener != null) {
            return mOnTouchListener.onTouch(this, event);
        }
        return TOUCH_FALSE;
    }

    public class DispatchChildrenRet {
        public DispatchChildrenRet() {
            retB = false;
            retI = TOUCH_TRUE;
        }
        public boolean retB = false;
        public int retI = TOUCH_TRUE;
    }

    private DispatchChildrenRet dispatchChildren(MotionEvent event, int touchRet) {

        DispatchChildrenRet ret = new DispatchChildrenRet();
        ret.retI = touchRet;

        final int numChildCount = getChildCount();
        for (int i = numChildCount-1; i >= 0; i--) {
            SMView child = getChild(i);
            if (!child.isVisible()) continue;
            ret.retI = dispatchTouchEvent(event, child, true);
            if (ret.retI!=TOUCH_FALSE) {
                _touchMotionTarget = child;
                if (child._touchMotionTarget==null) {
                    _touchMotionTarget.mIsPressed = true;
                    _touchMotionTarget.mMotionEventTime = _director.getTickCount();
                    _touchMotionTarget.stateChangeNormalToPress(event);

                    if (!_touchMotionTarget._touchHasFirstClicked) {
                        _touchMotionTarget.mInitialTouchX = event.getX();
                        _touchMotionTarget.mInitialTouchY = event.getY();
                    }

                    mIsPressed = false;
                    // 이거 문제되면 빼자.
                    onStateChangePressToNormal(event);
                    _touchHasFirstClicked = false;
                }
                ret.retB = true;
                return ret;
            }
        }

        ret.retB = false;
        return ret;
    }

    // 각종 오차 허용범위 값
    protected static final float COLOR_TOLERANCE = 0.0005f;
    protected static final float SMOOTH_DIVIDER = 3.0f;



    protected boolean isSmoothUpdate(long flag) {
        return (_smoothFlags & flag) > 0;
    }

    public static class InterpolateRet {
        public InterpolateRet(boolean b, float f) {
            retB = b;
            retF = f;
        }
        public boolean retB = false;
        public float retF = 0.0f;
    }

    private void onInternalSmoothUpate(float dt) {
        long flags = 0;

        // size
        if (isSmoothUpdate(VIEWFLAG_CONTENT_SIZE)) {
            flags |= VIEWFLAG_CONTENT_SIZE;

            boolean needUpdate = false;


            Size s = new Size(_contentSize);
            InterpolateRet ret1 = smoothInterpolate(s.width, _newContentSize.width, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret1.retB;
            s.width = ret1.retF;
            InterpolateRet ret2 = smoothInterpolate(s.height, _newContentSize.height, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret2.retB;
            s.height = ret2.retF;

            setContentSize(s, true);

            if (!needUpdate) {
                unscheduleSmoothUpdate(VIEWFLAG_CONTENT_SIZE);
            }

            _transformUpdated = true;
        }

        boolean animColor = false;
        if (isSmoothUpdate(VIEWFLAG_ANIM_COLOR)) {
            flags |= VIEWFLAG_ANIM_COLOR;
            animColor = true;
            // 일단 color는 나중에 alpha 부터

            boolean needUpdate = false;
            InterpolateRet ret1 = smoothInterpolate(_animAlpha, _newAnimAlpha, AppConst.Config.TOLERANCE_SCALE);
            needUpdate |= ret1.retB;
            _animAlpha = ret1.retF;

            if (!needUpdate) {
                unscheduleSmoothUpdate(VIEWFLAG_ANIM_COLOR);
            }
        }

        if (isSmoothUpdate(VIEWFLAG_COLOR) || animColor) {
            flags |= VIEWFLAG_COLOR;

            // color는 나중에
            // 일단 alpha부터
            boolean needUpdate = false;
            InterpolateRet ret1 = smoothInterpolate(_realAlpha, _newAlpha, AppConst.Config.TOLERANCE_SCALE);
            needUpdate |= ret1.retB;
            _realAlpha = ret1.retF;

            if (!needUpdate && !animColor) {
                unscheduleSmoothUpdate(VIEWFLAG_COLOR);
            }

            _bgColor[3] = _alpha = _realAlpha * _animAlpha;


            _transformUpdated = true;
        }

        // position
        boolean animOffset = false;
        if (isSmoothUpdate(VIEWFLAG_ANIM_OFFSET)) {
            flags |= VIEWFLAG_ANIM_OFFSET;
            animOffset = true;

            boolean needUpdate = false;
            InterpolateRet ret1 = smoothInterpolate(_animOffset.x, _newAnimOffset.x, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret1.retB;
            _animOffset.x = ret1.retF;

            InterpolateRet ret2 = smoothInterpolate(_animOffset.y, _newAnimOffset.y, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret2.retB;
            _animOffset.y = ret2.retF;

            InterpolateRet ret3 = smoothInterpolate(_animOffset.z, _newAnimOffset.z, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret3.retB;
            _animOffset.z = ret3.retF;

            if (!needUpdate) {
                unscheduleSmoothUpdate(VIEWFLAG_ANIM_OFFSET);
            }

        }

        if (isSmoothUpdate(VIEWFLAG_POSITION) || animOffset) {
            flags |= VIEWFLAG_POSITION;
            boolean needUpdate = false;


            InterpolateRet ret1 = smoothInterpolate(_realPosition.x, _newPosition.x, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret1.retB;
            _realPosition.x = ret1.retF;

            InterpolateRet ret2 = smoothInterpolate(_realPosition.y, _newPosition.y, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret2.retB;
            _realPosition.y = ret2.retF;

            InterpolateRet ret3 = smoothInterpolate(_realPosition.z, _newPosition.z, AppConst.Config.TOLERANCE_POSITION);
            needUpdate |= ret3.retB;
            _realPosition.z = ret3.retF;

            if (!needUpdate && !animOffset) {
                unscheduleSmoothUpdate(VIEWFLAG_POSITION);
            }

            _position.x = _realPosition.x + _animOffset.x;
            _position.y = _realPosition.y + _animOffset.y;
            _positionZ = _realPosition.z + _animOffset.z;

            _transformUpdated = true;
        }

        // scale
        boolean animScale = false;
        if (isSmoothUpdate(VIEWFLAG_ANIM_SCALE)) {
            flags |= VIEWFLAG_ANIM_SCALE;
            animScale = true;

            boolean needUpdate = false;
            InterpolateRet ret1 = smoothInterpolate(_animScale, _newAnimScale, AppConst.Config.TOLERANCE_ALPHA, AppConst.Config.SMOOTH_DIVIDER);
            needUpdate |= ret1.retB;
            _animScale = ret1.retF;

            if (!needUpdate) {
                unscheduleSmoothUpdate(VIEWFLAG_ANIM_SCALE);
            }
        }

        if (isSmoothUpdate(VIEWFLAG_SCALE) || animScale) {
            flags |= VIEWFLAG_SCALE;
            boolean needUpdate = false;
            InterpolateRet ret1 = smoothInterpolate(_realScale, _newScale, AppConst.Config.TOLERANCE_ALPHA, AppConst.Config.SMOOTH_DIVIDER);
            needUpdate |= ret1.retB;
            _realScale = ret1.retF;

            if (!needUpdate && !animScale) {
                unscheduleSmoothUpdate(VIEWFLAG_SCALE);
            }

            _scaleX = _scaleY = _scaleZ = _realScale * _animScale;

            _transformUpdated = true;
        }

        // rotate
        boolean animRotate = false;
        if (isSmoothUpdate(VIEWFLAG_ANIM_ROTATE)) {
            flags |= VIEWFLAG_ANIM_ROTATE;
            animRotate = true;

            boolean needUpdate = false;
            InterpolateRet ret1 = smoothInterpolate(_animRotation.x, _newAnimRotation.x, AppConst.Config.TOLERANCE_ROTATE);
            needUpdate |= ret1.retB;
            _animRotation.x = ret1.retF;

            InterpolateRet ret2 = smoothInterpolate(_animRotation.y, _newAnimRotation.y, AppConst.Config.TOLERANCE_ROTATE);
            needUpdate |= ret2.retB;
            _animRotation.y = ret2.retF;

            InterpolateRet ret3 = smoothInterpolate(_animRotation.z, _newAnimRotation.z, AppConst.Config.TOLERANCE_ROTATE);
            needUpdate |= ret3.retB;
            _animRotation.z = ret3.retF;

            if (!needUpdate) {
                unscheduleSmoothUpdate(VIEWFLAG_ANIM_ROTATE);
            }
        }

        if (isSmoothUpdate(VIEWFLAG_ROTATE) || animRotate) {
            flags |= VIEWFLAG_ROTATE;
            boolean needUpdate = false;

            InterpolateRet ret1 = smoothInterpolate(_realRotation.x, _newRotation.x, AppConst.Config.TOLERANCE_ROTATE);
            needUpdate |= ret1.retB;
            _realRotation.x = ret1.retF;

            InterpolateRet ret2 = smoothInterpolate(_realRotation.y, _newRotation.y, AppConst.Config.TOLERANCE_ROTATE);
            needUpdate |= ret2.retB;
            _realRotation.y = ret2.retF;

            InterpolateRet ret3 = smoothInterpolate(_realRotation.z, _newRotation.z, AppConst.Config.TOLERANCE_ROTATE);
            needUpdate |= ret3.retB;
            _realRotation.z = ret3.retF;

            if (!needUpdate && !animRotate) {
                unscheduleSmoothUpdate(VIEWFLAG_ROTATE);
            }

            _rotationX = _realRotation.x + _animRotation.x;
            _rotationY = _realRotation.y + _animRotation.y;
            _rotationZ_X = _rotationZ_Y = _realRotation.z + _animRotation.z;

            _transformUpdated = true;
        }

        onSmoothUpdate(flags, dt);
    }



//    public boolean isVisibleAnimation() {
//        return (mShowAnimator != null && mShowAnimator.hasStarted()) || (mHideAnimator != null && mHideAnimator.hasStarted());
//    }

    public void update(float dt) {}

    private SMView _internal_current_update_child_ = null;

    public void sortAllChildren() {
        if (_reorderChildDirty) {
            sortNodes(_children);
            _reorderChildDirty = false;
        }
    }

    public void updateTintColor() {}

    public void visit(float a) {

        if (!_visible) {
            return;
        }

        _director.pushProjectionMatrix();
        transformMatrix(_director.getProjectionMatrix());
        _director.updateProjectionMatrix();

        if (_scissorEnable) {
            enableScissorTest(true);
        }

        // base property... draw first me...
        renderOwn(a);


        // and children
        int i = 0;

        if (_children.size()>0) {
            sortAllChildren();

            for (int size = _children.size(); i<size; ++i) {
                SMView view = _children.get(i);

                if (view!=null && view._localZOrder<0) {
                    view.visit(a);
                } else break;
    }

            draw(a);

            ListIterator<SMView> iter = _children.listIterator(i);
            while (iter.hasNext()) {
                SMView child = iter.next();
                child.visit(a);
            }
        } else {
            draw(a);
        }

        if (_scissorEnable) {
            enableScissorTest(false);
        }

        _director.popProjectionMatrix();

        }

    private void renderOwn(float  alpha) {

        if (_alpha < .001f)
            return;

        // alpha는 여기서 쓰임...
        final float a = _alpha*alpha;

        if (_newBgColor != null) {
            drawBackground(_bgColor[0]*a, _bgColor[1]*a, _bgColor[2]*a, a);
        }

        if (_updateFlags>0) {
            onUpdateOnVisit();
        }

        if (_isCalledScissorEnabled) {
            final float scale = getScreenScale();
            float x, y, w, h;

            float screenX=0, screenY=0;

            Size scissorSize = new Size(0, 0);
            if (_scissorRect!=null) {
                w = _scissorRect.size.width * scale;
                h = _scissorRect.size.height * scale;

                x = getScreenX(_scissorRect.origin.x);
                // gl은 세로 좌표가 반대
                y = _director.getWinSize().height - (getScreenY(_scissorRect.origin.y) + h);
            } else {
                w = _contentSize.width * scale;
                h = _contentSize.height * scale;
                x = getScreenX();
                // gl은 세로 좌표가 반대
                y = _director.getWinSize().height - (getScreenY() + h);
            }

            _targetScissorRect.setRect(x, y, w, h);


            if (!intersectRectInWindow(_targetScissorRect, _director.getWinSize())) {
                // scissor에 해당. 그리지 않음.
                return;
            }

        }

    }

    protected void draw(float a) { }


    // clip to bounds
    // child가 view를 벗어나는 부분은 그리지 않는다. (자른다)
    public void enableScissorTest(boolean enable) {
        if (enable) {

            _director.enableScissorTest(true);
            GLES20.glScissor((int)_targetScissorRect.origin.x, (int)_targetScissorRect.origin.y, (int)_targetScissorRect.size.width, (int)_targetScissorRect.size.height);
        } else {
            _director.enableScissorTest(false);
        }
    }

    private Rect _targetScissorRect = new Rect();
    private Rect _scissorRect = null;
    public void setScissorRect(final Rect rect) {
        if (_scissorRect==null) {
            _scissorRect = new Rect(rect);
        } else {
            _scissorRect.setRect(rect);
        }
    }

    public boolean intersectRectInWindow(Rect rect, final Size winSize) {
        float dw = winSize.width;
        float dh = winSize.height;
        float sw = rect.size.width;
        float sh = rect.size.height;
        float x = rect.origin.x;
        float y = rect.origin.y;

        if (x+sw <= 0 || x >= dw || y+sh <= 0 || y >= dh || sw <= 0 || sh <= 0) {
            // 화면 밖은 리턴
            return false;
        }

        float sx = 0;
        float sy = 0;
        float width = sw;
        float height = sh;
        if (x < 0) {
            sx = -x;
            width -= sx;
        }
        if (y < 0) {
            sy = -y;
            height -= sy;
        }
        if (x + sw > dw) {
            width -= x + sw - dw;
        }
        if (y + sh > dh) {
            height -= y + sh - dh;
        }

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        rect.setRect(x, y, width, height);

        return true;
    }



    protected void drawBackground(float r, float g, float b, float a) {
        _director.setColor(r, g, b, a);
        _director.drawFillRect(0, 0, _contentSize.width, _contentSize.height);
    }

    protected long _updateFlags = 0;

    protected boolean isUpdate(final long flag) { return (_updateFlags & flag) > 0;}

    protected void registerUpdate(final long flag) {
        if ((_updateFlags&flag) > 0) {
            return;
        }

        _updateFlags |= flag;
    }

    protected void unregisterUpdate(final long flag) {
        if (flag==0) {
            _updateFlags = 0;
        } else {
            _updateFlags &= ~flag;
        }
    }

    protected void onUpdateOnVisit(){};

    protected boolean _touchTargeted = false;

    protected SMView _eventTargetTouch = null;
    protected SMView _eventTargetClick = null;
    protected SMView _eventTargetLongPress = null;
    protected SMView _eventTargetDoubleClick = null;
    protected SMView _eventTargetStateChange = null;

    public void cancelTouchEvent(SMView targetView, MotionEvent event) {
        if (targetView!=null) {
            if (targetView._touchMotionTarget!=null) {
                cancelTouchEvent(targetView._touchMotionTarget, event);
                targetView._touchMotionTarget = null;
            } else {
                targetView._touchTargeted = false;
                event.setAction(MotionEvent.ACTION_CANCEL);
                targetView.dispatchTouchEvent(event);
                targetView._touchHasFirstClicked = false;
                targetView.stateChangePressToNormal(event);
            }
        }
    }

    public void cancel() {

        if (_children != null) {
            int numChildCount = getChildCount();
            for (int i = 0; i < numChildCount; i++) {
                SMView child = getChild(i);
                if (child.isEnabled() && child.isVisible()) {
                    child.cancel();
                }
            }
        }
    }

    public void onInitView() {}

    public void onDestoryView() {
        if (_children != null) {
            int numChildCount = getChildCount();
            for (int i = 0; i < numChildCount; i++) {
                SMView child = getChild(i);
                child.onDestoryView();
            }
        }
    }

    public void onResume() {
        if (_children != null) {
            int numChildCount = getChildCount();
            for (int i = 0; i < numChildCount; i++) {
                SMView child = getChild(i);
                if (child.isInitialized()) {
                    child.onResume();
                }
            }
        }

        _scheduler.resumeTarget(this);
        _actionManager.resumeTarget(this);
    }

    public void onPause() {
        if (_children != null) {
            int numChildCount = getChildCount();
            for (int i = 0; i < numChildCount; i++) {
                SMView child = getChild(i);
                if (child.isInitialized()) {
                    child.onPause();
                }
            }
        }

        _scheduler.pauseTarget(this);
        _actionManager.pauseTarget(this);
    }

    public void resumeSchedulerAndActions() {
        _scheduler.resumeTarget(this);
        _actionManager.resumeTarget(this);
    }

    public void pauseSchedulerAndActions() {
        _scheduler.pauseTarget(this);
        _actionManager.pauseTarget(this);
    }

    protected Bundle getInstanceState() {
        return null;
    }
    protected void restoreInstanceState(Bundle bundle) {}

    protected void setTransform(float x, float y, float z,
                                float scale, float angleX, float angleY, float angleZ,
                                boolean immediate) {
        _newPosition.x = x;
        _newPosition.y = y;
        _newPosition.z = z;
        _newScale = scale;
        _newRotation.x = angleX;
        _newRotation.y = angleY;
        _newRotation.z = angleZ;

        if (immediate) {
            _position.x = x;
            _position.y = y;
            _positionZ = z;
            _scaleX = _scaleY = _scaleZ = scale;
            _rotationX = angleX;
            _rotationY = angleY;
            _rotationZ_X = angleZ;
        }
    }

    public Color4F getTintColor() {
        if (_tintColor==null) {
            _tintColor = new float[]{1, 1, 1, 1};
        }
        if (_realTintColor==null) {
            _realTintColor = new float[]{1, 1, 1, 1};
        }

        return new Color4F(_realTintColor[0], _realTintColor[1], _realTintColor[2], _realAlpha);
    }

    protected boolean _cascadeColorEnabled = false;

    public void setCascadeColorEnabled(boolean enable) {
        if (_cascadeColorEnabled == enable)
        {
            return;
        }

        _cascadeColorEnabled = enable;

        if (_cascadeColorEnabled)
        {
            updateCascadeColor();
        }
        else
        {
            disableCascadeColor();
        }
    }

    public void disableCascadeColor()
    {

        for (SMView child : _children) {
            child.updateDisplayedColor(Color4F.WHITE);
        }
    }

    public void updateDisplayedColor(Color4F parentColor) {
        if (_tintColor==null) {
            _tintColor = new float[]{1, 1, 1, 1};
        }
        if (_realTintColor==null) {
            _realTintColor = new float[]{1, 1, 1, 1};
        }

        _tintColor[0] = _realTintColor[0] * parentColor.r;
        _tintColor[1] = _realTintColor[1] * parentColor.g;
        _tintColor[2] = _realTintColor[2] * parentColor.b;
        _tintColor[3] = _realTintColor[3] * parentColor.a;

        updateTintColor();

        if (_cascadeColorEnabled) {
            for (SMView view : _children) {
                view.updateDisplayedColor(new Color4F(_tintColor));
            }
        }
    }

    public boolean isCascadeColorEnabled() {return _cascadeColorEnabled;}

    public void updateCascadeColor() {
        Color4F parentColor = new Color4F(Color4F.WHITE);
        if (_parent!=null && _parent.isCascadeColorEnabled()) {
            parentColor = _parent.getTintColor();
        }
        updateDisplayedColor(parentColor);
    }

    public void setTintColor(float r, float g, float b, float a) {
        if (_realTintColor==null) {
            _realTintColor = new float[] {r, g, b, a};
        } else {
            _realTintColor[0] = r;
            _realTintColor[1] = g;
            _realTintColor[2] = b;
            _realTintColor[3] = a;
        }
        if (_tintColor==null) {
            _tintColor = new float[]{r, g, b, a};
        } else {
            _tintColor[0] = r;
            _tintColor[1] = g;
            _tintColor[2] = b;
            _tintColor[3] = a;
        }

        setTintAlpha(a);

        updateCascadeColor();
    }

    public void setTintColor(float[] color) {
        if (color==null || color.length!=4) return;;

        setTintColor(color[0], color[1], color[2], color[3]);
    }

    public void setTintColor(Color4F color) {
        setTintColor(color.r, color.g, color.b, color.a);
    }

    public void setTintColor(Color4B color) {
        setTintColor(new Color4F(color));
    }

    protected float _realTintAlpha = 1.0f;

    public float getDisplayedAlpha() {return _tintColor[3];}
    public float getTintAlpha() {return _realTintAlpha;}

    public void setTintAlpha(float alpha) {
        _tintColor[3] = _realTintAlpha = alpha;

        updateCascadeAlpha();
    }

    public void updateCascadeAlpha() {
        float parentAlpha = 1.0f;
        if (_parent!=null && _parent.isCascadeColorEnabled()) {
            parentAlpha = _parent.getDisplayedAlpha();
        }
        updateDisplayedAlpha(parentAlpha);
    }

    protected boolean _cascadeAlphaEnabled = false;
    public boolean isCascadeAlphaEnabled() {
        return _cascadeAlphaEnabled;
    }
    public void setCascadeAlphaEnable(boolean enable) {
        if (_cascadeAlphaEnabled==enable) return;

        _cascadeAlphaEnabled = enable;
        if (enable) {
            updateCascadeAlpha();
        } else {
            disableCascadeAlpha();
        }
    }

    public void disableCascadeAlpha() {
        _tintColor[3] = _realAlpha;
        for (SMView child : _children) {
            child.updateDisplayedAlpha(1.0f);
        }
    }

    public void updateDisplayedAlpha(float alpha) {
        if (_tintColor==null) {
            _tintColor = new float[]{1, 1, 1, 1};
        }
        _tintColor[3] = _realTintAlpha * alpha;
        updateTintColor();

        if (_cascadeAlphaEnabled) {
            for (SMView child : _children) {
                child.updateDisplayedAlpha(_tintColor[3]);
            }
        }
    }

    protected void setRenderColor(float a) {
        if (_tintColor==null) {
            _tintColor = new float[]{1, 1, 1, 1};
        }
        getDirector().setColor(a*_tintColor[0], a*_tintColor[1], a*_tintColor[2], a*_tintColor[3]);
    }

//    public void updateTintAlpha(){}

    public Color4F getBackgroundColor() {
        if (_newBgColor == null) {
            _newBgColor = new float[]{1, 1, 1, 1};
        }
        if (_bgColor == null) {
            _bgColor = new float[]{1, 1, 1, 1};
        }
        return new Color4F(_bgColor[0], _bgColor[1], _bgColor[2], _bgColor[3]);
    }

    public void setBackgroundColor(final Color4F color, final float changeDurationTime) {
        Action action = getActionByTag(AppConst.TAG.ACTION_BG_COLOR);
        if (action!=null) {
            stopAction(action);
        }

        if (changeDurationTime>0) {
            action = BGColorTo.create(getDirector(), changeDurationTime, color);
            action.setTag(AppConst.TAG.ACTION_BG_COLOR);
            runAction(action);
        } else {
            setBackgroundColor(color);
        }
    }

    public void setBackgroundColor(final Color4F color) {
        setBackgroundColor(color.r, color.g, color.b, color.a, true);
    }

    public void setBackgroundColor(final Color4F color, boolean immediate) {
        setBackgroundColor(color.r, color.g, color.b, color.a, immediate);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        setBackgroundColor(r, g, b, a, true);
    }


    public void setBackgroundColor(float r, float g, float b, float a, boolean immediate) {
        if (_newBgColor == null) {
            _newBgColor = new float[]{ r, g, b, a };
        } else {
            _newBgColor[0] = r;
            _newBgColor[1] = g;
            _newBgColor[2] = b;
            _newBgColor[3] = a;
        }
        if (_bgColor == null) {
            _bgColor = new float[]{ r, g, b, a };
            _alpha = a;
        } else if (immediate) {
            _bgColor[0] = r;
            _bgColor[1] = g;
            _bgColor[2] = b;
            _bgColor[3] = _alpha = a;
        }
    }

    public void releaseGLResources() {}

    private long _touchMask = 0;
    private long _smoothFlags;

    private Vec2 _lastTouchLocation = new Vec2(0, 0);

    public Vec2 getLastTouchLocation() {return new Vec2(_lastTouchLocation);}

    protected Scheduler _scheduler = null;
    public Scheduler getScheduler() {return _scheduler;}

    protected ActionManager _actionManager = null;
    public ActionManager getActionManager() {return _actionManager;}

    public void setActionManager(ActionManager manager) {
        if (_actionManager!=manager) {
            stopAllActions();
            _actionManager = manager;
        }
    }

    public void stopAllActions() {
        if (_actionManager!=null) {
            _actionManager.removeallActionsFromTarget(this);
        }
    }

    public Action runAction(Action action) {
        assert (action!=null);
        _actionManager.addAction(action, this, !_running);
        return action;
    }

    public void stopAction(Action action) {
        _actionManager.removeAction(action);
    }

    public void stopActionByTag(int tag) {
        assert (tag!=Action.INVALID_TAG);
        _actionManager.removeActionByTag(tag, this);
    }

    public void stopAllActionByTag(int tag) {
        assert (tag!=Action.INVALID_TAG);
        _actionManager.removeAllActionsByTag(tag, this);
    }

    public void stopActionsByFlags(long flags) {
        if (flags>0) {
            _actionManager.removeActionsByFlags(flags, this);
        }
    }

    public Action getActionByTag(int tag) {
        assert (tag!=Action.INVALID_TAG);
        return _actionManager.getActionByTag(tag, this);
    }

    public int getNumberOfRunningActions() {
        return _actionManager.getNumberOfRunningActionsInTarget(this);
    }

    public int getNumberOfRunningActionsByTag(int tag) {
        return _actionManager.getNumberOfRunningActionsInTargetByTag(this, tag);
    }


    private SEL_SCHEDULE _onSmoothUpdateCallback = null;
    private SEL_SCHEDULE _onClickValidateCallback = null;
    private SEL_SCHEDULE _onLongClickValidateCallback = null;



    protected void onSmoothUpdate(final long flags, float dt) {}
    protected void scheduleSmoothUpdate(final long flag) {
        if ((_smoothFlags&flag) > 0) {
            return;
        }

        _smoothFlags |= flag;

        if (_onSmoothUpdateCallback == null) {
            _onSmoothUpdateCallback = new SEL_SCHEDULE() {
                @Override
                public void onFunc(float t) {
                    onInternalSmoothUpate(t);
                }
            };
        }

        if (!isScheduled(_onSmoothUpdateCallback)) {
            schedule(_onSmoothUpdateCallback);
        }
    }

    protected void unscheduleSmoothUpdate(final long flag) {
        if (flag==0) {
            _smoothFlags = 0;
        } else {
            _smoothFlags &= ~flag;
        }

        if (_smoothFlags==0) {
            if (_onSmoothUpdateCallback!=null && isScheduled(_onSmoothUpdateCallback)) {
                unschedule(_onSmoothUpdateCallback);
            }
        }
    }


    public boolean isScheduled(SEL_SCHEDULE selector) {
        return _scheduler.isScheduled(selector, this);
    }

    public void scheduleUpdate() {
        scheduleUpdateWithPriority(0);
    }

    public void scheduleUpdateWithPriority(int priority) {
        _scheduler.scheduleUpdate(this, priority, !_running);
    }

    public void unscheduleUpdate() {
        _scheduler.unscheduledUpdate(this);
    }

    public void schedule(SEL_SCHEDULE selector) {
        schedule(selector, 0.0f, Integer.MAX_VALUE-1, 0.0f);
    }

    public void schedule(SEL_SCHEDULE selector, float interval) {
        schedule(selector, interval, Integer.MAX_VALUE-1, 0.0f);
    }

    public void schedule(SEL_SCHEDULE selector, float interval, long repeat, float delay) {
        assert (selector!=null);
        assert (interval>=0);

        _scheduler.schedule(selector, this, interval, repeat, delay, !_running);
    }

    public void scheduleOnce(SEL_SCHEDULE selector, float delay) {
        schedule(selector, 0.0f, 0, delay);
    }

    // unschedule() 해야함
    public void unschedule(SEL_SCHEDULE selector) {
        if (selector==null) {
            return;
        }

        _scheduler.unschedule(selector, this);
    }

    public void unscheduleAllCallbacks() {
        _scheduler.unscheduleAllForTarget(this);
    }

    protected void setTouchMask(final long mask) {
        _touchMask |= mask;
    }

    protected void clearTouchMask(final long mask) {
        _touchMask &= ~mask;
    }

    protected final boolean isTouchMask(final long mask) {
        return (_touchMask & mask) != 0;
    }

    public boolean isTouchEnable() {
        return _touchMask > 0;
    }

    public static long VIEWFLAG_POSITION = 1;
    public static long VIEWFLAG_SCALE = 1<<1;
    public static long VIEWFLAG_ROTATE = 1<<2;
    public static long VIEWFLAG_CONTENT_SIZE = 1<<3;
    public static long VIEWFLAG_COLOR = 1<<4;

    public static long VIEWFLAG_ANIM_OFFSET = 1<<5;
    public static long VIEWFLAG_ANIM_SCALE = 1<<6;
    public static long VIEWFLAG_ANIM_ROTATE = 1<<7;
    public static long VIEWFLAG_ANIM_CONTENT_SIZE = 1<<8;
    public static long VIEWFLAG_ANIM_COLOR = 1<<9;

    public static long VIEWFLAG_USER_SHIFT = 8;

    public static long TOUCH_MASK_CLICK = 1;
    public static long TOUCH_MASK_DOUBLECLICK = 1<<1;
    public static long TOUCH_MASK_LONGCLICK = 1<<2;
    public static long TOUCH_MASK_TOUCH = 1<<3;

    public static long USER_VIEW_FLAG(int flagId) {
        return (1<<(VIEWFLAG_USER_SHIFT+flagId));
    }

    protected float runStateAnimation() {
        float f = 0;
        if (mStateChangeAni) {
            long time = _director.getTickCount() - mStateChangeTime;
            if (STATE.NORMAL == _pressState) {
                f = (float)time/TIME_PRESSED_TO_NORMAL;
            } else {
                f = (float)time/TIME_NORMAL_TO_PRESSED;
            }
            if (f > 1) {
                f = 1;
                mStateChangeAni = false;
            }
            if (STATE.NORMAL == _pressState) {
                f = 1f-f;
            }
        } else {
            f = STATE.NORMAL == _pressState ? 0 : 1;
        }

        return f;
    }

    public boolean setState(STATE state) {
        if (_pressState != state) {
            if (isEnabled()) {
                _pressState = state;
                mStateChangeAni  = true;
                mStateChangeTime = _director.getTickCount();
            } else {
                _pressState = state;
            }
            return true;
        }

        return false;
    }

    private boolean _scissorEnable = false;

    private boolean _isCalledScissorEnabled = false;
    public void setScissorEnable(boolean enable) {
        _isCalledScissorEnabled = true;
        _scissorEnable = enable;
    }

//    private static final String CAPTURE_SCREEN = "_CAPTURE_SCREEN_";

    public Bitmap captureView() {
        // 현재 뷰 크기로 그린다.
        String captureKeyName = "CAPTURE_VIEW_" + hashCode();
        CanvasSprite canvas = CanvasSprite.createCanvasSprite(getDirector(), (int)getContentSize().width, (int)getContentSize().height, captureKeyName);
        Bitmap bitmap = null;

        float oldScale = getScale();
        Vec2 oldPos = getPosition();
        Vec2 oldAnchor = getAnchorPoint();

        Vec2 drawPos = new Vec2(getContentSize().width/2, getContentSize().height/2);

        if (canvas.setRenderTarget(getDirector(), true)) {

            GLES20.glClearColor(0, 0, 0, 0);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            setPosition(drawPos);
            setAnchorPoint(Vec2.MIDDLE);
            setScale(1.0f);

            getDirector().pushProjectionMatrix();
            {
                transformMatrix(getDirector().getProjectionMatrix());
                getDirector().updateProjectionMatrix();
                visit(1);
            }
            getDirector().popProjectionMatrix();

            bitmap = Bitmap.createBitmap((int) canvas.getWidth(), (int) canvas.getHeight(), Config.ARGB_8888);
            ImageProcessing.glGrabPixels(0, 0, bitmap, true);

            canvas.setRenderTarget(getDirector(), false);
        }
        canvas.removeTexture();

        setScale(oldScale);
        setAnchorPoint(oldAnchor);
        setPosition(oldPos);

        return bitmap;
    }














    // static utility method
    public static float interpolation(float from, float to, float t) {
        return from + (to - from) * t;
    }


    public static InterpolateRet smoothInterpolate(float from, float to, float tolerance) {
        return smoothInterpolate(from, to, tolerance, SMOOTH_DIVIDER);
    }

    public static InterpolateRet smoothInterpolate(float from, float to, float tolerance, float smoothDivider) {
        if (from != to) {
            from = from + (to-from) / smoothDivider;
            if (Math.abs(from-to) < tolerance) {
                from = to;
                return new InterpolateRet(false, from);
            }
            return new InterpolateRet(true, from);
        }
        return new InterpolateRet(false, from);
    }

    public static boolean smoothInterpolateRotate(float from, float to, float tolerance) {
        if (from != to) {
            float diff = getShortestAngle(from, to);
            if (Math.abs(diff) < tolerance) {
                from = to;
                return false; // done
            }
            from += diff / SMOOTH_DIVIDER;
            return true; // still need update
        }
        return false;

    }

    public static final float getShortestAngle(float from, float to) {
        return ((((to - from) % 360) + 540) % 360) - 180;
    }

    public static void sortNodes(ArrayList<SMView> nodes) {
        Collections.sort(nodes, new Comparator<SMView>(){
            @Override
            public int compare(SMView a, SMView b) {
//                return a._localZOrder < b._localZOrder ? -1 : (a._localZOrder > b._localZOrder) ? 1 : 0;
                return a._localZOrderAndArrival < b._localZOrderAndArrival ? -1 : (a._localZOrderAndArrival > b._localZOrderAndArrival) ? 1 : 0;
                //_localZOrderAndArrival
//                return a._localZOrder > b._localZOrder ? -1 : (a._localZOrder < b._localZOrder) ? 1 : 0;
            }
        });
    }

    public static int getRandomColorB() {
        return randomInt(0, 255);
    }

    public static float getRandomColorF() {
        return (float)randomInt(0, 255)/255.0f;
    }

    public static int randomInt(int min, int max) {
        return min + (int)(Math.random() * ((max-min) + 1));
    }


    public static float getDecelateInterpolation(float t) {
        return (float)(1.0 - (1.0 - t) * (1.0 - t));
    }

    public static float getDecelateInterpolation(float t, float fractor) {
        return (float)(1.0 - Math.pow((1.0 - t), 2 * fractor));
    }

    public static Color4F interpolateColor4F(final Color4F from, final Color4F to, float t) {
        float a = interpolation(from.a, to.a, t);
        float r = interpolation(from.r, to.r, t);
        float g = interpolation(from.g, to.g, t);
        float b = interpolation(from.b, to.b, t);

        return new Color4F(r, g, b, a);
    }

    public static Color4B interpolateColor4B(final Color4B from, final Color4B to, float t) {
        int r = (int)interpolation(from.r, to.r, t);
        int g = (int)interpolation(from.g, to.g, t);
        int b = (int)interpolation(from.b, to.b, t);
        int a = (int)interpolation(from.a, to.a, t);

        return new Color4B(r, g, b, a);
    }

    public static Color4F interpolateColor4F(int from, int to, float t) {
        float a = interpolation((from&0xFF000000)>>24, (to&0xFF000000)>>24, t) / 255.0f;
        float r = interpolation((from&0x00FF0000)>>16, (to&0x00FF0000)>>16, t) / 255.0f;
        float g = interpolation((from&0x0000FF00)>>8,  (to&0x0000FF00)>>8,  t) / 255.0f;
        float b = interpolation((from&0x000000FF),     (to&0x000000FF),     t) / 255.0f;

        return new Color4F(r, g, b, a);
    }

    public static Color4F interpolateColor4F(int from, int to, float t, int[] outValue) {
        float a = interpolation((from&0xFF000000)>>24, (to&0xFF000000)>>24, t) / 255.0f;
        float r = interpolation((from&0x00FF0000)>>16, (to&0x00FF0000)>>16, t) / 255.0f;
        float g = interpolation((from&0x0000FF00)>>8,  (to&0x0000FF00)>>8,  t) / 255.0f;
        float b = interpolation((from&0x000000FF),     (to&0x000000FF),     t) / 255.0f;

        outValue[0] =  (((int)(a*0xFF))<<24)|(((int)(r*0xFF))<<16)|(((int)(g*0xFF))<<8)|((int)(b*0xFF));

        return new Color4F(r, g, b, a);
    }

    public static Color4F uint32ToColor4F(int value) {
        float a = ((value&0xFF000000)>>24) / 255.0f;
        float r = ((value&0x00FF0000)>>16) / 255.0f;
        float g = ((value&0x0000FF00)>>8) / 255.0f;
        float b = ((value&0x000000FF)) / 255.0f;

        return new Color4F(r, g, b, a);
    }

    public static double toRadians(double degrees) {
        return ( degrees * M_PI) / 180.0;
    }

    public static double toDegrees(double radians) {
        return ( radians * 180.0 ) / M_PI ;
    }

    public static int round(float value) {
        return (int)(value+0.5);
    }

    public static int signum(float value) {
        return value >= 0 ? 1 : -1;
    }

    public static float shortestAngle(float from, float to) {
        return ((((to-from) % 360.0f)+540) % 360.0f) - 180;
    }

    public enum Direction {
        UP, LEFT, DOWN, RIGHT
    };

    public static Direction getDirection(float dx, float dy) {
        final int VERTICAL_WIDE = 100;
        final int HORIZONTAL_WIDE = (180-VERTICAL_WIDE);

        double radians = Math.atan2(dy, dx);
        int degrees = (int)toDegrees(radians);
        degrees = (degrees % 360) + (degrees < 0 ? 360 : 0); // normalize

        int a = HORIZONTAL_WIDE/2;
        if (degrees > a && degrees < a + VERTICAL_WIDE) {
            return Direction.UP;
        }
        a += VERTICAL_WIDE;

        if (degrees > a && degrees < a + HORIZONTAL_WIDE) {
            return Direction.LEFT;
        }
        a += HORIZONTAL_WIDE;

        if (degrees > a && degrees < a + VERTICAL_WIDE) {
            return Direction.DOWN;
        }

        return Direction.RIGHT;
    }

    public static Color4F MakeColor4F(int rgb, float alpha) {
        float r = ((rgb & 0xFF0000) >> 16)/255.0f;
        float g = ((rgb & 0x00FF00) >> 8)/255.0f;
        float b = (rgb & 0x0000FF)/255.0f;

        return new Color4F(r, g, b, alpha);
    }

    public static Color4B MakeColor4B(int rgba) {
        int r = ((rgba & 0x00FF0000) >> 16);
        int g = ((rgba & 0x0000FF00) >> 8);
        int b = (rgba & 0x000000FF);
        int a = ((rgba & 0xFF000000) >> 24);

        return new Color4B(r, g, b, a);

    }

    public void changeParent(SMView newParent) {
        if (newParent==null || getParent()==newParent) {
            return;
        }

        SMView parent = getParent();

        parent.removeChild(this, false);
        newParent.addChild(this, getLocalZOrder());

    }

    public static final double M_PI = Math.PI;
    public static final double M_PI_2 = Math.PI/2;
    public static final double M_PI_4 = Math.PI/4;
    public static final double M_1_PI = 1/Math.PI;
    public static final double M_2_PI = 2/Math.PI;
    public static final double M_PI_X_2 = M_PI * 2.0f;
}
