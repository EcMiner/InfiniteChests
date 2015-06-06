package me.ecminer.superchest.chest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class ChestPage {

    private Inventory inventory;

    public ChestPage(String inventoryName) {
        inventory = Bukkit.createInventory(null, 6 * 9, inventoryName);

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, ChestItems.nothing);
        }
        inventory.setItem(3, ChestItems.newPage);
        inventory.setItem(5, ChestItems.destroyPage);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void addPreviousPageButton() {
        inventory.setItem(0, ChestItems.previousPage);
    }

    public void addNextPageButton() {
        inventory.setItem(8, null);
        inventory.setItem(8, ChestItems.nextPage);
    }

    public void removePreviousPageButton() {
        inventory.setItem(0, ChestItems.nothing);
    }

    public void removeNextPageButton() {
        inventory.setItem(8, ChestItems.nothing);
    }

}
