package com.interpark.smframework.util.ImageManager;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;

import java.util.List;

public class CameraHelper {
    private final CameraHelperImpl mImpl;
    private final CameraCaps mCaps;

    public class CameraCaps {
        boolean mFocusModeAuto;
        boolean mFocusModeContinuousPicture;
        boolean mFocusModeContinuousVideo;

        boolean mSupportFocusArea;
        boolean mSupportMeteringArea;
        boolean mSupportFaceDetection;

        boolean mFlashModeAuto;
        boolean mFlashModeOn;
        boolean mFlashModeOff;

        boolean mZoomSupported;
        List<Integer> mZoomRatios;
        int mMaxZoom;

        boolean mWhiteBalanceAuto;
        boolean mSceneModeAuto;

        boolean mMeteringAreaSupported;


        public boolean isAutoFocusSupported() {
            return mFocusModeAuto;
        }

        public boolean isFocusAreaSupported() {
            return mSupportFocusArea;
        }

        public boolean isMeteringAreaSupported() {
            return mSupportMeteringArea;
        }

        public boolean isFaceDetectionSupported() {
            return mSupportFaceDetection;
        }

        public boolean isFlashSupported() {
            return mFlashModeAuto || mFlashModeOn;
        }

        public boolean isZoomSupported() {
            return mZoomSupported;
        }

        public int getMaxZoom() {
            return mMaxZoom;
        }

        public List<Integer> getZoomRatio() {
            return mZoomRatios;
        }

    }

    public CameraHelper(final Context context) {
        mImpl = new CameraHelperGB();
        mCaps = new CameraCaps();
    }

    public interface CameraHelperImpl {
        int getNumberOfCameras();

        Camera openCamera(int id);

        boolean hasCamera(int cameraFacingFront);

        void getCameraInfo(int cameraId, CameraInfo2 cameraInfo);

        void prepareCameraCaps(Camera camera, CameraCaps caps);
    }

    public int getNumberOfCameras() {
        return mImpl.getNumberOfCameras();
    }

    public Camera openCamera(final int id) {
        Camera camera = mImpl.openCamera(id);

        if (camera != null) {
            prepareCameraCaps(camera);
        }

        return camera;
    }

    public boolean hasFrontCamera() {
        return mImpl.hasCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public boolean hasBackCamera() {
        return mImpl.hasCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        mImpl.getCameraInfo(cameraId, cameraInfo);
    }

    private void prepareCameraCaps(Camera camera) {
        mImpl.prepareCameraCaps(camera, mCaps);
    }

    public void setCameraDisplayOrientation(final Context context,
                                            final int cameraId, final Camera camera) {
        int result = getCameraDisplayOrientation(context, cameraId);
        camera.setDisplayOrientation(result);
    }

    public int getCameraDisplayOrientation(Context context, final int cameraId) {
        Activity activity = (Activity)context;
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        CameraInfo2 info = new CameraInfo2();
        getCameraInfo(cameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static class CameraInfo2 {
        public int facing;
        public int orientation;
    }

    public boolean isSupportAutoFocus() {
        return mCaps.mFocusModeAuto;
    }

    public boolean isSupportContinuousFocus() {
        return mCaps.mFocusModeContinuousPicture || mCaps.mFocusModeContinuousVideo;
    }

    public boolean isSupportFaceDetection() {
        return mCaps.mSupportFaceDetection;
    }

    public void setFocusModeContinuous(Camera.Parameters params) {
        if (mCaps.mFocusModeContinuousPicture) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (mCaps.mFocusModeContinuousVideo) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
    }

    public void setFocusModeAuto(Camera.Parameters params) {
        if (mCaps.mFocusModeAuto) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
    }

    public boolean isSupportFlash() {
        return mCaps.mFlashModeAuto || mCaps.mFlashModeOn;
    }

    public boolean isSupportFlashAuto() {
        return mCaps.mFlashModeAuto;
    }

    public boolean isSupportFlashOn() {
        return mCaps.mFlashModeOn;
    }

    public boolean isSupportZoom() {
        return mCaps.mZoomSupported;
    }

    public boolean isSupportFocusArea() {
        return mCaps.mSupportFocusArea;
    }

    public boolean isSupportMeteringArea() {
        return mCaps.mSupportMeteringArea;
    }

    public int getMaxZoom() {
        return mCaps.mMaxZoom;
    }

    public void setZoomValue(Camera.Parameters params, int zoomValue) {
        if (mCaps.mZoomSupported && zoomValue <= mCaps.mMaxZoom) {
            params.setZoom(zoomValue);
        }
    }

    public CameraCaps getCameraCaps() {
        return mCaps;
    }

    public boolean isSupportAutoWhiteBalance() {
        return mCaps.mWhiteBalanceAuto;
    }

    public boolean isSupportAutoSceneMode() {
        return mCaps.mSceneModeAuto;
    }

}
