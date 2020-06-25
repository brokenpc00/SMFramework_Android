package com.interpark.smframework.util;

import android.util.Log;

public final class Vec2 implements Cloneable {
    public float x;
    public float y;

    public Vec2(Vec2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vec2(float[] v) {
        this.x = v[0];
        this.y = v[1];
    }

    public Vec2() {
        this.x = 0;
        this.y = 0;
    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Size toSize() {
        return new Size(this.x, this.y);
    }

    public boolean isZero() {
        return this.x==0.0f && this.y==0.0f;
    }

    public boolean isOne() {
        return this.x==1.0f && this.y==1.0f;
    }

    public boolean equals(Vec2 v) {
        if (this.x==v.x && this.y==v.y) {
            return true;
        }
        return false;
    }

//    public float length() {
//        double v = (double)(this.x*this.x + this.y*this.y);
//        return (float)Math.sqrt(v);
//    }

    public void Normalize() {
        double len = length();
        this.x /= len;
        this.y /= len;
    }

    public Vec2 getVectorTo(Vec2 pt) {
        Vec2 aux = new Vec2();
        aux.setX(pt.x - this.x);
        aux.setY(pt.y - this.y);
        return aux;
    }

    public Vec2 set(Vec2 v) {
        this.x = v.x;
        this.y = v.y;

        return this;
    }

    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;

        return this;
    }

    public float setX(float x) {
        this.x = x;
        return this.x;
    }

    public float setY(float y) {
        this.y = y;
        return this.y;
    }

    public void offset(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public float x() {return this.x;}
    public float y() {return this.y;}

    public void print(String tag, String msg) {
        Log.i(tag, msg + " : " + this.x + ", " + this.y);
    }

    public float dot(Vec2 pt) {
        return (float)(this.x*pt.x() + this.y*pt.y());
    }

    public Vec2 add(Vec2 pt) {
        Vec2 ret = new Vec2();
        ret.setX(this.x + pt.x);
        ret.setY(this.y + pt.y);
        return ret;
    }

    public void addLocal(Vec2 pt) {
        this.x += pt.x();
        this.y += pt.y();
    }

    public Vec2 scale(float v) {
        Vec2 ret = new Vec2();
        ret.setX(this.x*v);
        ret.setY(this.y*v);
        return ret;
    }

    public void scaleLocal(float v) {
        this.x *= v;
        this.y *= v;
    }

    public Vec2 minus(Vec2 pt) {
        Vec2 ret = new Vec2();
        ret.setX(this.x-pt.x());
        ret.setY(this.y-pt.y());
        return ret;
    }

    public void minuLocal(Vec2 pt) {
        this.x -= pt.x();
        this.y -= pt.y();
    }

    public Vec2 multiply(float r) {
        Vec2 ret = new Vec2();
        ret.setX(this.x*r);
        ret.setY(this.y*r);
        return ret;
    }

    public void multiplyLocal(float r) {
        this.x *= r;
        this.y *= r;
    }

    public Vec2 divide(float r) {
        Vec2 ret = new Vec2();
        ret.setX(this.x/r);
        ret.setY(this.y/r);
        return ret;
    }

    public void divideLocal(float r) {
        this.x /= r;
        this.y /= r;
    }


    public Vec2 getIntValue() {
        Vec2 intV = new Vec2();
        intV.set((int)this.x, (int)this.y);
        return intV;
    }

    public boolean roundEqual(Vec2 pt) {
        return ((Math.round(this.x)==Math.round(pt.x())) && (Math.round(this.y)==Math.round(pt.y())));
    }

    public static float clampf(float value, float min_inclusive, float max_inclusive)
    {
        if (min_inclusive > max_inclusive) {
            float tmp = min_inclusive;
            min_inclusive = max_inclusive;
            max_inclusive = tmp;
        }
        return value < min_inclusive ? min_inclusive : value < max_inclusive? value : max_inclusive;
    }

    public float distance(Vec2 v) {
//        float dx = v.x - this.x;
//        float dy = v.y - this.y;
//        return (float) Math.sqrt(dx*dx + dy*dy);
        return (float) Math.sqrt(distanceSquared(v));
    }

    public float distanceSquared(Vec2 v) {
        float dx = v.x - this.x;
        float dy = v.y - this.y;
        return dx*dx + dy*dy;
    }

    public float lengthSquared() {
        return (this.x*this.x + this.y*this.y);
    }
    public float length() {
        return (float)Math.sqrt(x*x+y*y);
//        return (float)Math.sqrt(lengthSquared());
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public void smooth(Vec2 target, float elapsedTime, float responseTime) {
        if (elapsedTime>0) {
            Vec2 newV = new Vec2(target.x - this.x, target.y - this.y);
            newV.x = newV.x * (elapsedTime / (elapsedTime + responseTime));
            newV.y = newV.y * (elapsedTime / (elapsedTime + responseTime));
            this.x += newV.x;
            this.y += newV.y;
        }
    }

    public static final Vec2 ZERO = new Vec2(0, 0);
    public static final Vec2 MIDDLE = new Vec2(0.5f, 0.5f);
    public static final Vec2 LEFT_TOP = new Vec2(0.0f, 0.0f);
    public static final Vec2 LEFT_BOTTOM = new Vec2(0.0f, 1.0f);
    public static final Vec2 RIGHT_TOP = new Vec2(1.0f, 0.0f);
    public static final Vec2 RIGHT_BOTTOM = new Vec2(1.0f, 1.0f);
    public static final Vec2 MIDDLE_RIGHT = new Vec2(1, 0.5f);
    public static final Vec2 MIDDLE_LEFT = new Vec2(0, 0.5f);
    public static final Vec2 MIDDLE_TOP = new Vec2(0.5f, 0);
    public static final Vec2 MIDDLE_BOTTOM = new Vec2(0.5f, 1);
}
