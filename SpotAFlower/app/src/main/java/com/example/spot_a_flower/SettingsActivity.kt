package com.example.spot_a_flower

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // replace the frame by teh correct fragment
        val preferenceFrag: PreferenceFragmentCompat =
            if (intent.getStringExtra("Parent") == getString(R.string.setting)) {
                SettingsFragment()
            } else {
                HelpsFragment()
            }
        supportFragmentManager.beginTransaction().replace(R.id.settings, preferenceFrag).commit()

        // set toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("Parent")
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)

            // changes the theme when the user chooses night mode
            val theme: Preference? = findPreference("theme")
            theme!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    when (newValue) {
                        "system" -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                        "light" -> setDefaultNightMode(MODE_NIGHT_NO)
                        "night" -> setDefaultNightMode(MODE_NIGHT_YES)
                    }
                    true
                }

            // set the history preference to default when disabled
            val wiki: Preference? = findPreference("openWiki")
            wiki!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    if (newValue == false)
                        findPreference<ListPreference>("addHistoryWhen")!!.value = "search"
                    true
                }
        }
    }

    class HelpsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.helps_preferences, rootKey)
        }
    }
}