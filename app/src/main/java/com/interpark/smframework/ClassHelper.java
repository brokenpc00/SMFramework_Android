package com.interpark.smframework;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.interpark.app.MainActivity;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassHelper {

    private static final String PREFS_NAME = "SMFrameWorkPrefFile";
    private static final int RUNNABLES_PER_FRAME = 5;
    private static final String TAG = ClassHelper.class.getSimpleName();


    private static AssetManager sAssetManager;


//    private static Accelerometer sAccelerometer;
    private static boolean sAccelerometerEnabled;
    private static boolean sCompassEnabled;
    private static boolean sActivityVisible;
    private static String sPackageName;
    private static String sFileDirectory;
    private static Activity sActivity = null;

    private static HelperListener sHelperListener;
    private static Set<PreferenceManager.OnActivityResultListener> onActivityResultListeners = new LinkedHashSet<PreferenceManager.OnActivityResultListener>();

    private static Vibrator sVibrateService = null;

    private static final int BOOST_TIME = 7;

    private static String sAssetsPath = "";

    private static ZipResourceFile sOBBFile = null;

    public static Activity getActivity() {
        return sActivity;
    }


    private static boolean sInited = false;
    public static void init(final Activity activity) {
        sActivity = activity;
        ClassHelper.sHelperListener = (HelperListener)activity;

        if (!sInited) {

            PackageManager pm = activity.getPackageManager();

            boolean isSupportLowLatency = pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);

            int sampleRate = 44100;
            int bufferSizeInFrames = 192;


//            ClassHelper.sPackageName = applicationInfo.packageName;
            ClassHelper.sFileDirectory = activity.getFilesDir().getAbsolutePath();


//            ClassHelper.nativeSetApkPath(ClassHelper.getAssetsPath());

//            ClassHelper.sAccelerometer = new SMAccelerometer(activity);
//            ClassHelper.sMusic = new SMMusic(activity);
//            ClassHelper.sSound = new SMSound(activity);
            ClassHelper.sAssetManager = activity.getAssets();
//            ClassHelper.nativeSetContext((Context)activity, Cocos2dxHelper.sAssetManager);

            sInited = true;
        }
    }

    public static boolean isDirectoryExist(String strPath) {
        File file = new File(strPath);
        return file.isDirectory();
    }

    public static boolean isFileExist(String strPath) {
        File file = new File(strPath);
        return file.isFile();

    }

    public static String getWritablePath() {
        return ClassHelper.sFileDirectory;
    }



    public static void runOnGLThread(final Runnable r) {
        ((MainActivity)sActivity).runOnGLThread(r);
    }

    public static interface HelperListener {
        public void showDialog(final String pTitle, final String pMessage);

        public void runOnGLThread(final Runnable pRunnable);
    }

}
