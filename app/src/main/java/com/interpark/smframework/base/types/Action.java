package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import org.apache.http.cookie.SM;

public class Action implements Cloneable {

    public Action(IDirector director) {
        _director = director;
        _originalTarget = null;
        _target = null;
        _tag = INVALID_TAG;
        _flags = 0;
    }

    public IDirector getDirector() {return _director;}

    public static int INVALID_TAG = -1;

    public Action clone() {
        return null;
    }

    public Action reverse() {
        return null;
    }

    public boolean isDone() {
        return true;
    }

    public void startWithTarget(SMView target) {
        _originalTarget = _target = target;
    }

    public void stop() {
        _target = null;
    }

    public void step(float dt) {

    }

    public void update(float time) {

    }

    public SMView getTarget() {
        return _target;
    }

    public void setTarget(SMView target) {
        _target = target;
    }

    public Ref getOriginalTarget() {
        return _originalTarget;
    }

    public int getTag() {
        return _tag;
    }

    public void setTag(int tag) {
        _tag = tag;
    }

    public long getFlags() {
        return _flags;
    }

    public void setFlags(long flags) {
        _flags = flags;
    }

    IDirector _director = null;
    protected SMView _originalTarget = null;
    protected SMView _target = null;
    protected int _tag = INVALID_TAG;
    protected long _flags = 0;
}
