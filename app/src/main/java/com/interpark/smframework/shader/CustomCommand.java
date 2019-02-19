package com.interpark.smframework.shader;

public class CustomCommand extends RenderCommand {
    public CustomCommand() {
        _type = Type.CUSTOM_COMMAND;
    }

    public void init(float globalZOrder, final float[] modelViewTransform, long flags) {
        super.init(_globalOrder, modelViewTransform, flags);
    }

    public void init(float globalOrder) {
        _globalOrder = globalOrder;
    }

    public void execute() {
        if (func!=null) {
            func.func();;
        }
    }

    public boolean isTranslucent() {return true;}

    public interface CustomCommandFunc {
        public void func();
    }
    public CustomCommandFunc func = null;
}
