package com.interpark.smframework;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.interpark.app.MainActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassHelper {

    private static final String PREFS_NAME = "SMFrameWorkPrefFile";
    private static final int RUNNABLES_PER_FRAME = 5;
    private static final String TAG = ClassHelper.class.getSimpleName();


    private static AssetManager sAssetManager;
    public static AssetManager getAssetManager() {return sAssetManager;}


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

    public static String getAssetsPath()
    {
        if (ClassHelper.sAssetsPath.equals("")) {

            String pathToOBB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/obb/" + ClassHelper.sPackageName;

            // Listing all files inside the folder (pathToOBB) where OBB files are expected to be found.
            String[] fileNames = new File(pathToOBB).list(new FilenameFilter() { // Using filter to pick up only main OBB file name.
                public boolean accept(File dir, String name) {
                    return name.startsWith("main.") && name.endsWith(".obb");  // It's possible to filter only by extension here to get path to patch OBB file also.
                }
            });

            String fullPathToOBB = "";
            if (fileNames != null && fileNames.length > 0)  // If there is at least 1 element inside the array with OBB file names, then we may think fileNames[0] will have desired main OBB file name.
                fullPathToOBB = pathToOBB + "/" + fileNames[0];  // Composing full file name for main OBB file.

            File obbFile = new File(fullPathToOBB);
            if (obbFile.exists())
                ClassHelper.sAssetsPath = fullPathToOBB;
            else
                ClassHelper.sAssetsPath = ClassHelper.sActivity.getApplicationInfo().sourceDir;
        }

        return ClassHelper.sAssetsPath;
    }

    public static ZipResourceFile getObbFile()
    {
        return ClassHelper.sOBBFile;
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
