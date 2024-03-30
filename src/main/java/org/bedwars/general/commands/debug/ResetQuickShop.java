package org.bedwars.general.commands.debug;

import org.bedwars.utils.BWPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class ResetQuickShop implements CommandExecutor { // /resetquick, /rq
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        BWPlayer.get((OfflinePlayer) sender).setQuickShop(new ArrayList<>(Arrays.stream("9 0 3 8 26 23 25 10 1 4 6 -1 24 12 2 5 7 -2 27 -3 -3".split(" ")).map(Integer::valueOf).toList()));
        return true;
    }
}
