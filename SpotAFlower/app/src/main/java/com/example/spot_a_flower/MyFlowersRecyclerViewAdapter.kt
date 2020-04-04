package com.example.spot_a_flower

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_flowers.view.*


class MyFlowersRecyclerViewAdapter(
    private val Flowers: MutableList<FlowerSearch.Flower>
) : RecyclerView.Adapter<MyFlowersRecyclerViewAdapter.ViewHolder>() {

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
        val item = Flowers[position]
        holder.name.text = item.name
        holder.detail.text = item.detail
        holder.description.text = item.description
        //holder.icon.setImageDrawable(item.icon)

        with(holder.flowerCard) {
            tag = item
            //setOnClickListener(mOnClickListener)
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount(): Int = Flowers.size

    inner class ViewHolder(val flowerCard: View) : RecyclerView.ViewHolder(flowerCard) {
        val name: TextView = flowerCard.flower_name
        val detail: TextView = flowerCard.flower_detail
        val description: TextView = flowerCard.flower_description
        val icon: ImageView = flowerCard.flower_icon
    }
}
