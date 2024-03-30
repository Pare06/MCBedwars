package org.bedwars.general.commands;

import org.bedwars.utils.BWPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements CommandExecutor { // /stats, /st
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Bukkit.getLogger().info("Questo comando non è eseguibile da console!");
            return true;
        }

        Player player = (Player) sender;
        BWPlayer.get(player).getGameStats().showGUI();

        return true;
    }
}
