package org.tesira.civic

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.ItemCardDetailRowBinding
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.db.CivicViewModel
import org.tesira.civic.utils.SelectableItemViewHolder

open class AllCardsAdapter : ListAdapter<CardWithDetails, AllCardsAdapter.CardViewHolder>(CardDiffCallback()) {

    private var shouldShowCreditsLayout: Boolean = false
    private var shouldShowInfosLayout: Boolean = false

    fun setShowCredits(isVisible: Boolean) {
        if (shouldShowCreditsLayout != isVisible) {
            shouldShowCreditsLayout = isVisible
            submitList(currentList.toList())
        }
    }

    fun setShowInfos(isVisible: Boolean) {
        if (shouldShowInfosLayout != isVisible) {
            shouldShowInfosLayout = isVisible
            submitList(currentList.toList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardDetailRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val cardWithDetails = getItem(position)
        holder.bind(cardWithDetails, shouldShowCreditsLayout, shouldShowInfosLayout)
    }

    inner class CardViewHolder(internal val binding: ItemCardDetailRowBinding) : RecyclerView.ViewHolder(binding.root), SelectableItemViewHolder {

        private var currentItem: CardWithDetails? = null

        fun bind(cardWithDetails: CardWithDetails, showCredits: Boolean, showInfos: Boolean) {
            this.currentItem = cardWithDetails
            val textColorOnDark = Color.WHITE
            val textColorOnLight = Color.BLACK
            val card = cardWithDetails.card
            val effects = cardWithDetails.effects
            val immunities = cardWithDetails.immunities
            val specials = cardWithDetails.specialAbilities

            // Basis-Karteninformationen binden
            binding.name.text = card.name
            binding.vp.text = "${card.vp}"
            binding.price.text = "${card.price}"
            binding.heart.visibility = if (card.hasHeart) View.VISIBLE else View.INVISIBLE
            binding.name.background = CivicViewModel.Companion.getItemBackgroundColor(card, itemView.resources)
            when (card.group1) {
                CardColor.YELLOW, CardColor.GREEN -> binding.name.setTextColor(textColorOnLight)
                else -> binding.name.setTextColor(textColorOnDark)
            }
            val textColor = CivicViewModel.Companion.getTextColor(card)
            binding.name.setTextColor(textColor)

            // put the family bonus on the card
            if (card.bonus > 0) {
                binding.familybonus.visibility = View.VISIBLE
                val bonusText = "+${card.bonus} to ${card.bonusCard}"
                binding.familybonus.text = bonusText
            } else {
                binding.familybonus.visibility = View.INVISIBLE
            }
            if (card.currentPrice != card.price) {
                binding.currentPrice.visibility = View.VISIBLE
                binding.currentPrice.text = "${card.currentPrice}"
                binding.price.alpha = 0.5f
            } else {
                binding.currentPrice.visibility = View.GONE
                binding.price.alpha = 1.0f
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
                            itemView.context,
                            backgroundColorResId
                        )
                    )
                    textView.setTextColor(textColor)
                    textView.visibility = View.VISIBLE
                } else {
                    textView.visibility = View.GONE
                }
            }

            setupSingleBonusTextView(binding.textViewBonusBlue, card.creditsBlue, R.color.arts, textColorOnDark)
            setupSingleBonusTextView(binding.textViewBonusGreen, card.creditsGreen, R.color.science, textColorOnLight)
            setupSingleBonusTextView(binding.textViewBonusOrange, card.creditsOrange, R.color.crafts, textColorOnDark)
            setupSingleBonusTextView(binding.textViewBonusRed, card.creditsRed, R.color.civic, textColorOnDark)
            setupSingleBonusTextView(binding.textViewBonusYellow, card.creditsYellow, R.color.religion, textColorOnLight)

            if (!card.info.isNullOrEmpty()) {
                binding.infosTitle.visibility = View.VISIBLE
                binding.infos.visibility = View.VISIBLE
                // Schritt 1: Ersetze eventuelle literale "\\n" durch echte "\n" (falls nötig, wie zuvor)
                val infoWithNewlines = card.info.replace("\\n", "\n")

                // Schritt 2: Jede Zeile trimmen, um führende/nachfolgende Leerzeichen von der Einrückung zu entfernen
                val trimmedLines = infoWithNewlines.lines().joinToString("\n") { it.trim() }

                binding.infos.text = trimmedLines
            } else {
                binding.infosTitle.visibility = View.GONE
                binding.infos.visibility = View.GONE
            }

            // Effekte anzeigen
            if (effects.isNotEmpty()) {
                binding.effectsTitle.visibility = View.VISIBLE
                binding.effects.visibility = View.VISIBLE
                val spannableBuilder = SpannableStringBuilder()
                effects.forEachIndexed { index, effect ->
                    val valueString = if (effect.value != -99) " (${effect.value})" else ""
                    val effectText = "- ${effect.name}${valueString}"

                    val start = spannableBuilder.length
                    spannableBuilder.append(effectText)
                    val end = spannableBuilder.length

                    // Farbe basierend auf effect.value setzen
                    val colorRes = if (effect.value < 0 || effect.value > 10) {
                        R.color.calamity_green
                    } else {
                        R.color.calamity_red
                    }
                    val color = ContextCompat.getColor(itemView.context, colorRes)
                    spannableBuilder.setSpan(
                        ForegroundColorSpan(color),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // Zeilenumbruch hinzufügen, außer beim letzten Element
                    if (index < effects.size - 1) {
                        spannableBuilder.append("\n")
                    }
                }
                binding.effects.text = spannableBuilder
            } else {
                binding.effectsTitle.visibility = View.GONE
                binding.effects.visibility = View.GONE
            }

            // Spezialfähigkeiten anzeigen
            if (specials.isNotEmpty()) {
                binding.specialsTitle.visibility = View.VISIBLE
                binding.specials.visibility = View.VISIBLE
                binding.specials.text = specials.joinToString(separator = "\n") { special ->
                    "- ${special.ability}"
                }
            } else {
                binding.specialsTitle.visibility = View.GONE
                binding.specials.visibility = View.GONE
            }

            // Immunitäten anzeigen
            if (immunities.isNotEmpty()) {
                binding.immunitiesTitle.visibility = View.VISIBLE
                binding.immunities.visibility = View.VISIBLE
                binding.immunities.text = immunities.joinToString(separator = "\n") { immunity ->
                    "- ${immunity.immunity}"
                }
            } else {
                binding.immunitiesTitle.visibility = View.GONE
                binding.immunities.visibility = View.GONE
            }
            if (showCredits) {
                binding.bonusLayout.visibility = View.VISIBLE
            } else {
                binding.bonusLayout.visibility = View.GONE
            }
            if (showInfos) {
                binding.allDetailsSectionLayout.visibility = View.VISIBLE
            } else {
                binding.allDetailsSectionLayout.visibility = View.GONE
            }
        }

        override fun getItemDetails(): ItemDetailsLookup.ItemDetails<String>? {
            val item = currentItem

            if (bindingAdapterPosition != RecyclerView.NO_POSITION && item != null) {
                return BuyingItemDetails(bindingAdapterPosition, item.card.name)
            }
            return null
        }
    }

    // DiffUtil.ItemCallback für effiziente Updates der Liste
    class CardDiffCallback : DiffUtil.ItemCallback<CardWithDetails>() {
        override fun areItemsTheSame(oldItem: CardWithDetails, newItem: CardWithDetails): Boolean {
            return oldItem.card.name == newItem.card.name
        }

        override fun areContentsTheSame(oldItem: CardWithDetails, newItem: CardWithDetails): Boolean {
            return oldItem == newItem
        }
    }
}