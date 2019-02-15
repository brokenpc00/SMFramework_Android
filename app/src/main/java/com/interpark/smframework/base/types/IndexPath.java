package com.interpark.smframework.base.types;

public class IndexPath {
    public IndexPath() {
        _column = 0;
        _index = 0;
        _section = 0;
    }
    public IndexPath(final IndexPath indexPath) {
        _column = indexPath._column;
        _index = indexPath._index;
        _section = indexPath._section;
    }

    public IndexPath(final int index) {
        _column = 0;
        _index = index;
        _section = 0;
    }
    public IndexPath(final int section, final int index) {
        _column = 0;
        _index = index;
        _section = section;
    }
    public IndexPath(final int section, final int column, final int index) {
        _column = column;
        _index = index;
        _section = section;
    }

    public void set(final IndexPath indexPath) {
        this._column = indexPath._column;
        this._index = indexPath._index;
        this._section = indexPath._section;
    }

    public void set(final int section, final int column, final int index) {
        _column = column;
        _index = index;
        _section = section;
    }

    public int getSection() {return _section;}
    public int getColumn() {return _column;}
    public int getIndex() {return _index;}

    private int _section = 0;
    private int _column = 0;
    private int _index = 0;

    public boolean eqaul(final IndexPath rhs) {
        return _index == rhs._index && _section == rhs._section;
    }

    public boolean notequal(final IndexPath rhs) {
        return _index != rhs._index || _section != rhs._section;
    }

    public boolean lessequal(final IndexPath rhs)  {
        return _index <= rhs._index && _section == rhs._section;
    }

    public boolean greateequal(final IndexPath rhs) {
        return _index >= rhs._index && _section == rhs._section;
    }

    public boolean lessthan(final IndexPath rhs) {
        return _index < rhs._index && _section == rhs._section;
    }

    public boolean greatethan(final IndexPath rhs) {
        return _index > rhs._index && _section == rhs._section;
    }

    public IndexPath inc() {
        ++_index;
        return this;
    }

    public IndexPath dec() {
        --_index;
        return this;
    }

    public IndexPath add(final int value) {
        _index += value;
        return this;
    }

    public IndexPath min(final int value) {
        _index -= value;
        return this;
    }

}
