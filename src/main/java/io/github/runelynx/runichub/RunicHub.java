package io.github.runelynx.runichub;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

/**
 * Hello world!
 *
 */

public class RunicHub extends JavaPlugin implements PluginMessageListener,
		Listener {

	private static Plugin instance;
	public String SERVER_NAME = "Unknown";
	public static Permission perms = null;

	public static HashMap<String, ChatColor> rankColors = new HashMap<String, ChatColor>();

	@Override
	public void onEnable() {

		instance = this;

		getServer().getPluginManager().registerEvents(this, this);

		Bukkit.getLogger().log(Level.INFO, "RunicHub plugin is loading..");

		getConfig().options().copyDefaults(true);
		saveConfig();

		SERVER_NAME = instance.getConfig().getString("ServerName");

		setupPermissions();

		this.getServer().getMessenger()
				.registerOutgoingPluginChannel(this, "BungeeCord");
		this.getServer().getMessenger()
				.registerIncomingPluginChannel(this, "BungeeCord", this);

		// getCommand("goto").setExecutor(new Commands());

		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC
					+ "...starting up RunicHub plugin...");
		}

		rankColors.put("Wanderer", ChatColor.DARK_GRAY);

		Bukkit.getLogger().log(Level.INFO, "RunicHub plugin is loaded!");

		final MySQL MySQL = new MySQL(instance, instance.getConfig().getString(
				"dbHost"), "3306", "rpgame", instance.getConfig().getString(
						"dbUser"), instance.getConfig().getString(
						"dbPassword"));
		Connection z = MySQL.openConnection();
		try {
			// clear the table
			Statement insertStmt = z.createStatement();
			getLogger().log(Level.SEVERE, "Connected to MysQL successfully. ");
			z.close();
		} catch (SQLException e) {
			getLogger().log(
					Level.SEVERE,
					"Failed MySQL connection test in onEnable: "
							+ e.getMessage());
		}
		getLogger().log(Level.INFO,
				"Debug: " + instance.getConfig().getString("debug"));

	}

	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC
					+ "...shutting down RunicHub plugin...");
		}
	}

	public static Plugin getInstance() {
		return instance;
	}

	public void onPluginMessageReceived(String channel, Player player,
			byte[] message) {

		if (!channel.equals("BungeeCord")) {
			return;
		}

		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subChannel = in.readUTF();
		short len = in.readShort();
		byte[] msgbytes = new byte[len];
		in.readFully(msgbytes);

		DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(
				msgbytes));
		try {
			String somedata = msgin.readUTF();
			short somenumber = msgin.readShort();
			/*
			 * if (subChannel.equals("Chat")) { for (Player p :
			 * Bukkit.getOnlinePlayers()) { p.sendMessage(somedata); } }
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Read the data in the same way you wrote it

	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) throws IOException {

		String staffPrefix = ChatColor.DARK_GRAY + "" +ChatColor.ITALIC + "HB ";
		if (event.getPlayer().hasPermission("rp.staff")) {
			if ((event.getPlayer().hasPermission("rp.staff.admin"))) {
				staffPrefix += ChatColor.DARK_RED + "<Admin> ";
			} else if ((event.getPlayer().hasPermission("rp.staff.director"))) {
				staffPrefix += ChatColor.DARK_RED + "<Director> ";
			} else if ((event.getPlayer().hasPermission("rp.staff.architect"))) {
				staffPrefix += ChatColor.DARK_RED + "<Architect> ";
			} else if ((event.getPlayer().hasPermission("rp.staff.enforcer"))) {
				staffPrefix += ChatColor.DARK_RED + "<Enforcer> ";
			} else if ((event.getPlayer().hasPermission("rp.staff.helper"))) {
				staffPrefix += ChatColor.DARK_RED + "<Helper> ";
			}

		} else if (event.getPlayer().hasPermission("rp.guide")) {
			staffPrefix += ChatColor.DARK_GREEN + "<Guide> ";
		}

		event.setFormat(staffPrefix
				+ RunicHub.rankColors.get(perms.getPrimaryGroup(event
						.getPlayer()))
				+ perms.getPrimaryGroup(event.getPlayer())
				+ ChatColor.GRAY
				+ " "
				+ RunicHub.rankColors.get(perms.getPrimaryGroup(event
						.getPlayer())) + event.getPlayer().getDisplayName()
				+ ChatColor.WHITE + ": %2$s");

	}

	@EventHandler
	public void processServerWarpPortals(PlayerMoveEvent event) {

		// player has entered the portal in the Hub cave
		if (event.getTo().getWorld().getName().equalsIgnoreCase("world")) {
			if ((event.getTo().getX() >= 1779 && event.getTo().getX() <= 1781)
					&& (event.getTo().getY() >= 13 && event.getTo().getY() <= 14)
					&& (event.getTo().getZ() >= 344 && event.getTo().getZ() <= 346)) {

				if (!event.getPlayer().hasPermission("ru.astrid.survival")) {
					perms.playerAdd(event.getTo().getWorld().getName(),
							event.getPlayer(), "ru.astrid.survival");
					event.getPlayer().sendMessage(
							ChatColor.ITALIC + "" + ChatColor.DARK_AQUA
									+ "You have unlocked the Survival warp!");
					event.getPlayer().teleport(
							new Location(event.getTo().getWorld(), 1786.495,
									16.0, 345.55));
					showHubAstridTravelMenu(event.getPlayer());
				} else {
					event.getPlayer().teleport(
							new Location(event.getTo().getWorld(), 1786.495,
									16.0, 345.55));
					showHubAstridTravelMenu(event.getPlayer());
				}

			}

		}

	}

	public void showHubAstridTravelMenu(Player p) {

		Inventory faithInventory = Bukkit.createInventory(null, 36,
				ChatColor.DARK_PURPLE + "Astrid Warp Menu");

		ItemMeta meta;
		Random random = new Random();
		ArrayList<String> treeLore = new ArrayList<String>();
		treeLore.add(ChatColor.YELLOW
				+ "Astrid is the great tree at the heart of");
		treeLore.add(ChatColor.YELLOW
				+ "Runic Universe. She has the power to send");
		treeLore.add(ChatColor.YELLOW
				+ "you between worlds. Just click where you want to go!");
		treeLore.add(ChatColor.YELLOW + "You must find the relevant portal");
		treeLore.add(ChatColor.YELLOW
				+ "near Astrid to unlock it here, though.");

		ItemStack tree = new ItemStack(Material.SAPLING, 1, (short) 3);
		meta = tree.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD
				+ "Astrid Warp Menu");
		meta.setLore(treeLore);
		tree.setItemMeta(meta);

		ItemStack flower1 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower1.getItemMeta();
		meta.setDisplayName("");
		flower1.setItemMeta(meta);
		ItemStack flower2 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower2.getItemMeta();
		meta.setDisplayName("");
		flower2.setItemMeta(meta);
		ItemStack flower3 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower3.getItemMeta();
		meta.setDisplayName("");
		flower3.setItemMeta(meta);
		ItemStack flower4 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower4.getItemMeta();
		meta.setDisplayName("");
		flower4.setItemMeta(meta);
		ItemStack flower5 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower5.getItemMeta();
		meta.setDisplayName("");
		flower5.setItemMeta(meta);
		ItemStack flower6 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower6.getItemMeta();
		meta.setDisplayName("");
		flower6.setItemMeta(meta);
		ItemStack flower7 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower7.getItemMeta();
		meta.setDisplayName("");
		flower7.setItemMeta(meta);
		ItemStack flower8 = new ItemStack(Material.RED_ROSE, 1,
				(short) random.nextInt(8 + 1));
		meta = flower8.getItemMeta();
		meta.setDisplayName("");
		flower8.setItemMeta(meta);

		faithInventory.setItem(0, flower1);
		faithInventory.setItem(1, flower2);
		faithInventory.setItem(2, flower3);
		faithInventory.setItem(3, flower4);
		faithInventory.setItem(4, tree);
		faithInventory.setItem(5, flower5);
		faithInventory.setItem(6, flower6);
		faithInventory.setItem(7, flower7);
		faithInventory.setItem(8, flower8);

		ItemStack skyblockIcon;
		ItemStack survivalIcon;

		if (p.hasPermission("ru.astrid.survival")) {

			survivalIcon = new ItemStack(Material.STAINED_GLASS_PANE, 1,
					(short) 5);

			meta = survivalIcon.getItemMeta();
			meta.setDisplayName("Warp to Survival");
			survivalIcon.setItemMeta(meta);
		} else {
			survivalIcon = new ItemStack(Material.STAINED_GLASS_PANE, 1,
					(short) 15);

			ArrayList<String> survivalIconLore = new ArrayList<String>();
			survivalIconLore.add(ChatColor.RED
					+ "Find the portal to the survival world");
			survivalIconLore.add(ChatColor.RED
					+ "somewhere around Astrid to unlock this warp.");

			meta = survivalIcon.getItemMeta();
			meta.setDisplayName("Survival Warp Locked");
			meta.setLore(survivalIconLore);
			survivalIcon.setItemMeta(meta);
		}

		if (p.hasPermission("ru.astrid.skyblock")) {
			skyblockIcon = new ItemStack(Material.STAINED_GLASS_PANE, 1,
					(short) 3);

			meta = skyblockIcon.getItemMeta();
			meta.setDisplayName("Warp to Skyblock");
			skyblockIcon.setItemMeta(meta);
		} else {
			skyblockIcon = new ItemStack(Material.STAINED_GLASS_PANE, 1,
					(short) 15);

			ArrayList<String> skyblockIconLore = new ArrayList<String>();
			skyblockIconLore.add(ChatColor.RED
					+ "Find the portal to the skyblock world");
			skyblockIconLore.add(ChatColor.RED
					+ "somewhere around Astrid to unlock this warp.");

			meta = skyblockIcon.getItemMeta();
			meta.setDisplayName("Skyblock Warp Locked");
			meta.setLore(skyblockIconLore);
			skyblockIcon.setItemMeta(meta);
		}

		faithInventory.setItem(20, survivalIcon);
		faithInventory.setItem(24, skyblockIcon);

		p.openInventory(faithInventory);

	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.permission.Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

}