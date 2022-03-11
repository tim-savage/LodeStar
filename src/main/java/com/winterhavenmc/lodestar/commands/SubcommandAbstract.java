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

package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.messages.MessageId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


abstract class SubcommandAbstract implements Subcommand {

	protected String name;
	protected Collection<String> aliases = new HashSet<>();
	protected String permissionNode;
	protected String usageString;
	protected MessageId description;
	protected int minArgs;
	protected int maxArgs;


	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPermissionNode() {
		return permissionNode;
	}

	@Override
	public Collection<String> getAliases() {
		return aliases;
	}

	@Override
	public void addAlias(final String alias) {
		this.aliases.add(alias);
	}

	@Override
	public String getUsage() {
		return usageString;
	}

	@Override
	public void displayUsage(final CommandSender sender) {
		sender.sendMessage(usageString);
	}

	@Override
	public MessageId getDescription() {
		return description;
	}

	@Override
	public int getMinArgs() { return minArgs; }

	@Override
	public int getMaxArgs() { return maxArgs; }

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		return Collections.emptyList();
	}

}
