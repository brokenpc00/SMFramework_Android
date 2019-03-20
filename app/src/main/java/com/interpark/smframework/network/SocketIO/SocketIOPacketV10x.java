package com.interpark.smframework.network.SocketIO;

import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class SocketIOPacketV10x extends SocketIOPacket {

    public SocketIOPacketV10x() {
        _separator = "";
        _endpointseparator = ",";
        _types.add("disconnected");
        _types.add("connected");
        _types.add("heartbeat");
        _types.add("pong");
        _types.add("message");
        _types.add("upgrade");
        _types.add("noop");
        _typesMessage.add("connect");
        _typesMessage.add("disconnect");
        _typesMessage.add("event");
        _typesMessage.add("ack");
        _typesMessage.add("error");
        _typesMessage.add("binarevent");
        _typesMessage.add("binaryack");
    }

    @Override
    public int typeAsNumber() {

        int index = -1;
        for (int i=0; i<_typesMessage.size(); i++) {
            if (_type.equals(_typesMessage.get(i))) {
                index = i;
                break;
            }
        }
        if (index!=-1) {
            index += 40;
        } else {
            index = 0;
            for (int i=0; i<_types.size(); i++) {
                if (_type.equals(_types.get(i))) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    @Override
    public String stringify() {

        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);

        String outS = "";
        try {
            writer.beginObject();
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

        return outS;
  }


    private ArrayList<String> _typesMessage = new ArrayList<>();
}
