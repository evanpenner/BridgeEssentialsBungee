package com.fireboyev.bridgeessentialsbungee.discord;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.md_5.bungee.config.Configuration;

public class DiscordService {
	private JDA JDA;

	public DiscordService(JDABuilder builder, Configuration config) {
		JDABuilder jdaBuilder = builder;
		jdaBuilder.setToken(config.getString("discord.bot-token"));
		jdaBuilder.setAutoReconnect(true);
		jdaBuilder.setActivity(Activity.streaming("The server", config.getString("website", "http://google.com")));
		try {
			this.JDA = jdaBuilder.build();
		} catch (LoginException e) {
			System.err.println("[BridgeEssentialsBungee] Error initializing DiscordService");
			e.printStackTrace();
		}
	}

	public JDA getJDA() {
		return this.JDA;
	}

	public void shutdown() {
		this.JDA.shutdown();
	}
}
