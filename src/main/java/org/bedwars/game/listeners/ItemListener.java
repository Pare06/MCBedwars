package org.bedwars.game.listeners;

import org.bedwars.Bedwars;
import org.bedwars.config.Achievements;
import org.bedwars.config.MetadataConfig;
import org.bedwars.game.Arena;
import org.bedwars.utils.BWPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ItemListener implements Listener {
    // 1: blocco (nullable), 2[0]: player
    private final static Map<Material, BiFunction<Location, List<Object>, Boolean>> ITEM_EFFECTS = new HashMap<>();

    public static void initialize() {
        // .put(materiale con cui viene fatto click destro, funzione da eseguire)
        ITEM_EFFECTS.put(Material.TNT, (blockLoc, list) -> {
            // genera una TNT attivata se si clicca su un blocco
            if (blockLoc != null) {
                TNTPrimed tnt = spawn(blockLoc, TNTPrimed.class);
                tnt.setFuseTicks(2 * 20);
                tnt.setMetadata(MetadataConfig.SUMMONED_BY, new FixedMetadataValue(Bedwars.Plugin, list.get(0)));
            }
            return blockLoc != null;
        });
        ITEM_EFFECTS.put(Material.FIRE_CHARGE, (blockLoc, list) -> {
            // lancia una fireball con la direzione del player
            Player shooter = (Player) list.get(0);
            Location playerLoc = shooter.getLocation();
            Vector playerVec = playerLoc.getDirection().clone();
            playerVec.multiply(1.5);
            playerVec.setY(0);
            Location fireballLoc = playerLoc.add(0, 1, 0).add(playerVec);

            Fireball fireball = spawn(fireballLoc, Fireball.class);
            fireball.setRotation(playerLoc.getYaw(), playerLoc.getPitch());
            fireball.setYield(fireball.getYield() * 2);
            fireball.setShooter(shooter);
            return true;
        });
        ITEM_EFFECTS.put(Material.SLIME_BLOCK, (blockLoc, list) -> {
            // genera uno slimeblock che viene lanciato in aria e fa volare chi viene toccato
            if (blockLoc != null) {
                FallingBlock fB = blockLoc.getWorld().spawn(blockLoc, FallingBlock.class, (block -> block.setBlockData(Material.SLIME_BLOCK.createBlockData())));
                fB.setVelocity(new Vector(0, 0.5, 0));
                fB.setCancelDrop(true);
                fB.shouldAutoExpire(false);
                fB.setDropItem(false);
                blockLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, blockLoc, 50, 1, 0, 1, 0.01);

                new BukkitRunnable() {
                    final List<OfflinePlayer> hitPlayers = new ArrayList<>(); // players colpiti
                    @Override
                    public void run() {
                        // per ogni player colpito (= a meno di 1 blocco di distanza)
                        for (Player p : fB.getLocation().getNearbyPlayers(1).stream().filter(p -> !hitPlayers.contains(p)).toList()) {
                            Vector velocity = p.getVelocity();
                            p.setVelocity(velocity.add(new Vector(0, 0.75, 0))); // lancia in aria
                            hitPlayers.add(p);

                            boolean fullTeam = true;
                            for (OfflinePlayer oP : BWPlayer.get(p).getTeam().getPlayers()) {
                                if (!hitPlayers.contains(oP)) {
                                    fullTeam = false;
                                    break;
                                }
                            }
                            if (fullTeam) { // achievement?
                                BWPlayer.get((OfflinePlayer) list.get(0)).getAchievements().setAchievement(Achievements.EARTHQUAKE);
                            }
                        }
                        // sta scendendo?
                        if (fB.getVelocity().getY() <= 0) {
                            fB.remove();
                        }
                        if (fB.isDead()) {
                            cancel();
                        }
                    }
                }.runTaskTimer(Bedwars.Plugin, 0, 2); // ogni 2 ticks (0.1s)
            }
            return blockLoc != null;
        });
    }

    private static <E extends Entity> E spawn(Location l, Class<E> c) {
        return l.getWorld().spawn(l, c);
    }

    // click destro
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BWPlayer bwPlayer = BWPlayer.get(player);

        if (bwPlayer.getArena() == null || bwPlayer.getArena().getState() != Arena.ArenaState.STARTED) return;

        Material type = player.getInventory().getItemInMainHand().getType();
        BiFunction<Location, List<Object>, Boolean> effect = ITEM_EFFECTS.get(type);

        // esiste una funzione collegata all'oggetto cliccato?
        if (effect != null) {
            boolean result = effect.apply(event.getInteractionPoint(), List.of(player));
            if (result) { // funzione eseguita?
                event.setCancelled(true);
                player.getInventory().removeItem(new ItemStack(type, 1));
            }
        }
    }

    // snowball hit
    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (Arena.isNotArena(snowball.getLocation().getWorld())) return;
        Player player = (Player) snowball.getShooter();

        @SuppressWarnings("DataFlowIssue") // non dovrebbe mai esistere una snowball non lanciata da un player
        BWPlayer bwPlayer = BWPlayer.get(player);

        Silverfish silverfish = spawn(snowball.getLocation(), Silverfish.class);
        silverfish.setMetadata(MetadataConfig.SUMMONED_BY, new FixedMetadataValue(Bedwars.Plugin, player));
        silverfish.customName(player.name().color(bwPlayer.getTeam().getTextColor()));
    }

    // esplosione TNT
    @EventHandler
    public void onTntFuse(EntityExplodeEvent event) {
        event.blockList().removeIf(b -> !b.hasMetadata(MetadataConfig.PLAYER_PLACED)); // fai esplodere solo i blocchi piazzati da un player
    }

    // blocco bruciato
    @EventHandler
    public void onBlockBurn(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    // silverfish target
    @EventHandler
    public void onSilverfishAttack(EntityTargetLivingEntityEvent event) {
        if (event.getEntityType() == EntityType.SILVERFISH) {
            if (event.getTarget() == event.getEntity().getMetadata(MetadataConfig.SUMMONED_BY).get(0).value()) {
                event.setCancelled(true); // non andare dal player che ha generato il silferfish
            }
        }
    }
}
