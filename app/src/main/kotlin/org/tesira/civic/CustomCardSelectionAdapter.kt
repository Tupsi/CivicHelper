package org.tesira.civic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.ListItemCustomCardSelectableBinding

data class SelectableCardItem(
    val cardName: String,
    var isSelected: Boolean
)

class CustomCardSelectionAdapter(private val onItemClicked: (SelectableCardItem) -> Unit) : ListAdapter<SelectableCardItem, CustomCardSelectionAdapter.ViewHolder>(SelectableCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemCustomCardSelectableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    class ViewHolder(val binding: ListItemCustomCardSelectableBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectableCardItem) {
            binding.textViewCardNameSelectable.text = item.cardName
            binding.checkBoxCardSelected.isChecked = item.isSelected
        }
    }
}

class SelectableCardDiffCallback : DiffUtil.ItemCallback<SelectableCardItem>() {
    override fun areItemsTheSame(oldItem: SelectableCardItem, newItem: SelectableCardItem): Boolean {
        return oldItem.cardName == newItem.cardName
    }

    override fun areContentsTheSame(oldItem: SelectableCardItem, newItem: SelectableCardItem): Boolean {
        return oldItem == newItem
    }
}