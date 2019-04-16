package com.interpark.smframework.view.Sticker;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.view.SMImageView;

public class RemovableSticker extends SMImageView {
    public RemovableSticker(IDirector director) {
        super(director);
    }

    public boolean isRemovable() {return  true;}
}
