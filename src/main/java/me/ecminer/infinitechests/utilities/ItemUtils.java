package me.ecminer.infinitechests.utilities;

import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class ItemUtils {

	public static ItemStack setLore(ItemStack item, String... lore) {
		return setLore(item, Arrays.asList(lore));
	}

	public static ItemStack setLore(ItemStack item, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack setDisplayname(ItemStack item, String displayName) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack setItemMeta(ItemStack item, String displayName, String... lore) {
		return setItemMeta(item, displayName, Arrays.asList(lore));
	}

	public static ItemStack setItemMeta(ItemStack item, String displayName, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private static Gson gson = new GsonBuilder().create();
	private static JsonParser parser = new JsonParser();

	public static String serializeItemStack(ItemStack item) {
		JsonObject json = new JsonObject();
		json.addProperty("type", item.getType().name());
		json.addProperty("data", item.getData().getData());
		json.addProperty("amount", item.getAmount());
		json.addProperty("durability", item.getDurability());
		if (item.getEnchantments().size() > 0) {
			JsonObject enchantments = new JsonObject();
			for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
				Enchantment ench = entry.getKey();
				int lvl = entry.getValue();
				enchantments.addProperty(ench.getName(), lvl);
			}
			json.add("enchantments", enchantments);
		}
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			JsonObject itemmeta = new JsonObject();
			if (meta.hasDisplayName())
				itemmeta.addProperty("displayname", meta.getDisplayName());
			if (meta.hasLore())
				itemmeta.add("lore", gson.toJsonTree(meta.getLore()));

			json.add("itemmeta", itemmeta);
		}
		return json.toString();
	}

	public static ItemStack deserializeItemStack(String string) {
		try {
			JsonObject json = (JsonObject) parser.parse(string);
			Material type = Material.getMaterial(json.get("type").getAsString());
			byte data = json.get("data").getAsByte();
			int amount = json.get("amount").getAsInt();
			short durability = json.get("durability").getAsShort();

			ItemStack item = new ItemStack(type, amount, data);
			item.setDurability(durability);

			if (json.has("enchantments")) {
				JsonObject enchantments = json.getAsJsonObject("enchantments");
				for (Entry<String, JsonElement> entry : enchantments.entrySet()) {
					item.addUnsafeEnchantment(Enchantment.getByName(entry.getKey()), entry.getValue().getAsInt());
				}
			}

			if (json.has("itemmeta")) {
				JsonObject itemmeta = json.getAsJsonObject("itemmeta");
				ItemMeta meta = item.getItemMeta();

				if (itemmeta.has("displayname"))
					meta.setDisplayName(itemmeta.get("displayname").getAsString());

				if (itemmeta.has("lore")) {
					List<String> lore = new ArrayList<String>();
					Iterator<JsonElement> it = itemmeta.get("lore").getAsJsonArray().iterator();
					while (it.hasNext()) {
						lore.add(it.next().getAsString());
					}
					meta.setLore(lore);
				}
				item.setItemMeta(meta);
			}
			return item;
		} catch (Exception e) {
		}
		return null;
	}
}
