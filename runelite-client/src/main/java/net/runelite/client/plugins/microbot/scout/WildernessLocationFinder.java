package net.runelite.client.plugins.microbot.scout;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.runelite.client.plugins.microbot.scout.enums.SlayerCaveLocation;

import net.runelite.api.coords.WorldPoint;

public class WildernessLocationFinder {

    private static final int MAX_LOCATION_DISTANCE = 50;

    public static final List<SlayerCaveLocation> ALL_CAVE_LOCATIONS = Arrays.asList(
            SlayerCaveLocation.ABYSSAL_DEMONS,
            SlayerCaveLocation.BLACK_DRAGONS,
            SlayerCaveLocation.GREEN_DRAGONS_NORTH,
            SlayerCaveLocation.LESSER_DEMONS,
            SlayerCaveLocation.GREATER_DEMONS,
            SlayerCaveLocation.BLACK_DEMONS,
            SlayerCaveLocation.DUST_DEVILS,
            SlayerCaveLocation.JELLIES,
            SlayerCaveLocation.HELLHOUNDS,
            SlayerCaveLocation.GREEN_DRAGONS_SOUTH,
            SlayerCaveLocation.ANKOUS,
            SlayerCaveLocation.ICE_GIANTS,
            SlayerCaveLocation.GREATER_NECHRYAELS
    );

    /**
     * Finds the closest wilderness location to a given WorldPoint
     *
     * @param worldPoint The reference WorldPoint to measure distance from
     * @return The closest WildernessLocation
     */
    public static SlayerCaveLocation findClosestLocation(WorldPoint worldPoint) {
        return ALL_CAVE_LOCATIONS.stream()
                .filter(location -> location.getWorldPoint().distanceTo2D(worldPoint) <= MAX_LOCATION_DISTANCE)
                .min(Comparator.comparingInt(location -> location.getWorldPoint().distanceTo2D(worldPoint)))
                .orElse(null);
    }

    /**
     * @param worldPoint The reference WorldPoint to measure distance from
     * @return Location name or "Unknown"
     */
    public static String getLocationName(WorldPoint worldPoint) {
        SlayerCaveLocation closestLocation = findClosestLocation(worldPoint);
        return closestLocation != null ? closestLocation.getName() : "Unknown";
    }
}
