package com.interpark.smframework.base.animator;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

public class ColorAnimator extends Animator {
    private float[] mFrom;
    private float[] mTo;

    public ColorAnimator(IDirector director) {
        super(director);
    }

    public Animator setBackgroundColor(float oldr, float oldg, float oldb, float olda, float r, float g, float b, float a) {
        if (mFrom == null) {
            mFrom = new float[4];
        }
        mFrom[0] = oldr;
        mFrom[1] = oldg;
        mFrom[2] = oldb;
        mFrom[3] = olda;

        if (mTo == null) {
            mTo = new float[4];
        }
        mTo[0] = r;
        mTo[1] = g;
        mTo[2] = b;
        mTo[3] = a;

        return this;
    }


    @Override
    protected void onAnimationEnd() {
        mFrom = null;
        mTo = null;
    }

    @Override
    protected void update(SMView view, float t) {
        view.setBackgroundColor(
                mFrom[0]+(mTo[0]-mFrom[0])*t,
                mFrom[1]+(mTo[1]-mFrom[1])*t,
                mFrom[2]+(mTo[2]-mFrom[2])*t,
                mFrom[3]+(mTo[3]-mFrom[3])*t, false);
    }
}
