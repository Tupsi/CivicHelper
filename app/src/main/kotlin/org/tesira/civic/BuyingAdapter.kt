package org.tesira.civic

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.ItemRowPurchasablesBinding
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.db.CivicViewModel

/**
 * Adapter for the buying process. Displays all cards which are still buyable with current price.
 * Checks if there is enough treasure, otherwise grays out the item. Sets a heart if user has
 * set a "what do I want?" preference.
 */
class BuyingAdapter(private val mCivicViewModel: CivicViewModel) : ListAdapter<CardWithDetails, BuyingAdapter.ViewHolder>(BuyingDiffCallback()) {
    private lateinit var tracker: SelectionTracker<String>

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
        val item: CardWithDetails = getItem(position) // 6. getItem(position) verwenden
        val cardData = item.card
        val textColorOnDark = Color.WHITE
        val textColorOnLight = Color.BLACK
        val binding = viewHolder.binding
        val isSelected = tracker.isSelected(cardData.name)
        binding.name.text = cardData.name
        binding.name.background = CivicViewModel.Companion.getItemBackgroundColor(
            cardData,
            viewHolder.itemView.resources
        )
//        binding.price.text = item.price.toString()
        binding.price.text = cardData.currentPrice.toString()
        binding.vp.text = cardData.vp.toString()
        binding.card.isActivated = isSelected
        binding.heart.visibility = if (cardData.hasHeart) View.VISIBLE else View.INVISIBLE

        // auto select all gets which have costs are reduced from bonus to zero
        if (!isSelected && cardData.currentPrice == 0) {
            tracker.select(cardData.name)
        } else {
            // can we buy the card?
            if (!isSelected && mCivicViewModel.remaining.getValue()!! < cardData.currentPrice) {
                binding.card.alpha = 0.25f
            } else {
                binding.card.alpha = 1f
            }
        }

        // adjust color of name depending on background, so it is readable
        when (cardData.group1) {
            CardColor.YELLOW, CardColor.GREEN -> binding.name.setTextColor(textColorOnLight)
            else -> binding.name.setTextColor(textColorOnDark)
        }
        val textColor = CivicViewModel.Companion.getTextColor(cardData)
        binding.name.setTextColor(textColor)

        // put the family bonus on the card
        if (cardData.bonus > 0) {
            binding.familybonus.visibility = View.VISIBLE
            val bonusText = "+${cardData.bonus} to ${cardData.bonusCard}"
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
                textView.visibility = View.GONE
            }
        }

        setupSingleBonusTextView(binding.textViewBonusBlue, cardData.creditsBlue, R.color.arts, textColorOnDark)
        setupSingleBonusTextView(binding.textViewBonusGreen, cardData.creditsGreen, R.color.science, textColorOnLight)
        setupSingleBonusTextView(binding.textViewBonusOrange, cardData.creditsOrange, R.color.crafts, textColorOnDark)
        setupSingleBonusTextView(binding.textViewBonusRed, cardData.creditsRed, R.color.civic, textColorOnDark)
        setupSingleBonusTextView(binding.textViewBonusYellow, cardData.creditsYellow, R.color.religion, textColorOnLight)

        if (mCivicViewModel.showCredits.value!!) {
            binding.bonusLayout.visibility = View.VISIBLE
        } else {
            binding.bonusLayout.visibility = View.GONE
        }
    }

    fun getKeyAtPosition(position: Int): String? {
        if (position >= 0 && position < itemCount) {
            return getItem(position)?.card?.name
        }
        return null
    }

    inner class ViewHolder(val binding: ItemRowPurchasablesBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemDetails: ItemDetailsLookup.ItemDetails<String>
            get() = BuyingItemDetails(
                bindingAdapterPosition,
                binding.name.text.toString()
            )
    }

    class BuyingDiffCallback : DiffUtil.ItemCallback<CardWithDetails>() {
        override fun areItemsTheSame(oldItem: CardWithDetails, newItem: CardWithDetails): Boolean {
            return oldItem.card.name == newItem.card.name
        }

        override fun areContentsTheSame(oldItem: CardWithDetails, newItem: CardWithDetails): Boolean {
            return oldItem == newItem
        }
    }
}
