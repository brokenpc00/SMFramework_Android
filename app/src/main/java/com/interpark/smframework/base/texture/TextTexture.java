package com.interpark.smframework.base.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.TextTextureUtil;

public class TextTexture extends Texture {
    private static final int TEXT_PADDING = 4;
    private static final int MAX_TEXT_WIDTH = 1024-(TEXT_PADDING*2);

    private String mText;
    private final float mFontSize;
    private final Paint.Align mAlign;
    private final boolean mBold;
    private final boolean mItalic;
    private final boolean mStrikeThru;
    private final Rect mBounds;
    private float mCX, mCY;
    private int mMaxWidth;
    private int mMaxLines;
    private int mLineCount;
    private float mSpaceMult = 1f;
    private boolean mIsHtml = false;

    protected TextTexture(IDirector director, String key, String text, float fontSize, Paint.Align align, boolean bold, boolean italic, boolean strikeThru) {
        this(director, key, text, fontSize, align, bold, italic, strikeThru, -1, 1);
    }

    protected TextTexture(IDirector director, String key, String text, float fontSize, Paint.Align align, boolean bold, boolean italic, boolean strikeThru, int maxWidth, int maxLine) {
        super(director, key, false, null);
        mText = text;
        mFontSize = fontSize;
        mAlign = align;
        mBold = bold;
        mItalic = italic;
        mStrikeThru = strikeThru;
        mBounds = new Rect();
        mMaxWidth = maxWidth;
        mMaxLines = maxLine;
        mLineCount = 0;
        mIsHtml = false;
        initTextureDimen(director.getContext());
    }

    protected TextTexture(IDirector director, String key, String text, float fontSize, Paint.Align align, boolean bold, boolean italic, boolean strikeThru, boolean isHtml) {
        super(director, key, false, null);
        mText = text;
        mFontSize = fontSize;
        mAlign = align;
        mBold = bold;
        mItalic = italic;
        mStrikeThru = strikeThru;
        mBounds = new Rect();
        mMaxWidth = -1;
        mMaxLines = 0;
        mLineCount = 0;
        mIsHtml = isHtml;
        initTextureDimen(director.getContext());
    }

    public Rect getBounds() {
        return mBounds;
    }

    public float getCX() {
        return mCX;
    }

    public float getCY() {
        return mCY;
    }

    public int getLineCount() {
        return mLineCount;
    }

    @Override
    public boolean loadTexture(IDirector director, Bitmap unused) {
        Bitmap bitmap =  loadTextureBitmap(director.getContext());
        if (bitmap != null) {
            GLES20.glGenTextures(1, mTextureId, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  mTextureId[0]);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            return true;
        }
        return false;
    }

    //	private static final float SPACING_MULT = 1.5f;
    private static final float SPACING_ADD = 0.0f;
    private static TextPaint sTextPaint = new TextPaint();
    private static Canvas sTextCanvas = new Canvas();

    private void initPaint() {
        sTextPaint.setTypeface(Typeface.DEFAULT);
        sTextPaint.setColor(0xFFFFFFFF);

        sTextPaint.setStyle(Paint.Style.FILL);
        sTextPaint.setAntiAlias(true);
        sTextPaint.setTextAlign(mAlign);
        sTextPaint.setTextSize(mFontSize);
        sTextPaint.setFakeBoldText(mBold);
        sTextPaint.setTextSkewX(mItalic?-.20f:0f);
        sTextPaint.setStrikeThruText(mStrikeThru);
    }

    static final int[] sLineCount = new int[1];
    @Override
    protected void initTextureDimen(Context context) {

        initPaint();

        if (mMaxWidth > 0 && !mIsHtml) {
            mText = TextTextureUtil.getDivideString(sTextPaint, mText, mMaxWidth, mMaxLines, sLineCount);
            if (sLineCount[0] > 1) {
                mSpaceMult = 1.5f;
            } else {
                mSpaceMult = 1f;
            }
        }

        StaticLayout layout;
        if (mIsHtml) {
            layout = new StaticLayout(Html.fromHtml(mText), sTextPaint, MAX_TEXT_WIDTH, Layout.Alignment.ALIGN_NORMAL, mSpaceMult, SPACING_ADD, false);
        } else {
            layout = new StaticLayout(mText, sTextPaint, MAX_TEXT_WIDTH, Layout.Alignment.ALIGN_NORMAL, mSpaceMult, SPACING_ADD, false);
        }

        float l = Integer.MAX_VALUE;
        float t = Integer.MAX_VALUE;
        float r = 0;
        float b = 0;
        int lineCount = layout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            l = Math.min(layout.getLineLeft(i), l);
            t = Math.min(layout.getLineTop(i), t);
            r = Math.max(layout.getLineRight(i), r);
            b = Math.max(layout.getLineBottom(i), b);
        }
        int ll = Math.max((int)Math.floor(l), 0);
        int tt = Math.max((int)Math.floor(t), 0);
        int rr = Math.min((int)Math.ceil(r), MAX_TEXT_WIDTH)+TEXT_PADDING*2;
        int bb = Math.min((int)Math.ceil(b), MAX_TEXT_WIDTH)+TEXT_PADDING*2;
        mBounds.set(ll, tt, rr, bb);

        mOriginalWidth = mWidth = mBounds.width();
        mOriginalHeight = mHeight = mBounds.height();

        mCX = ll+mWidth/2f;
        float b1 = layout.getLineBaseline(0);
        float b2 = layout.getLineBaseline(lineCount-1);
        mCY = tt+TEXT_PADDING + b1 + (b2-b1)/2f;

        mLineCount = lineCount;
    }

    @Override
    protected Bitmap loadTextureBitmap(Context context) {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        initPaint();
        //Canvas canvas = new Canvas();
        sTextCanvas.setBitmap(bitmap);
        sTextCanvas.save();

        switch (mAlign) {
            case LEFT:
                sTextCanvas.translate(mBounds.left+TEXT_PADDING, TEXT_PADDING);
                break;
            case RIGHT:
                sTextCanvas.translate(mBounds.right-TEXT_PADDING, TEXT_PADDING);
                break;
            default: // CENTER
                sTextCanvas.translate(mCX, TEXT_PADDING);
                break;
        }
        //sTextCanvas.drawColor(0xFF00FF00);
        StaticLayout layout;
        if (mIsHtml) {
            layout = new StaticLayout(Html.fromHtml(mText), sTextPaint, MAX_TEXT_WIDTH, Layout.Alignment.ALIGN_NORMAL, mSpaceMult, SPACING_ADD, false);
        } else {
            layout = new StaticLayout(mText, sTextPaint, MAX_TEXT_WIDTH, Layout.Alignment.ALIGN_NORMAL, mSpaceMult, SPACING_ADD, false);
        }
        layout.draw(sTextCanvas);
        sTextCanvas.restore();

        return bitmap;
    }

    public String getText() {
        return mText;
    }

    public boolean updateText(IDirector director, String text) {
        if (text == null || text.length() <= 0 || text.equals(mText))
            return false;
        mText = text;

        deleteTexture(director.isGLThread());
        initTextureDimen(director.getContext());
        if (director.isGLThread()) {
            loadTexture(director, null);
        }

        return true;
    }
}
