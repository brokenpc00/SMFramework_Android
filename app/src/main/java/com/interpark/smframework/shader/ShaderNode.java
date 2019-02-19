package com.interpark.smframework.shader;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.Color4F;

public class ShaderNode extends SMView {
    public ShaderNode(IDirector director) {
        super(director);
    }

    public enum Quadrant {
        ALL,            // 0
        LEFT_HALF,      // 1
        RIGHT_HALF,     // 2
        TOP_HALF,       // 3
        BOTTOM_HALF,    // 4
        LEFT_TOP,       // 5
        LEFT_BOTTOM,    // 6
        RIGHT_TOP,      // 7
        RIGHT_BOTTOM    // 8
    }

    public final float DEFAULT_ANTI_ALIAS_WIDTH = 1.5f;

    @Override
    public void render(float a) {

    }

    public void setColor4F(final Color4F color) {

    }

//    public Color4F getColor4F() {
//
//    }
//
//    @Override
//    public boolean init() {
//
//    }
//
//    public boolean initWithShaderKey(final int shaderKey) {
//
//    }
//
//    public void onInitShaderParams()

    protected CustomCommand _customCommand;
    protected int _uniformColor;
    protected BlendFunc _blendFunc;
    int _quadrant;
}
