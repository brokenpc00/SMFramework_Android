package com.interpark.app.menu;

import android.graphics.Bitmap;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMCircleView;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMSolidCircleView;

import org.apache.http.cookie.SM;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TopMenu extends SMView {

    public interface TopMenuClickListener {
        void onTopMenuClick(TopMenuComponentType type);
    }
    public TopMenuClickListener _listener = null;

    public TopMenu(IDirector director) {
        super(director);
    }

    public enum TopMenuComponentType {
        MENU,
        TITLE,
        BACK,
        HOME,

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

        SMView underLine = SMView.create(getDirector(), 0, 20, AppConst.SIZE.TOP_MENU_HEIGHT-2, getDirector().getWinSize().width-40, 2);
        underLine.setBackgroundColor(0, 0, 1, 1);
        underLine.setLocalZOrder(90);
        _contentView.addChild(underLine);

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

}
