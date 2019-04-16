package com.interpark.smframework.util.ImageProcess;

import com.interpark.smframework.base.SMView;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class ImgPrcSimpleCapture extends ImageProcessFunction {
    public ImgPrcSimpleCapture() {
        this(1.0f);
    }
    public ImgPrcSimpleCapture(float scale) {
        setCaptureOnly();
        _scale = scale;
    }

    @Override
    public boolean onPreProcess(SMView view) {
        Size size = view.getContentSize();
        return startProcess(view, size, size.divide(2).toVec(), Vec2.MIDDLE, _scale, _scale);
    }

    @Override
    public boolean onProcessInBackground() {
        // capture only
        return true;
    }

    private float _scale = 1.0f;
}
