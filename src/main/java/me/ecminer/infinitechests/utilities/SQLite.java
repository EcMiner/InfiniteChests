package me.ecminer.infinitechests.utilities;

import me.ecminer.infinitechests.InfiniteChests;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;

public class SQLite {

    private InfiniteChests plugin;

    private Connection c = null;

    public SQLite(InfiniteChests core, File file) {
        this.plugin = core;
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(final String query) {
        try {
            Statement st = c.createStatement();
            st.execute(query);
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateQuery(final String query) {
        new BukkitRunnable() {
            public void run() {
                try {
                    Statement st = c.createStatement();
                    st.executeUpdate(query);
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public ResultSet executeQuery(final String query) {
        try {
            Statement st = c.createStatement();
            ResultSet set = st.executeQuery(query);
            st.close();
            return set;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeConnection() {
        try {
            c.close();
            c = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
