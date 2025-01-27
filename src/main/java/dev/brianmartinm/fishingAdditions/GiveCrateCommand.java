package dev.brianmartinm.fishingAdditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class GiveCrateCommand implements CommandExecutor {
    private final FishingAdditions plugin;

    public GiveCrateCommand(FishingAdditions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customfishingloot.givecrate")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givecrate <crate> [player]");
            return true;
        }

        String crateName = args[0];
        Player targetPlayer;

        if (args.length == 1) {
            if (sender instanceof Player) {
                targetPlayer = (Player) sender;
            } else {
                sender.sendMessage(ChatColor.RED + "You must specify a player when using this command from the console.");
                return true;
            }
        } else {
            targetPlayer = Bukkit.getPlayerExact(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
                return true;
            }
        }

        Crate crate = plugin.getCrates().get(crateName);
        if (crate == null) {
            sender.sendMessage(ChatColor.RED + "Crate '" + crateName + "' not found.");
            return true;
        }

        Optional<ItemStack> lootOpt = Optional.ofNullable(crate.getRandomLoot());
        if (lootOpt.isPresent()) {
            ItemStack loot = lootOpt.get();
            Map<Integer, ItemStack> overflow = targetPlayer.getInventory().addItem(loot);

            if (!overflow.isEmpty()) {
                overflow.values().forEach(item -> targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), item));
                sender.sendMessage(ChatColor.YELLOW + "Player's inventory is full. Some items were dropped on the ground.");
            }

            sender.sendMessage(ChatColor.GREEN + "Gave " + targetPlayer.getName() + " a " + crateName + " crate!");
            targetPlayer.sendMessage(ChatColor.GREEN + "You received a " + crateName + " crate!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to generate loot for the crate.");
        }

        return true;
    }
}
