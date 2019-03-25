package com.interpark.smframework.util.cache;

import java.util.Arrays;

public class MemoryCacheEntry {
    public static final int INIT_ALLOC_SIZE = 50*1024;
    public static MemoryCacheEntry createEntry() {
        return createEntry(null, 0);
    }
    public static MemoryCacheEntry createEntry(byte[] data) {
        return createEntry(data, data.length);
    }
    public static MemoryCacheEntry createEntry(byte[] data, int size) {
        MemoryCacheEntry entry = new MemoryCacheEntry();
        if (size>0) {
            entry._data = Arrays.copyOf(data, size);
        } else {
            entry._data = data;
        }

        entry._size = size;
        entry._capacity = size;

        return entry;
    }

    public MemoryCacheEntry() { }

    public byte[] getData() {
        return _data;
    }

    public int size() {
        return _size;
    }

    public void appendData(byte[] data, int size) {
        if (data==null||size==0) {
            return;
        }

        int oldPos = 0;
        if (_data==null) {
            int newCapacity = Math.max(_size+size, INIT_ALLOC_SIZE);
            _capacity = newCapacity;
            _data = new byte[_capacity];
        } else {
            oldPos = _data.length-1;
            int newCapacity = _capacity;
            if (_size+size>newCapacity) {
                newCapacity = Math.max(_size+size, (int)(_capacity*1.65f));
            }

            if (newCapacity>_capacity) {
                _capacity = newCapacity;
//                byte[] newData = new byte[_capacity];
//                System.arraycopy(data,0,newData,0,data.length);
//                System.arraycopy(data, 0, newCapacity, 0, data.length);

                // ToDo... check plz.
                _data = Arrays.copyOfRange(_data, 0, _capacity);
            }
        }

        System.arraycopy(data, 0, _data, oldPos, data.length);
        _size += size;
    }

    public void shrinkToFit() {
        if (_size!=_capacity && _data!=null) {
            if (_size>0) {
                _data = Arrays.copyOfRange(_data, 0, _size);
            } else {
                _data = null;
            }
            _capacity = _size;
        }
    }

    private byte[] _data = null;
    int _size = 0;
    int _capacity = 0;
}
