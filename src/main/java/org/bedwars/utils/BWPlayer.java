package org.bedwars.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bedwars.Bedwars;
import org.bedwars.config.ShopConfig;
import org.bedwars.game.Arena;
import org.bedwars.game.Team;
import org.bedwars.stats.ChatFlair;
import org.bedwars.stats.GameStats;
import org.bedwars.stats.achievements.AchievementData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Estensione di {@link OfflinePlayer} - aggiunge funzionalità relative alla modaltà Bedwars non presenti in Bukkit.
 */
public class BWPlayer {
    private OfflinePlayer player;
    private GUI gui;
    private Material shopSection; // sezione aperta
    private Arena arena;
    private Arena rejoinArena;
    private Team team;
    private BWScoreboard scoreboard;
    private ChatFlair flair;
    private final GameStats stats;
    private final AchievementData achievements;
    private int availableFlairs;
    private String quickShop;
    private boolean editingQuickShop;
    private int newQuickShopIndex;
    private static Map<String, BWPlayer> allPlayers;

    private OfflinePlayer combatLogPlayer; // con chi sta combattendo?
    private int combatLogTime; // quanto manca alla fine?
    private BukkitTask combatTask;
    private static final int COMBAT_LOG_DEFAULT = 10; // quando dura il combat?

    private final List<ItemStack> armorItems; // l'armatura
    private int pickaxeTier; // 0 - no, 1 - wooden, 2 - iron, 3 - gold, 4 - diamond
    private int axeTier;

    // achievement
    private Location fireballHit;

    private BWPlayer(Player p) {
        player = p;
        arena = null;
        rejoinArena = null;
        team = null;
        gui = null;
        stats = new GameStats(this);
        achievements = new AchievementData(p);
        scoreboard = null;
        editingQuickShop = false;
        newQuickShopIndex = -99;
        armorItems = new ArrayList<>();
        combatLogPlayer = null;
        combatLogTime = 0;
        combatTask = null;
        pickaxeTier = 0;
        axeTier = 0;
        fireballHit = null;
        giveInitialItems();
        loadFromQuery();
    }

    /**
     * Restituisce un'oggetto che rappresenta la testa del {@code Player} dato.
     * @param p il {@code Player} di cui prenderne la testa
     * @return la testa di {@code p}
     */
    public static ItemStack getPlayerHead(Player p) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skukllMeta = (SkullMeta) skull.getItemMeta();
        skukllMeta.setOwningPlayer(p);
        skull.setItemMeta(skukllMeta);

        return skull;
    }

    private void giveInitialItems() {
        armorItems.clear();
        armorItems.add(new ItemStack(Material.LEATHER_HELMET));
        armorItems.add(new ItemStack(Material.LEATHER_CHESTPLATE));
        armorItems.add(new ItemStack(Material.LEATHER_LEGGINGS));
        armorItems.add(new ItemStack(Material.LEATHER_BOOTS));
        armorItems.get(0).addEnchantment(Enchantment.WATER_WORKER, 1); // aqua affinity
    }

    private void loadFromQuery() {
        try {
            PreparedStatement psSelectA = Bedwars.database.prepareStatement("SELECT * FROM achievements WHERE name = ?");
            psSelectA.setString(1, player.getName());
            getAchievements().loadFromQuery(psSelectA.executeQuery());

            PreparedStatement psSelectQS = Bedwars.database.prepareStatement("SELECT quickShop FROM players WHERE name = ?");
            psSelectQS.setString(1, player.getName());
            quickShop = psSelectQS.executeQuery().getString("quickShop");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Aggiunge gli oggetti iniziali all'inventario del {@code Player}, se online.
     */
    @SuppressWarnings("DataFlowIssue") // player.getName() non può restituire null se player.isOnline() è true
    public void addInitialItems() {
        if (!player.isOnline()) return;

        PlayerInventory inventory = Bukkit.getPlayer(player.getName()).getInventory();

        inventory.addItem(Items.enchant(Material.WOODEN_SWORD, Enchantment.DAMAGE_ALL, team.getUpgrade("swordDamage")));
        inventory.setItem(EquipmentSlot.HEAD, getArmorPiece(EquipmentSlot.HEAD));
        inventory.setItem(EquipmentSlot.CHEST, getArmorPiece(EquipmentSlot.CHEST));
        inventory.setItem(EquipmentSlot.LEGS, getArmorPiece(EquipmentSlot.LEGS));
        inventory.setItem(EquipmentSlot.FEET, getArmorPiece(EquipmentSlot.FEET));
        if (pickaxeTier != 0) player.getPlayer().getInventory().addItem(ShopConfig.pickaxes.get(pickaxeTier - 1).getItem());
        if (axeTier != 0) player.getPlayer().getInventory().addItem(ShopConfig.axes.get(axeTier - 1).getItem());
    }

    /**
     * Restituisce la GUI aperta.
     * @return la GUI aperta
     */
    public GUI getGui() {
        return gui;
    }

    /**
     * Imposta la GUI aperta.
     * @param gui la nuova GUI
     */
    public void setGui(GUI gui) {
        this.gui = gui;
    }

    /**
     * Restituisce l'indice del team a cui appartiene il {@code Player}.
     * @return l'indice del team
     */
    public int getIndex() {
        return team.getIndex();
    }

    /**
     * Restituisce il {@code BWPlayer} corrispondente al {@code OfflinePlayer} dato.
     * @param p l'{@code OfflinePlayer}
     * @return un {@code BWPlayer} con lo stesso nome di {@code p}
     */
    public static BWPlayer get(OfflinePlayer p) {
        return allPlayers.get(p.getName());
    }

    /**
     * Crea un {@code BWPlayer}, se non esiste già, lo collega al {@code Player} dato e aggiorna le sue statistiche.
     * @param p il {@code Player} da collegare al {@code BWPlayer}
     */
    public static void addBWPlayer(Player p) {
        if (allPlayers.containsKey(p.getName())) { // già loggato?
            BWPlayer bwPlayer = BWPlayer.get(p);
            bwPlayer.refresh(p);
            bwPlayer.getGameStats().refresh(p);
            bwPlayer.getAchievements().refresh(p);
        } else {
            allPlayers.putIfAbsent(p.getName(), new BWPlayer(p)); // altrimenti aggiungi ai player
        }

        int flairId, unlockedFlairs;
        try {
            PreparedStatement psFlair = Bedwars.database.prepareStatement("SELECT flair FROM players WHERE name = ?");
            psFlair.setString(1, p.getName());
            flairId = psFlair.executeQuery().getInt("flair");

            PreparedStatement psUnlockedFlairs = Bedwars.database.prepareStatement("SELECT unlockedFlairs FROM players WHERE name = ?");
            psUnlockedFlairs.setString(1, p.getName());
            unlockedFlairs = psUnlockedFlairs.executeQuery().getInt("unlockedFlairs");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        BWPlayer.get(p).setFlair(ChatFlair.getFlair(flairId));
        BWPlayer.get(p).setAvailableFlairs(unlockedFlairs);
    }

    /**
     * Restituisce il giocatore associato al {@code BWPlayer}.
     * @return il giocatore associato
     */
    public OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Restituisce l'{@code Arena} in cui sta giocando il {@code BWPlayer}, se non è alla lobby.
     * @return l'{@code Arena} in cui è presente, altrimenti {@code null}
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * Imposta l'{@code Arena} in cui sta giocando il {@code BWPlayer}.
     * @param a l'{@code Arena}
     */
    public void setArena(Arena a) {
        arena = a;
        if (a != null) a.addPlayer(this.getPlayer());
    }

    /**
     * Restituisce il {@code Team} in cui è presente il {@code BWPlayer}.
     * @return il {@code Team}
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Aggiunge il giocatore a un {@code Team}, e lo rimuove da quello precedente, se esiste.
     * @param team il {@code Team} nuovo
     */
    public void setTeam(Team team) {
        if (this.team != null) {
            this.team.removePlayer(player);
        }
        if (team != null) {
            team.addPlayer(player);
        }

        this.team = team;
    }

    /**
     * Imposta la {@code BWScoreboard}.
     * @param scoreboard la nuova {@code BWScoreboard}
     */
    public void setScoreboard(BWScoreboard scoreboard) {
        if (this.scoreboard != null) {
            this.scoreboard.disable();
        }
        this.scoreboard = scoreboard;
        this.scoreboard.enable();
    }

    /**
     * Restituisce l'{@code ItemStack} presente nell'{@code EquipmentSlot} dato.
     * @param slot lo slot
     * @return l'{@code ItemStack} in {@code slot}
     */
    public ItemStack getArmorPiece(EquipmentSlot slot) {
        int index = switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> throw new IllegalArgumentException();
        };

        return armorItems.get(index);
    }

    /**
     * Imposta l'{@code ItemStack} nell'{@code EquipmentSlot} dato.
     * @param slot lo slot
     * @param i l'{@code ItemStack} da cambiare
     */
    public void setArmorPiece(EquipmentSlot slot, ItemStack i) {
        int index = switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> throw new IllegalArgumentException();
        };

        armorItems.set(index, i);
    }

    public static void initialize() {
        allPlayers = new HashMap<>();
    }

    /**
     * Restituisce l'{@code OfflinePlayer} con cui sta combattendo il {@code BWPlayer}.
     * @return l'{@code OfflinePlayer} in combattimento, se esiste, altrimenti {@code null}.
     */
    public OfflinePlayer getCombatLogPlayer() {
        return combatLogPlayer;
    }

    /**
     * Aggiorna l'{@code OfflinePlayer} con cui sta combattendo il {@code BWPlayer}.
     * @param combatLogPlayer il nuovo {@code OfflinePlayer}
     */
    public void setCombatLogPlayer(Player combatLogPlayer) {
        this.combatLogPlayer = combatLogPlayer;
        if (combatLogPlayer == null) return;

        combatLogTime = BWPlayer.COMBAT_LOG_DEFAULT;

        if (combatTask != null && !combatTask.isCancelled()) combatTask.cancel();

        combatTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (combatLogTime > 0) {
                    combatLogTime--;
                } else {
                    setCombatLogPlayer(null);
                    cancel();
                }
            }
        }.runTaskTimer(Bedwars.Plugin, 0, 20);
    }

    /**
     * Restituisce la sezione del negozio aperta.
     * @return la sezione aperta, se esiste, altrimenti {@code null}.
     */
    public Material getShopSection() {
        return shopSection;
    }

    /**
     * Aggiorna la sezione del negozio aperta
     * @param shopSection la nuova sezione
     */
    public void setShopSection(Material shopSection) {
        this.shopSection = shopSection;
    }

    /**
     * Restituisce l'{@code Arena} in cui il {@code BWPlayer} verrà portato se esce e rientra dal server.
     * @return l'{@code Arena} in cui verrà portato, se esiste, altrimenti {@code null}
     */
    public Arena getRejoinArena() {
        return rejoinArena;
    }

    /**
     * Imposta l'{@code Arena} in cui il {@code BWPlayer} verrà portato se esce e rientra dal server.
     * @param a la nuova {@code Arena}
     */
    public void setRejoinArena(Arena a) {
        rejoinArena = a;
    }

    /**
     * Restituisce le statistiche del giocatore.
     * @return le statistiche
     */
    public GameStats getGameStats() {
        return stats;
    }

    /**
     * Restituisce una {@code Map} contenente tutti i {@code BWPlayer} collegati ai nomi dei {@code Player}.
     * @return la {@code Map} con i {@code BWPlayer} e i loro nomi
     */
    public static Map<String, BWPlayer> getAllPlayers() {
        return allPlayers;
    }

    /**
     * Restituisce il grado del piccone del giocatore.
     * @return il grado del piccone, o 0 se il giocatore non ne ha uno.
     */
    public int getPickaxeTier() {
        return pickaxeTier;
    }

    /**
     * Incrementa di 1 il grado del piccone del giocatore, a meno che non sia già al massimo.
     */
    public void incrementPickaxeTier() {
        if (pickaxeTier < 4) pickaxeTier++;
    }

    /**
     * Restituisce il grado dell'ascia del giocatore.
     * @return il grado dell'ascia, o 0 se il giocatore non ne ha una.
     */
    public int getAxeTier() {
        return axeTier;
    }

    /**
     * Incrementa di 1 il grado dell'ascia del giocatore, a meno che non sia già al massimo.
     */
    public void incrementAxeTier() {
        if (axeTier < 4) axeTier++;
    }

    /**
     * Decrementa di 1 il grado del piccone e dell'ascia del giocatore, a meno che non siano già al grado minimo.
     */
    public void decrementTiers() {
        if (pickaxeTier > 1) pickaxeTier--;
        if (axeTier > 1) axeTier--;
    }

    /**
     * Reimposta ai valori di default i gradi del piccone e dell'ascia del giocatore.
     */
    public void resetTiers() {
        pickaxeTier = 0;
        axeTier = 0;
    }

    /**
     * Restituisce gli achievements del giocatore.
     * @return gli achievements del giocatore
     * @see AchievementData
     */
    public AchievementData getAchievements() {
        return achievements;
    }

    /**
     * Restituisce l'ultima {@code Location} dove il giocatore è stato colpito da una {@link Fireball}.
     * @return l'ultima {@code Location} dove il giocatore è stato colpito da una {@code Fireball}, o {@code null} se non è stato ancora colpito.
     */
    public Location getFireballHit() {
        return fireballHit;
    }

    /**
     * Imposta l'ultima {@code Location} dove il giocatore è stato colpito da una {@link Fireball}.
     * @param fireballHit la posizione
     */
    public void setFireballHit(Location fireballHit) {
        this.fireballHit = fireballHit;
    }

    /**
     * Restituisce la flair attualmente in uso dal giocatore
     * @return la flair in uso
     * @see ChatFlair
     */
    public ChatFlair getFlair() {
        return flair;
    }

    public void setFlair(ChatFlair flair) {
        this.flair = flair;
    }

    /**
     * Restituisce un int contenente tutte le flairs disponibili al giocatore.
     * @return le flairs disponibili
     */
    public int getAvailableFlairs() {
        return availableFlairs;
    }

    /**
     * Controlla se il giocatore possiede una flair con l'ID dato.
     * @param id l'ID da controllare
     * @return true se il giocatore possiede la flair con l'ID dato, altrimenti false
     */
    public boolean hasFlair(int id) {
        return (availableFlairs >>> id & 1) == 1;
    }

    /**
     * Aggiorna tutte le flairs disponibili al giocatore.
     * @param availableFlairs le nuove flairs disponibili
     */
    public void setAvailableFlairs(int availableFlairs) {
        this.availableFlairs = availableFlairs;
    }

    /**
     * Aggiunge una flair al giocatore.
     * @param id l'ID della flair
     */
    public void addFlair(int id) {
        this.availableFlairs |= 1 << id;
    }

    /**
     * Mostra una GUI contenente tutte le flairs disponibili al giocatore.
     */
    public void showFlairGUI() {
        int rows = (int) Math.ceil(ChatFlair.getFlairCount() / 9.0);
        Inventory inv = Bukkit.createInventory(null, rows * 9, Component.text("Seleziona flair"));

        for (int i = 0; i < ChatFlair.getFlairCount(); i++) {
            ChatFlair flair = ChatFlair.getFlair(i);

            ItemStack item;
            if (hasFlair(i)) {
                item = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(flair.getGUIView());
                meta.lore(List.of(flair.getDescription()));
                item.setItemMeta(meta);
            } else {
                item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(flair.getChatView());
                meta.lore(List.of(Component.text("Non hai sbloccato questo flair!").color(NamedTextColor.RED)));
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }
        ((Player) player).openInventory(inv);
    }

    private void refresh(Player p) {
        player = Bukkit.getOfflinePlayer(p.getName());
    }

    /**
     * Restituisce una stringa contenente la disposizione attuale del quickshop.
     * @return gli oggetti nel quickshop, ciascuno col proprio id, memorizzati in una stringa
     */
    public String getQuickShop() {
        return quickShop;
    }

    /**
     * Restituisce gli oggetti nel quickshop sotto forma di lista.
     * @return la lista contenente gli ID degli oggetti nel quickshop
     */
    public List<Integer> getQuickShopAsList() {
        return Arrays.stream(quickShop.split(" ")).map(Integer::parseInt).toList();
    }

    /**
     * Imposta gli oggetti nel quickshop.
     * @param list la lista contenente gli ID degli oggetti
     */
    public void setQuickShop(List<Integer> list) {
        quickShop = StringUtils.join(list, ' ');
    }

    // TODO

    public boolean isEditingQuickShop() {
        return editingQuickShop;
    }

    public void setEditingQuickShop(boolean editingQuickShop) {
        this.editingQuickShop = editingQuickShop;
    }

    public int getNewQuickShopIndex() {
        return newQuickShopIndex;
    }

    public void setNewQuickShopIndex(int newQuickShopIndex) {
        this.newQuickShopIndex = newQuickShopIndex;
    }
}
