package me.ecminer.infinitechests;

import me.ecminer.infinitechests.chest.Chest;
import me.ecminer.infinitechests.chest.ChestManager;
import me.ecminer.infinitechests.chest.ChestSaver;
import me.ecminer.infinitechests.listeners.ChestListener;
import me.ecminer.infinitechests.utilities.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class InfiniteChests extends JavaPlugin {

    private ChestManager chestManager = new ChestManager(this);
    private ChestSaver chestSaver = new ChestSaver(this);

    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ChestListener(this),
                this);
    }

    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (chestManager.hasOpenedChest(player)) {
                player.closeInventory();
            }
        }
        for (World world : Bukkit.getWorlds()) {
            for (Chest chest : chestManager.getChests(world)) {
                if (chest.isEdited()) {
                    chestSaver.saveChestSync(chest);
                }
            }
        }
        chestSaver.close();
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public ChestSaver getChestSaver() {
        return chestSaver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {
        if (cmd.getName().equalsIgnoreCase("test")) {
            ItemStack item = new ItemStack(Material.STONE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Hello!");
            item.setItemMeta(meta);
            ((Player) sender).getInventory().addItem(
                    ItemUtils.deserializeItemStack(ItemUtils
                            .serializeItemStack(item)));
        }
        return false;
    }

}