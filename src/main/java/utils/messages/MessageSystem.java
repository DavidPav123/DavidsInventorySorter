package utils.messages;

import utils.PluginPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import config.PlayerDataManager;
import config.PluginConfigManager;
import com.github.davidpav123.inventorysorter.InventorySorter;
import utils.messages.enums.MessageID;
import utils.messages.enums.MessageType;

import java.util.List;
import java.util.Objects;

public class MessageSystem {

	public static void sendMessageToCS(MessageType type, String arg, CommandSender cs) {
		if (cs != null) {
			cs.sendMessage(getMessageString(type, arg));
		} else {
			assert InventorySorter.main != null;
			InventorySorter.main.getServer().getConsoleSender().sendMessage(getMessageString(type, arg));
		}
	}

	public static void sendSortedMessage(CommandSender sender) {

		boolean flag;

		if(sender instanceof Player && PlayerDataManager.containsNotification((Player) sender)) {
			flag = PlayerDataManager.isNotification((Player) sender);
		}else {
			flag = PluginConfigManager.getDefaultChatNotificationBoolean();
		}

		if(flag) {
			MessageSystem.sendMessageToCS(MessageType.SUCCESS, MessageID.INFO_INVENTORY_SORTED, sender);
		}

	}

	public static void sendMessageToCS(MessageType type, MessageID messageID, CommandSender cs) {
		assert InventorySorter.main != null;
		sendMessageToCS(type, Objects.requireNonNull(InventorySorter.main.getRB()).getString(messageID.getID()), cs);
	}

	public static void sendConsoleMessage(MessageType type, MessageID messageID) {
		sendMessageToCS(type, messageID, null);
	}

	/**
	 * Sends a message with the MessageID {@code messageID} and the MessageType
	 * {@code messageType} to the CommandSender {@code cs} (player or console)
	 * replacing placeholder using java's String.format(str, args)
	 *
	 * @param type        the MessageType of the message.
	 * @param messageID   the MessageID of the Message.
	 * @param cs      the player who should receive the message.
	 * @param replacement the replacement variables
	 */
	public static void sendMessageToCSWithReplacement(MessageType type, MessageID messageID, CommandSender cs,
			Object... replacement) {
		assert InventorySorter.main != null;
		String message = Objects.requireNonNull(InventorySorter.main.getRB()).getString(messageID.getID());
		sendMessageToCS(type, String.format(message, replacement), cs);
	}

	public static void sendPermissionError(CommandSender sender, PluginPermissions permission) {
		MessageSystem.sendMessageToCS(MessageType.MISSING_PERMISSION, permission.getString(), sender);
	}

	public static void sendChangedValue(CommandSender sender, String key, String value) {
		MessageSystem.sendMessageToCSWithReplacement(MessageType.SUCCESS, MessageID.INFO_VALUE_CHANGED,
				sender,key, value);
	}

	public static void sendListPageToCS(List<String> list, CommandSender sender, String pageNrAsString,
									  int maxPageLines) {

		int pages = (int) Math.ceil(list.size() / (double) maxPageLines);
		int page;

		try {
			page = Integer.parseInt(pageNrAsString);
		} catch (NumberFormatException ex) {
			sendMessageToCSWithReplacement(MessageType.ERROR, MessageID.ERROR_VALIDATION_INTEGER, sender, pageNrAsString);
			return;
		}

		if (page < 0 || page > pages) {
			sendMessageToCSWithReplacement(MessageType.ERROR,
					MessageID.ERROR_PAGE_NUMBER, sender, "1 - " + pages);
			return;
		}

		sendMessageToCSWithReplacement(MessageType.SUCCESS,
				MessageID.COMMON_PAGE, sender, page + " / " + pages);

		for (int i = (page - 1) * maxPageLines; i < page * maxPageLines; i++) {
			if (list.size() == i) {
				break;
			} else {
				sendMessageToCS(MessageType.UNHEADED_INFORMATION, (i + 1) + ". " + list.get(i), sender);
			}
		}

		if (pages > page) {
			sendMessageToCSWithReplacement(MessageType.SUCCESS,
					MessageID.COMMON_PAGE_NEXT, sender, String.valueOf(page + 1));
		}
	}

	private static String getMessageString(MessageType type, String arg) {

		assert InventorySorter.main != null;
		String out = Objects.requireNonNull(InventorySorter.main.getRB()).getString(MessageID.COMMON_PREFIX.getID()) + " ";

		switch (type) {
			case SYNTAX_ERROR ->
					out += ChatColor.RED + InventorySorter.main.getRB().getString(MessageID.COMMON_ERROR_SYNTAX.getID()) + ": " + arg;
			case ERROR ->
					out += ChatColor.RED + InventorySorter.main.getRB().getString(MessageID.COMMON_ERROR.getID()) + ": " + arg;
			case SUCCESS -> out += ChatColor.GREEN + arg;
			case MISSING_PERMISSION ->
					out += ChatColor.RED + InventorySorter.main.getRB().getString(MessageID.ERROR_PERMISSION.getID())
							+ " (" + arg + ")";
			case UNHEADED_INFORMATION -> out = ChatColor.GRAY + arg;
			default -> throw new IllegalArgumentException();
		}

		return out;

	}

}
