package dev.brianmartinm.fishingAdditions;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishingAdditions extends JavaPlugin {

    private final Map<String, Crate> crates = new ConcurrentHashMap<>();
    private final Map<String, ItemStack> mythicMobsCache = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadCrates();

        getServer().getPluginManager().registerEvents(new FishingListeners(this), this);

        Objects.requireNonNull(getCommand("givecrate")).setExecutor(new GiveCrateCommand(this));

        getLogger().info("Fishing Additions has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Fishing Additions has been disabled!");
    }

    private void loadCrates() {
        FileConfiguration config = getConfig();
        ConfigurationSection cratesSection = config.getConfigurationSection("crates");
        if (cratesSection == null) {
            getLogger().warning("No 'crates' section found in config.yml.");
            return;
        }

        for (String crateName : cratesSection.getKeys(false)) {
            ConfigurationSection crateSection = cratesSection.getConfigurationSection(crateName);
            if (crateSection == null) {
                getLogger().warning("Crate section '" + crateName + "' is invalid or missing.");
                continue;
            }

            double dropChance = crateSection.getDouble("drop-chance", 0.1);
            ConfigurationSection lootSection = crateSection.getConfigurationSection("loot");
            if (lootSection == null) {
                getLogger().warning("No 'loot' section found for crate '" + crateName + "'.");
                continue;
            }

            Map<ItemStack, Double> loot = new HashMap<>();
            for (String lootKey : lootSection.getKeys(false)) {
                ConfigurationSection itemSection = lootSection.getConfigurationSection(lootKey);
                if (itemSection == null) {
                    getLogger().warning("Loot item '" + lootKey + "' in crate '" + crateName + "' is invalid.");
                    continue;
                }

                Optional<ItemStack> itemOpt = parseItemStack(itemSection);
                if (itemOpt.isPresent()) {
                    double chance = itemSection.getDouble("chance", 0.1);
                    loot.put(itemOpt.get(), chance);
                } else {
                    getLogger().warning("Failed to parse item '" + lootKey + "' in crate '" + crateName + "'.");
                }
            }

            if (loot.isEmpty()) {
                getLogger().warning("Crate '" + crateName + "' has no valid loot items.");
                continue;
            }

            crates.put(crateName, new Crate(dropChance, loot));
            getLogger().info("Loaded crate: " + crateName + " with drop chance: " + dropChance);
        }
    }

    private Optional<ItemStack> parseItemStack(ConfigurationSection itemSection) {
        String type = itemSection.getString("type", "vanilla").toLowerCase(Locale.ROOT);
        int amount = itemSection.getInt("amount", 1);

        switch (type) {
            case "mythicmobs":
                String itemId = itemSection.getString("id");
                if (itemId == null || itemId.isEmpty()) {
                    getLogger().warning("Missing 'id' for MythicMobs item.");
                    return Optional.empty();
                }
                return Optional.ofNullable(getMythicMobsItem(itemId, amount));
            case "vanilla":
            default:
                String materialName = itemSection.getString("material", "STONE").toUpperCase(Locale.ROOT);
                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    getLogger().warning("Invalid material '" + materialName + "'. Defaulting to STONE.");
                    material = Material.STONE;
                }
                return Optional.of(new ItemStack(material, amount));
        }
    }

    private ItemStack getMythicMobsItem(String itemId, int amount) {
        return mythicMobsCache.computeIfAbsent(itemId, id -> {
            return MythicBukkit.inst().getItemManager().getItem(id)
                    .map(mythicItem -> BukkitAdapter.adapt(mythicItem.generateItemStack(amount)))
                    .orElse(null);
        }) != null ? mythicMobsCache.get(itemId).clone() : null;
    }

    public Map<String, Crate> getCrates() {
        return Collections.unmodifiableMap(crates);
    }
}
