package com.interpark.app.scene.StickerLayer;

import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.base.types.IndexPath;
import com.interpark.smframework.util.ImageManager.DownloadConfig;
import com.interpark.smframework.util.ImageManager.ImageDownloader;
import com.interpark.smframework.util.Value;
import com.interpark.smframework.util.Vec2;
import com.interpark.smframework.view.SMLabel;
import com.interpark.smframework.view.Sticker.StickerItem;

import java.util.ArrayList;

public class StickerItemListView extends ItemListView {
    public StickerItemListView(IDirector director) {
        super(director);
        _resourceRootPath = "sticker/";
    }

    public static StickerItemListView create(IDirector director) {
        StickerItemListView listView = new StickerItemListView(director);
        listView.initWithOrientAndColumns(Orientation.HORIZONTAL, 1);
        listView.initLayout();
        return listView;
    }

    @Override
    public void show() {
        super.show();

        ArrayList<SMView> cells = getVisibleCells();

        for (SMView cell : cells) {
            if (cell instanceof StickerItemThumbView) {
                StickerItemThumbView thumb = (StickerItemThumbView)cell;
                // shake item.
                thumb.startShowAction();
            }

        }
    }

    public StickerItem findItem(final String name) {
        if (name.isEmpty()) {
            return null;
        }

        initLoadItemList();

        Value.ValueList array = _dict.get(ITEMS).getList();
        for (int index=0; _itemSize!=0; index++) {
            if (_items.get(index)._decoded) {
                if (_items.get(index)._name.compareTo(name)==0) {
                    return _items.get(index);
                }
            } else {
                Value.ValueMap m = array.get(index).getMap();
                if (name.compareTo(m.get(NAME).toString())==0) {
                    setStickerItem(_items.get(index), index);
                    return _items.get(index);
                }
            }
        }

        return null;
    }

    public StickerItem getItem(final int index) {
        if (index<0) {
            return null;
        }

        initLoadItemList();

        if (index>=_itemSize) {
            return null;
        }

        if (_items.get(index)._decoded) {
            return _items.get(index);
        }

        setStickerItem(_items.get(index), index);
        return _items.get(index);
    }

    @Override
    public boolean initLayout() {
        super.initLayout();

        cellForRowAtIndexPath = new CellForRowAtIndexPath() {
            @Override
            public SMView cellForRowAtIndexPath(IndexPath indexPath) {
                return getView(indexPath);
            }
        };
        numberOfRowsInSection = new NumberOfRowsInSection() {
            @Override
            public int numberOfRowsInSection(int section) {
                return getItemCount(section);
            }
        };

        setScrollMarginSize(15, 15);
        hintFixedCellSize(CELL_WIDTH);

        return true;
    }

    @Override
    public boolean initLoadItemList() {
        if (!_initLoaded) {
            if (super.initLoadItemList()) {
                Value.ValueList items = _dict.get(ITEMS).getList();
                for (int i=0; i<items.size(); i++) {
                    StickerItem item = new StickerItem();
                    item._decoded = false;
                    _items.add(item);
                }
            }
        }

        return _initLoaded;
    }

    public boolean setStickerItem(StickerItem item, int index) {
        item._index = index;
        item._decoded = true;
        item._rootPath = _resourceRootPath;

        Value.ValueMap m = _dict.get(ITEMS).getList().get(index).getMap();
        if (m.get(NAME)!=null) {
            item._name = m.get(NAME).getString();
        } else {
            item._name = "";
        }

        if (m.get(IMAGE)!=null) {
            String str = m.get(IMAGE).getString();
            // 여러개 있을 수 있다.
            item._imageArray.add(str);
        }

        // color는 나중에
        item._layout = -1;

        return true;
    }

    protected SMView getView(final IndexPath indexPath) {
        StickerItemThumbView thumb = null;

        int index = indexPath.getIndex();

        if (index==0) {
            // clear all sticker
            thumb = (StickerItemThumbView)dequeueReusableCellWithIdentifier("NOIMAGE");
        } else {
            thumb = (StickerItemThumbView)dequeueReusableCellWithIdentifier("STICKER");
        }

        if (thumb==null) {
            thumb = StickerItemThumbView.create(getDirector(), index, ImageDownloader.NO_DISK, ImageDownloader.NO_CACHE);
            thumb.setContentSize(CELL_WIDTH, PANEL_HEIGHT);
            thumb.setOnClickListener(this);

            if (index==0) {
                // clear all sticker (no image)
                SMLabel text = SMLabel.create(getDirector(), "CLEAR\nALL", 30);
                text.setColor(Color4F.TEXT_BLACK);
                text.setAnchorPoint(Vec2.MIDDLE);
                text.setPosition(thumb.getImageView().getContentSize().divide(2));
                thumb.addChild(text);
            }
        }
        thumb.setTag(index);

        Value.ValueMap m = _dict.get(ITEMS).getList().get(index).getMap();
        thumb.setTag(index);
        String thumbPath = _resourceRootPath + THUMB + "/" + m.get(NAME).getString() + IMG_EXTEND;
//        Log.i("STICKELIST", "[[[[[ thumb Path("+index+") : " + thumbPath);
        thumb.setImagePath(thumbPath);

        return thumb;
    }

    protected int getItemCount(int section) {
        return _itemSize;
    }

    protected ArrayList<StickerItem> _items = new ArrayList<>();

}
