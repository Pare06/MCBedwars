package org.bedwars.inventories.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bedwars.config.InventoryConfig;
import org.bedwars.config.LobbyConfig;
import org.bedwars.game.Arena;
import org.bedwars.game.ArenaLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LobbyInventories implements Listener {
    // click nell'inventario "seleziona modalità"
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        if (player.getWorld() == LobbyConfig.LOBBY) {
            event.setCancelled(true);

            if (event.getView().title().equals(InventoryConfig.MODE_SELECTOR)) {
                Component displayName = event.getCurrentItem().getItemMeta().displayName();

                if (displayName == null) return;

                switch (((TextComponent) displayName).content()) {
                    case "Solo" -> {
                        Arena arena = ArenaLoader.getBestArena();
                        if (arena == null) {
                            arena = ArenaLoader.loadArena();
                        }
                        arena.teleportPlayer(player);
                    }
                    case "Duo", "Trio", "4v4" -> player.sendMessage("non ancora fatto lol");
                }
            }
        }
    }
}
