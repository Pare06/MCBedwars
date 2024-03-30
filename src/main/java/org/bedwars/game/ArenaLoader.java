package org.bedwars.game;

import org.apache.commons.io.FileUtils;
import org.bedwars.config.ArenaConfig;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ArenaLoader {
    private ArenaLoader() { }

    private static int arenaNumber = 0; // per evitare 2 mondi uguali
    private static List<String> worldQueue;
    private static List<Arena> arenas;

    public static void initialize() {
        worldQueue = new ArrayList<>();
        arenas = new ArrayList<>();
        loadQueue();
        ArenaConfig.initialize();
    }

    public static Arena loadArena() {
        return loadArena(nextArena());
    }

    private static Arena loadArena(String worldName) {
        String name = String.format("%s_%d", worldName, arenaNumber); // nomearena_id

        // /arenas/cartella_mondo
        File src = new File(Paths.get(Bukkit.getWorldContainer().getAbsoluteFile().getParentFile().getAbsolutePath(), "arenas", worldName).toUri());
        // /worlds/cartella_mondo_arenaNumber
        File dst = new File(Paths.get(Bukkit.getWorldContainer().getAbsolutePath(), name).toUri());

        try {
            FileUtils.copyDirectory(src, dst); // copia l'arena
        } catch (IOException e) {
            throw new RuntimeException(e); // impossibile
        }
        WorldCreator wc = new WorldCreator(name);
        Bukkit.createWorld(wc); // crea il mondo

        Arena arena = new Arena(name, worldName);
        arenas.add(arena);
        moveQueue();
        arenaNumber++;

        return arena;
    }

    private static void loadQueue() {
        for (String s : getAllWorlds()) {
            if (!s.equals("lobby")) {
                worldQueue.add(s);
            }
        }
    }

    /**
     * Restituisce l'{@code Arena} in attesa con più giocatori, o {@code null} se non ce n'è una.
     * @return l'{@code Arena} con più giocatori in attesa
     */
    public static Arena getBestArena() {
        Optional<Arena> best = arenas.stream().filter(a -> a.getState() != Arena.ArenaState.STARTED) // tutte le arene non startate
                .filter(a -> a.getNPlayers() != a.getMaxPlayers())
                .max(Comparator.comparing(Arena::getNPlayers));
        return best.orElse(null);
    }

    /**
     * Cancella un'{@code Arena} dalla lista.
     * @param a l'{@code Arena} da cancellare
     */
    public static void deleteArena(Arena a) {
        arenas.remove(a);
    }

    private static String nextArena() {
        return worldQueue.get(0);
    }

    private static void moveQueue() {
        worldQueue.add(worldQueue.remove(0)); // sposta il primo elemento in fondo
    }

    @SuppressWarnings("DataFlowIssue")
    private static List<String> getAllWorlds() {
        return Arrays.stream(new File(ArenaConfig.ARENA_FOLDER.toUri()).list()).toList(); // /arenas/*
    }

    /**
     * Restituisce tutte le {@code Arena} attive.
     * @return tutte le {@code Arena}
     */
    public static List<Arena> getArenas() {
        return arenas;
    }
}
