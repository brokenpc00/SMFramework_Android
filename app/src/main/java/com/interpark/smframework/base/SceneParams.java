package com.interpark.smframework.base;

import android.graphics.Bitmap;

import java.util.HashMap;

public class SceneParams {
    private final HashMap<String, Object> mParams = new HashMap<String, Object>();
    private int mPopStackCount = 1;

    public boolean hasParam(String key) {
        return mParams.containsKey(key);
    }

    public void setPopStackCount(int stackCount) {
        mPopStackCount = stackCount;
        if (mPopStackCount < 1) {
            mPopStackCount = 1;
        }
    }

    public static SceneParams create() {
        return new SceneParams();
    }

    public int getPopStackCount() {
        return mPopStackCount;
    }

    public void putObject(String key, Object value) {
        mParams.put(key, value);
    }

    public void putString(String key, String value) {
        putObject(key, value);
    }

    public void putBitmap(String key, Bitmap value) {
        putObject(key, value);
    }

    public void putBoolean(String key, boolean value) {
        putObject(key, Boolean.valueOf(value));
    }


    public void putInt(String key, int value) {
        putObject(key, Integer.valueOf(value));
    }

    public void putLong(String key, long value) {
        putObject(key, Long.valueOf(value));
    }

    public void putFloat(String key, float value) {
        putObject(key, Float.valueOf(value));
    }

    public void putDouble(String key, double value) {
        putObject(key, Double.valueOf(value));
    }

    public Object getObject(String key) {
        return mParams.get(key);
    }

    public String getString(String key) {
        Object value = getObject(key);
        if (value != null && value instanceof String) {
            return (String)value;
        }
        return null;
    }

    public Bitmap getBitmap(String key) {
        Object value = getObject(key);
        if (value != null && value instanceof Bitmap) {
            return (Bitmap)value;
        }
        return null;
    }

    public boolean getBoolean(String key) {
        Object value = getObject(key);
        if (value != null && value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }
        return Boolean.FALSE;
    }

    public int getInt(String key) {
        Object value = getObject(key);
        if (value != null && value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        return Integer.MIN_VALUE;
    }

    public long getLong(String key) {
        Object value = getObject(key);
        if (value != null && value instanceof Long) {
            return ((Long)value).longValue();
        }
        return Long.MIN_VALUE;
    }

    public float getFloat(String key) {
        Object value = getObject(key);
        if (value != null && value instanceof Float) {
            return ((Float)value).floatValue();
        }
        return Float.MIN_VALUE;
    }

    public double getDouble(String key) {
        Object value = getObject(key);
        if (value != null && value instanceof Double) {
            return ((Double)value).doubleValue();
        }
        return Double.MIN_VALUE;
    }
}
