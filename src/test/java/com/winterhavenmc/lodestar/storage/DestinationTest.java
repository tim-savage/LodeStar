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

package com.winterhavenmc.lodestar.storage;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhavenmc.lodestar.PluginMain;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DestinationTest {

	private ServerMock server;
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private PluginMain plugin;


	@BeforeAll
	public void setUp() {
		// Start the mock server
		server = MockBukkit.mock();

		// start the mock plugin
		plugin = MockBukkit.load(PluginMain.class);
	}

	@AfterAll
	public void tearDown() {
		// Stop the mock server
		MockBukkit.unmock();
	}


	@Test
	void testToString() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals("&aTest Destination", destination.toString());
		assertNotEquals("WrongName", destination.toString());
	}

	@Test
	void isHome() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination homeDestination = new Destination("Home", location, Destination.Type.HOME);
		Destination spawnDestination = new Destination("Home", location, Destination.Type.SPAWN);
		assertTrue(homeDestination.isHome());
		assertFalse(spawnDestination.isHome());
	}

	@Test
	void isSpawn() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination homeDestination = new Destination("Spawn", location, Destination.Type.HOME);
		Destination spawnDestination = new Destination("Spawn", location, Destination.Type.SPAWN);
		assertTrue(spawnDestination.isSpawn());
		assertFalse(homeDestination.isSpawn());
	}

	@Test
	void getKey() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals("Test_Destination", destination.getKey());
		assertNotEquals("&aTest Destination", destination.getKey());
	}

	@Test
	void getDisplayName() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals("&aTest Destination", destination.getDisplayName());
		assertNotEquals("Test_Destination", destination.getDisplayName());
	}


//NOTE: MockBukkit does not provide world uid, so these methods can not be tested using the framework

//	@Test
//	void getLocation() {
//		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
//		Location otherLocation = new Location(server.getWorld("world"), 200.0, 200.0, 200.0);
//		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
//
//		System.out.println(server.getWorld("world").getUID().toString());
//		assertTrue(destination.getLocation().isPresent());
//		assertEquals(location, destination.getLocation().get());
//		assertNotEquals(otherLocation, destination.getLocation().get());
//	}

//	@Test
//	void getWorldUid() {
//	}

//	@Test
//	void getWorldName() {
//		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
//		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
//		assertEquals("world", destination.getWorldName());
//	}

//	@Test
//	void isWorldValid() {
//	}


	@Test
	void getX() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals(100.0, destination.getX());
		assertNotEquals(200.0, destination.getX());
	}

	@Test
	void getY() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals(100.0, destination.getY());
		assertNotEquals(200.0, destination.getY());
	}

	@Test
	void getZ() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals(100.0, destination.getZ());
		assertNotEquals(200.0, destination.getZ());
	}

	@Test
	void getYaw() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0, 90.0F, 90.0F);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals(90.0F, destination.getYaw());
		assertNotEquals(180.0F, destination.getYaw());
	}

	@Test
	void getPitch() {
		Location location = new Location(server.getWorld("world"), 100.0, 100.0, 100.0, 90.0F, 90.0F);
		Destination destination = new Destination("&aTest Destination", location, Destination.Type.STORED);
		assertEquals(90.0F, destination.getPitch());
		assertNotEquals(180.0F, destination.getPitch());
	}

}