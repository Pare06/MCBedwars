package org.bedwars.stats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bedwars.config.Achievements;
import org.bedwars.config.ChatConfig;
import org.bedwars.config.ExperienceConfig;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.GUI;
import org.bedwars.utils.Items;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GameStats {
    private final BWPlayer bwPlayer;
    private OfflinePlayer player;
    private int level;
    private int points;
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int finals;
    private int bedsBroken;
    private int streak;

    public GameStats(BWPlayer p) {
        bwPlayer = p;
        player = p.getPlayer();
    }

    public void loadFromQuery(ResultSet result) {
        try {
            level = result.getInt("level");
            points = result.getInt("points");
            wins = result.getInt("wins");
            losses = result.getInt("losses");
            kills = result.getInt("kills");
            deaths = result.getInt("deaths");
            finals = result.getInt("finals");
            bedsBroken = result.getInt("beds");
            streak = result.getInt("winstreak");

            addPoints(0); // aggiorna xp bar
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getLevel() {
        return level;
    }

    public int getPoints() {
        return points;
    }

    @SuppressWarnings("DataFlowIssue")
    public void addPoints(int points) {
        this.points += points;

        if (this.points >= requiredXPToNextLevel()) {
            this.points -= requiredXPToNextLevel();
            level++;

            if (player.isOnline()) {
                Player onlinePlayer = player.getPlayer();
                //noinspection DataFlowIssue
                onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                onlinePlayer.sendMessage(Component.text("Sei salito al livello ").color(NamedTextColor.YELLOW)
                    .append(Component.text(level).decorate(TextDecoration.BOLD).color(ChatConfig.levelColor(level))
                    .append(Component.text("!").color(NamedTextColor.YELLOW))));
            }
        }

        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            onlinePlayer.setLevel(level);
            onlinePlayer.setExp((float)this.points / requiredXPToNextLevel()); // setExp va da 0 (0%) a 1 (100%) godoooooo
        }
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        wins++;
        streak++;

        if (streak >= 10) {
            bwPlayer.getAchievements().setAchievement(Achievements.HOTSTREAK);
        }
        bwPlayer.getAchievements().setTieredAchievement(Achievements.WINNER, wins);

        addPoints(ExperienceConfig.WIN_XP);
    }

    public int getLosses() {
        return losses;
    }

    public void addLoss() {
        losses++;
        streak = 0;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;

        bwPlayer.getAchievements().setTieredAchievement(Achievements.KILLER, kills);

        addPoints(ExperienceConfig.KILL_XP);
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public int getFinals() {
        return finals;
    }

    public void addFinal() {
        finals++;
        addPoints(ExperienceConfig.FINAL_XP);
    }

    public int getBeds() {
        return bedsBroken;
    }

    public void addBed() {
        bedsBroken++;

        bwPlayer.getAchievements().setTieredAchievement(Achievements.BEDS, bedsBroken);

        addPoints(ExperienceConfig.BED_XP);
    }

    public int getStreak() {
        return streak;
    }

    public int requiredXPToNextLevel() {
        return (int) Math.pow((level) / ExperienceConfig.UNKNOWN_FACTOR, ExperienceConfig.SCALING_FACTOR);
    }

    public void refresh(Player p) {
        player = p;
    }

    public void showGUI() {
        GUI gui = new GUI(Component.text("Statistiche").color(NamedTextColor.GREEN), 36);
        gui.setPattern("         " +
                       " w l k d " +
                       " f b r s " +
                       "       a ");
        gui.setItem('w', Items.rename(Items.lore(Material.BLAZE_POWDER,
                Component.empty(),
                Component.text("Serie di vittorie: " + streak).color(NamedTextColor.WHITE)), "Vittorie: " + wins));
        gui.setItem('l', Items.renameColor(Material.PUFFERFISH, "Sconfitte: " + losses, NamedTextColor.GOLD));
        gui.setItem('k', Items.renameColor(Material.IRON_SWORD, "Uccisioni: " + kills, NamedTextColor.GOLD));
        gui.setItem('d', Items.renameColor(Material.BARRIER, "Morti: " + deaths, NamedTextColor.GOLD));
        gui.setItem('f', Items.renameColor(Material.DIAMOND_SWORD, "Uccisioni finali: " + finals, NamedTextColor.GOLD));
        gui.setItem('b', Items.renameColor(Material.RED_BED, "Letti rotti: " + bedsBroken, NamedTextColor.GOLD));
        gui.setItem('r', Items.renameColor(Material.GOLDEN_SWORD, "Rapporto uccisioni/morti: " +
                String.format("%.2f", (double)kills / (deaths == 0 ? 1 : deaths)), NamedTextColor.GOLD)); // 2 cifre decimali
        gui.setItem('s', Items.renameColor(Material.BOW, "Rapporto uccisioni finali/morti: " +
                String.format("%.2f", (double)finals / (deaths == 0 ? 1 : deaths)), NamedTextColor.GOLD));
        gui.setItem('a', Items.renameEnchant(Material.BOOK, "Achievements"));
        gui.applyPattern();
        gui.showToPlayer((Player) player);
    }
}
