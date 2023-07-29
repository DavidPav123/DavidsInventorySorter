package sorting.categorizer

import config.serializable.ListCategory
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.stream.Collectors
import kotlin.Comparator
import kotlin.String

class ListCategoryCategorizer(listCategory: ListCategory) : Categorizer() {
    var list: List<String>
    private var predicateCategorizer: PredicateCategorizer
    private var comparatorCategorizer: ComparatorCategorizer

    init {
        name = listCategory.getName()
        list = listCategory.value.stream().map { obj: String -> obj.lowercase(Locale.getDefault()) }
            .collect(Collectors.toList())
        predicateCategorizer = PredicateCategorizer(
            ""
        ) { item: ItemStack -> list.indexOf(item.type.name.lowercase(Locale.getDefault())) >= 0 }
        comparatorCategorizer = ComparatorCategorizer("",
            Comparator.comparing { item: ItemStack -> list.indexOf(item.type.name.lowercase(Locale.getDefault())) })
    }

    override fun doCategorization(items: List<ItemStack>): List<List<ItemStack>> {
        val returnItems: MutableList<List<ItemStack>> = ArrayList()
        val map = predicateCategorizer.doCategorizationGetMap(items)
        val sortedList = comparatorCategorizer.doCategorizationGetList(map[Boolean.equals(true)]!!)
        returnItems.add(sortedList)
        returnItems.add(map[Boolean.equals(false)]!!)
        return returnItems
    }
}
