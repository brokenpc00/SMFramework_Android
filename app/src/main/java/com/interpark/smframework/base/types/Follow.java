package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class Follow extends Action {
    public Follow(IDirector director) {
        super(director);
    }

    public static Follow create(IDirector director, SMView followView) {
        return Follow.create(director, followView, Rect.ZERO);
    }

    public static Follow create(IDirector director, SMView followView, Rect rect) {
        return createWithOffset(director, followView, 0, 0, rect);
    }

    public static Follow createWithOffset(IDirector director, SMView followedView,float xOffset,float yOffset) {
        return Follow.createWithOffset(director, followedView, xOffset, yOffset, Rect.ZERO);
    }

    public static Follow createWithOffset(IDirector director, SMView followedView,float xOffset,float yOffset, Rect rect) {
        Follow action = new Follow(director);

        if (action!=null && action.initWithTargetAndOffset(followedView, xOffset, yOffset, rect)) {
            return action;
        } else {
            return null;
        }
    }

    @Override
    public Follow clone() {
        return Follow.createWithOffset(_director, _followView, _offsetX, _offsetY, _worldRect);
    }

    @Override
    public Follow reverse() {
        return clone();
    }

    protected boolean initWithTargetAndOffset(SMView followView, float xOffset, float yOffset, Rect rect) {
        _followView = followView;
        _worldRect = rect;
        _boundarySet = !rect.equals(Rect.ZERO);
        _boundaryFullyCovered = false;

        Size winSize = new Size(getDirector().getWidth(), getDirector().getHeight());
        _fullScreenSize.set(new Vec2(winSize.width, winSize.height));
        _halfScreenSize = new Vec2(_fullScreenSize.x/2, _fullScreenSize.y/2);
        _offsetX = xOffset;
        _offsetY = yOffset;
        _halfScreenSize.x += _offsetX;
        _halfScreenSize.y += _offsetY;

        if (_boundarySet) {
            _leftBoundary = -((rect.origin.x+rect.size.width) - _fullScreenSize.x);
            _rightBoundary = -rect.origin.x ;
            _topBoundary = -rect.origin.y;
            _bottomBoundary = -((rect.origin.y+rect.size.height) - _fullScreenSize.y);

            if(_rightBoundary < _leftBoundary)
            {
                _rightBoundary = _leftBoundary = (_leftBoundary + _rightBoundary) / 2;
            }
            if(_topBoundary < _bottomBoundary)
            {
                _topBoundary = _bottomBoundary = (_topBoundary + _bottomBoundary) / 2;
            }

            if( (_topBoundary == _bottomBoundary) && (_leftBoundary == _rightBoundary) )
            {
                _boundaryFullyCovered = true;
            }
        }


        return true;
    }

    protected boolean initWithTarget(SMView followView, Rect rect) {
        return initWithTargetAndOffset(followView, 0, 0, rect);
    }

    @Override
    public void step(float dt) {
        if (_boundarySet) {
            if (_boundaryFullyCovered) {
                return;
            }

            Vec2 tempPos = new Vec2(_halfScreenSize.x-_followView.getX(), _halfScreenSize.y-_followView.getY());
            _target.setPosition(Vec2.clampf(tempPos.x, _leftBoundary, _rightBoundary),
                    Vec2.clampf(tempPos.y, _bottomBoundary, _topBoundary));
        } else {
            _target.setPosition(_halfScreenSize.x-_followView.getX(), _halfScreenSize.y-_followView.getY());
        }
    }

    @Override
    public boolean isDone() {
        return ( !_followView.isRunning());
    }

    @Override
    public void stop() {
        _target = null;
        super.stop();
    }

    protected SMView _followView = null;
    protected boolean _boundarySet = false;
    protected boolean _boundaryFullyCovered = false;
    protected Vec2 _halfScreenSize = null;
    protected Vec2 _fullScreenSize = null;
    protected float _leftBoundary = 0;
    protected float _rightBoundary = 0;
    protected float _topBoundary = 0;
    protected float _bottomBoundary = 0;
    protected float _offsetX = 0;
    protected float _offsetY = 0;
    protected Rect _worldRect = null;
}
