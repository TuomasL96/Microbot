package net.runelite.client.plugins.microbot.scout.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
public enum SlayerCaveLocation {
    ABYSSAL_DEMONS("Abyssal Demons", new WorldPoint(3340, 10162, 0)),
    BLACK_DRAGONS("Black Dragons", new WorldPoint(3363, 10157, 0)),
    GREEN_DRAGONS_NORTH("North Green Dragons", new WorldPoint(3401, 10123, 0)),
    LESSER_DEMONS("Lesser Demons", new WorldPoint(3338, 10135, 0)),
    GREATER_DEMONS("Greater Demons", new WorldPoint(3428, 10150, 0)),
    BLACK_DEMONS("Black Demons", new WorldPoint(3363, 10119, 0)),
    DUST_DEVILS("Dust Devils", new WorldPoint(3439, 10123, 0)),
    JELLIES("Jellies", new WorldPoint(3432, 10103, 0)),
    HELLHOUNDS("Hellhounds", new WorldPoint(3443, 10082, 0)),
    GREEN_DRAGONS_SOUTH("South Green Dragons", new WorldPoint(3415, 10067, 0)),
    ANKOUS("Ankous", new WorldPoint(3357, 10079, 0)),
    ICE_GIANTS("Ice Giants", new WorldPoint(3340, 10056, 0)),
    GREATER_NECHRYAELS("Greater Nechryaels", new WorldPoint(3335, 10106, 0));

    private final String displayName;
    @Getter
    private final WorldPoint worldPoint;

    public String getName() {
        return displayName;
    }

}

