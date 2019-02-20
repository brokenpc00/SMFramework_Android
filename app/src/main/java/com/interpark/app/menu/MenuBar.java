package com.interpark.app.menu;

import android.graphics.Paint;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayBaseAction;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRoundLine;
import com.interpark.smframework.view.SMSolidCircleView;
import com.interpark.smframework.view.SMToastBar;

public class MenuBar extends SMView {
    public MenuBar(IDirector director) {
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

    public static MenuBar create(IDirector director) {
        MenuBar menubar = new MenuBar(director);
        menubar.init();
        return menubar;
    }

    public final Vec2 MenuButtonCenter = new Vec2(AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2);
    @Override
    public boolean init() {
        super.init();

        Size s = getDirector().getWinSize();

        setContentSize(s.width, AppConst.SIZE.MENUBAR_HEIGHT);
        setAnchorPoint(new Vec2(0, 0));
        setPosition(new Vec2(0, 0));

        _textContainer = TextContainerCreate(getDirector());
        _textContainer.setTag(menuTypeToInt(MenuType.DROPDOWN));
        _textContainer.setContentSize(0, AppConst.SIZE.MENUBAR_HEIGHT);
        _textContainer.setAnchorPoint(Vec2.MIDDLE);
        _textContainer.setPosition(s.width/2, AppConst.SIZE.MENUBAR_HEIGHT/2);
        addChild(_textContainer);

        for (int i=0; i<2; i++) {
            _textLabel[i] = null;
            for (int j=0; j<2; j++) {
                _menuButtons[i][j] = null;
            }
        }

        _mainButton = SMButton.create(getDirector(), menuTypeToInt(MenuType.MENU), SMButton.STYLE.DEFAULT, 5+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2, 5+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE);
        _mainButton.setAnchorPoint(Vec2.MIDDLE);
        _mainButton.setButtonColor(STATE.NORMAL, MakeColor4F(0x222222, 1.0f));
        _mainButton.setButtonColor(STATE.PRESSED, MakeColor4F(0xadafb3, 1.0f));


        _buttonContainer = SMView.create(getDirector());
        _buttonContainer.setContentSize(AppConst.SIZE.TOP_MENU_BUTTONE_SIZE, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE);

        for (int i=0; i<4; i++) {
            _menuLine[i] = SMRoundLine.create(getDirector());
            _menuCircle[i] = SMSolidCircleView.create(getDirector());
            _buttonContainer.addChild(_menuLine[i]);
            _buttonContainer.addChild(_menuCircle[i]);

            _menuCircle[i].setPosition(sDotMenu[i].from.add(MenuButtonCenter));
            _menuCircle[i].setContentSize(AppConst.SIZE.DOT_DIAMETER, AppConst.SIZE.DOT_DIAMETER);
            _menuCircle[i].setAnchorPoint(Vec2.MIDDLE);
        }

        _mainButton.setButton(STATE.NORMAL, _buttonContainer);
        _mainButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(SMView view) {
                onClick(view);
            }
        });
        _mainButton.setPushDownScale(0.9f);
        _mainButton.setPushDownOffset(new Vec2(0, -3));
        addChild(_mainButton);


        return true;
    }

    public void onClick(SMView view) {
        if (_mainButton.getActionByTag(AppConst.TAG.USER+1)!=null) {
            // in transform action...
            return;
        }

        if (_listener!=null) {
            if (_listener.onMenuBarClick(view)) {
                // already process for click
                return;
            }
        }

        switch (intToMenuType(view.getTag())) {
            case MENU:
            {
                // just only type... now
            }
            break;
            case ALARM:
            case SETTINGS:
            case CART:
            case CANCEL:
            default:
            {

            }
            break;
        }
    }

    public interface MenuBarListener {
        public boolean onMenuBarClick(SMView view);
//        public void onMenuBarTouch();
    }

    public static DotPosition[] sDotMenu = new DotPosition[4];
    public static DotPosition[] sDotClose = new DotPosition[4];
    public static DotPosition[] sDotBack = new DotPosition[4];
    public static DotPosition[] sDotDot = new DotPosition[4];


    public enum DropDown {
        NOTHING,
        UP,
        DOWN
    }

    public enum MenuType {
        NONE,
        DROPDOWN,

        MENU,
        BACK,
        CLOSE,
        DOT,
        CLOSE2,
        ALARM,

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

    public static int menuTypeToInt(MenuType type) {
        switch (type) {
            case DROPDOWN: return 0x100;
            case MENU: return 0x1000;
            case BACK: return 0x1001;
            case CLOSE: return 0x1002;
            case DOT: return 0x1003;
            case CLOSE2: return 0x1004;
            case ALARM: return 0x1005;
            case SEARCH: return 0x2000;
            case CONFIRM: return 0x2001;
            case DELETE: return 0x2002;
            case CAMERA: return 0x2003;
            case ALBUM: return 0x2004;
            case CART: return 0x2005;
            case SETTINGS: return 0x2006;
            case NEXT: return 0x3000;
            case DONE: return 0x3001;
            case CANCEL: return 0x3002;
            case CROP: return 0x3003;
            case CLEAR: return 0x3004;
            default: return 0;
        }
    }

    public static MenuType intToMenuType(int value) {
        switch (value) {
            case 0x100: return MenuType.DROPDOWN;
            case 0x1000: return MenuType.MENU;
            case 0x1001: return MenuType.BACK;
            case 0x1002: return MenuType.CLOSE;
            case 0x1003: return MenuType.DOT;
            case 0x1004: return MenuType.CLOSE2;
            case 0x1005: return MenuType.ALARM;
            case 0x2000: return MenuType.SEARCH;
            case 0x2001: return MenuType.CONFIRM;
            case 0x2002: return MenuType.DELETE;
            case 0x2003: return MenuType.CAMERA;
            case 0x2004: return MenuType.ALARM;
            case 0x2005: return MenuType.CAMERA;
            case 0x2006: return MenuType.SETTINGS;
            case 0x3000: return MenuType.NEXT;
            case 0x3001: return MenuType.DONE;
            case 0x3002: return MenuType.CANCEL;
            case 0x3003: return MenuType.CROP;
            case 0x3004: return MenuType.CLEAR;
            default: return MenuType.NONE;
        }
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



    protected SMImageView _dropdownButton = null;
    protected TextContainer _textContainer = null;

    protected SMView _buttonContainer = null;
    protected SMButton _mainButton = null;
    protected SMButton[][] _menuButtons = new SMButton[2][2];
    protected SMRoundLine _menuLine[] = new SMRoundLine[4];
    protected SMSolidCircleView _menuCircle[] = new SMSolidCircleView[4];
    protected SMImageView _menuImage = null;
    protected SMSolidCircleView _alarmCircle = null;
    protected MenuTransform _menuTransform = null;
    protected TextTransform _textTransform = null;
    protected ColorTransform _colorTransform = null;
    protected MenuType _menuButtonType = MenuType.NONE;
    protected MenuBarListener _listener = null;
    protected ColorSet _colorSet = new ColorSet().NONE;
    protected ColorSet _activeColorSet = new ColorSet().NONE;
    protected SMLabel[] _textLabel = new SMLabel[2];
    protected String _textString = "";
    protected int _textIndex = 0;
    protected int _buttonIndex = 0;
    protected DropDown _dropdown = DropDown.NOTHING;
    protected DropDownAction _dropdownAction = null;
    protected TextTransition _textTransType = null;
    protected ButtonTransition _buttonTransType = null;
    protected SMView _overlapChild = null;
    protected SMToastBar _toast = null;
    protected boolean _newAlarm = false;

    protected void applyColorSet(final ColorSet colorSet) {

    }

    private void updateTextPosition(boolean dropdown) {

    }

    private void onToastHiddenComplete(SMToastBar toast) {

    }

    private void showAlarmBadge() {
        showAlaramBadge(false);
    }
    private void showAlaramBadge(boolean effect) {

    }

    public void setMenuButtonType(MenuType menuButtonType, boolean immediate) {
        setMenuButtonType(menuButtonType, immediate, false);
    }
    public void setMenuButtonType(MenuType menuButtonType, boolean immediate, boolean swipe) {

    }

    public MenuType getMenuButtonType() {return _menuButtonType;}

    public void setColorSet(final ColorSet colorSet, boolean immediate) {

    }

    public void setText(final String textString, boolean immediate) {
        setText(textString, immediate, false);
    }
    public void setText(final String textString, boolean immediate, boolean dropdown) {

    }

    public String getText() {
        return "";
    }

    public void setTextTransitionType(TextTransition type) {

    }

    public void setButtonTransitionType(ButtonTransition type) {

    }

    public void setTextWithDropDown(final String textString, boolean immediate) {

    }

    public void setOneButton(MenuType buttonId, boolean immediate) {
        setOneButton(buttonId, immediate, false);
    }
    public void setOneButton(MenuType buttonId, boolean immediate, boolean swipe) {

    }

    public void setTwoButton(MenuType buttonId1, MenuType buttonId2, boolean immediate) {
        setTwoButton(buttonId1, buttonId2, immediate, false);
    }
    public void setTwoButton(MenuType buttonId1, MenuType buttonId2, boolean immediate, boolean swipe) {

    }

    public void setDropDown(DropDown dropdown, boolean immediate) {
        setDropDown(dropdown, immediate, 0);
    }
    public void setDropDown(DropDown dropdown, boolean immediate, float delay) {

    }

    public void showButton(boolean show, boolean immediate) {

    }

    public void showActionButtonWithDelay(boolean show, float delay) {

    }

    public void showMenuButton(boolean show, boolean immediate) {

    }

    public DropDown getDropDownState() {return _dropdown;}

    void setActionBarListener(MenuBarListener l) {_listener = l;}

    @Override
    public int dispatchTouchEvent(MotionEvent event) {
        return TOUCH_TRUE;
    }

    public void setTextOffsetY(float textOffsetY) {

    }

    public void setOverlapChild(SMView child) {

    }

    public SMView getOverlapChild() {return _overlapChild;}


























    // for inner class

    protected class ColorSet implements Cloneable {
        public ColorSet() {

        }

        public ColorSet(final Color4F bg, final Color4F text, final Color4F normal, final Color4F press) {
            BG.set(bg);
            TEXT.set(text);
            NORMAL.set(normal);
            PRESS.set(press);
        }

        public Color4F BG = Color4F.WHITE;
        public Color4F TEXT = SMView.MakeColor4F(0x222222, 01.f);
        public Color4F NORMAL = SMView.MakeColor4F(0x222222, 01.f);
        public Color4F PRESS = SMView.MakeColor4F(0xadafb3, 01.f);

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
        public final ColorSet WHITE_TRANSULANT = new ColorSet(new Color4F(1, 1, 1, 0.7f), SMView.MakeColor4F(0x222222, 01.f), SMView.MakeColor4F(0x222222, 01.f), SMView.MakeColor4F(0xadafb3, 01.f));
        public final ColorSet BLACK = new ColorSet(SMView.MakeColor4F(0x222222, 01.f), Color4F.WHITE, Color4F.WHITE, SMView.MakeColor4F(0xadafb3, 01.f));
        public final ColorSet NONE = new ColorSet(Color4F.WHITE, Color4F.WHITE, Color4F.WHITE, Color4F.WHITE);
        public final ColorSet TRANSULANT = new ColorSet(new Color4F(1, 1, 1, 0), SMView.MakeColor4F(0x222222, 01.f), SMView.MakeColor4F(0x222222, 01.f), SMView.MakeColor4F(0xadafb3, 01.f));
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



    public TextContainer TextContainerCreate(IDirector director) {
        TextContainer container = new TextContainer(director);
        container.init();
        return container;
    }

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

    public MenuTransform MenuTransformCreate(IDirector director) {
        MenuTransform action = new MenuTransform(director);
        action.initWithDuration(0);
        return action;
    }

    public class MenuTransform extends DelayBaseAction {
        public MenuTransform(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {
            for (int i=0; i<4; i++) {
                SMRoundLine line = _menuBar._menuLine[i];
                _from[i] = line.getFromPosition();
                _to[i] = line.getToPosition();
                _diameter[i] = line.getLineWidth();

                _menuBar._menuCircle[i].setVisible(false);
                _menuBar._menuLine[i].setVisible(true);
            }

            switch (_menuButtonType) {
                case CLOSE:
                {
                    _toAngle = 180;
                }
                break;
                case CLOSE2:
                {
                    _toAngle = 360;//180, 90, 90
                }
                break;
                case BACK:
                {
                    _toAngle = 315;// 180, 90, 45
                }
                break;
                default:
                {
                    _toAngle = 0;
                }
                break;
            }

            _fromAngle = _menuBar._buttonContainer.getRotation();

            if (_menuButtonType==MenuType.BACK) {
                float diff = (float)(_fromAngle % 90.0f);
                _fromAngle = _toAngle - 45 - (90+diff);
            }
            if (_fromType==MenuType.BACK) {
                _fromAngle = SMView.getShortestAngle(0, _fromAngle);
            }
            if (_toAngle < _fromAngle) {
                _fromAngle -= 360;
            }
        }

        @Override
        public void onUpdate(float t) {
            t = tweenfunc.cubicEaseOut(t);

            for (int i=0; i<4; i++) {
                SMRoundLine line = _menuBar._menuLine[i];

                float x1 = SMView.interpolation(_from[i].x, _dst[i].from.x+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2, t);
                float y1 = SMView.interpolation(_from[i].y, _dst[i].from.y+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2, t);

                float x2 = SMView.interpolation(_to[i].x, _dst[i].to.x+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2, t);
                float y2 = SMView.interpolation(_to[i].y, _dst[i].to.y+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2, t);

                float angle = SMView.interpolation(_fromAngle, _toAngle, t);

                float diameter = 0.0f;
                if (_menuButtonType==MenuType.CLOSE || _menuButtonType==MenuType.CLOSE2) {
                    float tt = t * 1.5f;
                    if (tt>1) {
                        tt = 1;
                    }
                    diameter = SMView.interpolation(_diameter[i], _dst[i].diameter, tt);
                } else if (_menuButtonType==MenuType.MENU) {
                    float tt = t - 0.5f;
                    if (tt<0) {
                        tt = 0;
                    }
                    tt /= 0.5f;
                    diameter = SMView.interpolation(_diameter[i], _dst[i].diameter, tt);
                } else {
                    diameter = SMView.interpolation(_diameter[i], _dst[i].diameter, t);
                }

                line.setLineWidth(diameter);
                line.line(x1, y1, x2, y2);
                _menuBar._buttonContainer.setRotation(angle);

                // alram bubble count
                if (_fromType==MenuType.ALARM) {
                    line.setAlpha(t);
                } else if (_menuButtonType==MenuType.ALARM) {
                    line.setAlpha(1-t);
                }
            }

            if (_menuBar._menuImage!=null) {
                // alram bubble count
                if (_fromType==MenuType.ALARM) {
                    _menuBar._menuImage.setAlpha(1-t);
                } else if (_menuButtonType==MenuType.ALARM) {
                    _menuBar._menuImage.setAlpha(t);
                }
            }
        }

        @Override
        public void onEnd() {
            for (int i=0; i<4; i++) {
                if (_menuButtonType==MenuType.MENU) {
                    _menuBar._menuLine[i].setVisible(false);
                    _menuBar._menuCircle[i].setVisible(true);
                    _menuBar.showAlarmBadge();
                } else {
                    _menuBar._menuLine[i].setVisible(true);
                    _menuBar._menuCircle[i].setVisible(false);
                }
            }

            switch (_menuButtonType) {
                case CLOSE:
                {
                    _toAngle = 180;
                }
                break;
                case CLOSE2:
                {
                    _toAngle = 90;
                }
                break;
                case BACK:
                {
                    _toAngle = 315; // 180, 90, 45
                }
                break;
                default:
                {
                    _toAngle = 0;
                }
                break;
            }
            _menuBar._buttonContainer.setRotation(_toAngle);
        }

        public void cancel() {
            for (int i=0; i<4; i++) {
                if (_fromType==MenuType.MENU) {
                    _menuBar._menuLine[i].setVisible(false);
                    _menuBar._menuCircle[i].setVisible(true);
                    _menuBar.showAlarmBadge();
                } else {
                    _menuBar._menuLine[i].setVisible(true);
                    _menuBar._menuCircle[i].setVisible(false);
                }
            }

            switch (_fromType) {
                case CLOSE:
                {
                    _toAngle = 180;
                }
                break;
                case CLOSE2:
                {
                    _toAngle = 90;
                }
                break;
                case BACK:
                {
                    _toAngle = 315; // 180, 90, 45
                }
                break;
                default:
                {
                    _toAngle = 0;
                }
                break;
            }

            // restore angle
            _menuBar._buttonContainer.setRotation(_fromAngle);
            _menuBar._mainButton.setTag(menuTypeToInt(_fromType));
        }

        public void setMenuType(final MenuType fromMenuType, final MenuType menuType, final float duration) {
            setTimeValue(duration, 0);

            _fromType = fromMenuType;
            _menuButtonType = menuType;

            switch (menuType) {
                case MENU:
                {
                    _dst = sDotMenu;
                }
                break;
                case CLOSE:
                case CLOSE2:
                {
                    _dst = sDotClose;
                }
                break;
                case BACK:
                {
                    _dst = sDotBack;
                }
                break;
                default:
                {
                    _dst = sDotDot;
                }
                break;
            }
        }

        public void setMenuBar(MenuBar menuBar) {
            _menuBar = menuBar;
        }


        public Vec2[] _from = new Vec2[4];
        public Vec2[] _to = new Vec2[4];
        public float[] _diameter = new float[4];
        public float _fromAngle;
        public float _toAngle;
        public DotPosition[] _dst = null;
        public MenuType _menuButtonType;
        public MenuType _fromType;
        public MenuBar _menuBar = null;
    }

    public ColorTransform ColorTransformCreate(IDirector director) {
        ColorTransform action = new ColorTransform(director);
        action.initWithDuration(0);
        return action;
    }

    public class ColorTransform extends DelayBaseAction {
        public ColorTransform(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {
            MenuBar bar = (MenuBar)_target;
            _from = bar._activeColorSet;

            bar._colorSet = _to;
        }

        @Override
        public void onUpdate(float t) {
            MenuBar bar = (MenuBar)_target;

            bar._activeColorSet.BG = SMView.interpolateColor4F(_from.BG, _to.BG, t);
            bar._activeColorSet.TEXT = SMView.interpolateColor4F(_from.TEXT, _to.TEXT, t);
            bar._activeColorSet.NORMAL = SMView.interpolateColor4F(_from.NORMAL, _to.NORMAL, t);
            bar._activeColorSet.PRESS = SMView.interpolateColor4F(_from.PRESS, _to.PRESS, t);

            bar.applyColorSet(bar._activeColorSet);
        }

        public void setColorSet(final ColorSet toColorSet) {
            setTimeValue(0.25f, 0.1f);

            _to = toColorSet;
        }

        public ColorSet _from = null;
        public ColorSet _to = null;
    }

    public TextTransform TextTransformCreate(IDirector director) {
        TextTransform action = new TextTransform(director);
        action.initWithDuration(0);
        return action;
    }

    public class TextTransform extends DelayBaseAction {
        public TextTransform(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {

            _toPosition = _menuBar._textLabel[1-_toIndex].getPositionX();

            if (_fadeType) {
                if (_menuBar._textLabel[1-_toIndex]!=null) {
                    _menuBar._textLabel[1-_toIndex].setVisible(true);
                }
                if (_menuBar._textLabel[_toIndex]!=null) {
                    _menuBar._textLabel[_toIndex].setVisible(true);
                }
            } else {
                if (_menuBar._textLabel[_toIndex]!=null) {
                    _menuBar._textLabel[_toIndex].setVisible(true);
                    _menuBar._textLabel[_toIndex].setAlpha(1.0f);
                }
            }
        }

        @Override
        public void onUpdate(float t) {
            if (_fadeType) {
                if (_menuBar._textLabel[1-_toIndex]!=null) {
                    _menuBar._textLabel[1-_toIndex].setAlpha(1-t);
                }

                if (_menuBar._textLabel[_toIndex]!=null) {
                    _menuBar._textLabel[_toIndex].setAlpha(t);
                }
            } else {
                t = tweenfunc.cubicEaseOut(t);

                SMLabel label = _menuBar._textLabel[1-_toIndex];
                if (label!=null) {
                    // out label
                    // case char
                    if (label.isSeparateMode()) {
                        int len = label.getStringLength();
                        for (int i=0; i<len; i++) {
                            float tt = t - i*(AppConst.Config.TEXT_TRANS_DELAY/2);
                            if (tt>0) {
                                float f = tt / (AppConst.Config.TEXT_TRANS_DURATION/2);
                                if (f>1) f = 1;
                                SMLabel letter = label.getLetter(i);
                                if (letter!=null) {
                                    f = 1 - f;
                                    letter.setScale(f);
                                    letter.setAlpha(f);
                                }
                            }
                        }
                    } else {
                        label.setVisible(false);
                    }
                }

                if (t>_gap*0.6f) {
                    t -= _gap * 0.6f;

                    label = _menuBar._textLabel[_toIndex];
                    if (label!=null) {
                        // in label
                        if (label.isSeparateMode()) {
                            int len = label.getStringLength();
                            for (int i=0; i<len; i++) {
                                float tt = t - i*(AppConst.Config.TEXT_TRANS_DELAY);
                                if (tt>0) {
                                    if (!label.isVisible()) {
                                        label.setVisible(true);
                                    }
                                    float f = tt / AppConst.Config.TEXT_TRANS_DURATION;
                                    if (f>1) f=1;
                                    SMLabel letter = label.getLetter(i);
                                    if (letter!=null) {
                                        letter.setScale(0.5f + 0.5f*f);
                                        letter.setAlpha(f);
                                    }
                                }
                            }
                        } else {
                            label.setVisible(true);
                        }
                    }
                }
            }
        }

        @Override
        public void onEnd() {
            SMLabel label = _menuBar._textLabel[1-_toIndex];
            if (label!=null) {
                if (label.isSeparateMode()) {
                    label.setVisible(false);
                    int len = label.getStringLength();
                    for (int i=0; i<len; i++) {
                        SMLabel letter = label.getLetter(i);
                        if (letter!=null) {
                            letter.setScale(1);
                            letter.setAlpha(1);
                        }
                    }
                } else {
                    label.setVisible(true);
                }
            }

            if (_menuBar._textLabel[1-_toIndex]!=null) {
                _menuBar._textLabel[1-_toIndex].makeSeparate(false);
            }
            if (_menuBar._textLabel[_toIndex]!=null) {
                _menuBar._textLabel[_toIndex].makeSeparate(false);
            }
        }

        public void onCancel() {
            SMLabel label = _menuBar._textLabel[_toIndex];
            if (label!=null) {
                if (label.isSeparateMode()) {
                    label.setVisible(false);
                    int len = label.getStringLength();
                    for (int i=0; i<len; i++) {
                        SMLabel letter = label.getLetter(i);
                        if (letter!=null) {
                            letter.setScale(1);
                            letter.setAlpha(1);
                        }
                    }
                } else {
                    label.setVisible(true);
                }
            }

            if (_menuBar._textLabel[1-_toIndex]!=null) {
                _menuBar._textLabel[1-_toIndex].makeSeparate(false);
            }
            if (_menuBar._textLabel[_toIndex]!=null) {
                _menuBar._textLabel[_toIndex].makeSeparate(false);
            }
        }

        public void setFadeType() {
            _fadeType = true;
        }

        public void setElasticType() {
            _fadeType = false;
        }

        public void setMenuBar(MenuBar menuBar) {
            _menuBar = menuBar;
        }

        public void makeTextSeparate() {
            if (_menuBar._textLabel[1-_toIndex]!=null) {
                _menuBar._textLabel[1-_toIndex].makeSeparate();
            }
            if (_menuBar._textLabel[_toIndex]!=null) {
                _menuBar._textLabel[_toIndex].makeSeparate();
            }
        }

        public void setTextIndex(final int textIndex) {

            float duration = 0;
            _gap = 0;

            if (_fadeType) {
                duration = 0.25f;
            } else {
                // out label
                SMLabel label = _menuBar._textLabel[1-textIndex];
                if (label!=null) {
                    int len = label.getStringLength();
                    _gap = AppConst.Config.TEXT_TRANS_DURATION + AppConst.Config.TEXT_TRANS_DELAY*len;
                }

                // in label
                label = _menuBar._textLabel[textIndex];
                if (label!=null) {
                    if (label.isSeparateMode()) {
                        int len = label.getStringLength();
                        for (int i=0; i<len; i++) {
                            SMLabel letter = label.getLetter(i);
                            if (letter!=null) {
                                label.setScale(0);
                            }
                        }
                        duration = AppConst.Config.TEXT_TRANS_DURATION + AppConst.Config.TEXT_TRANS_DELAY*len + 0.1f;
                    } else {
                        label.setVisible(false);
                    }
                }

                duration = Math.max(duration, AppConst.Config.TEXT_TRANS_MOVE_DURATION);
                duration += _gap+0.1f;
            }

            setTimeValue(duration, 0.1f);
            _toIndex = textIndex;
        }

        public boolean _fadeType;
        public float _toPosition;
        public int _toIndex;
        public float _gap;
        public MenuBar _menuBar;
    }

    public ButtonAction ButtonActionCreate(IDirector director) {
        ButtonAction action = new ButtonAction(director);
        action.initWithDuration(0);
        return action;
    }

    public class ButtonAction extends DelayBaseAction {
        public ButtonAction(IDirector director) {
            super(director);
        }


        @Override
        public void onStart() {
            _from = _target.getScale();
            if (_show) {
                _to = 1.0f;
            } else {
                _to = 0.0f;
            }
            _target.setVisible(true);

            SMButton button = (SMButton)_target;
            button.setIconColor(STATE.NORMAL, _menuBar._activeColorSet.NORMAL);
            button.setIconColor(STATE.PRESSED, _menuBar._activeColorSet.PRESS);
        }

        @Override
        public void onUpdate(float t) {
            float tt = tweenfunc.cubicEaseOut(t);
            float scale = SMView.interpolation(_from, _to, tt);
            _target.setScale(scale);
        }

        @Override
        public void onEnd() {
            if (!_show) {
                _target.setVisible(false);
            }
        }

        public void setShow(MenuBar menuBar, final float delay) {
            _menuBar = menuBar;
            setTimeValue(0.25f, delay);
            _show = true;
        }

        public void setHide(MenuBar menuBar, final float delay) {
            _menuBar = menuBar;
            setTimeValue(0.25f, delay);
            _show = false;
        }

        protected boolean _show = false;
        protected float _from, _to;
        protected MenuBar _menuBar = null;
    }

    public ButtonFadeAction ButtonFadeActionCreate(IDirector director) {
        ButtonFadeAction action = new ButtonFadeAction(director);
        action.initWithDuration(0);
        return action;
    }

    public class ButtonFadeAction extends DelayBaseAction {
        public ButtonFadeAction(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {
            _from = _target.getAlpha();
            if (_show) {
                _to = 1.0f;
            } else {
                _to = 0.0f;
            }

            _target.setVisible(true);
            SMButton button = (SMButton)_target;
            button.setIconColor(STATE.NORMAL, _menuBar._activeColorSet.NORMAL);
            button.setIconColor(STATE.PRESSED, _menuBar._activeColorSet.PRESS);
        }

        @Override
        public void onUpdate(float t) {
            float alpha = SMView.interpolation(_from, _to, t);
            _target.setAlpha(alpha);
        }

        @Override
        public void onEnd() {
            if (!_show) {
                _target.setVisible(false);
            }
        }

        public void setShow(MenuBar menuBar, final float delay) {
            _menuBar = menuBar;
            setTimeValue(0.25f, delay);
            _show = true;
        }

        public void setHide(MenuBar menuBar, final float delay) {
            _menuBar = menuBar;
            setTimeValue(0.25f, delay);
            _show = false;
        }

        protected boolean _show;
        protected float _from, _to;
        protected MenuBar _menuBar = null;
    }

    public DropDownAction DropDownActionCreate(IDirector director) {
        DropDownAction action = new DropDownAction(director);
        action.initWithDuration(0);
        return action;
    }

    public class DropDownAction extends DelayBaseAction {
        public DropDownAction(IDirector director) {
            super(director);
        }

        @Override
        public void onStart() {
            if (_showAction) {
                _from = _target.getScale();
                if (_show) {
                    _to = 1.0f;
                } else {
                    _to = 0.0f;
                }
                _target.setVisible(true);
            } else {
                _from = _target.getRotation();
                if (_up) {
                    _to = 180;
                } else {
                    _to = 360;
                }
                _target.setVisible(true);
            }
        }

        @Override
        public void onUpdate(float t) {
            if (_showAction) {
                float tt = tweenfunc.cubicEaseOut(t);
                float scale = SMView.interpolation(_from, _to, tt);
                _target.setScale(scale);
            } else {
                float tt = tweenfunc.backEaseOut(t);
                float rotate = SMView.interpolation(_from, _to, tt);
                _target.setRotation(rotate);
            }
        }

        @Override
        public void onEnd() {
            if (_showAction) {
                if (!_show) {
                    _target.removeFromParent();
                    _menuBar._dropdownButton = null;
                    _menuBar.updateTextPosition(false);
                }
            } else {
                if (!_up) {
                    _target.setRotation(0);
                }
            }
        }

        public void setShow(MenuBar menuBar, final float delay) {
            _showAction = true;
            setTimeValue(0.25f, delay);
            _show = true;
        }

        public void setHdie(MenuBar menuBar, final float delay) {
            _showAction = true;
            setTimeValue(0.25f, delay);
            _show = false;
        }

        public void setUp(MenuBar menuBar, final float delay) {
            _showAction = false;
            _menuBar = menuBar;
            setTimeValue(0.5f, delay);
            _up = true;
        }

        public void setDown(MenuBar menuBar, final float delay) {
            _showAction = false;
            _menuBar = menuBar;
            setTimeValue(0.5f, delay);
            _up = false;
        }

        protected boolean _showAction;
        protected boolean _show;
        protected boolean _up;
        protected float _from, _to;
        protected MenuBar _menuBar;
    }
}
