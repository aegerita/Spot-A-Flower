package com.example.spot_a_flower

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
        holder.moreButton.setOnClickListener {
            holder.moreButton.startAnimation(AlphaAnimation(1.0f, 0.2f))
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(flower.link))
            context.startActivity(i)
        }
        holder.saveButton.setOnClickListener {
            holder.saveButton.startAnimation(AlphaAnimation(1.0f, 0.2f))
            //if (holder.saveButton.tag == 0)
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
        val moreButton: ImageButton = flowerCard.button_more
    }
}
