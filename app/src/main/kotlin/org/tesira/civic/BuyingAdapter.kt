package org.tesira.civic

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.ItemRowPurchasablesBinding
import org.tesira.civic.db.Card
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CivicViewModel

/**
 * Adapter for the buying process. Displays all cards which are still buyable with current price.
 * Checks if there is enough treasure, otherwise grays out the item. Sets a heart if user has
 * set a "what do I want?" preference.
 */
class BuyingAdapter(private val mCivicViewModel: CivicViewModel) : RecyclerView.Adapter<BuyingAdapter.ViewHolder?>() {
    private lateinit var tracker: SelectionTracker<String>
    val mValues: MutableList<Card> = mutableListOf()

    fun setSelectionTracker(tracker: SelectionTracker<String>) {
        this.tracker = tracker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRowPurchasablesBinding.inflate(
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
        val isSelected = tracker.isSelected(item.name)
        binding.name.text = item.name
        binding.name.background = CivicViewModel.Companion.getItemBackgroundColor(
            item,
            viewHolder.itemView.resources
        )
//        binding.price.text = item.price.toString()
        binding.price.text = item.currentPrice.toString()
        binding.vp.text = item.vp.toString()
        binding.card.isActivated = isSelected
        binding.heart.visibility = if (item.hasHeart) View.VISIBLE else View.INVISIBLE

        // auto select all gets which have costs are reduced from bonus to zero
        if (!isSelected && item.currentPrice == 0) {
            tracker.select(item.name)
        } else {
            // can we buy the card?
            if (!isSelected && mCivicViewModel.remaining.getValue()!! < item.currentPrice) {
                binding.card.alpha = 0.25f
            } else {
                binding.card.alpha = 1f
            }
        }

        // adjust color of name depending on background, so it is readable
        when (item.group1) {
            CardColor.YELLOW, CardColor.GREEN -> binding.name.setTextColor(textColorOnLight)
            else -> binding.name.setTextColor(textColorOnDark)
        }
        val textColor = CivicViewModel.Companion.getTextColor(item)
        binding.name.setTextColor(textColor)

        // put the family bonus on the card
        if (item.bonus > 0) {
            binding.familybonus.visibility = View.VISIBLE
            val bonusText = "+${item.bonus} to ${item.bonusCard}"
            binding.familybonus.text = bonusText
        } else {
            binding.familybonus.visibility = View.INVISIBLE
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
                textView.visibility = View.VISIBLE
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

    /**
     * This updates the adapter with a new list and restores the already selected cards to the
     * tracker if any.
     * @param newList new sorted list of cards.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun changeList(newList: MutableList<Card>) {
        val saveSelection = Bundle()
        tracker.onSaveInstanceState(saveSelection)
        mValues.clear()
        mValues.addAll(newList)
        notifyDataSetChanged()
        tracker.onRestoreInstanceState(saveSelection)
    }

    class ViewHolder(val binding: ItemRowPurchasablesBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var mItem: Card
        val itemDetails: ItemDetailsLookup.ItemDetails<String>
            get() = BuyingItemDetails(
                bindingAdapterPosition,
                binding.name.text.toString()
            )
    }
}