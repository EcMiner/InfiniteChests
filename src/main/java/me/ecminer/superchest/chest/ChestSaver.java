package me.ecminer.superchest.chest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ecminer.superchest.SuperChest;
import me.ecminer.superchest.utilities.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ChestSaver {

    private final SuperChest plugin;
    private Connection c = null;
    private JsonParser parser = new JsonParser();

    public ChestSaver(SuperChest plugin) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");
            File file = new File(plugin.getDataFolder(), "chests.sqlite");
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            this.c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            Statement st = c.createStatement();
            st.execute("create table if not exists chests(world varchar(255), location varchar(255), data LONGTEXT);");
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveChestAsync(final Chest chest) {
        chest.setIsEdited(false);
        runAsync(new Runnable() {
            public void run() {
                String worldName = chest.getWorldName();
                Vector location = chest.getLocationVector();
                StringBuilder invData = new StringBuilder();
                for (ChestPage page : chest.getInventory().getPages()) {
                    invData.append("[");
                    int j = 0;
                    for (int i = 9; i < page.getInventory().getSize(); i++) {
                        if (page.getInventory().getItem(i) != null) {
                            if (j != 0) {
                                invData.append(";;");
                            }
                            JsonObject json = ItemUtils.serializeItemStack(page.getInventory().getItem(i));
                            json.addProperty("slot", i);
                            invData.append(json.toString());
                            ++j;
                        }
                    }
                    invData.append("];");
                }
                try {
                    Statement st = c.createStatement();
                    ResultSet set = st.executeQuery("select world from chests where world='" + worldName + "' and location='" + location.toString() + "';");
                    Statement st1 = c.createStatement();
                    if (set.next()) {
                        st1.execute("update chests set data='" + invData.toString() + "' where world='" + worldName + "' and location='" + location.toString() + "';");
                    } else {
                        st1.execute("insert into chests(world, location, data) values('" + worldName + "', '" + location.toString() + "', '" + invData.toString() + "');");
                    }
                    st1.close();
                    st.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void saveChestSync(Chest chest) {
        chest.setIsEdited(false);
        String worldName = chest.getWorldName();
        Vector location = chest.getLocationVector();
        StringBuilder invData = new StringBuilder();
        for (ChestPage page : chest.getInventory().getPages()) {
            invData.append("[");
            int j = 0;
            for (int i = 9; i < page.getInventory().getSize(); i++) {
                if (page.getInventory().getItem(i) != null) {
                    if (j != 0) {
                        invData.append(";;");
                    }
                    JsonObject json = ItemUtils.serializeItemStack(page.getInventory().getItem(i));
                    json.addProperty("slot", i);
                    invData.append(json.toString());
                    ++j;
                }
            }
            invData.append("];");
        }
        try {
            Statement st = c.createStatement();
            ResultSet set = st.executeQuery("select world from chests where world='" + worldName + "' and location='" + location.toString() + "';");
            Statement st1 = c.createStatement();
            if (set.next()) {
                st1.execute("update chests set data='" + invData.toString() + "' where world='" + worldName + "' and location='" + location.toString() + "';");
            } else {
                st1.execute("insert into chests(world, location, data) values('" + worldName + "', '" + location.toString() + "', '" + invData.toString() + "');");
            }
            st1.close();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadChest(final Chest chest) {
        loadChest(chest, null);
    }

    public void loadChest(final Chest chest, final Runnable onFinish) {
        runAsync(new Runnable() {
            public void run() {
                String worldName = chest.getWorldName();
                Vector location = chest.getLocationVector();
                try {
                    Statement st = c.createStatement();
                    ResultSet set = st.executeQuery("select data from chests where world='" + worldName + "' and location='" + location.toString() + "';");
                    if (set.next()) {
                        String data = set.getString("data");
                        int i = 0;
                        for (String page : data.split("];")) {
                            if (page.length() > 1) {
                                ChestPage cPage = null;
                                if (i != 0) {
                                    cPage = chest.getInventory().addPage(new ChestPage("Chest"));
                                } else {
                                    cPage = chest.getInventory().getPage(0);
                                }
                                String substring = page.substring(1);
                                if (substring.contains(";;")) {
                                    for (String itemSerialized : substring.split(";;")) {
                                        JsonObject json = (JsonObject) parser.parse(itemSerialized);
                                        int slot = json.get("slot").getAsInt();
                                        ItemStack item = ItemUtils.deserializeItemStack(itemSerialized);
                                        cPage.getInventory().setItem(slot, item);
                                    }
                                } else {
                                    JsonObject json = (JsonObject) parser.parse(substring);
                                    int slot = json.get("slot").getAsInt();
                                    ItemStack item = ItemUtils.deserializeItemStack(substring);
                                    cPage.getInventory().setItem(slot, item);
                                }
                                i++;
                            }
                        }
                    }
                    st.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (onFinish != null) {
                    try {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, onFinish);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void destroyChest(Chest chest) {
        destroyChest(chest.getWorldName(), chest.getLocationVector());
    }

    public void destroyChest(Location loc) {
        destroyChest(loc.getBlock());
    }

    public void destroyChest(Block block) {
        destroyChest(block.getWorld().getName(), new Vector(block.getX(), block.getY(), block.getZ()));
    }

    public void destroyChest(final String worldName, final Vector location) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    Statement st = c.createStatement();
                    st.execute("DELETE FROM chests WHERE world='" + worldName + "' and location='" + location.toString() + "'");
                    st.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public void close() {
        try {
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
