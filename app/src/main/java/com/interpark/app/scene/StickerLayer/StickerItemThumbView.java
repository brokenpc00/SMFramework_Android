package com.interpark.app.scene.StickerLayer;

import android.util.Log;
import android.view.MotionEvent;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.ActionInterval;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayBaseAction;
import com.interpark.smframework.base.types.FadeIn;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.shader.ShaderNode;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.ImageManager.DownloadConfig;
import com.interpark.smframework.util.ImageManager.DownloadTask;
import com.interpark.smframework.util.ImageManager.IDownloadProtocol;
import com.interpark.smframework.util.ImageManager.ImageDownloader;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.LoadingSprite;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.view.SMRectView;

import java.util.ArrayList;
import java.util.ListIterator;

public class StickerItemThumbView extends SMView implements IDownloadProtocol {
    public StickerItemThumbView(IDirector director) {
        super(director);

        _shakeAction = null;
        _selectAction = null;
        _spinner = null;
        _selectBox = null;
        _imageView = null;
        _selected = false;
        _imgDlConfig = null;
        _thumbDlConfig = null;
    }

    public static StickerItemThumbView create(IDirector director, int tag, final DownloadConfig thumbDlConfig, final DownloadConfig imgDlConfig) {
        StickerItemThumbView view = new StickerItemThumbView(director);
        view.setTag(tag);
        view.init();
        view._thumbDlConfig = thumbDlConfig;
        view._imgDlConfig = imgDlConfig;
        return view;
    }

    @Override
    protected boolean init() {
        if (!super.init()) {
            return false;
        }

        _imageView = SMImageView.create(getDirector());
        _imageView.setAnchorPoint(Vec2.MIDDLE);
        addChild(_imageView);
        _imageView.setTag(_tag);

        _spinner = LoadingSprite.createWithFile(getDirector());
        _spinner.setAnchorPoint(Vec2.MIDDLE);
        addChild(_spinner);

        _imageView.setPadding(10.0f);
        _imageView.setScaleType(SMImageView.ScaleType.CENTER_INSIDE);

        return true;
    }

    @Override
    public void setContentSize(float width, float height) {
        this.setContentSize(new Size(width, height));
    }
    @Override
    public void setContentSize(final Size size) {
        super.setContentSize(size);

        _imageView.setContentSize(size);
        _imageView.setPosition(size.divide(2));
        _spinner.setPosition(size.divide(2));
    }

    @Override
    public void onEnter() {
        super.onEnter();

        if (_imageView.getSprite()!=null) {
            _imageView.getSprite().setColor(Color4F.WHITE);
            _imageView.getSprite().setOpacity(0xff);
            _spinner.setVisible(false);
        } else {
            _spinner.setVisible(true);
            ImageDownloader.getInstance().loadImageFromResource(this, _imagePath, _tag, _thumbDlConfig);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();

        resetDownload();

        _imageView.setSprite(null);

        _spinner.setAlpha(1.0f);
        _spinner.stopAllActions();
    }

    public boolean isSelected() {return _selected;}

    public void setImagePath(final String path) {
        if (!_imagePath.equals(path)) {
            resetDownload();
        }
        _imagePath = path;
    }

    public void startShowAction() {
        if (_imageView.getSprite()!=null) {
            if (_shakeAction==null) {
                _shakeAction = createShakeAction(getDirector());
                _shakeAction.setTag(AppConst.TAG.USER+1);
            }

            if (getActionByTag(AppConst.TAG.USER+1)!=null) {
                stopAction(_shakeAction);
            }

            _shakeAction.setValue(0);
            runAction(_shakeAction);
        }
    }

    public void setSelect(boolean select, boolean immediate) {
        if (_selected==select) {
            return;
        }

        Action action = getActionByTag(AppConst.TAG.USER+2);
        if (action!=null) {
            stopAction(action);
        }

        if (select && _selectBox==null) {
             _selectBox = SMRectView.create(getDirector());
             _selectBox.setColor(Color4F.TEXT_BLACK);
             _selectBox.setContentSize(new Size(204, 256));
             _selectBox.setPosition(15, 30);
             _imageView.addChild(_selectBox);
        }

        if (immediate) {
            if (_selectBox!=null) {
                _selectBox.setVisible(select);
                if (select) {
                    _selectBox.setLineWidth(12.0f);
                }
            }
        } else {
            if (_selectAction==null) {
                _selectAction = createSelectAction(getDirector());
                _selectAction.setTag(AppConst.TAG.USER+2);
            }

            _selectAction.select(select);
            runAction(_selectAction);
        }
    }

    public void setFocus() {
        Action action = getActionByTag(AppConst.TAG.USER+3);
        if (action!=null) {
            stopAction(action);
        }

        if (_selectBox==null) {
            _selectBox = SMRectView.create(getDirector());
            _selectBox.setColor(Color4F.TEXT_BLACK);
            _selectBox.setContentSize(new Size(204, 256));
            _selectBox.setPosition(15, 30);
            _selectBox.setLineWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH);
            _imageView.addChild(_selectBox);
        }

        FocusAction focusAction = createFocusAction(getDirector());
        focusAction.setTag(AppConst.TAG.USER+3);
        focusAction.setFocusTime(0.1f);
        runAction(focusAction);
    }

    public SMImageView getImageView() {return _imageView;}

    @Override
    protected void onStateChangePressToNormal(MotionEvent event) {
        setAnimOffset(Vec2.ZERO);
    }
    @Override
    protected void onStateChangeNormalToPress(MotionEvent event) {
        setAnimOffset(new Vec2(0, 15));
    }


    // private property
    public ShakeAction createShakeAction(IDirector director) {
        ShakeAction action = new ShakeAction(director);
        action.initWithDuration(0);
        return action;
    }
    public class ShakeAction extends ActionInterval {
        public ShakeAction(IDirector director) {
            super(director);
        }

        public float getShakeAngle(float t) {
            float f = 1.0f - (float)Math.sin(t*M_PI_2);
            return (float)(_shakeDir * SHAKE_ANGLE * f * Math.sin(_shakeCount * Math.sin(t * M_PI_2) * M_PI_2));
        }

        @Override
        public void update(float t) {
            StickerItemThumbView target = (StickerItemThumbView)_target;

            SMImageView imageView = target.getImageView();

            float time = t * (getDuration() + _delay);
            if (time<_delay) {
                return;
            }

            time -= _delay;
            t = time / _duration;

            final float deg = getShakeAngle(t);
            final float hangFactor = 2.2f;
            Size size = imageView.getContentSize();

            float cx = size.width * 0.5f;
            float cy = size.height * 0.5f;

            float dx = (float)(cx * Math.cos(SMView.toRadians(deg) - M_PI_2));
            float dy = (float)(cy * Math.sin(SMView.toRadians(deg) - M_PI_2) * hangFactor);

            float x = size.width * 0.5f;
            float y = size.height * 0.5f;

            imageView.setPosition(x-dx, y+dy+(cy*hangFactor));
            imageView.setRotation((float) (deg/M_PI));
        }

        public void setValue(float delay) {
            float duration = SHAKE_TIME + SMView.randomFloat(0.0f, 0.8f);
            setDuration(duration+delay);

            _delay = delay;

            _shakeCount = SMView.randomFloat(6.0f, 10.0f);
            _shakeDir = SMView.randomFloat(0.0f, 1.0f) > 0.5f ? 1 : -1;
        }

        private float _delay = 0.0f;
        private float _shakeCount = 0;
        private int _shakeDir = 0;
    }

    public SelectAction createSelectAction(IDirector director) {
        SelectAction action = new SelectAction(director);
        action.initWithDuration(0);
        return action;
    }
    public class SelectAction extends DelayBaseAction {
        public SelectAction(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {
            StickerItemThumbView target = (StickerItemThumbView)_target;

            _from = target._selectBox.getLineWidth();
            _to = _select ? 12.0f : 0.0f;

            target._selectBox.setVisible(true);
        }

        @Override
        public void onUpdate(float t) {
            float lineWidth = SMView.interpolation(_from, _to, t);

            StickerItemThumbView target = (StickerItemThumbView)_target;
            target._selectBox.setLineWidth(lineWidth);
        }

        @Override
        public void onEnd() {
            if (!_select) {
                StickerItemThumbView target = (StickerItemThumbView)_target;
                target._selectBox.setVisible(false);
            }
        }

        public void select(boolean select) {
            _select = select;

            if (select) {
                setTimeValue(0.15f, 0.0f);
            } else {
                setTimeValue(0.1f, 0.0f);
            }
        }

        private boolean _select = false;
        private float _from, _to;
    }

    public FocusAction createFocusAction(IDirector director) {
        FocusAction action = new FocusAction(director);
        action.initWithDuration(0);
        return action;
    }
    public class FocusAction extends DelayBaseAction {
        public FocusAction(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {
            StickerItemThumbView target = (StickerItemThumbView)_target;

            _from = target._selectBox.getLineWidth();
            target._selectBox.setVisible(true);
        }

        @Override
        public void onUpdate(float t) {
            float time = t * getDuration();
            float lineWidth;
            if (time<0.15f) {
                t = time / 0.15f;
                lineWidth = SMView.interpolation(_from, 12.0f, t);
            } else if (time<0.15f-_focusTime) {
                time -= 0.15f;
                t = time / _focusTime;
                lineWidth = 12.0f;
            } else {
                time -= 0.15f + _focusTime;
                t = time / 0.1f;
                lineWidth = SMView.interpolation(12, 0, t);
            }

            StickerItemThumbView target = (StickerItemThumbView)_target;
            target._selectBox.setLineWidth(lineWidth);
        }

        @Override
        public void onEnd() {
            StickerItemThumbView target = (StickerItemThumbView)_target;
            target._selectBox.setVisible(false);
        }

        public void setFocusTime(float focusTime) {
            _focusTime = focusTime;
            setTimeValue(0.15f + focusTime + 0.1f, 0.0f);
        }

        private float _focusTime = 0.0f;
        private float _from;
    }

    private ShakeAction _shakeAction = null;
    private SelectAction _selectAction = null;
    private boolean _selected = false;

    private SMRectView _selectBox = null;
    private SMImageView _imageView = null;
    private LoadingSprite _spinner = null;

    private DownloadConfig _thumbDlConfig = null;
    private DownloadConfig _imgDlConfig = null;
    private String _imagePath = "";

    private static final float SHAKE_TIME = 1.2f;
    private static final float SHAKE_ANGLE = 8.0f;






    // IDownloadProtocol
    private ArrayList<DownloadTask> _downloadTask = new ArrayList<>();
    @Override
    public void onImageLoadComplete(Sprite sprite, int tag, boolean direct) {
        if (sprite!=null) {
//            Log.i("STICKERTHUMB", "[[[[[ on image load complete : " + tag);
            _imageView.setSprite(sprite);
            _spinner.setAlpha(0);
            _spinner.runAction(FadeIn.create(getDirector(), 0.1f));

            startShowAction();
        }
        _spinner.setVisible(false);
    }
    @Override
    public void onImageCacheComplete(boolean success, int tag) { }
    @Override
    public void onImageLoadStart(DownloadStartState state) { }
    @Override
    public void onDataLoadComplete(byte[] data, int size, int tag) { }
    @Override
    public void onDataLoadStart(DownloadStartState state) { }
    @Override
    public void resetDownload() {
        synchronized (_downloadTask) {
            ListIterator<DownloadTask> iter = _downloadTask.listIterator();
            while (iter.hasNext()) {
                DownloadTask task = iter.next();
                if (task.isTargetAlive() && task.isRunning()) {
                    task.interrupt();
                }
                task = null;
            }
//
//                for (DownloadTask task : _downloadTask) {
//                if (task.isTargetAlive()) {
//                    if (task.isRunning()) {
//                        task.interrupt();
//                    }
//                }
//                task = null;
//            }

            _downloadTask.clear();
        }
    }

    @Override
    public void removeDownloadTask(DownloadTask task) {
        synchronized (_downloadTask) {
            ListIterator<DownloadTask> iter = _downloadTask.listIterator();
            while (iter.hasNext()) {
                DownloadTask t = iter.next();
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t);
                } else if (task != null && t != null && (t.equals(task) || task.getCacheKey().compareTo(t.getCacheKey()) == 0)) {
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
            ListIterator<DownloadTask> iter = _downloadTask.listIterator();
            while (iter.hasNext()) {
                DownloadTask t = iter.next();
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t);
                } else if (task!=null && t.isRunning() && (t.equals(task) || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    return false;
                }
            }

            _downloadTask.add(task);
            return true;
        }
    }
}
