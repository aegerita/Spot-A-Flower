package com.example.spot_a_flower

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_search_success.*
import java.util.*

class FlowerSearch : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var myDataset: MutableList<Flower> = ArrayList()
    private var viewManager = LinearLayoutManager(this)
    private lateinit var viewAdapter: RecyclerViewAdapter

    // Firebase instance variables
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get user database
        database = Firebase.database.reference
        mFirebaseAuth = FirebaseAuth.getInstance()

        // set up layout
        setContentView(R.layout.activity_search_success)
        viewAdapter = RecyclerViewAdapter(this, myDataset, intent.getStringExtra("Parent"))
        recyclerView = findViewById<RecyclerView>(R.id.flower_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("Parent")

        // if user not signed in and open saved or history, warn
        if (mFirebaseAuth.currentUser == null && (intent.getStringExtra("Parent") == getString(R.string.saved)
                    || intent.getStringExtra("Parent") == getString(R.string.history))
        ) {
            pageEmpty()
        } else {
            progressBar2.isVisible = true
            // make a search bar
            searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewAdapter.filter.filter(newText)
                    return false
                }
            })
            // modify search bar
            val searchIcon = searchBar.findViewById<ImageView>(R.id.search_mag_icon)
            searchIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorTitle))
            val cancelIcon = searchBar.findViewById<ImageView>(R.id.search_close_btn)
            cancelIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorTitle))
            val textView = searchBar.findViewById<TextView>(R.id.search_src_text)
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorTitle))
            textView.setHintTextColor(Color.parseColor("#eeeeee"))

            // visible when scroll down, invisible when scroll up
            var rememberedPosition = viewManager.findFirstVisibleItemPosition()
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    if (newState != SCROLL_STATE_IDLE) {
                        val currentFirstVisible: Int =
                            viewManager.findFirstVisibleItemPosition()
                        if (currentFirstVisible >= rememberedPosition && searchBar.translationY > 100) {
                            // hide search Bar
                            ObjectAnimator.ofFloat(searchBar, "translationY", 0f).apply {
                                duration = 500
                                start()
                            }
                            ObjectAnimator.ofFloat(flower_list, "translationY", 0f).apply {
                                duration = 500
                                start()
                            }
                        } else if (currentFirstVisible < rememberedPosition && searchBar.translationY < 100) {
                            // show search bar
                            ObjectAnimator.ofFloat(searchBar, "translationY", 144f).apply {
                                duration = 500
                                start()
                            }
                            ObjectAnimator.ofFloat(flower_list, "translationY", 132f).apply {
                                duration = 500
                                start()
                            }
                        }
                        rememberedPosition = currentFirstVisible
                    }
                }
            })
        }

        // TODO sorting in toolbar
        // change scenarios depending on parent activity
        when (intent.getStringExtra("Parent")) {
            getString(R.string.search) -> {
                myDataset.add(Flower(intent.getStringExtra("flower1_name"), intent.getStringExtra("flower1_detail")))
                myDataset.add(Flower(intent.getStringExtra("flower2_name"), intent.getStringExtra("flower2_detail")))
                myDataset.add(Flower(intent.getStringExtra("flower3_name"), intent.getStringExtra("flower3_detail")))

                // save the flower to history when search if the user choose so
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                if (sharedPreferences.getBoolean(
                        getString(R.string.store_after_search_key),
                        true
                    )
                ) {
                    mFirebaseAuth.currentUser?.uid?.let {
                        database.child(getString(R.string.fb_users)).child(it)
                            .child(getString(R.string.fb_history))
                            .child(myDataset[0].name).setValue(System.currentTimeMillis())
                    }
                }
                progressBar2.isVisible = false

                if (mFirebaseAuth.currentUser == null) {
                    Toast.makeText(
                        this@FlowerSearch,
                        "Sign up to save flowers to your account!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            getString(R.string.history) -> {
                // read history, turn to flower, and add to dataset
                mFirebaseAuth.currentUser?.uid?.let {
                    database.child(getString(R.string.fb_users)).child(it)
                        .child(getString(R.string.fb_history)).orderByValue()
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                myDataset.clear()
                                for (flowerSnapshot in dataSnapshot.children) {
                                    myDataset.add(
                                        0,
                                        Flower(
                                            flowerSnapshot.key!!,
                                            flowerSnapshot.value as Long
                                        )
                                    )
                                }
                                viewAdapter.notifyDataSetChanged()
                                if (myDataset.size == 0) pageEmpty()
                                progressBar2.isVisible = false
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Toast.makeText(
                                    this@FlowerSearch,
                                    "Error loading user database",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w("TAG", "loadData:onCancelled", databaseError.toException())
                                progressBar2.isVisible = false
                            }
                        })
                }
            }

            getString(R.string.saved) -> {
                // read saved, get info from history, turn to flower, add to dataset
                mFirebaseAuth.currentUser?.uid?.let {
                    database.child(getString(R.string.fb_users)).child(it)
                        .child(getString(R.string.fb_saved))
                        .orderByValue().addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                myDataset.clear()
                                for (flowerSnapshot in dataSnapshot.children) {
                                    myDataset.add(
                                        0,
                                        Flower(
                                            flowerSnapshot.key!!,
                                            flowerSnapshot.value as Long
                                        )
                                    )
                                }
                                viewAdapter.notifyDataSetChanged()
                                if (myDataset.size == 0) pageEmpty()
                                progressBar2.isVisible = false
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Toast.makeText(
                                    this@FlowerSearch,
                                    "Error loading user database",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w("TAG", "loadData:onCancelled", databaseError.toException())
                                progressBar2.isVisible = false
                            }
                        })
                }
            }

            getString(R.string.encyclopedia) -> {
                // add all flowers to the dataset
                val db = FlowerInfoDB(this)
                for (Flower in db.getAllFlowers()) {
                    myDataset.add(Flower)
                }
                if (myDataset.size == 0) pageEmpty()
                myDataset.sortBy { it.name }
                progressBar2.isVisible = false
            }
        }
    }

    // if dataset empty, all goes to fail page
    private fun pageEmpty() {
        setContentView(R.layout.activity_search_failed)
        findViewById<TextView>(R.id.failText).text = when (intent.getStringExtra("Parent")) {
            getString(R.string.history) ->
                if (mFirebaseAuth.currentUser == null) getString(R.string.fail_no_user)
                else getString(R.string.fail_history_text)
            getString(R.string.saved) ->
                if (mFirebaseAuth.currentUser == null) getString(R.string.fail_no_user)
                else getString(R.string.fail_save_text)
            getString(R.string.search) -> {
                findViewById<ImageView>(R.id.failImage)
                    .setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                getString(R.string.fail_search_text)
            }
            getString(R.string.encyclopedia) -> getString(R.string.fail_no_database)
            else -> getString(R.string.fail_no_database)
        }
        // set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("Parent")
    }

    // add delete history in menu when in history page
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        if (intent.getStringExtra("Parent") == getString(R.string.encyclopedia))
            menu.findItem(R.id.filter).isVisible = true
        else if (intent.getStringExtra("Parent") == getString(R.string.history) && mFirebaseAuth.currentUser != null)
            menu.findItem(R.id.delete_history).isVisible = true
        return true
    }

    // delete user history and hide search bar
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
                            database.child(getString(R.string.fb_users)).child(it)
                                .child(getString(R.string.fb_history)).removeValue()
                        }
                        pageEmpty()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
                true
            }
            R.id.filter -> {
                // turn on or off the search bar
                if (searchBar.translationY > 100) {
                    // hide search Bar
                    ObjectAnimator.ofFloat(searchBar, "translationY", 0f).apply {
                        duration = 500
                        start()
                    }
                    ObjectAnimator.ofFloat(flower_list, "translationY", 0f).apply {
                        duration = 500
                        start()
                    }
                } else {
                    // show search bar
                    ObjectAnimator.ofFloat(searchBar, "translationY", 144f).apply {
                        duration = 500
                        start()
                    }
                    ObjectAnimator.ofFloat(flower_list, "translationY", 132f).apply {
                        duration = 500
                        start()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
