package org.bedwars.lobby;

import org.bedwars.config.ChatConfig;
import org.bedwars.stats.GameStats;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.BWScoreboard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class LobbyInterface {
    private LobbyInterface() { }

    @SuppressWarnings("deprecation") // namedtextcolor sulla scoreboard non funziona
    public static void sendLobbyScoreboard(Player player) {
        BWScoreboard scoreboard = new BWScoreboard(player);
        BWPlayer bwPlayer = BWPlayer.get(player);
        GameStats stats = bwPlayer.getGameStats();

        ArrayList<String> scores = new ArrayList<>();
        scores.add(" ");
        scores.add(String.format("Livello: [%s%d%s★]", ChatConfig.legacyLevelColor(stats.getLevel()),
                                                        stats.getLevel(), ChatColor.RESET));
        scores.add(String.format("EXP: %d/%d", stats.getPoints(), stats.requiredXPToNextLevel()));
        scores.add(experienceBar(stats));
        scores.add("  ");
        scores.add("Vittorie: " + stats.getWins());
        scores.add("Sconfitte: " + stats.getLosses());
        scores.add("   ");
        scores.add("example.com");

        scoreboard.loadScores(scores);
        bwPlayer.setScoreboard(scoreboard);
    }

    // 0%   -> [          ]
    // 50%  -> [█████     ]
    // 100% -> [██████████]
    private static String experienceBar(GameStats stats) {
        StringBuilder string = new StringBuilder("[");

        double xp;
        for (xp = 0; xp <= (double)stats.getPoints() / stats.requiredXPToNextLevel() /* [0 .. 1] */; xp += 0.1) {
            string.append('█');
        }
        for (; xp <= 1; xp += 0.1) {
            string.append(' ');
        }
        string.append(']');

        return string.toString();
    }
}
