package com.example.spot_a_flower


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spot_a_flower.FlowersFragment.OnListFragmentInteractionListener
import com.example.spot_a_flower.dummy.DummyContent.DummyItem
import kotlinx.android.synthetic.main.fragment_flowers.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyFlowersRecyclerViewAdapter(
    private val mValues: List<DummyItem>
) : RecyclerView.Adapter<MyFlowersRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as DummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
        }
    }

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
        val item = mValues[position]
        holder.mIdView.text = "Flower " + item.id

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.flower_name
    }
}
