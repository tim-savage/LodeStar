package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

import static com.winterhavenmc.lodestar.sounds.SoundId.COMMAND_INVALID;


/**
 * Implements command executor for LodeStar commands.
 */
@SuppressWarnings("NullableProblems")
public class CommandManager implements CommandExecutor, TabCompleter {

	// reference to main class
	private final PluginMain plugin;

	// instantiate subcommand map
	private final SubcommandMap subcommandMap = new SubcommandMap();


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

		// register subcommands
		for (SubcommandType subcommandType : SubcommandType.values()) {
			subcommandType.register(plugin, subcommandMap);
		}
	}


	/**
	 * Tab completer for LodeStar
	 */
	@Override
	public final List<String> onTabComplete(final CommandSender sender, final Command command,
											final String alias, final String[] args) {

		// if more than one argument, use tab completer of subcommand
		if (args.length > 1) {

			// get subcommand from map
			Subcommand subcommand = subcommandMap.getCommand(args[0]);

			// if no subcommand returned from map, return empty list
			if (subcommand == null) {
				return Collections.emptyList();
			}

			// return subcommand tab completer output
			return subcommand.onTabComplete(sender, command, alias, args);
		}

		// return list of subcommands for which sender has permission
		return matchingCommands(sender, args[0]);
	}


	/**
	 * command executor method for LodeStar
	 */
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
							 final String label, final String[] args) {

		// convert args array to list
		List<String> argsList = new ArrayList<>(Arrays.asList(args));

		String subcommandName;

		// get subcommand, remove from front of list
		if (argsList.size() > 0) {
			subcommandName = argsList.remove(0);
		}

		// if no arguments, set command to help
		else {
			subcommandName = "help";
		}

		// get subcommand from map by name
		Subcommand subcommand = subcommandMap.getCommand(subcommandName);

		// if subcommand is null, get help command from map
		if (subcommand == null) {
			subcommand = subcommandMap.getCommand("help");
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND).send();
			plugin.soundConfig.playSound(sender, COMMAND_INVALID);
		}

		// execute subcommand
		return subcommand.onCommand(sender, argsList);
	}


	/**
	 * Get matching list of subcommands for which sender has permission
	 * @param sender the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender has permission
	 */
	private List<String> matchingCommands(CommandSender sender, String matchString) {

		List<String> returnList = new ArrayList<>();

		for (String subcommand : subcommandMap.getKeys()) {
			if (sender.hasPermission("lodestar." + subcommand)
					&& subcommand.startsWith(matchString.toLowerCase())) {
				returnList.add(subcommand);
			}
		}
		return returnList;
	}

}