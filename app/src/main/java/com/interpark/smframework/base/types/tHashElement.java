package com.interpark.smframework.base.types;

import com.interpark.smframework.base.SMView;

import java.util.ArrayList;

public class tHashElement {
    public tHashElement() {
        actions = new ArrayList<>();
        currentAction = null;
        actionIndex = 0;
        currentActionSalvaged = false;
        paused = false;
    }
    public ArrayList<Action> actions;
    public Ref target;
    public int actionIndex;
    public Action currentAction;
    public boolean currentActionSalvaged;
    public boolean paused;
}
