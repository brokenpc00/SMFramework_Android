package com.interpark.smframework.NativeImageProcess;

import android.graphics.Bitmap;

public class ImageProcessing {
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native String stringFromJNI();

    public static native void callTest();

    public static native void glGrabPixels(int x, int y, Bitmap bitmap, boolean zeroNonVisiblePixels);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

}
