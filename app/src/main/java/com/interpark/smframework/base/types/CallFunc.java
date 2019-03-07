package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class CallFunc extends ActionInstant {
    public CallFunc(IDirector director) {
        super(director);
    }

    public static CallFunc create(IDirector director, PERFORM_SEL func) {
        CallFunc action = new CallFunc(director);
        if (action!=null && action.initWithFunction(func)) {
            return action;
        }

        return null;
    }

    public void execute() {
        if (_function!=null) {
            _function.performSelector();
        }
    }

    // 필요 없다.
//    Ref getTargetCallback() {
//        return _selectorTarget;
//    }
//
//    public void setTargetCallback(Ref sel) {
//        if (sel!=_selectorTarget) {
//            _selectorTarget = sel;
//        }
//    }

    @Override
    public void update(float t) {
        super.update(t);
        execute();
    }

    @Override
    public CallFunc reverse() {
        return clone();
    }

    @Override
    public CallFunc clone() {
        CallFunc a = new CallFunc(getDirector());
        if (_function!=null) {
            a.initWithFunction(_function);
        }

        return a;
    }


    protected boolean initWithFunction(PERFORM_SEL func) {
        _function = func;
        return true;
    }

//    protected Ref _selectorTarget = null;
//    protected PERFORM_SEL _callFunc = null;
    protected PERFORM_SEL _function = null;

}
