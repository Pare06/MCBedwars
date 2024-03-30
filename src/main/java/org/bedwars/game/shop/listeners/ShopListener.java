package org.bedwars.game.shop.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bedwars.config.NPCConfig;
import org.bedwars.config.ShopConfig;
import org.bedwars.game.Team;
import org.bedwars.game.shop.ShopItem;
import org.bedwars.npc.traits.BaseShop;
import org.bedwars.npc.traits.UpgradeShop;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.WoolUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopListener implements Listener {
    // cliccato uno degli oggetti per cambiare sezione
    @EventHandler
    public void onSectionClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (ShopConfig.sectionNames.contains(event.getView().title())) {
            int index = ShopConfig.sectionNames.indexOf(event.getCurrentItem().getItemMeta().displayName());
            if (index == -1) return; // non è lo shop?
            BaseShop.openShop((Player) event.getWhoClicked(), BaseShop.sectionMaterials.get(index), false); // aggiorna shop
        }
    }

    // click nello shop
    @EventHandler
    public void onBaseShopClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (event.getClickedInventory() == event.getWhoClicked().getInventory()) return;
        if (!ShopConfig.sectionNames.contains(event.getView().title())) return;
        if (ShopConfig.sectionNames.contains(event.getCurrentItem().getItemMeta().displayName())) return;

        Player player = (Player) event.getWhoClicked();

        ShopItem sItem;
        if (WoolUtils.isWool(event.getCurrentItem().getType())) { // lana?
            sItem = ShopConfig.woolItem;
        } else {
            Optional<ShopItem> optional = ShopItem.get(event.getCurrentItem().clone()); // prendi lo ShopItem cliccato
            if (optional.isEmpty()) return;
            sItem = optional.get();
        }

        if (!player.getInventory().contains(sItem.getCostingMaterial(), sItem.getCost())) {
            player.sendMessage(Component.text("Non hai abbastanza materiali!")
                    .color(NamedTextColor.RED));
            return;
        }

        if (sItem.run(player)) { // l'oggetto si può comprare?
            player.getInventory().removeItem(new ItemStack(sItem.getCostingMaterial(), sItem.getCost()));
            if (sItem.shouldBeGiven()) {
                ItemStack item = sItem.getItem().clone();
                item.lore(null);
                player.getInventory().addItem(item);
            }
        }
        BaseShop.loadSection(BWPlayer.get(player).getShopSection(), player); // aggiorna
    }

    // click nell'upgrades shop
    @EventHandler
    public void onUpgradeShopClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (event.getClickedInventory() == event.getWhoClicked().getInventory()) return;
        if (event.getView().title() != NPCConfig.UPGRADE_SHOP_NAME) return;

        Optional<ShopItem> optional = ShopItem.get(event.getCurrentItem().clone());
        if (optional.isEmpty()) return;
        ShopItem sItem = optional.get();

        Player player = (Player) event.getWhoClicked();

        if (!player.getInventory().contains(sItem.getCostingMaterial(), sItem.getCost())) {
            player.sendMessage(Component.text("Non hai abbastanza materiali!")
                    .color(NamedTextColor.RED));
            return;
        }

        player.getInventory().removeItem(new ItemStack(sItem.getCostingMaterial(), sItem.getCost()));

        if (sItem.run(player)) {
            Team team = BWPlayer.get(player).getTeam();

            Component hasObtained = Component.text(" ha ottenuto ").color(NamedTextColor.WHITE);
            Component upgrade = sItem.getItem().displayName().hoverEvent(null).color(NamedTextColor.GOLD);
            Component esclMark = Component.text("!").color(NamedTextColor.WHITE);

            for (Player p : team.getOnlinePlayers()) {
                // <player> ha ottenuto <upgrade>!
                p.sendMessage((Component.text(player.getName()).color(team.getTextColor())).append(hasObtained).append(upgrade).append(esclMark));
            }
        }

        UpgradeShop.loadUpgrades(player, BWPlayer.get(player).getGui()); // aggiorna
    }

    // shift-click nello shop (cancella item dal quickshop)
    @EventHandler
    public void onShiftClick(InventoryClickEvent event) { // quickshop
        if (event.getCurrentItem() == null) return;
        if (event.getClickedInventory() == event.getWhoClicked().getInventory()) return;
        if (event.getClick() != ClickType.SHIFT_LEFT) return;
        if (!ShopConfig.sectionNames.contains(event.getView().title())) return;

        ItemStack item = event.getCurrentItem();
        if (ShopConfig.sectionNames.contains(item.getItemMeta().displayName())) return;

        BWPlayer bwPlayer = BWPlayer.get((OfflinePlayer) event.getWhoClicked());
        Optional<ShopItem> optItem = ShopItem.get(item);
        int itemIndex;
        int quickId;
        if (optItem.isPresent()) {
            quickId = optItem.get().getId();
        } else {
            if (WoolUtils.isWool(item.getType())) { // lana
                quickId = ShopConfig.woolItem.getId();
            } else {
                return; // no item
            }
        }

        if (bwPlayer.getShopSection() == Material.NETHER_STAR) { // nel quickshop
            if (quickId >= BaseShop.PICKAXE_OFFSET && quickId <= BaseShop.PICKAXE_OFFSET + 4) { // piccone
                itemIndex = getSpecialIndex(BaseShop.QS_PICKAXE, bwPlayer);
            } else if (quickId >= BaseShop.AXE_OFFSET && quickId <= BaseShop.AXE_OFFSET + 4) { // ascia
                itemIndex = getSpecialIndex(BaseShop.QS_AXE, bwPlayer);
            } else {
                itemIndex = getSpecialIndex(quickId, bwPlayer);
            }

            List<Integer> quickShop = new ArrayList<>(bwPlayer.getQuickShopAsList());
            quickShop.set(itemIndex, BaseShop.QS_NULL);
            bwPlayer.setQuickShop(quickShop);
        } else { // fuori dal quickshop
            bwPlayer.setEditingQuickShop(true);
            bwPlayer.setNewQuickShopIndex(quickId);
        }
        BaseShop.loadSection(Material.NETHER_STAR, bwPlayer.getPlayer().getPlayer()); // aggiorna
    }

    // TODO

    @EventHandler
    public void onEditingQuickShop(InventoryClickEvent event) {

    }

    private int getSpecialIndex(int id, BWPlayer bwPlayer) {
        return bwPlayer.getQuickShopAsList().indexOf(id);
    }
}
