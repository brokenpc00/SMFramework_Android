package com.interpark.smframework.util.security;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class RijndaelSecurity {
    public static final String TAG = RijndaelSecurity.class.getSimpleName();

    public static String secretKey;

    public static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";

    public static byte[] mStrIv = null;

    public static IvParameterSpec getIv() {

        // iv random 16자리 생성
        SecureRandom rnd = new SecureRandom();
        mStrIv = SecurityUtil.md5Byte(rnd.generateSeed(16).toString());


        // iv값 지정
//        try {
//            return new IvParameterSpec(mStrIv.getBytes("UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            Log.e(TAG, e.toString());
//            return null;
//        }

        return new IvParameterSpec(mStrIv);
    }

    /**
     *
     * @param secretKey
     * @return
     */
    public static SecretKeySpec generateKey(String secretKey) {

        // AES 128 키 생성
//        KeyGenerator keygen = null;
//
//        try {
//            keygen = KeyGenerator.getInstance("AES");
//        } catch (NoSuchAlgorithmException e) {
//            Log.e(TAG, e.toString());
//        }
//
//        keygen.init(128);
//        Key key = keygen.generateKey();
//
//        SecretKeySpec keyspec = new SecretKeySpec(key.getEncoded(), "AES");

        // SecretKey 지정
        return new SecretKeySpec(SecurityUtil.md5Byte(secretKey), "AES");
    }

    /**
     * iv + text 암호화 한 후 Base64 인코딩
     *
     * @param secretKey
     * @param text
     * @return
     */
    public static String encrypt(String secretKey, String text) {
        Cipher cipher = null;
        byte[] encrypted;

        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            Log.e(TAG, e.toString());
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(secretKey), getIv());
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            Log.e(TAG, e.toString());
        }

        try {
//            encrypted = cipher.doFinal(padString(text).getBytes("UTF-8"));
            encrypted = cipher.doFinal(SecurityUtil.concatenateByteArrays(mStrIv, text.getBytes("UTF-8")));

            byte[] encodeCode = Base64.encode(encrypted, Base64.DEFAULT);

            return new String(encodeCode, "UTF-8");

        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
//            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static String decrypt(String secretKey, String code) {
        String dec = null;

        Cipher cipher = null;
        byte[] decrypted;

        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            Log.e(TAG, e.toString());
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, generateKey(secretKey), new IvParameterSpec(mStrIv));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            Log.e(TAG, e.toString());
        }

        try {

            byte[] decodeCode = Base64.decode(code, Base64.DEFAULT);
            decrypted = cipher.doFinal(decodeCode);

            dec = SecurityUtil.getByteString(decrypted);

//            dec = new String(decrypted, "UTF-8");
//            dec = dec.replace('\0', ' '); // padding delete

        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
//            Log.e(TAG, e.toString());
        }

        return dec;
    }
}
