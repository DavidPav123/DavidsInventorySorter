package sorting.categorizer

import config.serializable.WordCategory
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

class PredicateCategorizer : Categorizer {
    var predicate: Predicate<ItemStack>

    constructor(name: String?, predicate: Predicate<ItemStack>) {
        this.name = name
        this.predicate = predicate
    }

    constructor(wordCategory: WordCategory) {
        name = wordCategory.name
        predicate = Predicate { item: ItemStack ->
            item.type.name.lowercase(Locale.getDefault()).contains(wordCategory.value.lowercase(Locale.getDefault()))
        }
    }

    override fun doCategorization(items: List<ItemStack>): List<List<ItemStack>> {
        val returnItems: MutableList<List<ItemStack>> = ArrayList()
        val lists = doCategorizationGetMap(items)
        returnItems.add(lists[java.lang.Boolean.TRUE]!!)
        returnItems.add(lists[java.lang.Boolean.FALSE]!!)
        return returnItems
    }

    fun doCategorizationGetMap(items: List<ItemStack>): Map<Boolean, List<ItemStack>> {
        return items.stream().collect(Collectors.partitioningBy(predicate))
    }
}