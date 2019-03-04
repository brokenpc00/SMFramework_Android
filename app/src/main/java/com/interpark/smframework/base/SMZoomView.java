package com.interpark.smframework.base;

import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.ActionInterval;
import com.interpark.smframework.base.types.EaseSineInOut;
import com.interpark.smframework.base.scroller.ZoomController;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMZoomView extends _UIContainerView {
    public static SMZoomView create(IDirector director) {
        return create(director, 0, 0, 0, 0);
    }

    public static SMZoomView create(IDirector director, float x, float y, float width, float height) {
        return create(director, x, y, width, height, 0, 0);
    }

    public static SMZoomView create(IDirector director, float x, float y, float width, float height, float anchorX, float anchorY) {

        SMZoomView zoomView = new SMZoomView(director);

        if (zoomView!=null) {
            zoomView.setPosition(x, y);
            zoomView.setContentSize(new Size(width, height));
            zoomView.setAnchorPoint(anchorX, anchorY);
            if (zoomView.init()) {
                return zoomView;
            }
        }

        return null;
    }

    public SMZoomView(IDirector director) {
        super(director);
    }

    protected boolean init() {
        if (super.init()) {
            _uiContainer.setAnchorPoint(0, 0);
            _uiContainer.setPosition(0, 0);

            _controller = new ZoomController(getDirector());
//            setTouchMask(TOUCH_MASK_DOUBLECLICK);
            setDoubleClickable(true);
            return true;
        }

        return false;
    }

    public enum FillType {
        INSIDE,
        FILL,
    }

    protected enum Mode {
        UNDEFINED,
        PAN,
        ZOOM,
    }

    public static long FLAG_ZOOM_UPDATE = 1<<0;

    private Mode _mode = Mode.UNDEFINED;

    private SMView _contentView = null;

    private ZoomController _controller = null;

    private VelocityTracker _velocityTracker = null;

    private float _panX = 0.0f;

    private float _panY = 0.0f;

    private float _zoom = 1.0f;

    private float _baseZoom = 1.0f;

    private float _prevZoom = 1.0f;

    private float _prevDistance = 0.0f;

    private float _prevTouchX = 0.0f;

    private float _prevTouchY = 0.0f;

    private float _initTouchX = 0.0f;

    private float _initTouchY = 0.0f;

    private boolean _panEnable = true;

    private boolean _zoomEnable = true;

    private boolean _interpolate = true;

    private float _accuX = 0.0f;

    private float _accuY = 0.0f;

    private FillType _fillType = FillType.INSIDE;

    private Size _innerSize = new Size(0, 0);

    private class ZoomTo extends ActionInterval {
        public ZoomTo(IDirector director, float zoom, Vec2 pan, float duration) {
            super(director);
            initWithDuration(duration);
            _toZoom = zoom;
            _toPan = new Vec2(pan.x, pan.y);
        }

        @Override
        public void startWithTarget(SMView target) {
            super.startWithTarget(target);

            SMZoomView zoomView = (SMZoomView)_target;
            ZoomController controller = zoomView.getController();
            _fromPan = new Vec2(controller.getPanX(), controller.getPanY());
            _fromZoom = controller.getZoom();
        }

        @Override
        public void update(float t) {
            SMZoomView zoomView = (SMZoomView)_target;
            ZoomController controller = zoomView.getController();
            float zoom = interpolation(_fromZoom, _toZoom, t);
            float panX = interpolation(_fromPan.x, _toPan.x, t);
            float panY = interpolation(_fromPan.y, _toPan.y, t);

            controller.zoomImmediate(zoom, panX, panY);
            zoomView.registerUpdate(FLAG_ZOOM_UPDATE);
        }

        private Vec2 _fromPan = new Vec2(0, 0);
        private Vec2 _toPan = new Vec2(0, 0);
        private float _fromZoom = 1.0f;
        private float _toZoom = 1.0f;
    }

    public void setFillType(final FillType type)
    {
        _fillType = type;
    }

    public void setPanEnable(boolean enable)
    {
        _panEnable = enable;
    }

    public void setZoomEnable(boolean enable)
    {
        _zoomEnable = enable;
    }

    public boolean isIdle() {
        return _mode==Mode.UNDEFINED;
    }

    public boolean isPanning() {
        return _controller.isPanning();
    }

    public SMView getContentNode() {
        return _contentView;
    }

    public void setPadding(final float padding)
    {
        assert(_contentView==null);

        super.setPadding(padding);
    }

    public void setPadding(final float left, final float top, final float right, final float bottom)
    {
        assert(_contentView==null);

        super.setPadding(left, top, right, bottom);
    }

    public void setContentSize(final Size size)
    {
        _innerSize.set(size.width-_paddingLeft-_paddingRight, size.height-_paddingTop-_paddingBottom);
        super.setContentSize(size);

        registerUpdate(FLAG_ZOOM_UPDATE);
    }

    public void refreshContentNode(final boolean reset) {
        if (_contentView!=null) {
            _uiContainer.setContentSize(_contentView.getContentSize());

            if (reset) {
                _controller.reset();

                _uiContainer.setAnchorPoint(0.5f, 0.5f);
                _uiContainer.setPosition(_paddingLeft+_innerSize.width/2, _paddingBottom+_innerSize.height/2);
                _uiContainer.setScale(_controller.getZoom()*_baseZoom);
            }

            _controller.updateAspect(_innerSize, _contentView.getContentSize(), _fillType==FillType.FILL);
            _controller.updateLimits();
            _baseZoom = computeBaseZoom(_innerSize, _contentView.getContentSize());
        }
    }

    public void setContentView(SMView contentView) {
        if (_contentView==contentView) {
            // already setting same node...
            return;
        }

        if (_contentView!=null) {
            _uiContainer.removeChild(_contentView);
            _controller.reset();
        }

        if (contentView!=null) {
            Size size = new Size(contentView.getContentSize());


            _uiContainer.addChild(contentView);

            _uiContainer.setContentSize(size);



            _controller.reset();
            _controller.updateAspect(_innerSize, size, _fillType==FillType.FILL);
            _controller.updateLimits();
            _baseZoom = computeBaseZoom(_innerSize, size);

            _uiContainer.setAnchorPoint(0.5f, 0.5f);
            _uiContainer.setPosition(getContentSize().width/2, getContentSize().height/2);

            float scale = _controller.getZoom()*_baseZoom;
            _uiContainer.setScale(scale);
            contentView.setIgnoreTouchBounds(true);

            _interpolate = true;
        }

        _contentView = contentView;
    }

    protected ZoomController getController() {
        return _controller;
    }

    private float computeBaseZoom(Size viewSize, Size contentSize) {
        float zoomX = viewSize.width/contentSize.width;
        float zoomY = viewSize.height/contentSize.height;

        if (_fillType==FillType.INSIDE) {
            // 안에 맞게 하려면 작은 값을 기준으로
            return Math.min(zoomX, zoomY);
        } else {
            // 채우려면 큰 값을 기준으로
            return Math.max(zoomX, zoomY);
        }
    }

    public float getContentZoomScale() {
        return _zoom;
    }

    public float getContentBaseScale() {
        return _baseZoom;
    }

    public Vec2 getContentPosition() {
        return new Vec2(_uiContainer.getPosition());
    }

    public float getZoom() {
        return _zoom;
    }

    public float getPanX() {
        return _panX;
    }

    public float getPanY() {
        return _panY;
    }

    @Override
    public boolean containsPoint(final Vec2 point) {
        if (_ignoreTouchBounds) {
            return true;
        }

        return super.containsPoint(point);
    }

    @Override
    public void onUpdateOnVisit() {
        if (!_controller.update()) {
            unregisterUpdate(FLAG_ZOOM_UPDATE);
        }

        _panX = _controller.getPanX();
        _panY = _controller.getPanY();
        _zoom = _controller.getZoom();

        float scale = _baseZoom*_zoom;

        float x = scale * (-(_panX-0.5f)) * _uiContainer.getContentSize().width;
        float y = scale * (-(_panY-0.5f)) * _uiContainer.getContentSize().height;

        _uiContainer.setPosition(_paddingLeft + _innerSize.width/2 + x, _paddingBottom + _innerSize.height/2 + y, _interpolate);
        _uiContainer.setScale(scale, _interpolate);

        _interpolate = false;
    }

    @Override
    public int dispatchTouchEvent(MotionEvent event, SMView view, boolean checkBounds) {
        return super.dispatchTouchEvent(event, view, false);
    }

    @Override
    public int dispatchTouchEvent(MotionEvent ev) {

        if (_mode==Mode.UNDEFINED) {
            int ret = super.dispatchTouchEvent(ev);
            if (ret==TOUCH_INTERCEPT) {
                return ret;
            }
        }

//        if (_contentView!=null) {
//
//        }

        Vec2 gv = new Vec2(ev.getX(), ev.getY());
        Vec2 mm = new Vec2(_paddingLeft, _paddingBottom);

        Vec2 point = new Vec2(gv.x-mm.x, gv.y-mm.y);

        float x = point.x;
        float y = point.y;

        if (_velocityTracker==null) {
            _velocityTracker = VelocityTracker.obtain();
        }

        _velocityTracker.addMovement(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            {
                if (_panEnable) {
                    // scrolling을 멈추고
                    _controller.stopFling();
                }

                _initTouchX = x;
                _initTouchY = y;
                _prevTouchX = x;
                _prevTouchY = y;

                _accuX = _accuY = 0;

//                _velocityTracker.addMovement(ev);
            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                if (_zoomEnable) {
                    Vec2 point2 = new Vec2(ev.getX(1), ev.getY(1));
                    point2.set(point2.x-_paddingLeft, point2.y-_paddingBottom);

                    float distance = spacing(ev);

                    if (distance>0) {
                        _prevDistance = distance;
                        if (_prevDistance>10.0f) {
                            _mode = Mode.ZOOM;

                            Vec2 midPoint = new Vec2((point.x+point2.x)/2, (point.y+point2.y)/2);
                            _initTouchX = _prevTouchX = midPoint.x;
                            _initTouchY = _prevTouchY = midPoint.y;

                            _prevZoom =_controller.getZoom();
                        }
                    }
                }

//                _velocityTracker.addMovement(ev);
            }
            break;
            case MotionEvent.ACTION_MOVE:
            {

                if (_mode==Mode.ZOOM) {
                    Vec2 point2 = new Vec2(ev.getX(1), ev.getY(1));
                    point2.set(point2.x-_paddingLeft, point2.y-_paddingBottom);

                    Vec2 midPoint = new Vec2((point.x+point2.x)/2, (point.y+point2.y)/2);

                    x = midPoint.x;
                    y = midPoint.y;

                    float dx = (x-_prevTouchX)/_innerSize.width;
                    float dy = (y-_prevTouchY)/_innerSize.height;

                    _controller.pan(-dx, -dy);

                    float distance = spacing(ev);

                    float scale = (distance/_prevDistance)*_prevZoom;

                    _controller.zoom(scale, x/_innerSize.width, y/_innerSize.height);
                    registerUpdate(FLAG_ZOOM_UPDATE);

                    _accuX += x - _prevTouchX;
                    _accuY += y - _prevTouchY;

//                    _velocityTracker.addMovement(ev);

                    _prevTouchX = x;
                    _prevTouchY = y;
                } else if (_mode==Mode.PAN) {
                    float dx = (x-_prevTouchX)/_innerSize.width;
                    float dy = (y-_prevTouchY)/_innerSize.height;

                    _controller.pan(-dx, -dy);
                    registerUpdate(FLAG_ZOOM_UPDATE);

                    _accuX += x - _prevTouchX;
                    _accuY += y - _prevTouchY;
//                    _velocityTracker.addMovement(ev);

                    _prevTouchX = x;
                    _prevTouchY = y;
                } else {
                    if (_panEnable) {
                        float scrollX = _initTouchX - x;
                        float scrollY = _initTouchY - y;
                        float distance = (float)Math.sqrt(scrollX*scrollX+scrollY*scrollY);

                        if (distance>AppConst.Config.SCALED_TOUCH_SLOPE) {
                            _mode = Mode.PAN;
                            registerUpdate(FLAG_ZOOM_UPDATE);
                        }
                    }

                    _accuX += x - _prevTouchX;
                    _accuY += y - _prevTouchY;
//                    _velocityTracker.addMovement(ev);

                    _prevTouchX = x;
                    _prevTouchY = y;
                }
            }
            break;
            case MotionEvent.ACTION_POINTER_UP:
            {
                // multi touch는 zoom일 경우만 반응
                if (_mode==Mode.ZOOM) {
                    int index = ev.getActionIndex();
                    if (index==0 || index==1) {
                        _mode = Mode.PAN;

                        Vec2 pt;
                        if (index==1) {
                            // 두번째가 떨어짐
                            pt = new Vec2(ev.getX(0)-_paddingLeft, ev.getY(0)-_paddingBottom);

                        } else {
                            // 첫번째가 떨어짐
                            pt = new Vec2(ev.getX(1)-_paddingLeft, ev.getY(1)-_paddingBottom);
                        }
                        _initTouchX = _prevTouchX = pt.x;
                        _initTouchY = _prevTouchY = pt.y;
                    }
                }
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                if (_mode==Mode.PAN) {
                    float vx, vy;
                    _velocityTracker.computeCurrentVelocity((int)AppConst.Config.MIN_VELOCITY, AppConst.Config.MAX_VELOCITY);
                    vx = _velocityTracker.getXVelocity(0);
                    vy = _velocityTracker.getYVelocity(0);
                    _controller.startFling(-vx/_innerSize.width, -vy/_innerSize.height);
                } else {
                    if (_panEnable) {
                        _controller.updateLimits();
                        _controller.startFling(0, 0);
                    }
                }

                _velocityTracker.recycle();
                _velocityTracker = null;

                _mode = Mode.UNDEFINED;
                registerUpdate(FLAG_ZOOM_UPDATE);
            }
            break;
        }

        return TOUCH_TRUE;
    }

    public void updateZoom() {
        registerUpdate(FLAG_ZOOM_UPDATE);
    }

    @Override
    protected void performDoubleClick(Vec2 worldPoint) {
        if (!_zoomEnable || _contentView==null) {
            return;
        }

        Action action = getActionByTag(AppConst.TAG.ACTION_ZOOM);
        if (action!=null) {
            stopAction(action);
        }

        float zoom = _controller.getZoom();
        Vec2 point = new Vec2(worldPoint.x - _paddingLeft, worldPoint.y - _paddingBottom);

        Vec2 pivot = new Vec2(point.x/_innerSize.width, point.y/_innerSize.height);

        float newZoom = 1.0f;
        if (Math.abs(zoom-1)<=0.5f) {
            // 처음 따닥...두배
            newZoom = 2.0f;
        } else if (Math.abs(zoom-2)<=0.5f) {
            // 두번째 따닥...네배
            newZoom = 4.0f;
        } else {
            // 세번째 따닥...처음으로
            newZoom = 1.0f;
        }

        Vec2 newPan = new Vec2(_controller.computePanPosition(newZoom, pivot));

        // erase in out action 할 차례임..

        EaseSineInOut zoomTo = EaseSineInOut.create(getDirector(), new ZoomTo(getDirector(), newZoom, newPan, AppConst.Config.ZOOM_NORMAL_TIME));
        zoomTo.setTag(AppConst.TAG.ACTION_ZOOM);
        runAction(zoomTo);
    }

    public void setFocusRect(Rect focusRect, float duration)
    {
        if (focusRect.size.width<=0 || focusRect.size.height<=0) {
            return;
        }

        Action action = getActionByTag(AppConst.TAG.ACTION_ZOOM);
        if (action!=null) {
            stopAction(action);
        }

        // 기본 zoom을 1로 놓고 계산
        Size size = _contentView.getContentSize();
        float aspectView = _contentSize.width/_contentSize.height;
        float aspectCont = size.width/size.height;

        float width, height;

        if (aspectCont>aspectView) {
            // 좌우가  걸림
            width = size.width;
            height = size.width / aspectView;
        } else {
            // 위아래가 걸림
            height = size.height;
            width = size.height * aspectView;
        }

        float newZoom = Math.min(width/(focusRect.size.width), height/(focusRect.size.height));
        float newPanX = focusRect.getMidX() / size.width;
        float newPanY = focusRect.getMidY() / size.height;

        EaseSineInOut zoomTo = EaseSineInOut.create(getDirector(), new ZoomTo(getDirector(), newZoom, new Vec2(newPanX, newPanY), duration));
        zoomTo.setTag(AppConst.TAG.ACTION_ZOOM);

        runAction(zoomTo);
    }

    public void setZoomWithAnimation(float panX, float panY, float zoom, float duration)
    {
        Action action = getActionByTag(AppConst.TAG.ACTION_ZOOM);
        if (action!=null) {
            stopAction(action);
        }

        EaseSineInOut zoomTo = EaseSineInOut.create(getDirector(), new ZoomTo(getDirector(), zoom, new Vec2(panX, panY), duration));
        zoomTo.setTag(AppConst.TAG.ACTION_ZOOM);

        runAction(zoomTo);
    }


    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float)Math.sqrt(x * x + y * y);
        } catch(Exception e) {
            // Does nothing
        }
        return -1;
    }

    private void midPoint(Vec2 point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public Rect getReverseFocusRect(final Rect focusRect) {
        // To. Do.... realization
        return new Rect(0, 0, 0, 0);
    }



}
