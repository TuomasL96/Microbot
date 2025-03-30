package net.runelite.client.plugins.microbot.scout;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.scout.enums.LowCombatDetourLocations;
import net.runelite.client.plugins.microbot.scout.enums.SlayerCaveScoutLocation;

import javax.inject.Inject;


public class ScoutDiscordBot {
    private JDA jda;

    @Inject
    private ScoutConfig config;

    @Inject
    private ScoutScript scoutScript;

    @Inject
    public ScoutDiscordBot() {
        // The bot will be initialized later when start() is called
    }

    public void start() {
        if (jda != null) {
            return; // Already running
        }

        String token = config.botToken();
        if (token == null || token.isEmpty()) {
            Microbot.log("Discord bot token not configured");
            return;
        }

        try {
            jda = JDABuilder.createLight(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new CommandListener())
                    .build();


            jda.updateCommands()
                    .addCommands(
                            Commands.slash("walk", "Walk to destination")
                                    .setContexts(InteractionContextType.GUILD)
                                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED))
                    .addCommands(
                            Commands.slash("hop", "Hop to world")
                                    .addOptions(new OptionData(OptionType.INTEGER, "world", "The world to hop to")
                                            .setRequired(true)
                                            .setRequiredRange(301, 580))
                                    .setContexts(InteractionContextType.GUILD)
                                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED))
                    .addCommands(
                            Commands.slash("stop", "Log out and stop scouting")
                                    .setContexts(InteractionContextType.GUILD)
                                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED))
                    .addCommands(
                            Commands.slash("start", "Log in and start scouting")
                                    .setContexts(InteractionContextType.GUILD)
                                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                    ).queue();

            Microbot.log("Discord bot started successfully");
        } catch (Exception e) {
            Microbot.log("Failed to initialize Discord bot: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return jda != null;
    }

    private class CommandListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            // Only accept commands from guilds
            if (event.getGuild() == null)
                return;
            switch (event.getName()) {
                case "start":
                    startBot(event);
                    break;
                case "stop":
                    stopBot(event);
                    break;
                case "hop":
                    hopToWorld(event);
                    break;
                case "walk":
                    walkTo(event, true);
                    break;
                default:
                    event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
            }
        }
    }

    public void hopToWorld(SlashCommandInteractionEvent event) {
        int worldNumber = event.getOption("world", 302, OptionMapping::getAsInt);
        event.reply("Hopping to world: " + worldNumber).queue();
        scoutScript.hopWithDelay(worldNumber);
    }

    public void startBot(SlashCommandInteractionEvent event) {
        event.reply("Logging in").queue();
        scoutScript.loginPlayer();
    }

    public void walkTo(SlashCommandInteractionEvent event, Boolean safely) {
        scoutScript.walkToLocationWithDetours(
                SlayerCaveScoutLocation.BLACK_DEMONS_SCOUT.getWorldPoint(),
                LowCombatDetourLocations.ANKOU_DETOUR.getWorldPoint(),
                LowCombatDetourLocations.GREEN_DRAGON_DETOUR.getWorldPoint()
        );
        event.reply("Trying to walk to destination").queue();
    }


    public void stopBot(SlashCommandInteractionEvent event) {
        event.reply("Logging out").queue();
        scoutScript.logoutPlayer();
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
    }
}