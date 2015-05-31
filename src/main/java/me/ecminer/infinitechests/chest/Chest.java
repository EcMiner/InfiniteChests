package me.ecminer.infinitechests.chest;

import me.ecminer.infinitechests.InfiniteChests;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Chest {

    protected final InfiniteChests plugin;
    private final String worldName;
    private final Vector locationVector;
    private ChestInventory inventory = new ChestInventory(this);
    private boolean isEdited = false;

    public Chest(Location location, InfiniteChests plugin) {
        this.worldName = location.getWorld().getName();
        this.locationVector = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.plugin = plugin;
    }

    public void setIsEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public String getWorldName() {
        return worldName;
    }

    public Vector getLocationVector() {
        return locationVector;
    }

    public Location getLocation() {
        return locationVector.toLocation(Bukkit.getWorld(worldName));
    }

    public ChestInventory getInventory() {
        return inventory;
    }

    public ChestPage openPage(Player player, int pageNumber) {
        ChestPage[] pages = inventory.getPages();
        pageNumber = pageNumber < pages.length && pageNumber > 0 ? pageNumber
                : 0;
        if (pages.length > 1) {
            for (ChestPage page : pages) {
                int viewers = page.getInventory().getViewers().size();
                if (viewers == 0) {
                    boolean hasItems = false;
                    for (int i = 9; i < page.getInventory().getSize(); i++) {
                        if (page.getInventory().getItem(i) != null) {
                            hasItems = true;
                            break;
                        }
                    }
                    if (!hasItems) {
                        inventory.removePage(page);
                        if (!isEdited)
                            setIsEdited(true);
                    }
                }
            }
        }
        ChestPage page = inventory.getPage(pageNumber);
        player.openInventory(page.getInventory());
        return page;
    }

    public void destroy() {
        for (ChestPage page : inventory.getPages()) {
            for (HumanEntity human : (HumanEntity[]) page
                    .getInventory()
                    .getViewers()
                    .toArray(
                            new HumanEntity[page.getInventory().getViewers()
                                    .size()])) {
                human.closeInventory();
            }
        }
    }

}