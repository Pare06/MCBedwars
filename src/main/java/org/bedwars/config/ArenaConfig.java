package org.bedwars.config;

import org.bedwars.game.Arena;
import org.bedwars.game.TeamState;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ArenaConfig {
    private ArenaConfig() { }

    public static final int DIAMOND_COOLDOWN = 30 * 20; // cooldown diamanti (in ticks)
    public static final int EMERALD_COOLDOWN = 70 * 20; // cooldown smeraldi
    public static final int MAX_IRON_SPAWNED = 32; // limite iron ai gen del team
    public static final int MAX_GOLD_SPAWNED = 8; // limite gold
    public static final int MAX_DIAMONDS_SPAWNED = 4; // limite diamanti
    public static final int MAX_EMERALDS_SPAWNED = 2; // limite smeraldi

    public static Map<String, double[]> SPAWN_COORDS; // spawn di ogni arena
    public static Map<String, String> MAP_NAMES; // nomi mappe
    public static Map<String, double[][][]> ARENA_BEDS; // coordinate dei letti
                                                     // ARENA_BEDS[x]: prende i letti del team x
                                                     // ARENA_BEDS[x][y]: il y° blocco del letto del team x
                                                     // ARENA_BEDS[x][y][z]: la z° coordinata del y° blocco
    public static Map<String, List<XYZRotation>> ARENA_SPAWNS; // spawnpoint
    public static Map<String, List<XYZRotation>> SHOP_LOCATIONS; // i villager
    public static Map<String, List<XYZRotation>> UPGRADE_LOCATION; // i villager upgrade
    public static Map<String, List<XYZCoords>> TEAM_SPAWNERS; // gli spawner dentro le basi
    public static Map<String, List<XYZCoords>> DIAMOND_SPAWNERS; // spawner di diamanti
    public static Map<String, List<XYZCoords>> EMERALD_SPAWNERS; // spawner di smeraldi
    public static Map<ArenaUpgrades, Integer> UPGRADE_COOLDOWNS; // cooldown a ogni upgrade
    public static Map<ArenaUpgrades, Function<Arena, Void>> ARENA_UPGRADES; // gli upgrade
    public static Map<ArenaUpgrades, String> UPGRADE_STRINGS; // i messaggi degli upgrade

    // server/arenas
    public static final Path ARENA_FOLDER = Paths.get(Bukkit.getWorldContainer().getAbsoluteFile().getParentFile().getAbsolutePath(), "arenas");

    // inizializzatori config

    public static void initialize() {
        setHashMaps();
        setArenaBeds();
        setArenaSpawns();
        setShopLocations();
        setUpgradeShopLocation();
        setTeamSpawners();
        setDiamondSpawners();
        setEmeraldSpawners();
        setUpgradeCooldowns();
        setArenaUpgrades();
        setUpgradeStrings();
    }

    private static void setHashMaps() {
        SPAWN_COORDS = new HashMap<>();
        SPAWN_COORDS.put("solo_airshow", new double[] {0.5, 118.0, 0.5});
        MAP_NAMES = new HashMap<>();
        MAP_NAMES.put("solo_airshow", "Airshow"); // "nome_del_mondo", "nome che vedono i player"
    }

    private static void setArenaBeds() {
        ARENA_BEDS = new HashMap<>();
        ARENA_BEDS.put("solo_airshow", new double[][][]{
            {{-40, 63, -76}, {-40, 63, -77}},
            {{40, 67, -76}, {40, 67, -77}},
            {{76, 67, -40}, {77, 67, -40}},
            {{76, 63, 40}, {77, 63, 40}},
            {{40, 63, 76}, {40, 63, 77}},
            {{-40, 67, 76}, {-40, 67, 77}},
            {{-76, 67, 40}, {-77, 67, 40}},
            {{-76, 63, -40}, {-77, 63, -40}}
        });
    }

    private static void setArenaSpawns() {
        ARENA_SPAWNS = new HashMap<>();
        // gli spawn sono 1 blocco davanti alla smooth stone
        ARENA_SPAWNS.put("solo_airshow", new ArrayList<>(List.of(new XYZRotation[]{
            new XYZRotation(-40.5, 63, -83.5, 0, 0), // stesso ordine di TeamColor.values()
            new XYZRotation(40.5, 67, -83.5, 0, 0),
            new XYZRotation(84.5, 67, -39.5, 90, 0),
            new XYZRotation(84.5, 63, 40.5, 90, 0),
            new XYZRotation(40.5, 63, 84.5, 180, 0),
            new XYZRotation(-39.5, 67, 84.5, 180, 0),
            new XYZRotation(-83.5, 67, 40.5, -90, 0),
            new XYZRotation(-83.5, 63, -39.5, -90, 0)
        })));
    }

    private static void setShopLocations() {
        SHOP_LOCATIONS = new HashMap<>();
        SHOP_LOCATIONS.put("solo_airshow", new ArrayList<>(List.of(new XYZRotation[]{
            new XYZRotation(-34.5, 63, -83, 90, 0),
            new XYZRotation(45.5, 67, -83, 90, 0),
            new XYZRotation(84, 67, -34.5, 180, 0),
            new XYZRotation(84, 63, 45.5, 180, 0),
            new XYZRotation(35.5, 63, 84, -90, 0),
            new XYZRotation(-44.5, 67, 84, -90, 0),
            new XYZRotation(-83, 67, 35.5, 0, 0),
            new XYZRotation(-83, 63, -44.5, 0, 0)
        })));
    }

    private static void setUpgradeShopLocation() {
        UPGRADE_LOCATION = new HashMap<>();
        UPGRADE_LOCATION.put("solo_airshow", new ArrayList<>(List.of(new XYZRotation[]{
                new XYZRotation(-44.5, 63, -83, -90, 0),
                new XYZRotation(35.5, 67, -83, -90, 0),
                new XYZRotation(84, 67, -44, 0, 0),
                new XYZRotation(84, 63, 35.5, 0, 0),
                new XYZRotation(45.5, 63, 84, 90, 0),
                new XYZRotation(34.5, 67, 84, 90, 0),
                new XYZRotation(-83, 67, 45.5, -180, 0),
                new XYZRotation(-83, 63, -34.5, -180, 0)
        })));
    }

    private static void setTeamSpawners() {
        TEAM_SPAWNERS = new HashMap<>();
        TEAM_SPAWNERS.put("solo_airshow", new ArrayList<>(List.of(new XYZCoords[]{
            new XYZCoords(-39.5, 64, -86), // +1.5 blocchi sopra le slab
            new XYZCoords(40.5, 68, -86),
            new XYZCoords(87, 68, -39.5),
            new XYZCoords(87, 64, 40.5),
            new XYZCoords(40.5, 64, 87),
            new XYZCoords(-39.5, 68, 87),
            new XYZCoords(-86, 68, 40.5),
            new XYZCoords(-86, 64, -39.5)
        })));
    }

    private static void setEmeraldSpawners() {
        EMERALD_SPAWNERS = new HashMap<>();
        EMERALD_SPAWNERS.put("solo_airshow", new ArrayList<>(List.of(new XYZCoords[] {
            new XYZCoords(12.5, 68, -11.5), // 4 blocchi sopra
            new XYZCoords(12.5, 64, 12.5),
            new XYZCoords(-11.5, 68, 12.5),
            new XYZCoords(-11.5, 64, -11.5),
        })));
    }

    private static void setDiamondSpawners() {
        DIAMOND_SPAWNERS = new HashMap<>();
        DIAMOND_SPAWNERS.put("solo_airshow", new ArrayList<>(List.of(new XYZCoords[] {
                new XYZCoords(-39.5, 68, 40.5), // 4 blocchi sopra
                new XYZCoords(-39.5, 64, -39.5),
                new XYZCoords(40.5, 68, -39.5),
                new XYZCoords(40.5, 64, 40.5),
        })));
    }

    private static void setUpgradeCooldowns() {
        UPGRADE_COOLDOWNS = new HashMap<>();
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.JUST_STARTED, 0);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.NO_UPGRADE, 5 * 60 * 20);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.DIAMOND_I, 5 * 60 * 20);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.EMERALD_I, 10 * 60 * 20);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.DIAMOND_II, 10 * 60 * 20);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.EMERALD_II, 10 * 60 * 20);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.DIAMOND_III, 15 * 60 * 20);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.EMERALD_III, 20 * 60 * 20);
        UPGRADE_COOLDOWNS.put(ArenaUpgrades.BREAK_BEDS, 0);
    }

    private static void setUpgradeStrings() {
        // %s -> tempo rimanente (min:sec)
        // il secondo arg. è il testo che manca al PROSSIMO upgrade
        UPGRADE_STRINGS = new HashMap<>();
        UPGRADE_STRINGS.put(ArenaUpgrades.JUST_STARTED, "Diamante I in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.NO_UPGRADE, "Diamante I in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.DIAMOND_I, "Smeraldo I in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.EMERALD_I, "Diamante II in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.DIAMOND_II, "Smeraldo II in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.EMERALD_II, "Diamante III in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.DIAMOND_III, "Smeraldo III in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.EMERALD_III, "Rottura letti in %s");
        UPGRADE_STRINGS.put(ArenaUpgrades.BREAK_BEDS, "fine");
    }

    private static void setArenaUpgrades() {
        ARENA_UPGRADES = new HashMap<>();
        // il secondo arg. è la funzione appartenente al PROSSIMO upgrade
        ARENA_UPGRADES.put(ArenaUpgrades.JUST_STARTED, (a) -> null);
        ARENA_UPGRADES.put(ArenaUpgrades.NO_UPGRADE, ArenaConfig::upgradeDiamondI);
        ARENA_UPGRADES.put(ArenaUpgrades.DIAMOND_I, ArenaConfig::upgradeEmeraldI);
        ARENA_UPGRADES.put(ArenaUpgrades.EMERALD_I, ArenaConfig::upgradeDiamondII);
        ARENA_UPGRADES.put(ArenaUpgrades.DIAMOND_II, ArenaConfig::upgradeEmeraldII);
        ARENA_UPGRADES.put(ArenaUpgrades.EMERALD_II, ArenaConfig::upgradeDiamondIII);
        ARENA_UPGRADES.put(ArenaUpgrades.DIAMOND_III, ArenaConfig::upgradeEmeraldIII);
        ARENA_UPGRADES.put(ArenaUpgrades.EMERALD_III, ArenaConfig::breakAllBeds);
        ARENA_UPGRADES.put(ArenaUpgrades.BREAK_BEDS, (a) -> null);
    }

    public static Void upgradeDiamondI(Arena a) {
        a.setDiamondCooldown(25 * 20);
        return null;
    }

    public static Void upgradeDiamondII(Arena a) {
        a.setDiamondCooldown(20 * 20);
        return null;
    }

    public static Void upgradeDiamondIII(Arena a) {
        a.setDiamondCooldown(15 * 20);
        return null;
    }

    public static Void upgradeEmeraldI(Arena a) {
        a.setEmeraldCooldown(60 * 20);
        return null;
    }

    public static Void upgradeEmeraldII(Arena a) {
        a.setEmeraldCooldown(50 * 20);
        return null;
    }

    public static Void upgradeEmeraldIII(Arena a) {
        a.setEmeraldCooldown(40 * 20);
        return null;
    }

    public static Void breakAllBeds(Arena a) {
        double[][][] beds = ARENA_BEDS.get(a.getOriginalName());
        for (double[][] bed : beds) {
            for (int j = 0; j < 2; j++) {
                a.getWorld().getBlockAt((int) bed[j][0], (int) bed[j][1], (int) bed[j][2]).setType(Material.AIR, false);
            }
        }
        a.getTeams().stream().filter(t -> t.getTeamState() == TeamState.WITH_BED).forEach(a::breakBed);
        return null;
    }

    public record XYZCoords(double x, double y, double z) { } // x(), y(), z()
    public record XYZRotation(double x, double y, double z, double yaw, double pitch) { } // XYZCoords + yaw(), pitch()

    public enum ArenaUpgrades {
        JUST_STARTED,
        NO_UPGRADE,
        DIAMOND_I,
        EMERALD_I,
        DIAMOND_II,
        EMERALD_II,
        DIAMOND_III,
        EMERALD_III,
        BREAK_BEDS
    }
}
