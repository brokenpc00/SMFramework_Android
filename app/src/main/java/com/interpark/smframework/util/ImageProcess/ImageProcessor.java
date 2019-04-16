package com.interpark.smframework.util.ImageProcess;

import android.graphics.Bitmap;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.SMDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.BitmapTexture;
import com.interpark.smframework.base.types.PERFORM_SEL;
import com.interpark.smframework.util.ImageManager.ImageDownloader;
import com.interpark.smframework.util.ImageManager.ImageThreadPool;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cz.msebera.android.httpclient.cookie.SM;

public class ImageProcessor {
    private static ImageProcessor _processInstance = null;
    public static ImageProcessor getInstance() {
        if (_processInstance==null) {
            _processInstance = new ImageProcessor();
        }
        return _processInstance;
    }

    public ImageProcessor() {
        _processThreadPool = null;
        init();
    }


    @Override
    public void finalize() throws Throwable {
        try {
            _processThreadPool.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.finalize();
        }
    }

    protected void init() {
        _processThreadPool = new ImageThreadPool(1);
    }


    public void executeImageProcess(ImageProcessProtocol target, SMView view, ImageProcessFunction function) {
        executeImageProcess(target, view, function, 0);
    }
    public void executeImageProcess(ImageProcessProtocol target, SMView view, ImageProcessFunction function, final int tag) {
        ImageProcessTask task = ImageProcessTask.createTaskForTarget(target);
        task.init(view, function, tag);
    }

    public void cancelImageProcess(ImageProcessProtocol target) {
        if (target!=null) {
            target.resetImageProcess();
        }
    }

    public enum State {
        INIT_SUCCESS,
        INIT_FAILED,

        PROCESS_SUCCESS,
        PROCESS_FAILED,

        PROGRESS,
    }

    public void handleState(ImageProcessTask task, State state) {
        handleState(task, state, 0);
    }
    public void handleState(ImageProcessTask task, State state, int intParam) {
        handleState(task, state, intParam, 0);
    }
    public void handleState(final ImageProcessTask task, State state, int intParam, final float floatParam) {
        switch (state) {
            case INIT_SUCCESS:
            {
                if (task.isTargetAlive()) {
                    if (task.getProcessFunction().isCaptureOnly()) {
                        BitmapSprite sprite = null;

                        BitmapTexture capturedTexture = (BitmapTexture)task.getProcessFunction().getCapturedTexture();
                        if (capturedTexture!=null) {
                            sprite = BitmapSprite.createFromTexture(SMDirector.getDirector(), capturedTexture);
                        }

                        task.getTarget().onImageProcessComplete(task.getTag(), true, sprite, task.getProcessFunction().getParam());
                        task.getTarget().removeImageProcessTask(task);
                        break;
                    } else {
                        // next step.
                        task.getTarget().onImageCaptureComplete(task.getTag(),
                                                                task.getProcessFunction().getCapturedTexture(),
                                                                task.getProcessFunction().getInputData(),
                                                                task.getProcessFunction().getInputSize(),
                                                                task.getProcessFunction().getInputBpp()
                                                                );
                    }
                }

                _processThreadPool.addTask(new PERFORM_SEL() {
                    @Override
                    public void performSelector() {
                        task.procImageProcessThread();
                    }
                });

            }
            break;
            case INIT_FAILED:
            {
                if (task.isTargetAlive()) {
                    task.getTarget().onImageProcessComplete(task.getTag(), false, null, null);
                    task.getTarget().removeImageProcessTask(task);
                }
            }
            break;
            case PROCESS_FAILED:
            {
                _mutex_process.lock();
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(new PERFORM_SEL() {
                            @Override
                            public void performSelector() {
                                if (task.isTargetAlive()) {
                                    task.getTarget().onImageProcessComplete(task.getTag(), false , null, null);
                                    task.getTarget().removeImageProcessTask(task);
                                }
                            }
                        });
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_process.unlock();
                }
            }
            break;
            case PROCESS_SUCCESS:
            {
                _mutex_process.lock();
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(new PERFORM_SEL() {
                            @Override
                            public void performSelector() {
                                if (task.isTargetAlive()) {
                                    task.getTarget().onImageProcessComplete(task.getTag(), true, task.getProcessFunction().onPostProcess(), task.getProcessFunction().getParam());
                                    task.getTarget().removeImageProcessTask(task);
                                }
                            }
                        });
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_process.unlock();
                }
            }
            break;
            case PROGRESS:
            {
                _mutex_process.lock();
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(new PERFORM_SEL() {
                            @Override
                            public void performSelector() {
                                if (task.isTargetAlive()) {
                                    task.getTarget().onImageProcessProgress(task.getTag(), floatParam);
                                }
                            }
                        });
                    }
                } catch (Exception e) {

                } finally {
                    _mutex_process.unlock();
                }
            }
            break;
        }
    }

    private Lock _mutex_process = new ReentrantLock(true);
    private ImageThreadPool _processThreadPool = null;
}
