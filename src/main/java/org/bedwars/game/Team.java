package org.bedwars.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bedwars.Bedwars;
import org.bedwars.config.ArenaConfig;
import org.bedwars.config.ArenaConfig.XYZCoords;
import org.bedwars.config.MetadataConfig;
import org.bedwars.utils.WoolUtils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta una squadra in un'{@link Arena}.
 */
public class Team {
    private final List<OfflinePlayer> players;
    private final TeamColor color;
    private final int mode; // 1-4
    private TeamState teamState;
    private final Location spawn;
    private final HashMap<String, Integer> upgrades;
    private final Arena arena;
    private int ironCooldown = 20;
    private int goldCooldown = 8 * 20;
    private boolean areGensUpgraded = false;

    /**
     * Crea un {@code Team}.
     * @param c il colore
     * @param m il numero massimo di {@code Player}
     * @param l la {@code Location} di inizio
     * @param arena l'{@code Arena}
     */
    public Team(TeamColor c, int m, Location l, Arena arena) {
        this.arena = arena;
        players = new ArrayList<>();
        color = c;
        mode = m;
        this.spawn = l;
        teamState = TeamState.WITH_BED;
        upgrades = new HashMap<>();
    }

    /**
     * Restituisce l'indice.
     * @return l'indice
     */
    public int getIndex() {
        return List.of(TeamColor.values()).indexOf(color);
    }

    /**
     * Restituisce i giocatori.
     * @return i giocatori
     */
    public List<OfflinePlayer> getPlayers() {
        return players;
    }

    /**
     * Restituisce i giocatori online.
     * @return i giocatori online
     */
    public List<Player> getOnlinePlayers() {
        return players.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toList();
    }

    /**
     * Aggiunge un giocatore al {@code Team}.
     * @param player il giocatore da aggiungere
     */
    public void addPlayer(OfflinePlayer player) {
        removeAllCopies(player);
        if (players.size() == mode) throw new UnsupportedOperationException("Il team è pieno.");
        // getName() non ritornerà null a meno che player non sia offline
        // (e non dovrebbe esserlo quando viene chiamato addPlayer)
        //noinspection DataFlowIssue
        players.add(Bukkit.getPlayer(player.getName())); // aggiorna il player
    }

    /**
     * Controlla se il {@code OfflinePlayer} dato è presente nel {@code Team}.
     * @param player l'{@code OfflinePlayer} da controllare
     * @return {@code true} se {@code player} è presente, altrimenti false
     */
    public boolean containsPlayer(OfflinePlayer player) {
        return players.stream().anyMatch(p -> Objects.equals(p.getName(), player.getName()));
    }

    /**
     * Cancella il {@code Team} dalla partita.
     */
    public void removeFromGame() {
        kill();
        players.clear();
    }

    /**
     * Rimuove il {@code player} dal team.
     * @param player l'{@code OfflinePlayer} da rimuovere
     */
    public void removePlayer(OfflinePlayer player) {
        if (getNPlayers() == 1 && arena.getState() == Arena.ArenaState.STARTED) {
            kill();
        }
        removeAllCopies(player);
    }

    private void kill() {
        arena.getOnlinePlayers().forEach(p -> p.sendMessage(
                Component.text("Il team ")
                        .color(NamedTextColor.DARK_GRAY)
                        .append(Component.text(WoolUtils.woolNames[getIndex()])
                                .color(getTextColor())
                                .append(Component.text(" è stato ")
                                        .color(NamedTextColor.DARK_GRAY))
                                .append(Component.text("eliminato!")
                                        .color(NamedTextColor.DARK_RED)))));
        teamState = TeamState.ELIMINATED;
        arena.getSharedBoard().setState(TeamState.ELIMINATED, getIndex());
    }

    /**
     * Cancella tutti i {@code Player} dal {@code Team}.
     */
    public void clear() {
        players.clear();
    }

    private void removeAllCopies(OfflinePlayer player) {
        players.removeIf(p -> Objects.equals(p.getName(), player.getName()));
    }

    /**
     * Controlla se il {@code Team} è pieno.
     * @return {@code true} se è pieno, altrimenti {@code false}.
     */
    public boolean isFull() {
        return players.size() == mode;
    }

    /**
     * Restituisce il colore del {@code Team}.
     * @return il colore
     */
    public TeamColor getColor() {
        return color;
    }

    /**
     * Restituisce lo stato del {@code Team}.
     * @return lo stato
     */
    public TeamState getTeamState() {
        return teamState;
    }

    /**
     * Imposta lo stato del {@code Team}.
     * @param teamState il nuovo stato
     */
    public void setTeamState(TeamState teamState) {
        this.teamState = teamState;
    }

    /**
     * Restituisce il numero di {@code Player} nel {@code Team}.
     * @return il numero di {@code Player}
     */
    public int getNPlayers() {
        return players.size();
    }

    /**
     * Restituisce il numero massimo di {@code Player}.
     * @return il numero massimo di {@code Player}
     */
    public int getMaxPlayers() {
        return mode;
    }

    /**
     * Restituisce la posizione iniziale.
     * @return la posizione iniziale
     */
    public Location getSpawn() {
        return spawn;
    }

    /**
     * Restituisce il colore visualizzato nella chat.
     * @return il colore nella chat
     */
    public NamedTextColor getTextColor() {
        return WoolUtils.woolColors[getIndex()];
    }

    /**
     * Controlla se il {@code Team} è vuoto.
     * @return {@code true} se è vuoto, altrimenti {@code false}
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Avvia le {@code BukkitTask} che generano i materiali nel generatore del {@code Team}.
     * @param originalName il nome originale del mondo (per determinare la posizione dei generatori)
     * @param world il {@code World} dove generare i materiali
     */
    public void setTasks(String originalName, World world) {
        startGen(Material.IRON_INGOT, originalName, world, ironCooldown);
        startGen(Material.GOLD_INGOT, originalName, world, goldCooldown);
    }

    private void startGen(Material m, String originalName, World world, int cd) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (areGensUpgraded) { // il gen. è appena stato upgradato?
                    areGensUpgraded = false;
                    cancel();
                    setTasks(originalName, world); // restarta i BukkitRunnable ma col cooldown aggiornato
                } else {
                    dropMaterial(m, originalName, world);
                }
            }
        }.runTaskTimer(Bedwars.Plugin, 0, cd);
    }

    private void dropMaterial(Material m, String originalName, World w) {
        // lo spawner di questo team
        XYZCoords spawner = ArenaConfig.TEAM_SPAWNERS.get(originalName).get(getIndex());
        Location loc = new Location(
                w,
                spawner.x(),
                spawner.y(),
                spawner.z()
        );

        int maxOres = m == Material.IRON_INGOT ? ArenaConfig.MAX_IRON_SPAWNED : ArenaConfig.MAX_GOLD_SPAWNED;
        int nearbyOres = (int) loc.getNearbyEntities(2, 2, 2).stream() // tutte le entità a 2 blocchi di distanza
                .filter(e -> e.hasMetadata(MetadataConfig.GEN_SPAWNED)) // spawnate dal gen
                .filter(e -> ((Item) e).getItemStack().getType() == m) // dello stesso materiale da droppare
                .count();

        // limite max. di ore spawnati?
        if (nearbyOres >= maxOres) {
            return;
        }

        Item item = w.dropItem(loc, new ItemStack(m));
        item.setVelocity(new Vector(0, 0, 0));
        item.setMetadata(MetadataConfig.GEN_SPAWNED, new FixedMetadataValue(Bedwars.Plugin, true));
        item.setUnlimitedLifetime(true);
    }

    /**
     * Incrementa di 1 (o imposta a 1, se non esiste) il potenziamento dato.
     * @param upgrade il potenziamento da incrementare
     * @return il nuovo stato del potenziamento
     */
    public int addUpgrade(String upgrade) {
        if (upgrades.containsKey(upgrade)) {
            upgrades.put(upgrade, upgrades.get(upgrade) + 1);
        } else {
            upgrades.put(upgrade, 1);
        }
        return upgrades.get(upgrade);
    }

    /**
     * Restituisce lo stato del potenziamento dato.
     * @param upgrade il potenziamento da controllare
     * @return lo stato del potenziamento
     */
    public int getUpgrade(String upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    /**
     * Cambia il tempo del generatore di ferro.
     * @param cd il nuovo tempo
     */
    public void setIronCooldown(int cd) {
        ironCooldown = cd;
        areGensUpgraded = true;
    }

    /**
     * Cambia il tempo del generatore di oro.
     * @param cd il nuovo tempo
     */
    public void setGoldCooldown(int cd) {
        goldCooldown = cd;
        areGensUpgraded = true;
    }
}
