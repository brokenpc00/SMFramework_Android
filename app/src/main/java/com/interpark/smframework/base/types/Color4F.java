package com.interpark.smframework.base.types;

import android.util.Log;

import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.AppConst;

public class Color4F implements Cloneable {
    public Color4F() {
        r = 1.0f;
        g = 1.0f;
        b = 1.0f;
        a = 0.0f;
    }

    public Color4F(float[] color) {
        if (color==null || color.length!=4) return;

        r = color[0];
        g = color[1];
        b = color[2];
        a = color[3];
    }

    public Color4F(float _r, float _g, float _b, float _a) {
        r = _r;
        g = _g;
        b = _b;
        a = _a;
        checkValue();
    }

    public Color4F(Color4F color) {
        set(color);
    }

    public Color4F(Color4B color) {
        r = ((float)color.r/255.0f);
        g = ((float)color.g/255.0f);
        b = ((float)color.b/255.0f);
        a = ((float)color.a/255.0f);
        checkValue();
    }

    public void set(float[] color) {
        if (color==null && color.length!=4) return;
        r = color[0];
        g = color[1];
        b = color[2];
        a = color[3];
    }

    public void set(Color4F color) {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
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

    public boolean equals(Color4F color) {
        return  (r==color.r && g==color.g && b==color.b && a==color.a);
    }

    public float r = 1.0f;
    public float g = 1.0f;
    public float b = 1.0f;
    public float a = 0.0f;

    public static final Color4F WHITE = new Color4F(1, 1, 1, 1);
    public static final Color4F TRANSPARENT = new Color4F(0, 0, 0, 0);
    public static final Color4F BLACK = new Color4F(0, 0, 0, 1);

    public static final Color4F XDBDCDF = SMView.MakeColor4F(0xdbdcdf, 1.0f);
    public static final Color4F XADAFB3 = SMView.MakeColor4F(0xadafb3, 1.0f);
    public static final Color4F XEEEFF1 = SMView.MakeColor4F(0xeeeff1, 1.0f);
    public static final Color4F X00A1E4 = SMView.MakeColor4F(0x00a1e4, 1.0f);

    public static final Color4F TEXT_BLACK = SMView.MakeColor4F(0x222222, 1.0f);
    public static final Color4F ALARM_BADGE_RED = SMView.MakeColor4F(0xFF3A2F, 1.0f);
    public static final Color4F ALARM_BADGE_RED_DIM = SMView.MakeColor4F(0xD53128, 1.0f);
    public static final Color4F MINT = SMView.MakeColor4F(0x64dbd5, 1.0f);

    public static final Color4F TOAST_RED = SMView.MakeColor4F(0xFF3A2F, 0.95f);
    public static final Color4F TOAST_GRAY = SMView.MakeColor4F(0xADAFB3, 0.95f);
    public static final Color4F TOAST_GREEN = SMView.MakeColor4F(0x64DBD5, 0.95f);
    public static final Color4F TOAST_BLUE = SMView.MakeColor4F(0x4399FA, 0.95f);

    public static final Color4F NEGATIVE_BUTTON_NORMAL = SMView.MakeColor4F(0xADAFB3, 1.0f);
    public static final Color4F NEGATIVE_BUTTON_PRESSED = SMView.MakeColor4F(0x9A9CA1, 1.0f);
    public static final Color4F POSITIVE_BUTTON_NORMAL = SMView.MakeColor4F(0x494949, 1.0f);
    public static final Color4F POSITIVE_BUTTON_PRESSED = SMView.MakeColor4F(0x373737, 1.0f);

}
