package com.m4399.videoeditor.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.m4399.videoeditor.App;

public class AppUtil {

    public static int getVersionCode() {
        int versionCode = 0;
        try {
            PackageManager pm = App.getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(App.getContext().getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getVersionName() {
        String versionName = "";
        try {
            PackageManager pm = App.getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(App.getContext().getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
