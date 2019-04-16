package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class CallFuncN extends CallFunc {
    public CallFuncN(IDirector director) {
        super(director);
    }

    public static CallFuncN create(IDirector director, PERFORM_SEL_N func) {
        CallFuncN ret = new CallFuncN(director);
        ret.initWithFunction(func);
        return ret;
    }

    @Override
    public void execute() {
        if (_functionN!=null) {
            _functionN.performSelectorN(_target);
        }
    }

    @Override
    public CallFuncN clone() {
        CallFuncN func = new CallFuncN(getDirector());

        if (_target!=null) {
            func.initWithFunction(_functionN);
        }

        return func;
    }

    protected boolean initWithFunction(final PERFORM_SEL_N func) {
        _functionN = func;
        return true;
    }

    protected PERFORM_SEL_N _functionN = null;
}
