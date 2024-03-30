package org.bedwars.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Estensione di {@link Inventory} - aggiunge metodi per renderne più semplice la creazione.
 */
public class GUI {
    private final Inventory inventory;
    private String pattern;
    private final Map<Character, ItemStack> items;
    private final int slots;
    private boolean isValid;

    /**
     * Crea una GUI con 27 slot.
     * @param name il nome dell'inventario
     */
    public GUI(Component name) {
        this(name, 27); // chest
    }

    /**
     * Crea una GUI.
     * @param name il nome dell'inventario
     * @param slots il numero di slot, multiplo di 9
     */
    public GUI(Component name, int slots) {
        if (slots % 9 != 0) throw new IllegalArgumentException("Il numero di slot deve essere un multiplo di 9.");

        inventory = Bukkit.createInventory(null, slots, name);
        this.slots = slots;
        pattern = null;
        items = new HashMap<>();
        isValid = false;
    }

    /**
     * Aggiunge un pattern all'inventario che descrive la posizione degli oggetti al suo interno.
     * @param pattern il nuovo pattern
     */
    public void setPattern(String pattern) {
        if (pattern.length() != slots) throw new IllegalArgumentException("Il numero di caratteri deve essere uguale a" +
                                                                          " quello degli slot.");

        this.pattern = pattern;
    }

    /**
     * Assegna un carattere nel pattern a un oggetto.
     * @param c il carattere
     * @param material l'oggetto
     */
    public void setItem(char c, Material material) {
        setItem(c, new ItemStack(material), null);
    }

    /**
     * Assegna un carattere nel pattern a un oggetto.
     * @param c il carattere
     * @param material l'oggetto
     * @param name il nome dell'oggetto
     */
    public void setItem(char c, Material material, String name) {
        setItem(c, material, Component.text(name)
                .decoration(TextDecoration.ITALIC, false)
                .content(), false);
    }

    /**
     * Assegna un carattere nel pattern a un oggetto.
     * @param c il carattere
     * @param material l'oggetto
     * @param name il nome dell'oggetto
     * @param enchant l'oggetto deve avere un enchant?
     */
    public void setItem(char c, Material material, String name, boolean enchant) {
        ItemStack i = new ItemStack(material);

        if (enchant) {
            i.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
            ItemMeta m = i.getItemMeta();
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            i.setItemMeta(m);
        }

        setItem(c, i, name);
    }

    /**
     * Assegna un carattere nel pattern a un oggetto.
     * @param c il carattere
     * @param item l'oggetto
     */
    public void setItem(char c, ItemStack item) {
        setItem(c, item, null);
    }

    /**
     * Assegna un carattere nel pattern a un oggetto.
     * @param c il carattere
     * @param item l'oggetto
     * @param name il nome dell'oggetto
     */
    public void setItem(char c, ItemStack item, String name) {
        if (name != null) {
            ItemMeta m = item.getItemMeta();
            m.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(m);
        }

        items.put(c, item);
    }

    /**
     * Sostituisce ogni carattere nel pattern con un oggetto assegnato in precedenza con {@code setItem}.
     */
    public void applyPattern() {
        for (int i = 0; i < slots; i++) {
            // se non trova l'item in this.items, assume che non ci sia niente e ci mette l'aria al suo posto.
            inventory.setItem(i, Objects.requireNonNullElse(items.get(pattern.charAt(i)), new ItemStack(Material.AIR)));
        }
        isValid = true;
    }

    /**
     * Fa vedere la GUI al {@code Player} dato.
     * @param player il {@code Player} a cui far vedere la GUI
     */
    public void showToPlayer(Player player) {
        if (isValid) {
            BWPlayer.get(player).setGui(this);
            player.openInventory(inventory);
        }
        else throw new IllegalStateException("L'inventario non è inizializzato.");
    }
}
