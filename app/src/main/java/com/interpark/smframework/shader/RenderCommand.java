package com.interpark.smframework.shader;

import android.util.Log;

import com.interpark.smframework.base.SMView;

public class RenderCommand {

    public RenderCommand() {

    }

    public enum Type {
        UNKNOWN_COMMAND,
        QUAD_COMMAND,
        CUSTOM_COMMAND,
        BATCH_COMMAND,
        GROUP_COMMAND,
        MESH_COMMAND,
        PRIMITIVE_COMMAND,
        TRIANGLES_COMMAND
    }

    public void init(float globalZOrder, float[] modelViewTransform, long flags) {
        _globalOrder = globalZOrder;
        if ((flags & SMView.FLAGS_RENDER_AS_3D) > 0) {
            set3D(true);
        } else {
            set3D(false);
            _depth = 0;
        }
    }

    public float getGlobalOrder() {return _globalOrder;}
    protected float _globalOrder = 0;

    public Type getType() {return _type;}
    protected Type _type = Type.UNKNOWN_COMMAND;

    public boolean isTransparent() {return _isTransparent;}
    public void setTransparent(boolean transparent) {_isTransparent = transparent;}
    protected boolean _isTransparent = true;

    public boolean isSkipBatching() {return _skipBatching;}
    public void setSkipBatching(boolean skipBatching) {_skipBatching = skipBatching;}
    protected boolean _skipBatching = false;

    public boolean is3D() {return _is3D;}
    public void set3D(boolean value) {_is3D=value;}
    protected boolean _is3D = false;

    public float getDepth() {return _depth;}
    protected float _depth = 0;

    protected void printID() {
        Log.i("RenderCommander", "[[[[[ Command Depth : " + _globalOrder);
    }



}
