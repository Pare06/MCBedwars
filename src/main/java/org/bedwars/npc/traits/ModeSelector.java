package org.bedwars.npc.traits;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bedwars.game.Arena;
import org.bedwars.game.ArenaLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import static org.bedwars.config.NPCConfig.*;

@TraitName("ModeSelector")
public class ModeSelector extends Trait {
    public ModeSelector() {
        super("ModeSelector");
    }

    // click destro sull'NPC "seleziona modalità"
    @EventHandler
    public void onRightClicked(NPCRightClickEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        Player player = event.getClicker();
        switch (event.getNPC().getRawName()) {
            case SOLO_START -> {
                Arena arena = ArenaLoader.getBestArena();
                if (arena == null) {
                    arena = ArenaLoader.loadArena();
                }
                arena.teleportPlayer(player);
            }
            case DOUBLES_START, TRIO_START, SQUAD_START -> player.sendMessage("non ancora fatto lol");
        }
    }
}
