package com.interpark.smframework.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class PrefManager {
    private static PrefManager gInstance = null;

    private SharedPreferences pref ;
    private SharedPreferences.Editor mEditor;

    private static final String PREF_NAME = "fituin_pref";

    public static final String APP_VERSION_CODE = "app_version_code";
    public static final String PUSH_KEY = "push_key";
    public static final String PUSH_AGREE = "push_agree";

    public static final String PUSH_KEY_UPDATE = "push_key_update";

    public static final String DEVICE_SEQ = "device_seq";
    public static final String DEVICE_REGDATE = "device_regdate";
    public static final String MEM_REGION_ID = "mem_region_id";
    public static final String DEVICE_TOKEN = "device_token";
    public static final String MEM_NO = "mem_no";
    public static final String AUTH_TYPE = "auth_type";

    public static final String FACEBOOK_TOKEN = "facebook_token";
    public static final String FACEBOOK_NAME = "facebook_name";
    public static final String FACEBOOK_ID = "facebook_id";

    public static final String QQ_TOKEN = "qq_token";
    public static final String QQ_ID = "qq_id";
    public static final String QQ_NAME = "qq_id";

    public static final String WECHAT_REFRESH_TOKEN = "wechat_token";
    public static final String WECHAT_TOKEN = "wechat_token";
    public static final String WECHAT_ID = "wechat_id";
    public static final String WECHAT_NAME = "wechat_name";

    public static final String MEM_PROFILE_IMG = "mem_profile_img";
    public static final String MEM_GENDER = "mem_gender";

    public static final String SETTINGS_SKIP_INTRO = "settings_skip_intro";
    public static final String FITTING_MODEL_SEQ = "fitting_model_seq";
    public static final String MODEL_EXPERIENCE_START = "model_experience_start";

    public static final String CAMERA_ID = "camera_id";
    public static final String CAMERA_FLASH = "camera_flash";
    public static final String PHOTOBOX_VIEWTYPE = "photobox_view_type";

    public static PrefManager getInstance(Context context) {
        if(gInstance == null) {
            gInstance = new PrefManager(context);
        }

        return gInstance;
    }

    private PrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        mEditor = pref.edit();
    }

    public void putInt(String keyName, int value){
        mEditor.putInt(keyName, value);
        mEditor.apply();
    }

    public void putString(String keyName,String value){
        mEditor.putString(keyName, value);
        mEditor.apply();
    }

    public void putBoolean(String keyName, boolean value){
        mEditor.putBoolean(keyName, value);
        mEditor.apply();
    }

    public void putLong(String keyName, long value) {
        mEditor.putLong(keyName, value);
        mEditor.apply();
    }

    public int getInt(String keyName){
        return pref.getInt(keyName, Integer.MIN_VALUE);
    }

    public int getInt(String keyName, int defValue){
        return pref.getInt(keyName, defValue);
    }

    public String getString(String keyName){
        return pref.getString(keyName, "");
    }

    public String getString(String keyName, String defValue){
        return pref.getString(keyName, defValue);
    }

    public boolean getBoolean(String keyName){
        return pref.getBoolean(keyName, false);
    }

    public boolean getBoolean(String keyName, boolean defValue){
        return pref.getBoolean(keyName, defValue);
    }

    public long getLong(String keyName){
        return pref.getLong(keyName, Long.MIN_VALUE);
    }

    public long getLong(String keyName, long defValue){
        return pref.getLong(keyName, defValue);
    }

    public void removePref(String keyName){
        mEditor.remove(keyName);
        mEditor.apply();
    }

    public void allClear() {
        mEditor.clear().apply();
    }

    public String getMemNo() {
        return getString(MEM_NO);
    }
    public String getDeviceSeq() { return getString(DEVICE_SEQ); }

    public boolean isLogin() {
        return !TextUtils.isEmpty(getString(MEM_NO));
    }

    public String isLoginStr() {
        if (TextUtils.isEmpty(getString(MEM_NO))) {
            return String.valueOf(false);
        } else {
            return String.valueOf(true);
        }
    }
}
