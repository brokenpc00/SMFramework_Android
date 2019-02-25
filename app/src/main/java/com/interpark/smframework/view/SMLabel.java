package com.interpark.smframework.view;

import android.graphics.Paint.Align;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base._UIContainerView;
import com.interpark.smframework.base.sprite.TextSprite;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMLabel extends _UIContainerView {

    private TextSprite _textSprite = null;
    private float _fontSize = 0.0f;
//    private Color4F _fontColor = new Color4F(Color4F.TEXT_BLACK);
    private String _text = "";
    private boolean _isSep = false;
    private SMView _letterConainerView = null;
    private SMLabel[] _letters = null;
    private Align _align = Align.CENTER;
    private boolean _bold = false;
    private boolean _italic = false;
    private boolean _strike = false;
    private int _maxWidth = -1;
    private int _maxLines = -1;

    public SMLabel(IDirector director) {
        super(director);
    }

    public static SMLabel create(IDirector director, String text, float fontSize) {
        return create(director, text, fontSize, Color4F.TEXT_BLACK);
    }
    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor) {
        return create(director, text, fontSize, fontColor, Align.CENTER);
    }
    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor, Align align) {
        return create(director, text, fontSize, fontColor, align, false);
    }
    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor, Align align, boolean bold) {
        return create(director, text, fontSize, fontColor, align, false, false);
    }
    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor, Align align, boolean bold, boolean italic) {
        return create(director, text, fontSize, fontColor, align, false, false, false);
    }
    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor, Align align, boolean bold, boolean italic, boolean strike) {
        return create(director, text, fontSize, fontColor, align, false, false, false, -1);
    }
    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor, Align align, boolean bold, boolean italic, boolean strike, int maxWidth) {
        return create(director, text, fontSize, fontColor, align, false, false, false, -1, 1);
    }
    public static SMLabel create(IDirector director, String text, float fontSize, Color4F fontColor, Align align, boolean bold, boolean italic, boolean strike, int maxWidth, int maxLines) {
        SMLabel label = new SMLabel(director);
        label.initWithFont(text, fontSize, fontColor, align, bold, italic, strike, maxWidth, maxLines);
        return label;
    }

    public void initWithFont(final String text, final float fontSize, final Color4F fontColor, Align align, boolean bold, boolean italic, boolean strike, int maxWidth, int maxLines) {
        _text = text;
        _fontSize = fontSize;
//        _fontColor.set(fontColor);
        setTintColor(fontColor);
        _align = align;
        _bold = bold;
        _italic = italic;
        _strike = strike;
        _maxWidth = maxWidth;
        _maxLines = maxLines;

        makeTextSprite();
    }

    private void makeTextSprite() {
        if (_textSprite!=null) {
            _textSprite.releaseResources();
        }
        _textSprite = TextSprite.createTextSprite(getDirector(), _text, _fontSize, _align, _bold, _italic, _strike, _maxWidth, _maxLines);
        setContentSize(new Size(_textSprite.getWidth(), _textSprite.getHeight()));
    }

    public void setBold(boolean bold) {
        if (_bold==bold) return;

        _bold = bold;
        makeTextSprite();
    }

    public void setItalic(boolean italic) {
        if (_italic==italic) return;
        _italic = italic;
        makeTextSprite();
    }

    public void setStrike(boolean strike) {
        if (_strike==strike) return;
        _strike = strike;
        makeTextSprite();
    }

    public void setMaxWidth(int maxWidth) {
        if (_maxWidth==maxWidth) return;
        _maxWidth = maxWidth;
        makeTextSprite();
    }

    public void setMaxLines(int maxLines) {
        if (_maxLines==maxLines) return;
        _maxLines = maxLines;
        makeTextSprite();
    }

    public void makeSeparate() {
        makeSeparate(true);
    }
    public void makeSeparate(boolean make) {
        _isSep = make;

        int len = _text.length();

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
                String str = _textSprite.getText().substring(i, i=1);
                SMLabel letter = SMLabel.create(getDirector(), str, _fontSize, new Color4F(_tintColor));
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
        return _text.length();
    }

    public String getText() {
        return _textSprite.getText();
    }

    public boolean isSeparateMode() {return _isSep;}

    public void setText(final String text) {
        _textSprite.setText(text);
    }

    public SMLabel getLetter(int index) {
        if (!_isSep) return null;

        if (index>_letters.length-1) return null;

        return _letters[index];
    }

    public void setFontColor(final Color4F color) {
        setTintColor(color);
    }

    public void setColor(final Color4F color) {
        setTintColor(color);
    }

//    @Override
//    public void updateTintColor() {
//        _fontColor.set(new Color4F(_tintColor));
//    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setTintColor(color);
    }

    @Override
    public void setBackgroundColor(float r, float g, float b, float a) {
        setTintColor(r, g, b, a);
    }

    @Override
    protected void render(float a) {
        if (_isSep) return;

        setRenderColor(a);

        _textSprite.draw(_textSprite.getWidth()/2, _textSprite.getHeight()/2);
    }

}
