package net.runelite.client.plugins.microbot.scout.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
public enum LowCombatDetourLocations {
    ANKOU_DETOUR("Avoid Ankous", new WorldPoint(3387, 10073, 0)),
    GREEN_DRAGON_DETOUR("Avoid Green Dragons", new WorldPoint(3378, 10134, 0));

    private final String displayName;
    @Getter
    private final WorldPoint worldPoint;

    public String getName() {
        return displayName;
    }

}
