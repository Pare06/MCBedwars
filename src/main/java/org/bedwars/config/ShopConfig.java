package org.bedwars.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bedwars.game.shop.ShopItem;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.WoolUtils;
import org.bedwars.utils.Items;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopConfig {
    private ShopConfig() { }

    public static final List<List<ShopItem>> allItems = new ArrayList<>();
    private static final List<ShopItem> emptyItems = new ArrayList<>();
    private static final List<ShopItem> offensiveItems = new ArrayList<>();
    private static final List<ShopItem> armorItems = new ArrayList<>();
    private static final List<ShopItem> bowsItems = new ArrayList<>();
    private static final List<ShopItem> blocksItems = new ArrayList<>();
    private static final List<ShopItem> toolsItems = new ArrayList<>();
    public static final List<Component> sectionNames = new ArrayList<>();

    public static ShopItem woolItem;
    public static ItemStack nullItem;
    public static final List<ShopItem> pickaxes = new ArrayList<>();
    public static final List<ShopItem> axes = new ArrayList<>();

    public static void initialize() {
        // NON RIORDINARE PER NESSUN MOTIVO GLI ITEM SENZA PRIMA CAMBIARE players.quickShop !!!!!!!

        // Spade
        offensiveItems.add(new ShopItem(Material.STONE_SWORD, 10, Material.IRON_INGOT, null, ShopConfig::stoneSword, false));
        offensiveItems.add(new ShopItem(Material.IRON_SWORD, 7, Material.GOLD_INGOT, null, ShopConfig::ironSword, false));
        offensiveItems.add(new ShopItem(Material.DIAMOND_SWORD, 4, Material.EMERALD, null, ShopConfig::diamondSword, false));

        // Armature
        armorItems.add(new ShopItem(Material.CHAINMAIL_BOOTS, 40, Material.IRON_INGOT, null, ShopConfig::chainArmor, false));
        armorItems.add(new ShopItem(Material.IRON_BOOTS, 12, Material.GOLD_INGOT, null, ShopConfig::ironArmor, false));
        armorItems.add(new ShopItem(Material.DIAMOND_BOOTS, 6, Material.EMERALD, null, ShopConfig::diamondArmor, false));

        // Archi
        bowsItems.add(new ShopItem(Material.BOW, 12, Material.GOLD_INGOT));
        bowsItems.add(new ShopItem(Items.renameEnchant(Material.BOW, "Arco potenziato"), 36, Material.GOLD_INGOT, null, ShopConfig::powerBow, false));
        bowsItems.add(new ShopItem(Items.quantity(Material.ARROW, 16), 16, Material.GOLD_INGOT));

        // la lana - è un caso a parte
        woolItem = new ShopItem(Material.DEAD_BUSH, 4, Material.IRON_INGOT, null, ShopConfig::wool, false);
        blocksItems.add(woolItem);

        // Blocchi
        blocksItems.add(new ShopItem(Items.quantity(Material.OAK_PLANKS, 4), 4, Material.GOLD_INGOT));
        blocksItems.add(new ShopItem(Items.quantity(Material.END_STONE, 12), 36, Material.IRON_INGOT));
        blocksItems.add(new ShopItem(Items.quantity(Material.OBSIDIAN, 4), 4, Material.EMERALD));

        // pickaxe / axe
        ArrayList<Component> toolLore = new ArrayList<>(List.of(Component.text("Questo strumento scenderà di grado dopo la morte!")
                                            .color(NamedTextColor.GRAY)
                                            .decorate(TextDecoration.ITALIC)));
        ArrayList<Component> maxedLore = new ArrayList<>(List.of(Component.text("Questo strumento ha raggiunto il livello massimo!")
                                            .color(NamedTextColor.GRAY)
                                            .decorate(TextDecoration.ITALIC)));

        pickaxes.add(new ShopItem(Material.WOODEN_PICKAXE, 10, Material.IRON_INGOT, null, ShopConfig::pickaxe, true));
        pickaxes.add(new ShopItem(Items.enchant(Material.IRON_PICKAXE, Enchantment.DIG_SPEED, 1), 10, Material.IRON_INGOT, toolLore, ShopConfig::pickaxe, true));
        pickaxes.add(new ShopItem(Items.enchant(Material.GOLDEN_PICKAXE, Enchantment.DIG_SPEED, 1), 3, Material.GOLD_INGOT, toolLore, ShopConfig::pickaxe, true));
        pickaxes.add(new ShopItem(Items.enchant(Material.DIAMOND_PICKAXE, Enchantment.DIG_SPEED, 3), 6, Material.GOLD_INGOT, toolLore, ShopConfig::pickaxe, true));
        pickaxes.add(new ShopItem(Items.enchant(Material.DIAMOND_PICKAXE, Enchantment.DIG_SPEED, 3), 6, Material.GOLD_INGOT, maxedLore, ShopConfig::maxedTool, false));
        axes.add(new ShopItem(Material.WOODEN_AXE, 10, Material.IRON_INGOT, null, ShopConfig::axe, true));
        axes.add(new ShopItem(Items.enchant(Material.IRON_AXE, Enchantment.DIG_SPEED, 1), 10, Material.IRON_INGOT, toolLore, ShopConfig::axe, true));
        axes.add(new ShopItem(Items.enchant(Material.GOLDEN_AXE, Enchantment.DIG_SPEED, 1), 3, Material.GOLD_INGOT, toolLore, ShopConfig::axe, true));
        axes.add(new ShopItem(Items.enchant(Material.DIAMOND_AXE, Enchantment.DIG_SPEED, 3), 6, Material.GOLD_INGOT, toolLore, ShopConfig::axe, true));
        axes.add(new ShopItem(Items.enchant(Material.DIAMOND_AXE, Enchantment.DIG_SPEED, 3), 6, Material.GOLD_INGOT, maxedLore, ShopConfig::maxedTool, false));

        // oggetti
        toolsItems.add(new ShopItem(Material.GOLDEN_APPLE, 3, Material.GOLD_INGOT));
        toolsItems.add(new ShopItem(Material.FIRE_CHARGE, 50, Material.IRON_INGOT));
        toolsItems.add(new ShopItem(Material.SNOWBALL, 35, Material.IRON_INGOT));
        toolsItems.add(new ShopItem(Material.TNT, 8, Material.GOLD_INGOT));
        toolsItems.add(new ShopItem(Items.rename(Material.SLIME_BLOCK, "Trampolino"), 30, Material.IRON_INGOT));

        allItems.add(emptyItems); // scelta rapida
        allItems.add(offensiveItems);
        allItems.add(armorItems);
        allItems.add(bowsItems);
        allItems.add(blocksItems);
        allItems.add(toolsItems);

        sectionNames.add(Component.text("Scelta rapida").decoration(TextDecoration.ITALIC, false));
        sectionNames.add(Component.text("Armi").decoration(TextDecoration.ITALIC, false));
        sectionNames.add(Component.text("Armature").decoration(TextDecoration.ITALIC, false));
        sectionNames.add(Component.text("Archi").decoration(TextDecoration.ITALIC, false));
        sectionNames.add(Component.text("Blocchi").decoration(TextDecoration.ITALIC, false));
        sectionNames.add(Component.text("Strumenti").decoration(TextDecoration.ITALIC, false));

        nullItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = nullItem.getItemMeta();
        meta.displayName(Component.text("Nessun oggetto!").color(NamedTextColor.RED));
        meta.lore(List.of(Component.empty(),
                Component.text("Aggiungi qui un oggetto facendo").color(NamedTextColor.WHITE),
                Component.text("SHIFT+TASTO SINISTRO").color(NamedTextColor.GRAY),
                Component.text("su un oggetto nel negozio!").color(NamedTextColor.WHITE)));
        nullItem.setItemMeta(meta);
    }

    // funzioni degli item

    private static boolean stoneSword(Player p) {
        deleteWoodenSword(p);
        p.getInventory().addItem(Items.enchant(Material.STONE_SWORD, Enchantment.DAMAGE_ALL,
                                BWPlayer.get(p).getTeam().getUpgrade("swordDamage")));
        return true;
    }

    private static boolean ironSword(Player p) {
        deleteWoodenSword(p);
        p.getInventory().addItem(Items.enchant(Material.IRON_SWORD, Enchantment.DAMAGE_ALL,
                                 BWPlayer.get(p).getTeam().getUpgrade("swordDamage")));
        return true;
    }

    private static boolean diamondSword(Player p) {
        deleteWoodenSword(p);
        p.getInventory().addItem(Items.enchant(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL,
                                 BWPlayer.get(p).getTeam().getUpgrade("swordDamage")));
        return true;
    }

    private static void deleteWoodenSword(Player p) {
        p.getInventory().remove(Material.WOODEN_SWORD);
    }

    @SuppressWarnings("DataFlowIssue") // i boots non possono essere null
    private static boolean chainArmor(Player p) {
        // dà al player la chain armor (aggiornandoli su bwPlayer)
        BWPlayer bwPlayer = BWPlayer.get(p);
        int tier = bwPlayer.getTeam().getUpgrade("armorResistance");
        PlayerInventory inv = p.getInventory();

        if (inv.getBoots().getType() == Material.LEATHER_BOOTS) { // ha un'armatura peggiore?
            bwPlayer.setArmorPiece(EquipmentSlot.LEGS, Items.enchant(Material.CHAINMAIL_LEGGINGS,
                    Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            bwPlayer.setArmorPiece(EquipmentSlot.FEET, Items.enchant(Material.CHAINMAIL_BOOTS,
                    Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            inv.setLeggings(Items.enchant(Material.CHAINMAIL_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            inv.setBoots(Items.enchant(Material.CHAINMAIL_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            return true;
        } else {
            if (inv.getBoots().getType() == Material.CHAINMAIL_BOOTS) {
                p.sendMessage(Component.text("Hai già quest'armatura!")
                        .color(NamedTextColor.RED));
            } else {
                p.sendMessage(Component.text("Hai un'armatura migliore!")
                        .color(NamedTextColor.RED));
            }
            return false;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static boolean ironArmor(Player p) {
        BWPlayer bwPlayer = BWPlayer.get(p);
        int tier = bwPlayer.getTeam().getUpgrade("armorResistance");
        PlayerInventory inv = p.getInventory();

        if (inv.getBoots().getType() == Material.LEATHER_BOOTS
        ||  inv.getBoots().getType() == Material.CHAINMAIL_BOOTS) {
            bwPlayer.setArmorPiece(EquipmentSlot.LEGS, Items.enchant(Material.IRON_LEGGINGS,
                    Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            bwPlayer.setArmorPiece(EquipmentSlot.FEET, Items.enchant(Material.IRON_BOOTS,
                    Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            inv.setLeggings(Items.enchant(Material.IRON_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            inv.setBoots(Items.enchant(Material.IRON_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            return true;
        } else {
            if (inv.getBoots().getType() == Material.IRON_BOOTS) {
                p.sendMessage(Component.text("Hai già quest'armatura!")
                        .color(NamedTextColor.RED));
            } else {
                p.sendMessage(Component.text("Hai un'armatura migliore!")
                        .color(NamedTextColor.RED));
            }
            return false;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static boolean diamondArmor(Player p) {
        BWPlayer bwPlayer = BWPlayer.get(p);
        int tier = bwPlayer.getTeam().getUpgrade("armorResistance");
        PlayerInventory inv = p.getInventory();

        if (inv.getBoots().getType() == Material.LEATHER_BOOTS
        ||  inv.getBoots().getType() == Material.CHAINMAIL_BOOTS
        ||  inv.getBoots().getType() == Material.IRON_BOOTS) {
            bwPlayer.setArmorPiece(EquipmentSlot.LEGS, Items.enchant(Material.DIAMOND_LEGGINGS,
                    Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            bwPlayer.setArmorPiece(EquipmentSlot.FEET, Items.enchant(Material.DIAMOND_BOOTS,
                    Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            inv.setLeggings(Items.enchant(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            inv.setBoots(Items.enchant(Material.DIAMOND_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, tier));
            return true;
        } else {
            p.sendMessage(Component.text("Hai già quest'armatura!")
                    .color(NamedTextColor.RED));
            return false;
        }
    }

    private static boolean powerBow(Player p) {
        // arco con power I
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        p.getInventory().addItem(bow);
        return true;
    }

    private static boolean wool(Player p) {
        BWPlayer bwPlayer = BWPlayer.get(p);

        // lana del colore del player
        p.getInventory().addItem(Items.quantity(WoolUtils.woolMaterials[bwPlayer.getIndex()], 16));
        return true;
    }

    private static boolean pickaxe(Player p) {
        // incrementa il pick. tier di 1 e rimuove gli altri picconi
        p.getInventory().remove(Material.WOODEN_PICKAXE);
        p.getInventory().remove(Material.IRON_PICKAXE);
        p.getInventory().remove(Material.GOLDEN_PICKAXE);
        BWPlayer.get(p).incrementPickaxeTier();
        return true;
    }

    private static boolean axe(Player p) {
        p.getInventory().remove(Material.WOODEN_AXE);
        p.getInventory().remove(Material.IRON_AXE);
        p.getInventory().remove(Material.GOLDEN_AXE);
        BWPlayer.get(p).incrementAxeTier();
        return true;
    }

    private static boolean maxedTool(Player p) {
        p.sendMessage(Component.text("Questo strumento ha raggiunto il livello massimo!")
                                .color(NamedTextColor.RED));
        return false;
    }
}
