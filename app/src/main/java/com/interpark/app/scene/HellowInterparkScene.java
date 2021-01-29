package com.interpark.app.scene;

import android.os.PatternMatcher;
import android.util.Log;
import android.util.Pair;

import com.interpark.app.menu.MenuBar;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.NativeImageProcess.ImageProcessing;
import com.interpark.smframework.SideMenu;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.shape.ShapeConstant;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.network.Downloader.AndroidDownloader;
import com.interpark.smframework.network.Downloader.DownloadTask;
import com.interpark.smframework.network.Downloader.Downloader;
import com.interpark.smframework.network.HttpClient.HttpClient;
import com.interpark.smframework.network.HttpClient.HttpRequest;
import com.interpark.smframework.network.HttpClient.HttpResponse;
import com.interpark.smframework.util.FileUtils;
import com.interpark.smframework.util.ImageProcess.ImageProcessor;
import com.interpark.smframework.util.Vec3;
import com.interpark.smframework.view.SMRoundRectView;
import com.interpark.smframework.view.SMTableView;
import com.interpark.smframework.base.transition.SlideInToLeft;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMImageView;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.SMRoundLine;

import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HellowInterparkScene extends SMScene implements SMTableView.CellForRowAtIndexPath, SMTableView.NumberOfRowsInSection, SMView.OnClickListener {

    protected HellowInterparkScene _mainScene = null;

    private SMTableView _tableView = null;

    public HellowInterparkScene(IDirector director) {
        super(director);
    }

    public static HellowInterparkScene create(IDirector director, SceneParams params, SwipeType type) {
        HellowInterparkScene scene = new HellowInterparkScene(director);
        if (scene!=null) {
            scene.initWithSceneParams(params, type);
        }

        return scene;
    }

    public static SMImageView tmpView = null;

    static boolean isBack = false;

    private MenuBar _menuBar = null;
//    private TopMenu _topMenu = null;
    private SMView _contentView = null;
    private ArrayList<String> _menuNames = new ArrayList<>();

    public static String _sceneTitle = "SMFrame Lib.";

    public SMView _uiLayer = null;

    @Override
    protected boolean init() {
        super.init();

        setBackgroundColor(1, 1, 1, 1);

        _mainScene = this;

        _menuBar = MenuBar.create(getDirector());
        _menuBar.setMenuButtonType(MenuBar.MenuType.MENU, true);
        _menuBar.setText(_sceneTitle, true);
        _menuBar.setColorSet(MenuBar.ColorSet.WHITE_TRANSLUCENT, true);
        _menuBar.setLocalZOrder(10);
        _menuBar.setMenuBarListener(_menuBarListener);
        addChild(_menuBar);

        Size s = getContentSize();
        _contentView = SMView.create(getDirector(), 0, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT);
        _contentView.setBackgroundColor(Color4F.WHITE);
        addChild(_contentView);


        _menuNames.add("Shapes.");
        _menuNames.add("Views.");
        _menuNames.add("Controls.");
        _menuNames.add("Etcetera.");

        _tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, 1, 0, 0, s.width, _contentView.getContentSize().height);
        _tableView.cellForRowAtIndexPath = this;
        _tableView.numberOfRowsInSection = this;

        _tableView.setScissorEnable(true);
//        _tableView.setScissorRect(new Rect(200, 200, s.width-400, _tableView.getContentSize().height-400));
//
//        _tableView.setAnchorPoint(Vec2.MIDDLE);
//        _tableView.setPosition(new Vec2(s.width/2, s.height/2+AppConst.SIZE.MENUBAR_HEIGHT/2));
        _contentView.addChild(_tableView);
        _contentView.setLocalZOrder(-10);
//        _contentView.setLocalZOrder(990);



//        imgPath.add("sticker/thumb/001.png");
//        imgPath.add("sticker/thumb/002.png");
//        imgPath.add("sticker/thumb/003.png");
//        imgPath.add("sticker/thumb/004.png");
//        imgPath.add("sticker/thumb/005.png");
//        imgPath.add("sticker/thumb/006.png");
//        imgPath.add("sticker/thumb/007.png");
//        imgPath.add("sticker/thumb/008.png");
//        imgPath.add("sticker/thumb/009.png");
//        imgPath.add("sticker/thumb/010.png");
//        imgPath.add("sticker/thumb/011.png");
//        imgPath.add("sticker/thumb/012.png");
//        imgPath.add("sticker/thumb/013.png");
//        imgPath.add("sticker/thumb/014.png");
//        imgPath.add("sticker/thumb/015.png");
//        imgPath.add("sticker/thumb/016.png");
//        imgPath.add("sticker/thumb/017.png");
//        imgPath.add("sticker/thumb/018.png");
//        imgPath.add("sticker/thumb/019.png");
//        imgPath.add("sticker/thumb/020.png");
//
//        tmpTableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.HORIZONTAL, 1);
//        tmpTableView.setContentSize(new Size(s.width, 400));
//        tmpTableView.setAnchorPoint(Vec2.MIDDLE);
//        tmpTableView.setPosition(s.divide(2));
//        _contentView.addChild(tmpTableView);
//
//        tmpTableView.numberOfRowsInSection = new SMTableView.NumberOfRowsInSection() {
//            @Override
//            public int numberOfRowsInSection(int section) {
//                return imgPath.size();
//            }
//        };
//
//        tmpTableView.cellForRowAtIndexPath = new SMTableView.CellForRowAtIndexPath() {
//            @Override
//            public SMView cellForRowAtIndexPath(IndexPath indexPath) {
//                int index = indexPath.getIndex();
//
//                SMView view = tmpTableView.dequeueReusableCellWithIdentifier("CELL");
//                SMImageView imgView = null;
//                if (view!=null) {
//                    imgView = (SMImageView)view;
//                    BitmapSprite sprite = BitmapSprite.createFromAsset(getDirector(), imgPath.get(index), false, null);
//                    imgView.setSprite(sprite);
//                } else {
//                    imgView = SMImageView.create(getDirector(), imgPath.get(index));
//                }
//
//                return imgView;
//            }
//        };

//        String str = "http://www.interpark.com/index.html?test=false&parse=true";
//        doParse(str);


//        _query = "test=false&parse=true";
////        _query = "http://www.interpark.com/index.html?test=false&parse=true";
//        _queryParams = getQueryParams();

//        downloadTest();

//        ArrayList<Byte> test = new ArrayList<>();
//
//        for (int i=0; i<10; i++) {
////            test[i] = (byte)('a'+i);
//            byte c = (byte)('a'+i);
//            test.add(c);
//        }
//        Log.i("Scene", "[[[[[ test : " + test.toString() + ", size : " + test.size());
//
//        sizeTest(test);
//
//        Log.i("Scene", "[[[[[ test 2 : " + test.toString() + ", size : " + test.size());

//        commTest();

//        SMRoundRectView dashRect = SMRoundRectView.create(getDirector(), 4.0f, ShapeConstant.LineType.Dash, 4);
//        dashRect.setContentSize(new Size(s.width/2, s.height/2));
//        dashRect.setAnchorPoint(Vec2.MIDDLE);
//        dashRect.setLineColor(new Color4F(1, 0, 0, 1));
//        dashRect.setPosition(s.divide(2));
//        _contentView.addChild(dashRect);

//        String imgUrl = "http://openimage.interpark.com/goods_image_big/7/2/5/3/6403007253_l.jpg";
//        SMImageView testImg = SMImageView.create(getDirector(), imgUrl, true);
//        testImg.setContentSize(new Size(s.width/2, s.height/2));
//        testImg.setAnchorPoint(Vec2.MIDDLE);
//        testImg.setPosition(s.divide(2));
//        _contentView.addChild(testImg);


//        float x = 0, y=0, z=0;
//        float[] dst = new float[3];
//        dst[0] = 0;
//        dst[1] = 1;
//        dst[2] = 0;
//
//        float[] m = getDirector().getProjectionMatrix();
//
//        float[] ret = ImageProcessing.transformVec4(m, x, y, z, 1.0f, dst);
//


        return true;
    }

    private SMTableView tmpTableView = null;
    private  ArrayList<String> imgPath = new ArrayList<>();

    public void commTest() {
        final HttpRequest request = new HttpRequest(getDirector());
        String url = "https://www.interpark.com";
        request.setUrl(url);
        request.setRequestType(HttpRequest.Type.GET);
        request.setResponseCallback(new HttpRequest.HttpRequestCallback() {
            @Override
            public void onHttpRequest(HttpClient client, HttpResponse response) {
                byte[] data = response.getResponseData();

//                ArrayList<Byte> data = response.getResponseData();
                String test = new String(data);
//                String test = data.toString();
                Log.i("Scene", "[[[[[ onHttpRequest : " + test);
            }
        });
        request.setTag("GET HTML");
//        HttpClient.getInstance().send(request);
        HttpClient.getInstance().sendImmediate(request);
    }

    public void sizeTest(ArrayList<Byte> test) {

//        test = new byte[20];
        test.clear();
        for (int i=0; i<20; i++) {
            byte c = (byte)('a'+i);
            test.add(c);
        }
    }

    private boolean _isDownloading = false;
    private String _version = "";
    private void downloadTest() {
        _downloader = new Downloader();
        _downloader._onTaskError = new Downloader.OnTaskError() {
            @Override
            public void onTaskError(DownloadTask task, int errorCode, int errorCodeInteral, String errorStr) {
                Log.i("Scene", "[[[[[ download error : " + errorStr);
                _isDownloading = false;


            }
        };

        _downloader._onTaskProgress = new Downloader.OnTaskProgress() {
            @Override
            public void onTaskProgress(DownloadTask task, long bytesReceived, long totalBytesReceived, long totalBytesExpected) {
                Log.i("Scene", "[[[[[ download progress received : " + bytesReceived + ", total : " + totalBytesReceived + ", expected : " + totalBytesExpected);
            }
        };

        _downloader._onDataTaskSuccess = new Downloader.OnDataTaskSuccess() {
            @Override
            public void onDataTaskSuccess(DownloadTask task, byte[] data) {

                StringBuffer str = new StringBuffer();
                str.append(data);

                _version = str.toString();

                Log.i("Scene", "[[[[[ received version : " + _version);
            }
        };

        _downloader._onFileTaskSuccess = new Downloader.OnFileTaskSuccess() {
            @Override
            public void onFileTaskSuccess(DownloadTask task) {
                Log.i("Scene", "[[[[[ onFileTaskSuccess !!!!");
            }
        };


//        String epubUrl = "http://tm.shop.interpark.com/test/ep/test.epub";
        String url = "http://openimage.interpark.com/goods_image_big/3/4/9/6/6415873496_l.jpg";

        String path = FileUtils.getInstance().getWritablePath() + "test.jpg";

        _downloader.createDownloadFileTask(url, path);

    }

//    private static String schemRegex = "([a-zA-Z][a-zA-Z0-9+.-]*):";
//    private static String authoRegex = "([^?#]*)";
//    private static String queryRegex = "(?:\\?([^#]*))?";
//    private static String fragRegex = "(?:#(.*))?";
//
//    private static String pathRegex = "//([^/]*)(/.*)?";
//
//    private boolean doParse(final String str) {
//        if (str.isEmpty()) {
//            return false;
//        }
//
//        Pattern schemP = Pattern.compile(schemRegex);
//        Pattern authP = Pattern.compile(authoRegex);
//        Pattern queryP = Pattern.compile(queryRegex);
//        Pattern fragP = Pattern.compile(fragRegex);
//        Pattern uriP = Pattern.compile(schemRegex+authoRegex+queryRegex+fragRegex);
//        Pattern authoP = Pattern.compile(pathRegex);
//
//        boolean hasScheme = true;
//        String copied = str;
//
//        if (copied.indexOf("://")<0) {
//            hasScheme = false;
//            copied = "abc://" + copied;
//        }
//
//        Matcher m = uriP.matcher(copied);
//
//        if (!m.find()) {
//            return false;
//        }
//        Log.i("Scene", "[[[[[ matching sring : " + m.toString());
//        for (int i=0; i<m.groupCount(); i++) {
//            Log.i("Scene", "[[[[[ group " + i + " : " + m.group(i));
//        }
//
//
//        if (hasScheme) {
//            _schems = m.group(1);
//            _schems = _schems.toLowerCase();
//            if (_schems=="https" || _schems=="wss") {
//
//            }
//        }
//
//        return true;
//    }

////    private String _schems;
//
//    private static String queryParamRegexString1 = "(^|&)";
//    private static String queryParamRegexString2 = "([^=&]*)=?";
//    private static String queryParamRegexString3 = "([^=&]*)";
//    private static String queryParamRegexString4 = "(?=(&|$))";
//
//    private static Pattern queryParamRegex = Pattern.compile(queryParamRegexString1+queryParamRegexString2+queryParamRegexString3+queryParamRegexString4);
//    private String _query = "";
//    private ArrayList<Pair<String, String>> _queryParams = new ArrayList<>();
//
//    public ArrayList<Pair<String, String>> getQueryParams() {
//        if (!_query.isEmpty() && _queryParams.isEmpty()) {
//            Log.i("scene", "[[[[[ query : " + _query);
//            Matcher m = queryParamRegex.matcher(_query);
//
//
//            while (m.find()) {
//                Log.i("scene", "[[[[[ match : " + m.group(2) + " = " + m.group(3));
//            }
//
////            if (!m.find()) {
////                Log.i("scene", "[[[[[ not found : " + m.toString() + ", valeu : " + _query);
////            } else {
////
////                for (int i=0; i<m.groupCount(); i++) {
////                    Log.i("scene", "[[[[[ match : " + m.group(i));
////                }
////            }
////            for (int i=0; i<_query.length(); i++) {
////                String substr = _query.substring(i, i+1);
////                Matcher m = queryParamRegex.matcher(_query);
////                Log.i("scene", "[[[[[ match : " + m.group(0));
////            }
//        }
//
//        return _queryParams;
//    }

//    @Override
//    public void onDownloadProgress(int id, int taskId, long dl, long dlnow, long dltotal) {
//        Log.i("Scene", "[[[[[ onProgress(bytesWritten:" + dlnow + " totalSize:" + dltotal);
//    }
//
//    @Override
//    public void onDownloadFinish(int id, int taskId, int errCode, String errStr, final byte[] data) {
//        Log.i("Scene", "[[[[[ download finish");
//    }


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
            cell = SMView.create(getDirector(), 0, 0, 0, s.width, 250);
                    cell.setBackgroundColor(Color4F.WHITE);

                    String str = _menuNames.get(index);
                    SMLabel title = SMLabel.create(getDirector(), str, 55, MakeColor4F(0x222222, 1.0f));
                    title.setAnchorPoint(Vec2.MIDDLE);
                    title.setPosition(new Vec2(s.width/2, cell.getContentSize().height/2));
                    cell.addChild(title);

                    SMRoundLine line = SMRoundLine.create(getDirector());
                    line.setBackgroundColor(MakeColor4F(0xdbdcdf, 1.0f));
                    line.setLineWidth(2);
            line.line(20, 248, s.width-20, 248);
                    line.setLengthScale(1);
                    cell.addChild(line);

                    cell.setTag(index);
            cell.setOnClickListener(this);

            cell.setOnStateChangeListener(new OnStateChangeListener() {
                @Override
                public void onStateChange(SMView view, STATE state) {
                    if (state==STATE.PRESSED) {
                        view.setBackgroundColor(Color4F.XEEEFF1, 0.15f);
                    } else {
                        view.setBackgroundColor(Color4F.WHITE, 0.15f);
                    }
                }
            });
        }
        return cell;
    }

                        @Override
                        public void onClick(SMView view) {
                            int index = view.getTag();

        SMScene scene = null;
        SceneParams params = new SceneParams();
        params.putInt("SCENE_TYPE", index);
        params.putString("MENU_NAME", _menuNames.get(index));
        scene = ListScene.create(getDirector(), _menuBar, params);
                                SlideInToLeft left = SlideInToLeft.create(getDirector(), 0.3f, scene);
                                getDirector().pushScene(left);
                            }

    protected SMView arrowView = null;

    public boolean onMenuClick(SMView view) {
        MenuBar.MenuType type = MenuBar.intToMenuType(view.getTag());
        switch (type) {
            case MENU:
            {
                // side menu open
                SideMenu.OpenMenu(this);
                return true;
            }
        }
        return false;
    }

    public void onMenuTouchg() {

    }

    public void openMenu() {
        SideMenu.OpenMenu(this);
    }

    @Override
    public void onTransitionStart(final Transition type, final int tag) {

        if (type==Transition.IN) {
            if (getSwipeType()==SwipeType.MENU) {
                _menuBar.setMenuButtonType(MenuBar.MenuType.MENU, false);
            } else {
                _menuBar.setMenuButtonType(MenuBar.MenuType.BACK, false);
            }
            _menuBar.setColorSet(MenuBar.ColorSet.WHITE_TRANSLUCENT, false);
            _menuBar.setText(_sceneTitle, false);
        }
    }

    protected MenuBar.MenuBarListener _menuBarListener = new MenuBar.MenuBarListener() {
        @Override
        public boolean func1(SMView view) {
            return onMenuClick(view);
        }

        @Override
        public void func2() {
            onMenuTouchg();
        }
    };



    @Override
    public void onTransitionComplete(final Transition type, final int tag) {
        if (type == Transition.RESUME) {
            bringMenuBarFromLayer();
        } else if (type == Transition.OUT) {

        }
    }

    protected void bringMenuBarFromLayer() {
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

    private Downloader _downloader;
}

