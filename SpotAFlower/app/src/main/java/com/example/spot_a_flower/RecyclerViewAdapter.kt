package com.example.spot_a_flower

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_flowers.view.*


class RecyclerViewAdapter(
    private val context: Context,
    private val Flowers: MutableList<FlowerSearch.Flower>
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

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
        holder.description.text = flower.description

        // initialize save button
        if (flower.isSaved) {
            holder.saveButton.tag = 1
            holder.saveButton.setImageResource(android.R.drawable.star_on)
        } else holder.saveButton.tag = 0

        // only if the user choose to open wiki link. The default is true tho
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getBoolean("openWiki", true)) {
            holder.itemView.setOnClickListener {
                // when the flower is clicked, open link in browser
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(flower.link)))
                // if the user choose to save to history when open link
                if (sharedPreferences.getString("addHistoryWhen", "search") == "link") {
                    // TODO save the flower to history when open wiki link
                    println("save ${flower.name} to history")
                }
            }
        }

        // change star and save to database
        holder.saveButton.setOnClickListener {
            holder.saveButton.startAnimation(AlphaAnimation(1.0f, 0.2f))
            if (holder.saveButton.tag == 1) {
                holder.saveButton.tag = 0
                holder.saveButton.setImageResource(android.R.drawable.star_off)
                // TODO delete saved flower from user database
                println("cancel storing " + flower.name)
            } else {
                holder.saveButton.tag = 1
                holder.saveButton.setImageResource(android.R.drawable.star_on)
                // TODO store the flower to user database and user history
                println("store " + flower.name)
                println("save ${flower.name} to history")
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
        //val icon: ImageView = flowerCard.flower_icon
        val saveButton: ImageButton = flowerCard.button_save
    }
}
