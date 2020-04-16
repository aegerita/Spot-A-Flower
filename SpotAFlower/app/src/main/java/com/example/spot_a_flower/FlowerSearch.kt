package com.example.spot_a_flower

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
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
        val constant: Int
        when (scenario) {
            "history" -> {
                constant = 5
                supportActionBar?.title = getString(R.string.history)
            }
            "saved" -> {
                constant = 10
                supportActionBar?.title = getString(R.string.saved)
            }
            else -> constant = 15
        }

        // create flower dataset
        val myDataset: MutableList<Flower> = ArrayList()
        for (i in 1..constant) {
            val name = names[(Math.random() * names.size).toInt()]

            val detail = if (scenario == "history" || scenario == "saved") {
                val date = Date((Random().nextDouble() * 60 * 60 * 24 * 365).toLong())
                val sdf = SimpleDateFormat("hh:mm:ss MM/dd", Locale.CANADA)
                sdf.format(date)
            } else {
                (Math.random() * 100).toInt().toString() + "% Probability"
            }

            //val icon = R.drawable.logo
            val description = "                        " + getString(R.string.description)
            val link = "https://en.wikipedia.org/wiki/${name}"
            val isSaved = if (scenario == "saved") true else Math.random() > 0.5

            myDataset.add(Flower(name, detail, description, link, isSaved))
        }

        // call the recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = RecyclerViewAdapter(this, myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.flower_list).apply {
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

    // flower class
    data class Flower(
        val name: String,
        val detail: String,
        val description: String,
        val link: String,
        val isSaved: Boolean
    ) {
        override fun toString(): String = "$name: $detail\ndescription"
    }
}
