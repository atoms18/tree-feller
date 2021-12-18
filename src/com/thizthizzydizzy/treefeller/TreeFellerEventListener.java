package com.thizthizzydizzy.treefeller;

import com.atoms18.actionbar.ActionBarComponent;
import com.atoms18.actionbar.ActionBarManager;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;

import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TreeFellerEventListener implements Listener {
	private final TreeFeller plugin;

	private HashMap<UUID, BukkitTask> TFSTATUSTask = new HashMap<>();

	public TreeFellerEventListener(TreeFeller plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {

		Player p = e.getPlayer();
		BukkitTask bt = TFSTATUSTask.get(p.getUniqueId());
		if (bt != null && !bt.isCancelled()) {
			bt.cancel();
			try {
				ActionBarManager.getPlayerQueueByUUID(p.getUniqueId()).setOverrideAvailable(true);
			} catch (NullPointerException exe) {

			}
		}

		int i = e.getNewSlot();
		ItemStack item = e.getPlayer().getInventory().getItem(i); // this get the item in the selected slot (i)
		if (item != null) {// check if there is an item and if the item has a lore
			for (Tool tool : TreeFeller.tools) {
				if (tool.material == item.getType()) {
					sendPlayerMessage(p);
					break;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BukkitTask bt = TFSTATUSTask.get(player.getUniqueId());
		if ((bt == null || (bt != null && bt.isCancelled())) && !player.isSneaking()) {
			PlayerInventory inv = player.getInventory();

			boolean isRightItem = false;
			ItemStack item;

			if ((item = inv.getItemInMainHand()) != null) {
				for (Tool tool : TreeFeller.tools) {
					if (item.getType() == tool.material) {
						isRightItem = true;
					}
				}
			}
			if (!isRightItem && (item = inv.getItemInOffHand()) != null) {
				for (Tool tool : TreeFeller.tools) {
					if (item.getType() == tool.material) {
						isRightItem = true;
					}
				}
			}
			if (isRightItem) {
				sendPlayerMessage(player);
			}
		}
	}

	private void sendPlayerMessage(Player p) {
		try {
			ActionBarManager.getPlayerQueueByUUID(p.getUniqueId()).setOverrideAvailable(false);
		} catch (NullPointerException exe) {

		}
		TFSTATUSTask.put(p.getUniqueId(), new BukkitRunnable() {
			public void run() {
				String statusText;
				if (p.isSneaking()) {
					statusText = ChatColor.AQUA + "เปิด";
				} else {
					statusText = ChatColor.RED + "ปิด";

				}
				TreeFeller.sendActionBarTreeMessage(p, statusText, "TF-STATUS", 1);
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (event.getEntity().getType() == EntityType.DROPPED_ITEM && damager.getType() == EntityType.FALLING_BLOCK
				&& damager.getScoreboardTags().contains("tree_feller"))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		plugin.fellTree(event);
	}

	@EventHandler
	public void onBlockLand(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			FallingTreeBlock falling = null;
			for (FallingTreeBlock b : plugin.fallingBlocks) {
				if (b.entity.getUniqueId().equals(event.getEntity().getUniqueId())) {
					falling = b;
					break;
				}
			}
			if (falling != null)
				falling.land(plugin, event);
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		TreeFeller.detectingTrees.remove(event.getPlayer());
		BukkitTask bt = TFSTATUSTask.get(event.getPlayer().getUniqueId());

		if (bt != null && !bt.isCancelled()) {
			bt.cancel();
			try {
				ActionBarManager.getPlayerQueueByUUID(event.getPlayer().getUniqueId()).setOverrideAvailable(true);
			} catch (NullPointerException exe) {

			}
		}

	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (TreeFeller.detectingTrees.containsKey(event.getPlayer())) {
				event.setCancelled(true);
				Tree tree = TreeFeller.detect(event.getClickedBlock(), event.getPlayer());
				if (tree != null) {
					TreeFeller.trees.add(tree);
					new MenuTreeConfiguration(TreeFeller.detectingTrees.get(event.getPlayer()), plugin,
							event.getPlayer(), tree).openInventory();
				} else {
					TreeFeller.detectingTrees.get(event.getPlayer()).openInventory();
				}
				TreeFeller.detectingTrees.remove(event.getPlayer());
			}
		}
	}
}