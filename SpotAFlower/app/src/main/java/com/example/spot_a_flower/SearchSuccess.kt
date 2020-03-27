package com.example.spot_a_flower

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity

class SearchSuccess : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_success)

        // my_child_toolbar is defined in the layout file
        setSupportActionBar(findViewById(R.id.toolbar))

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        menu.findItem(R.id.gallery).isVisible = false
        menu.findItem(R.id.falseGallery).isVisible = false
        return true
    }
}
