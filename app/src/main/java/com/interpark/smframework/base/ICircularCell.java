package com.interpark.smframework.base;

public interface ICircularCell {

    public int getCellIndex();
    public float getCellPosition();
    public String getCellIdentifier();
    public void markDelete();
    public void setCellIndex(int index);
    public void setCellPosition(final float position);
    public void setReuseIdentifier(final String identifier);


    public void setAniSrc(float src);
    public void setAndDst(float dst);
    public void setAniIndex(int index);

    public boolean isDeleted();
    public float getAniSrc();
    public float getAniDst();
    public int getAniIndex();
}
