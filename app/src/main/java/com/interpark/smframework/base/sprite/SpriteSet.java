package com.interpark.smframework.base.sprite;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.Texture;

public abstract class SpriteSet {
    protected abstract SpriteInfo[] getInfos();
    protected abstract int[] getTextureResIds();

    private IDirector _director;
    private Sprite[] mSprites;
    private Texture[] mTextures;

    public SpriteSet(IDirector director) {
        _director = director;
        init();
    }

    public static class SpriteInfo {

        public int 		id;
        public int 		w, h;
        public float 	cx, cy;
        public int 		tx, ty;

        public SpriteInfo(int id, int tx, int ty, int w, int h, float cx, float cy) {
            this.id		= id;
            this.w		= w;
            this.h		= h;
            this.cx		= cx;
            this.cy		= cy;
            this.tx		= tx;
            this.ty		= ty;
        }
    }

    protected void init() {
        int numTextures = getTextureResIds().length;
        mTextures = new Texture[numTextures];

        for (int i = 0; i < numTextures; i++) {
            mTextures[i] = _director.getTextureManager().createTextureFromResource(getTextureResIds()[i]);
        }

        SpriteInfo[] infos = getInfos();
        int numSprites = infos.length;
        mSprites = new Sprite[numSprites];

        for (int i = 0; i < numSprites; i++) {
            SpriteInfo s = infos[i];
            // TODO : s.w+1, s.h+1 에서 +1 없애야함.
            Sprite sprite = new Sprite(_director, s.w+1, s.h+1, s.cx, s.cy, s.tx, s.ty, mTextures[s.id]);
            mSprites[i] = sprite;
        }
    }

    public int getNumSprite() {
        return getInfos().length;
    }

    public Sprite get(int spriteId) {
        try {
            return mSprites[spriteId];
        } catch(IndexOutOfBoundsException e) {
            return null;
        }
    }
}
