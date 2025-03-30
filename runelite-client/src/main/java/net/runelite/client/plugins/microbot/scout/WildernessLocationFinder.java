package net.runelite.client.plugins.microbot.scout;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.runelite.client.plugins.microbot.scout.enums.WildernessLocation;

import net.runelite.api.coords.WorldPoint;

public class WildernessLocationFinder {

    private static final int MAX_LOCATION_DISTANCE = 50;

    public static final List<WildernessLocation> ALL_CAVE_LOCATIONS = Arrays.asList(
            WildernessLocation.ABYSSAL_DEMONS,
            WildernessLocation.BLACK_DRAGONS,
            WildernessLocation.GREEN_DRAGONS_NORTH,
            WildernessLocation.LESSER_DEMONS,
            WildernessLocation.GREATER_DEMONS,
            WildernessLocation.BLACK_DEMONS,
            WildernessLocation.DUST_DEVILS,
            WildernessLocation.JELLIES,
            WildernessLocation.HELLHOUNDS,
            WildernessLocation.GREEN_DRAGONS_SOUTH,
            WildernessLocation.ANKOUS,
            WildernessLocation.ICE_GIANTS,
            WildernessLocation.GREATER_NECHRYAELS
    );

    /**
     * Finds the closest wilderness location to a given WorldPoint
     *
     * @param worldPoint The reference WorldPoint to measure distance from
     * @return The closest WildernessLocation
     */
    public static WildernessLocation findClosestLocation(WorldPoint worldPoint) {
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
        WildernessLocation closestLocation = findClosestLocation(worldPoint);
        return closestLocation != null ? closestLocation.getName() : "Unknown";
    }
}
