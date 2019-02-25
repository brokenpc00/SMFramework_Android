package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class FadeIn extends FadeTo {
    public FadeIn(IDirector director) {
        super(director);
    }

    public static FadeIn create(IDirector director, float duration) {
        FadeIn action = new FadeIn(director);
        action.initWithDuration(duration, 1.0f);
        return action;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);

        if (_reverseAction!=null) {
            this._toAlpha = this._reverseAction._fromAlpha;
        } else {
            _toAlpha = 1.0f;
        }

        if (target!=null) {
            _fromAlpha = target.getAlpha();
        }
    }

    @Override
    public FadeIn clone() {
        return FadeIn.create(getDirector(), _duration);
    }

    @Override
    public FadeTo reverse() {
        Action action = FadeOut.create(getDirector(), _duration);
        ((FadeOut) action).setReverseAction(this);
        return (FadeTo)action;
    }

    public void setReverseAction(FadeTo ac) {
        _reverseAction = ac;
    }

    private FadeTo _reverseAction = null;
}
