package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;

public class Ref implements Cloneable {
    public Ref(IDirector director) {
        _director = director;
    }

    public IDirector getDirector() {
        return _director;
    }
    protected IDirector _director = null;
}
