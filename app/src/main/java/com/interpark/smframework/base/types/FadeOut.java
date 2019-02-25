package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import org.apache.http.cookie.SM;

public class FadeOut extends FadeTo {
    public FadeOut(IDirector director) {
        super(director);
    }

    public static FadeOut create(IDirector director, float duration) {
        FadeOut action = new FadeOut(director);
        action.initWithDuration(duration, 0.0f);
        return action;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);

        if (_reverseAction!=null) {
            this._toAlpha = this._reverseAction._fromAlpha;
        } else {
            this._toAlpha = 0.0f;
        }

        if (target!=null) {
            _fromAlpha = target.getAlpha();
        }
    }

    @Override
    public FadeOut clone() {
        return FadeOut.create(getDirector(), _duration);
    }

    @Override
    public FadeTo reverse() {
        Action action = FadeIn.create(getDirector(), _duration);
        ((FadeIn) action).setReverseAction(this);
        return (FadeTo)action;

    }

    public void setReverseAction(FadeTo ac) {
        _reverseAction = ac;
    }

    private FadeTo _reverseAction = null;
}
