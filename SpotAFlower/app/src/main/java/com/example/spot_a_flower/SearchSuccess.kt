package com.example.spot_a_flower

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class SearchSuccess : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val names = arrayOf("Lily", "Tulip", "Orchids", "Rose", "Poppy", "Sunflowers", "Iris")
    val myDataset: MutableList<DummyItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_success)

        // my_child_toolbar is defined in the layout file
        setSupportActionBar(findViewById(R.id.toolbar))

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        for (i in 1..25) {
            val name = names[(Math.random() * names.size).toInt()]
            val detail = (Math.random() * 100).toInt()
            //val icon = R.drawable.logo
            val description = getString(R.string.description)

            myDataset.add(DummyItem(name, detail, description, false))
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = MyFlowersRecyclerViewAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.flower_list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        menu.findItem(R.id.gallery).isVisible = false
        return true
    }

    data class DummyItem(
        val name: String,
        val detail: Int,
        val description: String,
        val isSaved: Boolean
    ) {
        override fun toString(): String = "$name: $detail% Probability\ndescription"
    }
}
