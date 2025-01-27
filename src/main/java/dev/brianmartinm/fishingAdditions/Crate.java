package dev.brianmartinm.fishingAdditions;

import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class Crate {
    private final double dropChance;
    private final NavigableMap<Double, ItemStack> cumulativeLootMap;
    private final double totalWeight;

    public Crate(double dropChance, Map<ItemStack, Double> loot) {
        if (loot == null || loot.isEmpty()) {
            throw new IllegalArgumentException("Loot map cannot be null or empty.");
        }

        this.dropChance = dropChance;
        this.cumulativeLootMap = new TreeMap<>();
        double cumulative = 0.0;

        for (Map.Entry<ItemStack, Double> entry : loot.entrySet()) {
            double weight = entry.getValue();
            if (weight <= 0) {
                continue;
            }
            cumulative += weight;
            cumulativeLootMap.put(cumulative, entry.getKey().clone());
        }

        this.totalWeight = cumulative;

        if (this.cumulativeLootMap.isEmpty()) {
            throw new IllegalArgumentException("Loot map must contain items with positive weights.");
        }
    }

    public double getDropChance() {
        return dropChance;
    }

    public ItemStack getRandomLoot() {
        double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
        Map.Entry<Double, ItemStack> entry = cumulativeLootMap.higherEntry(randomValue);
        return entry != null ? entry.getValue().clone() : null;
    }
}
