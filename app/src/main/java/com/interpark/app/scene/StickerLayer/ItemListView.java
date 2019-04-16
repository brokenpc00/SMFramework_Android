package com.interpark.app.scene.StickerLayer;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.FileUtils;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Value;
import com.interpark.smframework.view.SMTableView;
import com.interpark.smframework.base.SMView;

public class ItemListView extends SMTableView implements SMView.OnClickListener {
    public static final float PANEL_HEIGHT = 160.0f;
    public static final float CELL_WIDTH = 204.f+30.0f;

    public ItemListView(IDirector director) {
        super(director);
        _listener = null;
        _initLoaded = false;
    }

    public interface OnItemClickListener {
        public void onItemClick(ItemListView sender, StickerItemThumbView view);
    }

    public static final String ITEMS = "items";
    public static final String THUMB = "thumb";
    public static final String NAME = "name";
    public static final String IMG_EXTEND = ".png";
    public static final String IMAGE = "image";
    public static final String LAYOUT = "layout";


    @Override
    public void setVisible(boolean visible) {
        if (visible!=_visible) {
            if (visible) {
                show();
            } else {
                hide();
            }
        }
        super.setVisible(visible);
    }

    public void show() {
        initLoadItemList();
    }

    public void hide() {
        stop();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        _listener = l;
    }

    public String getResourceRootPath() {return _resourceRootPath;}

    public boolean initLayout() {
        setContentSize(new Size(_director.getWinSize().width, PANEL_HEIGHT));
        setMaxScrollVelocity(10000);
        setPreloadPaddingSize(200);

        setBackgroundColor(MakeColor4F(0x767678, 1.0f));

        super.setVisible(false);

        return true;
    }

    @Override
    public void onClick(SMView view) {
        if (_listener!=null) {
            if (view instanceof StickerItemThumbView) {
                _listener.onItemClick(this, (StickerItemThumbView)view);
            }
        }
    }

    public boolean initLoadItemList() {
        if (!_initLoaded) {
            String plist = _resourceRootPath + ITEMS + ".xml";
            String fullPath = FileUtils.getInstance().fullPathForFilename(plist);

            Value.ValueList itemList = new Value.ValueList();
            Value.ValueList thumbList = new Value.ValueList();
            for (int i=0; i<=20; i++) {

                Value.ValueMap map = new Value.ValueMap();

                String no = "0";
                if (i<10) {
                    no += "0" + i;
                } else {
                    no += i;
                }

                if (i==0) {
                    map.put(NAME, new Value("styling_no_image"));
                } else {
                    map.put(NAME, new Value(no));
//                    map.put(IMAGE, new Value("{" + no + "}"));
                    map.put(IMAGE, new Value(no));
                }

                itemList.add(new Value(map));
            }
            _dict.put(ITEMS, new Value(itemList));
            _itemSize = _dict.get(ITEMS).getList().size();
            _initLoaded = true;
        }

        return true;
    }





    protected boolean _initLoaded = false;
    protected Value.ValueMap _dict = new Value.ValueMap();
    protected OnItemClickListener _listener = null;
    protected String _resourceRootPath = "";
    protected int _itemSize = 0;
}
