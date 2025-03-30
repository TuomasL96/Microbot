package net.runelite.client.plugins.microbot.scout;

import com.google.inject.Provides;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;

import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


import java.awt.*;

import javax.inject.Inject;


@PluginDescriptor(
        name = PluginDescriptor.Default + "Player Scouter",
        description = "Player Scouter with discord integration",
        tags = {"discord", "scout", "pk"},
        enabledByDefault = false
)

public class ScoutPlugin extends Plugin {
    @Inject
    private ScoutConfig config;
    @Inject
    private ScoutScript scoutScript;
    @Inject
    private ScoutDiscordBot discordBot;

    private boolean previousBotState = false;

    @Override
    protected void startUp() throws Exception {
        if (config.enableDiscordBot()) { discordBot.start();}
        scoutScript.run(config);
    }

    @Override
    protected void shutDown() {
        discordBot.shutdown();
        scoutScript.shutdown();
    }


    @Provides
    ScoutConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ScoutConfig.class);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // Check if Discord bot state needs to change
        checkDiscordBotState();
    }


    private void checkDiscordBotState() {
        boolean shouldBeEnabled = config.enableDiscordBot();

        // Only take action if state has changed
        if (shouldBeEnabled != previousBotState) {
            if (shouldBeEnabled) {
                discordBot.start();
            } else {
                discordBot.shutdown();
            }
            previousBotState = shouldBeEnabled;
        }
    }

}
