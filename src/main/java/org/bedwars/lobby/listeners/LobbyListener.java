package org.bedwars.lobby.listeners;

import org.bedwars.config.LobbyConfig;
import org.bedwars.inventories.Inventories;
import org.bedwars.utils.BWPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

public class LobbyListener implements Listener {
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    // click sugli oggetti della lobby
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld() != LobbyConfig.LOBBY || event.getItem() == null) return;

        switch (event.getItem().getType()) {
            case COMPASS:
                Inventories.ModeSelection.showToPlayer(player);
                break;
            case PLAYER_HEAD:
                BWPlayer.get(player).getGameStats().showGUI();
                break;
            case NAME_TAG:
                BWPlayer.get(player).showFlairGUI();
                break;
        }
    }

    // player caduto nel vuoto
    @EventHandler
    public void onPlayerVoid(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (player.getLocation().getY() <= 0.0 && world == LobbyConfig.LOBBY) {
            player.teleport(LobbyConfig.SPAWN);
        }
    }

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        if (event.getBlock().getWorld() == LobbyConfig.LOBBY) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        if (event.getBlock().getWorld() == LobbyConfig.LOBBY) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld() == LobbyConfig.LOBBY) event.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity().getWorld() == LobbyConfig.LOBBY) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getWorld() == LobbyConfig.LOBBY) event.setCancelled(true);
    }
}
