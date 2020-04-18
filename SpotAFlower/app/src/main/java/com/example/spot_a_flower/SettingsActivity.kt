package com.example.spot_a_flower

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val preferenceFrag: PreferenceFragmentCompat =
            if (intent.getStringExtra("Parent") == "setting") {
                SettingsFragment()
            } else {
                HelpsFragment()
            }
        supportFragmentManager.beginTransaction().replace(R.id.settings, preferenceFrag).commit()

        // set toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title =
            if (intent.getStringExtra("Parent") == "setting") {
                getString(R.string.setting)
            } else {
                getString(R.string.helps)
            }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        }
    }

    class HelpsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.helps_preferences, rootKey)
        }
    }
}