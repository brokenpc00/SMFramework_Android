package com.interpark.smframework.view;

import android.media.Image;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayBaseAction;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.base.types.SEL_SCHEDULE;
import com.interpark.smframework.network.Downloader.Downloader;
import com.interpark.smframework.util.ImageManager.DownloadConfig;
import com.interpark.smframework.util.ImageManager.DownloadTask;
import com.interpark.smframework.util.ImageManager.IDownloadProtocol;
import com.interpark.smframework.util.ImageManager.ImageDownloader;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class SMKenBurnsView extends SMView implements IDownloadProtocol {
    public SMKenBurnsView(IDirector director) {
        super(director);
        _sequence = 0;
        _serial = 0;
        _runnable = true;
    }

    private static final float PAN_TIME = 8.0f;
    private static final float FADE_TIME = 1.3f;
    private static final float MINUM_RECT_FACTOR = 0.6f;
    private static final Color4F DIM_LAYER_COLOR = new Color4F(0, 0, 0, 0.6f);



    public static SMKenBurnsView createWithAssets(IDirector director, ArrayList<String> assetList) {
        SMKenBurnsView view = new SMKenBurnsView(director);
        view.initWithImageList(Mode.ASSET, assetList);
        return view;
    }

    public static SMKenBurnsView createWithURLs(IDirector director, ArrayList<String> urlList) {
        SMKenBurnsView view = new SMKenBurnsView(director);
        view.initWithImageList(Mode.URL, urlList);
        return view;
    }

    // for later
    public enum Mode {
        ASSET,
        URL
    }

    protected boolean initWithImageList(Mode mode, ArrayList<String> imageList) {
        if (imageList.size()==0) return false;

        _mode = mode;
        _imageList = imageList;

        _dimLayer = SMSolidRectView.create(getDirector());
        _dimLayer.setContentSize(_contentSize);
        _dimLayer.setColor(DIM_LAYER_COLOR);
        addChild(_dimLayer);

        return true;
    }

    @Override
    public void setContentSize(final Size size) {
        super.setContentSize(size);
        if (_dimLayer!=null) {
            _dimLayer.setContentSize(size);
        }
    }
    @Override
    public void setContentSize(final float width, final float height) {
        this.setContentSize(new Size(width, height));
    }

    private Downloader _downloader = null;
    public void startWithDelay(float delay) {
        if (delay<=0) {
            onNextTransition(0);
        } else {
            if (_mode==Mode.URL) {
                ImageDownloader.getInstance().loadImageFromNetwork(this, _imageList.get(0), _serial++, ImageDownloader.CACHE_ONLY);
            }
            scheduleOnce(new SEL_SCHEDULE() {
                @Override
                public void scheduleSelector(float t) {
                    onNextTransition(t);
                }
            }, delay);
        }
    }

    private void onNextTransition(float dt) {
        if (_mode==Mode.URL) {
            ImageDownloader.getInstance().loadImageFromNetwork(this, _imageList.get(_sequence++), _serial++);
        } else {
            ImageDownloader.getInstance().loadImageFromResource(this, _imageList.get(_sequence++), _serial++, ImageDownloader.NO_CACHE);
        }

        _sequence %= _imageList.size(); // imagesize >= -> set 0

    }

    @Override
    public void onImageLoadComplete(BitmapSprite sprite, int tag, boolean direct) {
        if (sprite!=null) {
            SMImageView imageView = SMImageView.create(getDirector(), sprite);
            imageView.setScaleType(SMImageView.ScaleType.CENTER);
            imageView.setContentSize(sprite.getContentSize());
            imageView.setAnchorPoint(Vec2.MIDDLE);
            addChild(imageView);

            Rect src = generateRandomRect(new Size(imageView.getContentSize()));
            Rect dst = generateRandomRect(new Size(imageView.getContentSize()));

            src.origin.x += _contentSize.width/2;
            src.origin.y += _contentSize.height/2;
            dst.origin.x += _contentSize.width/2;
            dst.origin.y += _contentSize.height/2;
//            Log.i("KenBurn", "[[[[[ src : (" + src.origin.x + ", " + src.origin.y + ", " + src.size.width + ", " + src.size.height + "), dst : (" + dst.origin.x + ", " + dst.origin.y + ", " + dst.size.width + ", " + dst.size.height + ")");

            TransitionAction action = TransitionActionCreate(getDirector());
            action.setValue(imageView, src, dst, PAN_TIME, 0);
            action.setTag(17);

            runAction(action);
    }

        if (_mode==Mode.URL) {
            int nextSeq = (_sequence+1) % _imageList.size();
            ImageDownloader.getInstance().loadImageFromNetwork(this, _imageList.get(nextSeq), _serial++, ImageDownloader.CACHE_ONLY_DISK_ONLY);
    }

        _scheduler.performFunctionInMainThread(new PERFORM_SEL() {
            @Override
            public void performSelector() {
                scheduleOnce(new SEL_SCHEDULE() {
                    @Override
                    public void scheduleSelector(float t) {
                        onNextTransition(t);
                    }
                }, (PAN_TIME-FADE_TIME-0.5f));
            }
        });
    }

    public void pauseKenBurns() {
        _runnable = false;
        onPause();
    }

    public void resumeKenBurns() {
        _runnable = true;
        onResume();
    }

    private Rect generateRandomRect(final Size imageSize) {
        float ratio1 = getAspectRatio(imageSize);
        float ratio2 = getAspectRatio(_contentSize);

        Rect maxCrop = new Rect();

        if (ratio1 > ratio2) {
            float r = (imageSize.height/_contentSize.height) * _contentSize.width;
            float b = imageSize.height;
            maxCrop.setRect(0, 0, r, b);
        } else {
            float r = imageSize.width;
            float b = (imageSize.width/_contentSize.width) * _contentSize.height;
            maxCrop.setRect(0, 0, r, b);
        }

        float rnd = truncate(SMView.randomFloat(0.0f, 1.0f), 2);
        float factor = MINUM_RECT_FACTOR + ((1-MINUM_RECT_FACTOR)*rnd);

        float width = factor * maxCrop.size.width;
        float height = factor * maxCrop.size.height;

        float diffWidth = imageSize.width - width;
        float diffHeight = imageSize.height - height;

        float x = diffWidth>0.0f ? SMView.randomFloat(0.0f, diffWidth) : 0.0f;
        float y = diffHeight>0.0f ? SMView.randomFloat(0.0f, diffHeight) : 0.0f;

        return new Rect(x, y, width, height);
    }

    public static float getAspectRatio(final Size rect) {
        return rect.width/rect.height;
    }

    public static float truncate(float f, int d) {
        float dShift = (float) Math.pow(10, d);

        return Math.round(f*dShift) / dShift;
    }

    private Mode _mode = Mode.ASSET;
    private int _sequence = 0;
    private int _serial = 0;
    private boolean _runnable = true;

    private ArrayList<String> _imageList = new ArrayList<>();

    SMSolidRectView _dimLayer = null;

    public TransitionAction TransitionActionCreate(IDirector director) {
        TransitionAction action = new TransitionAction(director);
        action.initWithDuration(0);
        return action;
    }
    public class TransitionAction extends DelayBaseAction {
        public TransitionAction(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onUpdate(float dt) {
            updateTextureRect(dt);

            float time = getDuration() * dt;
            float alpha = Math.min(time/FADE_TIME, 1.0f);
            _image.setAlpha(alpha);
            _image.setScale(_target.getContentSize().width/_image.getContentSize().width);
        }

        @Override
        public void onEnd() {
            _target.removeChild(_image);
        }

        public void updateTextureRect(float t) {
            float x = SMView.interpolation(_src.origin.x, _dst.origin.x, t);
            float y = SMView.interpolation(_src.origin.y, _dst.origin.y, t);
            float w = SMView.interpolation(_src.size.width, _dst.size.width, t);
            float h = SMView.interpolation(_src.size.height, _dst.size.height, t);

            _image.setAnchorPoint(Vec2.MIDDLE);
            _image.setContentSize(w, h);
            _image.setPosition(x, y);
        }

        public void setValue(SMImageView image, final Rect src, final Rect dst, float duration, float delay) {
            setTimeValue(duration, delay);

            _image = image;

            _src = src;
            _dst = dst;

            _image.setAlpha(0.0f);
            updateTextureRect(0.0f);
        }

        protected SMImageView _image;
        protected Rect _src = new Rect(), _dst = new Rect();
    }




    @Override
    public void onImageCacheComplete(boolean success, int tag) {

    }

    @Override
    public void onImageLoadStart(DownloadStartState state) {

    }

    @Override
    public void onDataLoadComplete(byte[] data, int size, int tag) {

    }

    @Override
    public void onDataLoadStart(DownloadStartState state) {

    }





    // IDownloadProtocol copy
    @Override
    public void resetDownload() {
        synchronized (_downloadTask) {
            for (DownloadTask task : _downloadTask) {
                if (task.isTargetAlive()) {
                    if (task.isRunning()) {
                        task.interrupt();
                    }
                }
                task = null;
            }

            _downloadTask.clear();

        }
    }

    @Override
    public void removeDownloadTask(DownloadTask task) {
        synchronized (_downloadTask) {
            for (DownloadTask t : _downloadTask) {
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t);
                } else if (task!=null && t!=null && (t.equals(task) || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    task.interrupt();
                    _downloadTask.remove(t);
                    t = null;
                }
            }
        }
    }

    @Override
    public boolean isDownloadRunning(final String requestPath, int requestTag) {
        synchronized (_downloadTask) {
            for (DownloadTask t : _downloadTask) {
                if (t.getRequestPath().compareTo(requestPath)==0 && t.getTag()==requestTag) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean addDownloadTask(DownloadTask task) {
        synchronized (_downloadTask) {
            for (DownloadTask t : _downloadTask) {
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t);
                } else if (task!=null && t!=null && t.isRunning() && (t.equals(task) || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    return false;
                }
            }

            _downloadTask.add(task);
            return true;
        }
    }


}
