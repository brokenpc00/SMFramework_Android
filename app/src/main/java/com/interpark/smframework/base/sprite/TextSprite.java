package com.interpark.smframework.base.sprite;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.TextTexture;

import android.graphics.Paint.Align;

public class TextSprite extends Sprite {
    public static TextSprite createTextSprite(IDirector director, String text, float fontSize) {
        return createTextSprite(director, text, fontSize, Align.CENTER, false, false, false);
    }

    public static TextSprite createTextSprite(IDirector director, String text, float fontSize, Align align, boolean bold, boolean italic, boolean strikeThru) {
        return createTextSprite(director, text, fontSize, align, bold, italic, strikeThru, -1, 1);
    }

    public static TextSprite createTextSprite(IDirector director, String text, float fontSize, Align align, boolean bold, boolean italic, boolean strikeThru, int maxWidth, int maxLines) {
        TextTexture texture = (TextTexture)director.getTextureManager().createTextureFromString(text, fontSize, align, bold, italic, strikeThru, maxWidth, maxLines);
        return new TextSprite(director, texture, texture.getWidth()/2, texture.getHeight()/2);
    }

    public static TextSprite createHtmlSprite(IDirector director, String text, float fontSize, boolean bold, boolean italic, boolean strikeThru) {
        TextTexture texture = (TextTexture)director.getTextureManager().createTextureFromHtmlString(text, fontSize, bold, italic, strikeThru);
        return new TextSprite(director, texture, texture.getWidth()/2, texture.getHeight()/2);
    }

    private TextSprite(IDirector director, TextTexture texture, float cx, float cy) {
        super(director,
                texture.getBounds().width(),
                texture.getBounds().height(),
                texture.getBounds().width()/2,
                texture.getBounds().height()/2,
                texture.getBounds().left,
                texture.getBounds().top,
                texture);
    }

    public String getText() {
        String retText = "";
        TextTexture texture = (TextTexture)getTexture();
        if (texture!=null) {
            retText = texture.getText();
        } else {
            retText = "";
        }

        return retText;
    }

    public void setText(String text) {
        if (((TextTexture)getTexture()).updateText(director, text)) {
            TextTexture texture = (TextTexture)getTexture();

            this.tx = texture.getBounds().left;
            this.ty = texture.getBounds().top;
            this.tw = texture.getWidth();
            this.th = texture.getHeight();

            initRect(director,
                    texture.getBounds().width(),
                    texture.getBounds().height(),
                    texture.getBounds().width()/2,
                    texture.getBounds().height()/2);
        }
    }

    public int getLineCount() {
        return ((TextTexture)getTexture()).getLineCount();
    }
}
