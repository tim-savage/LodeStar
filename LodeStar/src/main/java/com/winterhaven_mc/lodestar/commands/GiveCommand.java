package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.Message;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;
import com.winterhaven_mc.lodestar.util.LodeStar;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.winterhaven_mc.lodestar.messages.Macro.*;
import static com.winterhaven_mc.lodestar.messages.MessageId.*;


public class GiveCommand extends AbstractCommand {

	private final PluginMain plugin;

	final static String altUsageString = "/lodestar give <player> [quantity] [destination_name]";


	GiveCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("give");
		this.setUsage("/lodestar give <player> [quantity] [material] [destination_name]");
		this.setDescription(COMMAND_HELP_GIVE);
		this.setMinArgs(1);
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		List<String> returnList = new ArrayList<>();

		if (args.length == 2) {
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				returnList.add(player.getName());
			}
		}

		else if (args.length == 3) {
			return plugin.dataStore.selectAllKeys();
		}

		return returnList;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if command sender does not have permission to give LodeStars, output error message and return true
		if (!sender.hasPermission("lodestar.give")) {
			Message.create(sender, PERMISSION_DENIED_GIVE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// if too few arguments, send error and usage message
		if (args.size() < getMinArgs()) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// get required argument target player name
		String targetPlayerName = args.get(0);

		// remove targetPlayerName from ArrayList
		args.remove(0);

		// try to match target player name to currently online player
		Player targetPlayer = matchPlayer(sender, targetPlayerName);

		// if no match, do nothing and return (message was output by matchPlayer method)
		if (targetPlayer == null) {
			return true;
		}

		// initialize destinationName to empty string
		String destinationName = "";

		// initialize default quantity
		int quantity = 1;

		// initialize material as null
		Material material = null;

		// try to parse first argument as integer quantity
		if (!args.isEmpty()) {
			try {
				quantity = Integer.parseInt(args.get(0));

				// remove argument if no exception thrown
				args.remove(0);
			}
			catch (NumberFormatException e) {
				// not an integer, do nothing
			}
		}

		// if no remaining arguments, check if item in hand is LodeStar item
		if (args.isEmpty()) {

			// if sender is not player, send args-count-under error message
			if (!(sender instanceof Player)) {
				Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
				displayUsage(sender);
				return true;
			}

			Player player = (Player) sender;
			ItemStack playerItem = player.getInventory().getItemInMainHand().clone();

			// if item in hand is a LodeStar item, set destination and material from item
			if (LodeStar.isItem(playerItem)) {

				destinationName = LodeStar.getDestinationName(playerItem);
				material = playerItem.getType();
			}
		}

		// try to parse all remaining arguments as destinationName
		else {

			// join remaining arguments with spaces
			String testName = String.join(" ", args);

			// if resulting name is existing destination, get destinationName from datastore
			if (Destination.exists(testName)) {
				destinationName = Destination.getDisplayName(testName);

				// remove remaining arguments
				args.clear();
			}
		}

		// try to parse next argument as material
		if (!args.isEmpty()) {

			// try to match material
			material = Material.matchMaterial(args.get(0));

			// if material matched, remove argument from list
			if (material != null) {
				args.remove(0);
			}
		}

		// try to parse all remaining arguments as destinationName
		if (!args.isEmpty()) {
			String testName = String.join(" ", args);

			// if resulting name is existing destination, get destinationName from datastore
			if (Destination.exists(testName)) {
				destinationName = Destination.getDisplayName(testName);

				// remove remaining arguments
				args.clear();
			}

			// else given destination is invalid (but not blank), so send error message
			else {
				Message.create(sender, COMMAND_FAIL_INVALID_DESTINATION)
						.setMacro(DESTINATION, testName)
						.send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// if no destination name set, set destination to spawn
		if (destinationName == null || destinationName.isEmpty()) {
			destinationName = "spawn";
		}

		// if no material set or default-material-only configured true, try to parse material from config
		if (material == null || plugin.getConfig().getBoolean("default-material-only")) {
			material = Material.matchMaterial(Objects.requireNonNull(plugin.getConfig().getString("default-material")));
		}

		// if still no material match, set to nether star
		if (material == null) {
			material = Material.NETHER_STAR;
		}

		// create item stack with material, quantity and data
		ItemStack itemStack = new ItemStack(material, quantity);

		// set item metadata on item stack
		LodeStar.setMetaData(itemStack, destinationName);

		// give item stack to target player
		giveItem(sender, targetPlayer, itemStack);

		return true;
	}


	/**
	 * Helper method for give command
	 *
	 * @param giver        the player issuing the command
	 * @param targetPlayer the player being given item
	 * @param itemStack    the LodeStar item being given
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	@SuppressWarnings("UnusedReturnValue")
	private boolean giveItem(final CommandSender giver, final Player targetPlayer, final ItemStack itemStack) {

		String key = LodeStar.getKey(itemStack);
		int quantity = itemStack.getAmount();
		int maxGiveAmount = plugin.getConfig().getInt("max-give-amount");

		// check quantity against configured max give amount
		if (maxGiveAmount >= 0) {
			quantity = Math.min(maxGiveAmount, quantity);
			itemStack.setAmount(quantity);
		}

		// test that item is a LodeStar item
		if (!LodeStar.isItem(itemStack)) {
			Message.create(giver, COMMAND_FAIL_INVALID_ITEM).send();
			plugin.soundConfig.playSound(giver, SoundId.COMMAND_FAIL);
			return true;
		}

		// add specified quantity of LodeStars to player inventory
		HashMap<Integer, ItemStack> noFit = targetPlayer.getInventory().addItem(itemStack);

		// count items that didn't fit in inventory
		int noFitCount = 0;
		for (int index : noFit.keySet()) {
			noFitCount += noFit.get(index).getAmount();
		}

		// if remaining items equals quantity given, send player-inventory-full message and return
		if (noFitCount == quantity) {
			Message.create(giver, COMMAND_FAIL_GIVE_INVENTORY_FULL)
					.setMacro(ITEM_QUANTITY, quantity)
					.send();
			return false;
		}

		// subtract noFitCount from quantity
		quantity = quantity - noFitCount;

		// get destination display name
		String destinationName = Destination.getDisplayName(key);

		// don't display messages if giving item to self
		if (!giver.getName().equals(targetPlayer.getName())) {

			// send message and play sound to giver
			Message.create(giver, COMMAND_SUCCESS_GIVE)
					.setMacro(DESTINATION, destinationName)
					.setMacro(ITEM_QUANTITY, quantity)
					.setMacro(TARGET_PLAYER, targetPlayer)
					.send();

			// if giver is in game, play sound
			if (giver instanceof Player) {
				plugin.soundConfig.playSound(giver, SoundId.COMMAND_SUCCESS_GIVE_SENDER);
			}

			// send message to target player
			Message.create(targetPlayer, COMMAND_SUCCESS_GIVE_TARGET)
					.setMacro(DESTINATION, destinationName)
					.setMacro(ITEM_QUANTITY, quantity)
					.setMacro(TARGET_PLAYER, giver)
					.send();
		}

		// play sound to target player
		plugin.soundConfig.playSound(targetPlayer, SoundId.COMMAND_SUCCESS_GIVE_TARGET);
		return true;
	}


	/**
	 * Match a player name to player object
	 *
	 * @param sender           the player issuing the command
	 * @param targetPlayerName the player name to match
	 * @return the matched player object, or null if no match
	 */
	private Player matchPlayer(final CommandSender sender, final String targetPlayerName) {

		Player targetPlayer;

		// check exact match first
		targetPlayer = plugin.getServer().getPlayer(targetPlayerName);

		// if no match, try substring match
		if (targetPlayer == null) {
			List<Player> playerList = plugin.getServer().matchPlayer(targetPlayerName);

			// if only one matching player, use it, otherwise send error message (no match or more than 1 match)
			if (playerList.size() == 1) {
				targetPlayer = playerList.get(0);
			}
		}

		// if match found, return target player object
		if (targetPlayer != null) {
			return targetPlayer;
		}

		// check if name matches known offline player
		HashSet<OfflinePlayer> matchedPlayers = new HashSet<>();
		for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
			if (targetPlayerName.equalsIgnoreCase(offlinePlayer.getName())) {
				matchedPlayers.add(offlinePlayer);
			}
		}
		if (matchedPlayers.isEmpty()) {
			Message.create(sender, COMMAND_FAIL_PLAYER_NOT_FOUND).send();
		}
		else {
			Message.create(sender, COMMAND_FAIL_PLAYER_NOT_ONLINE).send();
		}
		return null;
	}

}
