package org.bedwars.general.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameModeAlias implements CommandExecutor { // gms, gmc, gma, gmsp
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (commandSender instanceof ConsoleCommandSender) { // console?
            Bukkit.getLogger().info("Questo comando non è eseguibile da console!");
            return true;
        }

        Player player = (Player) commandSender;
        GameMode gm = switch (s) {
            case "gms" -> GameMode.SURVIVAL;
            case "gmc" -> GameMode.CREATIVE;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> throw new RuntimeException(); // impossibile
        };

        player.setGameMode(gm);

        return true;
    }
}
