package org.bedwars.game.commands;

import org.bedwars.game.Arena;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ForceStartCommand implements CommandExecutor { // /forcestart, /fs
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof ConsoleCommandSender) {
            Bukkit.getLogger().info("Questo comando non è eseguibile da console!");
            return true;
        }

        Player player = (Player) commandSender;
        World world = player.getWorld();

        if (Arena.isNotArena(world)) {
            player.sendMessage("Non sei in un'arena!");
            return true;
        }
        Arena arena = Arena.getArena(world);
        if (arena.getState() == Arena.ArenaState.STARTED) {
            player.sendMessage("L'arena è già cominciata!");
            return true;
        }
        if (arena.getNPlayers() == 1) {
            player.sendMessage("Servono almeno 2 giocatori!");
            return true;
        }

        arena.setCountdown(0);
        arena.startGame();
        return true;
    }
}
