package com.interpark.smframework.base;

import android.util.Log;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;

import org.apache.http.cookie.SM;

import java.util.ArrayList;

public class SMMenuTransitionScene extends SMScene {
    public SMMenuTransitionScene(IDirector director) {
        super(director);
        _menuBarButton[0] = MenuBar.MenuType.NONE;
        _menuBarButton[1] = MenuBar.MenuType.NONE;
    }

    public static SMMenuTransitionScene create(IDirector director, MenuBar menuBar) {
        SMMenuTransitionScene scene = new SMMenuTransitionScene(director);
        scene.initWithMenuBar(menuBar);
        return scene;
    }

    protected MenuBar _menuBar = null;

    protected String _menuTitle = "";
    protected String _prevMenuTitle = "";

    protected MenuBar.MenuType[] _menuBarButton = new MenuBar.MenuType[2];
    protected ArrayList<MenuBar.MenuType> _prevManuBarButton = new ArrayList<>();

    protected boolean _swipeStarted = false;

    protected MenuBar.MenuBarListener _menuBarListener = new MenuBar.MenuBarListener() {
        @Override
        public boolean func1(SMView view) {
            return onMenuBarClick(view);
        }

        @Override
        public void func2() {
            onMenuBarTouch();
        }
    };

    protected MenuBar.MenuType _fromMenuType = MenuBar.MenuType.NONE;
    protected MenuBar.MenuType _toMenuType = MenuBar.MenuType.NONE;

    // func1 -> onMenuBarClick;
    // func2 -> onMenuBarTouch;

    // override to this func
    protected boolean onMenuBarClick(SMView view) {
        return false;
    }

    protected void onMenuBarTouch() { }

    protected boolean initWithMenuBar(MenuBar menuBar) {
        // We must use only SwipeType BACK !!!
        super.initWithSceneParams(null, SwipeType.BACK);
        Size size = new Size(getDirector().getWidth(), getDirector().getHeight());

        _fromMenuType = menuBar.getMenuButtonType();

        _menuBar = menuBar;
        if (_menuBar!=null) {
            SMView layer = _director.getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI);
            if (layer!=null) {
                _menuBar.changeParent(layer);

                _prevMenuTitle = _menuBar.getText();
                _prevManuBarButton = _menuBar.getButtonTypes();

                _menuBar.setTextTransitionType(MenuBar.TextTransition.FADE);
                switch (getSwipeType()) {
                    case MENU:
                    {
                        _toMenuType = MenuBar.MenuType.MENU;
                    }
                    break;
                    case DISMISS:
                    {
                        _toMenuType = MenuBar.MenuType.CLOSE;
                    }
                    break;
                    default:
                    {
                        _toMenuType = MenuBar.MenuType.BACK;
                    }
                    break;
                }

                _menuBar.setMenuButtonType(_toMenuType, false);
                _menuBar.setButtonTransitionType(MenuBar.ButtonTransition.FADE);
                _menuBar.setMenuBarListener(null);

                return true;
            }
        }

        return false;
    }

    public void setMenuBarTitle(final String title) {
        _menuTitle = title;
    }

    public void setMenuBarButton(final MenuBar.MenuType button1) {
        setMenuBarButton(button1, MenuBar.MenuType.NONE);
    }
    public void setMenuBarButton(final MenuBar.MenuType button1, final MenuBar.MenuType button2) {
        _menuBarButton[0] = button1;
        _menuBarButton[1] = button2;
    }

    @Override
    public void onTransitionStart(final Transition type, final int tag) {
        if (type==Transition.IN) {
            if (_menuBar!=null) {
                _menuBar.setText(_menuTitle, false);
                _menuBar.setTwoButton(_menuBarButton[0], _menuBarButton[1], false);
            }
        }

        if (type==Transition.OUT || type==Transition.SWIPE_OUT) {
            if (_menuBar==null) {
                return;
            }

                SMView layer = _director.getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI);
            if (layer==null) return;
            _menuBar.changeParent(layer);

            if (type==Transition.OUT) {
                _menuBar.setTextTransitionType(MenuBar.TextTransition.FADE);
                _menuBar.setText(_prevMenuTitle, false);
                _menuBar.setMenuButtonType(_fromMenuType, false, false);
                _menuBar.setButtonTransitionType(MenuBar.ButtonTransition.FADE);

                int numButtons = (int)_prevManuBarButton.size();
                if (numButtons==1) {
                    _menuBar.setOneButton((MenuBar.MenuType) _prevManuBarButton.get(0), false, false);
                } else if (numButtons==2) {
                    _menuBar.setTwoButton((MenuBar.MenuType)_prevManuBarButton.get(0), (MenuBar.MenuType)_prevManuBarButton.get(1), false, false);
                } else {
                    _menuBar.setOneButton(MenuBar.MenuType.NONE, false, false);
                }
            } else {
                _menuBar.setTextTransitionType(MenuBar.TextTransition.SWIPE);
                _menuBar.setText(_prevMenuTitle, false);
                _menuBar.setMenuButtonType(_fromMenuType, false, true);
                _menuBar.setButtonTransitionType(MenuBar.ButtonTransition.FADE);

                int numButtons = (int)_prevManuBarButton.size();
                if (numButtons==1) {
                    _menuBar.setOneButton((MenuBar.MenuType)_prevManuBarButton.get(0), true, true);
                } else if (numButtons==2) {
                    _menuBar.setTwoButton((MenuBar.MenuType)_prevManuBarButton.get(0), (MenuBar.MenuType)_prevManuBarButton.get(1), true, true);
                } else {
                    _menuBar.setOneButton(MenuBar.MenuType.NONE, true, true);
                }

                _menuBar.onSwipeStart();
            }
        }

    }

    @Override
    public void onTransitionProgress(final Transition type, final int tag, final float progress) {
        if (type==Transition.SWIPE_OUT) {
            if (_menuBar!=null) {
                _menuBar.onSwipeUpdate(progress);
            }
            _swipeStarted = true;
        }
    }

    @Override
    public void onTransitionComplete(final Transition type, final int tag) {
        boolean menuBarReturn = false;

        if (_swipeStarted && _menuBar!=null) {
            if (type==Transition.SWIPE_OUT) {
                _menuBar.onSwipeComplete();
            } else if (type==Transition.RESUME) {
                _menuBar.onSwipeCancel();
                menuBarReturn = true;
            }
        }

        _swipeStarted = false;

        if (type==Transition.IN || menuBarReturn) {
            SMView layer = _director.getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI);
            if (layer==null) return;;

            ArrayList<SMView> children = layer.getChildren();
            for (SMView child : children) {
                if (child==_menuBar && _menuBar!=null) {
                    child.changeParent(this);
                    _menuBar.setMenuBarListener(_menuBarListener);
                    break;
                }
            }
        }
    }

    @Override
    public void onTransitionReplaceSceneDidFinish() {
        SMView layer = _director.getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI);
        if (layer==null) return;

        ArrayList<SMView> children = layer.getChildren();
        for (SMView child : children) {
            if (child==_menuBar) {
                _menuBar.changeParent(this);
                break;
            }
        }

        _menuBar.setMenuBarListener(_menuBarListener);
    }
}
