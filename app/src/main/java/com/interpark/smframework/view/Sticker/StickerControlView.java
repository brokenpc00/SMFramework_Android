package com.interpark.smframework.view.Sticker;

import android.util.Log;
import android.view.MotionEvent;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.shape.ShapeConstant;
import com.interpark.smframework.base.sprite.GridSprite;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.Vec3;
import com.interpark.smframework.util.Vec4;
import com.interpark.smframework.view.RingWave;
import com.interpark.smframework.view.RingWave2;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMRoundRectView;
import com.interpark.smframework.view.SMSolidCircleView;

public class StickerControlView extends SMView implements SMView.OnClickListener, SMView.OnTouchListener {

    private static final int SIZE_BTN_TAG = 100;
    private static final float BORDER_MARGIN  = 30.0f;
    private static final int UTILBUTTON_ID_DELETE = 2000;

    private static final Color4F MENU_BUTTON_A = MakeColor4F(0xFFFFFF, 0.7f);
    private static final Color4F MENU_BUTTON_B = MakeColor4F(0x222222, 0.7f);
    private static final Color4F MENU_OUTLINE_A = MakeColor4F(0x222222, 0.7f);
    private static final Color4F MENU_OUTLINE_B = MakeColor4F(0xFFFFFF, 0.7f);

    private static final Color4F WAVE_COLOR = MakeColor4F(0xFFFFFF, 0.5f);

    private static final int UTIL_BUTTON_MODE_NONE = -1;
    private static final int UTIL_BUTTON_MODE_REMOVE = 1;


    public static StickerControlView create(IDirector director) {
        StickerControlView view = new StickerControlView(director);
        view.init();
        return view;
    }

    public StickerControlView(IDirector director) {
        super(director);
        _reset = false;
        _listener = null;
        _sizeButtonIndicator = null;
        _highlightSizeButton = false;
        _uiView = null;
        _borderRect = null;
        _sizeButton = null;
        _utilButton = null;
        _targetView = null;
    }

    @Override
    public boolean init() {
        if (!super.init()) {
            return false;
        }

        _uiView = SMView.create(getDirector(), 0,0, 0, 10, 10);
        _uiView.setAnchorPoint(Vec2.MIDDLE);
        _uiView.setIgnoreTouchBounds(true);
//        _uiView.setBackgroundColor(new Color4F(0, 1, 0, 0.4f));
        addChild(_uiView);

        // dashed out line
        _borderRect = SMRoundRectView.create(getDirector(), 4.0f, ShapeConstant.LineType.Dash, 2.0f);
        _borderRect.setAnchorPoint(Vec2.MIDDLE);
        _borderRect.setCornerRadius(20.0f);
        _borderRect.setLineColor(MakeColor4F(0xe6e6e9, 1.0f));
        _uiView.addChild(_borderRect);

        // size button
        _sizeButton = SMButton.create(getDirector(), SIZE_BTN_TAG, SMButton.STYLE.SOLID_CIRCLE, 0, 0, 140, 140, 0.5f, 0.5f);
        _sizeButton.setPadding(30.0f);
        _sizeButton.setButtonColor(STATE.NORMAL, Color4F.WHITE);
        _sizeButton.setButtonColor(STATE.PRESSED, new Color4F(0.9f, 0.9f, 0.9f, 1.0f));
        _sizeButton.setOutlineWidth(5.0f);
        _sizeButton.setOutlineColor(STATE.NORMAL, MakeColor4F(0xe6e6e9, 1.0f));
        _sizeButton.setIcon(STATE.NORMAL, "images/size_arrow.png");
        _sizeButton.setIconColor(STATE.NORMAL, MakeColor4F(0x222222, 1.0f));

        SMSolidCircleView shadow = SMSolidCircleView.create(getDirector());
        _sizeButton.setBackgroundView(shadow);
        shadow.setContentSize(new Size(90, 90));
        shadow.setAnchorPoint(Vec2.MIDDLE);
        shadow.setAntiAliasWidth(20.0f);
        shadow.setPosition(70, 65.0f);
        _sizeButton.setBackgroundColor(new Color4F(0, 0, 0, 0.15f));
        _sizeButton.setOnTouchListener(this);
        _uiView.addChild(_sizeButton);

        // trash button
        _utilButton = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_CIRCLE, 0, 0, 140, 140, 0.5f, 0.5f);
        _utilButton.setPadding(30.0f);
        SMSolidCircleView shadow2 = SMSolidCircleView.create(getDirector());
        _utilButton.setBackgroundView(shadow2);
        shadow2.setContentSize(new Size(90, 90));
        shadow2.setAnchorPoint(Vec2.MIDDLE);
        shadow2.setAntiAliasWidth(20.0f);
        shadow2.setPosition(70, 65);
        _utilButton.setBackgroundColor(new Color4F(0, 0, 0, 0.15f));
        _utilButton.setOnClickListener(this);
        _uiView.addChild(_utilButton);

        _uiView.setVisible(false);

        _utilButtonMode = UTIL_BUTTON_MODE_NONE;

        return true;
    }

    public void setStickerListener(StickerControlListener l) {
        _listener = l;
    }

    @Override
    public void onClick(SMView view) {
        if (_listener!=null) {
            _listener.onStickerMenuClick(_targetView, view.getTag());
        }
    }

    public void startGeineRemove(SMView view) {
        if (view!=null && view==_targetView) {
            Vec2 dst = convertToNodeSpace(_targetView.convertToWorldSpace(Vec2.ZERO));
            Size size = _utilButton.getContentSize();
            Vec2 src = convertToNodeSpace(_utilButton.convertToWorldSpace(new Vec2(size.width/2, size.height/2)));
            WasteBasketActionView.showForUtil(getDirector(), this, src, dst);

//            if (view instanceof Sticker) {
//                Sticker sticker = (Sticker)view;
//                if (sticker.getSprite() instanceof GridSprite) {
//                    GridSprite gridSprite = (GridSprite)sticker.getSprite();
//                }
//            }
        }
    }

    @Override
    public int dispatchTouchEvent(MotionEvent event, SMView view, boolean checkBounds) {
        int ret = super.dispatchTouchEvent(event, view, checkBounds);
        if (checkBounds && event.getAction()==MotionEvent.ACTION_DOWN && view==_uiView) {
            if (_sizeButtonIndicator!=null) {
                TransformAction action = TransformAction.create(getDirector());
                action.toAlpha(0).removeOnFinish();
                action.setTimeValue(0.5f, 0.0f);
                _sizeButtonIndicator.runAction(action);
                _sizeButtonIndicator = null;
            }
        }
        return ret;
    }


    @Override
    public int onTouch(SMView view, MotionEvent event) {
        int action = event.getAction();
        Vec2 point = new Vec2(event.getX(), event.getY());

        if (action==MotionEvent.ACTION_DOWN) {
            Size size = view.getContentSize();
            RingWave.show(getDirector(), view, size.width/2, size.height/2, 200, 0.25f, 0.0f, WAVE_COLOR);
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            {
                _grabPt.set(point);
                return TOUCH_FALSE;
            }
            case MotionEvent.ACTION_MOVE:
            {


//                view.setPosition(buttonPt);

//                view.setPosition(view.getX()+point.x-_grabPt.x, view.getY()+point.y-_grabPt.y);
//                float sx = _uiView.getScreenX()
//                float sy = _targetView.getScreenY();
/*

						setPosition(getX()+x-mGrabX, getY()+y-mGrabY);
						float zoom = mEditView.getContentsZoomScale();

						float sx = mEditView.toScreenX(mSelectedSticker.getX());
						float sy = mEditView.toScreenY(mSelectedSticker.getY());

						float dx = getX()-sx;
						float dy = getY()-sy;
						float radius = (float)Math.sqrt(dx*dx+dy*dy) / zoom;
						double slope = Math.atan2(dy, dx);

						float hw = 0.5f*mSelectedSticker.getWidth();
						float hh = 0.5f*mSelectedSticker.getHeight();
						float baseRadius = (float)Math.sqrt(hw*hw + hh*hh);
						double baseSlope = Math.atan2(-hh, hw);

						float newScale = radius / baseRadius;
						mSticker.setScale(newScale);
						mSticker.setRotateZ((float)Math.toDegrees(slope-baseSlope));




* */

//                Vec2 targetPosition = _targetView.convertToWorldPos(Vec2.ZERO);
//                Vec2 position = _uiView.convertToLocalPos(targetPosition);

                Vec2 pt =  new Vec2(view.getPosition().minus(new Vec2(BORDER_MARGIN, BORDER_MARGIN)).add(point).minus(_grabPt));
                

//                float dist = (float) Math.sqrt(pt.x*pt.x + pt.y*pt.y);
//
//
//                pt.multiplyLocal(0.5f);
//
//
//                Vec2 ppt = _uiView.convertCurPosToWorld(pt).minus(_uiView.convertCurPosToWorld(Vec2.ZERO));
//                float rot = (float) Math.atan2(ppt.y, ppt.x);
//
//                Size tsize = _targetView.getContentSize();
//                float ww = tsize.width/2;
//                float hh = tsize.height/2;
//                float baseDist = (float) Math.sqrt(ww*ww + hh*hh);
//                float baseRot = (float) Math.atan2(hh, ww);
//                float degrees = (float) Math.toDegrees(rot-baseRot);
//                Log.i("CONTROL", "[[[[[ rot : " + rot + ", baseRot : " + baseRot + ", radian : " + (rot-baseRot) + ", degrees : " + degrees);
//
//                float canvasScale = 1.0f;
//                float controlScale = 1.0f;
//                for (SMView p=_targetView.getParent(); p!=null; p=p.getParent()) {
//                    canvasScale *= p.getScale();
//                }
//                for (SMView p=getParent(); p!=null; p=p.getParent()) {
//                    controlScale *= p.getScale();
//                }
//                baseDist *= canvasScale / controlScale;
//
//
//                // 실제 거리 및 scale 계산
//                float scale = dist / baseDist;
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = Math.max((1+BORDER_MARGIN) / tsize.width, (1+BORDER_MARGIN) / tsize.height);
//                }
//                _targetView.setScale(scale);
//                _targetView.setRotation(degrees);


//                pt.multiplyLocal(0.5f);

//                Vec2 targetPosition = _targetView.convertToWorldPos(Vec2.ZERO);
//                Vec2 position = _uiView.convertToLocalPos(targetPosition);

                // size button center pos -> worlpos
//                Vec2 sizeButtonScreenPos = view.convertToWorldPos(new Vec2(BORDER_MARGIN, BORDER_MARGIN).add(point).minus(_grabPt));
//                Vec2 sizeButtonLocalPos = view.convertToLocalPos(sizeButtonScreenPos);
//                view.setPosition(sizeButtonLocalPos);


//
//                Vec2 uiViewCenterScreenPos = _uiView.convertToWorldPos(Vec2.ZERO);
//
//                Vec2 pt = sizeButtonScreenPos.minus(uiViewCenterScreenPos);
////                Vec2 pt2 =  new Vec2(view.getPosition().minus(new Vec2(BORDER_MARGIN, BORDER_MARGIN)).add(point).minus(_grabPt)).multiply(0.5f);
//
//                float dist = (float) Math.sqrt(pt.x*pt.x+pt.y*pt.y);
//
//                float canvasScale = _targetView.getScreenScale();
//                float controlScale = getScreenScale();
//
//                Size tsize = _targetView.getContentSize();
//                float ww = tsize.width/2;
//                float hh = tsize.height/2;
//                float baseDist = (float) Math.sqrt(ww*ww + hh*hh);
//                float baseRot = (float) Math.atan2(hh, ww);
//
//                baseDist *= canvasScale / controlScale;
//
//                // 실제 거리 및 scale 계산
//                float scale = dist / baseDist;
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = Math.max((1+BORDER_MARGIN) / tsize.width, (1+BORDER_MARGIN) / tsize.height);
//                }
//                _targetView.setScale(scale);



//                _targetView.setRotation(-(float)Math.toDegrees(rot-baseRot));


//                float dist2 = (float) Math.sqrt(pt2.x*pt2.x+pt2.y*pt2.y);


//                Log.i("CONTROL", "[[[[[ dist : " + dist + ", dist2 : " + dist2);

//                Vec2 pt2 =  new Vec2(view.getPosition().minus(new Vec2(BORDER_MARGIN, BORDER_MARGIN)).add(point).minus(_grabPt)).multiply(0.5f);
//
//
//
//
//
//                float dist = (float) Math.sqrt(pt.x*pt.x+pt.y*pt.y);
//
//
//
//                // size button 의 대칭점(스티커 중심으로)을 구한다.
//                Vec2 ppt = _uiView.convertToWorldSpace(pt).minus(_uiView.convertToWorldSpace(Vec2.ZERO));
//                float rot = (float) Math.atan2(ppt.y, ppt.x);
//
//                Size tsize = _targetView.getContentSize();
//                float ww = tsize.width/2;
//                float hh = tsize.height/2;
//                float baseDist = (float) Math.sqrt(ww*ww + hh*hh);
//                float baseRot = (float) Math.atan2(hh, ww);
//
//                // StickerCanvas에서의 실제 사이즈를 구하기 위해 scale을 계산
//                float canvasScale = 1.0f;
//                for (SMView p = _targetView.getParent();  p != null; p = p.getParent()) {
//                    canvasScale *= p.getScale();
//                }
//
//                // StickerControl(view가 붙어 있는 넘 - parent)의 world scale을 계산
//                float controlScale = 1.0f;
//                for (SMView p = getParent();  p != null; p = p.getParent()) {
//                    controlScale *= p.getScale();
//                }
//
//                baseDist *= canvasScale / controlScale;
//
//                // 실제 거리 및 scale 계산
//                float scale = dist / baseDist;
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = Math.max((1+BORDER_MARGIN) / tsize.width, (1+BORDER_MARGIN) / tsize.height);
//                }
//                _targetView.setScale(scale);
//                _targetView.setRotation(-(float)Math.toDegrees(rot-baseRot));


                // distance touch point to sticker select view's center point

//                Vec2 pt = new Vec2(view.getPosition().add(point).minus(_grabPt));
//
////                view.setPosition(pt);
//
//                pt.divideLocal(2);
//
//                float dist = (float) Math.sqrt(pt.x*pt.x + pt.y*pt.y);
//
//
//                Vec2 ppt = new Vec2(view.getScreenX(pt.x), view.getScreenY(pt.y)).minus(new Vec2(view.getScreenX()-_uiView.getContentSize().width*_uiView.getScreenScale(), view.getScreenY()));
////                Vec2 ppt = new Vec2(view.getScreenX()-_uiView.getContentSize().width, view.getScreenY()).minus(new Vec2(view.getScreenX(pt.x), view.getScreenY(pt.y)));
//
//                float rot = (float) Math.atan2(ppt.y, ppt.x);
//
////                Vec2 ppp = new Vec2(_targetView.getScreenX(_targetView.getContentSize().width), _targetView.getScreenY(0)).minus(new Vec2(_targetView.getScreenX(), _targetView.getScreenY()));
//                Size tsize = _targetView.getContentSize();
//                float ww = tsize.width/2;
//                float hh = tsize.height/2;
//                float baseDist = (float) Math.sqrt(ww*ww + hh*hh);
//                float baseRot = (float) Math.atan2(hh, ww);
////                float baseDist = (float) Math.sqrt(ppp.x*ppp.x+ppp.y*ppp.y);
//
//
//                float canvasScale  = 1.0f;
//                for (SMView p=_targetView.getParent(); p!=null; p=p.getParent()) {
//                    canvasScale *= p.getScale();
//                }
//
//                float controlScale = 1.0f;
//                for (SMView p=getParent(); p!=null; p=p.getParent()) {
//                    controlScale *= p.getScale();
//                }
//
//                baseDist *= canvasScale / controlScale;
//
//                float scale = dist / baseDist;
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = Math.max((1+BORDER_MARGIN) / tsize.width, (1+BORDER_MARGIN) / tsize.height);
//                }
//
//                _targetView.setScale(scale);
//                _targetView.setRotation((float)Math.toDegrees(rot-baseRot));


//                Vec2 centerPt = new Vec2(_uiView.getX(), _uiView.getY());
//
//                Vec2 ppt = centerPt.minus(pt);
//                float rot = (float) Math.atan2(-ppt.y, ppt.x);
//                _targetView.setRotation((float)Math.toDegrees(rot));

//                view.setPosition(pt);

//                Vec2 moveScreenPt = new Vec2(view.getScreenX(pt.x), view.getScreenY(pt.y));
////                view.setPosition(moveScreenPt.minus(new Vec2(_uiView.getScreenX(), _uiView.getScreenY())));
//                Vec2 centerScreenPt = new Vec2(_uiView.getScreenX(_uiView.getX()), _uiView.getScreenY(_uiView.getY()));
//
//                Vec2 ppt = moveScreenPt.minus(centerScreenPt);
//                float dist = (float) Math.sqrt(ppt.x*ppt.x+ ppt.y*ppt.y);
//                float slope = (float) Math.atan2(ppt.y, ppt.x);
//
//                _targetView.setRotation((float)Math.toDegrees(slope));

//                Log.i("CONTROL", "[[[[[ centerScreenPt("+centerScreenPt.x+", "+centerScreenPt.y+") == ("+_uiView.getScreenX()+_uiView.getContentSize().width/2*_uiView.getScreenScale()+", "+_uiView.getScreenY()+_uiView.getContentSize().height/2*_uiView.getScreenScale()+")");
//                pt.minuLocal(new Vec2(BORDER_MARGIN, BORDER_MARGIN));
//                pt.divideLocal(2);

//                float dist = (float) Math.sqrt(pt.x*pt.x+pt.y*pt.y);
//                Vec2 ppt = view.convertToWorldSpace(pt).minus(view.convertToWorldSpace(Vec2.ZERO));
//                float rot = (float) Math.atan2(ppt.y, ppt.x);

//                Log.i("CONTROL", "[[[[[ centerScreenPt("+centerScreenPt.x+", "+centerScreenPt.y+") Screen ("+moveScreenPt.x+", "+moveScreenPt.y+") == ("+view.getScreenX(view.getX())+", "+view.getScreenY(view.getY())+")");


//                Vec2 moveScreenPt = new Vec2(_uiView.getScreenX(pt.x), _uiView.getScreenY(pt.y));
//
//                Vec2 centerScreenPt = new Vec2(_uiView.getScreenX(), _uiView.getScreenY(_uiView.getContentSize().height));
////                Vec2 centerScreenPt = new Vec2(view.getScreenX(), view.getScreenY());
//
//                Vec2 ppt = moveScreenPt.minus(centerScreenPt);
//                float rot = (float) Math.atan2(ppt.y, ppt.x);
//                _targetView.setRotation((float)Math.toDegrees(rot));
////
////
//                Size tsize = _targetView.getContentSize();
//                float hw = tsize.width/2;
//                float hh = tsize.height/2;
//                float baseDist = (float) Math.sqrt(hw*hw + hh*hh);
//                float baseRot = (float) Math.atan2(-hh, hw);
//
//                float canvasScale  = 1.0f;
//                for (SMView p=_targetView.getParent(); p!=null; p=p.getParent()) {
//                    canvasScale *= p.getScale();
//                }
//
//                float controlScale = 1.0f;
//                for (SMView p=getParent(); p!=null; p=p.getParent()) {
//                    controlScale *= p.getScale();
//                }
//
//                baseDist *= canvasScale / controlScale;
//
//                float scale = dist / baseDist;
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = Math.max((1+BORDER_MARGIN) / tsize.width, (1+BORDER_MARGIN) / tsize.height);
//                }

//                _targetView.setScale(scale);
////                _targetView.setRotation((float)Math.toDegrees(rot));
//                float rotate = (float)Math.toDegrees(rot-baseRot);
//                _targetView.setRotation((float)Math.toDegrees(rot));


//                auto targetPosition = _targetNode->getParent()->convertToWorldSpace(_targetNode->getPosition());
//                Vec2 targetPosition = _targetView.convertToWorldSpace(_targetView.getPosition());
//                auto ppt = _uiView->convertToWorldSpace(pt) - _uiView->convertToWorldSpace(cocos2d::Vec2::ZERO);
//





//                Vec2 ppt = view.convertToWorldSpace(pt).minus(view.convertToWorldSpace(Vec2.ZERO));
//
//                Vec2 movePt = view.getPosition().add(point).minus(_grabPt);
////                view.setPosition(movePt);
////
//                // exactly
//                Vec2 moveScreenPt = new Vec2(view.getScreenX(movePt.x), view.getScreenY(movePt.y));
//
//                Vec2 centerScreenPt = new Vec2(_uiView.getScreenX() + _uiView.getContentSize().width/2*_uiView.getScreenScale(), _uiView.getScreenY() + _uiView.getContentSize().height/2*_uiView.getScreenScale());
//                float sx = centerScreenPt.x;
//                float sy = centerScreenPt.y;
////
//                float dx = moveScreenPt.x-sx;
//                float dy = moveScreenPt.y-sy;
//
//
//                float radian = (float) Math.atan2(dy, dx);
//
//                _targetView.setRotation((float)Math.toDegrees(radian-baseRot));
//
//                Math.ArcTan2(_pt2.x - _pt1.x,_pt2.y - _pt1.y);
//

//                float dist = (float)Math.sqrt(dx*dx+dy*dy);
//
//                Log.i("CONTROL", "[[[[[ dist : " + dist);
//
//                Size tsize = _targetView.getContentSize();
//                float hw = tsize.width/2;
//                float hh = tsize.height/2;
//                float baseDist = (float) Math.sqrt(hw*hw+hh*hh);
//                float baseRot = (float) Math.atan2(-hh, hw);
//
//                float canvasScale = 1.0f;
//                for (SMView p = _targetView.getParent();  p != null; p = p.getParent()) {
//                    canvasScale *= p.getScale();
//                }
//
//                // StickerControl(현재 부모)의 world scale
//                float controlScale = 1.0f;
//                for (SMView p = getParent();  p != null; p = p.getParent()) {
//                    controlScale *= p.getScale();
//                }
//
//                float baseDist2 = baseDist * _targetView.getParent().getScreenScale() / getParent().getScreenScale();
//
//                baseDist *= canvasScale / controlScale;
//
//
//
//                Log.i("CONTROL", "[[[[[ baseDist : " + baseDist + " == " + baseDist2);
//
//                float scale = dist/baseDist;


//
//                Vec2 targetPt = new Vec2(_targetView.getScreenX() + _targetView.getContentSize().width/2*_targetView.getScreenScale(), _targetView.getScreenY() - _targetView.getContentSize().height/2*_targetView.getScreenScale());
//
//                Vec2 targetCenterPt = new Vec2(_targetView.getScreenX(), _targetView.getScreenY());
//
////                float hw = targetPt.x - targetCenterPt.x;
////                float hh = targetPt.y - targetCenterPt.y;
////                float hw = _targetView.getScreenX(0) - _targetView.getScreenX(_targetView.getContentSize().width/2);
////                float hh = _targetView.getScreenY(0) - _targetView.getScreenY(f_targetView.getContentSize().height/2);
////                float baseDist = (float) Math.sqrt(hw*hw + hh*hh);
//
//
//
//                float hw2 = _targetView.getContentSize().width/2*_targetView.getScreenScale();
//                float hh2 = _targetView.getContentSize().height/2*_targetView.getScreenScale();
//
//                float baseDist = (float) Math.sqrt(hw2*hw2 + hh2*hh2);
////                baseDist *= _targetView.getParent().getScreenScale() / getParent().getScreenScale();
//
//                float scale = dist / baseDist;
//
////                Log.i("CONTROL", "[[[[[ x : " + _targetView.getScreenX(_targetView.getContentSize().width) + " == " + _targetView.getScreenX() + _targetView.getContentSize().width*_targetView.getScale());
//
//                Log.i("CONTROL", "[[[[[ dist : " + dist + ", baseDist : " + baseDist + ", scale : " + scale);
//                Log.i("CONTROL", "[[[[[ dist : " + dist + ",\t basePt : (" + sx + ", " + sy + "),\t screenPt : (" + _uiView.getScreenX() + ", " + _uiView.getScreenY() + "),\t size : (" + _uiView.getContentSize().width + ", " + _uiView.getContentSize().height + ")");


//
//
//                float hw = _targetView.getScreenX(0) - _targetView.getScreenX(_targetView.getContentSize().width/2);
//                float hh = _targetView.getScreenY(0) - _targetView.getScreenY(_targetView.getContentSize().height/2);
//                float baseDist = (float) Math.sqrt(hw*hw + hh*hh);
//
//                float scale = dist / baseDist;
//
//                _targetView.setScale(scale);




//                float sx = getScreenX(_targetView.getX());
//                float sy = getScreenY(_targetView.getY());
//
//                float dx = sizeBtnPt.x-sx;
//                float dy = sizeBtnPt.y-sy;
//                float radius = (float) Math.sqrt(dx*dx+dy*dy);
//                double slope = Math.atan2(dy, dx);
//
//                float hw = _targetView.getContentSize().width/2;
//                float hh = _targetView.getContentSize().height/2;
//
//                float baseRadius = (float) Math.sqrt(hw*hw+hh*hh);
//                double baseSlope = Math.atan2(-hh, hw);
//
//                float newScale = radius / baseRadius;
//                _targetView.setScale(newScale);

//                Log.i("CONTROL", "[[[[[ new Scale " + newScale);

                // size button point
//                Vec2 movePt = new Vec2(point.minus(_grabPt));

                // 현재 touch 좌표 (resize button이 있어야 할 것 같은 위치)
//                Vec2 currentSizeButtonPt = new Vec2(view.getPosition().add(point).minus(_grabPt));
//                // for test
////                view.setPosition(currentSizeButtonPt);
//
//                // center;
//                Vec2 pt = currentSizeButtonPt.divide(2);
//                Vec2 center = new Vec2(view.getParent().getContentSize().divide(2).toVec());
//                Vec2 pt2 = new Vec2(currentSizeButtonPt.x-center.x, currentSizeButtonPt.y-center.y);
//                float dist = (float) Math.sqrt(pt.x*pt.x + pt.y*pt.y);
//                float dist2 = (float) Math.sqrt(pt2.x*pt2.x+pt2.y*pt2.y);
//                float rot = (float) Math.atan2(-pt.y, pt.x);
//                float rot2 = (float) Math.atan2(-pt2.y, pt2.x);
//
//                Log.i("CONTROL", "[[[[[ dist : " + dist + ", dist2 : " + dist2 + ", rot : " + rot + ", rot2 : " + rot2);
//
//
////                Vec2 ppt = new Vec2(_uiView.convertToWorldSpace(pt).minus(_uiView.convertToWorldSpace(Vec2.ZERO)));
////                Vec2 ppt = _uiView.getParent().convertToWorldSpace(pt).minus(_uiView.getParent().convertToWorldSpace(Vec2.ZERO));
//
//
////                float rot = (float) Math.atan2(ppt.y, ppt.x);
//
//                Size tsize = _targetView.getContentSize();
//                float hw = tsize.width/2;
//                float hh = tsize.height/2;
//                float baseDist = (float) Math.sqrt(hw*hw+hh*hh);
//                float baseRot = (float) Math.atan2(-hh, hw);
//
//                baseDist *= _targetView.getParent().getScreenScale() / getParent().getScreenScale();
//
//
//                float scale = dist/baseDist;
//                float newRotate = (float) Math.toDegrees(rot2-baseRot);
//
//
////                Log.i("CONTROL", "[[[[[ dist : " + dist + ", baseDist : " + baseDist + ", screen Scale : " + _targetView.getScreenScale() / getScreenScale() + "... so... scale : " + scale);
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = Math.max((1+BORDER_MARGIN) / tsize.width, (1+BORDER_MARGIN) / tsize.height);
//                }
//
////                Log.i("CONTROL", "[[[[[ rotate : " + newRotate + ", rot : " + rot + ", baseRot : " + baseRot);
//                _targetView.setScale(scale);
//                _targetView.setRotation(newRotate);


//                // center로 부터 point
////                Vec2 pt = currentSizeButtonPt.minus(new Vec2(BORDER_MARGIN, -BORDER_MARGIN));
//                Vec2 pt = new Vec2(currentSizeButtonPt);
//
//                // for test
////                view.setPosition(currentSizeButtonPt);
//
//                // world 좌표
//                Vec2 ppt = new Vec2(convertToWorldSpace(pt).minus(convertToWorldSpace(Vec2.ZERO)));
//
////                Log.i("CONTROL", "[[[[[ pt("+pt.x+", "+pt.y+") ==> pprt("+ppt.x+", "+ppt.y+")");
//
//                // 중심으로 부터의 거리 sqrt((x2 - x1)^2 + (y2-y1)^2)... x1, y1이 0
//                // sqrt(x좌표^2 + y좌표^2)
//                float dist = (float) Math.sqrt(pt.x*pt.x + pt.y*pt.y);
//                // 각도
//                float rot = (float) Math.atan2(ppt.y, ppt.x);
//
//                Size tsize = new Size(_targetView.getContentSize());
//                Vec2 tarbetCenterPt = new Vec2(tsize.width/2, tsize.height/2);
//
//                // left-top으로 부터의 거리 (0, 0)
//                float baseDist = (float) Math.sqrt(tarbetCenterPt.x*tarbetCenterPt.x + tarbetCenterPt.y*tarbetCenterPt.y);
//                // 각도
//                float baseRot = (float) Math.atan2(tarbetCenterPt.y, tarbetCenterPt.x);
//
//                // 실제거리로 scale
//
////                baseDist *= (_targetView.getScreenScale()/getScreenScale());
//
//                float scale = dist / baseDist;
//
//                _targetView.setScale(scale);

                // move & scale target...
//                Vec2 pt = new Vec2();
//                pt.set(view.getPosition());
//                pt.minuLocal(new Vec2(BORDER_MARGIN, BORDER_MARGIN));
//                pt.addLocal(point);
//                pt.minuLocal(_grabPt);
//                pt.multiplyLocal(0.5f);
//
//                // radius
//                float dist = (float)Math.sqrt(pt.x*pt.x+pt.y+pt.y);
//
//                Vec2 ppt = new Vec2();
//                ppt.set(_uiView.convertToWorldSpace(pt));
//                ppt.minuLocal(_uiView.convertToWorldSpace(Vec2.ZERO));
//
//                // slope
//                float rot =(float) Math.atan2(ppt.y, ppt.x);
//
//                Size tsize = new Size(_targetView.getContentSize());
//                float hw = tsize.width/2;
//                float hh = tsize.height/2;
//                // baseRadius
//                float baseDist = (float)Math.sqrt(hw*hw + hh*hh);
//                // baseSlope -> (float) Math.atan2(-hh, hw);
//                float baseRot = (float) Math.atan2(-hh, hw);
//
//                float canvasScale = _targetView.getScreenScale();
//                float controlScale = getScreenScale();
//
//                // newScale = radius / baseRadius
//                float scale = dist / baseDist;
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = Math.max((1+BORDER_MARGIN) / tsize.width, (1+BORDER_MARGIN) / tsize.height);
//                }
//
//                _targetView.setScale(scale);
//                float degree = (float) (( (rot-baseRot) * 180.0f ) / M_PI);
//                // setRotateZ((float)Math.toDegrees(slope-baseSlope));
//                _targetView.setRotation(degree);

                return TOUCH_TRUE;
            }
        }
        return TOUCH_FALSE;
    }

    public void linkStickerView(SMView view) {
        if (_targetView!=view) {
            _targetView = view;
            if (view!=null) {
                _uiView.setVisible(true);

                Sticker.ControlType type = Sticker.ControlType.NONE;

                if (view instanceof Sticker) {
                    Sticker sticker = (Sticker)view;
                    type = sticker.getControlType();

                    if (type==Sticker.ControlType.DELETE) {
                        if (sticker.isRemovable()) {
                            _utilButton.setVisible(true);
                            if (_utilButtonMode!=UTIL_BUTTON_MODE_REMOVE) {
                                _utilButtonMode = UTIL_BUTTON_MODE_REMOVE;
                                _utilButton.setButtonColor(STATE.NORMAL, MakeColor4F(0xff683a, 1.0f));
                                _utilButton.setButtonColor(STATE.PRESSED, MakeColor4F(0xff683a, 1.0f));
                                _utilButton.setIcon(STATE.NORMAL, "images/delete_full.png");
                                _utilButton.setIconColor(STATE.NORMAL, Color4F.WHITE);
                                _utilButton.setIconColor(STATE.PRESSED, Color4F.WHITE);
                                _utilButton.setTag(UTILBUTTON_ID_DELETE);
                            }
                        } else {
                            _utilButton.setVisible(false);
                        }
                    }

                    _reset = true;

                    registerUpdate(USER_VIEW_FLAG(1));

                    if (_highlightSizeButton) {
                        _highlightSizeButton = false;

                        RingWave2 ringWave = RingWave2.create(getDirector(), 60, 102);
                        ringWave.setColor(MakeColor4F(0xff9a96, 1.0f));
                        _sizeButton.addChild(ringWave);
                        _sizeButtonIndicator = ringWave;
                    }
                }
            } else {
                _uiView.setVisible(false);
                _utilButton.setVisible(false);

                if (_sizeButtonIndicator!=null) {
                    _sizeButton.removeChild(_sizeButtonIndicator);
                    _sizeButtonIndicator = null;
                }
            }
        }
    }

    protected void logPoint(String title, Vec2 pt) {
        Log.i("CONTROL", "[[[[[ "+title+" x : " + pt.x + ", y : " + pt.y);
    }

    @Override
    public void onUpdateOnVisit() {
        if (_targetView==null) {
            return;
        }

        float localScale = getScreenScale();
        float localRotation = getScreenAngle();

        Vec2 targetPosition = _targetView.convertToWorldPos(Vec2.ZERO);
        Vec2 position = _uiView.convertToLocalPos(targetPosition);

        float targetScale = _targetView.getScreenScale();
        float targetRotation = _targetView.getScreenAngle();

//        float targetScale2 = _targetView.getScale();
//        float targetRotation2 = _targetView.getRotation();
//        // _targetNode parent의 scale과 rotation 모두 적용
//        for (SMView p=_targetView.getParent(); p!=null; p=p.getParent()) {
//            targetScale2 *= p.getScale();
//            targetRotation2 += p.getRotation();
//        }
//
//        Log.i("CONTROL", "[[[[[ scale " + targetScale + " == " + targetScale2 + ", rotation : " + targetRotation + " == " + targetRotation2);


        float scale = targetScale / localScale;
        float rotation = targetRotation - localRotation;

        Size size = _targetView.getContentSize().multiply(scale);


        if (_reset || size.width != _targetSize.width || size.height != _targetSize.height) {
            _reset = false;
            _targetSize.set(size);

            Size viewSize = size.add(new Size(BORDER_MARGIN, BORDER_MARGIN));
            _uiView.setContentSize(viewSize);
            _borderRect.setContentSize(viewSize);
            _borderRect.setPosition(viewSize.width/2, viewSize.height/2);
            _sizeButton.setPosition(viewSize.width, viewSize.height);
            _utilButton.setPosition(0, 0);
        }

        _uiView.setPosition(position);
        _uiView.setRotation(rotation);

    }

    private SMView _uiView = null;
    private SMRoundRectView _borderRect;
    private SMButton _sizeButton;
    private SMButton _utilButton; // for trash

    private SMView _targetView = null;
    private Size _targetSize = new Size();
    private Vec2 _grabPt = new Vec2();
    private Vec2 _deltaPt = new Vec2();
    private boolean _reset = false;

    private StickerControlListener _listener;
    private int _utilButtonMode = 0;
    private SMView _sizeButtonIndicator = null;
    private boolean _highlightSizeButton;

    public interface StickerControlListener {
        public void onStickerMenuClick(SMView sticker, int menuId);
    }

}
