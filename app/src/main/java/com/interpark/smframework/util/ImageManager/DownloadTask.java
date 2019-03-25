package com.interpark.smframework.util.ImageManager;

import android.graphics.Bitmap;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.SMDirector;
import com.interpark.smframework.network.Downloader.AndroidDownloader;
import com.interpark.smframework.network.Downloader.Downloader;
import com.interpark.smframework.util.AppUtil;
import com.interpark.smframework.util.FileManager;
import com.interpark.smframework.util.FileUtils;
import com.interpark.smframework.util.cache.ImageCacheEntry;
import com.interpark.smframework.util.cache.MemoryCacheEntry;
import com.interpark.smframework.util.cache.MemoryLRUCache;
import com.interpark.webp.WebPFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadTask {

    public enum MediaType {
        NETWORK,
        RESOURCE,
        FILE,
        THUMB
    }

    private static int __task_count__ = 0;

    public static DownloadTask createTaskForTarget(ImageDownloader downloader, DownloadProtocol target) {
        DownloadTask task = new DownloadTask();
        task._targetRef = new WeakReference<>(target);
        task._downloader = downloader;

        return task;
    }

    public DownloadTask() {
        _taskId = 0x100 + __task_count__++;
    }

    @Override
    public void finalize() throws Throwable {
        try {
            --__task_count__;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("All done!");
            super.finalize();
        }
    }


    public static String makeCacheKey(MediaType type, final String requestPath, DownloadConfig config, StringBuffer keyPath) {
        String key = "";

        switch (type) {
            case NETWORK:
            {
                key = requestPath.substring(0, requestPath.indexOf("?"));
            }
            break;
            case RESOURCE:
            {
                // asset?
                key = "@RES:" + requestPath;
            }
            break;
            case FILE:
            {
                key = "@FILE:" + requestPath;
            }
            break;
            case THUMB:
            {
                key = "@THUMB:" + requestPath;
            }
            break;
        }

        String resampleKey = "";
        switch (config._resamplePolicy) {
            case EXACT_FIT:
            {
                resampleKey = "_@EXACT_FIT_" + config._resParam1 + "x" + config._resParam2;
            }
            break;
            case EXACT_CROP:
            {
                resampleKey = "_@EXACT_CROP_" + config._resParam1 + "x" + config._resParam2;
            }
            break;
            case AREA:
            {
                resampleKey = "_@AREA_" + config._resParam1 + "x" + config._resParam2;
            }
            break;
            case LONGSIDE:
            {
                resampleKey = "_@LONGSIDE_" + config._resParam1 + "x" + config._resParam2;
            }
            break;
            case SCALE:
            {
                resampleKey = "_@SCALE_" + config._resParam1;
            }
            break;
        }
        key += resampleKey;

        if (keyPath!=null) {
            keyPath.append(key);
        }

        return new String(AppUtil.getMD5(key.getBytes()));
    }


    public void init(MediaType type, final String requestPath, DownloadConfig config) {
        _type = type;
        _requestPath = requestPath;

        if (config==null) {
            _config.setCachePolicy(DownloadConfig.CachePolycy.DEFAULT);
        } else {
            _config = config;
        }

        StringBuffer keypath = new StringBuffer(_keyPath);
        _cacheKey = makeCacheKey(type, requestPath, _config, keypath);
        _keyPath = keypath.toString();

        switch (_config._cachePolicy) {
            case DEFAULT: {
                _config._enableImageCache = true;
                _config._enableMemoryCache = true;
                _config._enableDiskCache = type == MediaType.NETWORK;
            }
            break;
            case NO_CACHE: {
                _config._enableImageCache = false;
                _config._enableMemoryCache = false;
                _config._enableDiskCache = false;
            }
            break;
            case ALL_CACHE: {
                _config._enableImageCache = true;
                _config._enableMemoryCache = true;
                _config._enableDiskCache = true;
            }
            break;
            case IMAGE_ONLY: {
                _config._enableImageCache = true;
                _config._enableMemoryCache = false;
                _config._enableDiskCache = false;
            }
            break;
            case MEMORY_ONLY: {
                _config._enableImageCache = false;
                _config._enableMemoryCache = true;
                _config._enableDiskCache = false;
            }
            break;
            case DISK_ONLY: {
                _config._enableImageCache = false;
                _config._enableMemoryCache = false;
                _config._enableDiskCache = true;
            }
            break;
            case NO_IMAGE: {
                _config._enableImageCache = false;
                _config._enableMemoryCache = true;
                _config._enableDiskCache = true;
            }
            break;
            case NO_MEMORY:
            {
                _config._enableImageCache = true;
                _config._enableMemoryCache = false;
                _config._enableDiskCache = true;

            }
            break;
            case NO_DISK:
            {
                _config._enableImageCache = true;
                _config._enableMemoryCache = true;
                _config._enableDiskCache = false;
            }
            break;
        }

        if (_config._enableDiskCache && type!=MediaType.NETWORK) {
            _config._enableDiskCache = false;
        }
    }


    public void interrupt() {_running=false;}
    public boolean isRunning() {return _running;}
    public boolean isTargetAlive() {
        return getTarget()!=null;
    }

    public int getTag() {return _tag;}
    public void setTag(final int tag) {_tag = tag;}

    public MediaType getMediaType() {return _type;}
    public DownloadProtocol getTarget() {
        return _targetRef.get();
    }
    public String getCacheKey() {return _cacheKey;}
    public String getRequestPath() {return _requestPath;}
    public String getKeyPath() {return _keyPath;}

    public MemoryCacheEntry getMemoryCacheEntry() {return _cacheEntry;}
    public void setMemoryCacheEntry(MemoryCacheEntry entry) {
        _cacheEntry = entry;
    }
    public ImageCacheEntry getImageCacheEntry() {return _imageEntry;}
    public void setImageCacheEntry(ImageCacheEntry entry) {
        _imageEntry = entry;
    }

    public void setDecodedImage(ImageCacheEntry entry) {
        _imageEntry = entry;
    }

    public DownloadConfig getConfig() {return _config;}

    public ImageDownloader getDownloader() {return _downloader;}

//    public void checkThreadInterrupt() {
//        try {
//
//            Thread.sleep(1);
//            if (!_running) break;
//
//        } catch (InterruptedException e) {
//
//        }
//    }

    // for network
    public void procDownloadThread() {

        try {
            do {

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                _cacheEntry = null;

                if (_config.isEnableMemoryCache()) {
                    MemoryCacheEntry cacheEntry = _downloader.getMemCache().get(_cacheKey);
                    if (cacheEntry!=null && cacheEntry.size()>0) {
                        _cacheEntry = cacheEntry;
                        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);
                        return;
                    }
                }

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                if (_config.isEnableDisckCache()) {
                    int[] error = new int[1];
                    byte[] data = FileManager.getInstance().loadFromFile(FileManager.FileType.Image, _cacheKey, error);

                    /*----------Check Thread Interrupt----------*/
                    Thread.sleep(1); if (!_running) break;
                    /*------------------------------------------*/

                    if (error[0]==FileManager.SUCCESS && data.length>0) {
                        _cacheEntry = MemoryCacheEntry.createEntry(data, data.length);
                        data = null;

                        if (_config.isEnableMemoryCache()) {
                            _downloader.getMemCache().put(_cacheKey, _cacheEntry);
                            Log.i("DT", "[[[[[ MEM CACHE");
                        }

                        /*----------Check Thread Interrupt----------*/
                        Thread.sleep(1); if (!_running) break;
                        /*------------------------------------------*/

                        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);
                        return;
                    }
                }

                _isSuccess = false;
                // downlod comm start

                _netDownloader = new Downloader();

                // start download
                _netDownloader.createDownloadDataTask(_requestPath);

                _netDownloader._onTaskError = new Downloader.OnTaskError() {
                    @Override
                    public void onTaskError(com.interpark.smframework.network.Downloader.DownloadTask task, int errorCode, int errorCodeInteral, String errorStr) {
                        Log.i("Scene", "[[[[[ download error : " + errorStr);

                        _isSuccess = false;
                        _cond.signal();
                    }
                };

                _netDownloader._onTaskProgress = new Downloader.OnTaskProgress() {
                    @Override
                    public void onTaskProgress(com.interpark.smframework.network.Downloader.DownloadTask task, long bytesReceived, long totalBytesReceived, long totalBytesExpected) {
                        Log.i("Scene", "[[[[[ download progress received : " + bytesReceived + ", total : " + totalBytesReceived + ", expected : " + totalBytesExpected);
                    }
                };

                _netDownloader._onDataTaskSuccess = new Downloader.OnDataTaskSuccess() {
                    @Override
                    public void onDataTaskSuccess(com.interpark.smframework.network.Downloader.DownloadTask task, byte[] data) {
                        writeDataProc(data, data.length);
                    }
                };


                // wait
                _mutex.lock();

                _cond.wait();
                _mutex.unlock();


                if (_isSuccess) {
                    _cacheEntry.shrinkToFit();

                    if (_config.isEnableMemoryCache()) {
                        _downloader.getMemCache().put(_cacheKey, _cacheEntry);
                        Log.i("DT", "[[[[[ MEM CACHE");
                    }

                    _netDownloader = null;

                    _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);

                    if (_config.isEnableDisckCache()) {
                        _downloader.writeToFileCache(_cacheKey, _cacheEntry);
                    }

                    return;
                }

            } while (false);
        } catch (InterruptedException e) {

        }

        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_FAILED);
        _cacheEntry = null;
    }

    // for resource/asset
    public void procLoadFromResourceThread() {

        try {
            do {

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/


                _cacheEntry = null;

                if (_config.isEnableMemoryCache()) {
                    MemoryCacheEntry cacheEntry = _downloader.getMemCache().get(_cacheKey);
                    if (cacheEntry!=null && cacheEntry.size()>0) {
                        _cacheEntry = cacheEntry;
                        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);
                        return;
                    }
                }

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                if (_config.isEnableDisckCache()) {
                    int[] error = new int[1];
                    byte[] data = FileManager.getInstance().loadFromFile(FileManager.FileType.Image, _cacheKey, error);

                    /*----------Check Thread Interrupt----------*/
                    Thread.sleep(1); if (!_running) break;
                    /*------------------------------------------*/

                    if (error[0]==FileManager.SUCCESS && data.length>0) {
                        _cacheEntry = MemoryCacheEntry.createEntry(data, data.length);
                        data = null;

                        if (_config.isEnableMemoryCache()) {
                            _downloader.getMemCache().put(_cacheKey, _cacheEntry);
                            Log.i("DT", "[[[[[ MEM CACHE");
                        }


                        /*----------Check Thread Interrupt----------*/
                        Thread.sleep(1); if (!_running) break;
                        /*------------------------------------------*/

                        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);

                        return;
                    }
                }


                FileUtils fs = FileUtils.getInstance();
                String filePath = fs.fullPathForFilename(_requestPath);
                byte[] data = fs.getDataFromFile(filePath);

                if (data==null || data.length==0) {
                    Log.i("DT", "[[[[[ Failed to resource file to loading!");
                    break;
                }

                _cacheEntry = MemoryCacheEntry.createEntry(data, data.length);
                data = null;

                _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);

                if (_config.isEnableMemoryCache()) {
                    _downloader.getMemCache().put(_cacheKey, _cacheEntry);
                    Log.i("DT", "[[[[[ MEM CACHE");
                }

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                if (_config.isEnableDisckCache()) {
                    _downloader.writeToFileCache(_cacheKey, _cacheEntry);
                }

                return;
            } while (false);

        } catch (InterruptedException e) {

        }

        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_FAILED);
        _cacheEntry = null;
    }

    // for local file path (SDCard?)
    public void procLoadFromFileThread() {
        try {

            do {
                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                MemoryCacheEntry cacheEntry = _downloader.getMemCache().get(_cacheKey);

                if (cacheEntry!=null && cacheEntry.size()>0) {
                    _cacheEntry = cacheEntry;
                    _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);
                    return;
                } else {
                    _cacheEntry = null;
                }


                _isSuccess = false;

                _mutex.lock();
                if (_phoneAlbums!=null && _phoneAlbums.size()>0) {
                    _isSuccess = true;
                } else {
                    ImageManager.getPhoneAlbumInfo(SMDirector.getDirector().getActivity(), new ImageManager.OnImageLoadListener() {
                        @Override
                        public void onAlbumImageLoadComplete(ArrayList<PhoneAlbum> albums) {
                            _isSuccess = true;
                            _phoneAlbums = albums;
                            _cond.notify();
                        }

                        @Override
                        public void onError() {
                            _cond.notify();
                        }
                    });

                    _cond.wait();
                }

                if (!_isSuccess || _phoneAlbums.size()==0) {
                    Log.i("DT", "[[[[[ Failed to get Album list~");
                    _mutex.unlock();
                    break;
                }

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                Bitmap bmp = getPhotoImage(_requestPath);
                if (bmp==null) {
                    Log.i("DT", "[[[[[ Failed to get Album list~");
                    _mutex.unlock();
                    break;
                }

                _imageEntry = ImageCacheEntry.createEntry(bmp);
                _mutex.unlock();

                _downloader.handleState(this, ImageDownloader.State.DECODE_SUCCESS);


                return;
            } while (false);


        } catch (InterruptedException e) {

        }

        _cacheEntry = null;
        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_FAILED);
    }

    private static int DEFAULT_SIDE_LENGTH = 256;
    private static int MAX_SIDE_LENGTH = 1280;
    private static ArrayList<PhoneAlbum> _phoneAlbums;

    public Bitmap getPhotoImage(String imageUrl) {
        // find photo
        PhoneAlbum phoneAlbum = _phoneAlbums.get(0);

        PhonePhoto phonePhoto= null;
        for (PhonePhoto photo : phoneAlbum.getAlbumPhotos()) {
            if (photo.getPhotoUri().compareTo(imageUrl)==0) {
                phonePhoto = photo;
                break;
            }
        }

        if (phonePhoto==null) {
            return null;
        }

        int orientation = phonePhoto.getOrientation();
        int sideLength = MAX_SIDE_LENGTH;

        return ImageManager.loadBitmapResize(imageUrl, orientation, sideLength);

    }

    // ... for resize
    public void procLoadFromThumbnailThread() {
        try {
            do {

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                MemoryCacheEntry cacheEntry = _downloader.getMemCache().get(_cacheKey);

                if (cacheEntry!=null && cacheEntry.size()>0) {
                    _cacheEntry = cacheEntry;
                    _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_SUCCESS);
                    return;
                } else {
                    _cacheEntry = null;
                }


                _isSuccess = false;

                _mutex.lock();
                if (_phoneAlbums!=null && _phoneAlbums.size()>0) {
                    _isSuccess = true;
                } else {
                    ImageManager.getPhoneAlbumInfo(SMDirector.getDirector().getActivity(), new ImageManager.OnImageLoadListener() {
                        @Override
                        public void onAlbumImageLoadComplete(ArrayList<PhoneAlbum> albums) {
                            _isSuccess = true;
                            _phoneAlbums = albums;
                            _cond.notify();
                        }

                        @Override
                        public void onError() {
                            _cond.notify();
                        }
                    });

                    _cond.wait();
                }

                if (!_isSuccess || _phoneAlbums.size()==0) {
                    Log.i("DT", "[[[[[ Failed to get Album list~");
                    _mutex.unlock();
                    break;
                }

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                Bitmap bmp = getPhotoThumbnail(_requestPath);
                if (bmp==null) {
                    Log.i("DT", "[[[[[ Failed to get Album list~");
                    _mutex.unlock();
                    break;
                }

                _imageEntry = ImageCacheEntry.createEntry(bmp);
                _mutex.unlock();

                _downloader.handleState(this, ImageDownloader.State.DECODE_SUCCESS);

                return;
            } while (false);
        } catch (InterruptedException e) {

        }

        _cacheEntry = null;
        _downloader.handleState(this, ImageDownloader.State.DOWNLOAD_FAILED);
    }

    public Bitmap getPhotoThumbnail(String imageUrl) {

        // find photo
        PhoneAlbum phoneAlbum = _phoneAlbums.get(0);

        PhonePhoto phonePhoto= null;
        for (PhonePhoto photo : phoneAlbum.getAlbumPhotos()) {
            if (photo.getPhotoUri().compareTo(imageUrl)==0) {
                phonePhoto = photo;
                break;
            }
        }

        if (phonePhoto==null) {
            return null;
        }

        String thumbPath = ImageManager.getThumbnailPath(SMDirector.getDirector().getActivity(), phonePhoto.getId());
        Bitmap bitmap = null;
        int orientation = phonePhoto.getOrientation();
        int sideLength = DEFAULT_SIDE_LENGTH;
        if (thumbPath != null) {
            bitmap = ImageManager.extractThumbnailFromFile(thumbPath, orientation, sideLength, sideLength);
        }

        if (bitmap == null) {
            bitmap = ImageManager.extractThumbnailFromFile(phonePhoto.getPhotoUri(), orientation, sideLength, sideLength);
        }

        return bitmap;

    }

    public void procDecodeThread() {
        try {

            _imageEntry = null;

            if (_cacheEntry==null) {
                _cacheEntry = _downloader.getMemCache().get(_cacheKey);
                if (_cacheEntry==null) {
                    // 여기 왔는데 _cacheEntry가 없을 수는 없다.
                    _downloader.handleState(this, ImageDownloader.State.DECODE_FAILED);
                    return;
                }
            }

            do {
                _imageEntry = null;

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                _mutex.lock();

                byte[] data = _cacheEntry.getData();

                Bitmap bmp = WebPFactory.decodeByteArray(data);
                if (bmp==null) {
                    Log.i("DT", "[[[[[ Failed to decode image~");
                    _mutex.unlock();
                    break;
                }

                _imageEntry = ImageCacheEntry.createEntry(bmp);
                _mutex.unlock();

                _downloader.handleState(this, ImageDownloader.State.DECODE_SUCCESS);

            } while (false);

        } catch (InterruptedException e) {

        }

        _imageEntry = null;
        _downloader.handleState(this, ImageDownloader.State.DECODE_FAILED);
    }

    public Bitmap getResampleImage(Bitmap srcImage) {
        // 이거 OpenCV로 빼야함..

        int srcWidth = srcImage.getWidth();
        int srcHeight = srcImage.getHeight();

        float scaleX = 1.0f;
        float scaleY = 1.0f;
        float scaleWidth, scaleHeight;

        switch (_config._resamplePolicy) {
            case NONE:
            {

            }
            break;
            case AREA:
            {
                scaleX = scaleY = (_config._resParam1 * _config._resParam2) / (srcWidth*srcHeight);
            }
            break;
            case EXACT_FIT:
            {
                scaleX = _config._resParam1 / srcWidth;
                scaleY = _config._resParam2 / srcHeight;
            }
            break;
            case EXACT_CROP:
            {
                scaleX = scaleY = Math.max(_config._resParam1/srcWidth, _config._resParam2/srcHeight);
            }
            break;
            case LONGSIDE:
            {
                if (srcWidth>srcHeight) {
                    scaleX = scaleY = _config._resParam1 / srcWidth;
                } else {
                    scaleX = scaleY = _config._resParam2 / srcHeight;
                }
            }
            break;
            case SCALE:
            {
                scaleX = scaleY = _config._resParam1;
            }
            break;
        }

        if (scaleX<=0 || scaleY<=0) {
            return srcImage;
        }

        scaleWidth = srcWidth * scaleX;
        scaleHeight = srcHeight * scaleY;

        if (_config._resampleShrinkOnly && srcWidth*srcHeight<=scaleWidth*scaleHeight) {
            return srcImage;
        }

        int interpolation;
        switch (_config._resampleMethod) {
            case CUBIC:
            {
//                interpolation = CV_INTER
                // 여기서 부터 CV...
            }
            break;
            case LANCZOS:
            {

            }
            break;
            case LINEAR:
            {

            }
            break;
            default:
            {

            }
            break;
        }

        return srcImage;
    }

    public void writeDataProc(final byte[] buffer, int size) {
        _isSuccess = false;

        if (_cacheEntry!=null) {
            _cacheEntry.appendData(buffer, size);

            _isSuccess = true;
        }

        _cond.signal();
    }


    private final Lock _mutex = new ReentrantLock(true);
    private final Condition _cond = _mutex.newCondition();

    private boolean _isSuccess = false;
    private Downloader _netDownloader = null;
    private boolean _running = true;
    private int _tag;
    private MediaType _type;
    private DownloadConfig _config = null;
    private MemoryCacheEntry _cacheEntry = null;
    private String _cacheKey;
    private String _requestPath;
    private String _keyPath;
    private WeakReference<DownloadProtocol> _targetRef = null;
    private ImageCacheEntry _imageEntry = null;
    private ImageDownloader _downloader = null;
    private int _taskId;

}
