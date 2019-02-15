package com.interpark.smframework.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapLoader {
    private static final String Tag = "BitmapLoader";

    /**
     * 비트맵을 정해진 최대폭, 회전값 고려하여 읽는다.
     */
    public static Bitmap loadBitmap(Context context, final String path, final int degrees, final int reqWidth, final int reqHeight) {
        return new BitmapLoader().internalLoadBitmapExactly(context, path, degrees, reqWidth, reqHeight);
    }

    public static Bitmap resizeBitmap(final Bitmap src, final int degrees, final int reqWidth, final int reqHeight) {
        return new BitmapLoader().internalResizeBitmap(src, degrees, reqWidth, reqHeight);
    }

    public static Bitmap loadBitmapRoughly(Context context, final String path, final int degrees, final int reqWidth, final int reqHeight) {
        return new BitmapLoader().internalLoadRoughly(context, path, degrees, reqWidth, reqHeight);
    }


    private Bitmap internalLoadRoughly(Context context, final String path, final int degrees, final int reqWidth, final int reqHeight) {
        if (path == null)
            return null;

        Bitmap bitmap = null;
        InputStream is = null;

        int outWidth = 0;
        int outHeight = 0;
        Uri uri = Uri.fromFile(new File(path));

        if (uri == null)
            return null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            is = context.getContentResolver().openInputStream(uri);
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
                is = null;
            }
        }

        if (degrees == 90 || degrees == 270) {
            outWidth = options.outHeight;
            outHeight = options.outWidth;
        } else {
            outWidth = options.outWidth;
            outHeight = options.outHeight;
        }

        // 비트맵 로드
        options.inSampleSize = calculateInSampleSize(outWidth, outHeight, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        int retry = 0;
        while (bitmap == null && retry < 5) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(is, null, options);
                if (bitmap != null) {
//		        	Log.d(Tag, "PhotoSize : "+bitmap.getWidth()+"x"+bitmap.getHeight());
                    break;
                }
            } catch (OutOfMemoryError e) {
                System.gc();
            } catch (FileNotFoundException e) {
                break;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                    is = null;
                }
            }
            retry += 1;
            options.inSampleSize += 1;
        }

        if (bitmap != null && degrees != 0) {

            Matrix matrix = new Matrix();
            float width, height;
            if (degrees == 90 || degrees == 270) {
                width = bitmap.getHeight();
                height = bitmap.getWidth();
            } else {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
            }

            Bitmap rotated = null;
            retry = 0;
            float scale = 1;
            while (rotated == null && retry < 3) {
                try {
                    rotated = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.RGB_565);
                    if (rotated != null) {
                        matrix.reset();
                        matrix.postTranslate(-bitmap.getWidth()/2f, -bitmap.getHeight()/2f);
                        matrix.postRotate(degrees);
                        matrix.postScale(scale, scale);
                        matrix.postTranslate(width/2f, height/2f);

                        Canvas canvas = new Canvas(rotated);
                        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
                        paint.setDither(true);
                        canvas.drawBitmap(bitmap, matrix, paint);
                        break;
                    }
                } catch (OutOfMemoryError e) {
                    System.gc();
                }

                retry += 1;
                width /= 2f;
                height/= 2f;
                scale /= 2f;
            }

            if (rotated != null) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                bitmap = rotated;
            }
        }

        return bitmap;
    }

    private Bitmap internalResizeBitmap(Bitmap bitmap, final int degrees, final int reqWidth, final int reqHeight) {
        if (bitmap == null)
            return null;

        if (bitmap != null &&
                (degrees != 0 || bitmap.getWidth()*bitmap.getHeight() > reqWidth*reqHeight)) {

            Matrix matrix = new Matrix();
            float width, height;
            if (degrees == 90 || degrees == 270) {
                width = bitmap.getHeight();
                height = bitmap.getWidth();
            } else {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
            }

            Bitmap rotated = null;
            float scale = Math.max((float)reqWidth/width, (float)reqHeight/height);
            int retry = 0;
            width = reqWidth;
            height = reqHeight;
            while (rotated == null && retry < 3) {
                try {
                    rotated = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
                    if (rotated != null) {
                        matrix.reset();
                        matrix.postTranslate(-bitmap.getWidth()/2f, -bitmap.getHeight()/2f);
                        matrix.postRotate(degrees);
                        matrix.postScale(scale, scale);
                        matrix.postTranslate(width/2f, height/2f);

                        Canvas canvas = new Canvas(rotated);
                        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
                        paint.setDither(true);
                        canvas.drawBitmap(bitmap, matrix, paint);
                        break;
                    }
                } catch (OutOfMemoryError e) {
                    System.gc();
                }

                retry += 1;
                width /= 2f;
                height/= 2f;
                scale /= 2f;
            }

            if (rotated != null && rotated != bitmap) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                bitmap = rotated;
                rotated = null;
            }
        }

        return bitmap;
    }


    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {

        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


    private Bitmap internalLoadBitmapExactly(Context context, final String pathName, final int degrees, final int reqWidth, final int reqHeight) {
        if (pathName == null)
            return null;

        Bitmap bitmap = null;

        int outWidth = 0;
        int outHeight = 0;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        if (degrees == 90 || degrees == 270) {
            outWidth = options.outHeight;
            outHeight = options.outWidth;
        } else {
            outWidth = options.outWidth;
            outHeight = options.outHeight;
        }

        // 비트맵 로드
//    	Log.d(Tag, "Original size : "+outWidth+"x"+outHeight);
        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.max(1, Math.max(outWidth/reqWidth, outHeight/reqHeight));
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        int retry = 0;
        while (bitmap == null && retry < 5) {
            try {
                bitmap = BitmapFactory.decodeFile(pathName, options);
                if (bitmap != null) {
//		        	Log.d(Tag, "Resample size: "+bitmap.getWidth()+"x"+bitmap.getHeight());
                    break;
                }
            } catch (OutOfMemoryError e) {
                System.gc();
            }
            retry += 1;
            options.inSampleSize += 1;
        }

        if (bitmap != null &&
                (degrees != 0 || bitmap.getWidth()*bitmap.getHeight() > reqWidth*reqHeight)) {

            Matrix matrix = new Matrix();
            float width, height;
            if (degrees == 90 || degrees == 270) {
                width = bitmap.getHeight();
                height = bitmap.getWidth();
            } else {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
            }

            Bitmap rotated = null;
            float scaleX = (float)reqWidth/width;
            float scaleY = (float)reqHeight/height;
            float scale = Math.max(scaleX, scaleY);
            retry = 0;
            width = reqWidth;
            height = reqHeight;
            while (rotated == null && retry < 3) {
                try {
                    rotated = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
                    if (rotated != null) {
                        matrix.reset();
                        matrix.postTranslate(-bitmap.getWidth()/2f, -bitmap.getHeight()/2f);
                        matrix.postRotate(degrees);
                        matrix.postScale(scale, scale);
                        matrix.postTranslate(width/2f, height/2f);

                        Canvas canvas = new Canvas(rotated);
                        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
                        paint.setDither(true);
                        canvas.drawBitmap(bitmap, matrix, paint);
//			        	Log.d(Tag, "Resize to : "+width+"x"+height);
                        break;
                    }
                } catch (OutOfMemoryError e) {
                    System.gc();
                }

                retry += 1;
                width /= 2f;
                height/= 2f;
                scaleX /= 2f;
                scaleY /= 2f;
            }

            if (rotated != null) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                bitmap = rotated;
            }
        }

        return bitmap;
    }
}
