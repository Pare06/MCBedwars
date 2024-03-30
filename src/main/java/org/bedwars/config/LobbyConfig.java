package org.bedwars.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LobbyConfig {
    private LobbyConfig() { }

    public static final World LOBBY = Bukkit.getWorld("lobby");
    public static final Location SPAWN = new Location(LOBBY, -25.5, 70, 0.5, -90, 0);

    // posizioni npc
    public static final Location SOLO_LOCATION = new Location(LOBBY, 1.5, 68, -5.5, 75, 0);
    public static final Location DOUBLES_LOCATION = new Location(LOBBY, 3.5, 68, -2.5, 85, 0);
    public static final Location TRIO_LOCATION = new Location(LOBBY, 3.5, 68, 3.5, 95, 0);
    public static final Location SQUAD_LOCATION = new Location(LOBBY, 1.5, 68, 6.5, 105, 0);
}
