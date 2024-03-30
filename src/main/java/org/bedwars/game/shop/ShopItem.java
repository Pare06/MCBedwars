package org.bedwars.game.shop;

import net.kyori.adventure.text.Component;
import org.bedwars.Bedwars;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Rappresenta un oggetto nel negozio.
 */
public class ShopItem {
    private static int items = 0;
    private final int cost;
    private final Material costingMaterial;
    private final ItemStack item;
    private final Function<Player, Boolean> function; // input: Player -> output: false se non si può completare
                                                      //                          la funzione
    private final boolean shouldGiveItem; // false per l'armatura, sennò true
    private final int id;

    private static final List<ShopItem> allShopItems = new ArrayList<>();

    /**
     * Crea uno ShopItem.
     * @param m l'oggetto
     * @param c il costo dell'oggetto
     * @param cM il materiale necessario per comprare l'oggetto
     */
    public ShopItem(Material m, int c, Material cM) {
        this(new ItemStack(m), c, cM);
    }

    /**
     * Crea uno ShopItem.
     * @param i l'oggetto
     * @param c il costo dell'oggetto
     * @param cM il materiale necessario per comprare l'oggetto
     */
    public ShopItem(ItemStack i, int c, Material cM) {
        this(i, c, cM, null, (x) -> true, true);
    }

    /**
     * Crea uno ShopItem.
     * @param m l'oggetto
     * @param c il costo dell'oggetto
     * @param cM il materiale necessario per comprare l'oggetto
     * @param lore il testo dell'oggetto nel
     * @param func la funzione che, se ritorna true, permette di comprare l'oggetto
     * @param give l'oggetto deve essere dato direttamente?
     */
    public ShopItem(Material m, int c, Material cM, List<Component> lore, Function<Player, Boolean> func, boolean give) {
        this(new ItemStack(m), c, cM, lore, func, give);
    }

    /**
     * Crea uno ShopItem.
     * @param i l'oggetto
     * @param c il costo dell'oggetto
     * @param cM il materiale necessario per comprare l'oggetto
     * @param lore il testo dell'oggetto nel
     * @param func la funzione che, se ritorna true, permette di comprare l'oggetto
     * @param give l'oggetto deve essere dato direttamente?
     */
    public ShopItem(ItemStack i, int c, Material cM, List<Component> lore, Function<Player, Boolean> func, boolean give) {
        if (lore == null) lore = new ArrayList<>();
        id = items++;
        ItemMeta meta = i.getItemMeta();
        meta.lore(lore);
        // aggiunge l'id dell'oggetto all'ItemStack
        meta.getPersistentDataContainer().set(new NamespacedKey(Bedwars.Plugin, "shop_id"), PersistentDataType.STRING, String.valueOf(id));
        i.setItemMeta(meta);
        item = i;
        cost = c;
        costingMaterial = cM;
        function = func;
        shouldGiveItem = give;
        allShopItems.add(this);
    }

    /**
     * Controlla se l'oggetto deve essere dato direttamente al Player.
     * @return {@code true} se l'oggetto deve essere dato direttamente, altrimenti {@code false}
     */
    public boolean shouldBeGiven() {
        return shouldGiveItem;
    }

    /**
     * Esegue la funzione collegata all'oggetto e controlla il suo risultato per vedere se l'acquisto è andato a buon fine.
     * @param p il player a cui controllare la validità dell'acquisto
     * @return {@code true} se l'oggetto è acquistabile, altrimenti {@code false}
     */
    public boolean run(Player p) {
        return function.apply(p);
    }

    /**
     * Restituisce una copia dell'oggetto con lo stesso {@code ItemStack} di {@code i}, o un {@code Optional} vuoto se non esiste.
     * @param i l'{@code ItemStack} da controllare
     * @return un {@code Optional} contenente lo ShopItem cercato, o un {@code Optional} vuoto se non esiste
     */
    public static Optional<ShopItem> get(ItemStack i) {
        ItemStack item = i.clone();
        item.lore(null);
        for (ShopItem x : allShopItems) {
            ItemStack shop = x.getItem().clone();
            shop.lore(null);
            if (item.isSimilar(shop)) {
                return Optional.of(x);
            }
        }
        return Optional.empty();
    }

    /**
     * Restituise l'oggetto con lo stesso ID di {@code i}, o {@code null} se non esiste.
     * @param i l'ID da controllare
     * @return uno {@code ShopItem}, o {@code null} se non esiste
     */
    public static ShopItem get(int i) {
        return allShopItems.stream().filter(s -> s.getId() == i).findFirst().orElse(null);
    }

    /**
     * Restituisce il costo dell'oggetto.
     * @return il costo
     */
    public int getCost() {
        return cost;
    }

    /**
     * Restituisce il materiale con cui comprare l'oggetto.
     * @return il materiale con cui comprare l'oggetto
     */
    public Material getCostingMaterial() {
        return costingMaterial;
    }

    /**
     * Restituisce l'oggetto visualizzato nel negozio.
     * @return l'oggetto visualizzato nel negozio
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Restituisce l'ID dell'oggetto.
     * @return l'ID dell'oggetto
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce il nome del materiale passato per parametro.
     * @param m il materiale
     * @return il nome del materiale
     */
    public static String getMaterialName(Material m) {
        return switch (m) {
            case IRON_INGOT -> "Ferro";
            case GOLD_INGOT -> "Oro";
            case DIAMOND -> "Diamanti";
            case EMERALD -> "Smeraldi";
            default -> throw new IllegalArgumentException("oggetto non aggiunto");
        };
    }
}