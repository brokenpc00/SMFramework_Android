package com.interpark.app.menu;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.transition.TransitionEaseScene;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayBaseAction;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMCircleView;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRoundLine;
import com.interpark.smframework.view.SMSolidCircleView;
import com.interpark.smframework.view.ToastBar;

import org.apache.http.cookie.SM;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TopMenu extends SMView {

    public enum DropDown {
        NOTHING,
        UP,
        DOWN
    }

    public enum Id {
        NONE,
        DROPDOWN,

        MENU_MENU,
        MENU_BACK,
        MENU_CLOSE,
        MENU_DOT,
        MENU_CLOSE2,
        MENU_ALARM,

        SEARCH,
        CONFIRM,
        DELETE,
        CAMERA,
        ALBUM,
        CART,
        SETTINGS,

        NEXT,
        DONE,
        CANCEL,
        CROP,
        CLEAR
    }

    public enum TextTransition {
        FADE,
        ELASTIC,
        SWIPE
    }

    public enum ButtonTransition {
        FADE,
        ELASTIC,
        SWIPE
    }

    public interface TopMenuClickListener {
        void onTopMenuClick(TopMenuComponentType type);
    }
    public TopMenuClickListener _listener = null;

    public static DotPosition sDotMenu[] = new DotPosition[4];
    public static DotPosition sDotClose[] = new DotPosition[4];
    public static DotPosition sDotBack[] = new DotPosition[4];
    public static DotPosition sDotDot[] = new DotPosition[4];

    public TopMenu(IDirector director) {
        super(director);

        sDotMenu[0] = new DotPosition(new Vec2(-13, 13), new Vec2(-13, 13), AppConst.SIZE.DOT_DIAMETER);
        sDotMenu[1] = new DotPosition(new Vec2(13, -13), new Vec2(13, -13), AppConst.SIZE.DOT_DIAMETER);
        sDotMenu[2] = new DotPosition(new Vec2(-13, 13), new Vec2(-13, 13), AppConst.SIZE.DOT_DIAMETER);
        sDotMenu[3] = new DotPosition(new Vec2(13, -13), new Vec2(13, -13), AppConst.SIZE.DOT_DIAMETER);

        sDotClose[0] = new DotPosition(new Vec2(-20, 20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotClose[1] = new DotPosition(new Vec2(20, -20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotClose[2] = new DotPosition(new Vec2(-20, -20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotClose[3] = new DotPosition(new Vec2(20, 20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);

        sDotBack[0] = new DotPosition(new Vec2(-16, 16), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotBack[1] = new DotPosition(new Vec2(16, -16), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotBack[2] = new DotPosition(new Vec2(-16, -12), new Vec2(-16, 16), AppConst.SIZE.LINE_DIAMETER);
        sDotBack[3] = new DotPosition(new Vec2(12, 16), new Vec2(-16, 16), AppConst.SIZE.LINE_DIAMETER);

        sDotDot[0] = new DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotDot[1] = new DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotDot[2] = new DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
        sDotDot[3] = new DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER);
    }

    protected SMButton _menuButton;
    protected SMButton[][] _actionButton = new SMButton[2][2];
    protected SMRoundLine _menuLine[] = new SMRoundLine[4];
    protected SMSolidCircleView _menuCircle[] = new SMSolidCircleView[4];
    protected BitmapSprite _menuSprite;
    protected MenuTransform _menuTransform;
    protected TextTransform _textTransform;
    protected ColorTransform _colorTransform;
    protected TextContainer _textContainer;

    private void updateTextPosition(boolean dropdown) {

    }

    private void onToastHiddenComplete(ToastBar toat) {

    }


    public enum TopMenuComponentType {
        MENU,
        TITLE,
        BACK,
        HOME,

    }

    protected class ColorSet implements Cloneable {
        public ColorSet() {

        }

        public ColorSet(final Color4F bg, final Color4F text, final Color4F normal, final Color4F press) {
            BG.set(bg);
            TEXT.set(text);
            NORMAL.set(normal);
            PRESS.set(press);
        }

        public Color4F BG = new Color4F(1, 1, 1, 1);
        public Color4F TEXT = new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff));
        public Color4F NORMAL = new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff));
        public Color4F PRESS = new Color4F(new Color4B(0xad, 0xaf, 0xb3, 0xff));

        public boolean equals(ColorSet set) {
            return BG.equals(set.BG) && TEXT.equals(set.TEXT) && NORMAL.equals(set.NORMAL) && PRESS.equals(set.PRESS);
        }

        public ColorSet set(ColorSet set) {
            if (equals(set)) return this;

            this.BG.set(set.BG);
            this.TEXT.set(set.TEXT);
            this.NORMAL.set(set.NORMAL);
            this.PRESS.set(set.PRESS);

            return this;
        }

        public final ColorSet WHITE = new ColorSet();
        public final ColorSet WHITE_TRANSULANT = new ColorSet(new Color4F(1, 1, 1, 0.7f), new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff)), new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff)), new Color4F(new Color4B(0xad, 0xaf, 0xb3, 0xff)));
        public final ColorSet BLACK = new ColorSet(new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff)), new Color4F(1, 1, 1, 1), new Color4F(1, 1, 1, 1), new Color4F(new Color4B(0xad, 0xaf, 0xb3, 0xff)));
        public final ColorSet NONE = new ColorSet(new Color4F(1, 1, 1, 1), new Color4F(1, 1, 1, 1), new Color4F(1, 1, 1, 1), new Color4F(1, 1, 1, 1));
        public final ColorSet TRANSULANT = new ColorSet(new Color4F(1, 1, 1, 0), new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff)), new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff)), new Color4F(new Color4B(0xad, 0xaf, 0xb3, 0xff)));
    }

    public class DotPosition {
        public Vec2 from = new Vec2();
        public Vec2 to = new Vec2();
        public float diameter = 0.0f;

        public DotPosition(DotPosition pos) {
            this.from.set(pos.from);
            this.to.set(pos.to);
            this.diameter = pos.diameter;
        }

        public DotPosition(final Vec2 from, final Vec2 to, float diameter) {
            this.from.set(from);
            this.to.set(to);
            this.diameter = diameter;
        }
    }



    public static TopMenu create(IDirector director) {
        TopMenu view = new TopMenu(director);
        if (view!=null) {
            view.init();
        }

        return view;
    }

    @Override
    protected boolean init() {
        super.init();
        setPosition(new Vec2(0, 0));
        setContentSize(new Size(getDirector().getWinSize().width, AppConst.SIZE.TOP_MENU_HEIGHT));
        setBackgroundColor(1, 1, 1, 1);

        _contentView = SMView.create(getDirector(), 0, 0, 0, getDirector().getWinSize().width, AppConst.SIZE.TOP_MENU_HEIGHT);
        addChild(_contentView);

        SMView underLine = SMView.create(getDirector(), 0, 20, AppConst.SIZE.TOP_MENU_HEIGHT-2, getDirector().getWinSize().width-40, 2);
        underLine.setBackgroundColor(0, 0, 1, 1);
        underLine.setLocalZOrder(90);
        _contentView.addChild(underLine);

        return true;
    }

    public void addMenuType(TopMenuComponentType type) {
        addMenuType(type, null);
    }

    public void addMenuType(TopMenuComponentType type, SceneParams params) {

        SMView component = null;

        switch (type) {
            case MENU:
            {
                float btnSize = AppConst.SIZE.TOP_MENU_HEIGHT;

                SMButton menuBtn = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_RECT, 0, 0, btnSize, btnSize);
                menuBtn.setButtonColor(STATE.NORMAL, new Color4F(1, 1, 1, 1));
                menuBtn.setButtonColor(STATE.PRESSED, new Color4F(new Color4B(0xee, 0xef, 0xf1, 0xff)));
                if (menuBtn==null) return;
                {
                    SMView menuBg = SMView.create(getDirector(), 0, 0, 0, btnSize, btnSize);
                SMView line1 = SMView.create(getDirector(), 0, 20, btnSize/4, btnSize-40, 2);
                SMView line2 = SMView.create(getDirector(), 0, 20, btnSize/2, btnSize-40, 2);
                SMView line3 = SMView.create(getDirector(), 0, 20, btnSize/4*3, btnSize-40, 2);
                menuBg.addChild(line1);
                menuBg.addChild(line2);
                menuBg.addChild(line3);

                line1.setBackgroundColor(new Color4F(1, 1, 1, 1));
                line2.setBackgroundColor(new Color4F(1, 1, 1, 1));
                line3.setBackgroundColor(new Color4F(1, 1, 1, 1));

                    Bitmap bmp = menuBg.captureView();
                    BitmapSprite sprite = BitmapSprite.createFromBitmap(getDirector(), "MENUICON", bmp);

                    SMImageView imageView = new SMImageView(getDirector());
                    imageView.setContentSize(new Size(btnSize, btnSize));
                    imageView.setAnchorPoint(new Vec2(0.5f, 0.5f));
                    imageView.setSprite(sprite);

                    menuBtn.setIcon(STATE.NORMAL, imageView);
                    menuBtn.setIconColor(STATE.NORMAL, new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff)));
                    menuBtn.setIconColor(STATE.PRESSED, new Color4F(new Color4B(0xad, 0xaf, 0xb3, 0xff)));
                }

                component = menuBtn;

                menuBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(SMView view) {
                        if (_listener!=null) {
                            _listener.onTopMenuClick(TopMenuComponentType.MENU);
                        }
                    }
                });
            }
            break;
            case TITLE:
            {
                String title = "TITLE";
                if (params!=null) {
                    title = params.getString("MENU_TITLE");
                }

                SMLabel titleLabel = SMLabel.create(getDirector(), title, 45, 0x22/255.0f, 0x22/255.0f, 0x22/255.0f, 1.0f);
                titleLabel.setAnchorPoint(new Vec2(0.5f, 0.5f));
                titleLabel.setPosition(new Vec2(getContentSize().width/2, getContentSize().height/2));

                component = titleLabel;
            }
            break;
            case BACK:
            {
                float btnSize = AppConst.SIZE.TOP_MENU_HEIGHT;

                SMButton menuBtn = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_RECT, 0, 0, btnSize, btnSize);
                menuBtn.setButtonColor(STATE.NORMAL, new Color4F(1, 1, 1, 1));
                menuBtn.setButtonColor(STATE.PRESSED, new Color4F(new Color4B(0xee, 0xef, 0xf1, 0xff)));
                if (menuBtn==null) return;
                {
                    SMView menuBg = SMView.create(getDirector(), 0, 0, 0, btnSize, btnSize);
                    SMView line1 = SMView.create(getDirector(), 0, 20, btnSize/4, btnSize-40, 2);
                    SMView line2 = SMView.create(getDirector(), 0, 20, btnSize/2, btnSize-40, 2);
                    SMView line3 = SMView.create(getDirector(), 0, 20, btnSize/4*3, btnSize-40, 2);
                    menuBg.addChild(line1);
                    menuBg.addChild(line2);
                    menuBg.addChild(line3);

                    line1.setBackgroundColor(new Color4F(1, 1, 1, 1));
                    line2.setBackgroundColor(new Color4F(1, 1, 1, 1));
                    line3.setBackgroundColor(new Color4F(1, 1, 1, 1));

                    Bitmap bmp = menuBg.captureView();
                    BitmapSprite sprite = BitmapSprite.createFromBitmap(getDirector(), "MENUICON", bmp);

                    SMImageView imageView = new SMImageView(getDirector());
                    imageView.setContentSize(new Size(btnSize, btnSize));
                    imageView.setAnchorPoint(new Vec2(0.5f, 0.5f));
                    imageView.setSprite(sprite);

                    menuBtn.setIcon(STATE.NORMAL, imageView);
                    menuBtn.setIconColor(STATE.NORMAL, new Color4F(new Color4B(0x22, 0x22, 0x22, 0xff)));
                    menuBtn.setIconColor(STATE.PRESSED, new Color4F(new Color4B(0xad, 0xaf, 0xb3, 0xff)));
                }

                component = menuBtn;

                menuBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(SMView view) {
                        if (_listener!=null) {
                            _listener.onTopMenuClick(TopMenuComponentType.BACK);
                        }
                    }
                });
            }
            break;
            case HOME:
            {

            }
            break;
        }

        if (component!=null) {
            _components.add(component);
        }

    }

    public void generateMenu() {
        for (SMView child : _contentView.getChildren()) {
            child.removeFromParent();
        }
        _contentView.removeAllChildren();

        SMRoundLine line = SMRoundLine.create(getDirector());
        line.setBackgroundColor(new Color4F(new Color4B(0xad, 0xaf, 0xb3, 0xff)));
        line.setLineWidth(2);
        line.line(20, AppConst.SIZE.TOP_MENU_HEIGHT-2, getDirector().getWinSize().width-20, AppConst.SIZE.TOP_MENU_HEIGHT-2);
        line.setLengthScale(1);
        line.setLocalZOrder(90);
        _contentView.addChild(line);

        for (SMView child : _components) {
//            if (child instanceof SMButton) {
//                SMButton btn = (SMButton)child;
//                _contentView.addChild(btn);
//            } else {
//                _contentView.addChild(child);
//            }
            _contentView.addChild(child);

        }
    }

    private SMView _contentView = null;
    private ArrayList<SMView> _components = new ArrayList<>();


    public class TextContainer extends SMView {
        public TextContainer(IDirector director) {
            super(director);
        }

        @Override
        protected boolean init() {

            stub[0] = new SMView(getDirector());

            stub[1] = new SMView(getDirector());

            return true;
        }

        @Override
        public void onStateChangeNormalToPress(MotionEvent event) {
            setAnimOffset(new Vec2(0, -3));
        }

        @Override
        public void onStateChangePressToNormal(MotionEvent event) {
            setAnimOffset(new Vec2(0, 0));
        }

        public SMView stub[] = new SMView[2];
    }

    public class MenuTransform extends DelayBaseAction {
        public MenuTransform(IDirector director) {
            super(director);
        }
    }

    public class ColorTransform extends DelayBaseAction {
        public ColorTransform(IDirector director) {
            super(director);
        }
    }

    public class TextTransform extends DelayBaseAction {
        public TextTransform(IDirector director) {
            super(director);
        }
    }


    public class ButtonAction extends DelayBaseAction {
        public ButtonAction(IDirector director) {
            super(director);
        }
    }

    public class ButtonFadeAction extends DelayBaseAction {
        public ButtonFadeAction(IDirector director) {
            super(director);
        }
    }

    public class DropDownAction extends DelayBaseAction {
        public DropDownAction(IDirector director) {
            super(director);
        }
    }


}
