package com.interpark.smframework.view.Sticker;

import java.util.ArrayList;


public class StickerItem {
    public StickerItem() {

    }

    public int _index = 0;
    public boolean _decoded = false;
    public String _name = "";
    public ArrayList<String> _imageArray = new ArrayList<>();
    public int _layout = 0;
    public int _defaultIndex = 0;
    public int _selectIndex = 0;
    public String _rootPath = "";
}
