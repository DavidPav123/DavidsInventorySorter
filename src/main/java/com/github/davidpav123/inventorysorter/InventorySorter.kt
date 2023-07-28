package com.github.davidpav123.inventorysorter

import commands.*
import config.PluginConfig
import config.serializable.ListCategory
import config.serializable.MasterCategory
import config.serializable.WordCategory
import listeners.RefillListener
import listeners.SortingListener
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.MalformedURLException
import java.net.URLClassLoader
import java.util.*

class InventorySorter : JavaPlugin() {
    var rB: ResourceBundle? = null
        private set
    private var locale: Locale? = null
    override fun onEnable() {
        main = this
        ConfigurationSerialization.registerClass(WordCategory::class.java)
        ConfigurationSerialization.registerClass(ListCategory::class.java)
        ConfigurationSerialization.registerClass(MasterCategory::class.java)
        PluginConfig.getInstance().loadConfig()
        val version = description.version.replace(".", "-")
        val bundleName = name + "_" + version
        getPlugin(this.javaClass).saveResource(bundleName + "_en_GB.properties", false)
        try {
            val fileUrl = File(dataFolder.toString()).toURI().toURL()
            val loader: ClassLoader = URLClassLoader(arrayOf(fileUrl))
            rB = locale?.let { ResourceBundle.getBundle(bundleName, it, loader) }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        Objects.requireNonNull(getCommand(CleanInventoryCommand.COMMAND_ALIAS))?.setExecutor(CleanInventoryCommand())
        Objects.requireNonNull(getCommand(CleaningItemCommand.COMMAND_ALIAS))?.setExecutor(CleaningItemCommand())
        Objects.requireNonNull(getCommand(BlacklistCommand.COMMAND_ALIAS))?.setExecutor(BlacklistCommand())
        Objects.requireNonNull(getCommand(SortingConfigCommand.COMMAND_ALIAS))?.setExecutor(SortingConfigCommand())
        Objects.requireNonNull(getCommand(SortingAdminCommand.COMMAND_ALIAS))?.setExecutor(SortingAdminCommand())
        Bukkit.getPluginManager().registerEvents(SortingListener(), this)
        Bukkit.getPluginManager().registerEvents(RefillListener(), this)
    }

    fun setLocale(language: String?, country: String?, variant: String?) {
        locale = Locale(language, country, variant)
    }

    companion object {
        @JvmField
		var main: InventorySorter? = null
    }
}
