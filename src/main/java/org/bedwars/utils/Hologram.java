package org.bedwars.utils;

import net.kyori.adventure.text.TextComponent;
import org.bedwars.Bedwars;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe per creare e gestire gli ologrammi - stringhe di testo "volanti" che vengono visualizzate nel mondo.
 */
public class Hologram {
    private Hologram() {}

    private static final List<ArmorStand> allHolograms = new ArrayList<>();

    /**
     * Piazza un ologramma.
     * @param loc la posizione
     * @param name il nome
     * @param world il mondo
     * @param text il testo
     */
    public static void placeHologram(Location loc, String name, World world, TextComponent text) {
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.setGravity(false);
        stand.setCanPickupItems(false);
        stand.customName(text);
        stand.setCustomNameVisible(true);
        stand.setVisible(false);

        stand.setMetadata(name, new FixedMetadataValue(Bedwars.Plugin, world));
        allHolograms.add(stand);
    }

    /**
     * Restituisce tutti gli ologrammi con nome e mondo dati.
     * @param name il nome degli ologrammi da prendere
     * @param world il mondo a cui appartengono
     * @return una lista contenente gli ologrammi trovati
     */
    public static List<ArmorStand> getHolograms(String name, World world) {
        return allHolograms.stream()
                .filter(s -> s.hasMetadata(name))
                .filter(s -> s.getMetadata(name).get(0).value() == world).toList();
    }
}
