package com.interpark.smframework.base.types;

import com.interpark.smframework.base.SMView;

import java.util.ArrayList;

public class tHashTimerEntry {
    public tHashTimerEntry() {
        timers = new ArrayList<>();
        target = null;
        timerIndex = 0;
        currentTimer = null;
        paused = false;
    }

    public ArrayList<TimerTargetSelector> timers;
    Ref target;
    int timerIndex;
    Timer currentTimer;
    boolean paused;
}
