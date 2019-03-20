package com.interpark.smframework.network.SocketIO;

import android.util.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

public class SocketIOPacket {
    public enum SocketIOVersion {
        V09x,
        V10x
    }

    public SocketIOPacket() {
        _types.add("disconnect");
        _types.add("connect");
        _types.add("heartbeat");
        _types.add("message");
        _types.add("json");
        _types.add("event");
        _types.add("ack");
        _types.add("error");
        _types.add("noop");
    }

    public void initWithType(final String packetType) {
        _type = packetType;
    }
    public void initWithTypeIndex(int index) {
        if (index>=_types.size()) {
            index = 0;
        }
        _type = _types.get(index);
    }

    public String toString() {
        StringBuffer encoded = new StringBuffer("");
        encoded.append(this.typeAsNumber());
        encoded.append(this._separator);

        String pIdL = _pId;
        if (_ack.equals("data")) {
            pIdL += "+";
        }

        if (!_type.equals("ack")) {
            encoded.append(pIdL);
        }
        encoded.append(this._separator);

        if (!_endpoint.equals("/") && !_endpoint.equals("") && !_type.equals("ack") && !_type.equals("heartbeat") && !_type.equals("disconnect")) {
            encoded.append(_endpoint).append(_endpointseparator);
        }
        encoded.append(this._separator);

        if (!_args.isEmpty()) {
            String ackpId = "";
            if (_type.equals("ack")) {
                ackpId += pIdL + "+";
            }
            encoded.append(ackpId).append(this.stringify());
        }

        return encoded.toString();
    }

    public int typeAsNumber() {
        int index = 0;
        for (int i=0; i<_types.size(); i++) {
            if (_type.equals(_types.get(i))) {
                index = i;
                break;
            }
        }

        return index;
    }

    public String typeForIndex(int index) {
        return _types.get(index);
    }


    public void setEndpoint(final String endpoint) {_endpoint = endpoint;}
    public String getEndpoint() {return _endpoint;}

    public void setEvent(final String event) {_name = event;}
    public String getEvent() {return _name;}

    public void addData(final String data) {
        this._args.add(data);
    }
    public ArrayList<String> getData() {return _args;}
    public String stringify() {
        String outS;
        if (_type.equals("message")) {
            outS = _args.get(0);
        } else {
            StringWriter out = new StringWriter();
            JsonWriter writer = new JsonWriter(out);


            try {
                writer.beginObject();
                writer.value("name");
                writer.value(_name);
                writer.value("args");

                writer.beginArray();
                for (int i=0; i<_args.size(); i++) {
                    writer.value(_args.get(i));
                }

                writer.endArray();
                writer.endObject();

                outS = writer.toString();
            } catch (IOException e) {
                outS = "";
            }
        }

        return outS;
    }

    public static SocketIOPacket createPacketWithType(final String type, SocketIOVersion version) {
        SocketIOPacket ret = null;

        switch (version) {
            case V09x:
            {
                ret = new SocketIOPacket();
            }
            break;
            case V10x:
            {
                ret = new SocketIOPacketV10x();
            }
            break;
        }

        ret.initWithType(type);
        return ret;
    }

    public static SocketIOPacket createPacketWithTypeIndex(int type, SocketIOVersion version) {
        SocketIOPacket ret = null;

        switch (version) {
            case V09x:
            {
                ret = new SocketIOPacket();
            }
            break;
            case V10x:
            {
                ret = new SocketIOPacketV10x();
            }
            break;
        }

        ret.initWithTypeIndex(type);
        return ret;
    }


    protected String _pId;
    protected String _ack;
    protected String _name;
    protected ArrayList<String> _args = new ArrayList<>();
    protected String _endpoint;
    protected String _endpointseparator;
    protected String _type;
    protected String _separator;
    protected ArrayList<String> _types = new ArrayList<>();

}
