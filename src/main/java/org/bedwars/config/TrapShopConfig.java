package org.bedwars.config;

import org.bedwars.game.shop.ShopItem;
import org.bedwars.utils.Items;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class TrapShopConfig { // W.I.P.
    private TrapShopConfig() {}

    public static final List<ShopItem> trapItems = new ArrayList<>();

    public static void initialize() {
        ShopItem blind = new ShopItem(Items.rename(Material.BLACK_STAINED_GLASS, "Cecita'"), 99, Material.DIAMOND, null, (a) -> null, false);
        ShopItem allarm = new ShopItem(Items.rename(Material.REDSTONE_LAMP, "Allarme"), 99, Material.DIAMOND, null, (a) -> null, false);
        ShopItem miningFatigue = new ShopItem(Items.rename(Material.WOODEN_HOE, "Fatica"), 99, Material.DIAMOND, null, (a) -> null, false);
        ShopItem damageReduction = new ShopItem(Items.rename(Material.WOODEN_SWORD, "SpadaRotta"), 99, Material.DIAMOND, null, (a) -> null, false);

        trapItems.add(blind);
        trapItems.add(allarm);
        trapItems.add(miningFatigue);
        trapItems.add(damageReduction);
    }
}
