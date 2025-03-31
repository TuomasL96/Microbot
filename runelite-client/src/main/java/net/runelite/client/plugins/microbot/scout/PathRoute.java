package net.runelite.client.plugins.microbot.scout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.plugins.microbot.scout.enums.LowCombatDetourLocations;
import net.runelite.client.plugins.microbot.scout.enums.SlayerCaveScoutLocation;
import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class PathRoute {
    private final SlayerCaveScoutLocation finalDestination;
    private final LowCombatDetourLocations[] detours;

    public WorldPoint getFinalDestinationPoint() {
        return finalDestination.getWorldPoint();
    }

    public WorldPoint[] getDetourPoints() {
        return Arrays.stream(detours)
                .map(LowCombatDetourLocations::getWorldPoint)
                .toArray(WorldPoint[]::new);
    }

    public static final PathRoute BLACK_DEMONS_PATH = new PathRoute(
            SlayerCaveScoutLocation.BLACK_DEMONS_SCOUT,
            new LowCombatDetourLocations[]{LowCombatDetourLocations.ANKOU_DETOUR}
    );

    public static final PathRoute GREATER_DEMONS_PATH = new PathRoute(
            SlayerCaveScoutLocation.GREATER_DEMONS_SCOUT,
            new LowCombatDetourLocations[]{LowCombatDetourLocations.ANKOU_DETOUR, LowCombatDetourLocations.GREEN_DRAGON_DETOUR}
    );

    public static final PathRoute HELLHOUNDS_PATH = new PathRoute(
            SlayerCaveScoutLocation.HELLHOUNDS_SCOUT,
            new LowCombatDetourLocations[]{LowCombatDetourLocations.ANKOU_DETOUR, LowCombatDetourLocations.GREEN_DRAGON_DETOUR}
    );

    public static final List<PathRoute> ALL_PATHS = List.of(
            BLACK_DEMONS_PATH,
            GREATER_DEMONS_PATH,
            HELLHOUNDS_PATH
    );
}
