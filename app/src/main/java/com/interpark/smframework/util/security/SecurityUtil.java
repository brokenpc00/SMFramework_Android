package com.interpark.smframework.util.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {
    public static final String TAG = SecurityUtil.class.getSimpleName();

    /**
     * 앞 16자리 iv 값 제거
     * @param s
     * @return
     * @throws UnsupportedEncodingException
     */
    static String getByteString(byte[] s) throws UnsupportedEncodingException {
        int startIdx = 16;
        int bytes = s.length - 16;

        return new String(s, startIdx, bytes, "UTF-8");
    }

    static byte[] md5Byte(String securityString) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(securityString.getBytes("UTF-8"));

            return digest.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
//            Log.e(TAG, e.toString());
            return null;
        }
    }

    static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
