package com.example.spot_a_flower

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class FlowerSearch : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var myDataset: MutableList<Flower>

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
        when (intent.getStringExtra("Parent")) {
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

        // create flower dataset TODO implement neural network
        myDataset = ArrayList()
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

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            if ((scenario != "history" && scenario != "saved")
                && (sharedPreferences.getString("addHistoryWhen", "search") == "search"
                        || sharedPreferences.getBoolean("openWiki", false))
            ) {
                // TODO save the flower to history when search
                println("save all of these flowers to history")
            }
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
        if (intent.getStringExtra("Parent") == "history")
            menu.findItem(R.id.delete_history).isVisible = true
        return true
    }

    // make a alert dialog when user want to delete history
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_history -> {
                AlertDialog.Builder(this)
                    .setTitle("Delete History")
                    .setMessage(
                        "Are you sure to delete user history? " +
                                "Your history can not be restored. "
                    )
                    // when user confirms, delete history
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        //TODO delete user history in database
                        println("delete user history in database")
                        // change the viewAdapter accordingly
                        myDataset.clear()
                        viewAdapter.notifyDataSetChanged()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    //.setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
