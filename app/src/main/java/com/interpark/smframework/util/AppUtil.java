package com.interpark.smframework.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.interpark.smframework.util.security.RijndaelSecurity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtil {
    public static final String TAG = AppUtil.class.getSimpleName();

    /**
     * token 생성
     * DEV_UUID : device 고유 아이디, MEM_NO : 멤버번호, REQ_TIME : 현재 타임스탬프
     * json 으로 변환해서 암호화
     *
     * @param context
     * @return
     */
    public static String getToken(Context context) {
        String token = null;

        String deviceId = DeviceUtils.getDeviceID(context);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("DEV_UUID", deviceId);
            jsonObject.put("MEM_NO", PrefManager.getInstance(context).getMemNo());
            jsonObject.put("REQ_TIME", System.currentTimeMillis() / 1000 );

            token = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // deviceId + device등록 시 시간 secretKey 생성
        String secretKey = deviceId + PrefManager.getInstance(context).getString(PrefManager.DEVICE_REGDATE);

        return RijndaelSecurity.encrypt(secretKey, token);
    }

    /**
     * 올바른 EMAIL 형식인지 검사
     *
     * @param email 검사할 email.
     * @return <code>true</code> == valid email address. otherwise
     *         <code>false</code>.
     */
    public static boolean isValidEmailAddress(String email) {
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);
        return m.find();
    }

    /**
     * volley param에 null은 에러 ""으로 변경
     * @param map
     * @return
     */
    public static Map<String, String> checkParams(Map<String, String> map) {
        for (Map.Entry<String, String> pairs : map.entrySet()) {
            if (pairs.getValue() == null) {
                map.put(pairs.getKey(), "");
            }
        }
        return map;
    }


    /**
     * UserAgent 설정
     * fitUin/서비스타입 (앱버전;API버전;OS버전;디바이스정보)
     *
     * @param context
     * @return
     */
    public static String getUserAgent(Context context) {

//        if(TextUtils.isEmpty(AppConfig.USER_AGENT)) {
//
//            String separator = ";";
//
//            StringBuilder myAgent = new StringBuilder();
//
//            myAgent.append("fitUin"); // 앱 이름
//            myAgent.append("/");
//            myAgent.append(AppConfig.REGION.toUpperCase()); // 서비스 타입(국가)
//
//            myAgent.append(" (");
//
//            myAgent.append(DeviceUtils.getAppVersionName(context)); // 앱 버전
//            myAgent.append(separator);
//            myAgent.append(AppConfig.SINGLE.substring(1).toUpperCase()); // API 버전
//            myAgent.append(separator);
//            myAgent.append("Android ").append(Build.VERSION.RELEASE); // Android OS 버전
//            myAgent.append(separator);
//            myAgent.append(Build.MODEL); // 디바이스 정보
//            myAgent.append(separator);
//
//            myAgent.append(")");
//
//            AppConfig.USER_AGENT = myAgent.toString();
//
//            // ex) fitUin/CN (1.0;S2;Android 5.0.1;LG-F240K Build/LRX21Y)
//        }

//        return AppConfig.USER_AGENT;
        return "";
    }

    /**
     * 앱 versionCode 가져오기
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * drawable 리소스를 Bitmap으로 가져오기
     * @param id R.drawable id
     * @return Bitmap
     */
    public static Bitmap drawableToBitmap(Context context, int id) {
        BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(id);
        return drawable.getBitmap();
    }

    /**
     * Action type 분류
     * Shop Banner click action 시
     * Notification click action 시
     *   EXHIBIT : 상품전시 (상품 번호 리턴)
     *   SEARCH : 상품검색(소팅)
     *   INTERPOP : 내부팝업
     *   EXTERPOP : 외부팝업
     *   APPSTORE : 앱스토어 이동
     *   WEAR : 입어보기
     *
     * @param action type string
     * @return
     */
    public static int getActionType(String action) {
//        if (!TextUtils.isEmpty(action)) {
//            if ("EXHIBIT".equalsIgnoreCase(action)) {
//                return AppConfig.ACTION_EXHIBIT;
//            } else if ("SEARCH".equalsIgnoreCase(action)) {
//                return AppConfig.ACTION_SEARCH;
//            } else if ("INTERPOP".equalsIgnoreCase(action)) {
//                return AppConfig.ACTION_INTERPOP;
//            } else if ("EXTERPOP".equalsIgnoreCase(action)) {
//                return AppConfig.ACTION_EXTERPOP;
//            } else if ("APPSTORE".equalsIgnoreCase(action)) {
//                return AppConfig.ACTION_APPSTORE;
//            } else if ("WEAR".equalsIgnoreCase(action)) {
//                return AppConfig.ACTION_WEAR;
//            }
//        }
        return -1;
    }

    public static String getBooleanStr(boolean chk) {
        return chk ? "Y" : "N";
    }

    public static File getExternalFilesDir(Context context, String type) {
        File dir = context.getExternalFilesDir(type);
        if (dir == null) {
            final String path = "/Android/data/" + context.getPackageName() + "/files/" + type;
            dir = new File(Environment.getExternalStorageDirectory().getPath() + path);
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
        }
        return dir;
    }

    public static File getExternalPicturesDir(Context context, String type) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + type);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }
}
