package org.bedwars.stats.achievements;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Rappresenta la rarità di un {@code Achievement} o {@code TieredAchievement}.
 * @see Achievement
 * @see TieredAchievement
 */
public enum Rarity {
    COMMON(NamedTextColor.WHITE),
    RARE(NamedTextColor.AQUA),
    EPIC(NamedTextColor.DARK_PURPLE),
    LEGENDARY(NamedTextColor.GOLD),
    MYTHIC(NamedTextColor.DARK_RED);

    Rarity(NamedTextColor color) {
        this.color = color;
    }

    private static final Rarity[] rarities = values();
    private final NamedTextColor color;

    /**
     * Restituisce la rarità con il grado dato.
     * @param tier il grado
     * @return la rarità
     */
    public static Rarity getRarity(int tier) {
        return rarities[tier - 1];
    }

    /**
     * Restituisce il colore.
     * @return il colore
     */
    public NamedTextColor getColor() {
        return color;
    }
}
