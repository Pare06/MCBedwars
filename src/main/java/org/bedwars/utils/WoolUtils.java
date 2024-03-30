package org.bedwars.utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

/**
 * Contiene funzioni relative agli oggetti "lana" per semplificarne il loro uso.
 */
public class WoolUtils {
    private WoolUtils() { }

    /**
     * Tutti i blocchi "lana".
     */
    public static final Material[] woolMaterials = {
            Material.RED_WOOL,
            Material.BLUE_WOOL,
            Material.GREEN_WOOL,
            Material.YELLOW_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.WHITE_WOOL,
            Material.PINK_WOOL,
            Material.GRAY_WOOL
    };

    private static final List<Material> woolList = Arrays.stream(woolMaterials).toList();

    /**
     * Tutti i blocchi "vetro".
     */
    public static final Material[] glassMaterials = {
            Material.RED_STAINED_GLASS,
            Material.BLUE_STAINED_GLASS,
            Material.GREEN_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS,
            Material.WHITE_STAINED_GLASS,
            Material.PINK_STAINED_GLASS,
            Material.GRAY_STAINED_GLASS
    };

    /**
     * Tutti i colori dei blocchi "lana", sotto forma di stringa, nello stesso ordine di {@code woolMaterials}.
     */
    public static final String[] woolNames = {
            "rosso", "blu", "verde", "giallo", "azzurro", "bianco", "rosa", "grigio"
    };

    /**
     * Tutti i colori dei blocchi "lana".
     */
    public static final NamedTextColor[] woolColors = new NamedTextColor[] {
            NamedTextColor.RED, NamedTextColor.BLUE, NamedTextColor.GREEN, NamedTextColor.YELLOW,
            NamedTextColor.AQUA, NamedTextColor.WHITE, NamedTextColor.LIGHT_PURPLE, NamedTextColor.GRAY
    };

    /**
     * Controlla se l'oggetto dato corrisponde a un blocco di lana.
     * @param m l'oggetto da controllare
     * @return {@code true} se l'oggetto corrisponde a un blocco di lana, altrimenti {@code false}
     */
    public static boolean isWool(Material m) {
        return woolList.contains(m);
    }
}
