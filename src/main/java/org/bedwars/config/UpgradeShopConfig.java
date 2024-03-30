package org.bedwars.config;

import org.bedwars.game.Team;
import org.bedwars.game.shop.ShopItem;
import org.bedwars.utils.BWPlayer;
import org.bedwars.utils.Items;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class UpgradeShopConfig {
    private UpgradeShopConfig() {}

    public static final List<List<ShopItem>> upgradeItems = new ArrayList<>();
    public static final List<String> upgradeNames = List.of("swordDamage", "armorResistance", "velocitaScavio",
                                                            "furnaceEfficiency");
    public static final Map<String, List<Material>> upgradeMaterials = new HashMap<>();

    public static void initialize() {
        //spada
        ArrayList<ShopItem> swordDamage = new ArrayList<>();
        swordDamage.add(new ShopItem(Items.rename(Material.WOODEN_SWORD, "Affilatezza I"), 4, Material.DIAMOND, null, UpgradeShopConfig::swordDamage, false));
        swordDamage.add(new ShopItem(Items.rename(Material.STONE_SWORD, "Affilatezza II"), 8, Material.DIAMOND, null, UpgradeShopConfig::swordDamage, false));
        swordDamage.add(new ShopItem(Items.rename(Material.IRON_SWORD, "Affilatezza III"), 32, Material.DIAMOND, null, UpgradeShopConfig::swordDamage, false));
        swordDamage.add(new ShopItem(Items.renameEnchant(Material.DIAMOND_SWORD, "La tua spada e' piu' affilata di quanto sia mai stata"), 0, Material.DIAMOND, null, (a) -> false, false)); //l'ultimo upgrade non ha costo e l'unica cosa che fa' e' indicare la fine degli upgrade

        //armatura
        ArrayList<ShopItem> armorResistance = new ArrayList<>();
        armorResistance.add(new ShopItem(Items.rename(Material.LEATHER_CHESTPLATE, "Protezione I"), 2, Material.DIAMOND, null, UpgradeShopConfig::armorResistance, false));
        armorResistance.add(new ShopItem(Items.rename(Material.CHAINMAIL_CHESTPLATE, "Protezione II"), 4, Material.DIAMOND, null, UpgradeShopConfig::armorResistance, false));
        armorResistance.add(new ShopItem(Items.rename(Material.IRON_CHESTPLATE, "Protezione III"), 8, Material.DIAMOND, null, UpgradeShopConfig::armorResistance, false));
        armorResistance.add(new ShopItem(Items.rename(Material.GOLDEN_CHESTPLATE, "Protezione IV"), 16, Material.DIAMOND, null, UpgradeShopConfig::armorResistance, false));
        armorResistance.add(new ShopItem(Items.renameEnchant(Material.DIAMOND_CHESTPLATE, "La tua armatura e' stata forgiata dal miglior fabbro del pianeta"), 0, Material.DIAMOND, null, (a) -> false, false));

        //minatore
        ArrayList<ShopItem> velocitaScavio = new ArrayList<>(); // :)
        velocitaScavio.add(new ShopItem(Items.rename(Material.WOODEN_PICKAXE, "Efficenza I"), 2, Material.DIAMOND, null, UpgradeShopConfig::velocitaScavio, false));
        velocitaScavio.add(new ShopItem(Items.rename(Material.IRON_PICKAXE, "Efficenza II"), 8, Material.DIAMOND, null, UpgradeShopConfig::velocitaScavio, false));
        velocitaScavio.add(new ShopItem(Items.renameEnchant(Material.DIAMOND_PICKAXE, "Il tuo piccone e' piu' veloce della luce"), 0, Material.DIAMOND, null, (a) -> false, false));

        //fornace
        ArrayList<ShopItem> furnaceEfficency = new ArrayList<>();
        if(LocalDate.now().getMonth() == Month.OCTOBER && LocalDate.now().getDayOfMonth() == 31) {
            furnaceEfficency.add(new ShopItem(Items.rename(Material.SOUL_CAMPFIRE, "Smelting I"), 4, Material.DIAMOND, null, UpgradeShopConfig::furnaceEfficency, false));
        } else {
            furnaceEfficency.add(new ShopItem(Items.rename(Material.CAMPFIRE, "Smelting I"), 8, Material.DIAMOND, null, UpgradeShopConfig::furnaceEfficency, false));
        }
        furnaceEfficency.add(new ShopItem(Items.rename(Material.SMOKER, "Smelting II"), 16, Material.DIAMOND, null, UpgradeShopConfig::furnaceEfficency, false));
        furnaceEfficency.add(new ShopItem(Items.rename(Material.FURNACE, "Smelting III"), 24, Material.DIAMOND, null, UpgradeShopConfig::furnaceEfficency, false));
        furnaceEfficency.add(new ShopItem(Items.renameEnchant(Material.BLAST_FURNACE, "Il fuoco della fornace e' a piena potenza"), 0, Material.DIAMOND, null, (a) -> false, false));

        upgradeItems.add(swordDamage);
        upgradeItems.add(armorResistance);
        upgradeItems.add(velocitaScavio);
        upgradeItems.add(furnaceEfficency);

        upgradeMaterials.put("swordDamage", List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                        Material.DIAMOND_SWORD));
        upgradeMaterials.put("armorResistance", List.of(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                        Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE));
        upgradeMaterials.put("velocitaScavio", List.of(Material.WOODEN_PICKAXE, Material.IRON_PICKAXE,
                        Material.DIAMOND_PICKAXE));
        Material campfire = LocalDate.now().getMonth() == Month.OCTOBER && LocalDate.now().getDayOfMonth() == 31
                            ? Material.SOUL_CAMPFIRE : Material.CAMPFIRE;
        upgradeMaterials.put("furnaceEfficiency", List.of(campfire, Material.SMOKER, Material.FURNACE,
                        Material.BLAST_FURNACE));
    }

    private static boolean swordDamage(Player p) {
        BWPlayer bwPlayer = BWPlayer.get(p);
        Team team = bwPlayer.getTeam();

        team.addUpgrade("swordDamage");
        // aggiunge sharpness <tier> a tutte le spade del team
        Arrays.stream(p.getInventory().getContents()).filter(Objects::nonNull).filter(UpgradeShopConfig::isSword)
                .forEach(i -> i.addEnchantment(Enchantment.DAMAGE_ALL, team.getUpgrade("swordDamage")));

        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    private static boolean armorResistance(Player p) {
        BWPlayer bwPlayer = BWPlayer.get(p);
        Team team = bwPlayer.getTeam();

        int tier = team.addUpgrade("armorResistance");
        // aggiunge protection <tier> all'armor del team
        Arrays.stream(p.getInventory().getArmorContents())
                .forEach(i -> i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, tier));
        Items.enchant(bwPlayer.getArmorPiece(EquipmentSlot.HEAD), Enchantment.PROTECTION_ENVIRONMENTAL, tier);
        Items.enchant(bwPlayer.getArmorPiece(EquipmentSlot.CHEST), Enchantment.PROTECTION_ENVIRONMENTAL, tier);
        Items.enchant(bwPlayer.getArmorPiece(EquipmentSlot.LEGS), Enchantment.PROTECTION_ENVIRONMENTAL, tier);
        Items.enchant(bwPlayer.getArmorPiece(EquipmentSlot.FEET), Enchantment.PROTECTION_ENVIRONMENTAL, tier);
        return true;
    }

    private static boolean velocitaScavio(Player p) {
        BWPlayer bwPlayer = BWPlayer.get(p);
        Team team = bwPlayer.getTeam();

        team.addUpgrade("velocitaScavio");
        // aggiunge effetto haste <tier> a tutto il team
        team.getOnlinePlayers().forEach(pl -> pl.addPotionEffect(
                new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION,
                        team.getUpgrade("velocitaScavio"), false, false)));
        return true;
    }

    private static boolean furnaceEfficency(Player p) {
        BWPlayer bwPlayer = BWPlayer.get(p);
        Team team = bwPlayer.getTeam();

        team.addUpgrade("furnaceEfficiency");
        // aggiorna il cooldown di iron/gold al generatore del team
        team.setGoldCooldown(goldCooldowns[team.getUpgrade("furnaceEfficiency") - 1]);
        team.setIronCooldown(ironCooldowns[team.getUpgrade("furnaceEfficiency") - 1]);

        return true;
    }

    private static boolean isSword(ItemStack i) {
        return i.getType() == Material.WOODEN_SWORD
                || i.getType() == Material.STONE_SWORD
                || i.getType() == Material.IRON_SWORD
                || i.getType() == Material.DIAMOND_SWORD;
    }

    private static final int[] ironCooldowns = { 15, 12, 10, 8 }; // .75, .6, .5, .4s

    private static final int[] goldCooldowns = { 6 * 20, 4 * 20, 3 * 20, 2 * 20 }; // 6, 4, 3, 2s
}
