package org.bedwars.stats.achievements;

import org.bukkit.Material;

/**
 * Rappresenta un obbiettivo ottenibile durante una partita.
 */
public class Achievement {
    public static int NEXT_ID = 0;
    private final String name;
    private final String description;
    private final Material item;
    private final int id;
    private final Rarity rarity;

    /**
     * Crea un {@code Achievement}.
     * @param name il nome
     * @param description la descrizione
     * @param item il materiale nella GUI
     * @param rarity la rarità
     */
    public Achievement(String name, String description, Material item, Rarity rarity) {
        this.name = name;
        this.description = description;
        this.item = item;
        this.rarity = rarity;
        this.id = NEXT_ID++;
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
     * Restituisce il materiale.
     * @return il materiale
     */
    public Material getItem() {
        return item;
    }

    /**
     * Restituisce l'ID.
     * @return l'ID
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce la rarità.
     * @return la rarità
     */
    public Rarity getRarity() {
        return rarity;
    }
}
