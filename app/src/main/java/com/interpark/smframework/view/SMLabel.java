package com.interpark.smframework.view;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base._UIContainerView;
import com.interpark.smframework.base.sprite.TextSprite;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;

public class SMLabel extends _UIContainerView {

    private TextSprite _text;
    private float coloR, colorG, colorB, alpha;


    public SMLabel(IDirector director, String text, float fontSize) {
        this(director, text, fontSize, 0, 0, 0, 1);
    }

    public SMLabel(IDirector director, String text, float fontSize, float textColorR, float textColorG, float textColorB, float textColorA) {
        super(director);

        _text = TextSprite.createTextSprite(director, text, fontSize);
        coloR = textColorR;
        colorG = textColorG;
        colorB = textColorB;
        alpha = textColorA;

        setContentSize(new Size(_text.getWidth(), _text.getHeight()));
    }

    public static SMLabel create(IDirector director, String text, float fontSize, float textColorR, float textColorG, float textColorB, float textColorA) {
        return new SMLabel(director, text, fontSize, textColorR, textColorG, textColorB, textColorA);
    }

    public String getText() {
        return _text.getText();
    }

    public void setText(final String text) {
        _text.setText(text);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        coloR = color.r;
        colorG = color.g;
        colorB = color.b;
        alpha = color.a;
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        coloR = r;
        colorG = g;
        colorB = b;
        alpha = a;
    }

    @Override
    protected void render(float a) {
        getDirector().setColor(coloR, colorG, colorB, alpha);
        _text.draw(_text.getWidth()/2, _text.getHeight()/2);
    }

}
