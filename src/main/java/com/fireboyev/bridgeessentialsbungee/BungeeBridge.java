package com.fireboyev.bridgeessentialsbungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

import com.fireboyev.bridgeessentialsbungee.discord.DiscordService;

import net.dv8tion.jda.api.JDABuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeBridge extends Plugin {
	private final String JDAVERSION = "4.0.0_39";
	private DiscordService dService;
	public Configuration config;

	@Override
	public void onEnable() {
		resolveConfig();
		getLogger().info("Downloading Required JDA Libraries...");
		DownloadLibraries(new File(this.getDataFolder(), "lib/"));
		getLogger().info("Library Download Complete.");
		getLogger().info("Loading JDA Library...");
		JDABuilder builder = LoadJDA(new File(this.getDataFolder(), "lib/"));
		getLogger().info("JDA Library Loaded.");
		getLogger().info("Loading Configuration Files");
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class)
					.load(new File(getDataFolder(), "config.yml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		getLogger().info("Configuration Files Loaded.");
		initDiscordService(builder);
		getLogger().info(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " loaded!");
		getLogger().info(this.getDescription().getName() + " created by " + this.getDescription().getAuthor());
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabling Plugin...");
		getLogger().info("Shutting down Discord Service...");
		dService.shutdown();
		getLogger().info("Discord Service Shutdown.");
		getLogger().info("Plugin Disabled.");
	}

	private File resolveConfig() {

		File configFile = new File(this.getDataFolder(), "config.yml");

		if (!configFile.exists()) {
			this.getDataFolder().mkdirs();
			try (InputStream is = this.getResourceAsStream("config.yml")) {
				Files.copy(is, configFile.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return configFile;
	}

	private void DownloadLibraries(File folder) {
		try {
			String fromUrl = "https://github.com/DV8FromTheWorld/JDA/releases/download/v4.0.0/JDA-4.0.0_39-withDependencies-no-opus.jar";
			File localFile = new File(folder, "JDA-" + JDAVERSION + ".jar");
			if (localFile.exists()) {
				return;
			}
			folder.mkdirs();
			localFile.createNewFile();
			URL url = new URL(fromUrl);
			ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
			FileOutputStream fileOutputStream = new FileOutputStream(localFile);

			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			fileOutputStream.close();
			readableByteChannel.close();
		} catch (IOException e) {
			getLogger().severe("Error downloading required JDA library!");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private JDABuilder LoadJDA(File folder) {
		try {
			File myJar = new File(folder, "JDA-" + JDAVERSION + ".jar");
			URL url = myJar.toURI().toURL();
			URL[] urls = new URL[] { url };
			URLClassLoader classLoader = new URLClassLoader(urls);
			Class<JDABuilder> classJda = (Class<JDABuilder>) classLoader.loadClass("net.dv8tion.jda.api.JDABuilder");

			JDABuilder instance = classJda.newInstance();
			classLoader.close();
			return instance;
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | ClassNotFoundException
				| InstantiationException | IOException e) {
			getLogger().severe("Unable to load JDA library!");
			e.printStackTrace();
		}
		return null;
	}

	private void initDiscordService(JDABuilder builder) {
		getLogger().info("Initializing DiscordService...");
		dService = new DiscordService(builder, this.config);
		getLogger().info("Completed initialization of DiscordService");
	}
}
