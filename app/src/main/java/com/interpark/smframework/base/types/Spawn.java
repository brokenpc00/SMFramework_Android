package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import java.io.FileInputStream;
import java.util.ArrayList;

public class Spawn extends ActionInterval {
    public Spawn(IDirector director) {
        super(director);
    }

    public static Spawn create(IDirector director, FiniteTimeAction... actions) {
        assert (actions!=null);

        Spawn ret = null;
        FiniteTimeAction now = null;
        FiniteTimeAction action1 = null;
        FiniteTimeAction prev = action1 = actions[0];

        int currentIndex = 0;
        if (actions.length==1) {
            // only one actoin
            ret = createWithTwoActions(director, prev, ExtraAction.create(director));
            return ret;
        } else {
            for (int i=1; i<actions.length-1; ++i) {
                now = actions[i];
                prev = createWithTwoActions(director, prev, now);
            }
            return (Spawn)prev;
        }

    }

    public static Spawn createWithVariableList(IDirector director, ArrayList<FiniteTimeAction> actions) {
        assert (actions!=null);

        Spawn ret = null;
        FiniteTimeAction now = null;
        FiniteTimeAction action1 = null;
        FiniteTimeAction prev = action1 = actions.get(0);

        int currentIndex = 0;
        if (actions.size()==1) {
            // only one actoin
            ret = createWithTwoActions(director, prev, ExtraAction.create(director));
            return ret;
        } else {
            for (int i=1; i<actions.size()-1; ++i) {
                now = actions.get(i);
                prev = createWithTwoActions(director, prev, now);
            }
            return (Spawn)prev;
        }
    }

    public static Spawn createWithTwoActions(IDirector director, FiniteTimeAction action1, FiniteTimeAction action2) {
        Spawn seq = new Spawn(director);
        if (seq!=null && seq.initWithTwoActions(action1, action2)) {
            return seq;
        }
        return null;
    }

    protected boolean init(final ArrayList<FiniteTimeAction> arrayOfActions) {
        int count = arrayOfActions.size();

        if (count==0) {
            return false;
        }

        if (count==1) {
            return initWithTwoActions(arrayOfActions.get(0), ExtraAction.create(getDirector()));
        }

        FiniteTimeAction prev = arrayOfActions.get(0);
        for (int i=1; i<count-1; ++i) {
            prev = createWithTwoActions(getDirector(), prev, arrayOfActions.get(i));
        }

        // together last action.
        return initWithTwoActions(prev, arrayOfActions.get(count-1));
    }

    protected boolean initWithTwoActions(FiniteTimeAction action1, FiniteTimeAction action2) {
        assert (action1!=null);
        assert (action2!=null);

        if (action1==null || action2==null) {
            return false;
        }

        boolean ret = false;

        float d1 = action1.getDuration();
        float d2 = action2.getDuration();

        if (super.initWithDuration(Math.max(d1, d2))) {
            _one = action1;
            _two = action2;

            if (d1 > d2) {
                _two = Sequence.createWithTwoActions(getDirector(), action2, DelayTime.create(getDirector(), d1-d2));
            } else if (d1 < d2) {
                _one = Sequence.createWithTwoActions(getDirector(), action1, DelayTime.create(getDirector(), d2-d1));
            }

            ret = true;
        }

        return ret;
    }

    @Override
    public Spawn clone() {
        if (_one!=null && _two!=null) {
            return Spawn.createWithTwoActions(getDirector(), _one.clone(), _two.clone());
        } else {
            return null;
        }
    }

    @Override
    public void startWithTarget(SMView target) {
        if (target==null) {
            return;
        }

        if (_one==null && _two==null) {
            return;
        }

        super.startWithTarget(target);
        _one.startWithTarget(target);
        _two.startWithTarget(target);
    }

    @Override
    public void stop() {
        if (_one!=null) {
            _one.stop();
        }

        if (_two!=null) {
            _two.stop();
        }

        super.stop();
    }

    @Override
    public void update(float t) {
        if (_one!=null) {
            _one.update(t);
        }
        if (_two!=null) {
            _two.update(t);
        }
    }

    @Override
    public Spawn reverse() {
        if (_one!=null && _two!=null) {
            return Spawn.createWithTwoActions(getDirector(), _one.reverse(), _two.reverse());
        }

        return null;
    }

    protected FiniteTimeAction _one = null;
    protected FiniteTimeAction _two = null;

}
