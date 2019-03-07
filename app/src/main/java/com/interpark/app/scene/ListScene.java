package com.interpark.app.scene;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMMenuTransitionScene;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMTableView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.transition.SlideInToTop;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRoundLine;

import java.util.ArrayList;

public class ListScene extends SMMenuTransitionScene implements  SMTableView.CellForRowAtIndexPath, SMTableView.NumberOfRowsInSection, SMView.OnClickListener {
        protected ListScene _mainScene = null;
        public ListScene(IDirector director) {
            super(director);
        }

        private SMTableView _tableView = null;
        private SMView _contentView = null;
        private ArrayList<String> _menuNames = new ArrayList<>();
        private int _sceneType = 0;


        public static ListScene create(IDirector director, MenuBar menuBar) {
            return create(director, menuBar, null);
        }

        public static ListScene create(IDirector director, MenuBar menuBar, SceneParams params) {
            ListScene scene = new ListScene(director);

            scene.initWithParams(menuBar, params);

            return scene;
        }


        protected boolean initWithParams(MenuBar menuBar, SceneParams params) {
            super.initWithMenuBar(menuBar);
            _sceneParam = params;
            _mainScene = this;

            getRootView().setBackgroundColor(Color4F.XEEEFF1);



            Size s = getDirector().getWinSize();

            _contentView = SMView.create(getDirector(), 0, 0, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT);
            addChild(_contentView);

            if (_sceneParam!=null) {
                _sceneType = _sceneParam.getInt("SCENE_TYPE");
            }
            if (_sceneType==0) {
                setMenuBarTitle("Shapes.");
                _menuNames.add("DOT");
                _menuNames.add("LINE");
                _menuNames.add("RECT");
                _menuNames.add("ROUNDEDRECT");
                _menuNames.add("CIRCLE");
                _menuNames.add("SOLID-RECT");
                _menuNames.add("SOLID-ROUNDEDRECT");
                _menuNames.add("SOLID_CIRCLE");
                _menuNames.add("SOLID_TRIANGLE");
            } else if (_sceneType==1) {
                setMenuBarTitle("Views.");
                _menuNames.add("IMAGE VIEW");
                _menuNames.add("ZOOM VIEW");
                _menuNames.add("TABLE VIEW");
                _menuNames.add("PAGE VIEW");
                _menuNames.add("CIRCULAR VIEW");
                _menuNames.add("KENBURN");
                _menuNames.add("WAVE & PULSE");
                _menuNames.add("STENCIL VIEW");
                _menuNames.add("STICKER VIEW");
                _menuNames.add("SWIPE VIEW");
            } else if (_sceneType==2) {
                setMenuBarTitle("Controls.");
                _menuNames.add("LABEL");
                _menuNames.add("BUTTON");
                _menuNames.add("SLID BUTTON");
                _menuNames.add("SLIDER");
                _menuNames.add("PROGRESS");
                _menuNames.add("LOADING");
            }


            _tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, 1, 0, 0, s.width, _contentView.getContentSize().height);
            _tableView.cellForRowAtIndexPath = this;
            _tableView.numberOfRowsInSection = this;

            _tableView.setScissorEnable(true);

            _contentView.addChild(_tableView);
            _contentView.setLocalZOrder(-10);
            return true;
        }

        @Override
        public int numberOfRowsInSection(int section) {
            return _menuNames.size();
        }

        @Override
        public SMView cellForRowAtIndexPath(IndexPath indexPath) {
            int index = indexPath.getIndex();
            String cellID = "CELL" + index;
            Size s = _tableView.getContentSize();
            SMView cell = _tableView.dequeueReusableCellWithIdentifier(cellID);
            if (cell==null) {
                cell = SMView.create(getDirector(), 0, 0, 0, s.width, 200);
                cell.setBackgroundColor(Color4F.WHITE);

                String str = _menuNames.get(index);
                SMLabel title = SMLabel.create(getDirector(), str, 55, MakeColor4F(0x222222, 1.0f));
                title.setAnchorPoint(Vec2.MIDDLE);
                title.setPosition(new Vec2(s.width/2, cell.getContentSize().height/2));
                title.setAlpha(0.6f);
                cell.addChild(title);

                SMRoundLine line = SMRoundLine.create(getDirector());
                line.setBackgroundColor(MakeColor4F(0xdbdcdf, 1.0f));
                line.setLineWidth(2);
                line.line(20, 198, s.width-20, 198);
                line.setLengthScale(1);
                cell.addChild(line);

                cell.setTag(index);
                cell.setOnClickListener(this);

                cell.setOnStateChangeListener(new OnStateChangeListener() {
                    @Override
                    public void onStateChange(SMView view, STATE state) {
                        if (state==STATE.PRESSED) {
                            view.setBackgroundColor(MakeColor4F(0xeeeff1, 1), 0.15f);
                        } else {
                            view.setBackgroundColor(MakeColor4F(0xffffff, 1), 0.15f);
                        }
                    }
                });
            }
            return cell;
        }

        @Override
        public void onClick(SMView view) {
            int tag = view.getTag();
            SceneParams params = new SceneParams();

            SMScene scene = null;
            if (_sceneType==0) {
                params.putInt("SHAPE_TYPE", tag);
                scene = ShapeDisplayScene.create(getDirector(), _menuBar, params);
            } else if (_sceneType==1) {
                params.putInt("VIEW_TYPE", tag);
            } else if (_sceneType==2) {
                params.putInt("CONTROL_TYPE", tag);
                _menuBar.showToast("Not Yet.", Color4F.TOAST_RED, 2.0f);
            }

            if (scene!=null) {
                SlideInToTop top = SlideInToTop.create(getDirector(), 0.3f, scene);
                getDirector().pushScene(top);
            }
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

            }
            return false;
        }

    }

