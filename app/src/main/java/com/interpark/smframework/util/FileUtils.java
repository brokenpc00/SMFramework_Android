package com.interpark.smframework.util;

import android.content.res.AssetManager;

import com.interpark.smframework.ClassHelper;
import com.interpark.smframework.SMDirector;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.util.Value.ValueMap;
import com.interpark.smframework.util.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FileUtils {

    private static FileUtils _instance = null;
    private static String ASSETS_FOLDER_NAME = "assets/";
    private static int ASSETS_FOLDER_NAME_LENGTH = 7;
    private static AssetManager assetsmanager = null;
    private static ZipFile obbfile = null;



    public static FileUtils getInstance() {
        if (_instance==null) {
            _instance = new FileUtils();
            _instance.init();
        }

        return _instance;
    }

    public boolean init() {

        _defaultResRootPath = ASSETS_FOLDER_NAME;
        assetsmanager = ClassHelper.getAssetManager();

        String assetsPath = ClassHelper.getAssetsPath();

        if (assetsPath.contains("/obb/")) {
            try {
                obbfile = new ZipFile(assetsPath);
            } catch (IOException e) {

            }
        }

        _searchPathArray.add(_defaultResRootPath);
        _searchResolutionsOrderArray.add("");

        return true;
    }

    protected String _storagePath = "";
    protected String _writablePath = "";
    protected String _defaultResRootPath = "";
    protected HashMap<String, String> _fullPathCache = new HashMap<>();
    protected ArrayList<String> _originalSearchPaths = new ArrayList<>();
    protected ArrayList<String> _searchPathArray = new ArrayList<>();
    protected ArrayList<String> _searchResolutionsOrderArray = new ArrayList<>();

    public String getWritablePath() {
        StringBuffer dir = new StringBuffer("");

        String tmp = ClassHelper.getWritablePath();
        if (tmp.length()>0) {
            dir.append(tmp).append("/");
            return dir.toString();
        } else {
            return "";
        }
    }

    public void setSearchPaths(final ArrayList<String> searchPaths) {

        boolean existDefaultRootPath = false;
        _originalSearchPaths = searchPaths;

        _fullPathCache.clear();
        _searchPathArray.clear();

        for (String path : _originalSearchPaths) {

            String prefix = "";
            String fullPath = "";

            if (!isAbsolutePath(path)) { // Not an absolute path
                prefix = _defaultResRootPath;
            }

            fullPath = prefix + path;
            if (!path.isEmpty() && !path.substring(path.length()-1).equals("/")) {
                fullPath += "/";
            }
            if (!existDefaultRootPath && path.compareTo(_defaultResRootPath)==0) {
                existDefaultRootPath = true;
            }

            _searchPathArray.add(fullPath);
        }

        if (!existDefaultRootPath)
        {
            //CCLOG("Default root path doesn't exist, adding it.");
            _searchPathArray.add(_defaultResRootPath);
        }
    }

    public boolean isDirectoryExistInternal(String dirPath) {
        if (dirPath.isEmpty()) return false;

        return ClassHelper.isDirectoryExist(dirPath);
    }

    public boolean isAbsolutePath(String path) {
        // On Android, there are two situations for full path.
        // 1) Files in APK, e.g. assets/path/path/file.png
        // 2) Files not in APK, e.g. /data/data/org.cocos2dx.hellocpp/cache/path/path/file.png, or /sdcard/path/path/file.png.
        // So these two situations need to be checked on Android.
        if (path.substring(0, 1).equals("/") || path.contains(_defaultResRootPath))
        {
            return true;
        }
        return false;
//        return path.substring(0, 1).equals("/");
    }

    public boolean isDirectoryExist(String dirPath) {
        if (isAbsolutePath(dirPath)) {
            return isDirectoryExistInternal(dirPath);
        }

        String cacheDir = _fullPathCache.get(dirPath);
        if (!cacheDir.equals("")) {
            return isDirectoryExistInternal(cacheDir);
        }

        String fullPath = "";

        for (final String search : _searchPathArray) {
            for (final String resolution : _searchResolutionsOrderArray) {
                fullPath = fullPathForFilename(search + dirPath + resolution);
                if (isDirectoryExistInternal(fullPath)) {
                    _fullPathCache.put(dirPath, fullPath);
                    return true;
                }
            }
        }

        return false;
    }

    public String fullPathForFilename(final String filename) {
        if (filename.isEmpty()) {
            return "";
        }

        if (isAbsolutePath(filename)) {
            return filename;
        }

        String cacheDir = _fullPathCache.get(filename);
        if (cacheDir!=null && !cacheDir.equals("")) {
            return cacheDir;
        }

        final String newFileName = getNewFileName(filename);

        String fullPath = "";

        for (String search : _searchPathArray) {
            for (String resolution : _searchResolutionsOrderArray) {
                fullPath = this.getPathForFilename(newFileName, resolution, search);
                if (!fullPath.isEmpty()) {
                    _fullPathCache.put(filename, fullPath);
                    return fullPath;
                }
            }
        }

        return "";
    }

    protected ValueMap _filenameLookupDict = new ValueMap();
    public String getNewFileName(final String filname) {
        String newFileName = "";
        Value value = _filenameLookupDict.get(filname);
        if (value==null) {
            newFileName = filname;
        } else {
            newFileName = value.getString();
        }

        int pos = newFileName.indexOf("../");
        if (pos>=0) {
            // first or not found
        return newFileName;
    }

        ArrayList<String> v = new ArrayList<>(3);

        boolean change = false;

        int size = newFileName.length();
        int idx = 0;

        boolean noexit = true;

        while (noexit) {
            pos = newFileName.indexOf("/", idx);
            String tmp = "";
            if (pos==-1) {
//                tmp = newFileName.substring(idx, size-idx);
                tmp = newFileName.substring(idx);
                noexit = false;
            } else {
                tmp = newFileName.substring(idx, pos+1);
            }

            int t = v.size();
            if (t>0 && !v.get(t-1).contains("../") && (tmp.contains("../") || tmp.contains(".."))) {
                v.remove(v.size()-1);
                change = true;
            } else {
                v.add(tmp);
            }
            idx = pos + 1;
        }

        if (change) {
            newFileName = "";
            for (String s : v) {
                newFileName += s;
            }
        }

        return newFileName;
    }

    public String getPathForFilename(final String fileName, final String resolutionDirectory, final String searchPath) {
        String file = fileName;
        String file_path = "";
        int pos = fileName.lastIndexOf("/");
        if (pos!=-1) {
            file_path = fileName.substring(0, pos+1);
            file = fileName.substring(pos+1);
        }

        String path = searchPath;
        path += file_path;
        path += resolutionDirectory;

        path = getFullPathForDirectoryAndFilename(path, file);

        return path;
    }

    public String getFullPathForDirectoryAndFilename(final String directory, final String filename) {
        String ret = directory;
        if (directory.length()>0) {
            String lastChar = directory.substring(directory.length()-1);
            if (!lastChar.equals("/")) {
                ret += "/";
            }
        }
        ret += filename;


        if (!isFileExistInternal(ret)) {
            ret ="";
        }

        return ret;
    }

    public interface VOID_BOOLEAN_CALLBACK {
        public void func(boolean b);
    }

    public boolean createDirectory(final String path) {
        if (isDirectoryExist(path)) return true;

        int start = 0;
        int found = path.indexOf("/\\", start);
        String subPath;
//        StringBuffer dirs;
        ArrayList<String> dirs = new ArrayList<>();

        if (found!=-1) {
            while (true) {
                subPath = path.substring(start, found - start+1);
                if (!subPath.isEmpty()) {
                    dirs.add(subPath);
                }

                start = found+1;
                found = path.indexOf("/\\", start);
                if (found==-1) {
                    if (start<path.length()) {
                        dirs.add(path.substring(start));
                    }
                    break;
                }
            }
        }

        File dir = null;

        subPath = "";
        for (final String a : dirs) {
            subPath += a;
            dir = new File(subPath);
            if (dir.exists()) {
                return false;
            }
            dir.mkdir();
        }

        return true;
    }

//    public void createDirectory(final String dirPath, final VOID_BOOLEAN_CALLBACK callback) {
//        SMDirector.getDirector().getScheduler().performFunctionInMainThread(new PERFORM_SEL() {
//            @Override
//            public void performSelector() {
//                boolean suceess = FileUtils.getInstance().removeDirectory(dirPath);
//                callback.func(suceess);
//            }
//        });
//    }



    public boolean removeDirectory(final String dirPath) {
        File dir = new File(dirPath);
        return dir.delete();
    }

    public boolean isFileExistInternal(final String filePath) {

        if (filePath.isEmpty()) {
            return false;
        }

        boolean bFound = false;

        if (!filePath.substring(0, 1).equals("/")) {
            int s = 0;

            if (filePath.indexOf(_defaultResRootPath)==0) s += _defaultResRootPath.length();

            String obbStr = filePath.substring(s);

            if (obbfile!=null && obbStr!=null && obbStr.length()>0) {
                bFound = true;
            } else if (assetsmanager!=null) {
                try {
                    InputStream is = assetsmanager.open(obbStr);
                    if (is != null) {
                        bFound = true;
                        is.close();
                    }
                } catch (IOException e) {

                }
            }
        } else {
            File file = new File(filePath);
            if (file!=null && file.exists()) {
                bFound = true;
            }
        }

        return bFound;
    }

    public boolean isFileExist(final String filename) {
        if (isAbsolutePath(filename)) {
            return isFileExistInternal(filename);
        } else {
            String fullPath = fullPathForFilename(filename);
            if (fullPath.isEmpty()) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean writeDataToFile(final byte[] data, final String fullPath) {

        File file = new File(fullPath);
        if (file.canWrite()) {
            try {
                if (file.createNewFile()) {
                    FileOutputStream stream = new FileOutputStream(file);
                    stream.write(data);
                    stream.close();
                    return true;
                }
            } catch (IOException e) {

            }
        }
        return false;
    }


    public boolean writeStringToFile(final String dataStr, final String fulPath) {
        return writeDataToFile(dataStr.getBytes(), fulPath);
    }

    public enum Status {
        OK,
        NotExists,
        OpenFailed,
        ReadFailed,
        NotInitialized,
        TooLarge,
        ObtainSizeFailed
    }

    private static String apkprefix = "assets/";

    private byte[] getInternalPathContents(final String fullPath) {

        String relativePath = "";
        int position = fullPath.indexOf(apkprefix);

        if (0 == position) {
            // "assets/" is at the beginning of the path and we don't want it
            relativePath += fullPath.substring(apkprefix.length());
            } else {
            relativePath = fullPath;
            }

        if (obbfile!=null)
        {
            ZipEntry entry = obbfile.getEntry(relativePath);
            return entry.getExtra();
        }

        if (null == assetsmanager) {
            return null;
            }


        try {
            InputStream is = assetsmanager.open(relativePath);

            byte[] data = new byte[is.available()];

            is.read(data);
            is.close();

            return data;
        } catch (IOException e) {

        }

        return null;
    }

    private byte[] getAbsolutePathContents(final String filename) {

        if (filename.isEmpty()) {
            return null;
        }

        FileUtils fs = FileUtils.getInstance();

        String fullPath = fs.fullPathForFilename(filename);

        if (fullPath.isEmpty())
            return null;

        File fp = new File(fullPath);

        if (!fp.canRead()) return null;

        try {
            FileInputStream fis = new FileInputStream(fp);
            int size = fis.available();
            byte[] data = new byte[size];

            fis.read(data);
            fis.close();

            return data;

        } catch (IOException e) {

        }

        return null;
    }


    public byte[] getContents(final String filename) {
        if (filename.isEmpty()) return null;

        String fullPath = fullPathForFilename(filename);

        if (fullPath.substring(0, 1).equals("/")) {
            return getAbsolutePathContents(fullPath);
        } else {
            return getInternalPathContents(fullPath);
        }

    }

    public byte[] getDataFromFile(final String fullPath) {
        return getContents(fullPath);
    }

    public String getStringFromFile(final String filename) {
        String fullPath = FileUtils.getInstance().fullPathForFilename(filename);
        File fs = new File(fullPath);
        try {
            FileInputStream fis = new FileInputStream(fs);
            byte[] data = getContents(filename);

            return new String(data);

        } catch (IOException e) {

        }

        return null;
    }

    public boolean removeFile(final String path) {
        String fullPath = FileUtils.getInstance().fullPathForFilename(path);
        File fs = new File(fullPath);

        if (fs.exists()) {
            return fs.delete();
        }
        return false;
    }

    public boolean renameFile(final String oldFullPath, final String newFullPath) {
        File src = new File(oldFullPath);
        if (!src.exists()) {
            return false;
        }

        File dst = new File(newFullPath);
        return src.renameTo(dst);
    }

}
