package org.tesira.civic

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.ItemRowInventoryBinding
import org.tesira.civic.db.Card
import org.tesira.civic.db.CivicViewModel

/**
 * [androidx.recyclerview.widget.RecyclerView.Adapter] that can display a [org.tesira.civic.db.Card].
 * Shows all Civilization Advances and highlights already purchases ones.
 */
class InventoryAdapter(private val mCivicViewModel: CivicViewModel) : RecyclerView.Adapter<InventoryAdapter.ViewHolder?>() {
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

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val textColorOnDark = Color.WHITE
        val textColorOnLight = Color.BLACK
        val item = mValues[position]
        viewHolder.mItem = item
        val binding = viewHolder.binding
        binding.name.text = item.name
        binding.name.background = CivicViewModel.Companion.getItemBackgroundColor(
            item,
            viewHolder.itemView.resources
        )
        binding.price.text = item.price.toString()
        binding.vp.text = item.vp.toString()
        binding.heart.visibility = if (item.hasHeart) View.VISIBLE else View.INVISIBLE

        val textColor = CivicViewModel.Companion.getTextColor(item)
        binding.name.setTextColor(textColor)

        if (item.bonus > 0) {
            binding.familybonus.visibility = View.VISIBLE
            val bonusText = "+${item.bonus} to ${item.bonusCard}"
            binding.familybonus.text = bonusText
        } else {
            binding.familybonus.visibility = View.INVISIBLE
        }

        if (item.price == 0) {
            binding.price.visibility = View.GONE
        } else {
            binding.price.visibility = View.VISIBLE
        }

        if (item.vp == 0) {
            binding.vp.visibility = View.GONE
        } else {
            binding.vp.visibility = View.VISIBLE
        }

        fun setupSingleBonusTextView(
            textView: TextView,
            creditValue: Int,
            backgroundColorResId: Int,
            textColor: Int
        ) {
            if (creditValue > 0) {
                textView.text = creditValue.toString()
                textView.setBackgroundColor(
                    ContextCompat.getColor(
                        viewHolder.itemView.context,
                        backgroundColorResId
                    )
                )
                textView.setTextColor(textColor)
                textView.visibility = View.VISIBLE // Nur diesen TextView sichtbar machen
            } else {
                textView.visibility = View.GONE // Diesen TextView ausblenden, wenn Bonus 0 ist
            }
        }

        // Blau
        setupSingleBonusTextView(
            binding.textViewBonusBlue,
            item.creditsBlue,
            R.color.arts,
            textColorOnDark
        )

        // Grün
        setupSingleBonusTextView(
            binding.textViewBonusGreen,
            item.creditsGreen,
            R.color.science,
            textColorOnLight
        )

        // Orange
        setupSingleBonusTextView(
            binding.textViewBonusOrange,
            item.creditsOrange,
            R.color.crafts,
            textColorOnDark
        )

        // Rot
        setupSingleBonusTextView(
            binding.textViewBonusRed,
            item.creditsRed,
            R.color.civic,
            textColorOnDark
        )

        // Gelb
        setupSingleBonusTextView(
            binding.textViewBonusYellow,
            item.creditsYellow,
            R.color.religion,
            textColorOnLight
        )

        if (mCivicViewModel.showCredits.value!!) {
            binding.bonusLayout.visibility = View.VISIBLE
        } else {
            binding.bonusLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val binding: ItemRowInventoryBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var mItem: Card

        override fun toString(): String {
            return super.toString() + " '" + binding.name.text + "'"
        }
    }
}