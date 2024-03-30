package org.bedwars.npc.traits;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bedwars.config.NPCConfig;
import org.bedwars.config.UpgradeShopConfig;
import org.bedwars.game.shop.ShopItem;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.GUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static org.bedwars.config.UpgradeShopConfig.upgradeItems;

@TraitName("UpgradeShop")
public class UpgradeShop extends Trait {

    public UpgradeShop() {
        super("UpgradeShop");
    }

    // click destro sull'NPC
    @EventHandler
    public void onPlayerInteractAtEntity(NPCRightClickEvent event) {
        Player player = event.getClicker();

        if (event.getNPC() == this.getNPC() && BWPlayer.get(player).getTeam() != null) {
            event.setCancelled(true);
            upgradeShop(player);
        }
    }

    public static void upgradeShop(Player player) {
        GUI gui = new GUI(NPCConfig.UPGRADE_SHOP_NAME, 45);

        gui.setPattern("         " +
                       " 0123456 " +
                       "GGGGGGGGG" +
                       "   789   " +
                       "         " );

        gui.setItem('G', Material.BLACK_STAINED_GLASS);
        loadUpgrades(player, gui);
        gui.applyPattern();
        gui.showToPlayer(player);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void loadUpgrades(Player player, GUI gui) {
        for (int i = 0; i < upgradeItems.size(); i++) {
            int index = BWPlayer.get(player).getTeam().getUpgrade(UpgradeShopConfig.upgradeNames.get(i));
            ShopItem sItem = upgradeItems.get(i).get(index);

            ItemStack item = sItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = meta.lore();
            lore.add(Component.empty());
            for (int j = 0; j < upgradeItems.get(i).size() - 1; j++) {
                ShopItem sI = upgradeItems.get(i).get(j);
                String name = ((TextComponent) sI.getItem().getItemMeta().displayName()).content();
                String tier = name.substring(name.lastIndexOf(' ') + 1);

                // Tier <tier>: <costo> <materiale>
                lore.add(Component.text("Tier ")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(tier))
                        .append(Component.text(": "))
                        .append(Component.text(sI.getCost()).color(
                                index >= fromRoman(tier)
                                ? NamedTextColor.GREEN
                                : NamedTextColor.RED))
                        .append(Component.text(" "))
                        .append(Component.text(getMaterialName(sI.getCostingMaterial())).color(NamedTextColor.AQUA)));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
            gui.setItem(String.valueOf(i).charAt(0), item);
        }
        gui.applyPattern();
    }

    private static String getMaterialName(Material m) {
        return switch (m) {
            case IRON_INGOT -> "Ferro";
            case GOLD_INGOT -> "Oro";
            case DIAMOND -> "Diamanti";
            case EMERALD -> "Smeraldi";
            default -> throw new IllegalArgumentException("impossibile");
        };
    }

    private static int fromRoman(String str) {
        return switch (str) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            default -> throw new IllegalArgumentException("impossibile, per ora");
        };
    }
}