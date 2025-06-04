package org.tesira.civic

import androidx.recyclerview.selection.ItemKeyProvider

class BuyingItemKeyProvider(scope: Int, private val adapter: BuyingAdapter) :
    ItemKeyProvider<String?>(scope) {
    override fun getKey(position: Int): String? {
        val itemList = adapter.items
        if (position >= 0 && position < itemList.size) {
            return itemList[position]!!.name
        }
        return null
    }

    override fun getPosition(key: String): Int {
        val itemList = adapter.items // Greife auf die Liste vom Adapter zu
        for (i in itemList.indices) {
            if (key == itemList[i]!!.name) {
                return i
            }
        }
        return -1
    }
}