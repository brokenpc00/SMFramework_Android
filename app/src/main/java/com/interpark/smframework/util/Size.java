package com.interpark.smframework.util;

import android.util.Log;

public final class Size implements Cloneable  {
    public float width;
    public float height;

    public Size(Size s) {
        this.width = s.width;
        this.height = s.height;
    }

    public Size(float[] v) {
        this.width = v[0];
        this.height = v[1];
    }

    public Size() {
        this.width = 0.0f;
        this.height = 0.0f;
    }

    public Size(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public boolean equals(Size s) {
        if (this.width==s.width && this.height==s.height) {
            return true;
        }
        return false;
    }

    public void set(Size v) {
        this.width = v.width;
        this.height = v.height;
    }

    public void set(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setSize(Size v) {
        this.width = v.width;
        this.height = v.height;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }


    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float width() {return this.width;}
    public float height() {return this.height;}

    public void print(String tag, String msg) {
        Log.i(tag, msg + " : " + this.width + ", " + this.height);
    }

    public Size scale(float v) {
        Size size = new Size();
        size.setWidth(this.width*v);
        size.setHeight(this.height*v);
        return size;
    }

    public void scaleLocal(float v) {
        this.width *= v;
        this.height *= v;
    }

    public Size getIntValue() {
        Size intS = new Size();
        intS.set((int)this.width, (int)this.height);
        return intS;
    }

    public boolean roundEqual(Size size) {
        return ((Math.round(this.width)==Math.round(size.width())) && (Math.round(this.height)==Math.round(size.height())));
    }

    public static final Size ZERO = new Size(0, 0);
}
