/*
 * Copyright (C) 2024 FortuneOS
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import com.android.settings.R
import com.android.settings.core.BasePreferenceController

class FortuneDisplayVersionPreferenceController(
    private val mContext: Context,
    key: String
) : BasePreferenceController(mContext, key) {

    companion object {
        private const val FORTUNE_DISPLAY_VERSION_PROP = "org.fortune.display.version"
    }

    override fun getAvailabilityStatus(): Int {
        return AVAILABLE_UNSEARCHABLE
    }

    override fun getSummary(): CharSequence {
        val display_version = SystemProperties.get(FORTUNE_DISPLAY_VERSION_PROP,
            mContext.getString(R.string.device_info_default))

        return display_version
    }
}
