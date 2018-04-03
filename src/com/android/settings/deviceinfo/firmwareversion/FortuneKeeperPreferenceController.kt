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

class FortuneKeeperPreferenceController(
    context: Context,
    key: String
) : BasePreferenceController(context, key) {

    companion object {
        private const val TAG = "FortuneKeeperPreferenceController"
        private const val KEEPER_PROP = "org.fortune.keeper"
    }

    override fun getAvailabilityStatus(): Int {
        return AVAILABLE
    }

    override fun getSummary(): CharSequence {
        val keeper = SystemProperties.get(KEEPER_PROP, mContext.getString(R.string.device_info_default))
        return keeper
    }
}
