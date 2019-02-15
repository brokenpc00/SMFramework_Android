package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class FlipX extends ActionInstant {
    public FlipX(IDirector director) {
        super(director);
    }

    public static FlipX create(IDirector director, boolean x) {
        FlipX action = new FlipX(director);
        if (action!=null && action.initWithFlipX(x)) {
            return action;
        }

        return null;
    }

    @Override
    public void update(float t) {
        super.update(t);
        // SMImageView setFlipX....
    }

    @Override
    public FlipX reverse() {
        return FlipX.create(getDirector(), !_flipX);
    }

    @Override
    public FlipX clone() {
        return FlipX.create(getDirector(), _flipX);
    }

    protected boolean initWithFlipX(boolean x) {
        _flipX = x;
        return true;
    }

    protected boolean _flipX;
}
