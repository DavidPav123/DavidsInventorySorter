package sorting.categorizer

import org.bukkit.inventory.ItemStack
import utils.Quicksort

class ComparatorCategorizer(name: String?, comparator: Comparator<ItemStack>) : Categorizer() {
    private var comparator: Comparator<ItemStack>

    init {
        this.name = name
        this.comparator = comparator
    }

    override fun doCategorization(items: List<ItemStack>): List<List<ItemStack>> {
        return ArrayList(listOf(doCategorizationGetList(items)))
    }

    fun doCategorizationGetList(items: List<ItemStack>): List<ItemStack> {
        //items.sort(comparator);
        //return items;
        return Quicksort.sort(items, comparator, 0, items.size - 1)
    }
}
