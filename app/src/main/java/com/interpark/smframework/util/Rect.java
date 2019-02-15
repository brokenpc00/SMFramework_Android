package com.interpark.smframework.util;

public class Rect implements Cloneable {
    public Rect(float x, float y, float width, float height) {
        origin.x = x;
        origin.y = y;
        size.width = width;
        size.height = height;
    }

    public Rect(Vec2 pos, Size dimension) {
        this(pos.x, pos.y, dimension.width, dimension.height);
    }

    public Rect(Rect rect) {
        this(rect.origin, rect.size);
    }

    public void setRect(float x, float y, float width, float height) {
        origin.x = x;
        origin.y = y;
        size.width = width;
        size.height = height;
    }

    public void setRect(Rect rect) {
        setRect(rect.origin, rect.size);
    }

    public void setRect(Vec2 origin, Size size) {
        this.origin.set(origin);
        this.size.set(size);
    }

    public boolean equals(final Rect rect) {
        return (this.origin.equals(rect.origin) && this.size.equals(rect.size));
    }

    public float getMaxX() {
        return origin.x + size.width;
    }

    public float getMidX() {
        return origin.x + size.width/2;
    }

    public float getMinX() {
        return origin.x;
    }

    public float getMaxY() {
        return origin.y + size.height;
    }

    public float getMidY() {
        return origin.y + size.height/2;
    }

    public float getMinY() {
        return origin.y;
    }

    public boolean containsPoint(Vec2 point) {
        boolean bRet = false;
        if (point.x >= getMinX() && point.x <= getMaxX() && point.y >= getMinY() && point.y <= getMaxY())
        {
            bRet = true;
        }

        return bRet;
    }

    public boolean intersectsRect(Rect rect) {
        return !(     getMaxX() < rect.getMinX() ||
                rect.getMaxX() <      getMinX() ||
                getMaxY() < rect.getMinY() ||
                rect.getMaxY() <      getMinY());
    }

    public boolean intersectsCircle(Vec2 center, float radius) {

        Vec2 rectangleCenter = new Vec2((origin.x + size.width / 2), (origin.y + size.height / 2));

        float w = size.width / 2;
        float h = size.height / 2;

        float dx = Math.abs(center.x - rectangleCenter.x);
        float dy = Math.abs(center.y - rectangleCenter.y);

        if (dx > (radius + w) || dy > (radius + h))
        {
            return false;
        }

        Vec2 circleDistance = new Vec2(Math.abs(center.x - origin.x - w), Math.abs(center.y - origin.y - h));

        if (circleDistance.x <= (w))
        {
            return true;
        }

        if (circleDistance.y <= (h))
        {
            return true;
        }

        float cornerDistanceSq = (float)(Math.pow(circleDistance.x - w, 2) + Math.pow(circleDistance.y - h, 2));

        return (cornerDistanceSq <= (Math.pow(radius, 2)));
    }

    public void merge(Rect rect) {
        float minX = Math.min(getMinX(), rect.getMinX());
        float minY = Math.min(getMinY(), rect.getMinY());
        float maxX = Math.max(getMaxX(), rect.getMaxX());
        float maxY = Math.max(getMaxY(), rect.getMaxY());
        setRect(minX, minY, maxX - minX, maxY - minY);
    }

    public Rect unionWithRect(Rect rect) {
        float thisLeftX = origin.x;
        float thisRightX = origin.x + size.width;
        float thisTopY = origin.y + size.height;
        float thisBottomY = origin.y;

        if (thisRightX < thisLeftX)
        {
            float tmp = thisRightX;
            thisRightX = thisLeftX;
            thisLeftX = tmp;
        }

        if (thisTopY < thisBottomY)
        {
            float tmp = thisTopY;
            thisTopY = thisBottomY;
            thisBottomY = tmp;
        }

        float otherLeftX = rect.origin.x;
        float otherRightX = rect.origin.x + rect.size.width;
        float otherTopY = rect.origin.y + rect.size.height;
        float otherBottomY = rect.origin.y;

        if (otherRightX < otherLeftX)
        {
            float tmp = otherRightX;
            otherRightX = otherLeftX;
            otherLeftX = tmp;
        }

        if (otherTopY < otherBottomY)
        {
            float tmp = otherTopY;
            otherTopY = otherBottomY;
            otherBottomY = tmp;
        }

        float combinedLeftX = Math.min(thisLeftX, otherLeftX);
        float combinedRightX = Math.max(thisRightX, otherRightX);
        float combinedTopY = Math.max(thisTopY, otherTopY);
        float combinedBottomY = Math.min(thisBottomY, otherBottomY);

        return new Rect(combinedLeftX, combinedBottomY, combinedRightX - combinedLeftX, combinedTopY - combinedBottomY);
    }

    public Rect clone() {
        return new Rect(this);
    }

    public static final Rect ZERO = new Rect(0, 0, 0, 0);

    public Vec2 origin;
    public Size size;
}
