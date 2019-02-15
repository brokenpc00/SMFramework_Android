package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.tweenfunc;


public class EaseBezierAction extends ActionEase {
    public EaseBezierAction(IDirector director) {
        super(director);
    }

    public static EaseBezierAction create(IDirector director, ActionInterval action) {
        EaseBezierAction ease = new EaseBezierAction(director);

        if (ease != null && ease.initWithAction(action)) {
            return ease;
        }

        return null;
    }

    @Override
    public EaseBezierAction clone() {
        if (_inner != null) {
            EaseBezierAction ret = EaseBezierAction.create(getDirector(), _inner.clone());
            if (ret!=null) {
                ret.setBezierParamer(_p0, _p1, _p2, _p3);
            }
            return  ret;
        }
        return null;
    }

    @Override
    public void update(float time) {
        _inner.update(tweenfunc.bezieratFunction(_p0, _p1, _p2, _p3, time));
    }

    @Override
    public ActionEase reverse() {
        EaseBezierAction reverseAction = EaseBezierAction.create(getDirector(), _inner.reverse());
        if (reverseAction!=null) {
            reverseAction.setBezierParamer(_p3, _p2, _p1, _p0);
            return reverseAction;
        }
        return null;
    }

    public void setBezierParamer( float p0, float p1, float p2, float p3) {
        _p0 = p0;
        _p1 = p1;
        _p2 = p2;
        _p3 = p3;
    }

    protected float _p0;
    protected float _p1;
    protected float _p2;
    protected float _p3;

}