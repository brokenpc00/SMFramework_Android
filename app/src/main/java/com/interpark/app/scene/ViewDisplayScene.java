package com.interpark.app.scene;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.view.SMPageView;
import com.interpark.smframework.view.SMTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.view.SMZoomView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.shader.ShaderNode;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMButton;
import com.interpark.smframework.view.SMImageView;

import java.util.ArrayList;

public class ViewDisplayScene extends SMMenuTransitionScene implements SMView.OnClickListener, SMPageView.OnPageChangedCallback {
    public ViewDisplayScene _mainScene = null;

    public ViewDisplayScene(IDirector director) {
        super(director);
    }

    private SMTableView _tableView = null;
    private SMView _contentView = null;

    public static ViewDisplayScene create(IDirector director, MenuBar menuBar) {
        return create(director, menuBar, null);
    }
    public static ViewDisplayScene create(IDirector director, MenuBar menuBar, SceneParams params) {
        ViewDisplayScene scene = new ViewDisplayScene(director);

        scene.initWithParams(menuBar, params);

        return scene;
    }

    protected boolean initWithParams(MenuBar menuBar, SceneParams params) {
        super.initWithMenuBar(menuBar, SwipeType.DISMISS);
        _sceneParam = params;
        _mainScene = this;
        getRootView().setBackgroundColor(Color4F.XEEEFF1);
        setMenuBarTitle(_sceneParam.getString("MENU_NAME"));

        Size s = getDirector().getWinSize();
        _contentView = SMView.create(getDirector(), 0, 0, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT);
        _contentView.setBackgroundColor(Color4F.WHITE);
        addChild(_contentView);

        makeView();
        return true;
    }

    private boolean _imageButtonGravityType = false;
    private SMImageView _mainImageView = null;
    private int _viewType = 0;
    public void makeView() {
        _viewType = _sceneParam.getInt("VIEW_TYPE");

        switch (_viewType) {
            case 0:
            {
                // Image View
                imageDisplay();
            }
            break;
            case 1:
            {
                // Zoom View
                zoomDisplay();
            }
            break;
            case 2:
            {
                // Page View
                pageViewDisplay();
            }
            break;
            case 3:
            {
                // Circular View
            }
            break;
            case 4:
            {
                // Table View
            }
            break;
            case 5:
            {
                // Kenburn
            }
            break;
            case 6:
            {
                // Wave & Pulse
            }
            break;
            case 7:
            {
                // Stencil View
            }
            break;
            case 8:
            {
                // Sticker View
            }
            break;
            case 9:
            default:
            {
                // Swipe View
            }
            break;
        }
    }

    private SMPageView _horPageView = null;
    private SMPageView _verPageView = null;
    private int _pageItemCount = 10;
    private ArrayList<SMImageView> _horImages = new ArrayList<SMImageView>();
    private ArrayList<SMImageView> _verImages = new ArrayList<SMImageView>();
    private void pageViewDisplay() {
        Size s = _contentView.getContentSize();


        for (int i=0; i<_pageItemCount; i++) {
            SMImageView imgH = SMImageView.create(getDirector(), "images/bigsize.jpg");
            imgH.setContentSize(s.width-200, s.height/2);
            imgH.setScaleType(SMImageView.ScaleType.CENTER_INSIDE);
            imgH.setScissorEnable(true);
            imgH.setBackgroundColor(1, 1, 0, 0.6f);
            _horImages.add(imgH);

            SMImageView imgV = SMImageView.create(getDirector(), "images/bigsize.jpg");
            imgV.setContentSize(s.width-200, s.height/2);
            imgV.setScaleType(SMImageView.ScaleType.FIT_CENTER);
            imgV.setScissorEnable(true);
            imgV.setBackgroundColor(1, 0, 1, 0.6f);
            _verImages.add(imgV);
        }

        _horPageView = SMPageView.create(getDirector(), SMTableView.Orientation.HORIZONTAL, 100, 0, s.width-200, s.height/2);
        _horPageView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
            @Override
            public int numberOfRowsInSection(int section) {
                return _pageItemCount;
            }
        };
        _horPageView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
            @Override
            public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                return _horImages.get(indexPath.getIndex());
            }
        };
        _horPageView.setScissorEnable(true);
        _contentView.addChild(_horPageView);



        _verPageView = SMPageView.create(getDirector(), SMTableView.Orientation.VERTICAL, 100, s.height/2, s.width-200, s.height/2);
        _verPageView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
            @Override
            public int numberOfRowsInSection(int section) {
                return _pageItemCount;
            }
        };
        _verPageView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
            @Override
            public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                return _verImages.get(indexPath.getIndex());
            }
        };
        _verPageView.setScissorEnable(true);
        _contentView.addChild(_verPageView);


    }

    @Override
    public void onPageChangedCallback(SMPageView pageView, int page) {
        if (pageView==_horPageView) {

        } else {

        }
    }

    private SMZoomView _zoomView = null;
    private void zoomDisplay() {
        Size s = _contentView.getContentSize();

        _zoomView = SMZoomView.create(getDirector(), 0, 0, s.width, s.height);
        _contentView.addChild(_zoomView);

        SMImageView contentView = SMImageView.create(getDirector(), "images/bigsize.jpg");
        _zoomView.setContentView(contentView);
    }



    private SMButton _scaleButton = null;
    private SMButton _gravityButton = null;
    private SMView _scaleBG = null;
    private SMView _gravitiBG = null;
    private void imageDisplay() {
        Size s = _contentView.getContentSize();


        float fontSize = 35;
        float padding = 30;
        float buttonSize = AppConst.SIZE.MENUBAR_HEIGHT - padding*2;

        float bgHeight = buttonSize * 5;

        _mainImageView = SMImageView.create(getDirector(), "images/defaults2.jpg");
        _mainImageView.setContentSize(new Size(s.width, s.height-bgHeight));
        _mainImageView.setPosition(Vec2.ZERO);
        _mainImageView.setBackgroundColor(new Color4F(1, 0, 0, 0.4f));
        _mainImageView.setScaleType(SMImageView.ScaleType.CENTER);
        _contentView.addChild(_mainImageView);

        SMView menuBg = SMView.create(getDirector(), 0, s.height-bgHeight, s.width, bgHeight);
        menuBg.setBackgroundColor(Color4F.XEEEFF1);
        _contentView.addChild(menuBg);

        _scaleButton = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_RECT, 0, 0, s.width/2, buttonSize);
        _gravityButton = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_RECT, s.width/2, 0, s.width/2, buttonSize);
        menuBg.addChild(_scaleButton);
        menuBg.addChild(_gravityButton);

        _scaleButton.setText("SCALE TYPE", 35);
        _gravityButton.setText("GRAVITY TYPE", 35);

        _scaleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(SMView view) {
                if (_imageButtonGravityType) {
                    _imageButtonGravityType = false;
                    setGravityButtonState();
                }
            }
        });
        _gravityButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(SMView view) {
                if (!_imageButtonGravityType) {
                    _imageButtonGravityType = true;
                    setGravityButtonState();
                }
            }
        });

        _imageButtonGravityType = false;

        _scaleBG = SMView.create(getDirector(), 0, 0, buttonSize, s.width, bgHeight-buttonSize);
        _scaleBG.setBackgroundColor(Color4F.WHITE);
        menuBg.addChild(_scaleBG);
        _gravitiBG = SMView.create(getDirector(), 0, 0, buttonSize, s.width, bgHeight-buttonSize);
        _gravitiBG.setBackgroundColor(Color4F.WHITE);
        menuBg.addChild(_gravitiBG);

        setGravityButtonState();


        float posY = 10;
        float buttonWidth = s.width/2-30;
        ArrayList<SMButton> scaleBtns = new ArrayList<SMButton>();
        SMButton centerButton = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_ROUNDRECT, 15, posY + 5, buttonWidth, buttonSize - 10);
        centerButton.setText("CENTER", fontSize);
        scaleBtns.add(centerButton);

        posY += buttonSize+20;
        SMButton centerInsideButton = SMButton.create(getDirector(), 1, SMButton.STYLE.SOLID_ROUNDRECT, 15, posY + 5, buttonWidth, buttonSize - 10);
        centerInsideButton.setText("CENTER INSIDE", fontSize);
        scaleBtns.add(centerInsideButton);

        posY += buttonSize+20;
        SMButton centerCropButton = SMButton.create(getDirector(), 2, SMButton.STYLE.SOLID_ROUNDRECT, 15, posY + 5, buttonWidth, buttonSize - 10);
        centerCropButton.setText("CENTER CROP", fontSize);
        scaleBtns.add(centerCropButton);

        posY = 10;
        SMButton fitXYButton = SMButton.create(getDirector(), 3, SMButton.STYLE.SOLID_ROUNDRECT, s.width/2 + 15, posY + 5, buttonWidth, buttonSize - 10);
        fitXYButton.setText("FIT XY", fontSize);
        scaleBtns.add(fitXYButton);

        posY += buttonSize+20;
        SMButton fitCenterButton = SMButton.create(getDirector(), 4, SMButton.STYLE.SOLID_ROUNDRECT, s.width/2 + 15, posY + 5, buttonWidth, buttonSize - 10);
        fitCenterButton.setText("FIT CENTER", fontSize);
        scaleBtns.add(fitCenterButton);

        for (int i=0; i<scaleBtns.size(); i++) {
            SMButton btn = scaleBtns.get(i);
            btn.setButtonColor(STATE.NORMAL, Color4F.WHITE);
            btn.setButtonColor(STATE.PRESSED, Color4F.XEEEFF1);

            btn.setOutlineColor(STATE.NORMAL, Color4F.XADAFB3);
            btn.setOutlineColor(STATE.PRESSED, Color4F.WHITE);

            btn.setOutlieWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH);
            btn.setShapeCornerRadius(buttonSize/2);

            btn.setTextColor(STATE.NORMAL, Color4F.XADAFB3);
            btn.setTextColor(STATE.PRESSED, Color4F.WHITE);

            btn.setOnClickListener(this);
            _scaleBG.addChild(btn);
        }



        posY = 10;
        buttonWidth = s.width/3-30;
        ArrayList<SMButton> gravityBtns = new ArrayList<SMButton>();
        SMButton LT = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_ROUNDRECT, 15, posY + 5, buttonWidth, buttonSize - 10);
        LT.setText("LT", fontSize);
        gravityBtns.add(LT);

        posY += buttonSize+20;
        SMButton LC = SMButton.create(getDirector(), 1, SMButton.STYLE.SOLID_ROUNDRECT, 15, posY + 5, buttonWidth, buttonSize - 10);
        LC.setText("LC", fontSize);
        gravityBtns.add(LC);

        posY += buttonSize+20;
        SMButton LB = SMButton.create(getDirector(), 2, SMButton.STYLE.SOLID_ROUNDRECT, 15, posY + 5, buttonWidth, buttonSize - 10);
        LB.setText("LB", fontSize);
        gravityBtns.add(LB);

        posY = 10;
        SMButton CT = SMButton.create(getDirector(), 3, SMButton.STYLE.SOLID_ROUNDRECT, s.width/3 + 15, posY + 5, buttonWidth, buttonSize - 10);
        CT.setText("CT", fontSize);
        gravityBtns.add(CT);

        posY += buttonSize+20;
        SMButton CC = SMButton.create(getDirector(), 4, SMButton.STYLE.SOLID_ROUNDRECT, s.width/3 + 15, posY + 5, buttonWidth, buttonSize - 10);
        CC.setText("CC", fontSize);
        gravityBtns.add(CC);

        posY += buttonSize+20;
        SMButton CB = SMButton.create(getDirector(), 5, SMButton.STYLE.SOLID_ROUNDRECT, s.width/3 + 15, posY + 5, buttonWidth, buttonSize - 10);
        CB.setText("CC", fontSize);
        gravityBtns.add(CB);

        posY = 10;
        SMButton RT = SMButton.create(getDirector(), 6, SMButton.STYLE.SOLID_ROUNDRECT, s.width/3*2 + 15, posY + 5, buttonWidth, buttonSize - 10);
        RT.setText("RT", fontSize);
        gravityBtns.add(RT);

        posY += buttonSize+20;
        SMButton RC = SMButton.create(getDirector(), 7, SMButton.STYLE.SOLID_ROUNDRECT, s.width/3*2 + 15, posY + 5, buttonWidth, buttonSize - 10);
        RC.setText("RC", fontSize);
        gravityBtns.add(RC);

        posY += buttonSize+20;
        SMButton RB = SMButton.create(getDirector(), 8, SMButton.STYLE.SOLID_ROUNDRECT, s.width/3*2 + 15, posY + 5, buttonWidth, buttonSize - 10);
        RB.setText("RB", fontSize);
        gravityBtns.add(RB);

        for (int i=0; i<gravityBtns.size(); i++) {
            SMButton btn = gravityBtns.get(i);
            btn.setButtonColor(STATE.NORMAL, Color4F.WHITE);
            btn.setButtonColor(STATE.PRESSED, Color4F.XEEEFF1);

            btn.setOutlineColor(STATE.NORMAL, Color4F.XADAFB3);
            btn.setOutlineColor(STATE.PRESSED, Color4F.WHITE);

            btn.setOutlieWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH);
            btn.setShapeCornerRadius(buttonSize/2);

            btn.setTextColor(STATE.NORMAL, Color4F.XADAFB3);
            btn.setTextColor(STATE.PRESSED, Color4F.WHITE);

            btn.setOnClickListener(this);
            _gravitiBG.addChild(btn);
        }

    }

    private void setGravityButtonState() {
        if (_scaleButton!=null && _gravityButton!=null) {
            _mainImageView.setScaleType(SMImageView.ScaleType.CENTER);
            _mainImageView.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL | SMImageView.GRAVITY_CENTER_VERTICAL);
            if (_imageButtonGravityType) {
                _gravityButton.setButtonColor(STATE.NORMAL, Color4F.WHITE);
                _gravityButton.setButtonColor(STATE.PRESSED, Color4F.WHITE);
                _gravityButton.setTextColor(STATE.NORMAL, Color4F.TEXT_BLACK);
                _gravityButton.setTextColor(STATE.PRESSED, Color4F.TEXT_BLACK);

                _scaleButton.setButtonColor(STATE.NORMAL, Color4F.XEEEFF1);
                _scaleButton.setButtonColor(STATE.PRESSED, Color4F.WHITE);
                _scaleButton.setTextColor(STATE.NORMAL, Color4F.XADAFB3);
                _scaleButton.setTextColor(STATE.PRESSED, Color4F.XDBDCDF);

                _scaleBG.setVisible(false);
                _gravitiBG.setVisible(true);
            } else {
                _scaleButton.setButtonColor(STATE.NORMAL, Color4F.WHITE);
                _scaleButton.setButtonColor(STATE.PRESSED, Color4F.WHITE);
                _scaleButton.setTextColor(STATE.NORMAL, Color4F.TEXT_BLACK);
                _scaleButton.setTextColor(STATE.PRESSED, Color4F.TEXT_BLACK);

                _gravityButton.setButtonColor(STATE.NORMAL, Color4F.XEEEFF1);
                _gravityButton.setButtonColor(STATE.PRESSED, Color4F.WHITE);
                _gravityButton.setTextColor(STATE.NORMAL, Color4F.XADAFB3);
                _gravityButton.setTextColor(STATE.PRESSED, Color4F.XDBDCDF);

                _scaleBG.setVisible(true);
                _gravitiBG.setVisible(false);
            }
        }
    }

    @Override
    public void onClick(SMView view) {
        switch (_viewType) {
            case 0:
            {
                if (_imageButtonGravityType) {
                    switch (view.getTag()) {
                        case 0:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_LEFT | SMImageView.GRAVITY_TOP);
                        }
                        break;
                        case 1:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_LEFT | SMImageView.GRAVITY_CENTER_VERTICAL);
                        }
                        break;
                        case 2:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_LEFT | SMImageView.GRAVITY_BOTTOM);
                        }
                        break;
                        case 3:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL | SMImageView.GRAVITY_TOP);
                        }
                        break;
                        case 4:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL | SMImageView.GRAVITY_CENTER_VERTICAL);
                        }
                        break;
                        case 5:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL | SMImageView.GRAVITY_BOTTOM);
                        }
                        break;
                        case 6:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_RIGHT | SMImageView.GRAVITY_TOP);
                        }
                        break;
                        case 7:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_RIGHT | SMImageView.GRAVITY_CENTER_VERTICAL);
                        }
                        break;
                        case 8:
                        {
                            _mainImageView.setGravity(SMImageView.GRAVITY_RIGHT | SMImageView.GRAVITY_BOTTOM);
                        }
                        break;
                    }
                } else {
                    switch (view.getTag()) {
                        case 0:
                        {
                            // center
                            _mainImageView.setScaleType(SMImageView.ScaleType.CENTER);
                        }
                        break;
                        case 1:
                        {
                            // center inside
                            _mainImageView.setScaleType(SMImageView.ScaleType.CENTER_INSIDE);
                        }
                        break;
                        case 2:
                        {
                            // center crop
                            _mainImageView.setScaleType(SMImageView.ScaleType.CENTER_CROP);
                        }
                        break;
                        case 3:
                        {
                            // fix xy
                            _mainImageView.setScaleType(SMImageView.ScaleType.FIT_XY);
                        }
                        break;
                        case 4:
                        {
                            // fit center
                            _mainImageView.setScaleType(SMImageView.ScaleType.FIT_CENTER);
                        }
                        break;
                    }                }

            }
            break;
        }
    }

}
