package com.interpark.smframework.util;

import android.util.Log;

import com.interpark.smframework.base.types.Timer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileManager {
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;

    public static final String DATA_ROOT_PATH = "Data/";
    public static final String IMAGE_ROOT = "Images/";
    public static final String DOC_ROOT = "Doc/";
    public static final String SNAPSHOT_ROOT = "SnapShot/";
    public static final String ZIP_ROOT = "Zip/";
    public static final String XML_ROOT = "XML/";
    public static final String PRELOAD_ROOT = "Preload/";
    public static final String DB_ROOT = "DB/";
    public static final String EPUB_DOWN_ROOT = "InterparkEBook/Downloads/";
    public static final String EPUB_EXTRACT_ROOT = "InterparkEBook/Extract/";



    public enum FileType {
        Image,
        Doc,
        SnapShot,
        ZIP,
        XML,
        Preload,
        DB,
        EPUB_DOWN,
        EPUB_EXTRACT
    }

    private static FileManager _instance = null;
    public static FileManager getInstance() {
        if (_instance==null) {
            _instance = new FileManager();
            _instance.init();
        }

        return _instance;
    }

    protected boolean init() {
        FileUtils fileUtils = FileUtils.getInstance();
        String rootPath = fileUtils.getWritablePath() + DATA_ROOT_PATH;

        if (!fileUtils.isDirectoryExist(rootPath)) {
            fileUtils.createDirectory(rootPath);
        }

        getFullPath(FileType.Image);
        getFullPath(FileType.Doc);
        getFullPath(FileType.SnapShot);
        getFullPath(FileType.ZIP);
        getFullPath(FileType.XML);
        getFullPath(FileType.Preload);
        getFullPath(FileType.DB);
        getFullPath(FileType.EPUB_DOWN);
        getFullPath(FileType.EPUB_EXTRACT);

        return true;
    }

    public String getFullFilePath(final FileType type, final String fileName) {
        return getFullPath(type) + fileName;
    }

    public String getLocalFilePath(final FileType type, final String fileName) {
        return getLocalPath(type) + fileName;
    }

    public String getFullPath(final FileType type) {
        FileUtils fileUtils = FileUtils.getInstance();
        String dir = fileUtils.getWritablePath() + getLocalPath(type);

        if (!fileUtils.isDirectoryExist(dir)) {
            if (fileUtils.isFileExist(dir)) {
                fileUtils.removeFile(dir);
            }

            fileUtils.createDirectory(dir);
        }

        return dir;
    }

    public String getLocalPath(final FileType type) {
        String baseName = "";
        switch (type) {
            case Image:
            {
                baseName = IMAGE_ROOT;
            }
            break;
            case Doc:
            {
                baseName = DOC_ROOT;
            }
            break;
            case SnapShot:
            {
                baseName = SNAPSHOT_ROOT;
            }
            break;
            case ZIP:
            {
                baseName = ZIP_ROOT;
            }
            break;
            case XML:
            {
                baseName = XML_ROOT;
            }
            break;
            case Preload:
            {
                baseName = PRELOAD_ROOT;
            }
            break;
            case DB:
            {
                baseName = DB_ROOT;
            }
            break;
            case EPUB_DOWN:
            {
                baseName = EPUB_DOWN_ROOT;
            }
            break;
            case EPUB_EXTRACT:
            {
                baseName = EPUB_EXTRACT_ROOT;
            }
            break;
        }

        return DATA_ROOT_PATH + baseName;
    }

    public boolean isFileEixst(final FileType type, final String fileName) {
        return FileUtils.getInstance().isFileExist(getFullFilePath(type, fileName));
    }

    public boolean writeToFile(final FileType type, final String fileName, byte[] buffer, int bufSize) {
        _mutex.lock();

        FileUtils fileUtils = FileUtils.getInstance();
        String fullPath = getFullFilePath(type, fileName);

        if (buffer==null || bufSize==0) {
            return false;
        }

        boolean bSuccess = fileUtils.writeDataToFile(buffer, fullPath);

        _mutex.unlock();

        return bSuccess;
    }

    public byte[] loadFromFile(final FileType type, final String fileName, int[] error) {
        FileUtils fileUtils = FileUtils.getInstance();
        String fullPath = getFullFilePath(type, fileName);

        error[0] = FAILED;

        if (fileUtils.isFileExist(fullPath)) {
            error[0] = SUCCESS;
            return fileUtils.getDataFromFile(fullPath);
        }

        return null;
    }

    public String loadStringFromFile(final FileType type, final String fileName, int[] error) {
        FileUtils fileUtils = FileUtils.getInstance();
        String fullPath = getFullFilePath(type, fileName);

        error[0] = FAILED;

        if (fileUtils.isFileExist(fullPath)) {
            error[0] = SUCCESS;
            return fileUtils.getStringFromFile(fullPath);
        }

        return "";
    }

    public boolean removeFile(final FileType type, final String fileName) {
        FileUtils fileUtils = FileUtils.getInstance();
        String fullPath = getFullFilePath(type, fileName);

        return fileUtils.removeFile(fullPath);
    }

    public boolean renameFile(final FileType type, final String oldName, final String newName) {
        FileUtils fileUtils = FileUtils.getInstance();

        return fileUtils.renameFile(oldName, newName);
    }

    public void clearCache(final FileType type) {
        ArrayList<String> fileList = getFileList(type);
        String pathName = getFullPath(type);
        FileUtils fs = FileUtils.getInstance();

        int numRemoved = 0;

        for (int i=0; i<fileList.size(); i++) {
            String name = fileList.get(i);
            if (!name.isEmpty()) {
                String fileFullPath = pathName + name;
                if (fs.removeFile(fileFullPath)) {
                    numRemoved++;
                }
            }
        }

        Log.i("FileManager", "[[[[[ file removed : " + numRemoved + " files...");
    }

    public String createSaveFileName() {
        return createSaveFileName("", "");
    }
    public String createSaveFileName(final String prefix) {
        return createSaveFileName(prefix, "");
    }
    public String createSaveFileName(final String prefix, final String postfix) {

        String timeFormat = AppUtil.getSaveFileTimeFormat();
        int rnd = (int)(Math.random() % 1000.0f);

        return prefix + "_" + timeFormat + "_" + rnd + "_" + postfix;
    }

    public String getFilePath(final String fullPath) {
        int pos = fullPath.lastIndexOf("/");
        return pos==-1?"":fullPath.substring(0, pos);
    }

    public String getFileName(final String fullPath) {
        return fullPath.substring(fullPath.lastIndexOf("/")+1);
    }

    public void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public boolean deleteFile(final String file) {
        FileUtils fs = FileUtils.getInstance();
        return fs.removeFile(file);
    }

    public boolean createFolder(final String path) {
        FileUtils fs = FileUtils.getInstance();
        return fs.createDirectory(path);
    }

    public int listFolder(final String path, ArrayList<String> entries, boolean directoryOnly) {
        FileUtils fs = FileUtils.getInstance();

        if (!fs.isDirectoryExist(path)) {
            return entries.size();
        }

        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                entries.add(file.getName());
            }
        }

        return entries.size();
    }

    private Lock _mutex = new ReentrantLock(true);

    private ArrayList<String> getFileList(final FileType type) {
        String pathName = getFullPath(type);

        ArrayList<String> fileList = new ArrayList<>();

        FileUtils fs = FileUtils.getInstance();

        if (!fs.isDirectoryExist(pathName)) {
            return fileList;
        }

        File dir = new File(pathName);
        File[] files = dir.listFiles();

        for (File file : files) {
            fileList.add(file.getName());
        }

        return fileList;
    }

}
