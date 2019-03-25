package com.interpark.smframework.util.ImageManager;

import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.interpark.smframework.util.ImageManager.CameraHelper.CameraCaps;
import com.interpark.smframework.util.ImageManager.CameraHelper.CameraHelperImpl;
import com.interpark.smframework.util.ImageManager.CameraHelper.CameraInfo2;


public class CameraHelperGB implements CameraHelperImpl {
    @Override
    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    @Override
    public Camera openCamera(final int id) {
        Camera camera = null;
        int retry = 5;
        do {
            try {
                camera = Camera.open(id);
            } catch (Exception e) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    // Does nothing
                }
            }
        } while (camera == null && --retry > 0);
        return camera;
    }


    @Override
    public boolean hasCamera(final int facing) {
        return getCameraId(facing) != -1;
    }

    @Override
    public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        Log.i("HELPER GB", "[[[[[ get Camera Info : " + Integer.toString(cameraId) + ", camera facing : " + Integer.toString(info.facing));
        cameraInfo.facing = info.facing;
        cameraInfo.orientation = info.orientation;
    }

    private int getCameraId(final int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }
        return -1;
    }


    @Override
    public void prepareCameraCaps(Camera camera, CameraCaps caps) {

        Camera.Parameters params = camera.getParameters();

        List<String> focusModeEnum = params.getSupportedFocusModes();
        caps.mFocusModeAuto = isSupported(Camera.Parameters.FOCUS_MODE_AUTO, focusModeEnum);

        if (Build.VERSION.SDK_INT >= 16) {
            caps.mFocusModeContinuousPicture = isSupported(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, focusModeEnum);
            caps.mFocusModeContinuousVideo   = isSupported(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, focusModeEnum);
        } else {
            caps.mFocusModeContinuousPicture = false;
            caps.mFocusModeContinuousVideo = false;
        }

        if (Build.VERSION.SDK_INT >= 14) {
            caps.mSupportFocusArea = params.getMaxNumFocusAreas() > 0;
            caps.mSupportMeteringArea = params.getMaxNumMeteringAreas() > 0;
            caps.mSupportFaceDetection = params.getMaxNumDetectedFaces() > 0;
        } else {
            caps.mSupportFocusArea = false;
            caps.mSupportMeteringArea = false;
            caps.mSupportFaceDetection = false;
        }

        List<String> flashModeEnum = params.getSupportedFlashModes();
        caps.mFlashModeAuto = isSupported(Camera.Parameters.FLASH_MODE_AUTO, flashModeEnum);
        caps.mFlashModeOn   = isSupported(Camera.Parameters.FLASH_MODE_ON, flashModeEnum);
        caps.mFlashModeOff  = isSupported(Camera.Parameters.FLASH_MODE_OFF, flashModeEnum);

        caps.mZoomSupported = params.isZoomSupported();
        // the zoom ratios in 1/100 increments. Ex: a zoom of 3.2x is returned as 320.
        // The number of elements is getMaxZoom + 1.
        // The list is sorted from small to large.
        // The first element is always 100.
        // The last element is the zoom ratio of the maximum zoom value.
        caps.mZoomRatios = null;
        if (caps.mZoomSupported) {
            caps.mZoomRatios = params.getZoomRatios();
        }
        if (caps.mZoomRatios != null) {
            caps.mMaxZoom = Math.min(caps.mZoomRatios.size()-1, params.getMaxZoom());
        } else {
            caps.mZoomRatios = new ArrayList<>();
            caps.mZoomRatios.add(100);
            caps.mMaxZoom = 0;
        }

        List<String> whiteBalanceEnum = params.getSupportedWhiteBalance();
        caps.mWhiteBalanceAuto = isSupported(Camera.Parameters.WHITE_BALANCE_AUTO, whiteBalanceEnum);

        List<String> sceneModeEnum = params.getSupportedSceneModes();
        caps.mSceneModeAuto = isSupported(Camera.Parameters.SCENE_MODE_AUTO, sceneModeEnum);
    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }
}

