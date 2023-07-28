package utils;

import config.PluginConfigManager;
import cooldown.CMRegistry;
import utils.messages.enums.MessageID;
import utils.messages.enums.MessageType;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import sorting.InventorySorter;
import utils.messages.MessageSystem;

public class SortingUtils {

	public static boolean sortPlayerInvWithEffects(Player player) {
		if (!CMRegistry.isOnCooldown(CMRegistry.CMIdentifier.SORTING, player)) {
			if (InventorySorter.sortPlayerInventory(player)) {
				InventorySorter.playSortingSound(player);
				MessageSystem.sendSortedMessage(player);
				return true;
			}
		}
		return false;
	}

	public static boolean isOnInventoryBlacklist(Block block, CommandSender sender){
		if (PluginConfigManager.getBlacklistInventory().contains(block.getType())) {
			MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_BLACKLIST_INVENTORY, sender);
			return true;
		}
		return false;
	}

	public static boolean sortInventoryWithEffects(Inventory inv, Player player) {
		if (!CMRegistry.isOnCooldown(CMRegistry.CMIdentifier.SORTING, player)) {
			if (InventorySorter.sortInventory(inv, player)) {
				InventorySorter.playSortingSound(player);
				MessageSystem.sendSortedMessage(player);
				return true;
			}
		}
		return false;
	}

}
