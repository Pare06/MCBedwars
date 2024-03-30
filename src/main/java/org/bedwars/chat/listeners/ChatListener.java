package org.bedwars.chat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bedwars.config.ChatConfig;
import org.bedwars.game.Arena;
import org.bedwars.utils.BWPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    @EventHandler
    public void onChatMessage(AsyncChatEvent event) {
        event.setCancelled(true); // blocca il messaggio originale

        Player player = event.getPlayer();
        BWPlayer bwPlayer = BWPlayer.get(player);
        World world = player.getWorld();
        boolean gameStarted = bwPlayer.getArena() != null && bwPlayer.getArena().getState() == Arena.ArenaState.STARTED;
        boolean teamOnly = gameStarted // startata?
                    //&& bwPlayer.getTeam().getMaxPlayers() != 1 // non è in modalità solo? (TOGLIERE COMMENTO DOPO AVER AGGIUNTO DUO/TRIO/4V4)
                    && !((TextComponent) event.message()).content().startsWith("!"); // non comincia con '!' ?

        TextComponent message = buildMessage(player, ((TextComponent)event.message()).content(), !teamOnly, gameStarted, false);

        if (teamOnly && bwPlayer.getTeam().getMaxPlayers() != 1) {
            bwPlayer.getTeam().getOnlinePlayers().forEach(p -> p.sendMessage(message));
        } else {
            world.getPlayers().forEach(p -> p.sendMessage(message));
        }
    }

    public static TextComponent buildMessage(Player p, String message, boolean shouted, boolean gameStarted, boolean fromCommand) {
        BWPlayer bwPlayer = BWPlayer.get(p);
        boolean isPlaying = bwPlayer.getArena() != null && bwPlayer.getArena().getState() == Arena.ArenaState.STARTED;

        if (shouted && gameStarted && !fromCommand) {
            message = message.substring(1); // togli !
        }

        TextComponent leftBracket = Component.text("[").color(NamedTextColor.WHITE);
        TextComponent level = Component.text(p.getLevel()).color(ChatConfig.levelColor(p.getLevel()));
        TextComponent rightBracket = Component.text("★] ").color(NamedTextColor.WHITE);
        TextComponent flair = bwPlayer.getFlair().getChatView();
        TextComponent shout = shouted && gameStarted ? Component.text("[SHOUT] ").color(NamedTextColor.RED) : Component.empty();
        TextComponent playerName = Component.text(p.getName()).color(
            isPlaying ? bwPlayer.getTeam().getTextColor() : NamedTextColor.WHITE
        );
        TextComponent colonMessage = Component.text(": " + message).color(NamedTextColor.WHITE);

        // [lvl★] (opzionale)[SHOUT] player: msg
        return leftBracket.append(level).append(rightBracket).append(shout).append(flair).append(playerName).append(colonMessage);
    }
}
