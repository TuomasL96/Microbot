package net.runelite.client.plugins.microbot.scout;

import net.runelite.client.config.*;

@ConfigGroup("scout")
public interface ScoutConfig extends Config {

    String scoutSettings = "Scout Settings";

    @Range(max = 30, min = 1)
    @ConfigItem(keyName = "scoutRadius", name = "Scout Radius", description = "Distance for another player to be scouted", position = 0, section = scoutSettings)
    default int scoutRadius() {
        return 30;
    }

    @ConfigItem(keyName = "blacklistResetTime", name = "Reset Time", description = "Ignore players after scouted (for x seconds). ", position = 1, section = scoutSettings)
    default int blacklistResetTime() {
        return 30;
    }

    @ConfigItem(keyName = "OnlyWilderness", name = "Only Wilderness?", description = "Apply said action only while in the wilderness?", position = 2, section = scoutSettings)
    default boolean onlyWilderness() {
        return true;
    }

    @ConfigItem(keyName = "ignoreFriends", name = "Ignore friends", description = "Do not scout players on your friends list", position = 3, section = scoutSettings)
    default boolean ignoreFriends() {
        return true;
    }

    @ConfigItem(keyName = "ignoreClan", name = "Ignore clan", description = "Do not alarm for players in your clan", position = 4, section = scoutSettings)
    default boolean ignoreClan() {
        return true;
    }

    @ConfigItem(keyName = "logOutOnScout", name = "Logout on scout", description = "Log out instead of hopping if you scout a player", position = 5, section = scoutSettings)
    default boolean logOutOnScout() {
        return false;
    }

    @ConfigItem(keyName = "scoutCannon", name = "Scout cannons", description = "Also scout cannons", position = 6, section = scoutSettings)
    default boolean scoutCannon() {
        return false;
    }

    @ConfigItem(keyName = "autoHop", name = "Auto hop", description = "Automatically hop worlds", position = 7, section = scoutSettings)
    default boolean autoHop() {
        return false;
    }

    @Range(max = 126, min = 3)
    @ConfigItem(keyName = "minScoutCb", name = "Min CB", description = "Minimum combat level of player to scout", position = 8, section = scoutSettings)
    default int minCombatToScout() {
        return 3;
    }

    @Range(max = 126, min = 3)
    @ConfigItem(keyName = "maxScoutCb", name = "Max CB", description = "Maximum combat level of player to scout", position = 9, section = scoutSettings)
    default int maxCombatToScout() {
        return 126;
    }

    @ConfigItem(keyName = "enableDiscordBot", name = "Enable Discord Bot", description = "Enable Discord bot integration", position = 10, section = scoutSettings)
    default boolean enableDiscordBot() {
        return true;
    }

    @ConfigItem(keyName = "safePath", name = "Avoid NPCs", description = "Take a safe path to locations when auto walking", position = 11, section = scoutSettings)
    default boolean safePath() {
        return true;
    }

    @ConfigItem(keyName = "discordBotToken", name = "Bot token", description = "Input your discord bot token", position = 12, section = scoutSettings)
    default String botToken() { return ""; }

    @ConfigItem(keyName = "guildId", name = "Guild Id", description = "Input your Guild Id", position = 13, section = scoutSettings)
    default String guildId() { return ""; }

}