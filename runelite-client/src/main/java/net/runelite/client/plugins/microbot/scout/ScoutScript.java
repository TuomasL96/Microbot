package net.runelite.client.plugins.microbot.scout;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import net.runelite.api.kit.KitType;
import net.runelite.client.account.SessionManager;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.discord.Rs2Discord;
import net.runelite.client.plugins.microbot.util.discord.models.DiscordEmbed;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.ui.ClientUI;


import javax.inject.Inject;
import java.awt.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

public class ScoutScript extends Script {
    public static String version = "1.0.0";
    @Inject
    private ScoutConfig config;
    @Inject
    private EquipmentFinder equipmentFinder;
    @Inject
    private Client client;

    private boolean cannonBallCheckActive = false;
    private final Set<WorldPoint> scoutedCannons = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Long> playerBlacklist = new ConcurrentHashMap<>();
    private long lastHopTime = 0; // Timestamp of last hop
    private long lastBlacklistCleanupTime = 0;
    private int lastHoppedWorld = 0;
    private long currentTime = 0;

    public boolean run(ScoutConfig config) {
        scoutedCannons.clear();
        this.config = config;


        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;
            if (this.config.onlyWilderness() && Microbot.getVarbitValue(Varbits.IN_WILDERNESS) != 1) {
                return;
            }
            try {
                if (Rs2Player.getWorld() != lastHoppedWorld) {
                    lastHoppedWorld = Rs2Player.getWorld();
                    scoutedCannons.clear();
                }
                currentTime = System.currentTimeMillis();
                cleanupBlacklist();

                List<Player> playersToScout = getPlayersInRange().stream()
                        .filter(this::shouldPlayerBeScouted)
                        .collect(Collectors.toList());

                boolean newPlayersFound = (!playersToScout.isEmpty());
                if (newPlayersFound) {
                    cannonBallCheckActive = false;
                    for (Player player : playersToScout) {
                        sendScoutMessage(player, Rs2Player.getWorld());
                        blacklistPlayer(player.getName());
                    }
                    if (config.logOutOnScout()) {
                        logoutPlayer();
                    }
                    if (config.autoHop()) {
                        sleep(61, 113);
                        hopWithDelay(Login.getNextWorld(Rs2Player.isMember()));
                    }
                }

                if (config.scoutCannon()) {
                    checkCannons();
                }

                if (!Microbot.isHopping() && Microbot.isLoggedIn()) {
                    if (config.autoHop() && !cannonBallCheckActive) {
                        if (currentTime - lastHopTime >= getRandomDelay(4412, 6667)) {
                            hopWithDelay(Login.getNextWorld(Rs2Player.isMember()));
                        }
                    }

                }

            } catch (Exception ex) {
                Microbot.log(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public void hopWithDelay(Integer world) {
        lastHopTime = currentTime;
        sleep(61, 93);
        Microbot.getClient().openWorldHopper();
        Rs2Widget.hasWidget("Current world - " + Rs2Player.getWorld());
        sleep(61, 93);
        Microbot.hopToWorld(world);
        //Login.getNextWorld(Rs2Player.isMember()
    }

    private void blacklistPlayer(String playerName) {
        playerBlacklist.put(playerName, System.currentTimeMillis());
    }

    private boolean isPlayerBlacklisted(String playerName) {
        return playerBlacklist.containsKey(playerName);
    }

    private void cleanupBlacklist() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBlacklistCleanupTime < 1000) {
            return;
        }
        lastBlacklistCleanupTime = currentTime;

        // Remove players after the reset time
        playerBlacklist.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > (config.blacklistResetTime() * 1000L)
        );
    }

    private long getRandomDelay(int min, int max) {
        return min + (long) (Math.random() * (max - min));
    }

    public void loginPlayer() {
        if (!(Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN)) {
            Microbot.log("Can't log in. Not in login screen");
            return;
        }
        String username = Login.activeProfile.getName();
        String password = Login.activeProfile.getPassword();
        new Login(username, password, Login.getNextWorld(Rs2Player.isMember()));

    }

    public void logoutPlayer() {
        if (!Microbot.isLoggedIn()) {
            Microbot.log("Can't log out. Player not logged in");
            return;
        }
        ClientUI.getClient().setEnabled(false);
        sleep(61, 93);
        Rs2Player.logout();
        sleep(61, 93);
        ClientUI.getClient().setEnabled(true);
        scoutedCannons.clear();
    }

    private void checkCannons() {
        List<Integer> cannonIds = Arrays.asList(6, 43027);

        boolean cannonballsFound = false;

        for (Integer id : cannonIds) {
            List<GameObject> cannons = Rs2GameObject.getGameObjects(id);

            if (cannons.isEmpty()) {
                continue;
            }

            for (GameObject cannon : cannons) {
                WorldPoint cannonLocation = cannon.getWorldLocation();
                cannonBallCheckActive = true;
                for (int i = 0; i < 6; i++) {
                    for (Projectile projectile : Microbot.getClient().getProjectiles()) {
                        if (projectile.getId() == 53 || projectile.getId() == 2018) {
                            cannonballsFound = true;
                            break;
                        }
                    }
                    if (cannonballsFound) break;
                    sleep(600);
                }
                cannonBallCheckActive = false;
                if (!scoutedCannons.contains(cannonLocation)) {
                    scoutedCannons.add(cannonLocation);
                    sendCannonScoutMessage(lastHoppedWorld, cannonballsFound, cannonLocation);
                }
            }
        }
    }

    private List<Player> getPlayersInRange() {
        LocalPoint currentPosition = this.client.getLocalPlayer().getLocalLocation();
        return this.client.getPlayers()
                .stream()
                .filter(player -> (player.getLocalLocation().distanceTo(currentPosition) / 128 <= this.config.scoutRadius()))
                .collect(Collectors.toList());
    }

    private boolean shouldPlayerBeScouted(Player player) {
        if (player.getId() == this.client.getLocalPlayer().getId()) {
            return false;
        }
        if (this.config.ignoreClan() && player.isClanMember()) {
            return false;
        }
        if (this.config.ignoreFriends() && player.isFriend()) {
            return false;
        }
        if (isPlayerBlacklisted(player.getName())) {
            return false;
        }
        if (config.minCombatToScout() > player.getCombatLevel() || config.maxCombatToScout() < player.getCombatLevel()) {
            return false;
        }
        return true;
    }

    public void sendCannonScoutMessage(Integer world, Boolean firing, WorldPoint location) {
        try {
            boolean cannonFiring = firing;
            WorldPoint cannonLocation = location;
            String closestLocation = WildernessLocationFinder.getLocationName(cannonLocation);
            DiscordEmbed embed = new DiscordEmbed();
            embed.setTitle(String.format("World: %d\nActive: %s",
                    world,
                    cannonFiring ? "YES" : "unknown"
            ));

            embed.setDescription(String.format(
                    "Location:  %s",
                    closestLocation));
            embed.setColor(Rs2Discord.convertColorToInt(cannonFiring ? Color.LIGHT_GRAY : Color.DARK_GRAY));
            Rs2Discord.sendWebhookMessage("Cannon scouted", Collections.singletonList(embed));

        } catch (Exception e) {
            Microbot.log(e.getMessage());
        }
    }


    public void sendScoutMessage(Player player, Integer world) {
        try {
            String playerName = "unknown";
            String playerLocation = "unknown";
            int playerCombatLevel = 0;
            boolean isSkulled = false;

            System.out.println("Trying to scout");
            if (player == null) return;

            playerName = player.getName();
            playerCombatLevel = player.getCombatLevel();
            int playerSkulled = player.getSkullIcon();
            if (playerSkulled != -1) {
                isSkulled = true;
            }

            equipmentFinder.getPlayerEquipmentAndWait(player);
            String combinedImagePath = equipmentFinder.createCombinedEquipmentImagePath();
            List<String> equipmentImagePath = Collections.singletonList(combinedImagePath);

            DiscordEmbed embed = new DiscordEmbed();
            embed.setTitle(String.format("World: %d\n%s\n%s",
                    world,
                    "Combat level: " + playerCombatLevel,
                    player != null ? "Player: " + playerName : ""
            ));
            embed.setDescription(String.format(
                    "Skulled: %s\n",
                    isSkulled ? "YES" : "NO"

            ));

            embed.setColor(Rs2Discord.convertColorToInt(isSkulled ? Color.RED : Color.YELLOW));
            Rs2Discord.sendWebhookMessage("Player Scouted", Collections.singletonList(embed),
                    equipmentImagePath);


        } catch (Exception e) {
            Microbot.log(e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        if (mainScheduledFuture != null) {
            mainScheduledFuture.cancel(true);
        }
        scoutedCannons.clear();
        super.shutdown();
    }
}
