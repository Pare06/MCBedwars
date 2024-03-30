package org.bedwars;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import org.apache.commons.io.FileUtils;
import org.bedwars.chat.commands.ShoutCommand;
import org.bedwars.chat.listeners.ChatListener;
import org.bedwars.chat.listeners.DefaultMessagesListener;
import org.bedwars.config.*;
import org.bedwars.game.ArenaLoader;
import org.bedwars.game.commands.ForceStartCommand;
import org.bedwars.game.listeners.BedwarsListener;
import org.bedwars.game.listeners.ItemListener;
import org.bedwars.game.listeners.LoadingListener;
import org.bedwars.game.shop.listeners.ShopListener;
import org.bedwars.general.commands.GameModeAlias;
import org.bedwars.general.commands.GameModeCommand;
import org.bedwars.general.commands.StatsCommand;
import org.bedwars.general.commands.debug.ResetQuickShop;
import org.bedwars.general.listeners.GeneralListener;
import org.bedwars.inventories.Inventories;
import org.bedwars.inventories.listeners.LobbyInventories;
import org.bedwars.inventories.listeners.StatsInventories;
import org.bedwars.lobby.listeners.LobbyListener;
import org.bedwars.npc.traits.BaseShop;
import org.bedwars.npc.traits.ModeSelector;
import org.bedwars.npc.traits.UpgradeShop;
import org.bedwars.stats.GameStats;
import org.bedwars.stats.achievements.AchievementData;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.BWScoreboard;
import org.bedwars.utils.GUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;

@SuppressWarnings("DataFlowIssue")
public class Bedwars extends JavaPlugin {
    public static Bedwars Plugin;
    public static NPCRegistry npcRegistry;
    public static Connection database;

    @Override
    public void onLoad() {
        // cancella tutti i mondi tranne lobby
        for (File f : Bukkit.getWorldContainer().listFiles()) { // \server\worlds\*
            if (!f.getName().equals("lobby")) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    throw new RuntimeException(e); // impossibile (?)
                }
            }
        }

        // \plugins\Bedwars\database.sql
        Path path = Path.of(getDataFolder().getAbsolutePath(), "database.sql");
        try {
            database = DriverManager.getConnection("jdbc:sqlite://" + path);
            Statement stmt = database.createStatement();
            Statement stmt1 = database.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS players (" +
                    "name varchar(16) UNIQUE," +
                    "level int DEFAULT 1," + // todo
                    "points int DEFAULT 0," +
                    "wins int DEFAULT 0," +
                    "losses int DEFAULT 0," +
                    "kills int DEFAULT 0," +
                    "deaths int DEFAULT 0," +
                    "finals int DEFAULT 0," +
                    "beds int DEFAULT 0," +
                    "winstreak int DEFAULT 0," +
                    "flair int DEFAULT 0," +
                    "unlockedFlairs int DEFAULT 1," +
                    // fino a 21 indici, 7 per riga, -1 = piccone, -2 = ascia, -3: niente
                    "quickShop varchar(255) DEFAULT '9 0 3 8 26 23 25 10 1 4 6 -1 24 12 2 5 7 -2 27 -3 -3')";

            stmt.execute(sql);

            String sql1 = "CREATE TABLE IF NOT EXISTS achievements (" +
                    "name varchar(16) UNIQUE, " +
                    "set1 int DEFAULT 0)";

            stmt1.execute(sql1);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        Plugin = this;

        ChatFlairs.initializeFlairs();
        ArenaLoader.initialize();
        BWPlayer.initialize();
        ItemListener.initialize();
        BWScoreboard.setEmptyScoreboard();

        initializeCommands();
        initializeEvents();
        initializeInventories();
        loadCitizens();
        runTasks();

        Bukkit.getLogger().info("[Bedwars] Plugin attivato.");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("[Bedwars] Plugin disattivato.");
    }

    private void initializeCommands() {
        getCommand("gm").setPermission("bedwars.gamemode");
        getCommand("gm").setExecutor(new GameModeCommand());
        getCommand("gm").setTabCompleter(new GameModeCommand());
        getCommand("gmc").setPermission("bedwars.gamemode");
        getCommand("gmc").setExecutor(new GameModeAlias());
        getCommand("forcestart").setPermission("bedwars.forcestart");
        getCommand("forcestart").setExecutor(new ForceStartCommand());
        getCommand("shout").setExecutor(new ShoutCommand());
        getCommand("stats").setExecutor(new StatsCommand());

        // COMANDI DEBUG - SENZA PERMESSI SETTATI, DA TOGLIERE
        getCommand("resetquick").setExecutor(new ResetQuickShop());
    }

    private void initializeEvents() {
        Bukkit.getPluginManager().registerEvents(new GeneralListener(), this);
        Bukkit.getPluginManager().registerEvents(new LobbyListener(), this);
        Bukkit.getPluginManager().registerEvents(new LobbyInventories(), this);
        Bukkit.getPluginManager().registerEvents(new LoadingListener(), this);
        Bukkit.getPluginManager().registerEvents(new BedwarsListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new DefaultMessagesListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
        Bukkit.getPluginManager().registerEvents(new StatsInventories(), this);
    }

    private void initializeInventories() {
        GUI mode = new GUI(InventoryConfig.MODE_SELECTOR);
        mode.setPattern("         " +
                        " 1 2 3 4 " +
                        "         ");
        mode.setItem('1', Material.IRON_BLOCK, "Solo");
        mode.setItem('2', Material.GOLD_BLOCK, "Duo");
        mode.setItem('3', Material.DIAMOND_BLOCK, "Trio");
        mode.setItem('4', Material.EMERALD_BLOCK, "4v4");
        mode.applyPattern();

        Inventories.ModeSelection = mode;

        // inventari villager

        ShopConfig.initialize();
        UpgradeShopConfig.initialize();
    }

    private void loadCitizens() {
        npcRegistry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

        // traits
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ModeSelector.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BaseShop.class));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(UpgradeShop.class));

        // i 4 npc per selezionare la modalità
        NPC soloNPC = npcRegistry.createNPC(EntityType.PLAYER, NPCConfig.SOLO_START);
        NPC doublesNPC = npcRegistry.createNPC(EntityType.PLAYER, NPCConfig.DOUBLES_START);
        NPC trioNPC = npcRegistry.createNPC(EntityType.PLAYER, NPCConfig.TRIO_START);
        NPC squadNPC = npcRegistry.createNPC(EntityType.PLAYER, NPCConfig.SQUAD_START);

        soloNPC.spawn(LobbyConfig.SOLO_LOCATION);
        doublesNPC.spawn(LobbyConfig.DOUBLES_LOCATION);
        trioNPC.spawn(LobbyConfig.TRIO_LOCATION);
        squadNPC.spawn(LobbyConfig.SQUAD_LOCATION);

        soloNPC.getOrAddTrait(ModeSelector.class);
        doublesNPC.getOrAddTrait(ModeSelector.class);
        trioNPC.getOrAddTrait(ModeSelector.class);
        squadNPC.getOrAddTrait(ModeSelector.class);
    }

    private void runTasks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String name : BWPlayer.getAllPlayers().keySet()) {
                    BWPlayer bwPlayer = BWPlayer.get(Bukkit.getOfflinePlayer(name));
                    GameStats gameStats = bwPlayer.getGameStats();
                    AchievementData achievements = bwPlayer.getAchievements();
                    try {
                        PreparedStatement psStats = database.prepareStatement(
                                "UPDATE players SET " +
                                        "level = ?," +
                                        "points = ?," +
                                        "kills = ?," +
                                        "deaths = ?," +
                                        "beds = ?," +
                                        "finals = ?," +
                                        "wins = ?," +
                                        "losses = ?," +
                                        "winstreak = ?," +
                                        "flair = ?," +
                                        "unlockedFlairs = ?," +
                                        "quickShop = ? " +
                                        "WHERE name = ?");

                        psStats.setInt(1, gameStats.getLevel());
                        psStats.setInt(2, gameStats.getPoints());
                        psStats.setInt(3, gameStats.getKills());
                        psStats.setInt(4, gameStats.getDeaths());
                        psStats.setInt(5, gameStats.getBeds());
                        psStats.setInt(6, gameStats.getFinals());
                        psStats.setInt(7, gameStats.getWins());
                        psStats.setInt(8, gameStats.getLosses());
                        psStats.setInt(9, gameStats.getStreak());
                        psStats.setInt(10, bwPlayer.getFlair().getId());
                        psStats.setInt(11, bwPlayer.getAvailableFlairs());
                        psStats.setString(12, bwPlayer.getQuickShop());
                        psStats.setString(13, name);
                        psStats.executeUpdate();

                        PreparedStatement psAchievements = database.prepareStatement(
                                "UPDATE achievements SET " +
                                        "set1 = ? " +
                                        "WHERE name = ?");

                        psAchievements.setInt(1, achievements.getSet(0));
                        psAchievements.setString(2, name);
                        psAchievements.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20 * 10); // 10s
    }
}
