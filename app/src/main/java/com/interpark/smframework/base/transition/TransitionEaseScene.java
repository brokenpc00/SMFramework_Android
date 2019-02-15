package com.interpark.smframework.base.transition;

import com.interpark.smframework.base.types.ActionInterval;

public interface TransitionEaseScene {
    ActionInterval easeActionWithAction(ActionInterval action);
}
