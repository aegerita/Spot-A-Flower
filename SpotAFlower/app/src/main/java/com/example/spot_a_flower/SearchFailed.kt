package com.example.spot_a_flower

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SearchFailed : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_failed)

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
