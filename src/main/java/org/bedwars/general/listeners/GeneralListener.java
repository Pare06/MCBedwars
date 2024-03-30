package org.bedwars.general.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bedwars.Bedwars;
import org.bedwars.config.Achievements;
import org.bedwars.config.LobbyConfig;
import org.bedwars.game.Arena;
import org.bedwars.lobby.LobbyInterface;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.BWScoreboard;
import org.bedwars.utils.Items;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;

public class GeneralListener implements Listener {
    @EventHandler
    public void onPortalEnter(PlayerPortalEvent event) { // portali nether/end
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onExperienceGain(PlayerPickupExperienceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BWPlayer bwPlayer = BWPlayer.get(player);

        BWPlayer.addBWPlayer(player);
        if (bwPlayer == null) { // prima volta?
            bwPlayer = BWPlayer.get(player);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(LobbyConfig.SPAWN);

            teleportSpawn(player);
            bwPlayer.getAchievements().setAchievement(Achievements.WELCOME);
        }

        try {
            // aggiungi player
            PreparedStatement psInsertS = Bedwars.database.prepareStatement("INSERT OR IGNORE INTO players(name) VALUES(?)");
            psInsertS.setString(1, player.getName());
            psInsertS.execute();

            // carica stats
            PreparedStatement psSelectS = Bedwars.database.prepareStatement("SELECT * FROM players WHERE name = ?");
            psSelectS.setString(1, player.getName());
            bwPlayer.getGameStats().loadFromQuery(psSelectS.executeQuery());

            // carica achievements
            PreparedStatement psInsertA = Bedwars.database.prepareStatement("INSERT OR IGNORE INTO achievements(name) VALUES(?)");
            psInsertA.setString(1, player.getName());
            psInsertA.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e); // impossibile
        }

        // era in una partita?
        if (bwPlayer.getRejoinArena() != null && bwPlayer.getRejoinArena().getState() != Arena.ArenaState.ENDED) {
            Arena arena = Arena.getArena(player.getWorld());
            bwPlayer.setArena(arena);

            player.teleport(new Location(
                    bwPlayer.getRejoinArena().getWorld(),
                    0, 128, 0
            ));
            player.setGameMode(GameMode.SPECTATOR);

            new BukkitRunnable() {
                int countdown = 5;
                @Override
                public void run() {
                    if (countdown == 1) {
                        cancel();
                    }
                    player.showTitle(Title.title(Component.text("Sei tornato!")
                                    .color(NamedTextColor.DARK_RED),
                            Component.text(String.format("Tornerai in vita tra %d secondi...", countdown--))
                                    .color(NamedTextColor.RED),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO)));
                }
            }.runTaskTimer(Bedwars.Plugin, 0, 20);

            BWPlayer bwCopy = bwPlayer; // per usare bwPlayer nel BukkitRunnable

            bwPlayer.getArena().addTask(new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(bwCopy.getTeam().getSpawn());
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setHealth(20);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION, bwCopy.getTeam().getUpgrade("velocitaScavio"), false, false));
                    PlayerInventory inventory = player.getInventory();
                    inventory.clear();
                    bwCopy.addInitialItems();
                    bwCopy.setCombatLogPlayer(null);
                }
            }.runTaskLater(Bedwars.Plugin, 5 * 20));

            bwPlayer.getArena().updatePlayer(player);

            //noinspection DataFlowIssue
            Component message = Component.text(bwPlayer.getPlayer().getName())
                                .color(bwPlayer.getTeam().getTextColor())
                                .append(Component.text(" è tornato in partita.")
                                        .color(NamedTextColor.GRAY));

            BWScoreboard scoreboard = new BWScoreboard(player);
            scoreboard.loadScores(bwPlayer.getArena().getSharedBoard().getScores());
            bwPlayer.setScoreboard(scoreboard);

            bwPlayer.getArena().addTask(new BukkitRunnable() {
                @Override
                public void run() {
                    scoreboard.loadScores(bwCopy.getArena().getSharedBoard().getScores());
                }
            }.runTaskTimer(Bedwars.Plugin, 0, 5));
            bwPlayer.getArena().getOnlinePlayers().forEach(p -> p.sendMessage(message));
        } else {
            teleportSpawn(player);
            LobbyInterface.sendLobbyScoreboard(player);
        }
    }

    /**
     * Sposta il {@code Player} alla lobby.
     * @param player il {@code Player} da spostare
     */
    public static void teleportSpawn(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setItem(0, Items.rename(Material.COMPASS, "Seleziona modalità"));
        inv.setItem(1, Items.rename(Material.NAME_TAG,"Seleziona flair"));
        inv.setItem(4, Items.rename(BWPlayer.getPlayerHead(player), "Statistiche"));
        player.teleport(LobbyConfig.SPAWN);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
    }

    // blocca l'azione "cambia mano", non compatibile con i client <=1.8
    @EventHandler
    public void onPlayerSwapHands(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    // blocca le interazioni con gli inventari del server
    @EventHandler
    public void onPlayerMoveItem(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getHolder() == null) {
            // non mettere roba dentro le GUI
            event.setCancelled(true);
        }
    }
}
