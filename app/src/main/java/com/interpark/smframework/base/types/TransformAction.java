package com.interpark.smframework.base.types;

import android.support.v7.widget.ViewUtils;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;
import com.interpark.smframework.util.tweenfunc.TweenType;

public class TransformAction extends DelayBaseAction {
    public TransformAction(IDirector director) {
        super(director);
    }

    public static TransformAction create(IDirector director) {
        TransformAction action = new TransformAction(director);
        action.initWithDuration(0);
        return action;
    }

    public TransformAction toScale(float scale) {
        _scaleAction = true;
        _toScale = scale;
        return this;
    }

    public TransformAction toPositionX(float positionX) {
        _positionXAction = true;
        _toPosition.set(positionX, 0);
        return this;
    }

    public TransformAction toPositoinY(float positionY) {
        _positionYAction = true;
        _toPosition.set(0, positionY);
        return this;
    }

    public TransformAction toPosition(final Vec2 position) {
        _positionXAction = true;
        _positionYAction = true;
        _toPosition.set(position);
        return this;
    }

    public TransformAction toAlpha(final float alpha) {
        _alphaAction = true;
        _toAlpha = alpha;
        return this;
    }

    public TransformAction removeOnFinish() {
        _removeOnFinish = true;
        return this;
    }

    public TransformAction enableOnFinish() {
        _enableOnFinish = true;
        return this;
    }

    public TransformAction enableSmooth() {
        _smooth = true;
        return this;
    }

    public TransformAction disableOnFinish() {
        _disableOnFinish = true;
        return this;
    }

    public TransformAction invisibleOnFinish() {
        _invisibleOnFinish = true;
        return this;
    }

    public TransformAction runActionOnFinish(Action action) {
        _action = action;
        return this;
    }

    public interface TransformFunc {
        public void onFinish(SMView target, int tag);
    }

    public interface TransformUpdateCallback {
        public void onUpdate(SMView target, int tag, float t);
    }

    public TransformAction runFuncOnFinish(final TransformFunc callback) {
        _finishCallback = callback;
        return this;
    }

    public TransformAction setUpdateCallback(final TransformUpdateCallback callback) {
        _updateCallback = callback;
        return this;
    }

    public TransformAction setTweenFunc(TweenType type) {
        return setTweenFunc(type, 0);
    }
    public TransformAction setTweenFunc(TweenType type, float easingParam) {
        _tweenType = type;
        _easingParam = easingParam;
        return this;
    }

    @Override
    public void onStart() {
        if (_scaleAction) {
            _fromScale = _target.getScale();
        }
        if (_rotateAction) {
            _fromAngle = _target.getRotation();
        }
        if (_positionXAction || _positionYAction) {
            _fromPosition = _target.getPosition();
        }
        if (_alphaAction) {
            _fromAlpha = _target.getAlpha();
        }
        _target.setVisible(true);

        if (_smooth) {
            _view = _target;
        }
    }

    @Override
    public void onUpdate(float t) {
        if (_tweenType != TweenType.Linear) {
            float[] easingParam = new float[1];
            easingParam[0] = _easingParam;
            t = tweenfunc.tweenTo(t, _tweenType, easingParam);
        }

        if (_scaleAction) {
            float scale = SMView.interpolation(_fromScale, _toScale, t);
            if (_view!=null) {
                _view.setScale(scale, false);
            } else {
                _target.setScale(scale);
            }
        }

        if (_rotateAction) {
            float angle = SMView.interpolation(_fromScale, _toScale, t);
            if (_view!=null) {
                _view.setRotation(angle, false);
            } else {
                _target.setRotation(angle);
            }
        }

        if (_alphaAction) {
            float alpha = SMView.interpolation(_fromAlpha, _toAlpha, t);
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            _target.setAlpha(alpha);
        }

        if (_positionXAction && _positionYAction) {
            float x = SMView.interpolation(_fromPosition.x, _toPosition.x, t);
            float y = SMView.interpolation(_fromPosition.y, _toPosition.y, t);
            if (_view!=null) {
                _view.setPosition(x, y, false);
            } else {
                _target.setPosition(x, y);
            }
        } else if (_positionXAction) {
            float x = SMView.interpolation(_fromPosition.x, _toPosition.x, t);
            if (_view!=null) {
                _view.setPositionX(x, false);
            } else {
                _target.setPositionX(x);
            }
        } else if (_positionYAction) {
            float y = SMView.interpolation(_fromPosition.y, _toPosition.y, t);
            if (_view!=null) {
                _view.setPositionY(y, false);
            } else {
                _target.setPositionY(y);
            }
        }

        if (_updateCallback!=null) {
            _updateCallback.onUpdate(_target, getTag(), t);
        }
    }

    @Override
    public void onEnd() {
        if (_enableOnFinish) {
            SMView view = _target;
            if (view!=null) {
                view.setEnabled(true);
            }
        }

        if (_disableOnFinish) {
            SMView view = _target;
            if (view!=null) {
                view.setEnabled(false);
            }
        }

        if (_invisibleOnFinish) {
            _target.setVisible(false);
        }

        if (_removeOnFinish) {
            if (_target.getParent()!=null) {
                _target.removeFromParent();
            }
        }

        if (_finishCallback!=null) {
            _finishCallback.onFinish(getTarget(), getTag());
        }

        if (_action!=null) {
            _target.runAction(_action);
            _action = null;
        }
    }

    private boolean _scaleAction = false;
    private boolean _positionXAction = false;
    private boolean _positionYAction = false;
    private boolean _alphaAction = false;
    private boolean _rotateAction = false;

    private float _fromScale = 1.0f;
    private float _toScale = 1.0f;
    private float _fromAlpha = 1.0f;
    private float _toAlpha = 1.0f;
    private float _fromAngle = 0.0f;
    private float _toAngle = 0.0f;
    private Vec2 _fromPosition = new Vec2(0, 0);
    private Vec2 _toPosition = new Vec2(0, 0);

    private boolean _removeOnFinish = false;
    private boolean _enableOnFinish = false;
    private boolean _disableOnFinish = false;
    private boolean _invisibleOnFinish = false;

    private boolean _smooth = false;

    private TweenType _tweenType = TweenType.Circ_EaseOut;
    private float _easingParam = 0;

    private TransformFunc _finishCallback = null;
    private TransformUpdateCallback _updateCallback = null;
    private Action _action = null;
    private SMView _view = null;
}
