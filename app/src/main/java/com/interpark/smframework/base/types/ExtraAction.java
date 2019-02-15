package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class ExtraAction extends FiniteTimeAction {
    public ExtraAction(IDirector director) {
        super(director);
    }

    public static ExtraAction create(IDirector director) {
        ExtraAction ret = new ExtraAction(director);
        if (ret!=null) {
            return ret;
        }
        return null;
    }

    @Override
    public ExtraAction clone() {
        return ExtraAction.create(getDirector());
    }

    @Override
    public ExtraAction reverse() {
        return ExtraAction.create(getDirector());
    }

    @Override
    public void update(float t) {
        // nothign to do
    }

    @Override
    public void step(float dt) {
        // nothing to do
    }

}
