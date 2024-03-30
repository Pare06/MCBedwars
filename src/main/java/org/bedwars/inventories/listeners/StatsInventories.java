package org.bedwars.inventories.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bedwars.stats.ChatFlair;
import org.bedwars.utils.BWPlayer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class StatsInventories implements Listener {
    // cliccato su "statistiche"
    @EventHandler
    public void onStatsClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null && event.getClickedInventory() != event.getWhoClicked().getInventory() && ((TextComponent) event.getView().title()).content().equals("Statistiche")) {
            if (item.getType() == Material.BOOK) {
                BWPlayer.get((OfflinePlayer) event.getWhoClicked()).getAchievements().showGUI();
            }
            event.setCancelled(true);
        }
    }

    // cliccato su "achievements"
    @EventHandler
    public void onAchievementsClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null && event.getClickedInventory() != event.getWhoClicked().getInventory() && ((TextComponent) event.getView().title()).content().startsWith("Pagina ")) {
            if (item.getType() == Material.BLUE_WOOL) {
                Player player = (Player) event.getWhoClicked();
                // dovrebbe rimanere solo il numero della pagina
                int pageIndex = Integer.parseInt(((TextComponent) event.getView().title()).content().replace("Pagina ", "")) - 1;
                BWPlayer.get(player).getAchievements().showGUI(event.getClickedInventory(), pageIndex, false);
            }
            event.setCancelled(true);
        }
    }

    // cliccato su "flairs"
    @EventHandler
    public void onFlairsClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null && event.getClickedInventory() != event.getWhoClicked().getInventory() && ((TextComponent) event.getView().title()).content().equals("Seleziona flair")) {
            Player player = (Player) event.getWhoClicked();
            BWPlayer bwPlayer = BWPlayer.get(player);

            if (item.getType() == Material.NAME_TAG) {
                int id = event.getSlot();
                ChatFlair flair = ChatFlair.getFlair(id);
                bwPlayer.setFlair(flair);
                player.sendMessage((Component.text("Hai impostato la flair ").color(NamedTextColor.GREEN))
                                        .append(flair.getGUIView()));
            } else {
                player.sendMessage(Component.text("Non hai sbloccato questa flair!").color(NamedTextColor.RED));
            }
            player.closeInventory();
            event.setCancelled(true);
        }
    }
}
