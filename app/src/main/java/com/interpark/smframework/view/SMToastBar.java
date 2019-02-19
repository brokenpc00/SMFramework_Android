package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.SEL_SCHEDULE;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;

public class SMToastBar extends SMView {
    public SMToastBar(IDirector director) {
        super(director);
        _label[0] = null;
        _label[1] = null;
    }

    public static final Color4F RED = new Color4F(1, 0, 0, 1);
    public static final Color4F GRAY = new Color4F(1, 0, 0, 1);
    public static final Color4F GREEN = new Color4F(1, 0, 0, 1);
    public static final Color4F BLUE = new Color4F(1, 0, 0, 1);

    private float FONT_SIZE = 38;

    private float SHOW_TIME = 0.2f;
    private float HIDE_TIME = 0.2f;
    private float TRANS_TIME = 0.5f;
    private float MOVE_TIME = 0.3f;
    private float TEXT_CHANGE_TIME = 0.2f;

    private float MIN_HEIGHT = 100.0f;
    private float PADDING = 20.0f;


    public interface ToastBarCallback {
        public void func(SMToastBar bar);
    }

    public static SMToastBar create(IDirector director, SMToastBar.ToastBarCallback callback) {
        SMToastBar bar = new SMToastBar(director);
        bar.initWithCallback(callback);
        return bar;
    }

    public void setMessage(String message, final Color4F color, float duration) {
        setVisible(true);

        _labelIndex = (_labelIndex+1)%2;

        if (_label[_labelIndex] == null) {
            SMLabel l = SMLabel.create(getDirector(), message, FONT_SIZE, 0, 0, 0, 1);
            l.setAnchorPoint(new Vec2(0.5f, 0.5f));
            _textContainer.addChild(l);
            _label[_labelIndex] = l;
        } else {
            _label[_labelIndex].setText(message);
            _label[_labelIndex].setVisible(true);
        }

        float reqHeight = Math.max(MIN_HEIGHT, _label[_labelIndex].getContentSize().height + PADDING*2);

        if (_label[1-_labelIndex]==null) {
            // first entered
            setContentSize(_contentSize.width, reqHeight);
            _textContainer.setPosition(_contentSize.width/2, _contentSize.height/2);

            TransformAction a = TransformAction.create(getDirector());
            a.toPositoinY(-reqHeight).setTweenFunc(tweenfunc.TweenType.Cubic_EaseOut);
            a.setTimeValue(SHOW_TIME, 0);
            runAction(a);
        } else {
            SMLabel l1 = _label[1-_labelIndex]; // previous label
            SMLabel l2 = _label[_labelIndex]; // current label

            l1.stopAllActions();
            l2.stopAllActions();

            _textContainer.stopAllActions();
            stopAllActions();

            // previous label hide
            if (l1.isVisible()) {
                TransformAction hide = TransformAction.create(getDirector());
                hide.toAlpha(0).invisibleOnFinish();
                hide.setTimeValue(TEXT_CHANGE_TIME, 0);
                l1.runAction(hide);
            }

            // current label show
            if (l2.getAlpha() < 1.0f) {
                TransformAction show = TransformAction.create(getDirector());
                show.toAlpha(1);
                show.setTimeValue(TEXT_CHANGE_TIME, 0);
                l2.runAction(show);
            }

            // move label position
            if (_textContainer.getPositionY() != reqHeight/2) {
                TransformAction moveY = TransformAction.create(getDirector());
                moveY.toPositoinY(reqHeight/2);
                moveY.setTimeValue(TEXT_CHANGE_TIME, 0);
                _textContainer.runAction(moveY);
            }

            // move bar position
            if (getPositionY() != -reqHeight) {
                TransformAction moveY = TransformAction.create(getDirector());
                moveY.toPositoinY(-reqHeight).setTweenFunc(tweenfunc.TweenType.Back_EaseOut);
                moveY.setTimeValue(MOVE_TIME, 0);
                runAction(moveY);
            }
        }

        setBgColor(color);

        if (isScheduled(_timeout)) {
            unschedule(_timeout);
        }

        scheduleOnce(_timeout, duration);

        TransformAction action = TransformAction.create(getDirector());
        action.toAlpha(1).toScale(1).setTweenFunc(tweenfunc.TweenType.Back_EaseOut);
        action.setTimeValue(0.2f, 0.1f);
        _label[_labelIndex].setAlpha(0.0f);
        _label[_labelIndex].setScale(0.8f);
        _label[_labelIndex].runAction(action);
    }

    private SEL_SCHEDULE _timeout = new SEL_SCHEDULE() {
        @Override
        public void onFunc(float t) {
            onTimeOut(t);
        }
    };

    public void setBgColor(final Color4F color) {
        if (color.equals(_bgColor)) return;

        _bgColor.set(color);

        if (!_colorInit) {
            _colorInit = true;
            setBackgroundColor(_bgColor);
        } else {
            setBackgroundColor(_bgColor, 0.25f);
        }
    }

    @Override
    public void renderFrame(float alpha) {
        if (!_visible) return;

//        setScissorEnable();
    }

    protected boolean initWithCallback(SMToastBar.ToastBarCallback callback) {
        _callback = callback;

        Size s = getDirector().getWinSize();
        setContentSize(s.width, 0);

        _textContainer = SMView.create(getDirector());
        addChild(_textContainer);


        setVisible(false);

        setScissorEnable(true);

        return true;
    }

    private void onTimeOut(float a) {
        stopAllActions();

        TransformAction moveY = TransformAction.create(getDirector());
        moveY.toPositoinY(0).setTweenFunc(tweenfunc.TweenType.Cubic_EaseOut).runFuncOnFinish(new TransformAction.TransformFunc() {
            @Override
            public void func(SMView target, int tag) {
                onHideComplete(target, tag);
            }
        });
        moveY.setTimeValue(HIDE_TIME, 0);
        runAction(moveY);
    }

    private void onHideComplete(SMView target, int tag) {
        _colorInit = false;

        for (int i=0; i<2; i++) {
            if (_label[i]!=null) {
                _textContainer.removeChild(_label[i]);
                _label[i] = null;
            }
        }

        setVisible(false);

        if (_callback!=null) {
            _callback.func(this);
        }
    }

    private boolean _colorInit = false;
    private int _labelIndex = -1;
    private SMLabel[] _label = new SMLabel[2];
    private Color4F _bgColor = new Color4F(0, 0, 0, 0);
    private SMView _textContainer = null;
    private SMToastBar.ToastBarCallback _callback = null;
    private Rect _clipRect = new Rect(0, 0, 0, 0);
}
