package org.hidetake.blebutton

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCompatDelegate.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction().replace(android.R.id.content, MyPreferenceFragment()).commit()
    }

    class MyPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            setOnPreferenceChangeListenerAndFire(MyPreference.notifications_ble_button_ringtone.name) { preference, value ->
                when (value) {
                    "" -> {
                        preference.summary = resources.getString(R.string.pref_ringtone_silent)
                        true
                    }
                    else -> {
                        val ringtone = RingtoneManager.getRingtone(context, Uri.parse(value.toString()))
                        when (ringtone) {
                            is Ringtone -> {
                                preference.summary = ringtone.getTitle(context)
                                true
                            }
                            else -> false
                        }
                    }
                }
            }

            setOnPreferenceChangeListenerAndFire(MyPreference.device_address.name) { preference, value ->
                when (Regex("""([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})""").matches(value.toString())) {
                    true -> {
                        preference.summary = value.toString()
                        true
                    }
                    else -> false
                }
            }
        }

        fun setOnPreferenceChangeListenerAndFire(key: String, listener: (Preference, Any) -> Boolean) {
            val preference = findPreference(key)
            preference.setOnPreferenceChangeListener(listener)
            listener(preference, preferenceManager.sharedPreferences.getString(key, ""))
        }
    }
}
