package com.interpark.smframework.view;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayBaseAction;
import com.interpark.smframework.base.types.SEL_SCHEDULE;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.util.Size;

import java.util.ArrayList;

public class SMKenBurnsView extends SMView {
    public SMKenBurnsView(IDirector director) {
        super(director);
    }

    private static final float PAN_TIME = 10.0f;
    private static final float FADE_TIME = 1.5f;
    private static final float MINUM_RECT_FACTOR = 0.8f;
    private static final Color4F DIM_LAYER_COLOR = new Color4F(0, 0, 0, 0.6f);



    public static SMKenBurnsView createWithAssets(IDirector director, ArrayList<String> assetList) {
        SMKenBurnsView view = new SMKenBurnsView(director);
        view.initWithImageList(Mode.ASSET, assetList);
        return view;
    }

    @Override
    public void setContentSize(final Size size) {
        super.setContentSize(size);
        if (_dimLayer!=null) {
            _dimLayer.setContentSize(size);
        }
    }
    @Override
    public void setContentSize(final float width, final float height) {
        this.setContentSize(new Size(width, height));
    }

    public void startWithDelay(float delay) {
        if (delay<=0) {
            onNextTransition(0);
        } else {
            scheduleOnce(new SEL_SCHEDULE() {
                @Override
                public void scheduleSelector(float t) {
                    onNextTransition(t);
                }
            }, delay);
        }
    }


    public void pauseKenBurns() {

    }

    public void resumeKenBurns() {

    }

    // for later
    public enum Mode {
        ASSET,
        URL
    }

    protected boolean initWithImageList(Mode mode, ArrayList<String> imageList) {
        if (imageList.size()==0) return false;

        _mode = mode;
        _imageList = imageList;

        _dimLayer = SMSolidRectView.create(getDirector());
        _dimLayer.setContentSize(_contentSize);
        _dimLayer.setColor(DIM_LAYER_COLOR);
        addChild(_dimLayer);

        return true;
    }

    private void onNextTransition(float dt) {

    }

//    private Rect generateRandomRect(final Size imageSize) {
//
//    }

    private Mode _mode = Mode.ASSET;
    private int _sequence = 0;
    private int _serial = 0;
    private boolean _runnable = true;

    private ArrayList<String> _imageList = new ArrayList<>();

    SMSolidRectView _dimLayer = null;

    public TransitionAction TransitionActionCreate(IDirector director) {
        TransitionAction action = new TransitionAction(director);
        action.initWithDuration(0);
        return action;
    }
    public class TransitionAction extends DelayBaseAction {
        public TransitionAction(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onUpdate(float dt) {
            updateTextureRect(dt);

            float time = getDuration() * dt;
            float alpha = Math.min(time/FADE_TIME, 1.0f);
            _image.setAlpha(alpha);
            _image.setScale(_target.getContentSize().width/_image.getContentSize().width);
        }

        @Override
        public void onEnd() {
            _target.removeChild(_image);
        }

        public void updateTextureRect(float t) {
            float x = SMView.interpolation(_src.origin.x, _dst.origin.x, t);
            float y = SMView.interpolation(_src.origin.y, _dst.origin.y, t);
            float w = SMView.interpolation(_src.size.width, _dst.size.width, t);
            float h = SMView.interpolation(_src.size.height, _dst.size.height, t);

            _image.setContentSize(w, h);
            _image.setPosition(x, y);
        }

        public void setValue(SMImageView image, final Rect src, final Rect dst, float duration, float delay) {
            setTimeValue(duration, delay);

            _image = image;
            _src = src;
            _dst = dst;

            _image.setAlpha(0.0f);
            updateTextureRect(0.0f);
        }

        protected SMImageView _image;
        protected Rect _src = new Rect(), _dst = new Rect();
    }
}
