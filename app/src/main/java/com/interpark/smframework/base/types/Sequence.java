package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;

import java.util.ArrayList;

public class Sequence extends ActionInterval {
    public Sequence(IDirector director) {
        super(director);
        _actions[0] = null;
        _actions[1] = null;
    }

    public static Sequence create(IDirector director, FiniteTimeAction... actions) {
        assert (actions!=null);

        Sequence seq = null;

        FiniteTimeAction now = null;
        FiniteTimeAction action1 = null;
        FiniteTimeAction prev = action1 = actions[0];
        int currentIndex = 0;
        if (actions.length==1) {
            // only one actoin
            seq = createWithTwoActions(director, prev, ExtraAction.create(director));
            return seq;
        } else {
            for (int i=1; i<actions.length-1; ++i) {
                now = actions[i];
                prev = createWithTwoActions(director, prev, now);
            }
            return (Sequence)prev;
        }
    }

    public static Sequence create(IDirector director, ArrayList<FiniteTimeAction> actions) {
        assert (actions!=null);

        Sequence seq = null;

        FiniteTimeAction now = null;
        FiniteTimeAction action1 = null;
        FiniteTimeAction prev = action1 = actions.get(0);
        int currentIndex = 0;
        if (actions.size()==1) {
            // only one actoin
            seq = createWithTwoActions(director, prev, ExtraAction.create(director));
            return seq;
        } else {
            for (int i=1; i<actions.size()-1; ++i) {
                now = actions.get(i);
                prev = createWithTwoActions(director, prev, now);
            }
            return (Sequence)prev;
        }
    }

    public static Sequence createWithTwoActions(IDirector director, FiniteTimeAction action1, FiniteTimeAction action2) {
        Sequence seq = new Sequence(director);
        if (seq!=null && seq.initWithTwoActions(action1, action2)) {
            return seq;
        }
        return null;
    }

    @Override
    public Sequence clone() {
        if (_actions[0]!=null && _actions[1]!=null) {
            return Sequence.create(getDirector(), _actions[0], _actions[1], null);
        } else {
            return null;
        }
    }

    @Override
    public Sequence reverse() {
        if (_actions[0]!=null && _actions[1]!=null) {
            return Sequence.createWithTwoActions(getDirector(), _actions[1], _actions[0]);
        } else {
            return null;
        }
    }


    @Override
    public void startWithTarget(SMView target) {
        if (target==null) {
            return;
        }

        if (_actions[0]==null || _actions[1]==null) {
            return;
        }

        if (_duration > EPSILON) {
            _split = _actions[0].getDuration() > EPSILON ? _actions[0].getDuration() / _duration : 0;
        }

        super.startWithTarget(target);
        _last = -1;
    }

    @Override
    public void stop() {
        if (_last != -1 && _actions[_last]!=null) {
            _actions[_last].stop();
        }

        super.stop();
    }

    @Override
    public boolean isDone() {
        return super.isDone();
    }

    @Override
    public void update(float t) {
        int found = 0;
        float new_t = 0;

        if (t<_split) {
            // _action[0]
            found = 0;
            if (_split!=0) {
                new_t = t / _split;
            } else {
                new_t = t;
            }
        } else {
            // _action[1]
            found = 1;
            if (_split==1) {
                new_t = 1;
            } else {
                new_t = (t-_split) / (1-_split);
            }
        }

        if (found==1) {
            if (_last==-1) {
                _actions[0].startWithTarget(_target);
                _actions[0].update(1.0f);
                _actions[0].stop();
            } else if (_last==0) {
                _actions[0].update(1.0f);
                _actions[0].stop();
            }
        } else if (found==0 && _last==1) {
            _actions[1].update(0);
            _actions[1].stop();
        }

        if (found==_last && _actions[found].isDone()) {
            return;
        }

        if (found!=_last) {
            _actions[found].startWithTarget(_target);
        }

        _actions[found].update(new_t);
        _last = found;
    }

    protected boolean initWithTwoActions(FiniteTimeAction action1, FiniteTimeAction action2) {

        assert (action1!=null);
        assert (action2==null);

        if (action1==null || action2==null) {
            return false;
        }

        float d = action1.getDuration() + action2.getDuration();
        super.initWithDuration(d);

        _actions[0] = action1;
        _actions[1] = action2;

        return true;
    }

    protected FiniteTimeAction[] _actions = new FiniteTimeAction[2];
    protected float _split;
    protected int _last;

}
