package com.interpark.smframework.base.types;

import android.util.Log;
import android.util.SparseArray;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

public class ActionManager extends Ref implements Cloneable {
    public ActionManager(IDirector director) {
        super(director);
        _targets = new SparseArray<tHashElement>();
    }

    protected SparseArray<tHashElement> _targets = new SparseArray<tHashElement>();
    protected tHashElement _currentTarget = null;
    protected boolean _currentTargetSalvaged = false;

    protected tHashElement findHashElement(Ref target) {
        if (_targets==null) {
            _targets = new SparseArray<tHashElement>();
        }

        return _targets.get(target.hashCode());
    }

    public void addAction(Action action, Ref target, boolean paused) {
        if (action==null || target==null) {
            return;
        }

        tHashElement elements = findHashElement(target);

        if (elements==null) {
            elements = new tHashElement();
            elements.paused = paused;
            elements.target = target;
            elements.actions = new ArrayList<>();
            _targets.remove(target.hashCode());
        }


        assert (!elements.actions.contains(action));

        elements.actionIndex = elements.actions.size();
        elements.actions.add(action);
//        elements.actions.add(elements.actionIndex, action);

        _targets.put(target.hashCode(), elements);

        action.startWithTarget((SMView)target);
    }

    public void removeAllActions() {
        for (int i=0; i<_targets.size(); i++) {
            tHashElement element = _targets.valueAt(i);
            Ref target = element.target;
            removeallActionsFromTarget(target);
        }
        _targets.clear();
    }

    public void removeallActionsFromTarget(Ref target) {

        if (target==null) {
            return;
        }

        tHashElement element = _targets.get(target.hashCode());
        if (element!=null) {
            if (element.actions.contains(element.currentAction) && (!element.currentActionSalvaged)) {
                element.currentActionSalvaged = true;
            }

            element.actions.clear();
            if (_currentTarget==element) {
                _currentTargetSalvaged = true;
            } else {
                deleteHashElement(element);
            }
        }
    }

    public void deleteHashElement(tHashElement element) {
        element.actions.clear();
        _targets.remove(element.target.hashCode());
    }

    public void removeAction(Action action) {
        if (action==null) {
            return;
        }

        Ref target = action.getOriginalTarget();
        tHashElement element = findHashElement(target);
        if (element!=null) {
            int index = element.actions.indexOf(action);
            if (index!=-1) {
                removeActionAtIndex(index, element);
            }
        }

    }

    protected tHashElement removeActionAtIndex(int index, tHashElement element) {
        Action action = element.actions.get(index);
        if (action==element.currentAction && (!element.currentActionSalvaged)) {
            element.currentActionSalvaged = true;
        }

        element.actions.remove(index);

        if (element.actionIndex >= index) {
            element.actionIndex--;
        }

        if (element.actions.size()==0) {
            if (_currentTarget==element) {
                _currentTargetSalvaged = true;
            } else {
                deleteHashElement(element);
            }
        }

        return element;
    }

    public void removeActionByTag(int tag, Ref target) {
        assert (tag!=Action.INVALID_TAG);

        if (target==null) {
            return;
        }

        tHashElement element = findHashElement(target);

        if (element!=null) {
            int limit = element.actions.size();
            for (int i=0; i<limit; ++i) {
                Action action = element.actions.get(i);

                if (action.getTag()==tag && action.getOriginalTarget()==target) {
                    removeActionAtIndex(i, element);
                    break;
                }
            }
        }
    }

    public void removeAllActionsByTag(int tag, Ref target) {
        assert (tag!=Action.INVALID_TAG);
        if (target==null) {
            return;
        }

        tHashElement element = findHashElement(target);

        if (element!=null) {
            int limit = element.actions.size();
            for (int i=0; i<limit;) {
                Action action = element.actions.get(i);
                if (action.getTag()==tag && action.getOriginalTarget()==target) {
                    removeActionAtIndex(i, element);
                    --limit;
                } else {
                    ++i;
                }
            }
        }
    }

    public void removeActionsByFlags(long flags, Ref target) {
        if (flags==0) {
            return;
        }

        if (target==null) {
            return;
        }

        tHashElement element = findHashElement(target);
        if (element!=null) {
            int limit = element.actions.size();
            for (int i=0; i<limit;) {
                Action action = element.actions.get(i);
                if ((action.getFlags()&flags)!=0 && action.getOriginalTarget()==target) {
                    removeActionAtIndex(i, element);
                    --limit;
                } else {
                    ++i;
                }
            }
        }
    }

    public Action getActionByTag(int tag, Ref target) {
        if (tag==Action.INVALID_TAG) {
            return null;
        }

        tHashElement element = findHashElement(target);
        if (element!=null) {
            if (element.actions!=null) {
                int limit = element.actions.size();
                for (int i=0; i<limit; ++i) {
                    Action action = element.actions.get(i);
                    if (action.getTag()==tag) {
                        return action;
                    }
                }
            }
        }

        return null;
    }

    public int getNumberOfRunningActionsInTarget(Ref target) {
        tHashElement element = findHashElement(target);
        if (element!=null) {
            return element.actions.size();
        }

        return 0;
    }

    public int getNumberOfRunningActions() {
        int count = 0;

        for (int i=0; i<_targets.size(); i++) {
            tHashElement element = _targets.valueAt(i);
            count += element.actions.size();
        }

        return count;
    }

    public int getNumberOfRunningActionsInTargetByTag(Ref target, int tag) {
        if (tag==Action.INVALID_TAG) {
            return 0;
        }

        tHashElement element = findHashElement(target);

        if (element==null || element.actions.size()==0) {
            return 0;
        }

        int count = 0;
        int limit = element.actions.size();
        for (int i=0; i<limit; ++i) {
            Action action = element.actions.get(i);
            if (action!=null && action.getTag()==tag) {
                ++count;
            }
        }

        return count;
    }

    public void pauseTarget(Ref target) {
        tHashElement element = findHashElement(target);
        if (element!=null) {
            element.paused = true;
        }
    }

    public void resumeTarget(Ref target) {
        tHashElement element = findHashElement(target);
        if (element!=null) {
            element.paused = false;
        }
    }

    public ArrayList<Ref> pauseAllRunningActions() {
        ArrayList<Ref> idsWithActions = new ArrayList<>();

        for (int i=0; i<_targets.size(); i++) {
            tHashElement element = _targets.valueAt(i);

            if (!element.paused) {
                element.paused = true;
                idsWithActions.add(element.target);
            }
        }

        return idsWithActions;
    }

    public void resumeTargets(ArrayList<Ref> targetsToResume) {
        for (Ref view : targetsToResume) {
            this.resumeTarget(view);
        }
    }

    public void update(float dt) {
        for (int i=0; i<_targets.size(); i++) {
            tHashElement elt = _targets.valueAt(i);
            _currentTarget = elt;
            _currentTargetSalvaged = false;

            if (!_currentTarget.paused) {
                for (_currentTarget.actionIndex=0; _currentTarget.actionIndex < _currentTarget.actions.size(); _currentTarget.actionIndex++) {
                    _currentTarget.currentAction = _currentTarget.actions.get(_currentTarget.actionIndex);
                    if (_currentTarget.currentAction==null) {
                        continue;
                    }


                    _currentTarget.currentActionSalvaged = false;

                    _currentTarget.currentAction.step(dt);

                    if (_currentTarget.currentAction.isDone()) {
                        _currentTarget.currentAction.stop();
                        Action action = _currentTarget.currentAction;
                        _currentTarget.currentAction = null;
                        removeAction(action);
                    }

                    _currentTarget.currentAction = null;
                }
            }

            if (_currentTargetSalvaged && _currentTarget.actions.size()==0) {
                deleteHashElement(_currentTarget);
            } else if (_currentTargetSalvaged && _currentTarget.target!=null) {
                deleteHashElement(_currentTarget);
            }
        }

        _currentTarget = null;
    }

}
