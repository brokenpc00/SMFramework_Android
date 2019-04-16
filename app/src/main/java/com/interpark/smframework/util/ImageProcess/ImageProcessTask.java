package com.interpark.smframework.util.ImageProcess;

import com.interpark.smframework.base.SMView;

import java.lang.ref.WeakReference;

public class ImageProcessTask {
    public static ImageProcessTask createTaskForTarget(ImageProcessProtocol target) {
        ImageProcessTask task = new ImageProcessTask();
        task._target = new WeakReference<>(target);
        task._this = task;
        task._processor = ImageProcessor.getInstance();

        return task;
    }

    private ImageProcessTask _this = null;


    public ImageProcessTask() {
        _running = false;
        _function = null;
    }

    public boolean init(SMView view, ImageProcessFunction function, final int tag) {
        _function = new WeakReference<>(function);
        _function.get().setTask(this);
        _tag = tag;

        if (_function.get().onPreProcess(view)) {
            _processor.handleState(this, ImageProcessor.State.INIT_SUCCESS);
            return true;
        } else {
            _processor.handleState(this, ImageProcessor.State.INIT_FAILED);
            return false;
        }
    }

    public void procImageProcessThread() {
        try {
            do {

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

                if (_function.get().onProcessInBackground()) {
                    /*----------Check Thread Interrupt----------*/
                    Thread.sleep(1); if (!_running) break;
                    /*------------------------------------------*/

                    // success
                    _processor.handleState(this, ImageProcessor.State.PROCESS_SUCCESS);
                    return;
                }

                /*----------Check Thread Interrupt----------*/
                Thread.sleep(1); if (!_running) break;
                /*------------------------------------------*/

            } while (false);

        } catch (InterruptedException e) {

        }

        // fail
        _processor.handleState(this, ImageProcessor.State.PROCESS_FAILED);
    }

    public void onProgress(float progress) {
        _processor.handleState(this, ImageProcessor.State.PROGRESS, 0, progress);
    }

    public void interrupt() {
        _running = false;
        if (_function.get()!=null)
            _function.get().interrupt();
    }

    public boolean isRunning() {return _running;}
    public boolean isTargetAlive() {
        return getTarget()!=null;
    }

    public int getTag() {return _tag;}
    public ImageProcessProtocol getTarget() {return _target.get();}
    public ImageProcessFunction getProcessFunction() {return _function.get();}

    private boolean _running = false;
    private int _tag = 0;
    private ImageProcessor _processor = null;
    private WeakReference<ImageProcessProtocol> _target;
    private WeakReference<ImageProcessFunction> _function;
}
