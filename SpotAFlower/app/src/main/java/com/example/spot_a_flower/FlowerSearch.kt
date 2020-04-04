package com.example.spot_a_flower

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class FlowerSearch : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val names = arrayOf("Lily", "Tulip", "Orchids", "Rose", "Poppy", "Sunflowers", "Iris")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_success)

        // my_child_toolbar is defined in the layout file
        setSupportActionBar(findViewById(R.id.toolbar))

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // change flower dataset according to where the user clicked from (saved, history or search)
        val scenario = intent.getStringExtra("Parent")
        var constant = 1
        when (scenario) {
            "history" -> {
                constant = 1
            }
            "saved" -> {
                constant = 2
            }
            else -> constant = 3
        }

        val myDataset: MutableList<Flower> = ArrayList()
        for (i in 1..constant) {
            val name = names[(Math.random() * names.size).toInt()]
            val detail: String = (Math.random() * 100).toInt().toString() + "% Probability"
            //val icon = R.drawable.logo
            val description = getString(R.string.description)

            myDataset.add(Flower(name, detail, description, false))
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

    data class Flower(
        val name: String,
        val detail: String,
        val description: String,
        val isSaved: Boolean
    ) {
        override fun toString(): String = "$name: $detail\ndescription"
    }
}
