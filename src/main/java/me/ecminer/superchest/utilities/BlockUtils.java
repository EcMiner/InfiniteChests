package me.ecminer.superchest.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class BlockUtils {

    private static final BlockFace[] chestFaces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    public static boolean isDoubleChest(Block block) {
        if (block.getType() == Material.CHEST) {
            for (BlockFace face : chestFaces) {
                Block block1 = block.getRelative(face);
                if (block1.getType() == Material.CHEST) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void dropItem(Location loc, ItemStack item) {
        World world = loc.getWorld();
        world.dropItemNaturally(loc, item);
    }

    public static void dropItem(Block block, ItemStack item) {
        dropItem(block.getLocation(), item);
    }

}
