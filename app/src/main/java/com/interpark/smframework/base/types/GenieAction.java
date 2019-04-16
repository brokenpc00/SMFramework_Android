package com.interpark.smframework.base.types;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.sprite.GridSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ProgGeineEffect2;
import com.interpark.smframework.shader.ProgSprite;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.util.Vec2;

public class GenieAction extends ActionInterval {
    public GenieAction(IDirector director) {
        super(director);
    }

    public static GenieAction create(IDirector director, float duration, Sprite sprite, final Vec2 removeAnchor) {
        GenieAction action = new GenieAction(director);
        action.initWithDuration(duration);
        action._sprite = GridSprite.create(director, sprite);
        action._sprite.setProgramType(ShaderManager.ProgramType.GeineEffect2);
        // now _sprite is grid sprite

        action._removeAnchor.set(removeAnchor);

        return action;
    }

    @Override
    public void startWithTarget(SMView target) {
        super.startWithTarget(target);

        ((GridSprite)_sprite).setGenieAnchor(_removeAnchor);

    }

    @Override
    public void update(float t) {
        if (t<0) t*=0.1f;

        ((GridSprite)_sprite).setGenieProgress((float) SMView.M_PI_2*t);
    }

    private Sprite _sprite = null;
    private Vec2 _removeAnchor = new Vec2();
}
