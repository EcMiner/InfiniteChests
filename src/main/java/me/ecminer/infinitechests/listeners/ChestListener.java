package me.ecminer.infinitechests.listeners;

import me.ecminer.infinitechests.InfiniteChests;
import me.ecminer.infinitechests.chest.Chest;
import me.ecminer.infinitechests.chest.ChestItems;
import me.ecminer.infinitechests.chest.ChestPage;
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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldSaveEvent;

public class ChestListener implements Listener {

    private final BlockFace[] blockFaces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
    private final byte[] requiredData = new byte[]{2, 3, 4, 5};
    private InfiniteChests plugin;

    public ChestListener(InfiniteChests plugin) {
        this.plugin = plugin;
    }

    /**
     * @EventHandler(priority = EventPriority.MONITOR)
     * public void onBlockPlace(BlockPlaceEvent evt) {
     * if (!evt.isCancelled()) {
     * if (evt.getBlockPlaced().getType() == Material.CHEST) {
     * plugin.getChestManager().placeChest(evt.getBlockPlaced());
     * evt.getPlayer()
     * .sendMessage(ChatColor.GREEN + "Placed a chest!");
     * }
     * }
     * }*
     */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent evt) {
        if (!evt.isCancelled()) {
            Sign sign = (Sign) evt.getBlock().getState();
            if (evt.getLine(0).equals("[SuperChest]") && evt.getLine(1).isEmpty() && evt.getLine(2).isEmpty() && evt.getLine(3).isEmpty()) {
                for (int i = 0; i < blockFaces.length; i++) {
                    Block relative = evt.getBlock().getRelative(blockFaces[i].getOppositeFace());
                    System.out.println(relative.getType());
                    if (isSuperChest(relative) || plugin.getChestManager().isChest(relative)) {
                        evt.setCancelled(true);
                        evt.getPlayer().sendMessage(ChatColor.RED + "This chest is already a SuperChest!");
                        return;
                    }
                    if (relative.getType() == Material.CHEST && sign.getData().getData() == requiredData[i]) {
                        plugin.getChestManager().placeChest(evt.getBlock());
                        evt.getPlayer().sendMessage(ChatColor.GREEN + "Placed!");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if (!evt.isCancelled()) {
            if (evt.getAction() == Action.RIGHT_CLICK_BLOCK
                    && evt.getClickedBlock().getType() == Material.CHEST) {
                if (plugin.getChestManager().isChest(evt.getClickedBlock())) {
                    Chest chest = plugin.getChestManager().getChest(
                            evt.getClickedBlock());
                    evt.setCancelled(true);
                    plugin.getChestManager().setOpenPage(evt.getPlayer(), chest,
                            chest.openPage(evt.getPlayer(), 0));
                } else if (isSuperChest(evt.getClickedBlock())) {
                    System.out.println("Is Super Chest!");
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
                                chest.getInventory().addPage(new ChestPage("Chest"));
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
    public void onWorldSave(WorldSaveEvent evt) {
        World world = evt.getWorld();
        for (Chest chest : plugin.getChestManager().getChests(world)) {
            plugin.getChestSaver().saveChestAsync(chest);
        }
    }

}
