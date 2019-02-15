package com.interpark.smframework.base.types;

import android.util.Log;

public class Color4B implements Cloneable {
    public Color4B() {
        r = 0xff;
        g = 0xff;
        b = 0xff;
        a = 0xff;
    }

    public Color4B(int _r, int _g, int _b, int _a) {
        r = _r;
        g = _g;
        b = _b;
        a = _a;

        checkValue();

    }

    public Color4B(Color4F color) {
        r = (int)color.r*0xff;
        g = (int)color.g*0xff;
        b = (int)color.b*0xff;
        a = (int)color.a*0xff;

        if (r>255) {
            r = 255;
        }
        if (g>255) {
            g = 255;
        }
        if (b>255) {
            b = 255;
        }
        if (a>255) {
            a = 255;
        }
        if (r<0) {
            r = 0;
        }
        if (g<0) {
            g = 0;
        }
        if (b<0) {
            b = 0;
        }
        if (a<0) {
            a = 0;
        }

        checkValue();
    }

    public void checkValue() {
        if (r>255) {
            r = 255;
        }
        if (g>255) {
            g = 255;
        }
        if (b>255) {
            b = 255;
        }
        if (a>255) {
            a = 255;
        }
        if (r<0) {
            r = 0;
        }
        if (g<0) {
            g = 0;
        }
        if (b<0) {
            b = 0;
        }
        if (a<0) {
            a = 0;
        }
    }

    public Color4B(int _r, int _g, int _b) {
        this (_r, _g, _b, 0xff);
    }

    public Color4F getColor4F() {
        return new Color4F(r/255.0f, g/255.0f, b/255.0f, a/255.0f);
    }

    public void setAlpha(final int _a) {
        a = _a;
    }

    public int r = 0xff;
    public int g = 0xff;
    public int b = 0xff;
    public int a = 0xff;
}
