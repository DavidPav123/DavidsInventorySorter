package utils

import org.bukkit.inventory.ItemStack

object Quicksort {
    fun sort(items: List<ItemStack>, comparator: Comparator<ItemStack>, l: Int, r: Int): List<ItemStack> {
        val q: Int
        if (l < r) {
            q = partition(items.toMutableList(), comparator, l, r)
            sort(items, comparator, l, q)
            sort(items, comparator, q + 1, r)
        }
        return items
    }

    private fun partition(items: MutableList<ItemStack>, comparator: Comparator<ItemStack>, l: Int, r: Int): Int {
        var i = l - 1
        var j = r + 1
        val item = items[(l + r) / 2]
        while (true) {
            do {
                i++
            } while (comparator.compare(items[i], item) < 0)
            do {
                j--
            } while (comparator.compare(item, items[j]) < 0)
            if (i < j) {
                val k = items[i]
                items[i] = items[j]
                items[j] = k
            } else {
                return j
            }
        }
    }
}