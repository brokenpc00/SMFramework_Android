package com.interpark.smframework.base.sprite;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.CanvasTexture;
import com.interpark.smframework.base.texture.Texture;

public class CanvasSprite extends Sprite {
    public static CanvasSprite createCanvasSprite(IDirector director, int width, int height, String keyName) {
        Texture texture = director.getTextureManager().createCanvasTexture(width, height, keyName);
        return new CanvasSprite(director, texture);
    }

    protected CanvasSprite(IDirector director, Texture texture) {
        super(director,
                texture.getWidth(),
                texture.getHeight(),
                0,//cx,
                0,//cy,
                0,
                0,
                texture);
    }

    public boolean setRenderTarget(IDirector director, boolean turnOn) {
        return ((CanvasTexture)getTexture()).setRenderTarget(director, turnOn);
    }

    public void clear() {
        ((CanvasTexture)getTexture()).clear();
    }
}
