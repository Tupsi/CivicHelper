package org.tesira.civic.utils

import androidx.recyclerview.selection.ItemDetailsLookup

interface SelectableItemViewHolder {
    fun getItemDetails(): ItemDetailsLookup.ItemDetails<String>?
}