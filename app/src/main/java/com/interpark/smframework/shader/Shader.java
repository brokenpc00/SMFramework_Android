package com.interpark.smframework.shader;

public class Shader {
    private int shaderId;
    private int refCount = 0;

    public Shader(int shaderId) {
        this.shaderId = shaderId;
    }

    public void incRef() {
        refCount++;
    }

    public boolean decRef() {
        return (--refCount <= 0);
    }

    public int getRefCount() {
        return refCount;
    }

    public int getId() {
        return shaderId;
    }
}
