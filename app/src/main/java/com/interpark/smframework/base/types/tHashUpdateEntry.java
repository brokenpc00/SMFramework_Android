package com.interpark.smframework.base.types;

import com.interpark.smframework.base.SMView;

import java.util.ArrayList;

public class tHashUpdateEntry {
    public tHashUpdateEntry() {
        list = new ArrayList<>();
        entry = null;
        target = null;
        callback = null;
    }
    public ArrayList<tListEntry> list;
    tListEntry entry;
    Ref target;
    SEL_SCHEDULE callback;
}
