package me.ecminer.infinitechests.chest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ecminer.infinitechests.InfiniteChests;
import me.ecminer.infinitechests.utilities.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ChestSaver {

    private final InfiniteChests plugin;
    private Connection c = null;
    private Gson gson = new GsonBuilder().create();
    private JsonParser parser = new JsonParser();

    public ChestSaver(InfiniteChests plugin) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");
            this.c = DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "chests.sqlite").getAbsolutePath());
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
                System.out.println(chest.getInventory().getAmountOfChests());
                for (ChestPage page : chest.getInventory().getPages()) {
                    invData.append("[");
                    for (int i = 9; i < page.getInventory().getSize(); i++) {
                        if (page.getInventory().getItem(i) != null) {
                            JsonObject json = new JsonObject();
                            json.addProperty("slot", i);
                            json.addProperty("item", ItemUtils.serializeItemStack(page.getInventory().getItem(i)));
                            invData.append(json.toString());
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
            for (int i = 9; i < page.getInventory().getSize(); i++) {
                if (page.getInventory().getItem(i) != null) {
                    JsonObject json = new JsonObject();
                    json.addProperty("slot", i);
                    json.addProperty("item", ItemUtils.serializeItemStack(page.getInventory().getItem(i)));
                    invData.append(json.toString());
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
                            System.out.println("Page: " + page);
                            ChestPage cPage = null;
                            if (i != 0) {
                                cPage = chest.getInventory().addPage(new ChestPage("Chest"));
                            } else {
                                cPage = chest.getInventory().getPage(0);
                            }
                            System.out.println("Page null? " + (cPage == null));
                            System.out.println("Page replaced: " + page.substring(1));
                            for (String itemSerialized : page.substring(1).split(";")) {
                                JsonObject json = (JsonObject) parser.parse(itemSerialized);
                                int slot = json.get("slot").getAsInt();
                                ItemStack item = ItemUtils.deserializeItemStack(json.get("item").getAsString());
                                cPage.getInventory().setItem(slot, item);
                            }
                            i++;
                        }
                    }
                    st.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (onFinish != null) {
                    try {
                        onFinish.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
