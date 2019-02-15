package com.interpark.smframework.base.animator;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.animator.Animator;

//public class ViewTransitionAnimator extends Animator {
//    SMView.
//    private float mFromAlpha;
//    private float mToAlpha;
//
//    public AlphaAnimator(IDirector director, float fromAlpha, float toAlpha) {
//        super(director);
//        mFromAlpha = fromAlpha;
//        mToAlpha = toAlpha;
//    }
//
//    public AlphaAnimator(IDirector director, SMView view, float fromAlpha, float toAlpha, long durationMillis) {
//        this(director, fromAlpha, toAlpha);
//        setView(view);
//        setDuration(durationMillis);
//    }
//
//    @Override
//    protected void update(SMView view, float t) {
//        view.setAlpha(mFromAlpha+(mToAlpha-mFromAlpha)*t, false);
//    }
//}
