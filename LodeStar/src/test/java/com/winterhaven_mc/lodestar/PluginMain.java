package com.winterhaven_mc.lodestar;

import com.winterhaven_mc.lodestar.commands.CommandManager;
import com.winterhaven_mc.lodestar.listeners.PlayerEventListener;
import com.winterhaven_mc.lodestar.storage.DataStore;
import com.winterhaven_mc.lodestar.teleport.TeleportManager;
import com.winterhaven_mc.lodestar.util.LodeStarFactory;
import com.winterhaven_mc.util.LanguageHandler;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.WorldManager;
import com.winterhaven_mc.util.YamlSoundConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;


/**
 * Bukkit plugin to create items that return player to
 * world spawn when clicked.<br>
 * An alternative to the /spawn command.
 *
 * @author Tim Savage
 */
public final class PluginMain extends JavaPlugin {

	// global debug field
	public Boolean debug = getConfig().getBoolean("debug");

	public LanguageHandler languageHandler;
	public DataStore dataStore;
	public TeleportManager teleportManager;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public CommandManager commandManager;
	public PlayerEventListener playerEventListener;
	public LodeStarFactory lodeStarFactory;


	/**
	 * Constructor for mocking
	 */
	@SuppressWarnings("unused")
	public PluginMain() {
		super();
	}


	/**
	 * Constructor for mocking
	 */
	@SuppressWarnings("unused")
	private PluginMain(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
		super(loader, descriptionFile, dataFolder, file);
	}


	@Override
	public void onEnable() {

		// install default config.yml if not present
		saveDefaultConfig();

		// instantiate language handler
		languageHandler = new LanguageHandler(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// get initialized destination storage object
//		dataStore = DataStore.create();

		// instantiate teleport manager
		teleportManager = new TeleportManager(this);

		// instantiate command manager
		commandManager = new CommandManager(this);

		// instantiate player listener
		playerEventListener = new PlayerEventListener(this);

		// instantiate lodestar factory
		lodeStarFactory = new LodeStarFactory(this);
	}


	@Override
	public void onDisable() {
//		dataStore.close();
	}

}
