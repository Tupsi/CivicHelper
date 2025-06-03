package org.tesira.civic

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.ItemRowInventoryBinding
import org.tesira.civic.db.Card
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CivicViewModel

/**
 * [androidx.recyclerview.widget.RecyclerView.Adapter] that can display a [org.tesira.civic.db.Card].
 * Shows all Civilization Advances and highlights already purchases ones.
 */
class InventoryAdapter : RecyclerView.Adapter<InventoryAdapter.ViewHolder?>() {
    private var mValues: MutableList<Card> = mutableListOf()

    /**
     * Aktualisiert die Liste der anzuzeigenden Karten und benachrichtigt den Adapter.
     * @param newValues Die neue Liste der Karten.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newValues: List<Card>) {
        this.mValues.clear()
        this.mValues.addAll(newValues)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRowInventoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mItem = item
        holder.binding.name.text = item.name
        holder.binding.name.background = CivicViewModel.Companion.getItemBackgroundColor(
            item,
            holder.itemView.resources
        )
        holder.binding.price.text = item.price.toString()
        holder.binding.vp.text = item.vp.toString()
        holder.binding.heart.visibility = if (item.hasHeart) View.VISIBLE else View.INVISIBLE

        if (holder.mItem.bonus > 0) {
            holder.binding.familybonus.visibility = View.VISIBLE
            val bonusText = "+${item.bonus} to ${item.bonusCard}"
            holder.binding.familybonus.text = bonusText
        } else {
            holder.binding.familybonus.visibility = View.INVISIBLE
        }

        when (item.group1) {
            CardColor.YELLOW, CardColor.GREEN -> holder.binding.name.setTextColor(
                Color.BLACK
            )

            else -> holder.binding.name.setTextColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val binding: ItemRowInventoryBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        lateinit var mItem: Card

        override fun toString(): String {
            return super.toString() + " '" + binding.name.text + "'"
        }
    }
}