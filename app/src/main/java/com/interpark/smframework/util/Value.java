package com.interpark.smframework.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class Value {
    public static final Value Null = new Value();

    public static class ValueList extends ArrayList<Value> {
        public ValueList() {
            super();
        }
        public ValueList(ValueList list) {
            super(list);
        }
    }

    public static class ValueMap extends HashMap<String, Value> {
        public ValueMap() {
            super();
        }
        public ValueMap(ValueMap map) {
            super(map);
        }
    }

    public static class ValueMapIntKey extends HashMap<Integer, Value> {
        public ValueMapIntKey() {

        }
        public ValueMapIntKey(ValueMapIntKey intkeyMap) {
            super(intkeyMap);
        }
    }

    public Value() {
        _type = Type.NONE;
        _field.clear();;
    }

    public Value(char v) {
        this();
        _type = Type.BYTE;
        _field.byteVal = v;
    }

    public Value(int v) {
        this();
        _type = Type.INTEGER;
        _field.intVal = v;
    }

    public Value(float v) {
        this();
        _type = Type.FLOAT;
        _field.floatVal = v;
    }

    public Value(double v) {
        this();
        _type = Type.DOUBLE;
        _field.doubleVal = v;
    }

    public Value(long v) {
        this();
        _type = Type.LONG;
        _field.longVal = v;
    }

    public Value(String v) {
        this();
        _type = Type.STRING;
        _field.stringVal = v;
    }

    public Value(ValueList v) {
        this();
        _type = Type.LIST;
        _field.listVal = v;
    }

    public Value(ValueMap v) {
        this();
        _type = Type.MAP;
        _field.mapVal = v;
    }

    public Value(ValueMapIntKey v) {
        this();
        _type = Type.INT_KEY_MAP;
        _field.intKeyVal = v;
    }

    public Value(Value other) {
        this(other, false);
    }
    public Value(Value other, boolean move) {
        this();
        _field.set(other._field);
        if (move) {
            other._field.clear();;
            other._type = Type.NONE;
        }
    }


    public Value set(char v) {
        reset(Type.BYTE)._field.byteVal = v;
        return this;
    }

    public Value set(int v) {
        reset(Type.INTEGER)._field.intVal = v;
        return this;
    }

    public Value set(float v) {
        reset(Type.FLOAT)._field.floatVal = v;
        return this;
    }

    public Value set(double v) {
        reset(Type.DOUBLE)._field.doubleVal = v;
        return this;
    }

    public Value set(long v) {
        reset(Type.LONG)._field.longVal = v;
        return this;
    }

    public Value set(String v) {
        reset(Type.STRING)._field.stringVal = v;
        return this;
    }

    public Value set(ValueList v) {
        reset(Type.LIST)._field.listVal = new ValueList(v);
        return this;
    }

    public Value set(ValueMap v) {
        reset(Type.MAP)._field.mapVal = new ValueMap(v);
        return this;
    }

    public Value set(ValueMapIntKey v) {
        reset(Type.INT_KEY_MAP)._field.intKeyVal = new ValueMapIntKey(v);
        return this;
    }

    public Value set(Value other) {
        if (!this.equal(other)) {
            reset(other._type);

            switch (other._type) {
                case BYTE:
                {
                    _field.byteVal = other._field.byteVal;
                }
                break;
                case INTEGER:
                {
                    _field.intVal = other._field.intVal;
                }
                break;
                case LONG:
                {
                    _field.longVal = other._field.longVal;
                }
                break;
                case FLOAT:
                {
                    _field.floatVal = other._field.floatVal;
                }
                break;
                case DOUBLE:
                {
                    _field.doubleVal = other._field.doubleVal;
                }
                break;
                case BOOLEAN:
                {
                    _field.booleanVal = other._field.booleanVal;
                }
                break;
                case STRING:
                {
                    _field.stringVal = other._field.stringVal;
                }
                break;
                case LIST:
                {
                    _field.listVal = new ValueList(other._field.listVal);
                }
                break;
                case MAP:
                {
                    _field.mapVal = new ValueMap(other._field.mapVal);
                }
                break;
                case INT_KEY_MAP:
                {
                    _field.intKeyVal = new ValueMapIntKey(other._field.intKeyVal);
                }
                break;
                default:break;
            }
        }

        return this;
    }

    public boolean equal(final Value v) {
        if (this.equals(v)) return true;

        if (v._type!=this._type) return false;

        if (this.isNull()) return true;

        switch (_type) {
            case BYTE: return v._field.byteVal==this._field.byteVal;
            case INTEGER: return v._field.intVal==this._field.intVal;
            case LONG: return v._field.longVal==this._field.longVal;
            case FLOAT: return v._field.floatVal==this._field.floatVal;
            case DOUBLE: return v._field.doubleVal==this._field.doubleVal;
            case STRING: return v._field.stringVal.equals(this._field.stringVal);
            case LIST:
            {
                ValueList v1 = this._field.listVal;
                ValueList v2 = v._field.listVal;
                int size = v1.size();
                if (size==v2.size()) {
                    for (int i=0; i<size; i++) {
                        if (!v1.get(i).equals(v2.get(i))) return false;
                    }
                    return true;
                }

                return false;
            }
            case MAP:
            {
                ValueMap m1 = this._field.mapVal;
                ValueMap m2 = v._field.mapVal;
                Set<String> keys = m1.keySet();
                for (String key : keys) {
                    Value value = m2.get(key);
                    if (value==null || !value.equal(m2.get(key))) return false;
                }
                return true;
            }
            case INT_KEY_MAP:
            {
                ValueMapIntKey m1 = this._field.intKeyVal;
                ValueMapIntKey m2 = v._field.intKeyVal;
                Set<Integer> keys = m1.keySet();
                for (Integer key : keys) {
                    Value value = m2.get(key);
                    if (value==null || !value.equal(m2.get(key))) return false;
                }
                return true;
            }
        }

        return false;
    }

    public enum Type {
        NONE,
        BYTE,
        INTEGER,
        FLOAT,
        LONG,
        DOUBLE,
        BOOLEAN,
        STRING,
        LIST,
        MAP,
        INT_KEY_MAP
    }

    public boolean isNull() {return _type==Type.NONE;}

    public Type getType() {return _type;}

    protected void clear() {
        switch (_type) {
            case BYTE: _field.byteVal='0';break;
            case INTEGER: _field.intVal=0;break;
            case LONG: _field.longVal=0;break;
            case FLOAT: _field.floatVal=0;break;
            case DOUBLE: _field.doubleVal=0;break;
            case BOOLEAN: _field.booleanVal=false;break;
            case STRING: _field.stringVal="";break;
            case LIST: _field.listVal=null;break;
            case MAP: _field.mapVal=null;break;
            case INT_KEY_MAP: _field.intKeyVal=null;break;
        }
    }

    protected Value reset(Type type) {
        if (_type==type) {
            return this;
        }

        clear();

        switch (type) {
            case STRING:
            {
                _field.stringVal = "";
            }
            break;
            case LIST:
            {
                _field.listVal = new ValueList();
            }
            break;
            case MAP:
            {
                _field.mapVal = new ValueMap();
            }
            break;
            case INT_KEY_MAP:
            {
                _field.intKeyVal = new ValueMapIntKey();
            }
            break;
        }

        _type = type;

        return this;
    }

    public char getByte() {
        return _field.byteVal;
    }

    public int getInt() {
        return _field.intVal;
    }

    public long getLong() {
        return _field.longVal;
    }

    public float getFloat() {
        return _field.floatVal;
    }

    public double getDouble() {
        return _field.doubleVal;
    }

    public String getString() {
        return _field.stringVal;
    }

    public ValueList getList() {
        return _field.listVal;
    }

    public ValueMap getMap() {
        return _field.mapVal;
    }

    public ValueMapIntKey getMapIntKey() {
        return _field.intKeyVal;
    }

    protected Field _field = new Field();

    public class Field {
        public Field() {
            clear();
        }

        public void set(Field f) {
            this.byteVal = f.byteVal;
            this.intVal = f.intVal;
            this.longVal = f.longVal;
            this.floatVal = f.floatVal;
            this.doubleVal = f.doubleVal;
            this.booleanVal = f.booleanVal;
            this.stringVal = f.stringVal;
            this.listVal = f.listVal;
            this.mapVal = f.mapVal;
            this.intKeyVal = f.intKeyVal;
        }

        public void clear() {
            byteVal = '\0';
            intVal = 0;
            longVal = 0;
            floatVal = 0;
            doubleVal = 0;
            booleanVal = false;
            stringVal = "";
            listVal = null;
            mapVal = null;
            intKeyVal = null;
        }

        public char byteVal;
        public int intVal;
        public long longVal;
        public float floatVal;
        public double doubleVal;
        public boolean booleanVal;
        public String stringVal;
        public ValueList listVal;
        public ValueMap mapVal;
        public ValueMapIntKey intKeyVal;
    }

    private Type _type = Type.NONE;

}
