package sorting.categorizer

import config.serializable.MasterCategory
import org.bukkit.inventory.ItemStack
import sorting.CategorizerManager

class MasterCategorizer(masterCategory: MasterCategory) : Categorizer() {
    private val subCategorizers: List<String>

    init {
        name = masterCategory.getName()
        subCategorizers = masterCategory.value
    }

    override fun doCategorization(items: List<ItemStack>): List<List<ItemStack>> {
        val returnItems: MutableList<List<ItemStack>> = ArrayList()
        returnItems.add(CategorizerManager.sort(items, subCategorizers))
        return returnItems
    }
}