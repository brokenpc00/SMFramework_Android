package com.interpark.smframework.util;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.os.Build;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public final class MotionEventHelper {
    public static MotionEvent transformEvent(MotionEvent event, Matrix matrix) {
        // We try to use the new transform method if possible because it uses
        // less memory.
        if (Build.VERSION.SDK_INT >= 11) {
            return transformEventNew(event, matrix);
        } else {
            return transformEventOld(event, matrix);
        }
    }

    @TargetApi(11)
    private static MotionEvent transformEventNew(MotionEvent event, Matrix matrix) {
        MotionEvent newEvent = MotionEvent.obtain(event);

        newEvent.transform(matrix);

        return newEvent;
    }

    // This is copied from Input.cpp in the android framework.
    private static MotionEvent transformEventOld(MotionEvent event, Matrix matrix) {
        long downTime = event.getDownTime();
        long eventTime = event.getEventTime();
        int action = event.getAction();
        int pointerCount = event.getPointerCount();
        int[] pointerIds = getPointerIds(event);
        PointerCoords[] pointerCoords = getPointerCoords(event);
        int metaState = event.getMetaState();
        float xPrecision = event.getXPrecision();
        float yPrecision = event.getYPrecision();
        int deviceId = event.getDeviceId();
        int edgeFlags = event.getEdgeFlags();
        int source = event.getSource();
        int flags = event.getFlags();

        // Copy the x and y coordinates into an array, map them, and copy back.
        float[] xy = new float[pointerCoords.length * 2];
        for (int i = 0; i < pointerCount; i++) {
            xy[2*i  ] = pointerCoords[i].x;
            xy[2*i+1] = pointerCoords[i].y;
        }
        matrix.mapPoints(xy);

        for (int i = 0; i < pointerCount;i++) {
            pointerCoords[i].x = xy[2 * i];
            pointerCoords[i].y = xy[2 * i + 1];
            pointerCoords[i].orientation = transformAngle(matrix, pointerCoords[i].orientation);
        }

        @SuppressWarnings("deprecation")
        MotionEvent newEvent = MotionEvent.obtain(downTime, eventTime, action,
                pointerCount, pointerIds, pointerCoords, metaState, xPrecision,
                yPrecision, deviceId, edgeFlags, source, flags);

        return newEvent;
    }

    private static int[] getPointerIds(MotionEvent event) {
        int n = event.getPointerCount();
        int[] r = new int[n];

        for (int i = 0; i < n; i++) {
            r[i] = event.getPointerId(i);
        }

        return r;
    }

    private static PointerCoords[] getPointerCoords(MotionEvent event) {
        int n = event.getPointerCount();
        PointerCoords[] r = new PointerCoords[n];

        for (int i = 0; i < n; i++) {
            r[i] = new PointerCoords();
            event.getPointerCoords(i, r[i]);
        }

        return r;
    }

    private static float transformAngle(Matrix matrix, float angleRadians) {
        // Construct and transform a vector oriented at the specified clockwise
        // angle from vertical.  Coordinate system: down is increasing Y, right is
        // increasing X.
        float[] v = new float[2];

        v[0] = (float)Math.sin(angleRadians);
        v[1] = -(float)Math.cos(angleRadians);
        matrix.mapVectors(v);

        // Derive the transformed vector's clockwise angle from vertical.
        float result = (float) Math.atan2(v[0], -v[1]);

        if (result < -Math.PI / 2) {
            result += Math.PI;
        } else if (result > Math.PI / 2) {
            result -= Math.PI;
        }

        return result;
    }
}





