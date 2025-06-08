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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.databinding.ItemCardDetailRowBinding
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CivicViewModel

class AllCardsAdapter : ListAdapter<CardWithDetails, AllCardsAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardDetailRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val cardWithDetails = getItem(position)
        holder.bind(cardWithDetails)
    }

    inner class CardViewHolder(private val binding: ItemCardDetailRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cardWithDetails: CardWithDetails) {
            val textColorOnDark = Color.WHITE
            val textColorOnLight = Color.BLACK
            val card = cardWithDetails.card
            val effects = cardWithDetails.effects
            val immunities = cardWithDetails.immunities
            val specials = cardWithDetails.specialAbilities // Annahme: Deine Entität heißt so

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
                    textView.visibility = View.GONE // Diesen TextView ausblenden, wenn Bonus 0 ist
                }
            }

            // Blau
            setupSingleBonusTextView(
                binding.textViewBonusBlue,
                card.creditsBlue,
                R.color.arts,
                textColorOnDark
            )

            // Grün
            setupSingleBonusTextView(
                binding.textViewBonusGreen,
                card.creditsGreen,
                R.color.science,
                textColorOnLight
            )

            // Orange
            setupSingleBonusTextView(
                binding.textViewBonusOrange,
                card.creditsOrange,
                R.color.crafts,
                textColorOnDark
            )

            // Rot
            setupSingleBonusTextView(
                binding.textViewBonusRed,
                card.creditsRed,
                R.color.civic,
                textColorOnDark
            )

            // Gelb
            setupSingleBonusTextView(
                binding.textViewBonusYellow,
                card.creditsYellow,
                R.color.religion,
                textColorOnLight
            )


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
                    "- ${special.ability}" // Passe die Formatierung an
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
                    "- ${immunity.immunity}" // Passe die Formatierung an
                }
            } else {
                binding.immunitiesTitle.visibility = View.GONE
                binding.immunities.visibility = View.GONE
            }
            // TODO: Weitere Kartendetails binden (z.B. family, color, was auch immer in `card` ist)
        }
    }

    // DiffUtil.ItemCallback für effiziente Updates der Liste
    class CardDiffCallback : DiffUtil.ItemCallback<CardWithDetails>() {
        override fun areItemsTheSame(oldItem: CardWithDetails, newItem: CardWithDetails): Boolean {
            // Überprüfe auf Basis einer eindeutigen ID (z.B. Kartenname)
            return oldItem.card.name == newItem.card.name
        }

        override fun areContentsTheSame(oldItem: CardWithDetails, newItem: CardWithDetails): Boolean {
            // Überprüfe, ob sich der Inhalt geändert hat.
            // Dies kann detaillierter sein, wenn nötig, aber für den Anfang reicht oft ein Vergleich
            // der Objekte, wenn sie Datenklassen sind.
            return oldItem == newItem
        }
    }
}