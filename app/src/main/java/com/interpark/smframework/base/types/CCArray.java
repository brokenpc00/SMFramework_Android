package com.interpark.smframework.base.types;

import com.interpark.smframework.base.SMView;

import java.util.ArrayList;

public class CCArray {
    public CCArray() {

    }

    public long num;
    public long max;
    ArrayList<Object> arr;

    static CCArray CCArrayNew(long capacity) {
        if (capacity==0) {
            capacity = 7;
        }

        CCArray arr = new CCArray();
        arr.num = 0;
        arr.arr = new ArrayList<Object>();
        arr.max = capacity;
        arr.arr.ensureCapacity((int)capacity);
        return arr;
    }

    public void CCArrayFree() {
        ccArrayRemoveAllObjects();
        arr = null;
    }

    public void ccArrayRemoveAllObjects() {
        while (num>0) {
            Object obj = arr.get((int)--num);
            obj = null;
        }
    }

    public void ccArrayDoubleCapacity() {
        max *= 2;
//        SMView
    }


}
