package net.runelite.client.plugins.microbot.scout.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
public enum SlayerCaveScoutLocation {
    BLACK_DEMONS_SCOUT("Black Demons Scout", new WorldPoint(3362, 10141, 0)),
    GREATER_DEMONS_SCOUT("Greater Demons Scout", new WorldPoint(3413, 10132, 0)),
    HELLHOUNDS_SCOUT("Hellhounds Scout", new WorldPoint(3420, 10082, 0));
    private final String displayName;
    @Getter
    private final WorldPoint worldPoint;

    public String getName() {
        return displayName;
    }

}

