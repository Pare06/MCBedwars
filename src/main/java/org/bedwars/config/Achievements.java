package org.bedwars.config;

import org.bedwars.stats.achievements.Achievement;
import org.bedwars.stats.achievements.Rarity;
import org.bedwars.stats.achievements.TieredAchievement;
import org.bukkit.Material;

public class Achievements {
    private Achievements() { }

    public static final int ACHIEVEMENTS_SETS = 1; // 1 set -> 32 achievement

    // prima di cambiare ordine agli achievement resetta la tabella achievement nell'sql
    public static final Achievement WELCOME = new Achievement("Benvenuto nel server!", "Unisciti al server", Material.EMERALD, Rarity.COMMON);
    public static final Achievement HOTSTREAK = new Achievement("Winstreak!", "Vinci 10 partite consecutive", Material.NETHER_STAR, Rarity.RARE);
    public static final TieredAchievement KILLER = new TieredAchievement("Killer", "Fai %d uccisioni", new int[]{ 150, 500, 1000, 2500 }, Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD);
    public static final TieredAchievement BEDS = new TieredAchievement("Distruttore di letti", "Distruggi %d letti", new int[] { 50, 150, 300, 1000 }, Material.RED_BED);
    public static final TieredAchievement WINNER = new TieredAchievement("Campione", "Vinci %d partite", new int[] { 25, 100, 250, 500 }, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD);
    public static final Achievement FIREBALL_BRIDGE = new Achievement("Demolitore di ponti", "Distruggi il ponte di qualcuno con una fireball,\nfacendolo cadere nel vuoto", Material.FIRE_CHARGE, Rarity.EPIC);
    public static final Achievement EARTHQUAKE = new Achievement("Terremoto!", "Fai volare un team intero con un trampolino", Material.SLIME_BLOCK, Rarity.RARE);
}
