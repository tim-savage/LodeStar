package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.winterhavenmc.lodestar.messages.Macro.DESTINATION;
import static com.winterhavenmc.lodestar.messages.MessageId.*;
import static com.winterhavenmc.lodestar.messages.MessageId.COMMAND_FAIL_INVALID_DESTINATION;


final class TeleportCommand extends AbstractCommand {

	private final PluginMain plugin;


	TeleportCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("teleport");
		this.setUsage("/lodestar teleport <destination name>");
		this.setDescription(COMMAND_HELP_TELEPORT);
		this.setMinArgs(1);
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		if (args.length == 2) {
			return plugin.dataStore.selectAllKeys();
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// check for permission
		if (!sender.hasPermission("lodestar.teleport")) {
			plugin.messageBuilder.build(sender, PERMISSION_DENIED_TELEPORT).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check for in game player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check min arguments
		if (args.size() < getMinArgs()) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// cast sender to player
		Player player = (Player) sender;

		// join remaining arguments to get destination name
		String destinationName = String.join(" ", args);

		// test that destination name is valid
		if (!Destination.exists(destinationName)) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get destination from datastore
		Destination destination = plugin.dataStore.selectRecord(destinationName);

		if (destination != null && destination.getLocation() != null) {
			plugin.soundConfig.playSound(player.getLocation(), SoundId.TELEPORT_SUCCESS_DEPARTURE);
			player.teleport(destination.getLocation());
			plugin.messageBuilder.build(sender, TELEPORT_SUCCESS).setMacro(DESTINATION, destination).send();
			plugin.soundConfig.playSound(destination.getLocation(), SoundId.TELEPORT_SUCCESS_ARRIVAL);
			return true;
		}
		else {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_INVALID_DESTINATION).send();
			plugin.soundConfig.playSound(sender, SoundId.TELEPORT_DENIED_WORLD_DISABLED);
		}

		return true;
	}

}
