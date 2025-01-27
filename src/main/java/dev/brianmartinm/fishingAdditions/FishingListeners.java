package dev.brianmartinm.fishingAdditions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FishingListeners implements Listener {
    private final FishingAdditions plugin;

    public FishingListeners(FishingAdditions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        boolean crateGiven = false;

        for (Map.Entry<String, Crate> entry : plugin.getCrates().entrySet()) {
            Crate crate = entry.getValue();
            double roll = ThreadLocalRandom.current().nextDouble();

            if (roll < crate.getDropChance()) {
                ItemStack loot = crate.getRandomLoot();
                if (loot != null) {
                    event.setCancelled(true);

                    Map<Integer, ItemStack> overflow = player.getInventory().addItem(loot);
                    if (!overflow.isEmpty()) {
                        overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                        player.sendMessage(ChatColor.YELLOW + "Your inventory was full! Some items were dropped on the ground.");
                    }

                    player.sendMessage(ChatColor.GREEN + "You caught a " + entry.getKey() + " crate!");
                    crateGiven = true;
                    break;
                }
            }
        }
    }
}
