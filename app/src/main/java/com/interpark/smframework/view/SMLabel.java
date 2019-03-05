package com.interpark.smframework.view;

import android.graphics.Paint.Align;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.UIContainerView;
import com.interpark.smframework.base.sprite.TextSprite;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

import org.apache.http.cookie.SM;

import java.util.ArrayList;

public class SMLabel extends UIContainerView {

    private TextSprite _textSprite = null;
    private float _fontSize = 0.0f;
    private String _text = "";
    private SMView _letterConainerView = null;
    private ArrayList<SMLabel> _letters = null;
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
        setColor(fontColor);
        _align = align;
        _bold = bold;
        _italic = italic;
        _strike = strike;
        _maxWidth = maxWidth;
        _maxLines = maxLines;

        makeTextSprite();
    }

    private void makeTextSprite() {
        releaseGLResources();
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

    public void clearSeparate() {
        if (_letterConainerView!=null) {
            if (_letters!=null) {
                for (SMLabel label : _letters) {
                    label.removeFromParent();
                    label = null;
                }
                _letters.clear();
                _letters = null;
            }
            _letterConainerView.removeAllChildren();
            _letterConainerView.removeFromParent();
            _letterConainerView = null;
        }
    }

    public void makeSeparate() {
        clearSeparate();

        int len = _text.length();
        if (len==0) {
            return;
        }

            _letterConainerView = SMView.create(getDirector());
            _letterConainerView.setAnchorPoint(new Vec2(0, 0));
            _letterConainerView.setPosition(new Vec2(0, 0));
            _letterConainerView.setContentSize(_contentSize);
            addChild(_letterConainerView);

        _letters = new ArrayList<SMLabel>(len);
        float padding = 4.0f;
        float posX = padding;
            for (int i=0; i<len; i++) {
            String str = _text.substring(i, i+1);
            SMLabel letter = SMLabel.create(getDirector(), str, _fontSize, new Color4F(_displayedColor));
                letter.setAnchorPoint(Vec2.MIDDLE);
            float letterSize = letter.getContentSize().width - padding*2;
            posX += letterSize/2;
                letter.setPosition(new Vec2(posX, _letterConainerView.getContentSize().height/2));
                _letterConainerView.addChild(letter);
            posX += letterSize/2;
            _letters.add(letter);
        }
    }

    public int getStringLength() {
        return _text.length();
    }

    public String getText() {
        return _textSprite.getText();
    }

    public int getSeparateCount() {
        if (_letters!=null && _letters.size()>0) {
            return _letters.size();
        }

        return 0;
    }

    public void setText(final String text) {
        _text = text;
        makeTextSprite();
    }

    public SMLabel getLetter(int index) {
        if (_letters==null || index>_letters.size()-1 || index<0) {
            return null;
        }

        return _letters.get(index);
    }

    public void setFontColor(final Color4F color) {
    setColor(color);
}

    @Override
    public void releaseGLResources() {
        if (_textSprite!=null) {
            _textSprite.releaseResources();
            _textSprite = null;
        }
    }

    public void setTestBgColor(Color4F color) {
        super.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setColor(color);
    }

    @Override
    protected void draw(float a) {
        if (_letters!=null && _letters.size()>0) {
            return;
        }

        if (_textSprite==null) return;;

        setRenderColor(a);

        _textSprite.draw(_textSprite.getWidth()/2, _textSprite.getHeight()/2);
    }

}
