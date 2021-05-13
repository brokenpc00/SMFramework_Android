package com.interpark.app.scene.StickerLayer;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.ImageManager.DownloadConfig;
import com.interpark.smframework.util.ImageManager.ImageDownloader;
import com.interpark.smframework.util.ImageProcess.ImageProcessTask;
import com.interpark.smframework.util.ImageProcess.ImageProcessor;
import com.interpark.smframework.util.ImageProcess.ImgPrcSimpleCapture;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.Sticker.Sticker;
import com.interpark.smframework.util.ImageProcess.ImageProcessProtocol;
import com.interpark.smframework.view.Sticker.StickerItem;

import java.util.ListIterator;

public class StickerItemView extends Sticker implements ImageProcessProtocol {
    private SMView _layout = null;
    private StickerItem _item = null;
    public StickerItemView(IDirector director) {
        super(director);
        _layout = null;
        _item = null;
        _isBlack = false;
        _alpha = 1.0f;

        setControlType(ControlType.DELETE);
    }

    public interface StickerLayoutListener {
        public void onStickerLayout(StickerItemView itemView, Sprite sprite, final StickerItem item, final int colorIndex);
    }
    private StickerLayoutListener _listener = null;

    public static StickerItemView createWithItem(IDirector director, final StickerItem item, StickerLayoutListener l) {
        StickerItemView itemView = new StickerItemView(director);
        itemView.initWithStickerItem(item, l);
        return itemView;
    }

    protected boolean initWithStickerItem(final StickerItem item, StickerLayoutListener l) {
        if (!super.init()) {
            return false;
        }

        _item = item;
        _listener = l;

        setAnchorPoint(Vec2.MIDDLE);

        _isBlack = false;

        String path = _item._rootPath + "image/" + _item._imageArray.get(0) + ".png";

        BitmapSprite sprite = BitmapSprite.createFromAsset(getDirector(), path, false, null);
        if (sprite!=null) {
            super.onImageLoadComplete(sprite, 0, true);
        }

//        setBackgroundColor(new Color4F(1, 0, 0, 0.4f));

        return true;
    }


    private boolean _isBlack = false;
    public boolean isBlack() {return _isBlack;}
    private float _alpha = 1.0f;
    public void setAlphaValue(float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        setAlpha(0.1f + 0.9f*alpha);

        _alpha = alpha;
    }
    public float getAlphaValue() {return _alpha;}

    @Override
    public void onImageLoadComplete(Sprite sprite, int tag, boolean direct) {
        super.onImageLoadComplete(sprite, tag, direct);

        if (getSprite()!=null) {
            if (tag==0 && isBlack()) {
                getSprite().setColor(Color4F.BLACK);
            }

            if (_item._layout>0 && _listener!=null) {
                _listener.onStickerLayout(this, (Sprite) getSprite(), _item, isBlack()?1:0);
            }
        }
    }

    public void setBlack() {
        if (_isBlack) {
            return;
        }

        _isBlack=true;

        if (_item._imageArray.size()>=2) {

            resetDownload();
            String path = _item._rootPath + "sticker/" + _item._imageArray.get(1) + ".png";
            ImageDownloader.getInstance().loadImageFromResource(this, path, 1, ImageDownloader.NO_DISK);

        } else {

            setColor(Color4F.BLACK);
            if (getSprite()!=null) {
                getSprite().setColor(Color4F.BLACK);
            }

            if (_item._layout>0 && getSprite()!=null && _listener!=null) {
                _listener.onStickerLayout(this, (Sprite) getSprite(), _item, 1);
            }
        }
    }

    public void setWhite() {
        if (!_isBlack) {
            return;
        }

        _isBlack=false;

        if (_item._imageArray.size()>=2) {
            resetDownload();
            String path = _item._rootPath + "sticker/" + _item._imageArray.get(0) + ".png";
            ImageDownloader.getInstance().loadImageFromResource(this, path, 0, ImageDownloader.NO_DISK);
        } else {
            setColor(Color4F.WHITE);
            if (getSprite()!=null) {
                getSprite().setColor(Color4F.WHITE);
            }

            if (_item._layout>0 && getSprite()!=null && _listener!=null) {
                _listener.onStickerLayout(this, (Sprite)getSprite(), _item, 0);
            }
        }
    }


    @Override
    public void onImageProcessComplete(int tag, final boolean success, Sprite sprite, SceneParams params) {
        clearLayout();
        setSprite(sprite, true);
        setColor(Color4F.WHITE);
    }

    @Override
    public void onImageCaptureComplete(int tag, Texture texture, byte[] data, final Size size, final int bpp) {

    }

    public void clearLayout() {
        if (_layout!=null) {
            removeChild(_layout);
            _layout = null;
        }
    }

    public void setLayout(SMView view) {
        clearLayout();
        view.setCascadeAlphaEnable(true);
        addChild(view);
        _layout = view;
    }

    private boolean _colorSelectable = false;
    public boolean isColorSelectable() {return _colorSelectable;}
    public void setColorSelectable(boolean colorSelectable) {_colorSelectable = colorSelectable;}
    public void prepareRemove() {
        if (_layout!=null) {
            ImageProcessor.getInstance().executeImageProcess(this, this, new ImgPrcSimpleCapture(), 0);
        }
    }

    @Override
    public void onImageProcessProgress(final int tag, final float progress) { }

    @Override
    public void resetImageProcess() {
        ListIterator<ImageProcessTask> iter = _imageProcessTask.listIterator();
        while (iter.hasNext()) {
            ImageProcessTask task = iter.next();
            if (task.isRunning()) {
                task.interrupt();
            }
        }

        _imageProcessTask.clear();
    }

    @Override
    public void removeImageProcessTask(ImageProcessTask task) {
        ListIterator<ImageProcessTask> iter = _imageProcessTask.listIterator();
        while (iter.hasNext()) {
            ImageProcessTask iterTask = iter.next();
            if (!iterTask.isTargetAlive()) {
                _imageProcessTask.remove(iterTask);
            } else if (iterTask!=null && task==iterTask) {
                task.interrupt();
                _imageProcessTask.remove(iterTask);
            }
        }
    }

    @Override
    public boolean addImageProcessTask(ImageProcessTask task) {
        ListIterator<ImageProcessTask> iter = _imageProcessTask.listIterator();
        while (iter.hasNext()) {
            ImageProcessTask iterTask = iter.next();
            if (!iterTask.isTargetAlive()) {
                _imageProcessTask.remove(iterTask);
            } else if (task!=null && task==iterTask && iterTask.isRunning()) {
                return false;
            }
        }

        _imageProcessTask.add(task);
        return true;
    }
}
