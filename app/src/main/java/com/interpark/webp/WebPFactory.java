package com.interpark.webp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.Log;

public final class WebPFactory {
    static {
        System.loadLibrary("webpdecoder");
    }

    /**
     * WebP의 width, height를 구한다
     *
     * @param data : Webp data
     * @param size : size[0] = width, size[1] = height
     *
     * @return 성공하면 true, 실패하면 false
     */
//    public static boolean decodeBounds(byte[] data, int[] size) {
//        if (data != null && size != null && size.length >= 2) {
//            if (nativeDecodeBounds(data, size)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * WebP decoding
     *
     * @param data : Webp data
     *
     * @return 디코딩된 bitmap, 실패하면 null 리턴
     */
//    public static Bitmap decodeByteArray(byte[] data) {
//        if (data == null)
//            return null;
//
//        Bitmap bitmap = null;
//        int[] dimen = new int[2];
//
//        if (nativeDecodeBounds(data, dimen)) {
//            if (dimen[0] > 0 && dimen[1] > 0) {
//                bitmap = Bitmap.createBitmap(dimen[0], dimen[1], Config.ARGB_8888);
//                if (!nativeDecodeIntoBitmap(data, bitmap)) {
//                    bitmap.recycle();
//                    bitmap = null;
//                }
//            }
//        }
//        if (bitmap == null) {
//            // 실패하면 BitmapFactory로 읽어본다.
////            Log.i("WebPFactory", "[[[[[ data length : " + data.length);
//            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
////            try {
////
////            } catch (Exception e) {
//////                Log.i("WebPFactory", "[[[[[ " + ;)
////                e.printStackTrace();
////            }
//
//        }
//
//        return bitmap;
//            }

    /**
     * WebP를 주어진 크기로 decoding
     *
     * @param data
     * @param scaledWidth : 디코딩할 bitmap의 width
     * @param scaledHeight : 디코딩할 bitmap의 height
     *
     * @return 디코딩된 bitmap, 실패하면 null 리턴
     */
    public static Bitmap decodeByteArrayScaled(byte[] data, int scaledWidth, int scaledHeight) {
        if (data == null || scaledWidth <= 0 || scaledHeight <= 0)
            return null;

        return decodeByteArray(data, scaledWidth, scaledHeight);
//        Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Config.ARGB_8888);
//        if (!nativeDecodeIntoBitmap(data, bitmap)) {
//            bitmap.recycle();
//            bitmap = null;
//        }
//
//        if (bitmap == null) {
//            // 실패하면 BitmapFactory로 읽어본다.
//            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            bitmap = getScaledBitmap(bitmap, scaledWidth, scaledHeight);
//        }
//
//        return bitmap;
    }

    /**
     * WebP decoding
     *
     * @param pathName : 디코드할 Webp 파일명
     *
     * @return 디코딩된 bitmap, 실패하면 null 리턴
     */
    public static Bitmap decodeFile(String pathName) {

        FileInputStream is = null;
        Bitmap bitmap = null;

        try {
            is = new FileInputStream(pathName);
            bitmap = decodeStream(is);
        } catch (Exception e) {
            // Does nothing
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // Does nothing
            }
        }

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeFile(pathName);
        }

        return bitmap;
    }

    /**
     * WebP를 주어진 크기로 decoding
     *
     * @param pathName : 디코드할 Webp 파일명
     * @param scaledWidth : 디코딩할 bitmap의 width
     * @param scaledHeight : 디코딩할 bitmap의 height
     *
     * @return 디코딩된 bitmap, 실패하면 null 리턴
     */
    public static Bitmap decodeFileScaled(String pathName, int scaledWidth, int scaledHeight) {

        FileInputStream is = null;
        Bitmap bitmap = null;

        try {
            is = new FileInputStream(pathName);
            return decodeStreamScaled(is, scaledWidth, scaledHeight);
        } catch (Exception e) {
            // Does nothing
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // Does nothing
            }
        }

        bitmap = BitmapFactory.decodeFile(pathName);
        bitmap = getScaledBitmap(bitmap, scaledWidth, scaledHeight);

        return bitmap;
    }

    /**
     * WebP decoding
     *
     * @param is : 디코드할 Webp FileInputStream
     *
     * @return 디코딩된 bitmap, 실패하면 null 리턴
     */
    public static Bitmap decodeStream(InputStream is) {
        Bitmap bitmap = null;
        try {
            bitmap = decodeByteArray(getByteArrayFromStream(is));
        } catch (IOException e) {
            // Does nothing
        }

        return bitmap;
    }

    /**
     * WebP를 주어진 크기로 decoding
     *
     * @param is : 디코드할 Webp FileInputStream
     * @param scaledWidth : 디코딩할 bitmap의 width
     * @param scaledHeight : 디코딩할 bitmap의 height
     *
     * @return 디코딩된 bitmap, 실패하면 null 리턴
     */
    public static Bitmap decodeStreamScaled(InputStream is, int scaledWidth, int scaledHeight) {

        try {
            return decodeByteArrayScaled(getByteArrayFromStream(is), scaledWidth, scaledHeight);
        } catch (IOException e) {
            // Does nothing
        }

        return null;
    }

    private static byte[] getByteArrayFromStream(InputStream is) throws IOException {

        if (is == null) {
            return null;
        }

        byte[] dataBytes = null;

        if (is instanceof FileInputStream) {
            FileChannel fileChannel = ((FileInputStream)is).getChannel();
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, fileChannel.size( ));

            dataBytes = new byte[mappedByteBuffer.remaining()];
            mappedByteBuffer.get(dataBytes);
        } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            int length;
            byte[] buffer = new byte[2048];
            while ((length = is.read(buffer)) >= 0) {
                os.write(buffer, 0, length);
            }
            os.close();

            dataBytes = os.toByteArray();
        }

        return dataBytes;
    }

    public static Bitmap decodeByteArray(byte[] data) {
        return decodeByteArray(data, 0, 0);
//        if (data == null)
//            return null;
//
//        Bitmap bitmap = null;
//        int[] dimen = new int[2];
//
//        if (nativeDecodeBounds(data, dimen)) {
//            if (dimen[0] > 0 && dimen[1] > 0) {
//                bitmap = Bitmap.createBitmap(dimen[0], dimen[1], Config.ARGB_8888);
//                if (!nativeDecodeIntoBitmap(data, bitmap)) {
//                    bitmap.recycle();
//                    bitmap = null;
//                }
//            }
//        }
//        if (bitmap == null) {
//            // 실패하면 BitmapFactory로 읽어본다.
////            Log.i("WebPFactory", "[[[[[ data length : " + data.length);
//            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
////            try {
////
////            } catch (Exception e) {
//////                Log.i("WebPFactory", "[[[[[ " + ;)
////                e.printStackTrace();
////            }
//
//        }
//
//        return bitmap;
    }

    public static Bitmap decodeByteArray(byte[] encoded, int w, int h) {
        int[] width = new int[]{w};
        int[] height = new int[]{h};

        byte[] decoded = decodeRGBAnative(encoded, encoded.length, width, height);
        int[] pixels;
        if (decoded.length == 0) {
            // Log.i("WEBP", "[[[[[[[[[[[[[[[[[[[[[[ decode faileld....... ]]]]]]]]]]]]]]]]]]");
            // return null;
            return BitmapFactory.decodeByteArray(encoded, 0, encoded.length);
//            pixels = new int[encoded.length / 4];
//            ByteBuffer.wrap(encoded).asIntBuffer().get(pixels);
//            return Bitmap.createBitmap(pixels, w, h, Config.ARGB_8888);
        } else {
            pixels = new int[decoded.length / 4];
            ByteBuffer.wrap(decoded).asIntBuffer().get(pixels);
            return Bitmap.createBitmap(pixels, width[0], height[0], Config.ARGB_8888);
        }

    }

//    private static native int[] nativeDecodeBuffer(byte[] data, int[] dimen);
//    private static native boolean nativeDecodeBounds(byte[] data, int[] dimen);
//    private static native boolean nativeDecodeIntoBitmap(byte[] data, Bitmap bitmap);
    public static native byte[] decodeRGBAnative(byte[] encoded, long encodedLength, int[] width, int[] height);

    private static Bitmap getScaledBitmap(Bitmap src, int width, int height) {
        Bitmap bitmap;

        if (src != null && (src.getWidth() != width || src.getHeight() != height)) {
            bitmap = Bitmap.createScaledBitmap(src, width, height, true);
            src.recycle();
        } else {
            bitmap = src;
        }

        return bitmap;
    }
}
