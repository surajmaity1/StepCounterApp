package com.example.stepcounterapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class CommonUtils {
    static int REQUEST_PERMISSION_SETTING = 2;

    public static void openSettingForPermission(Activity context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivityForResult(intent, CommonUtils.REQUEST_PERMISSION_SETTING);
    }
}
