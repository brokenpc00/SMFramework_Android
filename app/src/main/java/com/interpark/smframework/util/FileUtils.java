package com.interpark.smframework.util;

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


public class FileUtils {

    private static FileUtils _instance = null;

    public static FileUtils getInstance() {
        if (_instance==null) {
            _instance = new FileUtils();
        }

        return _instance;
    }

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

    public boolean isDirectoryExistInternal(String dirPath) {
        if (dirPath.isEmpty()) return false;

        return ClassHelper.isDirectoryExist(dirPath);
    }

    public boolean isAbsolutePath(String path) {
        return path.substring(0, 1).equals("/");
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
        if (!cacheDir.equals("")) {
            return cacheDir;
        }

        final String newFileName = getNewFileName(filename);

        String fullPath = "";

        for (String search : _searchPathArray) {
            for (String resolution : _searchResolutionsOrderArray) {
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

        return newFileName;
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
        if (filePath.isEmpty()) return false;

        return ClassHelper.isFileExist(filePath);
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

    public Status getContents(final String filename, byte[] data) {
        if (filename.isEmpty()) return Status.NotExists;

//        if (buffer==null) {
//            buffer = new ArrayList<>();
//        }

        String fullPath = FileUtils.getInstance().fullPathForFilename(filename);
        if (fullPath.isEmpty()) return Status.NotExists;

        File file = new File(fullPath);
        if (file==null) return Status.OpenFailed;

        if (!file.canRead()) return Status.ReadFailed;

        try {
            FileInputStream fis = new FileInputStream(file);
            int size = fis.available();
            if (data==null) {
                data = new byte[size];
            } else {
                if (size!=data.length) {
                    data = Arrays.copyOf(data, size);
                }
            }

            fis.read(data);
            fis.close();

            if (data.length<size) {
                data = Arrays.copyOf(data, data.length);
                return Status.ReadFailed;
            }

            return Status.OK;

        } catch (IOException e) {

        }

        return Status.ReadFailed;
    }

    public byte[] getDataFromFile(final String filename) {

        String fullPath = FileUtils.getInstance().fullPathForFilename(filename);
        File fs = new File(fullPath);
        try {
            FileInputStream fis = new FileInputStream(fs);
            byte[] data = new byte[fis.available()];
            fis.close();;

            getContents(filename, data);

            return data;

        } catch (IOException e) {

        }

        return null;
    }

    public String getStringFromFile(final String filename) {
        String fullPath = FileUtils.getInstance().fullPathForFilename(filename);
        File fs = new File(fullPath);
        try {
            FileInputStream fis = new FileInputStream(fs);
            byte[] data = new byte[fis.available()];
            fis.close();

            getContents(filename, data);

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
