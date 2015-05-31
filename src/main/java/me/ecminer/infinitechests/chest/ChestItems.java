package me.ecminer.infinitechests.chest;

import me.ecminer.infinitechests.utilities.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ChestItems {

    public static final ItemStack nextPage = ItemUtils.setItemMeta(new ItemStack(Material.EMERALD), ChatColor.GREEN + ChatColor.BOLD.toString() + "Next Page --->", ChatColor.GRAY + "Click this item to go to", ChatColor.GRAY + "the next page in the inventory.");
    public static final ItemStack previousPage = ItemUtils.setItemMeta(new ItemStack(Material.REDSTONE), ChatColor.RED + ChatColor.BOLD.toString() + "<--- Previous Page", ChatColor.GRAY + "Click this item to go to", ChatColor.GRAY + "the previous page in the inventory.");
    public static final ItemStack nothing = ItemUtils.setItemMeta(new ItemStack(Material.STAINED_GLASS_PANE), ChatColor.GOLD.toString());
    public static final ItemStack newPage = ItemUtils.setItemMeta(new ItemStack(Material.CHEST), ChatColor.GOLD + ChatColor.BOLD.toString() + "New Page", ChatColor.GRAY + "Click this item to create", ChatColor.GRAY + "a new page to store items in");
    public static final ItemStack destroyPage = ItemUtils.setItemMeta(new ItemStack(Material.TNT), ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Destroy Page", ChatColor.GRAY + "Click this item to destroy", ChatColor.GRAY + "the current page");

}
