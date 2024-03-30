package org.bedwars.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Classe per facilitare la creazione di oggetti personalizzati.
 */
public class Items {
    private Items() { }

    /**
     * Rinomina un oggetto.
     * @param m l'oggetto
     * @param s il nuovo nome
     * @return l'oggetto rinominato
     */
    public static ItemStack rename(Material m, String s) {
        return rename(new ItemStack(m), s);
    }

    /**
     * Rinomina un oggetto.
     * @param item l'oggetto
     * @param s il nuovo nome
     * @return l'oggetto rinominato
     */
    public static ItemStack rename(ItemStack item, String s) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(s)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Cambia il colore del nome di un oggetto.
     * @param item l'oggetto
     * @param color il nuovo colore
     * @return l'oggetto col nuovo colore
     */
    @SuppressWarnings("DataFlowIssue")
    public static ItemStack color(ItemStack item, NamedTextColor color) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(meta.displayName().color(color));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Rinomina un oggetto e cambia il suo colore.
     * @param m l'oggetto
     * @param s il nome
     * @param color il colore
     * @return l'oggetto rinominato e colorato
     */
    public static ItemStack renameColor(Material m, String s, NamedTextColor color) {
        return color(rename(m, s), color);
    }

    /**
     * Aggiunge l'effetto "enchant" a un oggetto.
     * @param m l'oggetto
     * @return l'oggetto modificato
     */
    public static ItemStack enchant(Material m) {
        return enchant(new ItemStack(m));
    }

    /**
     * Aggiunge l'effetto "enchant" a un oggetto.
     * @param item l'oggetto
     * @return l'oggetto modificato
     */
    public static ItemStack enchant(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Aggiunge un enchant a un oggetto.
     * @param m l'oggetto
     * @param ench l'enchant
     * @param level il livello
     * @return l'oggetto modificato
     */
    public static ItemStack enchant(Material m, Enchantment ench, int level) {
        return enchant(new ItemStack(m), ench, level);
    }

    /**
     * Aggiunge un enchant a un oggetto.
     * @param item l'oggetto
     * @param ench l'enchant
     * @param level il livello
     * @return l'oggetto modificato
     */
    public static ItemStack enchant(ItemStack item, Enchantment ench, int level) {
        if (level == 0) return item;
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(ench, level, true);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Rinomina un oggetto e gli aggiunge l'effetto "enchant".
     * @param m l'oggetto
     * @param s il nome
     * @return l'oggetto rinominato
     */
    public static ItemStack renameEnchant(Material m, String s) {
        return enchant(rename(m, s));
    }

    /**
     * Restituisce un oggetto corrispondente all'oggetto passato, con una quantità data.
     * @param m l'oggetto
     * @param i la quantità
     * @return l'oggetto con la quantità modificata
     */
    public static ItemStack quantity(Material m, int i) {
        return new ItemStack(m, i);
    }

    /**
     * Aggiunge una descrizione all'oggetto.
     * @param m l'oggetto
     * @param lines la descrizione, dove ogni {@code Component} corrisponde a una linea di testo
     * @return l'oggetto modificato
     */
    public static ItemStack lore(Material m, Component... lines) {
        return lore(new ItemStack(m), lines);
    }

    /**
     * Aggiunge una descrizione all'oggetto.
     * @param i l'oggetto
     * @param lines la descrizione, dove ogni {@code Component} corrisponde a una linea di testo
     * @return l'oggetto modificato
     */
    public static ItemStack lore(ItemStack i, Component... lines) {
        i.lore(Arrays.stream(lines).toList());
        return i;
    }
}
