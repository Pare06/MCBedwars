package org.bedwars.config;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;

public class ChatConfig {
    private ChatConfig() { }

    public static final int[] LEVEL_TIERS = {1, 5, 10, 25, 50, 100};
    public static final NamedTextColor[] CHAT_COLORS = {NamedTextColor.GRAY, NamedTextColor.WHITE, NamedTextColor.GREEN, NamedTextColor.RED, NamedTextColor.BLUE, NamedTextColor.GOLD};
    @SuppressWarnings("deprecation") // i Component (e quindi anche NamedTextColor) non funzionano sulla scoreboard
    public static final ChatColor[] LEGACY_CHAT_COLORS = {ChatColor.GRAY, ChatColor.WHITE, ChatColor.GREEN, ChatColor.RED, ChatColor.BLUE, ChatColor.GOLD};

    public static NamedTextColor levelColor(int level) {
        return getTier(level, CHAT_COLORS);
    }

    @SuppressWarnings("deprecation")
    public static ChatColor legacyLevelColor(int level) {
        return getTier(level, LEGACY_CHAT_COLORS);
    }

    private static <E> E getTier(int level, E[] array) {
        int tier = 0;
        for (; tier != array.length && level >= LEVEL_TIERS[tier]; tier++);
        return array[tier - 1];
    }
}
