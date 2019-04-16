package com.interpark.smframework.view.Sticker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.CallFuncN;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayTime;
import com.interpark.smframework.base.types.EaseIn;
import com.interpark.smframework.base.types.EaseInOut;
import com.interpark.smframework.base.types.EaseSineInOut;
import com.interpark.smframework.base.types.EaseSineOut;
import com.interpark.smframework.base.types.FadeTo;
import com.interpark.smframework.base.types.FiniteTimeAction;
import com.interpark.smframework.base.types.MoveTo;
import com.interpark.smframework.base.types.PERFORM_SEL_N;
import com.interpark.smframework.base.types.RotateTo;
import com.interpark.smframework.base.types.ScaleSine;
import com.interpark.smframework.base.types.ScaleTo;
import com.interpark.smframework.base.types.Sequence;
import com.interpark.smframework.base.types.Spawn;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.view.SMSolidCircleView;

import java.io.FileInputStream;

public class WasteBasketActionView extends SMView {
    public WasteBasketActionView(IDirector director) {
        super(director);
    }

    private static final float MOVE_DURATION = 0.2f;
    private static final float OPEN_DURATION = 0.1f;
    private static final float EXIT_DURATION = 0.15f;

    private static final float TRASH_SIZE = 80.0f;
    private static final float TRASH_SHADOW_SIZE = 90.0f;
    private static final Vec2 TRASH_TOP_ANCHOR = new Vec2(8/50.0f, (50-13)/50.0f);
    private static final Vec2 TRASH_TOP_POS = new Vec2(TRASH_SIZE/2.0f - (25-8), TRASH_SIZE/2.0f + (25-13));
    private static final Vec2 TRASH_BODY_POS = new Vec2(TRASH_SIZE/2.0f, TRASH_SIZE/2.0f);

    public static WasteBasketActionView show(IDirector director, SMView parent, final Vec2 from, final Vec2 to) {
        WasteBasketActionView view = new WasteBasketActionView(director);
        view.initWithParam(parent, from, to);
        return view;
    }

    public static WasteBasketActionView showForList(IDirector director, SMView parent, final Vec2 from, final Vec2 to) {
        WasteBasketActionView view = new WasteBasketActionView(director);
        view.initWithParam2(parent, from, to);
        return view;
    }

    public static WasteBasketActionView showForUtil(IDirector director, SMView parent, final Vec2 from, final Vec2 to) {
        WasteBasketActionView view = new WasteBasketActionView(director);
        view.initWithParam3(parent, from, to);
        return view;
    }

    @Override
    public void onExit() {
        super.onExit();
        if (getParent()!=null && !_removeSelfOnExit) {
            // remove self
            _removeSelfOnExit = true;
            removeFromParent();
        }
    }

    protected boolean initWithParam(SMView parent, final Vec2 from, final Vec2 to) {
        setContentSize(TRASH_SIZE, TRASH_SHADOW_SIZE);
        setAnchorPoint(Vec2.MIDDLE);

        // shadow
        SMSolidCircleView shadow = SMSolidCircleView.create(getDirector());
        shadow.setContentSize(new Size(TRASH_SHADOW_SIZE, TRASH_SHADOW_SIZE));
        shadow.setAntiAliasWidth(20);
        shadow.setPosition(-5, -10);
        shadow.setColor(new Color4F(0, 0, 0, 0.15f));
        addChild(shadow);

        // bg
        SMSolidCircleView bg = SMSolidCircleView.create(getDirector());
        bg.setContentSize(new Size(TRASH_SIZE, TRASH_SIZE));
        bg.setColor(MakeColor4F(0xff5825, 0.8f));
        addChild(bg);

        SMImageView icon1 = SMImageView.create(getDirector(), "images/delete_top.png");
        SMImageView icon2 = SMImageView.create(getDirector(), "images/delete_body.png");
        icon1.setAnchorPoint(TRASH_TOP_ANCHOR);
        icon1.setPosition(TRASH_TOP_POS);
        icon2.setPosition(TRASH_BODY_POS);
        addChild(icon1);
        addChild(icon2);

        setPosition(from);

        FiniteTimeAction move = EaseSineInOut.create(getDirector(), MoveTo.create(getDirector(), MOVE_DURATION, to));
        FiniteTimeAction scale = ScaleTo.create(getDirector(), MOVE_DURATION, 1.5f);
        FiniteTimeAction step1 = Spawn.create(getDirector(), move, scale, null);

        FiniteTimeAction open = EaseIn.create(getDirector(), RotateTo.create(getDirector(), OPEN_DURATION, -30), 1.0f);
        FiniteTimeAction close = RotateTo.create(getDirector(), 0.05f, 0);
        FiniteTimeAction step2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), MOVE_DURATION+0.1f), open, DelayTime.create(getDirector(), 0.7f), close, null);

        icon1.runAction(step2);

        FiniteTimeAction bounce = EaseInOut.create(getDirector(), ScaleSine.create(getDirector(), 0.4f, 1.5f), 2.0f);
        FiniteTimeAction exit = Spawn.create(getDirector(), EaseIn.create(getDirector(), ScaleTo.create(getDirector(), EXIT_DURATION, 0.7f), 3.0f), FadeTo.create(getDirector(), EXIT_DURATION, 0), null);
        FiniteTimeAction seq = Sequence.create(getDirector(), step1, DelayTime.create(getDirector(), 1.0f), bounce, exit, CallFuncN.create(getDirector(), new PERFORM_SEL_N() {
            @Override
            public void performSelectorN(SMView target) {
                _removeSelfOnExit = true;
                target.removeFromParent();
            }
        }), null);
        runAction(seq);

        return true;
    }


    protected boolean initWithParam2(SMView parent, final Vec2 from, final Vec2 to) {
        setContentSize(80, 80);
        setAnchorPoint(Vec2.MIDDLE);

        // shadow
        SMSolidCircleView shadow = SMSolidCircleView.create(getDirector());
        shadow.setContentSize(new Size(90, 90));
        shadow.setAntiAliasWidth(20);
        shadow.setPosition(-5, -10);
        shadow.setColor(new Color4F(0, 0, 0, 0.15f));
        addChild(shadow);

        // bg
        SMSolidCircleView bg = SMSolidCircleView.create(getDirector());
        bg.setContentSize(new Size(80, 80));
        bg.setColor(MakeColor4F(0xFF5825, 0.8f));
        addChild(bg);

        SMImageView icon1 = SMImageView.create(getDirector(), "images/delete_top.png");  // 뚜껑
        SMImageView icon2 = SMImageView.create(getDirector(), "images/delete_body.png"); // 쓰레기통
        icon1.setAnchorPoint(new Vec2(8/50.0f, (50-13)/50.0f));
        icon1.setPosition(40-(25-8), 40+(25-13));
        icon2.setPosition(40, 40);
        addChild(icon1);
        addChild(icon2);

        setPosition(from);
        setScaleX(-1);

        // 1) 이동
        FiniteTimeAction move = EaseSineInOut.create(getDirector(), MoveTo.create(getDirector(), 0.1f, to));
        FiniteTimeAction scale = ScaleTo.create(getDirector(), 0.1f, -1.2f, 1.2f);
        FiniteTimeAction step1 = Spawn.create(getDirector(), move, scale, null);

        // 2) 뚜껑 (스프라이트)
        FiniteTimeAction open = EaseIn.create(getDirector(), RotateTo.create(getDirector(), 0.05f, -30), 1.0f);
        FiniteTimeAction close = RotateTo.create(getDirector(), 0.05f, 0);
        FiniteTimeAction step2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), 0.1f), open, DelayTime.create(getDirector(), 0.5f), close, null);
        icon1.runAction(step2);

        // 종료
        FiniteTimeAction exit = Spawn.create(getDirector(), EaseIn.create(getDirector(), ScaleTo.create(getDirector(), EXIT_DURATION, -0.7f, 0.7f), 3.0f), FadeTo.create(getDirector(), EXIT_DURATION, 0), null);
        FiniteTimeAction seq = Sequence.create(getDirector(), step1, DelayTime.create(getDirector(), 0.35f), exit, CallFuncN.create(getDirector(), new PERFORM_SEL_N() {
            @Override
            public void performSelectorN(SMView target) {
                _removeSelfOnExit = true;
                target.removeFromParent();
            }
        }),
        null);

        runAction(seq);

        return true;
    }

    protected boolean initWithParam3(SMView parent, final Vec2 from, final Vec2 to) {
        setContentSize(new Size(80, 80));
        setAnchorPoint(Vec2.MIDDLE);

        // shadow
        SMSolidCircleView shadow = SMSolidCircleView.create(getDirector());
        shadow.setContentSize(new Size(90, 90));
        shadow.setAntiAliasWidth(20);
        shadow.setPosition(-5, -10);
        shadow.setColor(new Color4F(0, 0, 0, 0.15f));
        addChild(shadow);

        // bg
        SMSolidCircleView bg = SMSolidCircleView.create(getDirector());
        bg.setContentSize(new Size(80, 80));
        bg.setColor(MakeColor4F(0xFF5825, 0.8f));
        addChild(bg);

        SMImageView icon1 = SMImageView.create(getDirector(), "images/delete_top.png");  // 뚜껑
        SMImageView icon2 = SMImageView.create(getDirector(), "images/delete_body.png"); // 쓰레기통
        icon1.setAnchorPoint(new Vec2(8/50.0f, (50-13)/50.0f));
        icon1.setPosition(40-(25-12), 40+(25));
        icon2.setPosition(40, 40);
        addChild(icon1);
        addChild(icon2);

        setPosition(from);

        FiniteTimeAction scale = ScaleTo.create(getDirector(), 0.1f, 1.2f);
        FiniteTimeAction open = EaseIn.create(getDirector(), RotateTo.create(getDirector(), OPEN_DURATION, -30), 1.0f);
        FiniteTimeAction close = RotateTo.create(getDirector(), 0.05f, 0);
        FiniteTimeAction step2 = Sequence.create(getDirector(),
                open,
                DelayTime.create(getDirector(), 0.4f),
                close,
                null);
        icon1.runAction(step2);

        FiniteTimeAction exit = Spawn.create(getDirector(), EaseIn.create(getDirector(), ScaleTo.create(getDirector(), EXIT_DURATION, 0.7f), 3.0f),
        FadeTo.create(getDirector(), EXIT_DURATION, 0),
                null);


        FiniteTimeAction seq = Sequence.create(getDirector(), scale, //step1,
                DelayTime.create(getDirector(), 0.8f), // delay for genie
                exit,
                CallFuncN.create(getDirector(), new PERFORM_SEL_N() {
                    @Override
                    public void performSelectorN(SMView target) {
                        _removeSelfOnExit = true;
                        target.removeFromParent();
                    }
                }),
        null);

        runAction(seq);

        return true;
    }

    private boolean _removeSelfOnExit = false;
    private Vec2 _from = new Vec2();
    private Vec2 _to = new Vec2();
}
