package com.interpark.smframework.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Locale;
import java.util.UUID;

public class DeviceUtils {
    public static final String TAG = DeviceUtils.class.getSimpleName();

    /**
     * 해당 APP의 version을 리턴한다. 작성자 : 김관국
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, e.toString());
            return "";
        }
    }

    /**
     * 해당 디바이스의 ID 값을 리턴한다. 작성자 : 김관국
     *
     * @param context
     * @return
     */
    public static String getDeviceID(Context context) {

        if(AppConfig.DEVICE_UUID == null) {

            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (deviceId != null && "9774d56d682e549c".equals(deviceId)) {
                // Android 2.2 bug
                deviceId = null;
            }

            if (deviceId == null) {
                TelephonyManager telManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
//                deviceId = telManager.getDeviceId();
                deviceId = "";
            }

            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString();
            }

            AppConfig.DEVICE_UUID = deviceId;
        }

        return AppConfig.DEVICE_UUID;
    }


    /**
     * 단말 국가코드 가져오기 ISO 3166-1 alpha-3 세자리 (ex: KOR, JPN)
     * TelephonyManager 를 통해 가져오고 실패 할 경우
     * 단말에 설정된 국가 코드 가져오기
     * @param context
     * @return
     */
    public static String getCountryCode(Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // 현재 등록된 망 사업자의 MCC(Mobile Country Code)에 대한 ISO 국가코드 반환
//        Log.d(TAG, "getNETWORKCountryIso :" + tm.getNetworkCountryIso());
//        Log.d(TAG, "getSimCountryIso :" + tm.getSimCountryIso());

        String ISO2 = tm.getSimCountryIso(); // Sim 국가코드 ISO 2 kr, jp

        if (!TextUtils.isEmpty(ISO2)) {
            return new Locale("", ISO2).getISO3Country();
        } else {
            return Locale.getDefault().getISO3Language().toUpperCase();
        }

    }
}
