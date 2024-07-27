/*
 * Copyright (C) 2024 FortuneOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.aboutphone

import android.content.Context
import android.widget.TextView
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.utils.FortunePreferenceUtils
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference

class FortuneInfoPreferenceController(context: Context) : AbstractPreferenceController(context) {

    companion object {
        private const val KEY_FORTUNE_INFO = "fortune_info"
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val fortuneInfoPreference = screen.findPreference<LayoutPreference>(KEY_FORTUNE_INFO)

        fortuneInfoPreference?.let {
            val revision = it.findViewById<TextView>(R.id.revision_summary)
            val codename = it.findViewById<TextView>(R.id.codename_summary)
            val buildtype = it.findViewById<TextView>(R.id.buildtype_summary)

            if (revision != null && codename != null && buildtype != null) {
                val context = it.context

                revision.text = FortunePreferenceUtils.getRevision(context)
                codename.text = FortunePreferenceUtils.getCodename(context)
                buildtype.text = FortunePreferenceUtils.getBuildType(context)
            }
        }
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun getPreferenceKey(): String {
        return KEY_FORTUNE_INFO
    }
}
