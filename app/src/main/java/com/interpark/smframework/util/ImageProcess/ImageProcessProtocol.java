package com.interpark.smframework.util.ImageProcess;

import android.media.Image;

import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.util.Size;

import java.util.ArrayList;

public interface ImageProcessProtocol {
    public void onImageProcessComplete(int tag, final boolean success, Sprite sprite, SceneParams params);

    public void onImageCaptureComplete(int tag, Texture texture, byte[] data, final Size size, final int bpp);

    public void onImageProcessProgress(final int tag, final float progress);

    public void resetImageProcess();

    public void removeImageProcessTask(ImageProcessTask task);
    public boolean addImageProcessTask(ImageProcessTask task);

    public ArrayList<ImageProcessTask> _imageProcessTask = new ArrayList<>();
}

/*
// must override
// ImageProcessProtocol
    @Override
    public void onImageProcessComplete(int tag, final boolean success, Sprite sprite, SceneParams params);
    @Override
    public void onImageCaptureComplete(int tag, Texture texture, byte[] data, final Size size, final int bpp);
    @Override
    public void onImageProcessProgress(final int tag, final float progress);
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
            } else if (task==iterTask) {
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

 */
