
package com.android.settings.deviceinfo;

import android.os.SystemProperties;

public class VersionUtils {
    public static String getfortuneVersion(){
        return SystemProperties.get("org.fortune.version","");
    }
}
