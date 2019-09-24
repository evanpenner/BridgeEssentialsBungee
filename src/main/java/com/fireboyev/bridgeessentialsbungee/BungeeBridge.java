package com.fireboyev.bridgeessentialsbungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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
	private static ClassPathHacker cph;

	@Override
	public void onEnable() {
		resolveConfig();
		cph = new ClassPathHacker(this);
		getLogger().info("Downloading Required JDA Libraries...");
		DownloadLibraries(new File(this.getDataFolder(), "lib/"));
		getLogger().info("Library Download Complete.");
		getLogger().info("Loading JDA Library...");
		File jar = new File(new File(this.getDataFolder(), "lib/"), "JDA-" + JDAVERSION + ".jar");
		cph.loadJar(jar.toPath());
		JDABuilder builder = new JDABuilder();
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

	private JDABuilder LoadJDA(File folder) {

		File myJar = new File(folder, "JDA-" + JDAVERSION + ".jar");
		try {

			Constructor<?> cs = ClassLoader.getSystemClassLoader().loadClass("net.dv8tion.jda.api.JDABuilder")
					.getConstructor(String.class);
			JDABuilder instance = (JDABuilder) cs.newInstance();
			return instance;
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JDABuilder();
	}

	private void initDiscordService(JDABuilder builder) {
		getLogger().info("Initializing DiscordService...");
		dService = new DiscordService(builder, this.config);
		getLogger().info("Completed initialization of DiscordService");
	}
}
