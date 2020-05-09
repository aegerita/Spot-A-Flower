package com.example.spot_a_flower

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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


class RecyclerViewAdapter(
    private val context: Context,
    private val Flowers: MutableList<Flower>
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    // Firebase instance variables
    private var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: DatabaseReference = Firebase.database.reference

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
        val flower = Flowers[position]
        holder.name.text = flower.name
        holder.detail.text = flower.detail

        // TODO get these info from flower database
        holder.description.text =
            "                         " + "Lily (members of which are true lilies) is a genus of herbaceous flowering plants growing from bulbs, all with large prominent flowers. Lilies are a group of flowering plants which are important in culture and literature in much of the world. Most species are native to the temperate northern hemisphere, though their range extends into the northern subtropics. Many other plants have \"lily\" in their common name but are not related to true lilies."
        holder.icon

        mFirebaseAuth.currentUser?.uid?.let {
            database.child("users").child(it).child("saved").child(flower.name)
                .addValueEventListener(object : ValueEventListener {
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
                        Toast.makeText(context, "Error loading user database", Toast.LENGTH_SHORT)
                            .show()
                        Log.w("TAG", "loadData:onCancelled", error.toException())
                    }
                })
        }

        // only if the user choose to open wiki link. The default is true tho
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getBoolean("openWiki", true)) {
            holder.itemView.setOnClickListener {
                // when the flower is clicked, open link in browser
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://en.wikipedia.org/wiki/${flower.name}")
                    )
                )
                // if the user choose to save to history when open link
                if (sharedPreferences.getString("addHistoryWhen", "search") == "link") {
                    println("save ${flower.name} to history")
                    mFirebaseAuth.currentUser?.uid?.let {
                        database.child("users").child(it).child("history")
                            .child(flower.name).setValue(System.currentTimeMillis())
                    }
                }
            }
        }

        // change star and save to database
        holder.saveButton.setOnClickListener {
            //holder.saveButton.startAnimation(AlphaAnimation(1.0f, 0.2f))
            if (holder.saveButton.tag == 1) {
                // delete saved flower from user database
                println("cancel storing " + flower.name)
                mFirebaseAuth.currentUser?.uid?.let {
                    database.child("users").child(it).child("saved")
                        .child(flower.name).removeValue()
                }
            } else {
                // store the flower to saved database
                println("save ${flower.name} to saved")
                mFirebaseAuth.currentUser?.uid?.let {
                    database.child("users").child(it).child("saved")
                        .child(flower.name).setValue(System.currentTimeMillis())
                }
            }
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount(): Int = Flowers.size

    // view holder, declare the UI component
    inner class ViewHolder(flowerCard: View) : RecyclerView.ViewHolder(flowerCard) {
        val name: TextView = flowerCard.flower_name
        val detail: TextView = flowerCard.flower_detail
        val description: TextView = flowerCard.flower_description
        val icon: ImageView = flowerCard.flower_icon
        val saveButton: ImageButton = flowerCard.button_save
    }
}
