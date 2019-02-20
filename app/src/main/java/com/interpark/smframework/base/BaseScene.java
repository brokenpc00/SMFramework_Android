package com.interpark.smframework.base;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class BaseScene extends SMView {
    public BaseScene(IDirector director) {
        super(director);
        setAnchorPoint(Vec2.MIDDLE);

    }

    public static BaseScene create(IDirector director) {
        BaseScene scene = new BaseScene(director);
        if (scene!=null) {
            scene.init();
        }

        return scene;
    }

    public static BaseScene createWithSize(IDirector director, Size size) {
        BaseScene scene = new BaseScene(director);
        if (scene!=null) {
            scene.initWithSize(size);
        }

        return scene;
    }

    @Override
    protected boolean init() {
        Size size = new Size(getDirector().getWidth(), getDirector().getHeight());
        return initWithSize(size);
    }

    protected boolean initWithSize(Size size) {
        setContentSize(size);
        return true;
    }

}
