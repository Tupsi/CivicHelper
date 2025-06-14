package org.tesira.civic

import androidx.recyclerview.selection.ItemKeyProvider

class BuyingItemKeyProvider(private val adapter: BuyingAdapter) : ItemKeyProvider<String?>(SCOPE_CACHED) {
    override fun getKey(position: Int): String? {
        return adapter.getKeyAtPosition(position)
    }

    override fun getPosition(key: String): Int {
        return adapter.currentList.indexOfFirst { it.card.name == key }
    }
}