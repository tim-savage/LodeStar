package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.SimpleAPI;
import com.winterhaven_mc.lodestar.messages.MessageId;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.DataStore;
import com.winterhaven_mc.lodestar.storage.Destination;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Implements command executor for LodeStar commands.
 *
 * @author Tim Savage
 * @version 1.0
 */
public class CommandManager implements CommandExecutor, TabCompleter {

	// reference to main class
	private final PluginMain plugin;

	// constants for chat colors
	private final static ChatColor helpColor = ChatColor.YELLOW;
	private final static ChatColor usageColor = ChatColor.GOLD;

	// constant list of subcommands
	private final static List<String> subcommands =
			Collections.unmodifiableList(new ArrayList<>(
					Arrays.asList("bind", "give", "delete", "destroy", "list", "set", "status", "reload", "help")));


	/**
	 * constructor method for CommandManager class
	 *
	 * @param plugin reference to main class
	 */
	public CommandManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// register this class as command executor
		Objects.requireNonNull(plugin.getCommand("lodestar")).setExecutor(this);

		// register this class as tab completer
		Objects.requireNonNull(plugin.getCommand("lodestar")).setTabCompleter(this);
	}


	/**
	 * Tab completer for LodeStar
	 */
	@Override
	public final List<String> onTabComplete(final CommandSender sender, final Command command,
											final String alias, final String[] args) {

		final List<String> returnList = new ArrayList<>();

		// if first argument, return list of valid matching subcommands
		if (args.length == 1) {

			for (String subcommand : subcommands) {
				if (sender.hasPermission("lodestar." + subcommand)
						&& subcommand.startsWith(args[0].toLowerCase())) {
					returnList.add(subcommand);
				}
			}
		}

		return returnList;
	}


	/**
	 * command executor method for LodeStar
	 */
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
							 final String label, final String[] args) {

		String subcommand;

		// get subcommand
		if (args.length > 0) {
			subcommand = args[0];
		}
		// if no arguments, display usage for all commands
		else {
			displayUsage(sender, "all");
			return true;
		}

		// status command
		if (subcommand.equalsIgnoreCase("status")) {
			return statusCommand(sender);
		}

		// reload command
		if (subcommand.equalsIgnoreCase("reload")) {
			return reloadCommand(sender, args);
		}

		// give command
		if (subcommand.equalsIgnoreCase("give")) {
			return giveCommand(sender, args);
		}

		// destroy command
		if (subcommand.equalsIgnoreCase("destroy")) {
			return destroyCommand(sender, args);
		}

		//set command
		if (subcommand.equalsIgnoreCase("set")) {
			return setCommand(sender, args);
		}

		// delete command
		if (subcommand.equalsIgnoreCase("delete") || subcommand.equalsIgnoreCase("unset")) {
			return deleteCommand(sender, args);
		}

		// bind command
		if (subcommand.equalsIgnoreCase("bind")) {
			return bindCommand(sender, args);
		}

		// list command
		if (subcommand.equalsIgnoreCase("list")) {
			return listCommand(sender, args);
		}

		// help command
		if (subcommand.equalsIgnoreCase("help")) {
			return helpCommand(sender, args);
		}

		// send invalid command message
		plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND);
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		displayUsage(sender, "help");
		return true;
	}


	/**
	 * Display plugin settings
	 *
	 * @param sender the command sender
	 * @return {@code true} if command was successful, {@code false} to display usage
	 */
	private boolean statusCommand(final CommandSender sender) {

		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission("lodestar.status")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_STATUS);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// output config settings
		String versionString = plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + "Version: "
				+ ChatColor.RESET + versionString);

		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		sender.sendMessage(ChatColor.GREEN + "Storage type: "
				+ ChatColor.RESET + plugin.dataStore.getName());

		sender.sendMessage(ChatColor.GREEN + "Default material: "
				+ ChatColor.RESET + plugin.getConfig().getString("default-material"));

		sender.sendMessage(ChatColor.GREEN + "Minimum distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("minimum-distance"));

		sender.sendMessage(ChatColor.GREEN + "Warmup: "
				+ ChatColor.RESET
				+ plugin.messageManager.getTimeString(TimeUnit.SECONDS.toMillis(
				plugin.getConfig().getInt("teleport-warmup"))));

		sender.sendMessage(ChatColor.GREEN + "Cooldown: "
				+ ChatColor.RESET
				+ plugin.messageManager.getTimeString(TimeUnit.SECONDS.toMillis(
				plugin.getConfig().getInt("teleport-cooldown"))));

		sender.sendMessage(ChatColor.GREEN + "Shift-click required: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("shift-click"));

		sender.sendMessage(ChatColor.GREEN + "Cancel on damage/movement/interaction: "
				+ ChatColor.RESET + "[ "
				+ plugin.getConfig().getBoolean("cancel-on-damage") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-movement") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-interaction") + " ]");

		sender.sendMessage(ChatColor.GREEN + "Remove from inventory: "
				+ ChatColor.RESET + plugin.getConfig().getString("remove-from-inventory"));

		sender.sendMessage(ChatColor.GREEN + "Allow in recipes: " + ChatColor.RESET
				+ plugin.getConfig().getBoolean("allow-in-recipes"));

		sender.sendMessage(ChatColor.GREEN + "From nether: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-nether"));

		sender.sendMessage(ChatColor.GREEN + "From end: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-end"));

		sender.sendMessage(ChatColor.GREEN + "Lightning: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("lightning"));

		sender.sendMessage(ChatColor.GREEN + "Enabled Words: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());

		return true;
	}


	/**
	 * Reload plugin settings
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean reloadCommand(final CommandSender sender, final String[] args) {

		// if sender does not have permission to reload config, send error message and return true
		if (!sender.hasPermission("lodestar.reload")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_RELOAD);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String subcmd = args[0];

		// argument limits
		int maxArgs = 1;

		// check max arguments
		if (args.length > maxArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER);
			displayUsage(sender, subcmd);
			return true;
		}

		// reinstall main configuration if necessary
		plugin.saveDefaultConfig();

		// reload main configuration
		plugin.reloadConfig();

		// update enabledWorlds list
		plugin.worldManager.reload();

		// reload messages
		plugin.messageManager.reload();

		// reload datastore
		DataStore.reload();

		// set debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");

		// send reloaded message
		plugin.messageManager.sendMessage(sender, MessageId.COMMAND_SUCCESS_RELOAD);
		return true;
	}


	/**
	 * Destroy a LodeStar item in hand
	 *
	 * @param sender the command sender
	 * @param args   command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean destroyCommand(final CommandSender sender, final String[] args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_CONSOLE);
			return true;
		}

		// check that sender has permission
		if (!sender.hasPermission("lodestar.destroy")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_DESTROY);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String subcmd = args[0];

		// argument limits
		int maxArgs = 1;

		// if too many arguments, send error and usage message
		if (args.length > maxArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		Player player = (Player) sender;
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// check that player is holding a LodeStar item
		if (!SimpleAPI.isLodeStar(playerItem)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_INVALID_ITEM);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}
		int quantity = playerItem.getAmount();
		String destinationName = SimpleAPI.getDestinationName(playerItem);
		playerItem.setAmount(0);
		player.getInventory().setItemInMainHand(playerItem);
		plugin.messageManager.sendMessage(sender, MessageId.COMMAND_SUCCESS_DESTROY, quantity, destinationName);
		plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_DESTROY);
		return true;
	}


	/**
	 * Set attributes on LodeStar item
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean setCommand(final CommandSender sender, final String[] args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_CONSOLE);
			return true;
		}

		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;

		// check for permission
		if (!sender.hasPermission("lodestar.set")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_SET);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check min arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		Player player = (Player) sender;
		Location location = player.getLocation();

		// set destinationName to passed argument
		String destinationName = String.join(" ", arguments);

		// check if destination name is a reserved name
		if (SimpleAPI.isReservedName(destinationName)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_SET_RESERVED, destinationName);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check if destination name exists and if so if player has overwrite permission
		Destination destination = plugin.dataStore.getRecord(destinationName);

		// check for overwrite permission if destination already exists
		if (destination != null && sender.hasPermission("lodestar.set.overwrite")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_OVERWRITE, destinationName);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// send warning message if name begins with a number
		if (Destination.deriveKey(destinationName).matches("^\\d*_.*")) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_WARN_SET_NUMERIC_PREFIX, destinationName);
		}

		// create destination object
		destination = new Destination(destinationName, location);

		// store destination object
		plugin.dataStore.putRecord(destination);

		// send success message to player
		plugin.messageManager.sendMessage(sender, MessageId.COMMAND_SUCCESS_SET, destinationName);

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		return true;
	}


	/**
	 * Remove named destination
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean deleteCommand(final CommandSender sender, final String[] args) {

		// check for permission
		if (!sender.hasPermission("lodestar.delete")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_DELETE);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}
		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;

		// check min arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER);
			displayUsage(sender, subcmd);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String destinationName = String.join(" ", arguments);
		String key = Destination.deriveKey(destinationName);

		// test that destination name is not reserved name
		if (SimpleAPI.isReservedName(destinationName)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_DELETE_RESERVED, destinationName);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// test that destination name is valid
		if (!SimpleAPI.isValidDestination(destinationName)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION, destinationName);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// remove destination record from storage
		plugin.dataStore.deleteRecord(key);

		// send success message to player
		plugin.messageManager.sendMessage(sender, MessageId.COMMAND_SUCCESS_DELETE, destinationName);

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_DELETE);
		return true;
	}


	/**
	 * Bind item in hand to destination
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean bindCommand(final CommandSender sender, final String[] args) {

		// command sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_CONSOLE);
			return true;
		}

		// check sender has permission
		if (!sender.hasPermission("lodestar.bind")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_BIND);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;

		// check minimum arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		Player player = (Player) sender;
		String destinationName = String.join(" ", arguments);

		// test that destination name is valid
		if (!SimpleAPI.isValidDestination(destinationName)) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION, destinationName);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// if default-item-only configured true, check that item in hand has default material and data
		if (plugin.getConfig().getBoolean("default-material-only")
				&& !sender.hasPermission("lodestar.default-override")) {
			if (!SimpleAPI.isDefaultItem(playerItem)) {
				plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_INVALID_ITEM, destinationName);
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// try to get formatted destination name from storage
		Destination destination = plugin.dataStore.getRecord(destinationName);
		if (destination != null) {
			destinationName = destination.getDisplayName();
		}

		// set destination in item lore
		SimpleAPI.setMetaData(playerItem, destinationName);

		// send success message
		plugin.messageManager.sendMessage(sender, MessageId.COMMAND_SUCCESS_BIND, destinationName);

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_BIND);
		return true;
	}


	/**
	 * List LodeStar destination names
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean listCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to list destinations, output error message and return true
		if (!sender.hasPermission("lodestar.list")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_LIST);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String subcmd = args[0];
		// argument limits
		int maxArgs = 2;

		if (args.length > maxArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		int page = 1;

		if (args.length == 2) {
			try {
				page = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e) {
				// second argument not a page number, let default of 1 stand
			}
		}
		page = Math.max(1, page);

		int itemsPerPage = 20;

		List<String> displayNames = plugin.dataStore.getAllKeys();

		int pageCount = (displayNames.size() / itemsPerPage) + 1;
		if (page > pageCount) {
			page = pageCount;
		}
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page * itemsPerPage), displayNames.size());

		List<String> displayRange = displayNames.subList(startIndex, endIndex);

		sender.sendMessage(ChatColor.DARK_AQUA + "page " + page + " of " + pageCount);
		sender.sendMessage(ChatColor.AQUA + displayRange.toString().substring(1, displayRange.toString().length() - 1));
		return true;
	}


	/**
	 * Display help message for commands
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean helpCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission("lodestar.help")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_HELP);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String command = "help";

		if (args.length > 1) {
			command = args[1];
		}

		String helpMessage = "That is not a valid LodeStar command.";

		if (command.equalsIgnoreCase("bind")) {
			helpMessage = "Binds a LodeStar destination to an item in hand.";
		}
		if (command.equalsIgnoreCase("give")) {
			helpMessage = "Give LodeStar items directly to players.";
		}
		if (command.equalsIgnoreCase("delete")) {
			helpMessage = "Removes a LodeStar destination.";
		}
		if (command.equalsIgnoreCase("destroy")) {
			helpMessage = "Destroys a LodeStar item in hand.";
		}
		if (command.equalsIgnoreCase("help")) {
			helpMessage = "Displays help for LodeStar commands.";
		}
		if (command.equalsIgnoreCase("list")) {
			helpMessage = "Displays a list of all LodeStar destinations.";
		}
		if (command.equalsIgnoreCase("reload")) {
			helpMessage = "Reloads the configuration without needing to restart the server.";
		}
		if (command.equalsIgnoreCase("set")) {
			helpMessage = "Creates a LodeStar destination at current player location.";
		}
		if (command.equalsIgnoreCase("status")) {
			helpMessage = "Displays current configuration settings.";
		}
		sender.sendMessage(helpColor + helpMessage);
		displayUsage(sender, command);
		return true;
	}


	/**
	 * Give a LodeStar item to a player
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean giveCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to give LodeStars, output error message and return true
		if (!sender.hasPermission("lodestar.give")) {
			plugin.messageManager.sendMessage(sender, MessageId.PERMISSION_DENIED_GIVE);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		// argument limits
		int minArgs = 2;

		// if too few arguments, send error and usage message
		if (args.length < minArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		// get required argument target player name
		String targetPlayerName = arguments.get(0);

		// remove targetPlayerName from ArrayList
		arguments.remove(0);

		// try to match target player name to currently online player
		Player targetPlayer = matchPlayer(sender, targetPlayerName);

		// if no match, do nothing and return (message was output by matchPlayer method)
		if (targetPlayer == null) {
			return true;
		}

		//------------------------

		// set destinationName to empty string
		String destinationName = "";

		// set default quantity
		int quantity = 1;

		Material material = null;

		// try to parse first argument as integer quantity
		if (!arguments.isEmpty()) {
			try {
				quantity = Integer.parseInt(arguments.get(0));

				// remove argument if no exception thrown
				arguments.remove(0);
			}
			catch (NumberFormatException e) {
				// not an integer, do nothing
			}
		}

		// if no remaining arguments, check if item in hand is LodeStar item
		if (arguments.isEmpty()) {

			// if sender is not player, send args-count-under error message
			if (!(sender instanceof Player)) {
				plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER);
				displayUsage(sender, subcmd);
				return true;
			}

			Player player = (Player) sender;
			ItemStack playerItem = player.getInventory().getItemInMainHand().clone();

			// if item in hand is a LodeStar item, set destination and material from item
			if (SimpleAPI.isLodeStar(playerItem)) {

				destinationName = SimpleAPI.getDestinationName(playerItem);
				material = playerItem.getType();
			}
		}

		// try to parse all remaining arguments as destinationName
		if (!arguments.isEmpty()) {

			String testName = String.join(" ", arguments);

			// if resulting name is valid destination, set to destinationName
			if (SimpleAPI.isValidDestination(testName)) {
				destinationName = testName;

				// set arguments to empty list
				arguments.clear();
			}
		}

		// try to parse next argument as material
		if (!arguments.isEmpty()) {
			String[] materialElements = arguments.get(0).split("\\s*:\\s*");

			// try to match material
			if (materialElements.length > 0) {
				material = Material.matchMaterial(materialElements[0]);
			}

			// if material matched, remove argument from list
			if (material != null) {
				arguments.remove(0);
			}
		}

		// try to parse all remaining arguments as destinationName
		if (!arguments.isEmpty()) {
			String testName = String.join(" ", arguments);

			// if resulting name is valid destination, set to destinationName
			if (SimpleAPI.isValidDestination(testName)) {
				destinationName = testName;

				// set arguments to empty list
				arguments.clear();
			}
			// else given destination is invalid (but not blank), so send error message
			else {
				plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION, testName);
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// if no destination name set, set destination to spawn
		if (destinationName.isEmpty()) {
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
		SimpleAPI.setMetaData(itemStack, destinationName);

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

		String key = SimpleAPI.getKey(itemStack);
		int quantity = itemStack.getAmount();
		int maxGiveAmount = plugin.getConfig().getInt("max-give-amount");

		// check quantity against configured max give amount
		if (maxGiveAmount >= 0) {
			quantity = Math.min(maxGiveAmount, quantity);
			itemStack.setAmount(quantity);
		}

		// test that item is a LodeStar item
		if (!SimpleAPI.isLodeStar(itemStack)) {
			plugin.messageManager.sendMessage(giver, MessageId.COMMAND_FAIL_INVALID_ITEM);
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
			plugin.messageManager.sendMessage(giver, MessageId.COMMAND_FAIL_GIVE_INVENTORY_FULL, quantity);
			return false;
		}

		// subtract noFitCount from quantity
		quantity = quantity - noFitCount;

		// get destination display name
		String destinationName = SimpleAPI.getDestinationName(key);

		// don't display messages if giving item to self
		if (!giver.getName().equals(targetPlayer.getName())) {

			// send message and play sound to giver
			plugin.messageManager.sendMessage(giver, MessageId.COMMAND_SUCCESS_GIVE, quantity,
					destinationName, targetPlayer.getName());

			// if giver is in game, play sound
			if (giver instanceof Player) {
				plugin.soundConfig.playSound(giver, SoundId.COMMAND_SUCCESS_GIVE_SENDER);
			}

			// send message to target player
			plugin.messageManager.sendMessage(targetPlayer, MessageId.COMMAND_SUCCESS_GIVE_TARGET, quantity,
					destinationName, giver.getName());
		}
		// play sound to target player
		plugin.soundConfig.playSound(targetPlayer, SoundId.COMMAND_SUCCESS_GIVE_TARGET);
		return true;
	}


	/**
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
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_PLAYER_NOT_FOUND);
		}
		else {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_PLAYER_NOT_ONLINE);
		}
		return null;
	}


	/**
	 * Display command usage
	 *
	 * @param sender  the command sender
	 * @param command the command for which to display usage string
	 */
	private void displayUsage(final CommandSender sender, String command) {

		if (command.isEmpty() || command.equalsIgnoreCase("help")) {
			command = "all";
		}
		if ((command.equalsIgnoreCase("status")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.status")) {
			sender.sendMessage(usageColor + "/lodestar status");
		}
		if ((command.equalsIgnoreCase("reload")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.reload")) {
			sender.sendMessage(usageColor + "/lodestar reload");
		}
		if ((command.equalsIgnoreCase("destroy")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.destroy")) {
			sender.sendMessage(usageColor + "/lodestar destroy");
		}
		if ((command.equalsIgnoreCase("set")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.set")) {
			sender.sendMessage(usageColor + "/lodestar set <destination_name>");
		}
		if ((command.equalsIgnoreCase("delete")
				|| command.equalsIgnoreCase("unset")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.delete")) {
			sender.sendMessage(usageColor + "/lodestar delete <destination_name>");
		}
		if ((command.equalsIgnoreCase("help")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.help")) {
			sender.sendMessage(usageColor + "/lodestar help [command]");
		}
		if ((command.equalsIgnoreCase("list")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.list")) {
			sender.sendMessage(usageColor + "/lodestar list [page]");
		}
		if ((command.equalsIgnoreCase("bind")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.bind")) {
			sender.sendMessage(usageColor + "/lodestar bind <destination_name>");
		}
		if ((command.equalsIgnoreCase("give")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.give")) {
			if (plugin.getConfig().getBoolean("default-item-only")) {
				sender.sendMessage(usageColor + "/lodestar give <player> [quantity] [destination_name]");
			}
			else {
				sender.sendMessage(usageColor + "/lodestar give <player> [quantity] [material] [destination_name]");
			}
		}
	}

}
