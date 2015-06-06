package me.ecminer.superchest.chest;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestInventory {

    private final Chest chest;
    private Map<Inventory, ChestPage> invLookup = new HashMap<Inventory, ChestPage>();
    private List<ChestPage> pages = new ArrayList<ChestPage>();

    public ChestInventory(Chest chest) {
        this.chest = chest;
        addPage(new ChestPage("Chest Inventory"));
    }

    public Chest getChest() {
        return chest;
    }

    public boolean isPage(ChestPage page) {
        return pages.contains(page);
    }

    public ChestPage addPage(ChestPage page) {
        invLookup.put(page.getInventory(), page);
        pages.add(page);
        if (pages.size() > 1) {
            page.addPreviousPageButton();
            getPage(getPageNumber(page) - 1).addNextPageButton();
        }
        return page;
    }

    public void removePage(ChestPage page) {
        if (pages.size() != 1) {
            invLookup.remove(page.getInventory());
            pages.remove(page);
            for (HumanEntity viewer : (HumanEntity[]) page
                    .getInventory()
                    .getViewers()
                    .toArray(
                            new HumanEntity[page.getInventory().getViewers()
                                    .size()])) {
                ChestPage next = chest.getInventory().getPage(0);
                if (next != page) {
                    viewer.openInventory(next.getInventory());
                    chest.plugin.getChestManager().setOpenPage(
                            (Player) viewer, chest, next);
                } else {
                    viewer.closeInventory();
                }
            }

            pages.get(0).removePreviousPageButton();
            pages.get(pages.size() - 1).removeNextPageButton();
            if (pages.size() > 1) {
                for (int i = 1; i < pages.size() - 1; i++) {
                    pages.get(i).addNextPageButton();
                    pages.get(i).addPreviousPageButton();
                }
            }
        }
    }

    public void destroyPage(ChestPage page) {
        if (isPage(page)) {
            ItemStack[] contents = page.getInventory().getContents();
            for (int i = 9; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null) {
                    chest.getLocation().getWorld().dropItemNaturally(chest.getLocation(), item);
                }
            }
            if (pages.size() == 1) {
                for (int i = 9; i < page.getInventory().getSize(); i++) {
                    page.getInventory().setItem(i, null);
                }
            } else {
                removePage(page);
            }
        }
    }

    public ChestPage getPage(int page) {
        return pages.get(page);
    }

    public int getPageNumber(ChestPage page) {
        return pages.indexOf(page);
    }

    public ChestPage getPrevious(ChestPage page) {
        int pageNumber = getPageNumber(page);
        return pageNumber > 0 ? getPage(pageNumber - 1) : page;
    }

    public ChestPage getNext(ChestPage page) {
        int pageNumber = getPageNumber(page);
        return pageNumber + 1 < pages.size() ? getPage(pageNumber + 1) : page;
    }

    public boolean isFirstPage(ChestPage page) {
        return getPageNumber(page) == 0;
    }

    public boolean isLastPage(ChestPage page) {
        return getPageNumber(page) == pages.size() - 1;
    }

    public ChestPage[] getPages() {
        return (ChestPage[]) pages.toArray(new ChestPage[pages.size()]);
    }

    public int getAmountOfChests() {
        return pages.size();
    }

}
