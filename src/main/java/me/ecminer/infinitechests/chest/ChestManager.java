package me.ecminer.infinitechests.chest;

import me.ecminer.infinitechests.InfiniteChests;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChestManager {

    private final InfiniteChests plugin;
    private Map<String, Map<Vector, Chest>> chests = new HashMap<String, Map<Vector, Chest>>();
    private Map<String, Chest> openChest = new HashMap<String, Chest>();
    private Map<String, ChestPage> openPage = new HashMap<String, ChestPage>();

    public ChestManager(InfiniteChests plugin) {
        this.plugin = plugin;
    }

    public boolean isChest(Location loc) {
        return isChest(loc.getBlock());
    }

    public boolean isChest(Block block) {
        return block != null && block.getType() == Material.CHEST
                && (chests.containsKey(block.getWorld().getName()) && chests.get(block.getWorld().getName()).containsKey(new Vector(block.getX(), block.getY(), block.getZ())));
    }

    public Chest getChest(Block block) {
        return getChest(block.getLocation());
    }

    public Chest getChest(Location loc) {
        return chests.get(loc.getWorld().getName()).get(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public Chest placeChest(Block block) {
        return placeChest(block.getLocation());
    }

    public Chest placeChest(Location loc) {
        if (!chests.containsKey(loc.getWorld().getName())) {
            chests.put(loc.getWorld().getName(), new HashMap<Vector, Chest>());
        }
        Chest chest = new Chest(loc, plugin);
        chests.get(loc.getWorld().getName()).put(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), chest);
        return chest;
    }

    public void setOpenPage(Player player, Chest chest, ChestPage page) {
        if (chest.getInventory().isPage(page)) {
            openChest.put(player.getName(), chest);
            openPage.put(player.getName(), page);
        }
    }

    public void chestClosed(Player player) {
        openChest.remove(player.getName());
        openPage.remove(player.getName());
    }

    public boolean hasOpenedChest(Player player) {
        return openChest.containsKey(player.getName());
    }

    public Chest getOpenChest(Player player) {
        return openChest.get(player.getName());
    }

    public ChestPage getOpenPage(Player player) {
        return openPage.get(player.getName());
    }

    public Collection<Chest> getChests(World world) {
        return chests.containsKey(world.getName()) ? chests.get(world.getName()).values() : new ArrayList<Chest>();
    }

}
