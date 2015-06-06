package me.ecminer.superchest.listeners;

import me.ecminer.superchest.SuperChest;
import me.ecminer.superchest.chest.Chest;
import me.ecminer.superchest.chest.ChestItems;
import me.ecminer.superchest.chest.ChestPage;
import me.ecminer.superchest.utilities.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;

public class ChestListener implements Listener {

    private final BlockFace[] blockFaces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
    private final byte[] requiredData = new byte[]{2, 3, 4, 5};
    private SuperChest plugin;

    public ChestListener(SuperChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent evt) {
        if (!evt.isCancelled()) {
            Player player = evt.getPlayer();
            Sign sign = (Sign) evt.getBlock().getState();
            if (evt.getLine(0).equals("[SuperChest]") && evt.getLine(1).isEmpty() && evt.getLine(2).isEmpty() && evt.getLine(3).isEmpty()) {
                if (player.hasPermission(SuperChest.placeSuperChest)) {
                    for (int i = 0; i < blockFaces.length; i++) {
                        Block relative = evt.getBlock().getRelative(blockFaces[i].getOppositeFace());
                        if (isSuperChest(relative) || plugin.getChestManager().isChest(relative)) {
                            evt.setLine(0, "");
                            evt.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "This chest is already a SuperChest!");
                            return;
                        }
                        if (relative.getType() == Material.CHEST && sign.getData().getData() == requiredData[i]) {
                            if (!BlockUtils.isDoubleChest(relative)) {
                                plugin.getChestSaver().destroyChest(relative);
                                plugin.getChestManager().placeChest(relative);
                                Chest chest = plugin.getChestManager().getChest(relative);
                                for (ItemStack item : ((org.bukkit.block.Chest) relative.getState()).getInventory().getContents()) {
                                    if (item != null) {
                                        chest.getInventory().getPage(0).getInventory().addItem(item);
                                    }
                                }
                                ((org.bukkit.block.Chest) relative.getState()).getInventory().clear();
                                player.sendMessage(ChatColor.GREEN + "You created a SuperChest!");
                                return;
                            } else {
                                evt.setLine(0, "");
                                player.sendMessage(ChatColor.RED + "You can't create a SuperChest on a double chest! Make sure you place the sign on a single chest.");
                                return;
                            }
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the right permission to create a SuperChest!");
                }
                evt.setLine(0, "");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent evt) {
        if (!evt.isCancelled()) {
            Player player = evt.getPlayer();
            if (evt.getBlockPlaced().getType() == Material.CHEST) {
                Block block = evt.getBlockPlaced();
                for (BlockFace face : blockFaces) {
                    Block block1 = block.getRelative(face);
                    if (block1.getType() == Material.CHEST && (plugin.getChestManager().isChest(block1) || isSuperChest(block1))) {
                        evt.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You are trying to place a chest next to a SuperChest! A SuperChest can only be a single chest block!");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent evt) {
        Player player = evt.getPlayer();
        final Block block = evt.getBlock();
        if (!evt.isCancelled())
            if (block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();
                for (BlockFace face : blockFaces) {
                    final Block relative = block.getRelative(face);
                    if (plugin.getChestManager().isChest(relative) || isSuperChest(relative)) {
                        evt.setCancelled(true);
                        return;
                    }
                }
            } else if (block.getType() == Material.CHEST) {
                if ((plugin.getChestManager().isChest(block) || isSuperChest(block)) && !player.hasPermission(SuperChest.breakSuperChest)) {
                    player.sendMessage(ChatColor.RED + "You don't have the right permissions to break a SuperChest!");
                    return;
                }
                if (plugin.getChestManager().isChest(block)) {
                    Chest chest = plugin.getChestManager().getChest(block);
                    plugin.getChestManager().destroyChest(chest);
                    return;
                } else if (isSuperChest(block)) {
                    final Chest chest = plugin.getChestManager().placeChest(block);
                    plugin.getChestSaver().loadChest(chest, new Runnable() {
                        public void run() {
                            plugin.getChestManager().destroyChest(chest);
                        }
                    });
                    return;
                }
            }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if (!evt.isCancelled()) {
            if (evt.getAction() == Action.RIGHT_CLICK_BLOCK
                    && evt.getClickedBlock().getType() == Material.CHEST) {
                if (evt.getPlayer().isSneaking() && evt.getItem() != null) {
                    return;
                }
                if (plugin.getChestManager().isChest(evt.getClickedBlock())) {
                    Chest chest = plugin.getChestManager().getChest(
                            evt.getClickedBlock());
                    evt.setCancelled(true);
                    plugin.getChestManager().setOpenPage(evt.getPlayer(), chest,
                            chest.openPage(evt.getPlayer(), 0));
                } else if (isSuperChest(evt.getClickedBlock())) {
                    evt.setCancelled(true);
                    final Chest chest = plugin.getChestManager().placeChest(evt.getClickedBlock());
                    final String playerName = evt.getPlayer().getName();
                    plugin.getChestSaver().loadChest(chest, new Runnable() {
                        public void run() {
                            Player player = Bukkit.getPlayer(playerName);
                            if (player != null) {
                                plugin.getChestManager().setOpenPage(player, chest,
                                        chest.openPage(player, 0));
                            }
                        }
                    });
                }
            }
        }
    }

    private boolean isSuperChest(Block block) {
        for (int i = 0; i < blockFaces.length; i++) {
            if (block.getRelative(blockFaces[i]).getType() == Material.WALL_SIGN) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getRelative(blockFaces[i]).getState();
                if (sign.getData().getData() == requiredData[i]) {
                    return sign.getLine(0).equals("[SuperChest]") && sign.getLine(1).isEmpty() && sign.getLine(2).isEmpty() && sign.getLine(3).isEmpty();
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent evt) {
        if (!evt.isCancelled()) {
            Player player = (Player) evt.getWhoClicked();
            if (plugin.getChestManager().hasOpenedChest(player)) {
                if (plugin.getChestManager().getOpenPage(player).getInventory()
                        .equals(evt.getInventory())) {
                    Chest chest = plugin.getChestManager().getOpenChest(player);
                    ChestPage page = plugin.getChestManager().getOpenPage(
                            player);

                    int slot = evt.getRawSlot();
                    if (slot >= 0 && slot < 9) {
                        if (evt.getCurrentItem() != null) {
                            if (evt.getCurrentItem().isSimilar(
                                    ChestItems.nextPage)) {
                                ChestPage next = chest.getInventory().getNext(
                                        page);
                                if (next == page) {
                                    page.removeNextPageButton();
                                } else {
                                    player.openInventory(next.getInventory());
                                    plugin.getChestManager().setOpenPage(
                                            player, chest, next);
                                }
                            } else if (evt.getCurrentItem().isSimilar(
                                    ChestItems.previousPage)) {
                                ChestPage previous = chest.getInventory()
                                        .getPrevious(page);
                                if (previous == page) {
                                    page.removePreviousPageButton();
                                } else {
                                    player.openInventory(previous
                                            .getInventory());
                                    plugin.getChestManager().setOpenPage(
                                            player, chest, previous);
                                }
                            } else if (evt.getCurrentItem().isSimilar(ChestItems.newPage)) {
                                page.getInventory().setItem(8, ChestItems.nextPage);
                                chest.getInventory().addPage(new ChestPage("Super Chest"));
                                chest.setIsEdited(true);
                            } else if (evt.getCurrentItem().isSimilar(ChestItems.destroyPage)) {
                                chest.getInventory().destroyPage(page);
                                chest.setIsEdited(true);
                            }
                        }
                        evt.setCancelled(true);
                        return;
                    } else {
                        chest.setIsEdited(true);
                    }
                } else {
                    plugin.getChestManager().chestClosed(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        if (plugin.getChestManager().hasOpenedChest(player)) {
            plugin.getChestManager().chestClosed(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        Player player = (Player) evt.getPlayer();
        if (plugin.getChestManager().hasOpenedChest(player)) {
            plugin.getChestManager().chestClosed(player);
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent evt) {
        World world = evt.getWorld();
        for (Chest chest : plugin.getChestManager().getChests(world)) {
            plugin.getChestSaver().saveChestAsync(chest);
        }
    }

}
