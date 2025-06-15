package org.tesira.civic

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeCalamityAdapter(private val mContext: Context) :
    RecyclerView.Adapter<HomeCalamityAdapter.ViewHolder?>() {
    private val calamityList: MutableList<Calamity> = ArrayList<Calamity>()

    class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val calamity: TextView = mView.findViewById<TextView>(R.id.tvCalamity)
        val bonus: TextView = mView.findViewById<TextView>(R.id.tvBonus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_calamity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = calamityList[position]
        holder.calamity.text = item.calamity
        holder.bonus.text = item.bonus
        if (item.bonus.toInt() > 0) {
            holder.calamity.setTextColor(
                mContext.resources.getColor(R.color.calamity_red, null)
            )
            holder.bonus.setTextColor(mContext.resources.getColor(R.color.calamity_red, null))
        } else {
            holder.calamity.setTextColor(
                mContext.resources.getColor(R.color.calamity_green, null)
            )
            holder.bonus.setTextColor(
                mContext.resources.getColor(R.color.calamity_green, null)
            )
        }
        // für Bonieffekte ohne Zahl z.B. Engineering
        if (item.bonus.toInt() == -99) {
            holder.bonus.text = " "
        }
        holder.calamity.setBackgroundResource(R.drawable.bg_specials)
        holder.bonus.setBackgroundResource(R.drawable.bg_specials)
    }

    override fun getItemCount(): Int {
        return calamityList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitCalamityList(newCalamities: List<Calamity>) {
        this.calamityList.clear()
        this.calamityList.addAll(newCalamities)
        notifyDataSetChanged()
    }
}