package com.interpark.app.menu;

import android.graphics.Paint;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.scroller.SMScroller;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.types.Action;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.DelayBaseAction;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.util.tweenfunc;
import com.interpark.smframework.view.RingWave;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRoundLine;
import com.interpark.smframework.view.SMSolidCircleView;
import com.interpark.smframework.view.SMToastBar;

import java.util.ArrayList;

public class MenuBar extends SMView {
    public MenuBar(IDirector director) {
        super(director);
        sDotMenu[0] = new DotPosition(new Vec2(-13, -13), new Vec2(-13, -13), AppConst.SIZE.DOT_DIAMETER); // Left-Top dot
        sDotMenu[1] = new DotPosition(new Vec2(13, 13), new Vec2(13, 13), AppConst.SIZE.DOT_DIAMETER); // Right-Bottom dot
        sDotMenu[2] = new DotPosition(new Vec2(-13, 13), new Vec2(-13, 13), AppConst.SIZE.DOT_DIAMETER); // Left-Bottom dot
        sDotMenu[3] = new DotPosition(new Vec2(13, -13), new Vec2(13, -13), AppConst.SIZE.DOT_DIAMETER); // Right-Top dot

        sDotClose[0] = new DotPosition(new Vec2(-20, -20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER); // Left-Top to center (ZERO)
        sDotClose[1] = new DotPosition(new Vec2(20, 20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER); // Right-Bottom to center (ZERO)
        sDotClose[2] = new DotPosition(new Vec2(-20, 20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER); // Left-Bottom to center (ZERO)
        sDotClose[3] = new DotPosition(new Vec2(20, -20), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER); // Right-Top to center (ZERO)

        sDotBack[0] = new DotPosition(new Vec2(-16, -16), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER); // Left-Top to center (ZERO)
        sDotBack[1] = new DotPosition(new Vec2(16, +16), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER); // Rgith-Bottom to center (ZERO)
        sDotBack[2] = new DotPosition(new Vec2(-16, +12), new Vec2(-16, -16), AppConst.SIZE.LINE_DIAMETER); // Left-Bottom middle to Left-Top
        sDotBack[3] = new DotPosition(new Vec2(12, -16), new Vec2(-16, -16), AppConst.SIZE.LINE_DIAMETER); // Right middle-Top to Left-Top

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
        setAnchorPoint(Vec2.ZERO);
        setPosition(Vec2.ZERO);

//        _contentView = SMView.create(getDirector(), 0, 0, 0, getContentSize().width, getContentSize().height);
//        _contentView.setBackgroundColor(Color4F.TOAST_RED);
//        _contentView.setLocalZOrder(100);
//        addChild(_contentView);


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
//        _mainButton.setBackgroundColor(Color4F.XADAFB3);


        _buttonContainer = SMView.create(getDirector());
        _buttonContainer.setContentSize(AppConst.SIZE.TOP_MENU_BUTTONE_SIZE, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE);
        _buttonContainer.setCascadeColorEnabled(true);
        _buttonContainer.setCascadeAlphaEnable(true);

        for (int i=0; i<4; i++) {
            _menuLine[i] = SMRoundLine.create(getDirector());
            _menuLine[i].setAnchorPoint(Vec2.MIDDLE);
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
                MainClick(view);
            }
        });
        _mainButton.setPushDownScale(0.9f);
        _mainButton.setPushDownOffset(new Vec2(0, -3));
        addChild(_mainButton);


        return true;
    }

    public void MainClick(SMView view) {
        if (_mainButton.getActionByTag(AppConst.TAG.USER+1)!=null) {
            // in transform action...
            return;
        }

        if (_listener!=null) {
            if (_listener.func1(view)) {
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

    // func1 -> onMenuBarClick;
    // func2 -> onMenuBarTouch;
    public interface MenuBarListener {
        public boolean func1(SMView view);
        public void func2();
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



    protected SMView _contentView = null;
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

    public void setMenuButtonType(MenuType menuButtonType, boolean immediate) {
        setMenuButtonType(menuButtonType, immediate, false);
    }
    public void setMenuButtonType(MenuType menuButtonType, boolean immediate, boolean swipe) {
        if (_menuButtonType==menuButtonType) {
            if (_menuTransform!=null) {
                _menuTransform.setMenuType(_menuButtonType, menuButtonType, 0.0f);
            }
            return;
        }

        DotPosition[] to = null;

        switch (menuButtonType) {
            case MENU: to = sDotMenu; _mainButton.setTag(menuTypeToInt(MenuType.MENU)); break;
            case CLOSE: to = sDotClose; _mainButton.setTag(menuTypeToInt(MenuType.CLOSE)); break;
            case BACK: to = sDotBack; _mainButton.setTag(menuTypeToInt(MenuType.BACK)); break;
            case DOT: to = sDotDot; _mainButton.setTag(menuTypeToInt(MenuType.DOT)); break;
            case CLOSE2: to = sDotClose; _mainButton.setTag(menuTypeToInt(MenuType.CLOSE)); break;
            case ALARM:
            {
                if (_menuImage==null) {
                    _menuImage = SMImageView.create(getDirector(), "images/ic_titlebar_notice.png");
                    _menuImage.setPosition(_buttonContainer.getContentSize().width/2, _buttonContainer.getContentSize().height/2);
                    _menuImage.setColor(Color4F.TEXT_BLACK);
                    _buttonContainer.addChild(_menuImage);

                    // if you need.. regist... ready to receiving
                }
                to = null;
                _mainButton.setTag(menuTypeToInt(MenuType.ALARM));

                // getting new Alaram count.
                _newAlarm = false;

                showAlarmBadge();
            }
            break;
            default:
            {
                return;
    }
    }

        Action action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_MENU);
        if (action!=null) {
            // action is _menuTransform
            stopAction(action);
    }

        if (_menuButtonType==MenuType.NONE || immediate) {
            if (menuButtonType==MenuType.ALARM) {
                for (int i=0; i<4; i++) {
                    _menuLine[i].setVisible(false);
                    _menuCircle[i].setVisible(false);
                }

                _menuImage.setVisible(true);
                _menuImage.setScale(1.0f);
                showAlarmBadge();
            } else {
            for (int i=0; i<4; i++) {
                SMRoundLine l = _menuLine[i];
                l.setLineWidth(to[i].diameter);
                l.line(to[i].from.add(MenuButtonCenter), to[i].to.add(MenuButtonCenter));

                if (menuButtonType==MenuType.MENU) {
                    _menuLine[i].setVisible(false);
                    _menuCircle[i].setVisible(true);
                    showAlarmBadge();
                } else {
                    _menuLine[i].setVisible(true);
                    _menuCircle[i].setVisible(false);
                }
            }
            }


        float angle = 0;
        switch (menuButtonType) {
            case CLOSE: angle=180;break;
            case CLOSE2: angle=90;break;
            case BACK: angle=315;break;//180, 90, 45
            default: angle=0; break;
    }

        _buttonContainer.setRotation(angle);
        _menuButtonType = menuButtonType;
    }

        if (_menuTransform==null) {
            _menuTransform = MenuTransformCreate(getDirector());
            _menuTransform.setMenuBar(this);
            _menuTransform.setTag(AppConst.TAG.ACTION_MENUBAR_MENU);
        }

        _menuTransform.setMenuType(_menuButtonType, menuButtonType, 0.45f);
        if (!swipe) {
            runAction(_menuTransform);
        }

        _menuButtonType = menuButtonType;
    }

    public MenuType getMenuButtonType() {return _menuButtonType;}

    public void setColorSet(final ColorSet colorSet, boolean immediate) {
        if (_colorSet.equals(colorSet)) {
            return;
        }

        Action action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_COLOR);
        if (action!=null) {
            stopAction(action);
        }

        if (immediate) {
            _colorSet.set(colorSet);
            _activeColorSet.set(colorSet);

            applyColorSet(colorSet);
        } else {
            if (_colorTransform!=null) {
                _colorTransform = ColorTransformCreate(getDirector());
                _colorTransform.setTag(AppConst.TAG.ACTION_MENUBAR_COLOR);
            }

            _colorTransform.setColorSet(colorSet);
            runAction(_colorTransform);
        }
    }

    public void setText(final String textString, boolean immediate) {
        setText(textString, immediate, false);
    }
    public void setText(final String textString, boolean immediate, boolean dropdown) {
        if (_textString==textString) return;

        _textString = textString;
        _textIndex = 1 - _textIndex;

        if (_textLabel[_textIndex]==null) {
            _textLabel[_textIndex] = SMLabel.create(getDirector(), "", 40, Color4F.TEXT_BLACK, Paint.Align.CENTER, true);
            _textLabel[_textIndex].setAnchorPoint(Vec2.MIDDLE);
            _textLabel[_textIndex].setCascadeAlphaEnable(true);
            _textContainer.stub[_textIndex].addChild(_textLabel[_textIndex]);
        }

        _textLabel[_textIndex].setText(textString);
        _textLabel[_textIndex].setColor(_activeColorSet.TEXT);
        _textLabel[_textIndex].setVisible(false);

        updateTextPosition(dropdown);

        Action action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_TEXT);
        if (action!=null) {
            stopAction(action);
        }

        if (immediate) {
            _textLabel[_textIndex].setVisible(true);
            if (_textLabel[1-_textIndex]!=null) {
                _textLabel[1-_textIndex].setVisible(false);
            }
            return;
        }

        if (_textTransform==null) {
            _textTransform = TextTransformCreate(getDirector());
            _textTransform.setTag(AppConst.TAG.ACTION_MENUBAR_TEXT);
            _textTransform.setMenuBar(this);
        }

        if (_textTransType==TextTransition.ELASTIC) {
            // for separate text animation... make letter
            _textTransform.makeTextSeparate();
            _textTransform.setElasticType();
        } else {
            _textTransform.setFadeType();
        }

//        // for ElasticTest
//        if (_textTransType==TextTransition.SWIPE) {
//            _textTransform.setElasticType();
//            _textTransform.makeTextSeparate();
//        }

        _textTransform.setTextIndex(_textIndex);

        if (_textTransType!=TextTransition.SWIPE) {
            runAction(_textTransform);
        }
    }

    public String getText() {
        return _textString;
    }

    public void setTextTransitionType(TextTransition type) {
        _textTransType = type;
    }

    public void setButtonTransitionType(ButtonTransition type) {
        _buttonTransType = type;
    }

    public void setTextWithDropDown(final String textString, boolean immediate) {
        setText(textString, immediate, true);
    }

    public void setOneButton(MenuType buttonType, boolean immediate) {
        setOneButton(buttonType, immediate, false);
    }
    public void setOneButton(MenuType buttonType, boolean immediate, boolean swipe) {
        setTwoButton(buttonType, MenuType.NONE, immediate, swipe);
    }
    public void setTwoButton(MenuType buttonType1, MenuType buttonType2, boolean immediate) {
        setTwoButton(buttonType1, buttonType2, immediate, false);
    }
    public void setTwoButton(MenuType buttonType1, MenuType buttonType2, boolean immediate, boolean swipe) {
//        ACTION_BUTTON_DELAY
        float delay = 0;

        final MenuType[] types = new MenuType[] {buttonType1, buttonType2};

        if (!swipe) {
            for (int i=0; i<2; i++) {
                SMButton button = _menuButtons[_buttonIndex][i];
                if (button!=null && button.isVisible()) {
                    if (immediate) {
                        button.setVisible(false);
                    } else {
                        Action action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                        if (action!=null) {
                            stopAction(action);
                        }

                        if (_buttonTransType==ButtonTransition.ELASTIC) {
                            ButtonAction buttonAction = ButtonActionCreate(getDirector());
                            buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                            buttonAction.setHide(this, delay);
                            button.runAction(buttonAction);
                        } else {
                            ButtonFadeAction buttonFadeAction = ButtonFadeActionCreate(getDirector());
                            buttonFadeAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                            buttonFadeAction.setHide(this, 0);
                            button.runAction(buttonFadeAction);
                        }

                        delay += AppConst.Config.ACTION_BUTTON_DELAY;
                    }
                }
            }

            if (delay>0) {
                delay += 0.2f;
            }
        } else {
            immediate = true;
        }

        _buttonIndex = 1 - _buttonIndex;

        float x = _contentSize.width - 20;
        int index = 0;

        for (int i=0; i<2; i++) {
            if (types[i]==MenuType.NONE) continue;

            SMButton button = _menuButtons[_buttonIndex][i];
            if (button==null) {
                button = SMButton.create(getDirector(), menuTypeToInt(MenuType.NONE), SMButton.STYLE.DEFAULT, 0, 0, AppConst.SIZE.TOP_MENU_BUTTON_HEIGHT, AppConst.SIZE.TOP_MENU_BUTTON_HEIGHT, 0.5f, 0.5f);
                button.setIconColor(STATE.NORMAL, _activeColorSet.NORMAL);
                button.setIconColor(STATE.PRESSED, _activeColorSet.PRESS);
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(SMView view) {
                        MainClick(view);
                    }
                });
                button.setPushDownScale(0.9f);
                button.setPushDownOffset(new Vec2(0, -3));
                addChild(button);
                _menuButtons[_buttonIndex][index] = button;
            }

            SMView icon = null;
            boolean textIcon = false;
            if (button.getTag()!=menuTypeToInt(types[i])) {
                button.setTag(menuTypeToInt(types[i]));
                // incase text icon... (if image icon then textIcon is false)
                switch (types[i]) {
                    case NEXT:
                    {
                        icon = SMLabel.create(getDirector(), "NEXT", 38);
                        icon.setAnchorPoint(Vec2.MIDDLE);
                        textIcon = true;
                    }
                    break;
                    case DONE:
                    {
                        icon = SMLabel.create(getDirector(), "DONE", 38);
                        icon.setAnchorPoint(Vec2.MIDDLE);
                        textIcon = true;
                    }
                    break;
                    case CANCEL:
                    {
                        icon = SMLabel.create(getDirector(), "CANCEL", 38);
                        icon.setAnchorPoint(Vec2.MIDDLE);
                        textIcon = true;
                    }
                    break;
                    default:
                    {
                        icon = SMLabel.create(getDirector(), "Whatever", 38);
                        icon.setAnchorPoint(Vec2.MIDDLE);
                        textIcon = true;
                    }
                    break;
                }
                button.setIcon(STATE.NORMAL, icon);
            }
            if (icon!=null) {
                int width = 0;
                if (textIcon) {
                    width = (int)icon.getContentSize().width;
                    x -= 20;
                }

                x -= width/2;
                button.setPosition(x, AppConst.SIZE.MENUBAR_HEIGHT/2);
                x -= width/2 + 20;
            }

            if (immediate) {
                button.setScale(1);
                button.setVisible(true);
            } else {
                Action action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                if (action!=null) {
                    stopAction(action);
                }

                if (_buttonTransType==ButtonTransition.ELASTIC) {
                    button.setScale(0);
                    button.setAlpha(1.0f);
                    ButtonAction buttonAction = ButtonActionCreate(getDirector());
                    buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                    buttonAction.setShow(this, delay);
                    button.runAction(buttonAction);
                } else {
                    button.setAlpha(0);
                    button.setScale(1);
                    ButtonFadeAction buttonFadeAction = ButtonFadeActionCreate(getDirector());
                    buttonFadeAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                    buttonFadeAction.setShow(this, 0.1f);
                    button.runAction(buttonFadeAction);
                }

                delay += AppConst.Config.ACTION_BUTTON_DELAY;
            }

            index++;
        }
    }

    public void setDropDown(DropDown dropdown, boolean immediate) {
        setDropDown(dropdown, immediate, 0);
    }
    public void setDropDown(DropDown dropdown, boolean immediate, float delay) {
        if (dropdown == _dropdown)
            return;

        if (dropdown == DropDown.NOTHING) {
            _textContainer.setOnClickListener(null);
        } else {
            _textContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(SMView view) {
                    MainClick(view);
                }
            });
        }

        if (_dropdownButton!=null) {
            Action action = _dropdownButton.getActionByTag(AppConst.TAG.ACTION_MENUBAR_DROPDOWN);
            if (action!=null) {
                _dropdownButton.stopAction(action);
            }
        }

        if (immediate) {
            _dropdown = dropdown;
            if (dropdown == DropDown.NOTHING) {
                if (_dropdownButton!=null) {
                    removeChild(_dropdownButton);
                    _dropdownButton = null;
                }
            } else {
                if (_dropdownButton == null) {
                    _dropdownButton = SMImageView.create(getDirector(), "images/arrow_bottom.png");
                    _dropdownButton.setColor(_activeColorSet.TEXT);
                    addChild(_dropdownButton);
                }
                if (dropdown == DropDown.UP) {
                    _dropdownButton.setRotation(180);
                } else {
                    _dropdownButton.setRotation(0);
                }
                _dropdownButton.setScale(1);
                _dropdownButton.setVisible(true);
            }

            updateTextPosition(dropdown != DropDown.NOTHING);

            return;
        }
        // animation
        if (_dropdownAction == null) {
            _dropdownAction = DropDownActionCreate(getDirector());
            _dropdownAction.setTag(AppConst.TAG.ACTION_MENUBAR_DROPDOWN);
        }

        boolean created = false;
        if (_dropdownButton == null) {
            _dropdownButton = SMImageView.create(getDirector(), "images/arrow_bottom.png");
            _dropdownButton.setColor(_activeColorSet.TEXT);
            _dropdownButton.setVisible(false);
            _dropdownButton.setScale(0);
            addChild(_dropdownButton);
            updateTextPosition(true);
            created = true;
        }

        if (dropdown == DropDown.UP) {
            if (created) {
                _dropdownButton.setRotation(180);
                _dropdownAction.setShow(this, delay);
                _dropdownButton.runAction(_dropdownAction);
            } else {
                _dropdownAction.setUp(this, delay);
                _dropdownButton.runAction(_dropdownAction);
            }
        } else if (dropdown == DropDown.DOWN) {
            if (created) {
                _dropdownButton.setRotation(0);
                _dropdownAction.setShow(this, delay);
                _dropdownButton.runAction(_dropdownAction);
            } else {
                _dropdownAction.setDown(this, delay);
                _dropdownButton.runAction(_dropdownAction);
            }
        } else { // NOTHING
            _dropdownAction.setHide(this, delay);
            _dropdownButton.runAction(_dropdownAction);
        }

        _dropdown = dropdown;
    }

    public void showButton(boolean show, boolean immediate) {
        for (int i = 0;  i < 2; i++) {
            SMButton button = _menuButtons[_buttonIndex][i];
            if (button!=null && button.getTag() != menuTypeToInt(MenuType.NONE)) {
                if (immediate) {
                    if (show) {
                        button.setScale(1);
                        button.setVisible(true);
                    } else {
                        button.setScale(0);
                        button.setVisible(false);
                    }
                } else {
                    Action action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                    if (action!=null) {
                        button.stopAction(action);
                    }
                    ButtonAction buttonAction = ButtonActionCreate(getDirector());
                    buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                    if (show) {
                        buttonAction.setShow(this, 0);
                    } else {
                        buttonAction.setHide(this, 0);
                    }
                    button.runAction(buttonAction);
                }
            }
        }
        // menu button
        if (_mainButton==null)
            return;

        SMButton button = _mainButton;
        if (immediate) {
            if (show) {
                button.setScale(1);
                button.setVisible(true);
            } else {
                button.setScale(0);
                button.setVisible(false);
            }
        } else {
            Action action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
            if (action!=null) {
                button.stopAction(action);
            }
            if (_buttonTransType == ButtonTransition.ELASTIC) {
                ButtonAction buttonAction = ButtonActionCreate(getDirector());
                buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                if (show) {
                    buttonAction.setShow(this, 0);
                } else {
                    buttonAction.setHide(this, 0);
                }
                button.runAction(buttonAction);
            } else {
                ButtonFadeAction buttonFadeAction = ButtonFadeActionCreate(getDirector());
                buttonFadeAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                if (show) {
                    buttonFadeAction.setShow(this, 0);
                } else {
                    buttonFadeAction.setHide(this, 0);
                }
                button.runAction(buttonFadeAction);
            }
        }
    }

    public void showActionButtonWithDelay(boolean show, float delay) {
        for (int i = 0;  i < 2; i++) {
            SMButton button = _menuButtons[_buttonIndex][i];
            if (button!=null && button.getTag() != menuTypeToInt(MenuType.NONE)) {
                Action action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                if (action!=null) {
                    button.stopAction(action);
                }
                ButtonAction buttonAction = ButtonActionCreate(getDirector());
                buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
                if (show) {
                    buttonAction.setShow(this, delay);
                } else {
                    buttonAction.setHide(this, delay);
                }
                button.runAction(buttonAction);
            }
        }
    }

    public void showMenuButton(boolean show, boolean immediate) {
        if (_mainButton==null)
            return;

        SMButton button = _mainButton;
        if (immediate) {
            if (show) {
                button.setScale(1);
                button.setVisible(true);
            } else {
                button.setScale(0);
                button.setVisible(false);
            }
        } else {
            Action action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
            if (action!=null) {
                button.stopAction(action);
            }
            ButtonAction buttonAction = ButtonActionCreate(getDirector());
            buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON);
            if (show) {
                buttonAction.setShow(this, 0);
            } else {
                buttonAction.setHide(this, 0);
            }
            button.runAction(buttonAction);
        }
    }

    public DropDown getDropDownState() {return _dropdown;}

    public void setMenuBarListener(MenuBarListener l) {_listener = l;}

    @Override
    public int dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);

        int action = event.getAction();
        if (action==MotionEvent.ACTION_DOWN) {
            if (_listener!=null) {
                _listener.func2();
            }
        }

        return TOUCH_TRUE;
    }

    public void setTextOffsetY(float textOffsetY) {
        if (_textContainer!=null) {
            _textContainer.setPositionY(AppConst.SIZE.MENUBAR_HEIGHT/2+textOffsetY);
        }
    }

    public void setOverlapChild(SMView child) {
        _overlapChild = child;
    }

    public SMView getOverlapChild() {return _overlapChild;}

    @Override
    public boolean containsPoint(float x, float y) {
        if (_overlapChild!=null) {
            Vec2 worldPt = convertToWorldSpace(new Vec2(x, y));
            Vec2 pt = _overlapChild.convertToNodeSpace(worldPt);
            if (_overlapChild.containsPoint(pt)) {
                return false;
            }
        }

        return super.containsPoint(x, y);
    }

    public MenuBarListener getMenuBarListener() {return _listener;}

    public SMView getButtonByType(MenuType type) {
        for (int i=0; i<2; i++) {
            SMButton button = _menuButtons[_buttonIndex][i];
            if (button!=null && button.getTag()==menuTypeToInt(type)) {
                return button;
            }
        }
        return null;
    }

    public ArrayList<MenuType> getButtonTypes() {
        ArrayList<MenuType> buttonTypes = new ArrayList<>();
        for (int i=0; i<2; i++) {
            SMButton button = _menuButtons[_buttonIndex][i];
            if (button!=null && button.isVisible()) {
                buttonTypes.add(intToMenuType(button.getTag()));
            }
        }
        return buttonTypes;
    }

    public void onSwipeStart() {
        if (_textTransType == TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform.onStart();
            _menuTransform.onStart();
        }
    }

    public void onSwipeUpdate(float t) {
        if (_textTransType == TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform.onUpdate(t);
            _menuTransform.onUpdate(t);

            for (int i = 0; i < 2; i++) {
                SMButton button = _menuButtons[1 - _buttonIndex][i];
                if (button!=null && button.isVisible()) {
                    button.setAlpha(1-t);
                }
                button = _menuButtons[_buttonIndex][i];
                if (button!=null && button.isVisible()) {
                    button.setAlpha(t);
                }
            }
        }
    }

    public void onSwipeComplete() {
        if (_textTransType == TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform.onEnd();
            _menuTransform.onEnd();

            for (int i = 0; i < 2; i++) {
                SMButton button = _menuButtons[1-_buttonIndex][i];
                if (button!=null && button.isVisible()) {
                    button.setVisible(false);
                }
                button = _menuButtons[_buttonIndex][i];
                if (button!=null && button.isVisible()) {
                    button.setAlpha(1);
                }
            }
        }
    }

    public void onSwipeCancel() {
        if (_textTransType == TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform.onCancel();
            _textIndex = 1 - _textIndex;
            _textString = _textLabel[_textIndex].getText();

            _menuButtonType = _menuTransform._fromType;
            _menuTransform.onCancel();

            _buttonIndex = 1 - _buttonIndex;
            for (int i = 0; i < 2; i++) {
                SMButton button = _menuButtons[1 - _buttonIndex][i];
                if (button!=null && button.isVisible()) {
                    button.setVisible(false);
                }
                button = _menuButtons[_buttonIndex][i];
                if (button!=null && button.isVisible()) {
                    button.setAlpha(1);
                }
            }
        }
    }

    public void showToast(final String message, final Color4F color, float duration) {
        if (_toast == null) {
            _toast = SMToastBar.create(getDirector(), new SMToastBar.ToastBarCallback() {
                @Override
                public void onToastBarHide(SMToastBar bar) {
                    onToastHideComplete(bar);
                }
            });
            addChild(_toast, -100);
        }

        _toast.setMessage(message, color, duration);
    }

    protected void applyColorSet(final ColorSet colorSet) {
        setBackgroundColor(colorSet.BG);

        if (_textLabel[0]!=null) {
            _textLabel[0].setColor(colorSet.TEXT);
        }
        if (_textLabel[1]!=null) {
            _textLabel[1].setColor(colorSet.TEXT);
        }
        if (_dropdownButton!=null) {
            _dropdownButton.setColor(colorSet.TEXT);
        }

        _mainButton.setButtonColor(STATE.NORMAL, colorSet.NORMAL);
        _mainButton.setButtonColor(STATE.PRESSED, colorSet.PRESS);

        for (int i=0; i<2; i++) {
            for (int j=0; j<2; j++) {
                SMButton button = _menuButtons[i][j];
                if (button!=null) {
                    button.setIconColor(STATE.NORMAL, colorSet.NORMAL);
                    button.setIconColor(STATE.PRESSED, colorSet.PRESS);
                }
            }
        }
    }

    private void updateTextPosition(boolean dropdown) {
        if (_textLabel[_textIndex] == null)
            return;

        float containerWidth = _textLabel[_textIndex].getContentSize().width;

        if (dropdown) {
            containerWidth += 16 + 36;
    }

        Vec2 centerPt = new Vec2(containerWidth/2, AppConst.SIZE.MENUBAR_HEIGHT/2);
        _textContainer.setContentSize(new Size(containerWidth, AppConst.SIZE.MENUBAR_HEIGHT));
        _textContainer.stub[0].setPosition(centerPt);
        _textContainer.stub[1].setPosition(centerPt);

        if (dropdown) {
            _textLabel[_textIndex].setPositionX(-16-18);
        } else {
            _textLabel[_textIndex].setPositionX(0);
        }

        if (_dropdownButton!=null && dropdown) {
            _dropdownButton.setPosition((_contentSize.width+containerWidth)/2 - 18, AppConst.SIZE.MENUBAR_HEIGHT/2);
        }
    }

    private void onToastHideComplete(SMToastBar toast) {
        if (_toast!=null) {
            removeChild(_toast);
            _toast = null;
        }
    }

    private void showAlarmBadge() {
        showAlaramBadge(false);
    }
    private void showAlaramBadge(boolean effect) {
        if (_newAlarm==false) return;

        if (_alarmCircle == null) {
            if (_menuImage!=null) {
                _alarmCircle = SMSolidCircleView.create(getDirector());

                _alarmCircle.setContentSize(new Size(12, 12));
                _alarmCircle.setColor(Color4F.ALARM_BADGE_RED);
                _alarmCircle.setAnchorPoint(Vec2.MIDDLE);
                _alarmCircle.setPosition(73, 73);
                _menuImage.addChild(_alarmCircle );
            }
        }
        if (_alarmCircle!=null) {
            _alarmCircle.setAlpha(0);
            _alarmCircle.stopAllActions();
            TransformAction a = TransformAction.create(getDirector());
            a.toAlpha(1).setTimeValue(0.2f, 0);
            _alarmCircle.runAction(a);

            if (effect) {
                Size size = new Size(_alarmCircle.getContentSize());
                RingWave.show(getDirector(), _alarmCircle, size.width/2, size.height/2, 50, 0.4f, 0.1f, Color4F.ALARM_BADGE_RED);
            }
        }
    }





    // for inner class

    public static class ColorSet implements Cloneable {
        public ColorSet() {

        }

        public ColorSet(final Color4F bg, final Color4F text, final Color4F normal, final Color4F press) {
            BG.set(bg);
            TEXT.set(text);
            NORMAL.set(normal);
            PRESS.set(press);
        }

        public Color4F BG = new Color4F(Color4F.WHITE);
        public Color4F TEXT = SMView.MakeColor4F(0x222222, 1.0f);
        public Color4F NORMAL = SMView.MakeColor4F(0x222222, 1.0f);
        public Color4F PRESS = SMView.MakeColor4F(0xadafb3, 1.0f);

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

        public static final ColorSet WHITE = new ColorSet();
        public static final ColorSet WHITE_TRANSULANT = new ColorSet(new Color4F(1, 1, 1, 0.7f), SMView.MakeColor4F(0x222222, 1.0f), SMView.MakeColor4F(0x222222, 1.0f), SMView.MakeColor4F(0xadafb3, 1.0f));
        public static final ColorSet BLACK = new ColorSet(SMView.MakeColor4F(0x222222, 1.0f), Color4F.WHITE, Color4F.WHITE, SMView.MakeColor4F(0xadafb3, 1.0f));
        public static final ColorSet NONE = new ColorSet(Color4F.WHITE, Color4F.WHITE, Color4F.WHITE, Color4F.WHITE);
        public static final ColorSet TRANSULANT = new ColorSet(new Color4F(1, 1, 1, 0), SMView.MakeColor4F(0x222222, 1.0f), SMView.MakeColor4F(0x222222, 1.0f), SMView.MakeColor4F(0xadafb3, 1.0f));
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
            stub[0].setAnchorPoint(Vec2.MIDDLE);
            stub[0].setCascadeAlphaEnable(true);
            stub[1] = new SMView(getDirector());
            stub[1].setAnchorPoint(Vec2.MIDDLE);
            stub[1].setCascadeAlphaEnable(true);

            addChild(stub[0]);
            addChild(stub[1]);
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
            float lineWidth = 1.0f;

            for (int i=0; i<4; i++) {
                SMRoundLine line = _menuBar._menuLine[i];
                _from[i] = line.getFromPosition();
                _to[i] = line.getToPosition();
                if (_menuButtonType==MenuType.MENU && isFirstMenu) {
                    _diameter[i] = 1.0f;
                } else {
                _diameter[i] = line.getLineWidth();
                }

                _menuBar._menuCircle[i].setVisible(false);
                _menuBar._menuLine[i].setVisible(true);
            }
            if (_menuButtonType==MenuType.MENU && isFirstMenu) {
                isFirstMenu = false;
            }

            _fromAngle = _menuBar._buttonContainer.getRotation();

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
                    if (_fromType!=MenuType.BACK) {
                    _toAngle = 315;// 180, 90, 45
                    } else {
                        // on rotate back to back
                        _toAngle = _fromAngle;
                    }
                }
                break;
                default:
                {
                    _toAngle = 0;
                }
                break;
            }


            if (_menuButtonType==MenuType.BACK) {
                float diff = _fromAngle % 90.0f;
                // make 180 degrees bottom -> left bottom -> left
                if (_fromType!=_menuButtonType) {
                _fromAngle = _toAngle - 45 - (90 + diff);
            }
            }
            if (_fromType==MenuType.BACK) {
                if (_fromType!=_menuButtonType) {
                _fromAngle = SMView.getShortestAngle(0, _fromAngle);
                _toAngle = 90;
            }
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
                    _toAngle = 315;
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

        public void onCancel() {
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
            if (_fromAngle!=_toAngle) {
            _menuBar._buttonContainer.setRotation(_fromAngle);
            }
            _menuBar._mainButton.setTag(menuTypeToInt(_fromType));
        }

        public void setMenuType(final MenuType fromMenuType, final MenuType toMenuType, final float duration) {
            setTimeValue(duration, 0);

            _fromType = fromMenuType;
            _menuButtonType = toMenuType;

            switch (toMenuType) {
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


        public boolean isFirstMenu = true;
        public Vec2[] _from = new Vec2[4];
        public Vec2[] _to = new Vec2[4];
        public float[] _diameter = new float[4];
        public float _fromAngle = 0.0f;
        public float _toAngle = 0.0f;
        public DotPosition[] _dst = null;
        public MenuType _menuButtonType = MenuType.NONE;
        public MenuType _fromType = MenuType.MENU;
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

//            _toPosition = _menuBar._textLabel[1-_toIndex].getPositionX();

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
                if (t<0)t=0;
                if (t>1)t=1;

                // out text... start 0.4... end 0.8f
                float outValue = 0;
                float outStart = 0.4f;
                float outEnd = 0.8f;
                if (t>=outStart && t<=outEnd) {
                    outValue = t-outStart;
                    if (outValue>0) {
                        outValue /= 0.4f;
                    }
                }
                if (t>outEnd)outValue=1;
                // in text... start 0.6... end 1.0f
                float inValue = 0;
                float inStart = 0.5f;
                float inEnd = 0.9f;
                if (t>=inStart && t<inEnd) {
                    inValue = t-inStart;
                    if (inValue>0) {
                        inValue /= 0.4f;
                    }
                }
                if (t>inEnd)inValue=1;

                if (t>=outStart) {
                    // out text label stride moving.
                SMLabel label = _menuBar._textLabel[1-_toIndex];
                if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (int i=0; i<label.getSeparateCount(); i++) {
                                SMLabel letter = label.getLetter(i);
                                if (letter!=null) {
                                    float tt = outValue - i*(AppConst.Config.TEXT_TRANS_DELAY/2);
                                    if (tt>0) {
                                        float f = tt / (AppConst.Config.TEXT_TRANS_DURATION/2);
                                        if (f>1)f=1;

                                    f = 1.0f - f;
                                    letter.setScale(f);
                                        letter.setAlpha(f);
                                }
                            }
                        }
                    } else {
                            label.setAlpha(1-t);
                    }
                }

                    label = _menuBar._textLabel[_toIndex];
                    if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (int i=0; i<label.getSeparateCount(); i++) {
                                SMLabel letter = label.getLetter(i);
                                if (letter!=null) {
                                    float tt = inValue - i*(AppConst.Config.TEXT_TRANS_DELAY);
                                if (tt>0) {
                                    if (!label.isVisible()) {
                                        label.setVisible(true);
                                    }
                                    float f = tt / AppConst.Config.TEXT_TRANS_DURATION;
                                        if (f>1)f=1;

                                        float newScale = 0.5f + 0.5f*f;
                                        letter.setScale(newScale);
                                        letter.setAlpha(f);
                                    }
                                }
                            }
                        } else {
                            label.setAlpha(t);
                        }
                    }
                } else {
                    // not animation...yet...

                    // out text
                    SMLabel label = _menuBar._textLabel[1-_toIndex];
                    if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (int i=0; i<label.getSeparateCount(); i++) {
                                SMLabel letter = label.getLetter(i);
                                if (letter!=null) {
                                    letter.setScale(1);
                                    letter.setAlpha(1);
                                }
                            }
                        } else {
                            label.setAlpha(1);
                        }
                    }

                    // in text
                    label = _menuBar._textLabel[_toIndex];
                    if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (int i=0; i<label.getSeparateCount(); i++) {
                                SMLabel letter = label.getLetter(i);
                                if (letter!=null) {
                                    letter.setScale(0);
                                    letter.setAlpha(0);
                                }
                            }
                        } else {
                            label.setAlpha(0);
                        }
                    }
                }
            }
        }

        @Override
        public void onEnd() {
            // out text hidden
            SMLabel label = _menuBar._textLabel[1-_toIndex];
            if (label!=null) {
                if (label.getSeparateCount()>0) {
                    label.setVisible(false);
                    int len = label.getStringLength();
                    for (int i=0; i<len; i++) {
                        SMLabel letter = label.getLetter(i);
                        if (letter!=null) {
                            letter.setScale(0);
                            letter.setAlpha(0);
                        }
                    }
                } else {
                    label.setVisible(true);
                }
            }

            if (_menuBar._textLabel[1-_toIndex]!=null) {
                _menuBar._textLabel[1-_toIndex].clearSeparate();
            }
            if (_menuBar._textLabel[_toIndex]!=null) {
                _menuBar._textLabel[_toIndex].clearSeparate();
            }
        }

        public void onCancel() {
            // in text hidden
            SMLabel label = _menuBar._textLabel[_toIndex];
            if (label!=null) {
//                label.setVisible(true);
                if (label.getSeparateCount()>0) {
                    label.setVisible(false);
                    int len = label.getSeparateCount();
                    for (int i=0; i<len; i++) {
                        SMLabel letter = label.getLetter(i);
                        if (letter!=null) {
                            letter.setScale(0);
                            letter.setAlpha(0);
                        }
                    }
                } else {
                    label.setVisible(true);
                }
            }

            if (_menuBar._textLabel[1-_toIndex]!=null) {
                _menuBar._textLabel[1-_toIndex].clearSeparate();
            }
            if (_menuBar._textLabel[_toIndex]!=null) {
                _menuBar._textLabel[_toIndex].clearSeparate();
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
                    if (label.getSeparateCount()>0) {
                        int len = label.getSeparateCount();
                        for (int i=0; i<len; i++) {
                            SMLabel letter = label.getLetter(i);
                            if (letter!=null) {
                                letter.setScale(0);
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
//        public float _toPosition;
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

        public void setHide(MenuBar menuBar, final float delay) {
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
