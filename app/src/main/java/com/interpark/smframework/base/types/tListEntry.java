package com.interpark.smframework.base.types;

import com.interpark.smframework.base.SMView;

import java.util.ArrayList;

public class tListEntry {
    public tListEntry() {
        callback = null;
        target = null;
        priority = 0;
        paused = false;
        markedForDeletion= false;
    }
    public SEL_SCHEDULE callback;
    Ref target;
    int priority;
    boolean paused;
    boolean markedForDeletion;
}
