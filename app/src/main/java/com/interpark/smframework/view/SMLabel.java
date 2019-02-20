package com.interpark.smframework.view;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base._UIContainerView;
import com.interpark.smframework.base.sprite.TextSprite;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMLabel extends _UIContainerView {

    private TextSprite _text = null;
    private float _fontSize = 0.0f;
    private Color4F _fontColor = new Color4F(0, 0, 0, 1);
    private String _str = "";
    private boolean _isSep = false;
    private SMView _letterConainerView = null;
    private SMLabel[] _letters = null;

    public SMLabel(IDirector director) {
        super(director);
    }

    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor) {
        SMLabel label = new SMLabel(director);
        label.initWithFont(text, fontSize, fontColor);
        return label;
    }

    public static SMLabel create(IDirector director, String text, float fontSize, float textColorR, float textColorG, float textColorB, float textColorA) {
        return create(director, text, fontSize, new Color4F(textColorR, textColorG, textColorB, textColorA));
    }

    public void initWithFont(final String text, final float fontSize, final Color4F fontColor) {
        _str = text;
        _fontSize = fontSize;
        _fontColor = fontColor;

        _text = TextSprite.createTextSprite(getDirector(), text, fontSize);
        setContentSize(new Size(_text.getWidth(), _text.getHeight()));
    }

    public void makeSeparate() {
        makeSeparate(true);
    }
    public void makeSeparate(boolean make) {
        _isSep = make;

        int len = _str.length();

        if (_letterConainerView!=null) {
            if (_letters!=null) {
                for (int i=0; i<len; i++) {
                    if (_letters[i]!=null) {
                        _letters[i].removeFromParent();
                        _letters[i] = null;
                    }
                }
                _letters = null;
            }
            _letterConainerView.removeAllChildren();
            _letterConainerView.removeFromParent();
            _letterConainerView = null;
        }

        if (_isSep) {
            _letterConainerView = SMView.create(getDirector());
            _letterConainerView.setAnchorPoint(new Vec2(0, 0));
            _letterConainerView.setPosition(new Vec2(0, 0));
            _letterConainerView.setContentSize(_contentSize);
            addChild(_letterConainerView);

            _letters = new SMLabel[len];
            float posX = 0.0f;
            for (int i=0; i<len; i++) {
                String str = _text.getText().substring(i, i=1);
                SMLabel letter = SMLabel.create(getDirector(), str, _fontSize, _fontColor);
                letter.setAnchorPoint(Vec2.MIDDLE);
                posX += letter.getContentSize().width/2;
                letter.setPosition(new Vec2(posX, _letterConainerView.getContentSize().height/2));
                _letterConainerView.addChild(letter);
                posX += letter.getContentSize().width/2;
                _letters[i] = letter;
            }
        }
    }

    public int getStringLength() {
        return _str.length();
    }

    public String getText() {
        return _text.getText();
    }

    public boolean isSeparateMode() {return _isSep;}

    public void setText(final String text) {
        _text.setText(text);
    }

    public SMLabel getLetter(int index) {
        if (!_isSep) return null;

        if (index>_letters.length-1) return null;

        return _letters[index];
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        _fontColor.set(color);
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        _fontColor = new Color4F(r, g, b, a);
    }

    @Override
    protected void render(float a) {
        if (_isSep) return;
        getDirector().setColor(_fontColor.r, _fontColor.g, _fontColor.b, _fontColor.a);
        _text.draw(_text.getWidth()/2, _text.getHeight()/2);
    }

}
