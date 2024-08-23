/*
 * Copyright (C) 2024 FortuneOS
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class FortuneVersionPreferenceController extends BasePreferenceController {

    private static final String FORTUNE_VERSION_PROP = "org.fortune.revision";
    private static final String FORTUNE_CODENAME_PROP = "org.fortune.codename";
    private static final String FORTUNE_DEVICE_PROP = "org.fortune.device";
    private static final String FORTUNE_BUILDTYPE_PROP = "org.fortune.build.type";

    private final Context mContext;

    public FortuneVersionPreferenceController(Context context, String key) {
        super(context, key);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE_UNSEARCHABLE;
    }

    @Override
    public CharSequence getSummary() {
        String version = SystemProperties.get(FORTUNE_VERSION_PROP,
                mContext.getString(R.string.device_info_default));
        String codename = SystemProperties.get(FORTUNE_CODENAME_PROP,
                mContext.getString(R.string.device_info_default));
        String device = SystemProperties.get(FORTUNE_DEVICE_PROP,
                mContext.getString(R.string.device_info_default));
        String buildType = SystemProperties.get(FORTUNE_BUILDTYPE_PROP,
                mContext.getString(R.string.device_info_default));

        // Example: 1.0 | Ulysses | topaz | COMMUNITY
        return version + " | " + codename + " | " + device + " | " + buildType;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            String baseUrl = mContext.getString(R.string.fortune_url);
            String version = SystemProperties.get(FORTUNE_VERSION_PROP, "");
            Uri uri = Uri.parse(baseUrl + version);

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
            return true;
        }
        return super.handlePreferenceTreeClick(preference);
    }
}
