package com.interpark.smframework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

// file io util... 뭐. 이것저것..

public class IOUtils {
    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // Does nothing
        }
    }

    public static byte[] arrayToBytes(ArrayList<Byte> array) {


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(array);
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
//
    public static ArrayList<Byte> bytesToArray(byte[] bytes) {

//        try {
//            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
//            try {
//                @SuppressWarnings("unchecked")
//                ArrayList<Byte> array = (ArrayList<Byte>)ois.readObject();
//                return array;
//            } catch (ClassNotFoundException e) {
//                return null;
//            }
//        } catch (IOException e) {
//            return null;
//        }
        try {
            ArrayList<Byte> array = (ArrayList<Byte>)toObject(bytes);
            return array;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }

    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);

            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }

    public static String toString(byte[] bytes) {
        return new String(bytes);
    }

    public static void copy(File src, File dst) throws IOException {

        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);

        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);

        inStream.close();
        outStream.close();
    }

    public static byte[] readFile(String fileName) throws IOException {
        return readFile(new File(fileName));
    }

    public static byte[] readFile(File file) throws IOException {
        if (!file.exists())
            return null;

        FileInputStream is = null;

        try {
            is = new FileInputStream(file);

            FileChannel fileChannel = is.getChannel();
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, fileChannel.size( ));

            byte[] dataBytes = new byte[mappedByteBuffer.remaining()];
            mappedByteBuffer.get(dataBytes);

            return dataBytes;
        } finally {
            closeSilently(is);
        }
    }

    public static void writeFile(byte[] data, String fileName) throws IOException{
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.close();
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1)
                out.write(buffer,0,read);
        }
        out.close();
        return out.toByteArray();
    }
}
