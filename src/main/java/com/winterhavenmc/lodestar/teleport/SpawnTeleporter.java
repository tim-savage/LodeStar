/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.lodestar.teleport;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.storage.Destination;

import org.bukkit.entity.Player;


final class SpawnTeleporter extends AbstractTeleporter implements Teleporter {

	private final TeleportExecutor teleportExecutor;


	/**
	 * Class constructor
	 *
	 * @param plugin the player to teleport
	 * @param teleportExecutor the teleport executor
	 */
	SpawnTeleporter(final PluginMain plugin, final TeleportExecutor teleportExecutor) {
		super(plugin);
		this.teleportExecutor = teleportExecutor;
	}


	/**
	 * Begin teleport to world spawn destination
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player) {
		getSpawnDestination(player).ifPresentOrElse(
				destination -> execute(player, destination, MessageId.TELEPORT_WARMUP_SPAWN),
				() -> sendInvalidDestinationMessage(player, plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"))
		);
	}


	/**
	 * Execute the teleport to destination
	 *
	 * @param player      the player to teleport
	 * @param destination the destination
	 * @param messageId   the teleport warmup message to send to player
	 */
	@Override
	public void execute(final Player player, final Destination destination, final MessageId messageId) {
		teleportExecutor.execute(player, destination, messageId);
	}

}
