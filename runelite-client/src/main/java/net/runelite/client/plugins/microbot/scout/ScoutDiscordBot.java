package net.runelite.client.plugins.microbot.scout;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.runelite.client.plugins.microbot.Microbot;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;


public class ScoutDiscordBot {
    private JDA jda;

    @Inject
    private ScoutConfig config;

    @Inject
    private ScoutScript scoutScript;

    @Inject
    public ScoutDiscordBot() {
        // The bot will be initialized when start() is called
    }

    public void start() {
        if (jda != null) {
            return;
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

            jda.awaitReady();

            jda.updateCommands().queue(); // delete global commands for now

            String guildId = config.guildId();
            if (guildId != null && !guildId.isEmpty()) {
                Guild guild = jda.getGuildById(guildId);
                if (guild == null) {
                    Microbot.log("ERROR: Could not find guild with ID: " + guildId);
                    return;
                }
                guild.updateCommands()
                        .addCommands(
                                Commands.slash("walk", "Walk to destination")
                                        .addOption(OptionType.STRING, "location", "Select a location", true, true)
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
                        ).queue(success -> {
                    System.out.println("Successfully updated commands for guild: " + guild.getName());
                }, error -> {
                    System.out.println("Failed to update commands: " + error.getMessage());
                });;

            } else {
                System.out.println("Discord Guild id not configured");
                Microbot.log("Discord Guild id not configured");
            }
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
                    walk(event);
                    break;
                default:
                    event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
            }
        }


        @Override
        public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
            if (event.getName().equals("walk") && event.getFocusedOption().getName().equals("location")) {
                List<Command.Choice> choices = PathRoute.ALL_PATHS.stream()
                        .map(path -> new Command.Choice(path.getFinalDestination().getName(),
                                path.getFinalDestination().getName()))
                        .collect(Collectors.toList());

                event.replyChoices(choices).queue();
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

    private void walk(SlashCommandInteractionEvent event) {
        String selectedLocation = event.getOption("location").getAsString();

        PathRoute selectedPath = PathRoute.ALL_PATHS.stream()
                .filter(path -> path.getFinalDestination().getName().equals(selectedLocation))
                .findFirst()
                .orElse(null);

        if (selectedPath == null) {
            event.reply("Invalid selection!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Reply to user and start movement
        event.reply("Navigating to " + selectedPath.getFinalDestination().getName() +
                (config.safePath() ? " with detours!" : " directly!")).queue();
        scoutScript.walkPath(selectedPath, config.safePath());
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