package org.bedwars.stats.achievements;

import org.bukkit.Material;

import java.util.*;

/**
 * Rappresenta un obbiettivo ottenibile (e aumentabile) durante una partita.
 */
public class TieredAchievement {
    private final String name;
    private final String description;
    private final Material[] items;
    private final int[] tiers;
    private final int maxTier;
    private final int id;

    /**
     * Crea un {@code TieredAchievement}.
     * @param name il nome
     * @param description la descrizione
     * @param tiers i gradi
     * @param item l'oggetto da visualizzare
     */
    public TieredAchievement(String name, String description, int[] tiers, Material item) {
        this(name, description, tiers, filledArray(item, tiers.length));
    }

    /**
     * Crea un {@code TieredAchievement}.
     * @param name il nome
     * @param description la descrizione
     * @param tiers i gradi
     * @param items gli oggetti da visualizzare
     */
    public TieredAchievement(String name, String description, int[] tiers, Material... items) {
        this.name = name;
        this.description = description;
        this.items = items;
        this.tiers = tiers;
        this.id = Achievement.NEXT_ID;
        Achievement.NEXT_ID += tiers.length;
        maxTier = tiers.length - 1;
    }

    /**
     * Restituisce il nome.
     * @return il nome
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce la descrizione.
     * @return la descrizione
     */
    public String getDescription() {
        return description;
    }

    /**
     * Restituisce gli oggetti da visualizzare.
     * @return gli oggetti
     */
    public Material[] getItems() {
        return items;
    }

    /**
     * Restituisce il grado più alto del {@code TieredAchievement} ottenibile con la statistica data.
     * @param value la statistica
     * @return il grado più alto ottenibile
     */
    // 1 - primo tier, 2 - secondo, ... , 0 - nessun tier
    public int highestTier(int value) {
        int tier = 0;
        for (; tier != tiers.length && value >= tiers[tier]; tier++);
        return tier;
    }

    /**
     * Restituisce i gradi.
     * @return i gradi
     */
    public int[] getTiers() {
        return tiers;
    }

    /**
     * Restituisce il grado massimo.
     * @return il grado massimo
     */
    public int getMaxTiers() {
        return maxTier;
    }

    /**
     * Restituisce l'ID.
     * @return l'ID
     */
    public int getId() {
        return id;
    }

    private static Material[] filledArray(Material m, int times) {
        Material[] array = new Material[times];
        Arrays.fill(array, m);
        return array;
    }
}
