package com.interpark.smframework.base.scroller;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Dynamics;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class ZoomController {
    public ZoomController(IDirector director) {
        _director = director;
        reset();
    }

    private final float MIN_ZOOM = 1.0f;

    private final float MAX_ZOOM = 20.0f;

    private final float REST_VELOCITY_TOLERANCE = 0.1f;

    private final float REST_POSITION_TOLERANCE = 0.1f;

    private final float REST_ZOOM_TOLERANCE = 0.005f;

    private final float PAN_OUTSIDE_SNAP_FACTOR = 0.4f;

    private final float ZOOM_OUTSIDE_SNAP_FACTOR = 0.023f;

    private Dynamics _panDynamicsX = new Dynamics();

    private Dynamics _panDynamicsY = new Dynamics();

    private Dynamics _zoomDynamics = new Dynamics();

    private float _panMinX = 0.0f;

    private float _panMaxX = 0.0f;

    private float _panMinY = 0.0f;

    private float _panMaxY = 0.0f;

    private boolean _needUpdate = false;

    private float _aspect = 1.0f;

    private float _zoom = 1.0f;

    private float _panX = 0.0f;

    private float _panY = 0.0f;

    private Boolean _fillMode = false;

    private Size _viewSize = new Size(0, 0);

    public void reset() {
        _panDynamicsX.reset();
        _panDynamicsY.reset();
        _panDynamicsX.setFriction(2.0f);
        _panDynamicsY.setFriction(2.0f);
        _panDynamicsX.setSpring(500.0f, 0.9f);
        _panDynamicsY.setSpring(500.0f, 0.9f);

        _zoomDynamics.reset();
        _zoomDynamics.setFriction(5.0f);
        _zoomDynamics.setSpring(800.0f, 1.3f);

        _zoomDynamics.setMinPosition(MIN_ZOOM);
        _zoomDynamics.setMaxPosition(MAX_ZOOM);
        _zoomDynamics.setState(1.0f, 0.0f, 0.0f);

        _zoom = 1.0f;
        _panX = 0.5f;
        _panY = 0.5f;
        updateLimits();

        _needUpdate = false;
    }

    public float getPanX() {
        return _panX;
    }

    public float getPanY() {
        return _panY;
    }

    public float getZoom() {
        return _zoom;
    }

    public float getZoomX() {
        if (_fillMode) {
            return Math.max(_zoom, _zoom * _aspect);
        } else {
            return Math.min(_zoom, _zoom * _aspect);
        }
    }

    public float getZoomY() {
        if (_fillMode) {
            return Math.max(_zoom, _zoom / _aspect);
        } else {
            return Math.min(_zoom, _zoom / _aspect);
        }
    }

    public void setPanX(final float panX) {
        _panX = panX;
    }

    public void setPanY(final float panY) {
        _panY = panY;
    }

    public void setZoom(final float zoom) {
        _zoom = zoom;
    }

    public void setViewSize(final Size viewSize) {
        _viewSize = new Size(viewSize.width, viewSize.height);
    }

    public void updateAspect(final Size viewSize, final Size contentSize, final boolean fillMode) {
        _aspect = (contentSize.width / contentSize.height) / (viewSize.width / viewSize.height);
        setViewSize(viewSize);

        _fillMode = fillMode;
    }

    public void zoom(final float zoom, final float panX, final float panY) {
        float prevZoomX = getZoomX();
        float prevZoomY = getZoomY();

        float deltaZoom = zoom - getZoom();

        if ((zoom > MAX_ZOOM && deltaZoom > 0) || (zoom < MIN_ZOOM && deltaZoom < 0)) {
            deltaZoom *= ZOOM_OUTSIDE_SNAP_FACTOR;
        }

        setZoom(zoom);
        limitZoom();

        _zoomDynamics.setState(getZoom(), 0, _director.getGlobalTime());

        float newZoomX = getZoomX();
        float newZoomY = getZoomY();

        setPanX(getPanX() + (panX - 0.5f) * (1.0f / prevZoomX - 1.0f / newZoomX));
        setPanY(getPanY() + (panY - 0.5f) * (1.0f / prevZoomY - 1.0f / newZoomY));

        updatePanLimits();
    }

    public void zoomImmediate(final float zoom, final float panX, final float panY) {
        setZoom(zoom);
        setPanX(panX);
        setPanY(panY);

        _zoomDynamics.setState(zoom, 0, _director.getGlobalTime());
        _panDynamicsY.setState(panY, 0, _director.getGlobalTime());
        _panDynamicsX.setState(panX, 0, _director.getGlobalTime());

        updatePanLimits();
    }

    public void pan(float dx, float dy) {

        dx /= getZoomX();
        dy /= getZoomY();

        if ((getPanX() > _panMaxX && dx > 0) || (getPanX() < _panMinX && dx < 0)) {
            dx *= PAN_OUTSIDE_SNAP_FACTOR;
        }
        if ((getPanY() > _panMaxY && dy > 0) || (getPanY() < _panMinY && dy < 0)) {
            dy *= PAN_OUTSIDE_SNAP_FACTOR;
        }

        float newPanX = getPanX() + dx;
        float newPanY = getPanY() + dy;

        setPanX(newPanX);
        setPanY(newPanY);
    }

    public boolean update() {
        if (_needUpdate) {
            float nowTime = _director.getGlobalTime();
            _panDynamicsX.update(nowTime);
            _panDynamicsY.update(nowTime);
            _zoomDynamics.update(nowTime);

            boolean isAtRest = _panDynamicsX.isAtRest(REST_VELOCITY_TOLERANCE, REST_POSITION_TOLERANCE, _viewSize.width) &&
                            _panDynamicsY.isAtRest(REST_VELOCITY_TOLERANCE, REST_POSITION_TOLERANCE, _viewSize.height) &&
                                    _zoomDynamics.isAtRest(REST_VELOCITY_TOLERANCE, REST_ZOOM_TOLERANCE, 1);

            setPanX(_panDynamicsX.getPosition());
            setPanY(_panDynamicsY.getPosition());
            setZoom(_zoomDynamics.getPosition());

            if (isAtRest) {
                if (Math.abs(MIN_ZOOM - getZoom()) < REST_ZOOM_TOLERANCE) {
                    setZoom(MIN_ZOOM);
                    _zoomDynamics.setState(MIN_ZOOM, 0, 0);
                }
                stopFling();
            }
            updatePanLimits();
        }

        return _needUpdate;
    }

    public void startFling(float vx, float vy) {
        float now = _director.getGlobalTime();

        _panDynamicsX.setState(getPanX(), vx / getZoomX(), now);
        _panDynamicsY.setState(getPanY(), vy / getZoomY(), now);

        _panDynamicsX.setMinPosition(_panMinX);
        _panDynamicsX.setMaxPosition(_panMaxX);
        _panDynamicsY.setMinPosition(_panMinY);
        _panDynamicsY.setMaxPosition(_panMaxY);

        _needUpdate = true;
    }

    public void stopFling() {
        _needUpdate = false;
    }

    public float getMaxPanDelta(float zoom) {
        return Math.max(0.0f, .5f * ((zoom - 1) / zoom));
    }

    private void limitZoom() {
        if (getZoom() < MIN_ZOOM-0.3f) {
            setZoom(MIN_ZOOM-0.3f);
        } else if (getZoom() > MAX_ZOOM) {
            setZoom(MAX_ZOOM);
        }
    }

    private void updatePanLimits() {
        float zoomX = getZoomX();
        float zoomY = getZoomY();

        _panMinX = .5f - getMaxPanDelta(zoomX);
        _panMaxX = .5f + getMaxPanDelta(zoomX);
        _panMinY = .5f - getMaxPanDelta(zoomY);
        _panMaxY = .5f + getMaxPanDelta(zoomY);
    }

    public Vec2 computePanPosition(final float zoom, final Vec2 pivot) {

        float zoomX = Math.min(zoom, getZoomX());
        float zoomY = Math.min(zoom, getZoomY());

        float panMinX = .5f - getMaxPanDelta(zoomX);
        float panMaxX = .5f + getMaxPanDelta(zoomX);
        float panMinY = .5f - getMaxPanDelta(zoomY);
        float panMaxY = .5f + getMaxPanDelta(zoomY);

        float x = Math.max(.0f, Math.min(1.f, pivot.x));
        float y = Math.max(.0f, Math.min(1.f, pivot.y));

        Vec2 pan = new Vec2(0, 0);

        pan.x = _panX + (x - 0.5f) * (1.0f / getZoomX() - 1.0f / zoomX);
        pan.y = _panY + (y - 0.5f) * (1.0f / getZoomY() - 1.0f / zoomY);

        pan.x = Math.min(panMaxX, Math.max(panMinX, pan.x));
        pan.y = Math.min(panMaxY, Math.max(panMinY, pan.y));

        return pan;
    }

    public void updateLimits() {
        limitZoom();
        updatePanLimits();
    }

    public boolean isPanning() {
        return _needUpdate;
    }

    private IDirector _director;
}
