package com.interpark.smframework.base.types;

import android.util.Log;
import android.util.SparseArray;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import java.sql.Time;
import java.util.ArrayList;

public class Scheduler {

    public Scheduler(IDirector director) {
        _director = director;
    }

    public static final int PRIORITY_SYSTEM = Integer.MIN_VALUE;
    public static final int PRIORITY_NON_SYSTEM_MIN = PRIORITY_SYSTEM + 1;

    protected void removeHashElement(tHashTimerEntry element) {
        element.timers.clear();
        if (_hashForTimers!=null) {
            _hashForTimers.remove(element.target.hashCode());
        }
    }

    public void schedule(SEL_SCHEDULE selector, Ref target, float interval, boolean paused) {
        schedule(selector, target, interval, Integer.MAX_VALUE-1, 0.0f, paused);
    }

    public void schedule(SEL_SCHEDULE selector, Ref target, float interval, long repeat, float delay, boolean paused) {
        assert (target!=null);

        tHashTimerEntry element = findTimerElement(target);

        if (element==null) {
            element = new tHashTimerEntry();
            element.target = target;

            _hashForTimers.append(target.hashCode(), element);

            element.paused = paused;
        } else {
            assert (element.paused==paused);
        }

        if (element.timers==null) {
            element.timers= new ArrayList<>(10);
        } else {
            for (int i=0; i<element.timers.size(); i++) {
                TimerTargetSelector timer = element.timers.get(i);
                if (timer!=null && !timer.isExhausted() && selector==timer.getSelector()) {
                    timer.setupTimerWithInterval(interval, repeat, delay);
                    return;
                }
            }
        }

        TimerTargetSelector timer = new TimerTargetSelector(_director);
        timer.initWithSelector(this, selector, target, interval, repeat, delay);
        element.timers.add(timer);

        _hashForTimers.put(target.hashCode(), element);
//        _hashForTimers.append(target.hashCode(), element);
    }

    public void scheduleUpdate(final ActionManager manager, int priority, boolean paused) {
        schedulePerFrame(new SEL_SCHEDULE() {
            @Override
            public void scheduleSelector(float t) {
                manager.update(t);
            }
        }, manager, priority, paused);
    }

    public void scheduleUpdate(final SMView target, int priority, boolean paused) {
        schedulePerFrame(new SEL_SCHEDULE() {
            @Override
            public void scheduleSelector(float t) {
                target.update(t);
            }
        }, target, priority, paused);
    }

    public void unschedule(SEL_SCHEDULE selector, Ref target) {
        if (target==null || selector==null) {
            return;
        }

        tHashTimerEntry element = findTimerElement(target);
        if (element!=null) {
            for (int i=0; i<element.timers.size(); i++) {
                TimerTargetSelector timer = element.timers.get(i);

                if (timer!=null && selector==timer.getSelector()) {

                    if (timer==element.currentTimer && !timer.isAborted()) {
                        timer.setAborted();
                    }

                    element.timers.remove(i);

                    if (element.timerIndex>=i) {
                        element.timerIndex--;
                    }

                    if (element.timers.size()==0) {
                        if (_currentTarget==element) {
                            _currentTargetSalvaged = true;
                        } else {
                            removeHashElement(element);
                        }
                    }

                    return;
                }

            }
        }
    }

    public void unscheduleUpdate(Ref target) {
        if (target==null) {
            return;
        }

        tHashUpdateEntry element = findUpdateElement(target);
        if (element!=null) {
            removeUpdateFromHash(element.entry);
        }
    }

    protected tHashTimerEntry findTimerElement(Ref target) {
        if (_hashForTimers==null) {
            _hashForTimers = new SparseArray<tHashTimerEntry>();
        }

        return _hashForTimers.get(target.hashCode());
    }

    public void unscheduleAllForTarget(Ref target) {
        if (target==null) {
            return;
        }

        tHashTimerEntry element = findTimerElement(target);
        if (element!=null) {
            if (element.timers.contains(element.currentTimer) &&
                    (!element.currentTimer.isAborted())) {
                element.currentTimer.setAborted();
            }

            element.timers.clear();

            if (_currentTarget==element) {
                _currentTargetSalvaged = true;
            } else {
                removeHashElement(element);
            }
        }

        unscheduledUpdate(target);
    }


    protected tHashUpdateEntry findUpdateElement(Ref target) {
        if (_hashForUpdates==null) {
            _hashForUpdates = new SparseArray<tHashUpdateEntry>();
        }

        return _hashForUpdates.get(target.hashCode());
    }

    protected void schedulePerFrame(SEL_SCHEDULE selector, Ref target, int priority, boolean paused) {
        tHashUpdateEntry hashElement = findUpdateElement(target);
        if (hashElement!=null) {
            if (hashElement.entry.priority!=priority) {
                unscheduledUpdate(target);
            } else {
                return;
            }
        }

        appendIn(selector, target, priority, paused);
    }



    protected void appendIn(SEL_SCHEDULE selector, Ref target, int priority, boolean paused) {
        tListEntry listElement = new tListEntry();
        listElement.callback = selector;
        listElement.target = target;
        listElement.paused = paused;
        listElement.markedForDeletion = false;

        tHashUpdateEntry hashElement = new tHashUpdateEntry();

        if (priority==0) {
            // append in

            _updates0List.add(listElement);

            hashElement.list = _updates0List;
        } else if (priority<0) {
            // priority negative
            if (_updatesNegList==null) {
                _updatesNegList = new ArrayList<>();
                _updatesNegList.add(listElement);
            } else {
                boolean added = false;

                for (int i=0; i<_updatesNegList.size(); i++) {
                    tListEntry element = _updatesNegList.get(i);
                    if (priority<element.priority) {
                        _updatesNegList.add(i, listElement);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    _updatesNegList.add(listElement);
                }
            }

            hashElement.list = _updatesNegList;
        } else {
            // priority positive
            if (_updatesPosList==null) {
                _updatesPosList = new ArrayList<>();
                _updatesPosList.add(listElement);
            } else {
                boolean added = false;

                for (int i=0; i<_updatesPosList.size(); i++) {
                    tListEntry element = _updatesPosList.get(i);
                    if (priority<element.priority) {
                        _updatesPosList.add(i, listElement);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    _updatesPosList.add(listElement);
                }
            }

            hashElement.list = _updatesPosList;
        }

        hashElement.target = target;
        hashElement.entry = listElement;
        _hashForUpdates.put(hashElement.target.hashCode(), hashElement);
    }

    public void unscheduledUpdate(Ref target) {
        if (target==null) {
            return;
        }

        tHashUpdateEntry element = findUpdateElement(target);
        if (element!=null) {
            removeUpdateFromHash(element.entry);
        }
    }

    protected void removeUpdateFromHash(tListEntry entry) {
        tHashUpdateEntry element = findUpdateElement(entry.target);
        if (element!=null) {
            element.list.remove(element.entry);
            if (_updateHashLocked) {
                element.entry.markedForDeletion = true;
                _updateDeleteVector.add(element.entry);
            } else {
                element.entry = null;
            }

            _hashForUpdates.remove(element.target.hashCode());
        }
    }

    public void unscheduleAll() {
        unscheduleAllWithMinPriority(PRIORITY_SYSTEM);
    }

    public void unscheduleAllWithMinPriority(int minPriority) {
        tHashTimerEntry element = null;
        for (int i=0; i<_hashForTimers.size(); i++) {
            element = _hashForTimers.valueAt(i);
            if (element!=null) {
                unscheduleAllForTarget(element.target);
            }
        }

        tListEntry entry = null;
        if (minPriority<0) {
            for (int i=0; i<_updatesNegList.size(); i++) {
                entry = _updatesNegList.get(i);
                if (entry!=null && entry.priority>=minPriority) {
                    unscheduledUpdate(entry.target);
                }
            }
        }

        if (minPriority<=0) {
            for (int i=0; i<_updates0List.size(); i++) {
                entry = _updates0List.get(i);
                if (entry!=null) {
                    unscheduledUpdate(entry.target);
                }
            }
        }

        for (int i=0; i<_updatesPosList.size(); i++) {
            entry = _updatesPosList.get(i);
            if (entry!=null && entry.priority >= minPriority) {
                unscheduledUpdate(entry.target);
            }
        }
    }

    public boolean isScheduled(SEL_SCHEDULE selector, Ref target) {
        assert (selector!=null);
        assert (target!=null);

        tHashTimerEntry element = findTimerElement(target);

        if (element==null) {
            return false;
        }

        if (element.timers==null) {
            return false;
        }

        for (int i=0; i<element.timers.size(); i++) {
            TimerTargetSelector timer = element.timers.get(i);

            if (timer!=null && !timer.isExhausted() && selector==timer.getSelector()) {
                return true;
            }
        }

        return false;
    }

    public void update(float dt) {
        _updateHashLocked = true;

        if (_timeScale!=1.0f) {
            dt *= _timeScale;
        }

        tListEntry entry = null;

        for (int i=0; i<_updatesNegList.size(); i++) {
            entry = _updatesNegList.get(i);
            if (entry!=null && !entry.paused && !entry.markedForDeletion) {
                entry.callback.scheduleSelector(dt);
            }
        }

        for (int i=0; i<_updates0List.size(); i++) {
            entry = _updates0List.get(i);
            if (entry!=null && !entry.paused && !entry.markedForDeletion) {
                entry.callback.scheduleSelector(dt);
            }
        }

        for (int i=0; i<_updatesPosList.size(); i++) {
            entry = _updatesPosList.get(i);
            if (entry!=null && !entry.paused && !entry.markedForDeletion) {
                entry.callback.scheduleSelector(dt);
            }
        }

        for (int i=0; i<_hashForTimers.size(); i++) {
            tHashTimerEntry elt = _hashForTimers.valueAt(i);
            _currentTarget = elt;
            _currentTargetSalvaged = false;
            if (!_currentTarget.paused) {
                for (elt.timerIndex=0; elt.timerIndex<elt.timers.size(); ++elt.timerIndex) {
                    elt.currentTimer = elt.timers.get(elt.timerIndex);
                    assert (!elt.currentTimer.isAborted());

                    elt.currentTimer.update(dt);

                    elt.currentTimer = null;
                }
            }

            if (_currentTargetSalvaged && _currentTarget.timers.size()==0) {
                removeHashElement(_currentTarget);
                i--;
            }
        }

        _updateDeleteVector.clear();

        _updateHashLocked = false;
        _currentTarget = null;

        if (!_functionsToPerform.isEmpty()) {

            synchronized (_functionsToPerform) {
                for (int i=0; i<_functionsToPerform.size(); i++) {
                    PERFORM_SEL func = _functionsToPerform.get(i);
                    func.performSelector();
                }

                _functionsToPerform.clear();
            }
        }
    }

    public float getTimeScale() {return _timeScale;}

    public void setTimeScale(float scale) {
        _timeScale = scale;
    }

    public void performFunctionInMainThread(PERFORM_SEL func) {
        synchronized (_functionsToPerform) {
            _functionsToPerform.add(func);
        }
    }

    public void removeAllFunctionsToBePerformedInMainThread() {
        synchronized (_functionsToPerform) {
            _functionsToPerform.clear();
        }
    }

    public void pauseTarget(Ref target) {
        assert (target!=null);

        tHashTimerEntry element = findTimerElement(target);
        if (element!=null) {
            element.paused = true;
        }

        tHashUpdateEntry elementUpdate = findUpdateElement(target);
        if (elementUpdate!=null) {
            assert (elementUpdate.entry!=null);
            elementUpdate.entry.paused = true;
        }
    }

    public void resumeTarget(Ref target) {
        tHashTimerEntry element = findTimerElement(target);
        if (element!=null) {
            element.paused = false;
        }

        tHashUpdateEntry  elementUpdate = findUpdateElement(target);
        if (elementUpdate!=null) {
            assert (elementUpdate.entry!=null);

            elementUpdate.entry.paused = false;
        }
    }

    public boolean isTargetPaused(Ref target) {
        assert (target!=null);

        tHashTimerEntry element = findTimerElement(target);
        if (element!=null) {
            return element.paused;
        }

        tHashUpdateEntry elementUpdate = findUpdateElement(target);
        if (elementUpdate!=null) {
            return elementUpdate.entry.paused;
        }

        return false;
    }



    protected float _timeScale = 1.0f;

    protected IDirector _director = null;

    // priority < 0
    protected ArrayList<tListEntry> _updatesNegList = new ArrayList<>();

    // priority == 0
    protected ArrayList<tListEntry> _updates0List = new ArrayList<>();

    // priority > 0
    protected ArrayList<tListEntry> _updatesPosList = new ArrayList<>();

    protected SparseArray<tHashUpdateEntry> _hashForUpdates = new SparseArray<tHashUpdateEntry>();

    protected ArrayList<tListEntry> _updateDeleteVector = new ArrayList<>();

    protected SparseArray<tHashTimerEntry> _hashForTimers = new SparseArray<tHashTimerEntry>();

    protected tHashTimerEntry _currentTarget = null;

    protected boolean _currentTargetSalvaged = false;

    protected boolean _updateHashLocked = false;

    protected ArrayList<PERFORM_SEL> _functionsToPerform = new ArrayList<>(30);
}
