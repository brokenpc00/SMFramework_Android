package com.interpark.smframework.base;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class _UIContainerView extends SMView {
    public _UIContainerView(IDirector director) {
        super(director);
        _uiContainer = SMView.create(director);
        _uiContainer.setAnchorPoint(new Vec2(0.5f, 0.5f));
        addChild(_uiContainer, 0, "");
    }

    public SMView getUIContainer() {return _uiContainer;}
    protected SMView _uiContainer = null;

    protected void setSMViewContentSize(final float width, final float height) {
        setSMViewContentSize(new Size(width, height));
    }
    protected void setSMViewContentSize(final Size size) {
        super.setContentSize(size);
    }

    @Override
    public void setContentSize(final Size size) {
        Size innerSize = new Size(size.width()-_paddingLeft-_paddingRight, size.height()-_paddingTop-_paddingBottom);

        Vec2 pos = new Vec2((_paddingLeft+(size.width()-_paddingRight))/2, (_paddingBottom + (size.height()-_paddingTop))/2);
//        _uiContainer->setPosition((_paddingLeft + (size.width - _paddingRight))/2, (_paddingBottom + (size.height - _paddingTop))/2);
        _uiContainer.setPosition(pos);
        _uiContainer.setContentSize(innerSize);

        super.setContentSize(size);
    }

    public void setPadding(final float padding) {
        setPadding(padding, padding, padding, padding);
    }

    public void setPadding(final float left, final float top, final float right, final float bottom) {
        _paddingLeft = left;
        _paddingTop = top;
        _paddingRight = right;
        _paddingBottom = bottom;
        setContentSize(getContentSize());
    }

    public float getPaddingTop() {return _paddingTop;}
    public float getPaddingBottom() {return _paddingBottom;}
    public float getPaddingLeft() {return _paddingLeft;}
    public float getPaddingRight() {return _paddingRight;}

    protected float _paddingLeft = 0;
    protected float _paddingRight = 0;
    protected float _paddingTop = 0;
    protected float _paddingBottom = 0;

}
