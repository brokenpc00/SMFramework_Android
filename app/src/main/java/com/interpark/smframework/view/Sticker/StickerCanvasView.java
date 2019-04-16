package com.interpark.smframework.view.Sticker;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.CallFunc;
import com.interpark.smframework.base.types.CallFuncN;
import com.interpark.smframework.base.types.DelayTime;
import com.interpark.smframework.base.types.EaseBackIn;
import com.interpark.smframework.base.types.EaseInOut;
import com.interpark.smframework.base.types.EaseOut;
import com.interpark.smframework.base.types.FadeOut;
import com.interpark.smframework.base.types.FadeTo;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.GenieAction;
import com.interpark.smframework.base.types.MoveBy;
import com.interpark.smframework.base.types.PERFORM_SEL_N;
import com.interpark.smframework.base.types.Ref;
import com.interpark.smframework.base.types.RotateBy;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.base.types.Spawn;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import cz.msebera.android.httpclient.cookie.SM;

public class StickerCanvasView extends SMView implements MultiTouchController.MultiTouchObjectCanvas<SMView> {

    private static float MIN_FLY_TOLERANC = 6000;
    private static float FLY_DURATION = 0.3f;

    public StickerCanvasView(IDirector director) {
        super(director);
    }

    public static StickerCanvasView create(IDirector director) {
        StickerCanvasView view = new StickerCanvasView(director);
        view.init();
        return view;
    }

    @Override
    protected boolean init() {
        if (!super.init()) {
            return false;
        }

        _controller = new MultiTouchController<>(this);
        _velocityTracker = VelocityTracker.obtain();

        return true;
    }

    public void setRemoveAfterFlying(boolean bEnable) {
        _flyRemovable = bEnable;
    }

    public void setChildPosition(SMView view, final int position) {
        assert (view!=null);

        ArrayList<SMView> children = getChildren();

        assert (children.contains(view));

        int zorder = 0;

        ListIterator<SMView> iter = children.listIterator();

        while (iter.hasNext()) {
            SMView child = iter.next();
            if (child==_bgView) {
                continue;
            }

            if (child==view) {
                view.setLocalZOrder(position);
                continue;
            }

            if (zorder==-position) {
                zorder--;
            }

            child.setLocalZOrder(zorder--);
        }

        sortAllChildren();
    }

    public void bringChildToTop(SMView view) {
        assert view!=null;

        ArrayList<SMView> children = getChildren();

        assert children.contains(view);

        ListIterator<SMView> iter = children.listIterator();

        int zorder = 0;

        while (iter.hasNext()) {
            SMView child = iter.next();

            if (child==_bgView) {
                continue;
            }

            if (child==view) {
                continue;
            }

            child.setLocalZOrder(zorder--);
        }

        view.setLocalZOrder(0);
        sortAllChildren();
    }

    public void sendChildToBack(SMView view) {
        assert view!=null;

        ArrayList<SMView> children = getChildren();

        assert children.contains(view);

        int zorder = 0;

        ListIterator<SMView> iter = children.listIterator();

        while (iter.hasNext()) {
            SMView child = iter.next();

            if (child==_bgView) {
                continue;
            }

            if (child==view) {
                continue;
            }

            child.setLocalZOrder(zorder--);
        }

        view.setLocalZOrder(zorder);
        sortAllChildren();
    }

    public void aboveView(SMView view, SMView aboveView) {
        assert view!=null;

        if (aboveView==null) {
            bringChildToTop(view);
            return;
        }

        ArrayList<SMView> children = getChildren();

        assert children.contains(view);

        if (!children.contains(aboveView) || view==aboveView) {
            return;
        }

        int zorder = 0;
        int target = 0;

        ListIterator<SMView> iter = children.listIterator();

        while (iter.hasNext()) {
            SMView child = iter.next();

            if (child==_bgView) {
                continue;
            }

            if (child==view) {
                continue;
            }

            if (child==aboveView) {
                target = zorder;
                zorder--;
            }

            child.setLocalZOrder(zorder--);
        }

        view.setLocalZOrder(target);
        sortAllChildren();
    }

    public void belowView(SMView view, SMView belowView) {
        assert view!=null;

        if (belowView==null) {
            sendChildToBack(view);
            return;
        }

        ArrayList<SMView> children = getChildren();

        assert children.contains(view);

        if (!children.contains(belowView) || view==belowView) {
            return;
        }

        int zorder = 0;
        int target = 0;

        ListIterator<SMView> iter = children.listIterator();

        while (iter.hasNext()) {
            SMView child = iter.next();

            if (child==_bgView) {
                continue;
            }

            if (child==view) {
                continue;
            }

            child.setLocalZOrder(zorder--);

            if (child==belowView) {
                target = zorder;
                zorder--;
            }
        }

        view.setLocalZOrder(target);
        sortAllChildren();
    }

    @Override
    public void addChild(SMView view) {
        addChild(view, 0);
    }
    @Override
    public void addChild(SMView view, int localZOrder) {
        addChild(view, localZOrder, "");
    }
    @Override
    public void addChild(SMView child, int localZOrder, final String name) {
        super.addChild(child, localZOrder, name);
        setSelectedSticker(null);
    }

    @Override
    public void removeChild(SMView child) {
        removeChild(child, true);
    }
    @Override
    public void removeChild(SMView child, boolean cleanup) {
        if (child!=null && child==_selectedView) {
            performSelected(_selectedView, false);
            _selectedView = null;
        }

        super.removeChild(child, cleanup);
    }

    public boolean setSelectedSticker(SMView view) {
        if (view==null) {
            if (_selectedView!=null) {
                performSelected(_selectedView, false);
                _selectedView = null;
            }
            return true;
        } else if (view!=_selectedView) {
            ArrayList<SMView> children = getChildren();
            if (children.contains(view)) {
                if (_selectedView!=null) {
                    performSelected(_selectedView, false);
                }
                _selectedView = view;
                performSelected(view, true);
                return true;
            }
        }

        return false;
    }

    public SMView getSelectedSticker() {
        return _selectedView;
    }

    @Override
    public int dispatchTouchEvent(MotionEvent event) {
        int ret = super.dispatchTouchEvent(event);
        int action = event.getAction();
        int mode = _controller.getMode();

        if (action==MotionEvent.ACTION_UP) {
            if (_listener!=null) {
                _listener.onStickerTouch(_selectedView, MotionEvent.ACTION_UP);
            }
        }

        if (_controller.onTouchEvent(event)) {
            if (mode==MultiTouchController.MODE_NOTHING && action==MotionEvent.ACTION_DOWN) {
                _listener.onStickerTouch(_selectedView, MotionEvent.ACTION_DOWN);
            }
            _velocityTracker.addMovement(event);

            if (mode==MultiTouchController.MODE_DRAG && action==MotionEvent.ACTION_MOVE) {
                if (!_trackFlyEvent) {
                    if (_selectedView.getClass()==RemovableSticker.class) {
                        RemovableSticker removable = (RemovableSticker)_selectedView;
                        if (removable==null || removable.isRemovable()) {
                            MultiTouchController.PointInfo point = _controller.getCurrentPoint();
                            float dist = point.getDistance(_lastTouchPoint);
                            if (dist>AppConst.Config.SCROLL_TOLERANCE) {
                                _trackFlyEvent = true;
                            }
                        }
                    } else {
                        MultiTouchController.PointInfo point = _controller.getCurrentPoint();
                        float dist = point.getDistance(_lastTouchPoint);
                        if (dist>AppConst.Config.SCROLL_TOLERANCE) {
                            _trackFlyEvent = true;
                        }
                    }
                }
            }

            return TOUCH_INTERCEPT;
        } else if (mode!=MultiTouchController.MODE_NOTHING && action==MotionEvent.ACTION_UP) {
            if (mode==MultiTouchController.MODE_DRAG) {
                if (_trackFlyEvent) {
                    _trackFlyEvent = false;

                    float vx = _velocityTracker.getXVelocity(0);
                    float vy = _velocityTracker.getYVelocity(0);

                    double radians = Math.atan2(vy, vx);
                    float degrees = (float)SMView.toDegrees(radians);
                    float speed = (float)Math.sqrt(vx*vx + vy*vy);

                    degrees = (degrees+360.0f) % 360.0f;

                    if (speed>MIN_FLY_TOLERANC) {
                        Vec2 wspeed = convertToNodeSpace(new Vec2(speed, 0));
                        performFly(_selectedView, degrees, wspeed.x);
                    }
                }
                _velocityTracker.clear();
            }
            return TOUCH_TRUE;
        }
        return ret;
    }

    @Override
    public SMView getDraggableObjectAtPoint(MultiTouchController.PointInfo touchPoint) {
        Vec2 worldPoint = convertToWorldSpace(touchPoint.getPoint());

        ArrayList<SMView> children = getChildren();
        ListIterator<SMView> iter = children.listIterator();
        while (iter.hasNext()) {
            SMView child = iter.next();
            if (child==_bgView) {
                continue;
            }

            Vec2 nodePoint = convertToNodeSpace(worldPoint);
            Size size = child.getContentSize();
            if (!(nodePoint.x<0 || nodePoint.y<0 || nodePoint.x>size.width-1 || nodePoint.y>size.height-1)) {
                if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null) {
                    return child;
                }
            }
        }

        if (_selectedView!=null) {
            performSelected(_selectedView, false);
            _selectedView = null;
        }

        return null;
    }

    @Override
    public void getPositionAndScale(SMView view, MultiTouchController.PositionAndScale objPosAndScaleOut) {
        Vec2 pt = view.getPosition();
        objPosAndScaleOut.set(pt.x, pt.y, true, view.getScale(), false, 1.0f, 1.0f, true, (float) SMView.toRadians(-view.getRotation()));
    }

    @Override
    public boolean setPositionAndScale(SMView view, MultiTouchController.PositionAndScale newObjPosAndScale, MultiTouchController.PointInfo touchPoint) {
        view.setPosition(newObjPosAndScale.getXOff(), newObjPosAndScale.getYOff(), false);
        view.setScale(newObjPosAndScale.getScale(), false);
        view.setRotation((float)(-SMView.toDegrees(newObjPosAndScale.getAngle())), false);

        return true;
    }

    @Override
    public void selectObject(SMView view, MultiTouchController.PointInfo touchPoint) {
        if (view!=null) {
            bringChildToTop(view);

            if (_selectedView!=view) {
                if (_selectedView!=null) {
                    performSelected(_selectedView, false);
                }

                _selectedView = view;
                performSelected(view, true);

                if (view!=null) {
                    _velocityTracker.clear();
                    _lastTouchPoint.set(touchPoint);
                }
            }
        }
    }

    @Override
    public void doubleClickObject(SMView view, MultiTouchController.PointInfo touchPoint) {
        if (_listener!=null) {
            Vec2 point = touchPoint.getPoint();
            _listener.onStickerDoubleClicked(view, convertToWorldSpace(point));
        }
    }

    @Override
    public void touchModeChanged(final int touchMode, MultiTouchController.PointInfo touchPoint) {
        if (touchMode==MultiTouchController.MODE_DRAG) {
            _velocityTracker.clear();
            _lastTouchPoint.set(touchPoint);
            _trackFlyEvent = false;
        }
    }

    @Override
    public Vec2 toWorldPoint(final Vec2 canvasPoint) {
        return convertToWorldSpace(canvasPoint);
    }

    @Override
    public Vec2 toCanvasPoint(final Vec2 worldPoint) {
        return convertToNodeSpace(worldPoint);
    }

    public void performSelected(SMView view, boolean selected) {
        if (_listener!=null) {
            _listener.onStickerSelected(view, selected);
        }
    }

    public void performFly(SMView view, final float degrees, final float speed) {
        if (!_flyRemovable) {
            return;
        }

        if (_selectedView==view) {
            performSelected(view, false);
            _selectedView = null;
        }

        Vec2 pt = view.getPosition();
        float dist = speed / 10.0f;

        float deltaX = dist * (float) Math.cos(SMView.toRadians(degrees));
        float deltaY = dist * (float) Math.sin(SMView.toRadians(degrees));
        float rotate = speed / 100.0f;

        float direction = (degrees>90 && degrees<270) ? -1 : 1;
        FiniteTimeAction moveTo = EaseOut.create(getDirector(), MoveBy.create(getDirector(), FLY_DURATION, new Vec2(deltaX, deltaY)), 3.0f);
        FiniteTimeAction rotateTo = RotateBy.create(getDirector(), FLY_DURATION, direction*rotate);
        FiniteTimeAction fadeTo = Sequence.create(getDirector(), DelayTime.create(getDirector(), FLY_DURATION/2), FadeOut.create(getDirector(), FLY_DURATION/2), null);
        FiniteTimeAction remove = Spawn.create(getDirector(), moveTo, rotateTo, fadeTo, null);
        FiniteTimeAction seq = Sequence.create(getDirector(), remove, CallFuncN.create(getDirector(), new PERFORM_SEL_N() {
            @Override
            public void performSelectorN(SMView target) {
                if (_listener!=null) {
                    _listener.onStickerRemoveEnd(target);
                }
                target.removeFromParent();
            }
        }), null);
        seq.setTag(AppConst.TAG.ACTION_STICKER_REMOVE);
        view.runAction(seq);

        if (_listener!=null) {
            _listener.onStickerRemoveBegin(view);
        }
    }

    public void removeChildWithGenieAction(SMView child, Sprite sprite, final Vec2 removeAnchor) {
        removeChildWithGenieAction(child, sprite, removeAnchor, 0.7f);
    }
    public void removeChildWithGenieAction(SMView child, Sprite sprite, final Vec2 removeAnchor, float duration) {
        removeChildWithGenieAction(child, sprite, removeAnchor, duration, 0.15f);
    }
    public void removeChildWithGenieAction(SMView child, Sprite sprite, final Vec2 removeAnchor, float duration, float delay) {
        if (sprite==null) {
            removeChild(child);
            return;
        }

        if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null) {
            return;
        }

        if (_selectedView==child) {
            performSelected(child, false);
            _selectedView = null;
        }

        FiniteTimeAction genie = EaseBackIn.create(getDirector(), GenieAction.create(getDirector(), duration, sprite, removeAnchor));
        FiniteTimeAction seq = Sequence.create(getDirector(), DelayTime.create(getDirector(), delay), CallFuncN.create(getDirector(), new PERFORM_SEL_N() {
            @Override
            public void performSelectorN(SMView target) {
                if (_listener!=null) {
                    _listener.onStickerRemoveEnd(target);
                }
                target.removeFromParent();
            }
        }), null);

        seq.setTag(AppConst.TAG.ACTION_STICKER_REMOVE);
        child.runAction(seq);

        if (_listener!=null) {
            _listener.onStickerRemoveBegin(child);
        }
    }

    // just fade out
    public void removeChildWithFadeOut(SMView child, float duration, float delay) { // just fade
        if (child.getAlpha()<=0) {
            removeChild(child);
            return;
        }

        if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null) {
            return;
        }

        if (_selectedView==child) {
            performSelected(child, false);
            _selectedView = null;
        }

        FiniteTimeAction seq = Sequence.create(getDirector(), DelayTime.create(getDirector(), delay), FadeTo.create(getDirector(), duration, 0), CallFuncN.create(getDirector(), new PERFORM_SEL_N() {
            @Override
            public void performSelectorN(SMView target) {
                if (_listener!=null) {
                    _listener.onStickerRemoveEnd(target);
                }
                target.removeFromParent();
            }
        }), null);

        seq.setTag(AppConst.TAG.ACTION_STICKER_REMOVE);
        child.runAction(seq);

        if (_listener!=null) {
            _listener.onStickerRemoveBegin(child);
        }
    }

    public void removeChildWithFly(SMView child, final float degress, final float speed) { // flying
        if (child.getAlpha()<=0) {
            removeChild(child);
            return;
        }

        if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null) {
            return;
        }

        if (_selectedView==child) {
            performSelected(child, false);
            _selectedView = null;
        }

        performFly(child, degress, speed);
    }

    @Override
    public boolean containsPoint(final Vec2 point) {
        return true;
    }

    public interface StickerCanvasListener {
        public void onStickerSelected(SMView view, final boolean select);
        public void onStickerRemoveBegin(SMView view);
        public void onStickerRemoveEnd(SMView view);
        public void onStickerDoubleClicked(SMView view, final Vec2 worldPoint);
        public void onStickerTouch(SMView view, int action);
    }

    public void setStickerListener(StickerCanvasListener l) {_listener = l;}


    @Override
    public int dispatchTouchEvent(MotionEvent event, SMView view, boolean checkBounds) {
        return super.dispatchTouchEvent(event, view, false);
    }

    public void cancel() {
        if (_selectedView!=null) {
            setSelectedSticker(null);
        }
    }


    private MultiTouchController _controller = null;

    private SMView _selectedView = null;
    private VelocityTracker _velocityTracker = null;

    private boolean _trackFlyEvent = false;
    private boolean _flyRemovable = true;

    private MultiTouchController.PointInfo _lastTouchPoint = new MultiTouchController.PointInfo();
    private StickerCanvasListener _listener = null;


}
