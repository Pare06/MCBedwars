package org.bedwars.npc.traits;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bedwars.config.ShopConfig;
import org.bedwars.game.shop.ShopItem;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.GUI;
import org.bedwars.utils.WoolUtils;
import org.bedwars.utils.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bedwars.config.ShopConfig.allItems;
import static org.bedwars.config.ShopConfig.sectionNames;

@TraitName("BaseShop")
public class BaseShop extends Trait {
    public static final int QS_PICKAXE = -1;
    public static final int QS_AXE = -2;
    public static final int QS_NULL = -3;
    public static final int PICKAXE_OFFSET = 13;
    public static final int AXE_OFFSET = 18;

    public BaseShop() {
        super("BaseShop");
    }

    // click destro su un NPC
    @EventHandler
    public void onPlayerInteractAtEntity(NPCRightClickEvent event) {
        Player player = event.getClicker();

        if (event.getNPC() == this.getNPC() && BWPlayer.get(player).getTeam() != null) {
            event.setCancelled(true);
            openShop(player, Material.NETHER_STAR, true);
        }
    }

    /**
     * Apre il negozio se {@code player} non ne aveva aperto uno, altrimenti lo aggiorna.
     * @param player il {@code Player} a cui aprire il negozio
     * @param section la sezione da visualizzare
     * @param isNew aveva aperto un negozio prima?
     */
    public static void openShop(Player player, Material section, boolean isNew) {
        GUI shop = isNew
                ? new GUI(sectionNames.get(sectionMaterials.indexOf(section)), 45)
                : BWPlayer.get(player).getGui();

        shop.setPattern("012345678" +
                "GGGGGGGGG" +
                " abcdefg " +
                " hijklmn " +
                " opqrstu ");
        shop.setItem('0', Material.NETHER_STAR, "Scelta rapida");
        shop.setItem('1', Material.IRON_SWORD, "Armi");
        shop.setItem('2', Material.IRON_CHESTPLATE, "Armature");
        shop.setItem('3', Material.BOW, "Archi");

        BWPlayer bwPlayer = BWPlayer.get(player);
        bwPlayer.setShopSection(section);
        int woolIndex = BWPlayer.get(player).getIndex();
        Material woolMaterial = WoolUtils.woolMaterials[woolIndex];
        shop.setItem('4', woolMaterial, "Blocchi");
        shop.setItem('5', Material.IRON_PICKAXE, "Strumenti");

        for (Material material : sectionMaterials) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();

            if(material == section) {
                Items.enchant(item);
                lore.add(Component.text("Selezionato")
                        .color(NamedTextColor.GREEN));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        shop.setItem('G', Material.WHITE_STAINED_GLASS_PANE);
        bwPlayer.setGui(shop);
        loadSection(section, player);
        shop.applyPattern();
        if (isNew) shop.showToPlayer(player);
    }

    /**
     * Carica gli oggetti di una sezione e li visualizza nel negozio del {@code Player}.
     * @param section la sezione da visualizzare
     * @param player il {@code Player} a cui mandare gli oggetti
     */
    @SuppressWarnings("DataFlowIssue")
    public static void loadSection(Material section, Player player) {
        List<ShopItem> sectionItems = new ArrayList<>(allItems.get(sectionMaterials.indexOf(section)));
        BWPlayer bwPlayer = BWPlayer.get(player);
        GUI shop = bwPlayer.getGui();
        String alphabet = "abcdefghijklmnopqrstu"; // per accedere solo agli slot dedicati agli oggetti - vedi openShop

        String quickShop = bwPlayer.getQuickShop().replace("-1", String.valueOf(bwPlayer.getPickaxeTier() + PICKAXE_OFFSET))
                                                  .replace("-2", String.valueOf(bwPlayer.getAxeTier() + AXE_OFFSET));
        List<ShopItem> quickShopItems = new ArrayList<>(Arrays.stream(quickShop.split(" ")).map(Integer::valueOf).map(ShopItem::get).toList());

        if (section == Material.NETHER_STAR) {
            sectionItems = quickShopItems;
        }

        if (section == Material.IRON_PICKAXE) {
            addTools(sectionItems, bwPlayer.getPickaxeTier(), bwPlayer.getAxeTier());
        }

        int index = 0;
        for (char ch : alphabet.toCharArray()) { // cancella tutti gli item
            shop.setItem(ch, Material.AIR);
        }
        for (int i = 17; i < 44; i++) { // file 3, 4, 5
            if (sectionItems.isEmpty()) break; // sezione finita?

            ShopItem sItem = sectionItems.remove(0);

            ItemStack item;
            if (sItem != null) {
                item = sItem.getItem().clone();
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = meta.lore();
                lore.add(Component.text("Costo: ")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(String.format("%d %s", sItem.getCost(), ShopItem.getMaterialName(sItem.getCostingMaterial())))
                                .color(player.getInventory().contains(sItem.getCostingMaterial(), sItem.getCost()) // il player ha abbastanza materiali?
                                        ? NamedTextColor.GREEN
                                        : NamedTextColor.RED)));

                if (section == Material.NETHER_STAR) {
                    lore.add(Component.empty());
                    lore.add(Component.text("Premi ").color(NamedTextColor.WHITE).append(
                            Component.text("SHIFT+TASTO SINISTRO").color(NamedTextColor.GRAY)));
                    lore.add(Component.text(bwPlayer.isEditingQuickShop() ? "per sostituire questo oggetto!" : "per rimuovere questo oggetto!").color(NamedTextColor.WHITE));
                }
                meta.lore(lore);
                item.setItemMeta(meta);

                // lana?
                if (item.getType() == Material.DEAD_BUSH) {
                    item.setType(WoolUtils.woolMaterials[bwPlayer.getIndex()]);
                    item.setAmount(16);
                }
            } else {
                item = ShopConfig.nullItem;
            }

            shop.setItem(alphabet.charAt(index), item);
            index++;
        }
        shop.applyPattern();
    }

    private static void addTools(List<ShopItem> items, int pickaxe, int axe) {
        items.add(0, ShopConfig.pickaxes.get(pickaxe)); // >= 0
        items.add(1, ShopConfig.axes.get(axe));
    }

    public static final List<Material> sectionMaterials = List.of(new Material[] {
            Material.NETHER_STAR, Material.IRON_SWORD, Material.IRON_CHESTPLATE,
            Material.BOW, Material.DEAD_BUSH, Material.IRON_PICKAXE
    });
}
