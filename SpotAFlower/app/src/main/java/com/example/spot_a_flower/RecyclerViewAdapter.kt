package com.example.spot_a_flower

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_flowers.view.*
import java.util.*
import kotlin.collections.ArrayList

// TODO alphabetical scroll: last time didn't work
class RecyclerViewAdapter(
    private val context: Context,
    private val flowers: MutableList<Flower>,
    private val intent: String?
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(), Filterable {

    // Firebase instance variables
    private var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: DatabaseReference = Firebase.database.reference

    // Create filtered flowers dataset
    private var filteredFlowers = flowers

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_flowers, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        val flower = filteredFlowers[position]
        holder.name.text = flower.name
        holder.detail.text = flower.detail
        // justify the text alignment if it allows
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.detail.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
        }

        // get flower information from DB
        val db = FlowerInfoDB(context)
        holder.description.text = db.getDescription(flower.name)
        holder.icon.setImageBitmap(db.getIcon(flower.name))

        // save button status (only with internet)
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (mFirebaseAuth.currentUser != null && connectivityManager.activeNetworkInfo != null) {
            database.child(context.getString(R.string.fb_users)).child(mFirebaseAuth.currentUser!!.uid).child(context.getString(R.string.fb_saved))
                .child(flower.name).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // initialize and update the UI
                        if (dataSnapshot.exists()) {
                            holder.saveButton.tag = 1
                            holder.saveButton.setImageResource(android.R.drawable.star_on)
                        } else {
                            holder.saveButton.tag = 0
                            holder.saveButton.setImageResource(android.R.drawable.star_off)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("TAG", "loadData:onCancelled", error.toException())
                    }
                })
        } else holder.saveButton.isVisible = false

        // only if the user choose to open wiki link.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getBoolean(context.getString(R.string.open_wiki_key), true)) {
            val openWikiListener = View.OnClickListener { _ ->
                // when the flower is clicked, open link in browser
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(db.getWikiLink(flower.name))))
                // if the user choose to save to history when open link
                if (sharedPreferences.getBoolean(context.getString(R.string.store_after_wiki_key), true))
                    mFirebaseAuth.currentUser?.uid?.let {
                    database.child(context.getString(R.string.fb_users)).child(it).child(context.getString(R.string.fb_history)).child(flower.name)
                        .setValue(System.currentTimeMillis())
                }
            }
            holder.name.setOnClickListener(openWikiListener)
            holder.icon.setOnClickListener(openWikiListener)
            holder.description.setOnClickListener(openWikiListener)
        }

        // change star and save to database
        holder.saveButton.setOnClickListener {
            //holder.saveButton.startAnimation(AlphaAnimation(1.0f, 0.2f))
            // store the flower to saved database
            if (holder.saveButton.tag == 1) {
                // delete saved flower from user database
                if (intent == context.getString(R.string.saved))
                    AlertDialog.Builder(context)
                    .setTitle("Delete Saved Flower")
                    .setMessage("Are you sure to delete this flower from saved?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        mFirebaseAuth.currentUser?.uid?.let {
                            database.child(context.getString(R.string.fb_users)).child(it)
                                .child(context.getString(R.string.fb_saved))
                                .child(flower.name).removeValue()
                        }
                    }.setNegativeButton(android.R.string.no, null).show()
                else
                    mFirebaseAuth.currentUser?.uid?.let {
                    database.child(context.getString(R.string.fb_users)).child(it)
                        .child(context.getString(R.string.fb_saved))
                        .child(flower.name).removeValue()
                }
            } else
                mFirebaseAuth.currentUser?.uid?.let {
                database.child(context.getString(R.string.fb_users)).child(it)
                    .child(context.getString(R.string.fb_saved))
                    .child(flower.name).setValue(System.currentTimeMillis())
            }
        }

        // show and use the delete button in history
        if (intent == context.getString(R.string.history)) {
            holder.deleteButton.isVisible = true
            holder.deleteButton.setOnClickListener {
                // delete saved flower from user database
                AlertDialog.Builder(context).setTitle("Delete Flower History")
                    .setMessage("Are you sure to delete this flower from history?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        mFirebaseAuth.currentUser?.uid?.let {
                            database.child(context.getString(R.string.fb_users)).child(it)
                                .child(context.getString(R.string.fb_history))
                                .child(flower.name).removeValue()
                        }
                    }.setNegativeButton(android.R.string.no, null).show()
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredFlowers = if (charSearch.isEmpty()) flowers else {
                    val resultList: MutableList<Flower> = ArrayList()
                    for (row in flowers) {
                        if (row.name.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                            || row.detail.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredFlowers
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredFlowers = results?.values as MutableList<Flower>
                notifyDataSetChanged()
            }
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount(): Int {
        return filteredFlowers.size
    }

    // view holder, declare the UI component
    inner class ViewHolder(flowerCard: View) : RecyclerView.ViewHolder(flowerCard) {
        val name: TextView = flowerCard.flower_name
        val detail: TextView = flowerCard.flower_detail
        val description: TextView = flowerCard.flower_description
        val icon: ImageView = flowerCard.flower_icon
        val saveButton: ImageButton = flowerCard.button_save
        val deleteButton: ImageButton = flowerCard.button_delete
    }
}
