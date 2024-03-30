package org.bedwars.stats.achievements;

import com.google.common.math.IntMath;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bedwars.config.Achievements;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una collezione di {@link Achievement} e {@link TieredAchievement}.
 */
public class AchievementData {
    // ogni int rappresenta 32 achievement (= bit)
    private final int[] sets;
    private OfflinePlayer player;

    /**
     * Crea un {@code AchievementData}.
     * @param player il proprietario degli achievement
     */
    public AchievementData(OfflinePlayer player) {
        sets = new int[Achievements.ACHIEVEMENTS_SETS];
        this.player = player;
    }

    /**
     * Ricarica il proprietario degli achievement.
     * @param p il {@code Player} aggiornato
     */
    public void refresh(Player p) {
        player = p;
    }

    /**
     * Carica gli achievement dal risultato di una query SQL.
     * @param set la query
     */
    public void loadFromQuery(ResultSet set) {
        try {
            sets[0] = set.getInt("set1");
        } catch (SQLException e) {
            throw new RuntimeException(e); // impossibile
        }
    }

    /**
     * Restituisce l'{@code n}-esimo set di achievement.
     * @param n il set da prendere
     * @return l'{@code n}-esimo set di achievement
     */
    public int getSet(int n) {
        return sets[n];
    }

    /**
     * Restituisce lo stato dell'{@code Achievement} dato.
     * @param ach l'{@code Achievement}
     * @return lo stato dell'{@code Achievement}
     */
    public boolean getAchievement(Achievement ach) {
        return (sets[ach.getId() / 32] >>> (ach.getId() % 32) & 1) == 1;
    }

    /**
     * Sblocca l'{@code Achievement} dato.
     * @param ach l'{@code Achievement}
     */
    @SuppressWarnings("DataFlowIssue") // se il player è online, getPlayer() non può ritornare null
    public void setAchievement(Achievement ach) {
        if (!getAchievement(ach)) {
            sets[ach.getId() / 32] |= 1 << ach.getId() % 32;
            if (player.isOnline()) player.getPlayer().sendMessage((Component.text("Achievement sbloccato! ").color(NamedTextColor.GREEN))
                    .append(Component.text(ach.getName()).color(ach.getRarity().getColor())).hoverEvent(HoverEvent.showText(Component.text(ach.getDescription()).color(NamedTextColor.AQUA))));
        }
    }

    public int getTieredAchievement(TieredAchievement ach) {
        int power = IntMath.log2(ach.getMaxTiers(), RoundingMode.CEILING);

        return (sets[ach.getId() / 32] >>> (ach.getId() % 32)) & ((1 << power) - 1);
    }

    /**
     * Sblocca, o aumenta, il {@code TieredAchievement} dato in base al valore della statistica corrispondente ad esso.
     * @param ach il {@code TieredAchievement}
     * @param value la statistica
     */
    @SuppressWarnings("DataFlowIssue")
    public void setTieredAchievement(TieredAchievement ach, int value) {
        int tier = ach.highestTier(value);

        if (getTieredAchievement(ach) >= tier) return; // già sbloccato?

        int setId = ach.getId() / 32;
        int achId = ach.getId() % 32;

        // esempio:
        //
        // set      (..)00011100
        // tier     (..)00000101 (5)
        // id              ^     (pos. 4)
        // tierMax: 4

        // calcoliamo la potenza del 2 più alta, ma <= al tier più alto possibile - ci interessa solo l'esponente, quindi il log base 2 (arrotondato per eccesso, +1)
        // power = log2(4) + 1 = 3 (il n. di bit necessari per rappresentare 4)
        //
        // ora ci servono i bit da sostituire al tier, quindi i bit del tier spostati di 'id' bit (cioè che partono dal bit id)
        // newBits = tier << id = 101 << 4 = 1010000
        //
        // adesso dobbiamo trovare i bit da azzerare (per non lasciare parte del tier vecchio)
        // ci servono tutti i bit del tier impostati a 0, e tutto il resto a 1
        // clearMask = ~((1 << 3 - 1) << 4) (dove 1 << 3 - 1 sono tutti i bit del tier impostati a 1)
        //           = ~(111 << 4) = ~(1110000)
        //           = (tanti 1) + 1110001111
        //
        // azzeriamo i bit per il tier:
        // clearedSet =     000011100 &
        //              (..)110001111
        //            = (..)000001100
        //
        // inseriamo i bit nuovi:
        // set = 000001100 |
        //       001010000
        //     = 001011100

        int power = IntMath.log2(ach.getMaxTiers(), RoundingMode.CEILING) + 1;
        int newBits = tier << achId;
        int clearMask = ~(((1 << power) - 1) << achId);
        int clearedSet = sets[setId] & clearMask;
        sets[setId] = clearedSet | newBits;

        if (player.isOnline()) {
            player.getPlayer().sendMessage((Component.text( "Achievement sbloccato! ").color(NamedTextColor.GREEN))
                    .append(Component.text(ach.getName() + toRoman(tier)).color(Rarity.getRarity(tier).getColor())).hoverEvent(HoverEvent.showText(Component.text(
                            String.format(ach.getDescription(), value)).color(NamedTextColor.AQUA))));
        }
    }

    public void showGUI() {
        showGUI(Bukkit.createInventory(null, 54, Component.text("Achievements")), 0, true);
    }

    public void showGUI(Inventory inv, int page, boolean isNew) {
        //noinspection SwitchStatementWithTooFewBranches (da togliere quando ci saranno 3+ pagine)
        switch (page) {
            case 0:
                inv.setItem(0, getAchievementStatus(Achievements.WELCOME));
                inv.setItem(1, getAchievementStatus(Achievements.HOTSTREAK));
                inv.setItem(2, getAchievementStatus(Achievements.FIREBALL_BRIDGE));
                inv.setItem(3, getAchievementStatus(Achievements.EARTHQUAKE));

                // assume che ogni achievement abbia esattamente 4 tier
                for (int i = 1; i <= 4; i++) {
                    inv.setItem(i * 9 + 9, getAchievementStatus(Achievements.WINNER, i));
                    inv.setItem(i * 9 + 10, getAchievementStatus(Achievements.KILLER, i));
                    inv.setItem(i * 9 + 11, getAchievementStatus(Achievements.BEDS, i));
                }

                inv.setItem(53, nextItem(1));
                break;
            // aggiungere altri case dopo aver finito lo spazio
        }
        if (isNew) ((Player) player).openInventory(inv);
    }

    private ItemStack getAchievementStatus(Achievement ach) {
        boolean isGranted = getAchievement(ach);

        Component itemName = Component.text(ach.getName()).color(ach.getRarity().getColor());
        Material material = isGranted ? ach.getItem() : Material.RED_STAINED_GLASS_PANE;
        List<Component> desc = new ArrayList<>();
        for (String line : ach.getDescription().split("\n")) {
            desc.add(Component.text(line).color(isGranted ? NamedTextColor.GREEN : NamedTextColor.DARK_RED));
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(itemName);
        meta.lore(desc);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack getAchievementStatus(TieredAchievement ach, int tier) {
        boolean isGranted = getTieredAchievement(ach) >= tier;

        Component itemName = Component.text(ach.getName() + toRoman(tier)).color(Rarity.getRarity(tier).getColor());
        Material material = isGranted ? ach.getItems()[tier - 1] : Material.RED_STAINED_GLASS_PANE;
        Component desc = Component.text(String.format(ach.getDescription(), ach.getTiers()[tier - 1]))
                .color(isGranted ? NamedTextColor.GREEN : NamedTextColor.DARK_RED);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(itemName);
        meta.lore(List.of(desc));
        item.setItemMeta(meta);

        return item;
    }

    public static String toRoman(int i) {
        String[] romans = { " I", " II", " III", " IV", " V" };
        return romans[i - 1];
    }

    @SuppressWarnings("SameParameterValue") // da togliere dopo aver fatto la seconda pagina
    private static ItemStack nextItem(int nextPage) {
        ItemStack item = new ItemStack(Material.BLUE_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Pagina " + (nextPage + 1)).color(NamedTextColor.GREEN));
        item.setItemMeta(meta);
        return item;
    }
}