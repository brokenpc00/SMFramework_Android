package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.UIContainerView;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class SMShapeView extends SMView {
    public SMShapeView(IDirector director) {
        super(director);
    }

    public enum Quadrant {
        ALL,
        LEFT_HALF,
        RIGHT_HALF,
        TOP_HALF,
        BOTTOM_HALF,
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_BOTTOM,
    };

    public Quadrant getIntToQuadrent(int value) {
        switch (value) {
            case 1: return Quadrant.LEFT_HALF;
            case 2: return Quadrant.RIGHT_HALF;
            case 3: return Quadrant.TOP_HALF;
            case 4: return Quadrant.BOTTOM_HALF;
            case 5: return Quadrant.LEFT_TOP;
            case 6: return Quadrant.RIGHT_TOP;
            case 7: return Quadrant.RIGHT_BOTTOM;
            default: return Quadrant.ALL;
        }
    }

    public int getQuadrantToInt(Quadrant q) {
        switch (q) {
            case ALL: return 0;
            case LEFT_HALF: return 1;
            case RIGHT_HALF: return 2;
            case TOP_HALF: return 3;
            case BOTTOM_HALF: return 4;
            case LEFT_TOP: return 5;
            case LEFT_BOTTOM: return 6;
            case RIGHT_TOP: return 7;
            case RIGHT_BOTTOM: return 8;
            default: return 0;
        }
    }

    public void setCornerRadius(float radius) {_cornerRadius = radius;}
    public void setAntiAliasWidth(float aaWidth) {_aaWidth = aaWidth;}
    public void setLineWidth(final float lineWidth) {_lineWidth = lineWidth;}
    public void setCornerQuadrant(Quadrant q) {_quadrant = getQuadrantToInt(q);}
    public void setCornerQuadrant(int value) {
        // max 가 넘어가는 걸 방지하려고 미련한 짓 함... 8 넘어가는 걸 체크해도 되지만...
        _quadrant = getQuadrantToInt(getIntToQuadrent(value));
    }

    public float getCornerRadius() {return _cornerRadius;}
    public float getAntiAliasWidth() {return _aaWidth;}
    public float getLineWidth() {return _lineWidth;}

    public static Vec2 getQuadDimen(Quadrant q, final Vec2 size) {
        if (q==Quadrant.ALL) {
            return size;
        }

        switch (q) {
            default: return size;

            case LEFT_HALF:
            case RIGHT_HALF:
            {
                return new Vec2(size.x*2, size.y);
            }

            case TOP_HALF:
            case BOTTOM_HALF:
            {
                return new Vec2(size.x, size.y*2);
            }

            case LEFT_TOP:
            case LEFT_BOTTOM:
            case RIGHT_TOP:
            case RIGHT_BOTTOM:
            {
                return new Vec2(size.x*2, size.y*2);
            }
        }
    }

    @Override
    public void updateColor() {
        if (_shapeColor==null) {
            _shapeColor = new Color4F(Color4F.TRANSPARENT);
        }
        _shapeColor.set(_displayedColor);

    }

    @Override
    protected void draw(float a) {
        getDirector().setColor(_shapeColor.r*a, _shapeColor.g*a, _shapeColor.b*a, _shapeColor.a*a);
    }

    protected Color4F _shapeColor = new Color4F(Color4F.TRANSPARENT);
    protected float _cornerRadius = 0.0f;
    protected float _lineWidth = 1.0f;
    protected float _aaWidth = 0.015f;
    protected int _quadrant;
}
