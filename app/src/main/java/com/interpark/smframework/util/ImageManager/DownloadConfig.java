package com.interpark.smframework.util.ImageManager;

public class DownloadConfig {
    public DownloadConfig() {
        this (CachePolycy.DEFAULT, false);
    }
    public DownloadConfig(final CachePolycy cachePolicy) {
        this (cachePolicy, false);
    }
    public DownloadConfig(final CachePolycy cachePolicy, final boolean cacheOnly) {
        _cachePolicy = cachePolicy;
        _resamplePolicy = ResamplePolicy.NONE;
        _resampleMethod = ResampleMethod.LINEAR;
        _resampleShrinkOnly = true;
        _resParam1 = 0;
        _resParam2 = 0;
        _reqDegress = 0;
        _cacheOnly = cacheOnly;
        _smallThumbnail = false;
    }

    public enum CachePolycy {
        DEFAULT,
        NO_CACHE,
        ALL_CACHE,
        IMAGE_ONLY,
        MEMORY_ONLY,
        DISK_ONLY,
        NO_IMAGE,
        NO_MEMORY,
        NO_DISK
    }

    public enum ResampleMethod {
        NEAREST,
        LINEAR,
        CUBIC,
        LANCZOS
    }

    public enum ResamplePolicy {
        NONE,
        EXACT_FIT,
        EXACT_CROP,
        AREA,
        LONGSIDE,
        SCALE,
    }

    public void setCachePolicy(final CachePolycy cachePolycy) {
        _cachePolicy = cachePolycy;
    }

    public void setResamplePolicy(final ResamplePolicy resamplePolicy) {
        setResamplePolicy(resamplePolicy, 0, 0);
    }
    public void setResamplePolicy(final ResamplePolicy resamplePolicy, final float param1) {
        setResamplePolicy(resamplePolicy, param1, 0);
    }
    public void setResamplePolicy(final ResamplePolicy resamplePolicy, final float param1, final float param2) {
        _resamplePolicy = resamplePolicy;
        _resParam1 = param1;
        _resParam2 = param2;

        switch (_resamplePolicy) {
            case AREA:
            {
                assert (_resParam1 > 0 && _resParam2 > 0);
            }
            break;
            case EXACT_FIT:
            {
                assert (_resParam1 > 0 && _resParam2 > 0);
            }
            break;
            case EXACT_CROP:
            {
                assert (_resParam1 > 0 && _resParam2 > 0);
            }
            break;
            case LONGSIDE:
            {
                assert (_resParam1 > 0);
            }
            break;
            case SCALE:
            {
                assert (_resParam1 > 0);

                if (_resParam1>=1.0f) {
                    _resamplePolicy = ResamplePolicy.NONE;
                }
            }
            break;
            default:
            {

            }
            break;
        }
    }

    public void setResampleMethod(final ResampleMethod resampleMethod) {
        _resampleMethod = resampleMethod;
    }

    public void setRotation(final int degress) {
        _reqDegress = degress;
    }

    public void setCacheOnly() {_cacheOnly = true;}

    public void setSmallThumbnail() {_smallThumbnail = true;}

    boolean isEnableMemoryCache() {return _enableMemoryCache;}
    boolean isEnableImageCache() {return _enableImageCache;}
    boolean isEnableDisckCache() {return _enableDiskCache;}
    boolean isEnablePhysicsBody() {return _enablePhysicsBody;}
    boolean isCacheOnly() {return _cacheOnly;}
    boolean isSmallThumbnail() {return _smallThumbnail;}




    public CachePolycy _cachePolicy;
    public ResamplePolicy _resamplePolicy;
    public ResampleMethod _resampleMethod;

    public float _resParam1;
    public float _resParam2;
    public float _reqDegress;

    public boolean _resampleShrinkOnly;
    public boolean _cacheOnly;
    public boolean _enableMemoryCache;
    public boolean _enableImageCache;
    public boolean _enableDiskCache;
    public boolean _enablePhysicsBody;
    public boolean _smallThumbnail;

//    private float _physicsBodyEpsilon;
//    private float _physicsBodyThreshold;
}
