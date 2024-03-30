package org.bedwars.general.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GameModeCommand implements CommandExecutor, TabCompleter { // /gamemode, /gm
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (commandSender instanceof ConsoleCommandSender) { // console?
            Bukkit.getLogger().info("Questo comando non è eseguibile da console!");
            return true;
        }
        if (strings.length != 1) {
            return false;
        }

        Player player = (Player) commandSender;
        int gmIndex;

        try {
            gmIndex = Integer.parseInt(strings[0]);
        } catch (NumberFormatException ex) { // non è un numero?
            return false;
        }
        if (gmIndex < 0 || gmIndex > 3) {
            return false;
        }

        player.setGameMode(switch (gmIndex) {
            case 0 -> GameMode.SURVIVAL;
            case 1 -> GameMode.CREATIVE;
            case 2 -> GameMode.ADVENTURE;
            case 3 -> GameMode.SPECTATOR;
            default -> throw new RuntimeException(); // impossibile
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        List<String> validArgs = List.of(new String[] { "0", "1", "2", "3" }); // possibile 1° argomento
        // ritorna tutti gli elementi in validArgs che iniziano per strings[0]
        return strings.length == 1 ? StringUtil.copyPartialMatches(strings[0], validArgs, new ArrayList<>()) : new ArrayList<>();
    }
}
