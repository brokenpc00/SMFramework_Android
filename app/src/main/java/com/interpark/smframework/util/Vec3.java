package com.interpark.smframework.util;

public final class Vec3 implements Cloneable  {
    public float x;
    public float y;
    public float z;

    public Vec3(Vec3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vec3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(float[] val) {
        this.x = val[0];
        this.y = val[1];
        this.z = val[2];
    }

    public float x() {return this.x;}
    public float y() {return this.y;}
    public float z() {return this.z;}

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void set(Vec3 r) {
        this.x = r.x();
        this.y = r.y();
        this.z = r.z();
    }

    public void set(float[] val) {
        this.x = val[0];
        this.y = val[1];
        this.z = val[2];
    }

    public float magnitude() {
        return (float)Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }

    public Vec3 getUintVector() {
        float m = magnitude();
        return new Vec3(this.x/m, this.y/m , this.z/m);
    }

    public float dot(Vec3 r) {
        return this.x*r.x() + this.y*r.y() + this.z*r.z();
    }

    public Vec3 add(Vec3 r) {
        Vec3 ret = new Vec3();
        ret.x = this.x + r.x();
        ret.y = this.y + r.y();
        ret.z = this.z = r.z();
        return ret;
    }

    public void addLocal(Vec3 r) {
        this.x += r.x();
        this.y += r.y();
        this.z += r.z();
    }

    public Vec3 scale(float v) {
        Vec3 ret = new Vec3();
        ret.setX(this.x * v);
        ret.setY(this.y * v);
        ret.setZ(this.z * v);
        return ret;
    }

    public void scaleLocal(float v) {
        this.x *= v;
        this.y *= v;
        this.z *= z;
    }

    public Vec3 minus(Vec3 r) {
        Vec3 ret = new Vec3();
        ret.setX(this.x-r.x());
        ret.setY(this.y-r.y());
        ret.setZ(this.z-r.z());
        return ret;
    }

    public void minusLocal(Vec3 r) {
        this.x -= r.x();
        this.y -= r.y();
        this.z -= r.z();
    }

    public Vec3 multiply(float r) {
        Vec3 ret = new Vec3();
        ret.setX(this.x*r);
        ret.setY(this.y*r);
        ret.setZ(this.z*r);
        return ret;
    }

    public void multiplyLocal(float r) {
        this.x *= r;
        this.y *= r;
        this.z *= r;
    }

    public Vec3 divide(float r) {
        Vec3 ret = new Vec3();
        ret.setX(this.x/r);
        ret.setY(this.y/r);
        ret.setZ(this.z/r);
        return ret;
    }

    public void divideLocal(float r) {
        this.x /= r;
        this.y /= r;
        this.z /= r;
    }

    public boolean equals(Vec3 v) {
        if (this.x==v.x && this.y==v.y && this.z==v.z) {
            return true;
        }
        return false;
    }

    public static final Vec3 ZERO = new Vec3(0, 0, 0);

    public float[] toFloats() {
        return new float[] {this.x, this.y, this.z};
    }
}
