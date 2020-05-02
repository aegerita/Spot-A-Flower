package com.example.spot_a_flower

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class FlowerSearch : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var myDataset: MutableList<Flower>

    // Firebase instance variables
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val names = arrayOf("Lily", "Tulip", "Orchids", "Rose", "Poppy", "Sunflowers", "Iris")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get user database
        database = Firebase.database.reference
        mFirebaseAuth = FirebaseAuth.getInstance()

        // change flower dataset according to where the user clicked from (saved, history or search)
        val scenario = intent.getStringExtra("Parent")

        // replaced the neural network with random number generator, for now
        val constant: Int = if (Math.random() < 0.9) {
            3
        } else
            0

        myDataset = ArrayList()// if dataset empty, all goes to fail page

        when (scenario) {
            getString(R.string.search) -> {
                for (i in 1..constant) {
                    // making up variables, for now TODO neural network
                    val detail: String = (Math.random() * 100).toInt().toString() + "% Probability"
                    val name: String = names[(Math.random() * names.size).toInt()]

                    myDataset.add(Flower(name, detail))

                    // save the flower to history when search if the user choose so
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                    if (sharedPreferences.getString(
                            getString(R.string.add_history),
                            "search"
                        ) == "search"
                        || !sharedPreferences.getBoolean("openWiki", false)
                    ) {
                        val timestamp = System.currentTimeMillis()
                        mFirebaseAuth.currentUser?.uid?.let {
                            database.child("users").child(it).child("history")
                                .child(name).setValue(timestamp)
                        }
                    }
                }
            }
            getString(R.string.history) -> {
                // TODO read history, turn to flower, and add to dataset
                val date = Date()
                val sdf = SimpleDateFormat("hh:mm:ss MM/dd", Locale.CANADA)
                val detail = sdf.format(date)
            }
            getString(R.string.saved) -> {
                // TODO read saved, get info from history, turn to flower, add to dataset
            }
        }

        // set up user interface
        if (myDataset.size != 0) {
            // if dataset not empty, all use search success
            setContentView(R.layout.activity_search_success)
            viewManager = LinearLayoutManager(this)
            viewAdapter = RecyclerViewAdapter(this, myDataset)

            recyclerView = findViewById<RecyclerView>(R.id.flower_list).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        } else {
            // if dataset empty, all goes to fail page
            setContentView(R.layout.activity_search_failed)
            findViewById<TextView>(R.id.failText).text =
                when (scenario) {
                    getString(R.string.history) -> {
                        findViewById<ImageView>(R.id.failImage)
                            .setImageResource(android.R.drawable.ic_menu_myplaces)
                        getString(R.string.fail_history_text)
                    }
                    getString(R.string.saved) -> {
                        findViewById<ImageView>(R.id.failImage)
                            .setImageResource(android.R.drawable.ic_menu_myplaces)
                        getString(R.string.fail_save_text)
                    }
                    else -> getString(R.string.fail_search_text)
                }
        }
        // set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = scenario
    }

    // add delete history in menu when in history page
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        menu.findItem(R.id.gallery).isVisible = false
        if (intent.getStringExtra("Parent") == getString(R.string.history))
            menu.findItem(R.id.delete_history).isVisible = true
        return true
    }

    // delete user history
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_history -> {
                // alarm dialog, warn the user
                AlertDialog.Builder(this)
                    .setTitle("Delete History")
                    .setMessage(
                        "Are you sure to delete user history? " +
                                "Your history can not be restored. "
                    )
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        // delete user history in database
                        mFirebaseAuth.currentUser?.uid?.let {
                            database.child("users").child(it).child("history").removeValue()
                        }
                        // go back to failing page
                        setContentView(R.layout.activity_search_failed)
                        findViewById<ImageView>(R.id.failImage)
                            .setImageResource(android.R.drawable.ic_menu_myplaces)
                        findViewById<TextView>(R.id.failText).text =
                            getString(R.string.fail_history_text)
                        // set up toolbar
                        setSupportActionBar(findViewById(R.id.toolbar))
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        supportActionBar?.title = getString(R.string.history)
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
