package com.interpark.app.scene;

import android.graphics.Paint;
import android.util.Log;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.ICircularCell;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.scroller.SMScroller;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.types.Color4B;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.base.types.Mat4;
import com.interpark.smframework.base.types.TransformAction;
import com.interpark.smframework.util.Rect;
import com.interpark.smframework.view.RingWave;
import com.interpark.smframework.view.RingWave2;
import com.interpark.smframework.view.SMCircleView;
import com.interpark.smframework.view.SMCircularListView;
import com.interpark.smframework.view.SMKenBurnsView;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMPageView;
import com.interpark.smframework.view.SMSolidCircleView;
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

    private SMView _contentView = null;

    public static ViewDisplayScene create(IDirector director, MenuBar menuBar) {
        return create(director, menuBar, null);
    }
    public static ViewDisplayScene create(IDirector director, MenuBar menuBar, SceneParams params) {
        ViewDisplayScene scene = new ViewDisplayScene(director);

        scene.initWithParams(menuBar, params);

        return scene;
    }

    @Override
    protected boolean onMenuBarClick(SMView view) {
        MenuBar.MenuType type = MenuBar.intToMenuType(view.getTag());
        switch (type) {
            case BACK:
            {
                finishScene();
                return true;
            }
            case CLOSE:
            {
                finishScene();
                return true;
            }
        }
        return false;
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
                circularViewDisplay();
            }
            break;
            case 4:
            {
                // Table View
                tableViewDisplay();
            }
            break;
            case 5:
            {
                // Kenburn
                kenburnDisplay();
            }
            break;
            case 6:
            {
                // Wave & Pulse
                ringWaveDisplay();
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

    private RingWave2 _ringView = null;
    private SMSolidCircleView _alarmCircle = null;
    private boolean _ringFlag = false;
    private Rect _buttunRect = new Rect();
    private void ringWaveDisplay() {
        Size s = _contentView.getContentSize();


        _buttunRect.setRect(40, s.height-AppConst.SIZE.MENUBAR_HEIGHT+20, s.width-80, AppConst.SIZE.MENUBAR_HEIGHT-40);

        SMButton btn = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_ROUNDRECT, _buttunRect.origin.x, _buttunRect.origin.y, _buttunRect.size.width, _buttunRect.size.height);
        btn.setShapeCornerRadius((AppConst.SIZE.MENUBAR_HEIGHT-40)/2);
        btn.setOutlieWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2);
        btn.setButtonColor(STATE.NORMAL, Color4F.WHITE);
        btn.setButtonColor(STATE.PRESSED, Color4F.XEEEFF1);
        btn.setOutlineColor(STATE.NORMAL, Color4F.XDBDCDF);
        btn.setOutlineColor(STATE.PRESSED, Color4F.XADAFB3);
        btn.setText("RING FULSE", 55);
        btn.setTextColor(STATE.NORMAL, Color4F.TEXT_BLACK);
        btn.setTextColor(STATE.PRESSED, Color4F.XADAFB3);
        _contentView.addChild(btn);

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(SMView view) {
                Size s = _contentView.getContentSize();
                _ringFlag = !_ringFlag;
                if (_ringView!=null) {
                    _contentView.removeChild(_ringView);
                    _ringView = null;
                }
                if (_alarmCircle!=null) {
                    _contentView.removeChild(_alarmCircle);
                    _alarmCircle = null;
                }



                if (_ringFlag) {

                    _ringView = RingWave2.create(getDirector(), 80, 100);
                    _ringView.setAnchorPoint(Vec2.MIDDLE);
                    _ringView.setPosition(s.width/2, s.height/2-AppConst.SIZE.MENUBAR_HEIGHT/2);
                    _ringView.setColor(new Color4F(SMView.getRandomColorF(), SMView.getRandomColorF(), SMView.getRandomColorF(), 1));
                    _contentView.addChild(_ringView);

                    Rect src = new Rect(_buttunRect);
                    Rect dst = new Rect(0, s.height-AppConst.SIZE.MENUBAR_HEIGHT-40, s.width, AppConst.SIZE.MENUBAR_HEIGHT);
                    ViewTransitionAction action = ViewTransitionActionCreate(getDirector(), view);
                    action.setValue(src, dst, 0.3f, 0.1f);
                    view.runAction(action);
                } else {

                    Color4F pulseColor = new Color4F(SMView.getRandomColorF(), SMView.getRandomColorF(), SMView.getRandomColorF(), 1);
                    _alarmCircle = SMSolidCircleView.create(getDirector());

                    _alarmCircle.setContentSize(new Size(70, 70));
                    _alarmCircle.setColor(pulseColor);
                    _alarmCircle.setAnchorPoint(Vec2.MIDDLE);
                    _alarmCircle.setPosition(s.width / 2, s.height / 2 - AppConst.SIZE.MENUBAR_HEIGHT / 2);
                    _contentView.addChild(_alarmCircle);

                    _alarmCircle.setAlpha(0);
                    _alarmCircle.stopAllActions();
                    TransformAction a = TransformAction.create(getDirector());
                    a.toAlpha(1).setTimeValue(0.2f, 0);
                    _alarmCircle.runAction(a);
                    Size size = _alarmCircle.getContentSize();
                    RingWave.show(getDirector(), _alarmCircle, size.width / 2, size.height / 2, 350, 0.6f, 0.1f, pulseColor, true);

                    Rect src = new Rect(0, s.height-AppConst.SIZE.MENUBAR_HEIGHT-40, s.width, AppConst.SIZE.MENUBAR_HEIGHT);
                    Rect dst = new Rect(_buttunRect);
                    ViewTransitionAction action = ViewTransitionActionCreate(getDirector(), view);
                    action.setValue(src, dst, 0.3f, 0.1f);
                    view.runAction(action);
                }
            }
        });
    }

    private void kenburnDisplay() {
        Size s = _contentView.getContentSize();

        ArrayList<String> imageList = new ArrayList<>();
        imageList.add("images/ken1.jpg");
        imageList.add("images/ken2.jpg");
        imageList.add("images/ken3.jpg");

        SMKenBurnsView view = SMKenBurnsView.createWithAssets(getDirector(), imageList);
        view.setContentSize(s);
        view.setBackgroundColor(Color4F.BLACK);
        view.startWithDelay(0.0f);
        _contentView.addChild(view);
    }

    private SMPageView _tableContainView = null;
    private ArrayList<SMView> _tableBgViews = null;
    private SMTableView _tableView1, _tableView2, _tableView3, _tableView4, _tableView5;
    private void tableViewDisplay() {
        Size s = _contentView.getContentSize();

        _tableBgViews = new ArrayList<>();
        for (int i=0; i<5; i++) {
            SMView bgView = SMView.create(getDirector(), 0, 0, 0, s.width, s.height);
            bgView.setBackgroundColor(SMView.getRandomColor4F());
            _tableBgViews.add(bgView);

            SMTableView tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, i+1, 0, 0, s.width, s.height);
            bgView.addChild(tableView);

            tableView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
                @Override
                public int numberOfRowsInSection(int section) {
                    return 100;
                }
            };
            switch (i) {
                case 0:
                {
                    _tableView1 = tableView;
                    tableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
                        @Override
                        public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                            Size s = _contentView.getContentSize();
                            String cellID = "CELL" + indexPath.getIndex();
                            SMView cell = _tableView1.dequeueReusableCellWithIdentifier(cellID);
                            if (cell==null) {
                                int height = SMView.randomInt(50, 300);
                                cell = SMView.create(getDirector(), 0, 0, 0, s.width, height);
                                float r = SMView.getRandomColorF();
                                float g = SMView.getRandomColorF();
                                float b = SMView.getRandomColorF();
                                cell.setBackgroundColor(new Color4F(r, g, b, 1));

                                Color4F text = new Color4F(Math.abs(1-r), Math.abs(1-r), Math.abs(1-b), 1);
                                SMLabel label = SMLabel.create(getDirector(), cellID, 35, text);
                                label.setAnchorPoint(Vec2.MIDDLE);
                                label.setPosition(cell.getContentSize().width/2, cell.getContentSize().height/2);
                                cell.addChild(label);

                            }
                            return cell;
                        }
                    };

                }
                break;
                case 1:
                {
                    _tableView2 = tableView;
                    tableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
                        @Override
                        public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                            Size s = _contentView.getContentSize();
                            String cellID = "CELL" + indexPath.getIndex() ;
                            SMView cell = _tableView2.dequeueReusableCellWithIdentifier(cellID);
                            if (cell==null) {
                                int height = SMView.randomInt(50, 300);
                                cell = SMView.create(getDirector(), 0, 0, 0, s.width/2, height);
                                float r = SMView.getRandomColorF();
                                float g = SMView.getRandomColorF();
                                float b = SMView.getRandomColorF();
                                cell.setBackgroundColor(new Color4F(r, g, b, 1));

                                Color4F text = new Color4F(Math.abs(1-r), Math.abs(1-r), Math.abs(1-b), 1);
                                SMLabel label = SMLabel.create(getDirector(), cellID, 35, text);
                                label.setAnchorPoint(Vec2.MIDDLE);
                                label.setPosition(cell.getContentSize().width/2, cell.getContentSize().height/2);
                                cell.addChild(label);

                            }

                            return cell;
                        }
                    };

                }
                break;
                case 2:
                {
                    _tableView3 = tableView;
                    tableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
                        @Override
                        public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                            Size s = _contentView.getContentSize();
                            String cellID = "CELL" + indexPath.getIndex() ;
                            SMView cell = _tableView3.dequeueReusableCellWithIdentifier(cellID);
                            if (cell==null) {
                                int height = SMView.randomInt(50, 300);
                                cell = SMView.create(getDirector(), 0, 0, 0, s.width/3, height);
                                float r = SMView.getRandomColorF();
                                float g = SMView.getRandomColorF();
                                float b = SMView.getRandomColorF();
                                cell.setBackgroundColor(new Color4F(r, g, b, 1));

                                Color4F text = new Color4F(Math.abs(1-r), Math.abs(1-r), Math.abs(1-b), 1);
                                SMLabel label = SMLabel.create(getDirector(), cellID, 35, text);
                                label.setAnchorPoint(Vec2.MIDDLE);
                                label.setPosition(cell.getContentSize().width/2, cell.getContentSize().height/2);
                                cell.addChild(label);

                            }
                            return cell;
                        }
                    };
                }
                break;
                case 3:
                {
                    _tableView4 = tableView;
                    tableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
                        @Override
                        public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                            Size s = _contentView.getContentSize();
                            String cellID = "CELL" + indexPath.getIndex() ;
                            SMView cell = _tableView4.dequeueReusableCellWithIdentifier(cellID);
                            if (cell==null) {
                                int height = SMView.randomInt(50, 300);
                                cell = SMView.create(getDirector(), 0, 0, 0, s.width/4, height);
                                float r = SMView.getRandomColorF();
                                float g = SMView.getRandomColorF();
                                float b = SMView.getRandomColorF();
                                cell.setBackgroundColor(new Color4F(r, g, b, 1));

                                Color4F text = new Color4F(Math.abs(1-r), Math.abs(1-r), Math.abs(1-b), 1);
                                SMLabel label = SMLabel.create(getDirector(), cellID, 35, text);
                                label.setAnchorPoint(Vec2.MIDDLE);
                                label.setPosition(cell.getContentSize().width/2, cell.getContentSize().height/2);
                                cell.addChild(label);

                            }
                            return cell;
                        }
                    };
                }
                break;
                case 4:
                {
                    _tableView5 = tableView;
                    tableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
                        @Override
                        public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                            Size s = _contentView.getContentSize();
                            String cellID = "CELL" + indexPath.getIndex() ;
                            SMView cell = _tableView5.dequeueReusableCellWithIdentifier(cellID);
                            if (cell==null) {
                                int height = SMView.randomInt(50, 300);
                                cell = SMView.create(getDirector(), 0, 0, 0, s.width/5, height);
                                float r = SMView.getRandomColorF();
                                float g = SMView.getRandomColorF();
                                float b = SMView.getRandomColorF();
                                cell.setBackgroundColor(new Color4F(r, g, b, 1));

                                Color4F text = new Color4F(Math.abs(1-r), Math.abs(1-r), Math.abs(1-b), 1);
                                SMLabel label = SMLabel.create(getDirector(), cellID, 35, text);
                                label.setAnchorPoint(Vec2.MIDDLE);
                                label.setPosition(cell.getContentSize().width/2, cell.getContentSize().height/2);
                                cell.addChild(label);
                            }
                            return cell;
                        }
                    };
                }
                break;
            }
        }

        _tableContainView = SMPageView.create(getDirector(), SMTableView.Orientation.HORIZONTAL, 0, 0, s.width, s.height);
        _tableContainView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
            @Override
            public int numberOfRowsInSection(int section) {
                return _tableBgViews.size();
            }
        };
        _tableContainView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
            @Override
            public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                return _tableBgViews.get(indexPath.getIndex());
            }
        };
        _tableContainView.setScissorEnable(true);
        _tableContainView.setOnPageChangedCallback(this);
        _contentView.addChild(_tableContainView);

        layoutTableViewLabel();
    }

    private SMLabel _tableViewLabel = null;
    private void layoutTableViewLabel() {
        if (_tableViewLabel==null) {
            _tableViewLabel = SMLabel.create(getDirector(), "", 60, Color4F.WHITE);
            _tableViewLabel.setAnchorPoint(Vec2.MIDDLE);
            _tableViewLabel.setPosition(_contentView.getContentSize().width/2, _contentView.getContentSize().height - 200);
            _tableViewLabel.setLocalZOrder(999);
            _contentView.addChild(_tableViewLabel);
        }

        int pageNo = _tableContainView.getCurrentPage()+1;
        String desc  = "TableView " + pageNo + " / " + _tableBgViews.size() + " page";
        _tableViewLabel.setText(desc);
    }

    private CircularImageCell CircularImageCellCreate(IDirector director, String assetName) {
        CircularImageCell cell = new CircularImageCell(director, assetName);
        if (cell!=null) {
            if (cell.getContentSize().width==0 && cell.getContentSize().height==0) {
                cell.setContentSize(cell.getSprite().getWidth(), cell.getSprite().getHeight());
            }
        }
        return cell;
    }
    private class CircularImageCell extends SMImageView implements ICircularCell {
        public CircularImageCell(IDirector director, String assetName) {
            super(director, assetName);
        }

        @Override
        public int getCellIndex() {
            return _index;
        }
        @Override
        public float getCellPosition() {
            return _position;
        }
        @Override
        public String getCellIdentifier() {
            return _reuseIdentifier;
        }
        @Override
        public void markDelete() {
            _deleted = true;
        }
        @Override
        public void setCellIndex(int index) {
            _index = index;
        }
        @Override
        public void setCellPosition(final float position) {
            _position = position;
        }
        @Override
        public void setReuseIdentifier(final String identifier) {
            _reuseIdentifier = identifier;
        }

        @Override
        public void setAniSrc(float src) {
            _aniSrc = src;
        }
        @Override
        public void setAndDst(float dst) {
            _aniDst = dst;
        }
        @Override
        public void setAniIndex(int index) {
            _aniIndex = index;
        }

        @Override
        public boolean isDeleted() {return _deleted;}
        @Override
        public float getAniSrc() {return _aniSrc;}
        @Override
        public float getAniDst() {return _aniDst;}
        @Override
        public int getAniIndex() {return _aniIndex;}

        public int _index=0;
        public boolean _deleted = false;
        public float _position = 0.0f;
        public float _aniSrc=0, _aniDst=0;
        public int _aniIndex=0;
        public String _reuseIdentifier="";
    }

    private final float PAGER_PADDING = 200.0f;
    private SMCircularListView.Config _circularConfig = null;
    private SMCircularListView _circularListview = null;
    private SMCircularListView.Orientation _circularOrientation = SMCircularListView.Orientation.HORIZONTAL;
    private SMScroller.ScrollMode _circularScrollMode = SMScroller.ScrollMode.PAGER;
    private boolean _isCircular = false;
    private void circularViewDisplay() {
        Size s = _contentView.getContentSize();

        //float pageSize = s.height - AppConst.SIZE.MENUBAR_HEIGHT*3;
        float pageSize = s.height;
        Size listViewSize = new Size(s.width+PAGER_PADDING*2, pageSize);

        for (int i=0; i<5; i++) {
            CircularImageCell cell = CircularImageCellCreate(getDirector(), "images/defaults.jpg");
            cell.setContentSize(new Size(s.width, pageSize));
            _circularImages.add(cell);
        }

        _circularOrientation = SMCircularListView.Orientation.HORIZONTAL;
        _circularScrollMode = SMScroller.ScrollMode.PAGER;
        _isCircular = true;

        _circularConfig = new SMCircularListView.Config();
        _circularConfig.orient = _circularOrientation;
        _circularConfig.scrollMode = _circularScrollMode;
        _circularConfig.circular = _isCircular;
        _circularConfig.cellSize = s.width;
        _circularConfig.windowSize = s.width + PAGER_PADDING*2;
        _circularConfig.anchorPosition = PAGER_PADDING;
        _circularConfig.maxVelocity = 5000;
        _circularConfig.minVelocity = 5000;
        _circularConfig.preloadPadding = 0;

        _circularListview = SMCircularListView.create(getDirector(), _circularConfig);
        _contentView.addChild(_circularListview);
        _circularListview.setContentSize(listViewSize);
        _circularListview.setPositionX(-PAGER_PADDING);
        _circularListview.cellForRowsAtIndex = new SMCircularListView.CellForRowsAtIndex() {
            @Override
            public SMView cellForRowsAtIndex(int index) {
                CircularImageCell cell = _circularImages.get(index);

                cell.setTag(index);

                return cell;
            }
        };
        _circularListview.numberOfRows = new SMCircularListView.NumberOfRows() {
            @Override
            public int numberOfRows() {
                return _circularImages.size();
            }
        };
        _circularListview.positionCell = new SMCircularListView.PositionCell() {
            @Override
            public void positionCell(SMView cell, float position, boolean created) {
                cell.setPositionX(position);
            }
        };
//        _circularListview.initFillWithCells = new SMCircularListView.InitFillWithCells() {
//            @Override
//            public void initFillWithCells() {
//                if (_circularListview!=null) {
////                    SMImageView
//                }
//            }
//        };
//        _circularListview.scrollAlignedCallback = null;
        _circularListview.pageScrollCallback = new SMCircularListView.PageScrollCallback() {
            @Override
            public void pageScrollCallback(float pagePosition) {
                layoutCircularLabel(pagePosition);
            }
        };

    }

    private ArrayList<CircularImageCell> _circularImages = new ArrayList<>();
    private SMLabel _circularLabel = null;
    private void layoutCircularLabel(float pagePosition) {
        if (_circularLabel==null) {
            _circularLabel = SMLabel.create(getDirector(), "", 45, new Color4F(1, 0, 0, 1));
            _circularLabel.setAnchorPoint(Vec2.MIDDLE);
            _circularLabel.setPosition(_contentView.getContentSize().width/2, _circularListview.getContentSize().height-150);
            _circularLabel.setLocalZOrder(999);
            _contentView.addChild(_circularLabel);
        }

        int pageNo = (int)(Math.floor(pagePosition+0.5f) % _circularImages.size());
        String desc = "Circular Paging " + (pageNo+1) + "/" + _circularImages.size() + " page";
        _circularLabel.setText(desc);

     }

    private SMPageView _horPageView = null;
    private SMPageView _verPageView = null;
    private SMLabel _horLabel = null;
    private SMLabel _verLabel = null;
    private int _pageItemCount = 10;
    private int _currentHorPage = 0;
    private int _currentVerPage = 0;
    private ArrayList<SMImageView> _horImages = new ArrayList<>();
    private ArrayList<SMImageView> _verImages = new ArrayList<>();
    private void pageViewDisplay() {
        Size s = _contentView.getContentSize();


        for (int i=0; i<_pageItemCount; i++) {
            SMImageView imgH = SMImageView.create(getDirector(), "images/bigsize.jpg");
            imgH.setContentSize(s.width, s.height/2);
            imgH.setScaleType(SMImageView.ScaleType.FIT_CENTER);
            imgH.setScissorEnable(true);
            imgH.setBackgroundColor(1, 1, 0, 0.6f);
            imgH.setTag(i);
            _horImages.add(imgH);

            SMImageView imgV = SMImageView.create(getDirector(), "images/bigsize.jpg");
            imgV.setContentSize(s.width, s.height/2);
            imgV.setScaleType(SMImageView.ScaleType.FIT_CENTER);
            imgV.setBackgroundColor(0, 1, 1, 0.6f);
            imgV.setTag(i);
            _verImages.add(imgV);
        }

        _horPageView = SMPageView.create(getDirector(), SMTableView.Orientation.HORIZONTAL, 0, 0, s.width, s.height/2);
        _horPageView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
            @Override
            public int numberOfRowsInSection(int section) {
                return _horImages.size();
            }
        };
        _horPageView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
            @Override
            public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                return _horImages.get(indexPath.getIndex());
            }
        };
        _horPageView.setScissorEnable(true);
        _horPageView.setOnPageChangedCallback(this);
        _contentView.addChild(_horPageView);



        _verPageView = SMPageView.create(getDirector(), SMTableView.Orientation.VERTICAL, 0, s.height/2, s.width, s.height/2);
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
        _verPageView.setOnPageChangedCallback(this);
        _contentView.addChild(_verPageView);



        layoutPageLabel();
    }

    private void layoutPageLabel() {
        if (_horLabel==null) {
            _horLabel = SMLabel.create(getDirector(), "", 55, new Color4F(1, 0, 0, 1));
            _horLabel.setAnchorPoint(Vec2.MIDDLE);
            _horLabel.setPosition(_contentView.getContentSize().width/2, _contentView.getContentSize().height/2 - 150);
            _horLabel.setLocalZOrder(999);
            _contentView.addChild(_horLabel);
        }

        if (_verLabel==null) {
            _verLabel = SMLabel.create(getDirector(), "", 55, new Color4F(1, 0, 0, 1));
            _verLabel.setAnchorPoint(Vec2.MIDDLE);
            _verLabel.setPosition(_contentView.getContentSize().width/2, _contentView.getContentSize().height - 150);
            _verLabel.setLocalZOrder(999);
            _contentView.addChild(_verLabel);
        }

        String horString = "Horizontal Paging " + (_horPageView.getCurrentPage()+1) + "/" + _horImages.size() + " page";
        _horLabel.setText(horString);

        String verString = "Vertical Paging " + (_verPageView.getCurrentPage()+1) + "/" + _verImages.size() + " page";
        _verLabel.setText(verString);
    }

    @Override
    public void onPageChangedCallback(SMPageView pageView, int page) {

        if (_viewType==2) { // page view
        layoutPageLabel();
        } else if (_viewType==4) {  // circular page view
            layoutTableViewLabel();
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
        ArrayList<SMButton> scaleBtns = new ArrayList<>();
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
        ArrayList<SMButton> gravityBtns = new ArrayList<>();
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
