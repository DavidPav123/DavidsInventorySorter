package listeners

import com.github.davidpav123.inventorysorter.InventorySorter
import config.PlayerDataManager.containsRefillBlocks
import config.PlayerDataManager.containsRefillBreakables
import config.PlayerDataManager.containsRefillConumables
import config.PlayerDataManager.isRefillBlocks
import config.PlayerDataManager.isRefillBreakables
import config.PlayerDataManager.isRefillConumables
import config.PluginConfigManager
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import utils.InventoryDetector
import utils.PluginPermissions
import java.util.*

class RefillListener : Listener {
    private val specialBlockRefills: HashMap<Material?, Material>

    init {
        specialBlockRefills = HashMap()
        try {
            specialBlockRefills[Material.WHEAT] = Material.WHEAT_SEEDS
            specialBlockRefills[Material.BEETROOTS] = Material.BEETROOT_SEEDS
            specialBlockRefills[Material.MELON_STEM] = Material.MELON_SEEDS
            specialBlockRefills[Material.PUMPKIN_STEM] = Material.PUMPKIN_SEEDS
            specialBlockRefills[Material.TRIPWIRE] = Material.STRING
            specialBlockRefills[Material.COCOA] = Material.COCOA_BEANS
            specialBlockRefills[Material.POTATOES] = Material.POTATO
            specialBlockRefills[Material.CARROTS] = Material.CARROT
            specialBlockRefills[Material.REDSTONE_WIRE] = Material.REDSTONE
            specialBlockRefills[Material.REDSTONE_WALL_TORCH] = Material.REDSTONE_TORCH
            specialBlockRefills[Material.WALL_TORCH] = Material.TORCH
            specialBlockRefills[Material.OAK_WALL_SIGN] = Material.OAK_SIGN
            specialBlockRefills[Material.SPRUCE_WALL_SIGN] = Material.SPRUCE_SIGN
            specialBlockRefills[Material.BIRCH_WALL_SIGN] = Material.BIRCH_SIGN
            specialBlockRefills[Material.JUNGLE_WALL_SIGN] = Material.JUNGLE_SIGN
            specialBlockRefills[Material.ACACIA_WALL_SIGN] = Material.ACACIA_SIGN
            specialBlockRefills[Material.DARK_OAK_WALL_SIGN] = Material.DARK_OAK_SIGN
            specialBlockRefills[Material.WARPED_WALL_SIGN] = Material.WARPED_SIGN
            specialBlockRefills[Material.BAMBOO_SAPLING] = Material.BAMBOO
            specialBlockRefills[Material.SWEET_BERRY_BUSH] = Material.SWEET_BERRIES
            specialBlockRefills[Material.SOUL_WALL_TORCH] = Material.SOUL_TORCH
            specialBlockRefills[Material.CRIMSON_WALL_SIGN] = Material.CRIMSON_SIGN
        } catch (ignore: NoSuchFieldError) {
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onPlacingBlock(e: BlockPlaceEvent) {
        val player = e.player
        if (isPlayerAllowedToRefillBlocks(player)) {
            var config = PluginConfigManager.isDefaultBlockRefill()
            if (containsRefillBlocks(player)) {
                config = isRefillBlocks(player)
            }
            if (config) {
                val item = e.itemInHand
                var material: Material? = e.blockPlaced.type
                if (specialBlockRefills.containsKey(material)) {
                    material = specialBlockRefills[material]
                }
                if (item.amount == 1) {
                    if (e.player.inventory.getItem(e.hand).type == material) {
                        if (isNotOnBlackList(item)) {
                            refillBlockInSlot(e.player, material, e.hand)
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onConsuming(e: PlayerItemConsumeEvent) {
        val player = e.player
        if (isPlayerAllowedToRefillConsumables(player)) {
            var config = PluginConfigManager.isDefaultConsumablesRefill()
            if (containsRefillConumables(player)) {
                config = isRefillConumables(player)
            }
            if (config) {
                val item = e.item
                if (item.amount == 1) {
                    if (isNotOnBlackList(item)) {
                        if (item.maxStackSize > 1) {
                            var mainhand = false
                            var offhand = false
                            if (isPlayerHoldingAItemInMainHand(player)) {
                                if (playerMainHandHeldItemMaterialEquals(item, player)) {
                                    mainhand = true
                                } else if (isPlayerHoldingAItemInOffHand(player)) {
                                    if (playerOffHandHeldItemMaterialEquals(item, player)) {
                                        offhand = true
                                    }
                                }
                            } else if (isPlayerHoldingAItemInOffHand(player)) {
                                if (playerOffHandHeldItemMaterialEquals(item, player)) {
                                    offhand = true
                                }
                            }
                            if (mainhand || offhand) {
                                var hand = e.player.inventory.heldItemSlot
                                if (!mainhand) hand = -999
                                refillConsumableInSlot(hand, player, e.item)
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onPlayerItemBreaks(e: PlayerItemBreakEvent) {
        val player = e.player
        if (isPlayerAllowedToRefillBrokenItems(player)) {
            var config = PluginConfigManager.isDefaultBreakableRefill()
            if (containsRefillBreakables(player)) {
                config = isRefillBreakables(player)
            }
            if (config) {
                val item = e.brokenItem
                if (isNotOnBlackList(item)) {
                    val refillSlot = getRefillStack(item.type, player)
                    if (refillSlot >= 0) {
                        val refillItem = player.inventory.getItem(refillSlot)
                        if (player.inventory.itemInMainHand == item) {
                            assert(InventorySorter.main != null)
                            InventorySorter.main!!.server.scheduler.scheduleSyncDelayedTask(
                                InventorySorter.main!!,
                                {
                                    player.inventory.setItem(
                                        player.inventory.heldItemSlot,
                                        refillItem
                                    )
                                    player.inventory.setItem(refillSlot, null)
                                }, 1L
                            )
                        } else if (player.inventory.itemInOffHand == item) {
                            assert(InventorySorter.main != null)
                            InventorySorter.main!!.server.scheduler.scheduleSyncDelayedTask(
                                InventorySorter.main!!,
                                {
                                    player.inventory.setItem(40, refillItem)
                                    player.inventory.setItem(refillSlot, null)
                                }, 1L
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isNotOnBlackList(item: ItemStack): Boolean {
        return !PluginConfigManager.getBlacklistAutoRefill().contains(item.type)
    }

    /**
     * Returns the index of a refill stack if it gets found, if not it returns -1.
     *
     * @param material the material of the stack you want to find.
     * @param player        the owner of the inventory.
     * @return the index of the item in the inventory if it got found otherwise -1.
     */
    private fun getRefillStack(material: Material, player: Player): Int {
        val items = InventoryDetector.getFullInventory(player.inventory)
        for (i in 0..35) {
            if (i != player.inventory.heldItemSlot) {
                if (items[i] != null) {
                    if (items[i]!!.type == material) {
                        return i
                    }
                }
            }
        }
        return -1
    }

    /**
     * If the player is in survival mode and has the permission
     * PluginPermissions.AUTOFILL_BROKEN_ITEMS it returns true otherwise false.
     *
     * @param player the player you want to check.
     * @return true if the player is allowed to auto refill broken items otherwise
     * false.
     */
    private fun isPlayerAllowedToRefillBrokenItems(player: Player): Boolean {
        return player.gameMode == GameMode.SURVIVAL && player.hasPermission(PluginPermissions.AUTOFILL_BROKEN_ITEMS.string)
    }

    /**
     * If the player is in survival mode and has the permission
     * PluginPermissions.AUTOFILL_CONSUMABLES it returns true otherwise false.
     *
     * @param player the player you want to check.
     * @return true if the player is allowed to auto refill otherwise false.
     */
    private fun isPlayerAllowedToRefillConsumables(player: Player): Boolean {
        return player.gameMode == GameMode.SURVIVAL && player.hasPermission(PluginPermissions.AUTOFILL_CONSUMABLES.string)
    }

    /**
     * If the player is in survival mode and has the permission
     * PluginPermissions.AUTOFILL_CONSUMABLES it returns true otherwise false.
     *
     * @param player the player you want to check.
     * @return true if the player is allowed to auto refill otherwise false.
     */
    private fun isPlayerAllowedToRefillBlocks(player: Player): Boolean {
        return player.gameMode == GameMode.SURVIVAL && player.hasPermission(PluginPermissions.AUTOFILL_BLOCKS.string)
    }

    /**
     * Returns true if the player has an item in his main hand.
     *
     * @param player the player you want to check.
     * @return true if the player holds an item in his main hand otherwise false.
     */
    private fun isPlayerHoldingAItemInMainHand(player: Player): Boolean {
        return player.inventory.getItem(player.inventory.heldItemSlot) != null
    }

    /**
     * Returns true if the player has an item in his offhand.
     *
     * @param player the player you want to check.
     * @return true if the player holds an item in his offhand otherwise false.
     */
    private fun isPlayerHoldingAItemInOffHand(player: Player): Boolean {
        return !player.inventory.itemInOffHand.type.isAir
    }

    /**
     * If the Material of the `item` is equal to the material in the held item
     * slot (main hand) it returns true otherwise false.
     *
     * @param item   the item which material you want to compare.
     * @param player the player owning the held item slot (main hand).
     * @return true if the materials are equal otherwise false.
     */
    private fun playerMainHandHeldItemMaterialEquals(item: ItemStack, player: Player): Boolean {
        return Objects.requireNonNull(player.inventory.getItem(player.inventory.heldItemSlot))!!.type == item.type
    }

    /**
     * If the Material of the `item` is equal to the material in the offhand
     * it returns true otherwise false.
     *
     * @param item   the item which material you want to compare.
     * @param player the player owning the offhand.
     * @return true if the materials are equal otherwise false.
     */
    private fun playerOffHandHeldItemMaterialEquals(item: ItemStack, player: Player): Boolean {
        return player.inventory.itemInOffHand.type == item.type
    }

    private fun isViableSlot(i: Int, player: Player): Boolean {
        return i != player.inventory.heldItemSlot && i != 40
    }

    /**
     * Searches through the main inventory (slots 9 - 35) taking the first ItemStack
     * with the same type, an amount bigger than 1 (bigger than 0 would work but
     * causes a rendering-bug in the client, so the item is invisible) and puts it
     * into the slot (+ one amount) while consuming.
     */
    private fun refillConsumableInSlot(hand: Int, player: Player, conItem: ItemStack) {
        assert(InventorySorter.main != null)
        InventorySorter.main!!.server.scheduler.scheduleSyncDelayedTask(
            InventorySorter.main!!, {
                val items = InventoryDetector.getFullInventory(player.inventory)
                for (i in 0..35) {
                    if (items[i] != null) {
                        if (isViableSlot(i, player)) {
                            if (items[i]!!.type == conItem.type) {
                                if (hand > -999) {
                                    player.inventory.setItem(hand, items[i])
                                } else {
                                    player.inventory.setItemInOffHand(items[i])
                                }
                                player.inventory.setItem(i, null)
                                break
                            }
                        }
                    }
                }
            }, 1L
        )
    }

    /**
     * Searches through the main inventory (slots 9 - 35) taking the first ItemStack
     * with the same type of materials the placed block has and puts it into the
     * slot after placing.
     */
    private fun refillBlockInSlot(player: Player, material: Material, hand: EquipmentSlot) {
        val items = InventoryDetector.getFullInventory(player.inventory)
        for (i in 0..35) {
            if (items[i] != null) {
                if (isViableSlot(i, player)) {
                    if (items[i]!!.type == material) {
                        if (hand == EquipmentSlot.HAND) {
                            player.inventory.setItemInMainHand(items[i])
                            player.inventory.setItem(i, null)
                            break
                        } else if (hand == EquipmentSlot.OFF_HAND) {
                            player.inventory.setItemInOffHand(items[i])
                            player.inventory.setItem(i, null)
                            break
                        }
                    }
                }
            }
        }
    }
}
