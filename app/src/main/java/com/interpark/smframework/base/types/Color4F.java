package com.interpark.smframework.base.types;

import android.util.Log;

public class Color4F implements Cloneable {
    public Color4F() {
        r = 1.0f;
        g = 1.0f;
        b = 1.0f;
        a = 0.0f;
    }

    public Color4F(float _r, float _g, float _b, float _a) {
        r = _r;
        g = _g;
        b = _b;
        a = _a;
        checkValue();
    }

    public Color4F(Color4F color) {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
        checkValue();
    }

    public Color4F(Color4B color) {
        r = ((float)color.r/255.0f);
        g = ((float)color.g/255.0f);
        b = ((float)color.b/255.0f);
        a = ((float)color.a/255.0f);
        checkValue();
    }

    public void checkValue() {
        return;
//        if (r>1.0f) {
//            r = 1.0f;
//        }
//        if (g>1.0f) {
//            g = 1.0f;
//        }
//        if (b>1.0f) {
//            b = 1.0f;
//        }
//        if (a>1.0f) {
//            a = 1.0f;
//        }
//        if (r<0.0f) {
//            r = 0.0f;
//        }
//        if (g<0.0f) {
//            g = 0.0f;
//        }
//        if (b<0.0f) {
//            b = 0.0f;
//        }
//        if (a<0.0f) {
//            a = 0.0f;
//        }
    }

    public Color4F(float _r, float _g, float _b) {
        this (_r, _g, _b, 1.0f);
    }

    public void setAlpha(final float _a) {
        a = _a;
    }

    public Color4B getColor4B() {
        return new Color4B((int)(r*0xff), (int)(g*0xff), (int)(b/0xff), (int)(a/0xff));
    }

    public Color4F minus(Color4F color) {
        return new Color4F(r - color.r, g - color.g, b - color.b, a - color.a);
    }

    public Color4F add(Color4F color) {
        return new Color4F(r + color.r, g + color.g, b + color.b, a + color.a);
    }

    public Color4F multiply(float t) {
        return new Color4F(r * t, g * t, b * t, a * t);
    }

    public Color4F divide(float t) {
        return new Color4F(r / t, g / t, b / t, a / t);
    }

    public float r = 1.0f;
    public float g = 1.0f;
    public float b = 1.0f;
    public float a = 0.0f;
}
