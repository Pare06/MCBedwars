package org.bedwars.game;

import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bedwars.Bedwars;
import org.bedwars.config.MetadataConfig;
import org.bedwars.config.NPCConfig;
import org.bedwars.general.listeners.GeneralListener;
import org.bedwars.lobby.LobbyInterface;
import org.bedwars.npc.traits.UpgradeShop;
import org.bedwars.npc.traits.BaseShop;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.BWScoreboard;
import org.bedwars.utils.Hologram;
import org.bedwars.utils.WoolUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.bedwars.config.ArenaConfig.*;

/**
 * Rappresenta lo stato di una partita.
 */
public class Arena {
    private final String worldName; // modalita_nome_*
    private final String originalName; // modalita_nome
    private final List<OfflinePlayer> players; // giocatori nella partita
    private final int maxPlayers; // giocatori max
    private final String modeName; // solo/doubles/trio/4v4
    private final List<Team> teams; // i team
    private ArenaState state; // wait/starting/started/ended
    private int countdown; // per l'inizio del game
    private boolean starting;
    private final World world;
    private ScoreboardData sharedBoard;
    private ArenaUpgrades upgrades;
    private int nextUpgradeCooldown;
    private int diamondCooldown;
    private int diamondCdRemaining; // quanto manca?
    private int emeraldCooldown;
    private int emeraldCdRemaining;
    private final List<BukkitTask> tasks; // tasks da fermare quando finisce la partita
    private final List<NPC> npcs; // shop
    private final Arena thisArena; // per i BukkitRunnable

    /**
     * Crea un'{@code Arena}.
     * @param world il nome del mondo visto dal server (nella forma modalità_nome_id)
     * @param original il nome del mondo originale
     */
    public Arena(String world, String original) {
        worldName = world;
        originalName = original;
        players = new ArrayList<>();
        countdown = 30;
        this.world = Bukkit.getWorld(worldName);
        diamondCooldown = DIAMOND_COOLDOWN;
        emeraldCooldown = EMERALD_COOLDOWN;
        diamondCdRemaining = diamondCooldown;
        emeraldCdRemaining = emeraldCooldown;
        upgrades = ArenaUpgrades.JUST_STARTED;
        nextUpgradeCooldown = 0;
        tasks = new ArrayList<>();
        npcs = new ArrayList<>();

        int modeInt;
        switch (worldName.substring(0, 3)) {
            case "sol" -> {
                maxPlayers = 8;
                modeName = "Solo";
                modeInt = 1;
            }
            case "duo" -> {
                maxPlayers = 16;
                modeName = "Doubles";
                modeInt = 2;
            }
            case "tri" -> {
                maxPlayers = 12;
                modeName = "Trio";
                modeInt = 3;
            }
            case "4v4" -> {
                maxPlayers = 16;
                modeName = "4v4";
                modeInt = 4;
            }
            default -> throw new IllegalArgumentException("impossibile");
        }

        teams = new ArrayList<>();
        for (int i = 0; i < TeamColor.values().length; i++) {
            // crea i team vuoti
            teams.add(new Team(TeamColor.values()[i], modeInt, toLocation(ARENA_SPAWNS.get(originalName).get(i)), this));
        }
        state = ArenaState.WAITING;

        thisArena = this;
    }

    /**
     * Restituisce l'{@code Arena} con il {@code World} passato come parametro.
     * @param world il {@code World} da cercare
     * @return l'{@code Arena} trovata
     * @throws NullPointerException non è stata trovata un'{@code Arena}.
     */
    public static Arena getArena(World world) {
        for (Arena arena : ArenaLoader.getArenas()) {
            if (world.getName().equals(arena.getWorldName())) {
                return arena;
            }
        }
        throw new NullPointerException(String.format("L'arena %s non esiste.", world.getName()));
    }

    /**
     * Sposta un {@code Player} al punto di spawn dell'{@code Arena}.
     * @param player il {@code Player} da spostare
     */
    public void teleportPlayer(Player player) {
        World world = Bukkit.getWorld(worldName);
        double[] coords = SPAWN_COORDS.get(originalName);
        player.teleport(new Location(world, coords[0], coords[1], coords[2]));
    }

    /**
     * Restituisce il {@code Team} con il colore {@code color}.
     * @param color il colore da cercare
     * @return il {@code Team} trovato
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent") // findFirst() troverà sempre un team
    public Team getTeam(TeamColor color) {
        return teams.stream().filter(t -> t.getColor() == color).findFirst().get();
    }

    /**
     * Restituisce il {@code World} dove è rappresentata l'{@code Arena}.
     * @return il {@code World}
     */
    public World getWorld() {
        return world;
    }

    /**
     * Restituisce il nome del mondo visto dal server, con la forma {@code modalità_nome_id}.
     * @return il nome del mondo
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Restituisce il nome del mondo originale.
     * @return il nome del mondo originale
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * Restituisce i {@code Player} online.
     * @return i {@code Player} online
     */
    public List<Player> getOnlinePlayers() {
        return players.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toList();
    }

    /**
     * Restituisce il numero di {@code Player}.
     * @return il numero di {@code Player}
     */
    public int getNPlayers() {
        return players.size();
    }

    /**
     * Aggiunge un {@code OfflinePlayer}, controllando prima se non esiste già.
     * @param player il {@code OfflinePlayer} da aggiungere
     */
    public void addPlayer(OfflinePlayer player) {
        // se un Player esce e rientra, l'OfflinePlayer di prima viene scartato e ne viene generato uno nuovo,
        // quindi non si può controllare se esiste già con contains().
        if (players.stream().noneMatch(p -> Objects.equals(p.getName(), player.getName()))) {
            players.add(player);
        }
    }

    /**
     * Rimuove l'{@code OfflinePlayer} dall'{@code Arena}.
     * @param player l'{@code OfflinePlayer} da rimuovere
     */
    public void removePlayer(OfflinePlayer player) {
        players.removeIf(p -> Objects.equals(p.getName(), player.getName()));
    }

    /**
     * Restituisce il numero massimo di giocatori.
     * @return il numero massimo di giocatori
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Restituisce la modalità di gioco.
     * @return la modalità di gioco
     */
    public String getModeName() {
        return modeName;
    }

    /**
     * Restituisce lo stato dell'{@code Arena}.
     * @return lo stato dell'{@code Arena}
     */
    public ArenaState getState() {
        return state;
    }

    /**
     * Imposta lo stato dell'{@code Arena}.
     * @param state il nuovo stato dell'{@code Arena}
     */
    public void setState(ArenaState state) {
        this.state = state;
        if (state == ArenaState.WAITING) starting = false;
    }

    /**
     * Cambia il numero di ticks da aspettare prima che si generi un diamante.
     * @param cd il nuovo cooldown
     */
    public void setDiamondCooldown(int cd) {
        diamondCooldown = cd;
    }

    /**
     * Cambia il numero di ticks da aspettare prima che si generi uno smeraldo.
     * @param cd il nuovo cooldown
     */
    public void setEmeraldCooldown(int cd) {
        emeraldCooldown = cd;
    }

    /**
     * Controlla se il {@code World} dato non corrisponde a un'{@code Arena}.
     * @param world il {@code World} da controllare
     * @return {@code false} se {@code world} corrisponde a un'{@code Arena}, altrimenti {@code true}
     */
    public static boolean isNotArena(World world) {
        return !isArena(world.getName());
    }

    private static boolean isArena(String s) {
        return s.startsWith("solo")
                || s.startsWith("duo")
                || s.startsWith("trio")
                || s.startsWith("4v4");
    }

    /**
     * Restituisce il numero di secondi prima che la partita inizi.
     * @return i secondi mancanti
     */
    public int getCountdown() {
        return countdown;
    }

    /**
     * Imposta il numero di secondi prima che la partita inizi.
     * @param countdown il nuovo countdown
     */
    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    /**
     * Restituisce i dati della {@code BWScoreboard} da visualizzare a ogni {@code Player} nellla partita.
     * @return i dati della {@code BWScoreboard}
     * @see BWScoreboard
     */
    public ScoreboardData getSharedBoard() {
        return sharedBoard;
    }

    /**
     * Restituisce tutti i {@code Team} presenti nella partita.
     * @return i {@code Team} nella partita
     */
    public List<Team> getTeams() {
        return teams;
    }

    /**
     * Restituisce il numero di {@code Team} ancora in gioco.
     * @return i {@code Team} non eliminati
     */
    public long getAliveTeams() {
        return teams.stream().filter(t -> !t.isEmpty()).count();
    }

    /**
     * Se il {@code Player} passato è in un {@code Team}, lo rimpiazza con una sua copia aggiornata.
     * @param p il {@code Player} da aggiornare
     */
    public void updatePlayer(Player p) {
        for (Team t : teams) {
            if (t.containsPlayer(p)) {
                t.addPlayer(p);
            }
        }
    }

    /**
     * Inizia il conto alla rovescia per iniziare la partita.
     * @param player il {@code Player} a cui assegnare il conto alla rovescia
     */
    public void start(Player player) {
        if (!starting) {
            state = ArenaState.STARTING;

            starting = true;
            countdown = 30;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) cancel();
                    if (countdown <= 0) {
                        startGame();
                        cancel();
                    } else {
                        countdown--;
                    }
                }
            }.runTaskTimer(Bedwars.Plugin, 20, 20); // ogni secondo
        }
    }

    /**
     * Inizia la partita.
     */
    public void startGame() {
        if (state == ArenaState.STARTED) return;
        state = ArenaState.STARTED;

        players.forEach(p -> BWPlayer.get(p).setRejoinArena(this));

        List<Player> withoutTeam = new ArrayList<>(getOnlinePlayers().stream()
                .filter(p -> BWPlayer.get(p).getTeam() == null).toList()); // rimangono solo quelli senza team
        List<Team> notFull = new ArrayList<>(teams.stream()
                .filter(t -> t.getPlayers().size() != t.getMaxPlayers()).toList()); // rimangono quelli non pieni
        notFull.sort(Comparator.comparing(Team::getNPlayers)); // ordinati per n. di player

        assignTeams(notFull, withoutTeam);

        // i team senza player volano via
        teams.stream().filter(Team::isEmpty).forEach(t -> t.setTeamState(TeamState.ELIMINATED));

        // tippa tutti i player al loro spawn
        List<XYZRotation> spawns = ARENA_SPAWNS.get(originalName);
        for (Team team : teams) {
            for (Player p : team.getOnlinePlayers()) {
                XYZRotation coords = spawns.get(teams.indexOf(team));
                // Debug.printObjectInfo(p, p.getName());

                p.teleport(toLocation(coords));
            }
        }
        initializePlayers();

        // inizializza gli spawner
        teams.stream().filter(t -> !t.isEmpty()).forEach(t -> t.setTasks(originalName, world));

        // spawna tutti i villager
        List<XYZRotation> shopSpawns = SHOP_LOCATIONS.get(originalName);
        List<XYZRotation> upgradeSpawns = UPGRADE_LOCATION.get(originalName);
        for (int i = 0; i < teams.size(); i++) {
            if (!teams.get(i).isEmpty()) {
                NPC villager = Bedwars.npcRegistry.createNPC(EntityType.PLAYER, NPCConfig.SHOP_NAME);
                villager.setProtected(true);
                villager.getOrAddTrait(BaseShop.class);
                villager.spawn(toLocation(shopSpawns.get(i)));

                NPC upgradeVillager = Bedwars.npcRegistry.createNPC(EntityType.PLAYER, NPCConfig.UPGRADE_NAME);
                upgradeVillager.setProtected(true);
                upgradeVillager.getOrAddTrait(UpgradeShop.class);
                upgradeVillager.spawn(toLocation(upgradeSpawns.get(i)));

                npcs.add(villager);
                npcs.add(upgradeVillager);
            }
        }

        // generatori + ologrammi
        startGen(Material.DIAMOND, diamondCooldown);
        startGen(Material.EMERALD, emeraldCooldown);
        for (XYZCoords spawner : DIAMOND_SPAWNERS.get(originalName)) {
            Hologram.placeHologram(toLocation(spawner), MetadataConfig.DIAMOND_SPAWNER, world, Component.text(""));
        }
        for (XYZCoords spawner : EMERALD_SPAWNERS.get(originalName)) {
            Hologram.placeHologram(toLocation(spawner), MetadataConfig.EMERALD_SPAWNER, world, Component.text(""));
        }
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                diamondCdRemaining -= 20;
                emeraldCdRemaining -= 20;
                for (ArmorStand stand : Hologram.getHolograms(MetadataConfig.DIAMOND_SPAWNER, world)) {
                    stand.customName(Component.text("Spawna in ")
                            .color(NamedTextColor.WHITE)
                            .append(Component.text(diamondCdRemaining / 20)
                                    .color(NamedTextColor.BLUE))
                            .append(Component.text(" secondi")
                                    .color(NamedTextColor.WHITE)));
                }
                for (ArmorStand stand : Hologram.getHolograms(MetadataConfig.EMERALD_SPAWNER, world)) {
                    stand.customName(Component.text("Spawna in ")
                            .color(NamedTextColor.WHITE)
                            .append(Component.text(emeraldCdRemaining / 20)
                                    .color(NamedTextColor.GREEN))
                            .append(Component.text(" secondi")
                                    .color(NamedTextColor.WHITE)));
                }
            }
        }.runTaskTimer(Bedwars.Plugin, 0, 20));

        // rompe tutti i letti nelle isole senza team
        double[][][] allBeds = ARENA_BEDS.get(originalName);
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).isEmpty()) {
                for (int j = 0; j < 2; j++) { // per cancellare entrambi i blocchi del letto
                    Location bed = new Location(
                            world,
                            allBeds[i][j][0], // x
                            allBeds[i][j][1], // y
                            allBeds[i][j][2]  // z
                    );
                    world.getBlockAt(bed).setType(Material.AIR, false);
                }
            }
        }

        // inizializza la scoreboard condivisa
        sharedBoard = new ScoreboardData();
        for (Player p : getOnlinePlayers()) {
            BWScoreboard scoreboard = new BWScoreboard(p);
            scoreboard.loadScores(sharedBoard.getScores());
            BWPlayer.get(p).setScoreboard(scoreboard);

            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    scoreboard.loadScores(sharedBoard.getScores());
                }
            }.runTaskTimer(Bedwars.Plugin, 0, 5));
        }

        // upgrades
        nextUpgrade(ArenaUpgrades.JUST_STARTED);
        decreaseCooldown();
    }

    /**
     * Rompe il letto del {@code Team}.
     * @param team il {@code Team} a cui rompere il letto
     */
    public void breakBed(Team team) {
        // cambia la scoreboard
        Arena.ScoreboardData sharedBoard = getSharedBoard();
        sharedBoard.setState(TeamState.NO_BED, team.getIndex());

        // avverte tutti i player del team
        for (Player p : team.getOnlinePlayers()) {
            p.showTitle(Title.title(Component.text("LETTO DISTRUTTO!")
                            .color(NamedTextColor.DARK_RED),
                    Component.text("Non potrai più respawnare!")
                            .color(NamedTextColor.RED)));
            BWPlayer.get(p).getTeam().setTeamState(TeamState.NO_BED);
            BWPlayer.get(p).setRejoinArena(null);
        }
    }

    private void assignTeams(List<Team> notFull, List<Player> withoutTeam) {
        for (Team team : notFull) {
            if (withoutTeam.isEmpty()) break;
            while (!team.isFull()) {
                if (withoutTeam.isEmpty()) break;
                Player player = randomItem(withoutTeam);
                BWPlayer bwPlayer = BWPlayer.get(player);
                bwPlayer.setTeam(team);
                withoutTeam.remove(player);
            }
        }
    }

    private void initializePlayers() {
        for (Player p : getOnlinePlayers()) {
            // inventario
            BWPlayer bwPlayer = BWPlayer.get(p);
            bwPlayer.setArena(this);
            p.getInventory().clear();
            bwPlayer.addInitialItems();
        }
    }

    private void nextUpgrade(ArenaUpgrades upgrade) {
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                ARENA_UPGRADES.get(upgrade).apply(thisArena);

                int index = List.of(ArenaUpgrades.values()).indexOf(upgrade);

                // prossimo upgrade
                ArenaUpgrades next = index + 1 < ArenaUpgrades.values().length ? ArenaUpgrades.values()[index + 1] : null;
                if (next != null) {
                    upgrades = next;
                    nextUpgrade(next);
                }
            }
        }.runTaskLater(Bedwars.Plugin, UPGRADE_COOLDOWNS.get(upgrade)));
    }

    private void decreaseCooldown() {
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                nextUpgradeCooldown--;

                try {
                    if (nextUpgradeCooldown <= 0) {
                        nextUpgradeCooldown = UPGRADE_COOLDOWNS.get(ArenaUpgrades.values()[upgrades.ordinal() + 1]);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // upgrade finiti
                }
            }
        }.runTaskTimer(Bedwars.Plugin, 0, 1));
    }

    /**
     * Termina la partita.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void endGame() {
        state = ArenaState.ENDED;

        // cancella tutte le task
        tasks.forEach(BukkitTask::cancel);

        List<String> winners = new ArrayList<>();

        // quando finisce il game c'è solo un team rimasto - non importa controllare se teams ha dei team non vuoti
        for (Player p : teams.stream().filter(t -> !t.isEmpty()).findFirst().get().getOnlinePlayers()) {
            p.showTitle(Title.title(Component.text("HAI VINTO!")
                            .color(NamedTextColor.RED),
                    Component.text("Vuoi fare un'altra partita?")
                            .color(NamedTextColor.WHITE)));
            BWPlayer.get(p).getGameStats().addWin();
            winners.add(p.getName());
        }

        getOnlinePlayers().forEach(Player::clearActivePotionEffects);
        for (OfflinePlayer p : players) {
            if (!winners.contains(p.getName())) {
                BWPlayer.get(p).getGameStats().addLoss();
            }
        }

        // tippa via tutti dopo 5 secondi
        new BukkitRunnable() {
            @Override
            public void run() {
                //noinspection DataFlowIssue (se world è null mi sa che sono cazzi)
                for (Player p : world.getPlayers()) {
                    BWPlayer bwPlayer = BWPlayer.get(p);

                    bwPlayer.setArena(null);
                    bwPlayer.setRejoinArena(null);
                    bwPlayer.setTeam(null);
                    bwPlayer.resetTiers();

                    GeneralListener.teleportSpawn(p);
                    LobbyInterface.sendLobbyScoreboard(p);
                }
                npcs.forEach(NPC::destroy);
                teams.forEach(Team::clear);

                Bukkit.unloadWorld(world, false);
                ArenaLoader.deleteArena(thisArena);
            }
        }.runTaskLater(Bedwars.Plugin, 5 * 20);
    }

    /**
     * Aggiunge una {@code BukkitTask} alla lista delle {@code BukkitTask} da fermare alla fine della partita.
     * @param task {@code la BukkitTask}
     */
    public void addTask(BukkitTask task) {
        tasks.add(task);
    }

    private static String formatTime(int ticks) { // 1600 -> 1:20
        ticks /= 20; // tick -> secondi
        int seconds = ticks % 60;
        // per fare i secondi sempre a 2 cifre
        return String.format("%d:%s", ticks / 60, (seconds < 10) ? "0" + seconds : seconds);
    }

    private void startGen(Material m, int cd) {
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                dropMaterial(m);
                if (m == Material.DIAMOND) diamondCdRemaining = diamondCooldown;
                else emeraldCdRemaining = emeraldCooldown;
                startGen(m, m == Material.DIAMOND ? diamondCooldown : emeraldCooldown);
            }
        }.runTaskLater(Bedwars.Plugin, cd));
    }

    private void dropMaterial(Material m) {
        Map<String, List<XYZCoords>> spawnerList = m == Material.DIAMOND
                ? DIAMOND_SPAWNERS
                : EMERALD_SPAWNERS;
        for (XYZCoords spawner : spawnerList.get(originalName)) {
            Location loc = new Location(
                    world,
                    spawner.x(),
                    spawner.y(),
                    spawner.z()
            );

            int nearbyOres = (int) loc.getNearbyEntities(1, 5, 1).stream() // tutte le entità a 5 blocchi Y di distanza
                    .filter(e -> e.hasMetadata(MetadataConfig.GEN_SPAWNED)) // spawnate dal gen
                    .filter(e -> ((Item) e).getItemStack().getType() == m) // dello stesso materiale da droppare
                    .count();

            // limite max. di ore spawnati?
            if (nearbyOres >= (m == Material.DIAMOND
                    ? MAX_DIAMONDS_SPAWNED
                    : MAX_EMERALDS_SPAWNED)) {
                continue;
            }

            Item item = world.dropItem(loc, new ItemStack(m));
            item.setVelocity(new Vector(0, 0, 0));
            item.setMetadata(MetadataConfig.GEN_SPAWNED, new FixedMetadataValue(Bedwars.Plugin, true));
            item.setUnlimitedLifetime(true);
        }
    }

    private static <E> E randomItem(List<E> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    private Location toLocation(XYZCoords coords) {
        return new Location(
                world,
                coords.x(),
                coords.y(),
                coords.z()
        );
    }

    private static Location toLocation(XYZRotation coords, World world) {
        return new Location(
                world,
                coords.x(),
                coords.y(),
                coords.z(),
                (float) coords.yaw(),
                (float) coords.pitch()
        );
    }

    private Location toLocation(XYZRotation coords) {
        return toLocation(coords, world);
    }

    public enum ArenaState {
        WAITING,
        STARTING,
        STARTED,
        ENDED
    }

    /**
     * La data e lo stato dei {@code Team} da visualizzare nella {@code BWScoreboard} della partita.
     */
    public class ScoreboardData {
        private final LocalDateTime today;
        private final List<TeamState> states;

        /**
         * Crea un nuovo {@code ScoreboardData}.
         */
        public ScoreboardData() {
            today = LocalDateTime.now();
            states = new ArrayList<>();
            loadStates();
        }

        /**
         * Carica i dati dei {@code Team} dell'{@code Arena}.
         */
        public void loadStates() {
            for (Team t : teams) {
                states.add(t.getTeamState());
            }
        }

        /**
         * Restituisce lo stato dei {@code Team} sotto forma di stringhe da stampare nella {@code BWScoreboard}.
         * @return lo stato dei {@code Team}
         */
        @SuppressWarnings("deprecation") // perchè sulla scoreboard Component non funziona
        public List<String> getScores() {
            List<String> scores = new ArrayList<>();
            scores.add(ChatColor.GRAY + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            scores.add("");
            // quanto manca al prossimo upgrade?
            scores.add(String.format(UPGRADE_STRINGS.get(thisArena.upgrades), formatTime(nextUpgradeCooldown)));
            scores.add(" ");

            for (int i = 0; i < states.size(); i++) {
                StringBuilder string = new StringBuilder(WoolUtils.woolNames[i]);
                string.setCharAt(0, Character.toUpperCase(string.charAt(0)));
                scores.add(String.format("%s%s: %s", woolColors[i], string, woolColors[i] + getStateString(states.get(i), i)));
            }

            scores.add("  ");
            scores.add("mc.epiccity.it");
            return scores;
        }

        @SuppressWarnings("deprecation") // leggi sopra
        private static final ChatColor[] woolColors = {
            ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW,
            ChatColor.AQUA, ChatColor.WHITE, ChatColor.LIGHT_PURPLE, ChatColor.GRAY
        };

        private String getStateString(TeamState state, int index) {
            // TODO: simboli più umani (https://imgur.com/a/jFpKFO2)
            return switch (state) {
                case WITH_BED -> "✓";
                case NO_BED -> String.valueOf(teams.get(index).getPlayers().size());
                case ELIMINATED -> "✘";
            };
        }

        /**
         * Cambia lo stato di un {@code Team}.
         * @param state il nuovo stato
         * @param index l'indice del team
         */
        public void setState(TeamState state, int index) {
            states.set(index, state);
        }
    }
}
