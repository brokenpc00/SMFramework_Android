package com.interpark.smframework.base.scroller;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.types.Dynamics;

public class ScrollController {
    public ScrollController(IDirector director) {
        _director = director;
    }

    private IDirector _director;

    private final float REST_VELOCITY_TOLERANCE = 0.5f;

    private final float REST_POSITION_TOLERANCE = 0.5f;

    private final float PAN_OUTSIDE_SNAP_FACTOR = 0.2f;

    Dynamics _panDynamicsY = new Dynamics();

    private float _panMinY = 0.0f;

    private float _panMaxY = 0.0f;

    private boolean _needUpdate = false;

    private float _panY = 0.0f;

    private float _viewSize = 0.0f;

    private float _scrollSize = 0.0f;

    private float _hangSize = 0.0f;

    private final float SCROLLING_STIFFNESS = 150.0f;

    private final float SCROLLING_DAMPING = 1.0f;

    public void reset() {
        _panDynamicsY.reset();
        _panDynamicsY.setFriction(3.0f);

        // bouncing... 조절...
        _panDynamicsY.setSpring(SCROLLING_STIFFNESS, SCROLLING_DAMPING);

        _panY = 0.0f;
        updateLimits();

        _needUpdate = false;
    }

    public float getPanY() {
        return _panY;
    }

    public void setPanY(final float panY) {
        setPanY(panY, false);
    }

    public void setPanY(final float panY, final boolean force) {
        _panY = panY;

        if (force) {
            _panDynamicsY.setState(_panY, 0, _director.getGlobalTime());
        }
    }

    public void setViewSize(final float viewSize) {
        _viewSize = viewSize;
        updateLimits();
    }

    public void setScrollSize(final float scrollSize) {
        _scrollSize = scrollSize;

        updateLimits();
    }

    public void pan(float dy) {
        if ((getPanY() > _panMaxY && dy > 0) || (getPanY() < _panMinY && dy < 0)) {
            dy *= PAN_OUTSIDE_SNAP_FACTOR;
        }

        float newPanY = getPanY() + dy;

        setPanY(newPanY);
    }

    public boolean update() {
        if (_needUpdate) {
            float nowTime = _director.getGlobalTime();
            _panDynamicsY.update(nowTime);

            boolean isAtRest = _panDynamicsY.isAtRest(REST_VELOCITY_TOLERANCE, REST_POSITION_TOLERANCE);

            setPanY(_panDynamicsY.getPosition());

            if (isAtRest) {
                stopFling();
            }
        }

        return _needUpdate;
    }

    public void startFling(float vy) {
        float now = _director.getGlobalTime();

        if ((vy < 0 && _panY < _panMinY) || (vy > 0 && _panY > _panMaxY)) {
            vy /= 5;
        }

        _panDynamicsY.setState(getPanY(), vy, now);

        _panDynamicsY.setMinPosition(_panMinY-_hangSize);
        _panDynamicsY.setMaxPosition(_panMaxY);

        _needUpdate = true;
    }

    public void stopFling() {
        _needUpdate = false;
    }

    private void updatePanLimits() {
        _panMinY = 0;
        _panMaxY = Math.max(0.0f, _scrollSize - _viewSize);
    }

    public void updateLimits() {
        updatePanLimits();
    }

    public boolean isPanning() {
        return _needUpdate;
    }

    public void stopIfExceedLimit() {
        _panDynamicsY.setMinPosition(_panMinY-_hangSize);
        _panDynamicsY.setMaxPosition(_panMaxY);

        if ((getPanY() > _panMaxY) || (getPanY() < _panMinY)) {
            startFling(0);
        }
    }

    public void setHangSize(float size) {
        _hangSize = size;
        _panDynamicsY.setMinPosition(_panMinY-_hangSize);
    }
}
