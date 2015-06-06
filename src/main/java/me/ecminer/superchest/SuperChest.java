package me.ecminer.superchest;

import me.ecminer.superchest.chest.Chest;
import me.ecminer.superchest.chest.ChestManager;
import me.ecminer.superchest.chest.ChestSaver;
import me.ecminer.superchest.listeners.ChestListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperChest extends JavaPlugin {

    public static final String placeSuperChest = "superchest.place";
    public static final String breakSuperChest = "superchest.break";

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

}