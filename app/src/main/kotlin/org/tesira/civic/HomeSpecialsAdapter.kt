package org.tesira.civic

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for showing a list of Special Abilities or Immunities against on the dashboard.
 */
class HomeSpecialsAdapter : RecyclerView.Adapter<HomeSpecialsAdapter.ViewHolder?>() {
    private var localDataSet: MutableList<String> = ArrayList<String>()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView? = view.findViewById<TextView?>(R.id.textView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_row_specials, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView!!.text = localDataSet[position]
        viewHolder.textView.setBackgroundResource(R.drawable.bg_specials)
        if (viewHolder.textView.text.toString().startsWith("_")) {
            viewHolder.textView.setTypeface(viewHolder.textView.typeface, Typeface.BOLD)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return localDataSet.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitSpecialsList(newSpecialsAndImmunities: List<String>) {
        this.localDataSet.clear()
        this.localDataSet.addAll(newSpecialsAndImmunities)
        notifyDataSetChanged()
    }
}