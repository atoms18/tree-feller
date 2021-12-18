package com.thizthizzydizzy.treefeller;

import com.atoms18.actionbar.ActionBarComponent;
import com.atoms18.actionbar.ActionBarManager;
//import com.thizthizzydizzy.treefeller.compat.TestResult;
//import com.thizthizzydizzy.treefeller.compat.TreeFellerCompat;
import com.thizthizzydizzy.treefeller.menu.MenuTreesConfiguration;

import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TreeFeller extends JavaPlugin {
	public static ArrayList<Tool> tools = new ArrayList<>();
	public static ArrayList<Tree> trees = new ArrayList<>();
	public static ArrayList<Effect> effects = new ArrayList<>();
	public static HashMap<UUID, Cooldown> cooldowns = new HashMap<>();
	public static HashMap<Player, MenuTreesConfiguration> detectingTrees = new HashMap<>();
	public HashSet<UUID> disabledPlayers = new HashSet<>();
	public ArrayList<FallingTreeBlock> fallingBlocks = new ArrayList<>();
	public ArrayList<Sapling> saplings = new ArrayList<>();
	boolean debug = false;
	private static final HashMap<Material, int[]> exp = new HashMap<>();
	static {// Perhaps this should be in the config rather than hard-coded...
		exp.put(Material.COAL_ORE, new int[] { 0, 2 });
		exp.put(Material.DIAMOND_ORE, new int[] { 3, 7 });
		exp.put(Material.EMERALD_ORE, new int[] { 3, 7 });
		exp.put(Material.LAPIS_ORE, new int[] { 2, 5 });
		exp.put(Material.NETHER_QUARTZ_ORE, new int[] { 2, 5 });
		exp.put(Material.REDSTONE_ORE, new int[] { 1, 5 });
		exp.put(Material.SPAWNER, new int[] { 15, 43 });
	}

	public void fellTree(BlockBreakEvent event) {
		fellTree(event.getBlock(), event.getPlayer());
//			event.setCancelled(true);
	}

	public boolean fellTree(Block block, Player player) {
		return fellTree(block, player.getInventory().getItemInMainHand(), player);
	}

	public boolean fellTree(Block block, ItemStack axe, Player player) {
		return fellTree(block, player, axe);
	}

	public boolean fellTree(Block block, Player player, ItemStack axe) {
		return !fellTree(block, player, axe, true).isEmpty();
	}

	/**
	 * Fells a tree
	 * 
	 * @param block     the block that was broken
	 * @param player    the player whose permissions are to be used. CAN BE NULL
	 * @param axe       the tool used to break the block
	 * @param dropItems weather or not to drop items
	 * @return the items that would have been dropped. <b>This is not the actual
	 *         dropped items, but possible dropped items</b> Returns null if the
	 *         tree was not felled.
	 */
	private int diggingTime = 0;
	private int total;
	private int durabilityCost;

	public static void sendActionBarTreeMessage(Player player, String statusText) {
		sendActionBarTreeMessage(player, statusText, "TF-OTHER-STATUS");
	}

	public static void sendActionBarTreeMessage(Player player, String statusText, String eventName) {
		sendActionBarTreeMessage(player, statusText, eventName, 40);
	}

	public static void sendActionBarTreeMessage(Player player, String statusText, String eventName, int duration) {
//		System.out.println(statusText.contains(ChatColor.));
		ActionBarManager
				.addActionBarComponentToQueue(
						new ActionBarComponent(player, new TextComponent(ChatColor.BOLD + "[ระบบตัดต้นไม้ "
								+ ChatColor.BOLD + "" + statusText + ChatColor.RESET + ChatColor.BOLD + "]")),
						duration, eventName);
	}

	public ArrayList<ItemStack> fellTree(Block block, Player player, ItemStack axe, boolean dropItems) {
		final int durability = axe.getType().getMaxDurability() - axe.getDurability();

		DetectedTree detectedTree = detectTree(block, player, axe, (testTree) -> {
			Tool tool = testTree.tool;
			Tree tree = testTree.tree;
//			int durability = axe.getType().getMaxDurability() - axe.getDurability();
//			if (Option.STACKED_TOOLS.get(testTree.tool, testTree.tree)) {
//				durability += axe.getType().getMaxDurability() * (axe.getAmount() - 1);
//			}
			total = getTotal(testTree.trunk);
//			durabilityCost = 0;
//			if (Option.DAMAGE_MULT.globalValue != null)
//				durabilityCost *= Option.DAMAGE_MULT.globalValue;
//			if (Option.DAMAGE_MULT.treeValues.containsKey(tree))
//				durabilityCost *= Option.DAMAGE_MULT.treeValues.get(tree);
//			if (Option.DAMAGE_MULT.toolValues.containsKey(tool))
//				durabilityCost *= Option.DAMAGE_MULT.toolValues.get(tool);
//			if (Option.RESPECT_UNBREAKING.get(tool, tree)) {
//			for (int i = 0; i < total; i++) {
//				if (ThreadLocalRandom.current().nextDouble() <= 1.
//						/ (axe.getEnchantmentLevel(Enchantment.DURABILITY) + 1.)) {
//					durabilityCost++;
//				}
//			}
//				durabilityCost /= (axe.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
//				if (durabilityCost < 1)
//					durabilityCost++;
//			}
//			if (axe.getType().getMaxDurability() == 0)
//				durabilityCost = 0;// there is no durability
//			if (player != null && player.getGameMode() == GameMode.CREATIVE)
//				durabilityCost = 0;// Don't cost durability

//			if (Option.PREVENT_BREAKAGE.get(tool, tree)) {
			if(player.getGameMode() == GameMode.CREATIVE) {
				return true;
			}
			if (total == durability) {
				sendActionBarTreeMessage(player, ChatColor.LIGHT_PURPLE + "เครื่องมือใกล้พัง", "TF-BROKEN-STATUS");
				debug(player, false, false, "prevent-breakage");
				return null;
			}
//				debug(player, false, true, "prevent-breakage-success");
//			}
			if (total > durability) {
				sendActionBarTreeMessage(player, ChatColor.LIGHT_PURPLE + "เครื่องมือใกล้พัง", "TF-BROKEN-STATUS");
//				if (!Option.ALLOW_PARTIAL.get(tool, tree)) {
				debug(player, false, false, "durability-low", durability, total);
				return null;
//				}
//				debug(player, "partial", false);
//				durabilityCost = total = durability;
			}
			return true;
		});
		ArrayList<ItemStack> droppedItems = new ArrayList<>();
		if (detectedTree == null)
			return droppedItems;
		Tool tool = detectedTree.tool;
		Tree tree = detectedTree.tree;
//		if (Option.STACKED_TOOLS.get(detectedTree.tool, detectedTree.tree)) {
//			durability += axe.getType().getMaxDurability() * (axe.getAmount() - 1);
//		}
		total = getTotal(detectedTree.trunk);

		debug(player, true, true, "success");
//		TreeFellerCompat.fellTree(block, player, axe, tool, tree, detectedTree.trunk);
//		if (Option.LEAVE_STUMP.get(tool, tree)) {
		for (int i : detectedTree.trunk.keySet()) {
			for (Iterator<Block> it = detectedTree.trunk.get(i).iterator(); it.hasNext();) {
				Block b = it.next();
				if (b.getY() < block.getY())
					it.remove();
			}
		}
//		}
		int lower = block.getY();
		for (Block b : toList(detectedTree.trunk)) {
			if (b.getY() < lower)
				lower = b.getY();
		}
		int lowest = lower;
//		if (player != null && player.getGameMode() != GameMode.CREATIVE) {
//			if (axe.getType().getMaxDurability() > 0) {
//				if (Option.STACKED_TOOLS.get(tool, tree)) {
//					int amt = axe.getAmount();
//					while (durabilityCost > axe.getType().getMaxDurability() - axe.getDurability()) {
//						amt--;
//						durabilityCost -= axe.getType().getMaxDurability();
//					}
//					axe.setAmount(amt);
//				}
//				axe.setDurability((short) (axe.getDurability() + durabilityCost));
//				if (durability <= durabilityCost)
//					axe.setAmount(0);
//			}
//		}
		// now the blocks
		final int t = total;
		long seed = new Random().nextLong();
		ArrayList<Integer> distances = new ArrayList<>(detectedTree.trunk.keySet());
		Collections.sort(distances);
		saplings.addAll(detectedTree.saplings);

		double defaultDigSpeedMultipiler = Tool.toolSpeed.getOrDefault(axe.getType(), 1);

		int effEnchant = axe.getEnchantmentLevel(Enchantment.DIG_SPEED);
		if (effEnchant > 0) {
			defaultDigSpeedMultipiler += 1. + Math.pow(effEnchant, 2.);
		}

//		if (true) {
		int delay = 0;
//		int ttl = t;
		int tTL = t;
		int Ttl = 0;
		for (int i : distances) {
			if (i == 0)
				continue;
			int TTL = tTL - Ttl;

			double digSpeedMultipiler = defaultDigSpeedMultipiler;

			if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
				digSpeedMultipiler *= (100.
						+ (20. * (player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier() + 1.))) * 0.01;
			}

			if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
				double slowPercent;
				switch (player.getPotionEffect(PotionEffectType.SLOW_DIGGING).getAmplifier()) {
				case 0:
					slowPercent = 0.7;
					break;
				case 1:
					slowPercent = 0.91;

					break;
				case 2:
					slowPercent = 0.9973;
					break;
				default:
					slowPercent = 0.99919;
					break;
				}
				digSpeedMultipiler *= 1. - slowPercent;
			}

//			for (Block b : detectedTree.trunk.get(i)) {
//				if (ttl <= 0)
//					break;
//					for (Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b,
//							Option.LEAF_BREAK_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree),
//							Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree),
//							Option.FORCE_DISTANCE_CHECK.get(tool, tree)))) {
//						droppedItems.addAll(getDrops(leaf, tool, tree, axe, new int[1]));
//					}
//				droppedItems.addAll(getDrops(b, tool, tree, axe, new int[1]));
//				ttl--;
//			}
			int tTl = TTL;
			for (Block b : detectedTree.trunk.get(i)) {
				if (tTl <= 0)
					break;

				diggingTime = player.getGameMode() != GameMode.CREATIVE ? (int) Math.ceil(3. / digSpeedMultipiler * 20.)
						: 1;
				delay += diggingTime;// Option.ANIM_DELAY.get(tool, tree);
				droppedItems.addAll(getDrops(b, tool, tree, axe, new int[1]));
				new BukkitRunnable() {
					@SuppressWarnings("deprecation")
					@Override
					public void run() {

						if (player != null && player.getGameMode() != GameMode.CREATIVE) {
							if (axe.getType().getMaxDurability() > 0) {
								durabilityCost = 0;

								if (ThreadLocalRandom.current().nextDouble() <= 1.
										/ (axe.getEnchantmentLevel(Enchantment.DURABILITY) + 1.)) {
									durabilityCost++;
								}

								axe.setDurability((short) (axe.getDurability() + durabilityCost));
								if (durability <= durabilityCost)
									axe.setAmount(0);
							}
						}

						for (Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b,
								Option.LEAF_BREAK_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree),
								Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree),
								Option.FORCE_DISTANCE_CHECK.get(tool, tree)))) {
							breakBlock(detectedTree, dropItems, tree, tool, axe, leaf, block, lowest, player, seed);
							player.setExhaustion(player.getExhaustion() + 0.01f);
						}
						sendActionBarTreeMessage(player, ChatColor.GREEN + "กำลังทำงาน...", "TF-SUCCESS-STATUS",
								(int) (diggingTime * 2));
						player.setExhaustion(player.getExhaustion() + 0.01f);
						breakBlock(detectedTree, dropItems, tree, tool, axe, b, block, lowest, player, seed);
//					processNaturalFalls();
					}
				}.runTaskLater(this, delay);
				tTl--;
			}

			Ttl += detectedTree.trunk.get(i).size();
		}

		sendActionBarTreeMessage(player, ChatColor.GREEN + "กำลังทำงาน...", "TF-SUCCESS-STATUS",
				(int) (diggingTime * 2));
//			Integer maxSaplings = Option.MAX_SAPLINGS.get(tool, tree);
//			if (maxSaplings != null && maxSaplings >= 1) {
//				if (Option.SPAWN_SAPLINGS.get(tool, tree) == 2) {
//					new BukkitRunnable() {
//						@Override
//						public void run() {
//							for (Sapling s : detectedTree.saplings) {
//								s.place(null);
//							}
//						}
//					}.runTaskLater(this, delay + 1);
//				}
//			}
//		}
//		else {
//			for (int i : distances) {
//				for (Block b : detectedTree.trunk.get(i)) {
//					if (total <= 0)
//						break;
//					for (Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b,
//							Option.LEAF_BREAK_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree),
//							Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree),
//							Option.FORCE_DISTANCE_CHECK.get(tool, tree)))) {
//						breakBlock(detectedTree, dropItems, tree, tool, axe, leaf, block, lowest, player, seed);
//					}
//					breakBlock(detectedTree, dropItems, tree, tool, axe, b, block, lowest, player, seed);
//					total--;
//				}
//			}
//			if (Option.SPAWN_SAPLINGS.get(tool, tree) == 2) {
//				for (Sapling s : detectedTree.saplings) {
//					s.place(null);
//				}
//			}
//			processNaturalFalls();
//		}
		if (player != null) {
			long time = System.currentTimeMillis();
			Cooldown cooldown = cooldowns.get(player.getUniqueId());
			if (cooldown == null)
				cooldown = new Cooldown();
			cooldown.globalCooldown = time;
			cooldown.treeCooldowns.put(tree, time);
			cooldown.toolCooldowns.put(tool, time);
			cooldowns.put(player.getUniqueId(), cooldown);
		}
//		for (Effect e : Option.EFFECTS.get(tool, tree)) {
//			if (e.location == Effect.EffectLocation.TOOL) {
//				if (new Random().nextDouble() < e.chance)
//					e.play(block);
//			}
//		}
		createSaplingHandler();
		return droppedItems;
	}

	/**
	 * Detect any type of tree from a source block
	 * 
	 * @param block  the block to search for the tree from
	 * @param player The player to use while detecting the tree; can be null
	 * @param axe    The ItemStack to use while detecting the tree; can be null.
	 *               (Used to find the Tool; tool durability for partial trees is
	 *               not considered)
	 * @return a DetectedTree object, or null if no tree was found
	 */
	public DetectedTree detectTree(Block block, Player player, ItemStack axe) {
		return detectTree(block, player, axe, (t) -> {
			return true;
		});
	}

	/**
	 * Detect any type of tree from a source block
	 * 
	 * @param block     the block to search for the tree from
	 * @param player    The player to use while detecting the tree; can be null
	 * @param axe       The ItemStack to use while detecting the tree; can be null.
	 *                  (Used to find the Tool; tool durability for partial trees is
	 *                  not considered)
	 * @param checkFunc A function to perform any additional checks to ensure a tree
	 *                  is valid. returns true if the tree is valid, null if the
	 *                  tool is invalid, or false if the tree is invalid.
	 * @return a DetectedTree object, or null if no tree was found
	 */
	public DetectedTree detectTree(Block block, Player player, ItemStack axe,
			Function<DetectedTree, Boolean> checkFunc) {
		if (player != null && player.getGameMode() == GameMode.SPECTATOR)
			return null;
		debugIndent = 0;
		Material material = block.getType();
		TREE: for (Tree tree : trees) {
			if (!tree.trunk.contains(material))
				continue;
			TOOL: for (Tool tool : tools) {
				if (player != null && disabledPlayers.contains(player.getUniqueId())) {
					debug(player, true, false, "toggle");
					return null;
				}
				debug(player, "checking", false, trees.indexOf(tree), tools.indexOf(tool));
				if (axe != null && tool.material != Material.AIR && axe.getType() != tool.material)
					continue;
				for (Option o : Option.options) {
					DebugResult result = o.check(this, tool, tree, block, player, axe);
					if (result == null)
						continue;

					debug(player, false, result);
					if (!result.isSuccess())
						continue TOOL;
				}
				int scanDistance = Option.SCAN_DISTANCE.get(tool, tree);
				Integer maxBlocks = Option.MAX_LOGS.get(tool, tree);
				HashMap<Integer, ArrayList<Block>> blocks = getBlocks(tree.trunk, block, scanDistance,
						maxBlocks == null ? Integer.MAX_VALUE : (maxBlocks * 2), true, false, false);// TODO what if the
																										// trunk is made
																										// of leaves?
				for (Option o : Option.options) {
					DebugResult result = o.checkTrunk(this, tool, tree, blocks, block);
					if (result == null)
						continue;

					debug(player, false, result);
					if (!result.isSuccess()) {
						Message message = Message.getMessage(result.message + result.type.suffix);
						String text = message.getDebugText();
						for (int i = 0; i < result.args.length; i++) {
							text = text.replace("{" + i + "}", result.args[i].toString());
						}
						sendActionBarTreeMessage(player, ChatColor.LIGHT_PURPLE + text);
						continue TOOL;
					}
				}
				int minY = block.getY();
				for (int i : blocks.keySet()) {
					for (Block b : blocks.get(i)) {
						minY = Math.min(minY, b.getY());
					}
				}
				ArrayList<Integer> distances = new ArrayList<>(blocks.keySet());
				Collections.sort(distances);
				int leaves = 0;
				HashMap<Integer, ArrayList<Block>> allLeaves = new HashMap<>();
				FOR: for (int i : distances) {
					for (Block b : blocks.get(i)) {
						HashMap<Integer, ArrayList<Block>> someLeaves = getBlocksWithLeafCheck(tree.trunk, tree.leaves,
								b, Option.LEAF_DETECT_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree),
								Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree),
								Option.FORCE_DISTANCE_CHECK.get(tool, tree));
						leaves += toList(someLeaves).size();
						for (int in : someLeaves.keySet()) {
							if (allLeaves.containsKey(in)) {
								allLeaves.get(in).addAll(someLeaves.get(in));
							} else {
								allLeaves.put(in, someLeaves.get(in));
							}
						}
					}
				}
				ArrayList<Block> everything = new ArrayList<>();
				everything.addAll(toList(blocks));
				everything.addAll(toList(allLeaves));
//				TestResult res = TreeFellerCompat.test(player, everything);
//				if (res != null) {
//					debug(player, false, false, "protected", res.plugin, res.block.getX(), res.block.getY(),
//							res.block.getZ());
//					continue TREE;
//				}
				for (Option o : Option.options) {
					DebugResult result = o.checkTree(this, tool, tree, blocks, leaves);
					if (result == null)
						continue;
					debug(player, false, result);
					if (!result.isSuccess()) {
						Message message = Message.getMessage(result.message + result.type.suffix);
						String text = message.getDebugText();
						for (int i = 0; i < result.args.length; i++) {
							text = text.replace("{" + i + "}", result.args[i].toString());
						}
						sendActionBarTreeMessage(player, ChatColor.LIGHT_PURPLE + text);
						continue TOOL;
					}
				}
				DetectedTree detected = new DetectedTree(tool, tree, blocks, allLeaves);
				Boolean result = checkFunc.apply(detected);
				if (result == null)
					continue;
				if (Objects.equals(result, false))
					continue TREE;
				debug(player, true, true, "success");
//				if (Option.LEAVE_STUMP.get(tool, tree)) {
				for (int i : blocks.keySet()) {
					for (Iterator<Block> it = blocks.get(i).iterator(); it.hasNext();) {
						Block b = it.next();
						if (b.getY() < block.getY()) {
							detected.stump.add(b);
						}
					}
				}
//				}
//				HashMap<Block, Integer> possibleSaplings = new HashMap<>();
//				if (Option.SAPLING.get(tool, tree) != null && Option.REPLANT_SAPLINGS.get(tool, tree)) {
//					ArrayList<Block> logs = toList(blocks);
//					for (Block log : logs) {
//						if (Option.GRASS.get(tool, tree).contains(log.getRelative(0, -1, 0).getType())) {
//							possibleSaplings.put(log, -1);
//						}
//					}
//					for (Block b : possibleSaplings.keySet()) {
//						int above = -1;
//						Block b1 = b;
//						while (tree.trunk.contains(b1.getType())) {
//							above++;
//							b1 = b1.getRelative(0, 1, 0);
//						}
//						possibleSaplings.put(b, above);
//					}
//					Integer maxSaplings = Option.MAX_SAPLINGS.get(tool, tree);
//					if (maxSaplings != null) {
//						while (possibleSaplings.size() > maxSaplings) {
//							ArrayList<Integer> ints = new ArrayList<>(possibleSaplings.values());
//							Collections.sort(ints);
//							int i = ints.get(0);
//							for (Block b : possibleSaplings.keySet()) {
//								if (possibleSaplings.get(b) == i) {
//									possibleSaplings.remove(b);
//									break;
//								}
//							}
//						}
//					}
//					for (Block b : possibleSaplings.keySet()) {
//						detected.addSapling(b, Option.SAPLING.get(tool, tree));
//					}
//				}
				return detected;
			}
		}
		return null;
	}

	private static HashMap<Integer, ArrayList<Block>> getBlocks(ArrayList<Material> materialTypes, Block startingBlock,
			int maxDistance, int maxBlocks, boolean diagonal, boolean playerLeaves, boolean ignoreLeafData) {
		// layer zero
		HashMap<Integer, ArrayList<Block>> results = new HashMap<>();
		int total = 0;
		ArrayList<Block> zero = new ArrayList<>();
		if (materialTypes.contains(startingBlock.getType())) {
			zero.add(startingBlock);
		}
		results.put(0, zero);
		total += zero.size();
		// all the other layers
		for (int i = 0; i < maxDistance; i++) {
			ArrayList<Block> layer = new ArrayList<>();
			ArrayList<Block> lastLayer = new ArrayList<>(results.get(i));
			if (i == 0 && lastLayer.isEmpty()) {
				lastLayer.add(startingBlock);
			}
			for (Block block : lastLayer) {
				if (diagonal) {
					for (int x = -1; x <= 1; x++) {
						for (int y = -1; y <= 1; y++) {
							for (int z = -1; z <= 1; z++) {
								if (x == 0 && y == 0 && z == 0)
									continue;// same block
								Block newBlock = block.getRelative(x, y, z);
								if (!materialTypes.contains(newBlock.getType())) {
									continue;
								}
								if (lastLayer.contains(newBlock))
									continue;// if the new block is on the same layer, ignore
								if (i > 0 && results.get(i - 1).contains(newBlock))
									continue;// if the new block is on the previous layer, ignore
								if (layer.contains(newBlock))
									continue;// if the new block is on the next layer, but already processed, ignore
								if (newBlock.getBlockData() instanceof Leaves) {
									Leaves newLeaf = (Leaves) newBlock.getBlockData();
									if (!playerLeaves && newLeaf.isPersistent())
										continue;
									if (!ignoreLeafData) {
										if (block.getBlockData() instanceof Leaves) {
											Leaves oldLeaf = (Leaves) block.getBlockData();
											if (newLeaf.getDistance() <= oldLeaf.getDistance()) {
												continue;
											}
										}
									}
								}
								layer.add(newBlock);
							}
						}
					}
				} else {
					for (int j = 0; j < 6; j++) {
						int x = 0, y = 0, z = 0;
						switch (j) {
						case 0:
							x = -1;
							break;
						case 1:
							x = 1;
							break;
						case 2:
							y = -1;
							break;
						case 3:
							y = 1;
							break;
						case 4:
							z = -1;
							break;
						case 5:
							z = 1;
							break;
						default:
							throw new IllegalArgumentException("How did this happen?");
						}
						Block newBlock = block.getRelative(x, y, z);
						if (!materialTypes.contains(newBlock.getType())) {
							continue;
						}
						if (lastLayer.contains(newBlock))
							continue;// if the new block is on the same layer, ignore
						if (i > 0 && results.get(i - 1).contains(newBlock))
							continue;// if the new block is on the previous layer, ignore
						if (layer.contains(newBlock))
							continue;// if the new block is on the next layer, but already processed, ignore
						if (newBlock.getState().getBlockData() instanceof Leaves) {
							Leaves newLeaf = (Leaves) newBlock.getBlockData();
							if (!playerLeaves && newLeaf.isPersistent())
								continue;
							if (!ignoreLeafData) {
								if (block.getBlockData() instanceof Leaves) {
									Leaves oldLeaf = (Leaves) block.getBlockData();
									if (newLeaf.getDistance() <= oldLeaf.getDistance()) {
										continue;
									}
								}
							}
						}
						layer.add(newBlock);
					}
				}
			}
			if (layer.isEmpty())
				break;
			results.put(i + 1, layer);
			total += layer.size();
			if (total > maxBlocks)
				return results;
		}
		return results;
	}

	private HashMap<Integer, ArrayList<Block>> getBlocksWithLeafCheck(ArrayList<Material> trunk,
			ArrayList<Material> leaves, Block startingBlock, int maxDistance, boolean diagonal, boolean playerLeaves,
			boolean ignoreLeafData, boolean forceDistanceCheck) {
		HashMap<Integer, ArrayList<Block>> blocks = getBlocks(leaves, startingBlock, maxDistance, Integer.MAX_VALUE,
				diagonal, playerLeaves, ignoreLeafData);
		if (forceDistanceCheck)
			leafCheck(blocks, trunk, leaves, diagonal, playerLeaves, ignoreLeafData);
		return blocks;
	}

	private static int getTotal(HashMap<Integer, ArrayList<Block>> blocks) {
		int total = 0;
		for (int i : blocks.keySet()) {
			total += blocks.get(i).size();
		}
		return total;
	}

	private ArrayList<Block> toList(HashMap<Integer, ArrayList<Block>> blocks) {
		ArrayList<Block> list = new ArrayList<>();
		for (int i : blocks.keySet()) {
			list.addAll(blocks.get(i));
		}
		return list;
	}

	private int distance(Block from, ArrayList<Material> to, ArrayList<Material> materialTypes, int max,
			boolean diagonal, boolean playerLeaves) {
		materialTypes.add(from.getType());
		materialTypes.addAll(to);
		for (int d = 0; d < max; d++) {
			for (Block b : toList(getBlocks(materialTypes, from, d, Integer.MAX_VALUE, diagonal, playerLeaves, true))) {
				if (to.contains(b.getType()))
					return d;
			}
		}
		return max;
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		Logger logger = getLogger();
//		TreeFellerCompat.init();
		// <editor-fold defaultstate="collapsed" desc="Register Events">
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new TreeFellerEventListener(this), this);
//</editor-fold>
		// <editor-fold defaultstate="collapsed" desc="Register Config">
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
//</editor-fold>
		pm.addPermission(new Permission("treefeller.reload"));
		pm.addPermission(new Permission("treefeller.debug"));
		getCommand("treefeller").setExecutor(new CommandTreeFeller(this));
		logger.log(Level.INFO, "{0} has been enabled! (Version {1}) by ThizThizzyDizzy",
				new Object[] { pdfFile.getName(), pdfFile.getVersion() });
		reload();
	}

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		Logger logger = getLogger();
		logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy",
				new Object[] { pdfFile.getName(), pdfFile.getVersion() });
	}

	public static Particle getParticle(String string) {
		Particle p = null;
		try {
			p = Particle.valueOf(string.toUpperCase().replace(" ", "-").replace("-", "_"));
		} catch (IllegalArgumentException ex) {
		}
		if (p != null)
			return p;
		switch (string.toLowerCase().replace("_", " ").replace("-", " ")) {
		case "barrier":
			return Particle.BARRIER;
		case "block":
//                return Particle.BLOCK_CRACK;
			return Particle.BLOCK_DUST;
		case "enchanted hit":
			return Particle.CRIT_MAGIC;
		case "dripping lava":
			return Particle.DRIP_LAVA;
		case "dripping water":
			return Particle.DRIP_WATER;
		case "enchant":
			return Particle.ENCHANTMENT_TABLE;
		case "explosion emitter":
			return Particle.EXPLOSION_HUGE;
		case "explode":
			return Particle.EXPLOSION_LARGE;
		case "poof":
			return Particle.EXPLOSION_NORMAL;
		case "firework":
			return Particle.FIREWORKS_SPARK;
		case "item":
			return Particle.ITEM_CRACK;
		case "elder guardian":
			return Particle.MOB_APPEARANCE;
		case "dust":
			return Particle.REDSTONE;
		case "item slime":
			return Particle.SLIME;
		case "large smoke":
			return Particle.SMOKE_LARGE;
		case "smoke":
			return Particle.SMOKE_NORMAL;
		case "item snowball":
			return Particle.SNOWBALL;
		case "effect":
			return Particle.SPELL;
		case "instant effect":
			return Particle.SPELL_INSTANT;
		case "entity effect":
			return Particle.SPELL_MOB;
		case "mob spell ambient":
		case "ambient entity effect":
			return Particle.SPELL_MOB_AMBIENT;
		case "witch":
			return Particle.SPELL_WITCH;
		case "underwater":
			return Particle.SUSPENDED;
		case "totem of undying":
			return Particle.TOTEM;
		case "mycelium":
			return Particle.TOWN_AURA;
		case "angry villager":
			return Particle.VILLAGER_ANGRY;
		case "happy villager":
			return Particle.VILLAGER_HAPPY;
		case "bubble":
			return Particle.WATER_BUBBLE;
		case "rain":
			return Particle.WATER_DROP;
		case "splash":
			return Particle.WATER_SPLASH;
		case "fishing":
			return Particle.WATER_WAKE;
		default:
			return null;
		}
	}

	public void reload() {
		Logger logger = getLogger();
		trees.clear();
		tools.clear();
		effects.clear();
		saplings.clear();
		if (saplingHandler != null)
			saplingHandler.cancel();
		saplingHandler = null;
		fallingBlocks.clear();
		cooldowns.clear();
		// <editor-fold defaultstate="collapsed" desc="Effects">
		ArrayList<Object> effects = null;
		try {
			effects = new ArrayList<>(getConfig().getList("effects"));
		} catch (NullPointerException ex) {
			if (getConfig().get("effects") != null)
				logger.log(Level.WARNING, "Failed to load effects!");
		}
		if (effects != null) {
			for (Object o : effects) {
				if (o instanceof LinkedHashMap) {
					LinkedHashMap map = (LinkedHashMap) o;
					if (!map.containsKey("name") || !(map.get("name") instanceof String)) {
						logger.log(Level.WARNING, "Cannot find effect name! Skipping...");
						continue;
					}
					String name = (String) map.get("name");
					String typ = (String) map.get("type");
					Effect.EffectType type = Effect.EffectType.valueOf(typ.toUpperCase().trim());
					if (type == null) {
						logger.log(Level.WARNING, "Invalid effect type: {0}! Skipping...", typ);
						continue;
					}
					String loc = (String) map.get("location");
					Effect.EffectLocation location = Effect.EffectLocation.valueOf(loc.toUpperCase().trim());
					if (location == null) {
						logger.log(Level.WARNING, "Invalid effect location: {0}! Skipping...", loc);
						continue;
					}
					double chance = 1;
					if (map.containsKey("chance")) {
						chance = ((Number) map.get("chance")).doubleValue();
					}
					Effect effect = type.loadEffect(name, location, chance, map);
					if (Option.STARTUP_LOGS.isTrue())
						effect.print(logger);
					this.effects.add(effect);
				} else if (o instanceof String) {
					Material m = Material.matchMaterial((String) o);
					if (m == null) {
						logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", o);
					}
					Tool tool = new Tool(m);
					if (Option.STARTUP_LOGS.isTrue())
						tool.print(logger);
					this.tools.add(tool);
				} else {
					logger.log(Level.INFO, "Unknown tool declaration: {0} | {1}",
							new Object[] { o.getClass().getName(), o.toString() });
				}
			}
		}
//</editor-fold>
		for (Option option : Option.options) {
			if (option.global) {
				option.setValue(option.loadFromConfig(getConfig()));
			}
		}
		for (Message message : Message.messages) {
			message.load(getConfig());
		}
		if (Option.STARTUP_LOGS.isTrue()) {
			logger.log(Level.INFO, "Server version: {0}",
					Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1));
			logger.log(Level.INFO, "Loaded global values:");
			for (Option option : Option.options) {
				Object value = option.getValue();
				if (value != null) {
					logger.log(Level.INFO, "- {0}: {1}", new Object[] { option.name, option.makeReadable(value) });
				}
			}
		}
		// <editor-fold defaultstate="collapsed" desc="Trees">
		ArrayList<Object> trees = new ArrayList<>(getConfig().getList("trees"));
		for (Object o : trees) {
			if (o instanceof ArrayList) {
				ArrayList<Material> trunk = new ArrayList<>();
				ArrayList<Material> leaves = new ArrayList<>();
				if (((ArrayList) o).get(0) instanceof String) {
					Material t = Material.matchMaterial((String) ((ArrayList) o).get(0));
					if (t != null)
						trunk.add(t);
				} else {
					for (Object obj : (ArrayList) ((ArrayList) o).get(0)) {
						if (obj instanceof String) {
							Material t = Material.matchMaterial((String) obj);
							if (t != null)
								trunk.add(t);
						}
					}
				}
				if (((ArrayList) o).get(1) instanceof String) {
					Material l = Material.matchMaterial((String) ((ArrayList) o).get(1));
					if (l != null)
						leaves.add(l);
				} else {
					for (Object obj : (ArrayList) ((ArrayList) o).get(1)) {
						if (obj instanceof String) {
							Material l = Material.matchMaterial((String) obj);
							if (l != null)
								leaves.add(l);
						}
					}
				}
				if (trunk.isEmpty() || leaves.isEmpty()) {
					logger.log(Level.WARNING, "Cannot load tree: {0}", o);
					continue;
				}
				Tree tree = new Tree(trunk, leaves);
				if (((ArrayList) o).size() > 2) {
					LinkedHashMap map = (LinkedHashMap) ((ArrayList) o).get(2);
					for (Object key : map.keySet()) {
						if (!(key instanceof String)) {
							logger.log(Level.WARNING, "invalid tree option: {0}", key);
							continue;
						}
						String s = ((String) key).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
						boolean found = false;
						for (Option option : Option.options) {
							if (!option.tree)
								continue;
							if (option.getLocalName().equals(s)) {
								found = true;
								option.setValue(tree, option.load(map.get(key)));
							}
						}
						if (!found)
							logger.log(Level.WARNING, "Found unknown tree option: {0}", key);
					}
				}
				if (Option.STARTUP_LOGS.isTrue())
					tree.print(logger);
				this.trees.add(tree);
			} else if (o instanceof String) {
				ArrayList<Material> trunk = new ArrayList<>();
				ArrayList<Material> leaves = new ArrayList<>();
				Material t = Material.matchMaterial((String) o);
				Material l = Material.matchMaterial(
						((String) o).replace("STRIPPED_", "").replace("LOG", "LEAVES").replace("WOOD", "LEAVES"));
				if (t != null)
					trunk.add(t);
				if (l != null)
					leaves.add(l);
				if (trunk.isEmpty() || leaves.isEmpty()) {
					logger.log(Level.WARNING, "Cannot load tree: {0}", o);
					continue;
				}
				Tree tree = new Tree(trunk, leaves);
				if (Option.STARTUP_LOGS.isTrue())
					tree.print(logger);
				this.trees.add(tree);
			} else {
				logger.log(Level.WARNING, "Cannot load tree: {0}", o);
			}
		}
//</editor-fold>
		// <editor-fold defaultstate="collapsed" desc="Tools">
		ArrayList<Object> tools = new ArrayList<>(getConfig().getList("tools"));
		for (Object o : tools) {
			if (o instanceof LinkedHashMap) {
				LinkedHashMap map = (LinkedHashMap) o;
				if (!map.containsKey("type") || !(map.get("type") instanceof String)) {
					logger.log(Level.WARNING, "Cannot find tool material! Skipping...");
					continue;
				}
				String typ = (String) map.get("type");
				Material type = Material.matchMaterial(typ.trim());
				if (type == null) {
					logger.log(Level.WARNING, "Unknown tool material: {0}! Skipping...", map.get("type"));
					continue;
				}
				Tool tool = new Tool(type);
				for (Object key : map.keySet()) {
					if (key.equals("type"))
						continue;// already got that
					if (!(key instanceof String)) {
						logger.log(Level.WARNING, "Unknown tool property: {0}; Skipping...", key);
						continue;
					}
					String s = ((String) key).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
					boolean found = false;
					for (Option option : Option.options) {
						if (!option.tool)
							continue;
						if (option.getLocalName().equals(s)) {
							found = true;
							option.setValue(tool, option.load(map.get(key)));
						}
					}
					if (!found)
						logger.log(Level.WARNING, "Found unknown tool option: {0}", key);
				}
				if (Option.STARTUP_LOGS.isTrue())
					tool.print(logger);
				this.tools.add(tool);
			} else if (o instanceof String) {
				Material m = Material.matchMaterial((String) o);
				if (m == null) {
					logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", o);
				}
				Tool tool = new Tool(m);
				if (Option.STARTUP_LOGS.isTrue())
					tool.print(logger);
				this.tools.add(tool);
			} else {
				logger.log(Level.INFO, "Unknown tool declaration: {0} | {1}",
						new Object[] { o.getClass().getName(), o.toString() });
			}
		}
//</editor-fold>
//		TreeFellerCompat.reload();
	}

	private void breakBlock(DetectedTree detectedTree, boolean dropItems, Tree tree, Tool tool, ItemStack axe,
			Block block, Block origin, int lowest, Player player, long seed) {
		ArrayList<Material> overridables = new ArrayList<>(Option.defaultOverridables);
		ArrayList<Effect> effects = new ArrayList<>();
		boolean isLeaf = !tree.trunk.contains(block.getType());
//		for (Effect e : Option.EFFECTS.get(tool, tree)) {
//			if (e.location == Effect.EffectLocation.TREE)
//				effects.add(e);
//			if (isLeaf) {
//				if (e.location == Effect.EffectLocation.LEAVES)
//					effects.add(e);
//			} else {
//				if (e.location == Effect.EffectLocation.LOGS)
//					effects.add(e);
//			}
//		}
		FellBehavior behavior = FellBehavior.BREAK;
//        double dropChance = dropItems?(isLeaf?Option.LEAF_DROP_CHANCE.get(tool, tree):Option.LOG_DROP_CHANCE.get(tool, tree)):0;
		double directionalFallVelocity = .35d;
		double verticalFallVelocity = .05d;
		double explosiveFallVelocity = 0d;
		double randomFallVelocity = 0d;
		boolean rotate = false;
		DirectionalFallBehavior directionalFallBehavior = DirectionalFallBehavior.RANDOM;
		boolean lockCardinal = false;
		ArrayList<Modifier> modifiers = new ArrayList<>();
//		if (behavior == FellBehavior.FALL || behavior == FellBehavior.FALL_HURT || behavior == FellBehavior.NATURAL) {
//			TreeFellerCompat.removeBlock(player, block);
//		} else {
//			TreeFellerCompat.breakBlock(tree, tool, player, axe, block, modifiers);
//		}
		behavior.breakBlock(detectedTree, this, dropItems, tree, tool, axe, block, origin, lowest, player, seed,
				modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables,
				randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
		Random rand = new Random();
		for (Effect e : effects) {
			if (rand.nextDouble() < e.chance)
				e.play(block);
		}
	}

	int randbetween(int[] minmax) {
		return randbetween(minmax[0], minmax[1]);
	}

	int randbetween(int min, int max) {
		return new Random().nextInt(max - min + 1) + min;
	}

	Collection<? extends ItemStack> getDropsWithBonus(Block block, Tool tool, Tree tree, ItemStack axe, int[] xp,
			List<Modifier> modifiers) {
		if (xp.length != 1)
			throw new IllegalArgumentException("xp must be an array of size 1!");
		ArrayList<ItemStack> drops = new ArrayList<>();
		double dropChance = 1d;
		for (Modifier mod : modifiers) {
			dropChance = mod.apply(dropChance, tree, block);
		}
		boolean drop = true;
		int bonus = 0;
		if (dropChance <= 1) {
			drop = new Random().nextDouble() < dropChance;
		} else {
			while (dropChance > 1) {
				dropChance--;
				bonus++;
			}
			if (new Random().nextDouble() < dropChance)
				bonus++;
		}
		if (drop) {
			for (int i = 0; i < bonus + 1; i++) {
				int[] blockXP = new int[1];
				drops.addAll(getDrops(block, tool, tree, axe, blockXP));
				xp[0] += blockXP[0];
			}
		}
		return drops;
	}

	Collection<? extends ItemStack> getDrops(Block block, Tool tool, Tree tree, ItemStack axe, int[] xp) {
		if (xp.length != 1)
			throw new IllegalArgumentException("blockXP must be an array of length 1!");
		if (exp.containsKey(block.getType())) {
			xp[0] += randbetween(exp.get(block.getType()));
		}
		ArrayList<ItemStack> drops = new ArrayList<>();
		boolean convert = false;
		boolean fortune = true, silk = true;
//		if (tree.trunk.contains(block.getType())) {
//			fortune = Option.LOG_FORTUNE.get(tool, tree);
//			silk = Option.LOG_SILK_TOUCH.get(tool, tree);
//		} else {
//			fortune = Option.LEAF_FORTUNE.get(tool, tree);
//			silk = Option.LEAF_SILK_TOUCH.get(tool, tree);
//		}
		Material type = block.getType();
		drops.addAll(block.getDrops());
		for (Iterator<ItemStack> it = drops.iterator(); it.hasNext();) {
			ItemStack next = it.next();
			if (next.getType().isAir())
				it.remove();// don't try to drop air
		}
		if (axe.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS) && fortune)
			applyFortune(type, drops, axe, axe.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS), xp);
		if (axe.containsEnchantment(Enchantment.SILK_TOUCH) && silk)
			applySilkTouch(type, drops, axe, axe.getEnchantmentLevel(Enchantment.SILK_TOUCH), xp);
		if (convert) {
			for (ItemStack s : drops) {
				if (s.getType().name().endsWith("_WOOD")) {
					s.setType(Material.matchMaterial(s.getType().name().replace("_WOOD", "_LOG")));
				}
				if (s.getType().name().endsWith("_HYPHAE")) {
					s.setType(Material.matchMaterial(s.getType().name().replace("_HYPHAE", "_STEM")));
				}
			}
		}
		return drops;
	}

	// Bukkit API lacks fortune/silk touch handling, so I have to do it the hard
	// way...
	static void applyFortune(Material type, ArrayList<ItemStack> drops, ItemStack axe, int enchantmentLevel, int[] xp) {
		if (enchantmentLevel == 0)
			return;
		switch (type) {
		case COAL_ORE:
		case DIAMOND_ORE:
		case EMERALD_ORE:
		case LAPIS_ORE:
		case NETHER_QUARTZ_ORE:
		case REDSTONE_ORE:// incorrect
		case NETHER_GOLD_ORE:// might be incorrect
			ArrayList<Integer> mults = new ArrayList<>();
			mults.add(1);
			mults.add(1);
			for (int i = 0; i < enchantmentLevel; i++) {
				mults.add(i + 2);
			}
			Random rand = new Random();
			int mult = mults.get(rand.nextInt(mults.size()));
			for (ItemStack s : drops) {
				s.setAmount(s.getAmount() * mult);
			}
			break;
		case OAK_LEAVES:
		case BIRCH_LEAVES:
		case SPRUCE_LEAVES:
		case ACACIA_LEAVES:
		case DARK_OAK_LEAVES:
		case JUNGLE_LEAVES:
			Random r = new Random();
			int fortune = Math.min(4, enchantmentLevel);
			Material sapling = Material.matchMaterial(type.name().replace("LEAVES", "SAPLING"));
			if (type == Material.JUNGLE_LEAVES) {
				switch (fortune) {
				case 1:
					if (r.nextDouble() < .0023)
						drops.add(new ItemStack(sapling));
					break;
				case 2:
					if (r.nextDouble() < .00625)
						drops.add(new ItemStack(sapling));
					break;
				case 3:
				case 4:
					if (r.nextDouble() < .0167)
						drops.add(new ItemStack(sapling));
					break;
				}
			} else {
				switch (fortune) {
				case 1:
					if (r.nextDouble() < .0125)
						drops.add(new ItemStack(sapling));
					break;
				case 2:
					if (r.nextDouble() < .0333)
						drops.add(new ItemStack(sapling));
					break;
				case 3:
				case 4:
					if (r.nextDouble() < .05)
						drops.add(new ItemStack(sapling));
					break;
				}
			}
			switch (fortune) {
			case 1:
				if (r.nextDouble() < .0022)
					drops.add(new ItemStack(Material.STICK));
				break;
			case 2:
				if (r.nextDouble() < .005)
					drops.add(new ItemStack(Material.STICK));
				break;
			case 3:
				if (r.nextDouble() < .0133)
					drops.add(new ItemStack(Material.STICK));
				break;
			case 4:
				if (r.nextDouble() < .08)
					drops.add(new ItemStack(Material.STICK));// why, minecraft, why?
				break;
			}
			if (type == Material.OAK_LEAVES) {
				switch (fortune) {
				case 1:
					if (r.nextDouble() < .00056)
						drops.add(new ItemStack(Material.APPLE));
					break;
				case 2:
					if (r.nextDouble() < .00125)
						drops.add(new ItemStack(Material.APPLE));
					break;
				case 3:
				case 4:
					if (r.nextDouble() < .00333)
						drops.add(new ItemStack(Material.APPLE));
					break;
				}
			}
			break;
		}
	}

	static void applySilkTouch(Material type, ArrayList<ItemStack> drops, ItemStack axe, int enchantmentLevel,
			int[] xp) {
		if (enchantmentLevel == 0)
			return;
		switch (type) {
		case BEEHIVE:
		case BEE_NEST:
		case CAMPFIRE:
		case BLUE_ICE:
		case BOOKSHELF:
		case CLAY:
		case BUBBLE_CORAL:
		case HORN_CORAL:
		case FIRE_CORAL:
		case TUBE_CORAL:
		case BRAIN_CORAL:
		case BUBBLE_CORAL_FAN:
		case HORN_CORAL_FAN:
		case FIRE_CORAL_FAN:
		case TUBE_CORAL_FAN:
		case BRAIN_CORAL_FAN:
		case BUBBLE_CORAL_WALL_FAN:
		case HORN_CORAL_WALL_FAN:
		case FIRE_CORAL_WALL_FAN:
		case TUBE_CORAL_WALL_FAN:
		case BRAIN_CORAL_WALL_FAN:
		case GLASS:
		case BLUE_STAINED_GLASS:
		case RED_STAINED_GLASS:
		case ORANGE_STAINED_GLASS:
		case PINK_STAINED_GLASS:
		case YELLOW_STAINED_GLASS:
		case LIME_STAINED_GLASS:
		case GREEN_STAINED_GLASS:
		case CYAN_STAINED_GLASS:
		case LIGHT_BLUE_STAINED_GLASS:
		case MAGENTA_STAINED_GLASS:
		case PURPLE_STAINED_GLASS:
		case GRAY_STAINED_GLASS:
		case LIGHT_GRAY_STAINED_GLASS:
		case BLACK_STAINED_GLASS:
		case WHITE_STAINED_GLASS:
		case BROWN_STAINED_GLASS:
		case BLUE_STAINED_GLASS_PANE:
		case RED_STAINED_GLASS_PANE:
		case ORANGE_STAINED_GLASS_PANE:
		case PINK_STAINED_GLASS_PANE:
		case YELLOW_STAINED_GLASS_PANE:
		case LIME_STAINED_GLASS_PANE:
		case GREEN_STAINED_GLASS_PANE:
		case CYAN_STAINED_GLASS_PANE:
		case LIGHT_BLUE_STAINED_GLASS_PANE:
		case MAGENTA_STAINED_GLASS_PANE:
		case PURPLE_STAINED_GLASS_PANE:
		case GRAY_STAINED_GLASS_PANE:
		case LIGHT_GRAY_STAINED_GLASS_PANE:
		case BLACK_STAINED_GLASS_PANE:
		case WHITE_STAINED_GLASS_PANE:
		case BROWN_STAINED_GLASS_PANE:
		case GLOWSTONE:
		case GRASS_BLOCK:
		case GRAVEL:
		case ICE:
		case OAK_LEAVES:
		case BIRCH_LEAVES:
		case SPRUCE_LEAVES:
		case JUNGLE_LEAVES:
		case ACACIA_LEAVES:
		case DARK_OAK_LEAVES:
		case MELON:
		case MUSHROOM_STEM:
		case BROWN_MUSHROOM_BLOCK:
		case RED_MUSHROOM_BLOCK:
		case MYCELIUM:
		case PODZOL:
		case SEA_LANTERN:
		case TURTLE_EGG:
		case SOUL_CAMPFIRE:
			drops.clear();
			drops.add(new ItemStack(type));
			xp[0] = 0;
			break;
		// pickaxe only (conditional too!)
		case COAL_ORE:
		case BUBBLE_CORAL_BLOCK:
		case HORN_CORAL_BLOCK:
		case FIRE_CORAL_BLOCK:
		case TUBE_CORAL_BLOCK:
		case BRAIN_CORAL_BLOCK:
		case DIAMOND_ORE:
		case EMERALD_ORE:
		case ENDER_CHEST:
		case LAPIS_ORE:
		case NETHER_QUARTZ_ORE:
		case REDSTONE_ORE:
		case NETHER_GOLD_ORE:
		case GILDED_BLACKSTONE:
		case STONE:
			if (axe.getType().name().toLowerCase().contains("pickaxe")) {
				xp[0] = 0;
				if (drops.isEmpty())
					return;
				drops.clear();
				drops.add(new ItemStack(type));
			}
			break;
		// shovel only
		case SNOW:
		case SNOW_BLOCK:
			if (axe.getType().name().toLowerCase().contains("shovel")) {
				drops.clear();
				drops.add(new ItemStack(type));
				xp[0] = 0;
			}
			break;
		}
	}

	private void leafCheck(HashMap<Integer, ArrayList<Block>> someLeaves, ArrayList<Material> trunk,
			ArrayList<Material> leaves, Boolean diagonal, Boolean playerLeaves, Boolean ignoreLeafData) {
		if (ignoreLeafData)
			return;
		ArrayList<Integer> ints = new ArrayList<>();
		ints.addAll(someLeaves.keySet());
		Collections.sort(ints);
		for (int d : ints) {
			for (Iterator<Block> it = someLeaves.get(d).iterator(); it.hasNext();) {
				Block leaf = it.next();
				if (distance(leaf, trunk, leaves, d, diagonal, playerLeaves) < d) {
					it.remove();
				}
			}
		}
	}

	ArrayList<ItemStack> getDrops(Material m, Tool tool, Tree tree, ItemStack axe, Block location, int[] xp,
			List<Modifier> modifiers) {
		ArrayList<ItemStack> drops = new ArrayList<>();
		if (!m.isBlock())
			return drops;
		Block block = findAir(location);
		if (block == null) {
			drops.add(new ItemStack(m));
			return drops;
		}
		block.setType(m);
		drops.addAll(getDropsWithBonus(block, tool, tree, axe, xp, modifiers));
		block.setType(Material.AIR);
		return drops;
	}

	private Block findAir(Block location) {
		if (location.getType() == Material.AIR)
			return location;
		for (int x = -8; x <= 8; x++) {
			for (int y = 255; y > 0; y--) {
				for (int z = -1; z <= 8; z++) {
					Block b = location.getWorld().getBlockAt(x, y, z);
					if (b.getType() == Material.AIR)
						return b;
				}
			}
		}
		getLogger().log(Level.SEVERE, "Could not find any nearby air blocks to simulate drops!");
		return null;
	}

	private int debugIndent = 0;

	private void debug(Player player, String text, boolean indent, Object... vars) {
		Message message = Message.getMessage(text);
		if (message != null) {
			message.send(player, vars);
			text = message.getDebugText();
		}
		for (int i = 0; i < vars.length; i++) {
			text = text.replace("{" + i + "}", vars[i].toString());
		}
		if (!debug)
			return;
		if (indent)
			debugIndent++;
		text = "[TreeFeller] " + getDebugIndent() + " " + text;
		getLogger().log(Level.INFO, text);
		if (player != null)
			player.sendMessage(text);
	}

	private void debug(Player player, boolean critical, DebugResult result) {
		Message message = Message.getMessage(result.message + result.type.suffix);
		if (message != null) {
			message.send(player, result.args);
			if (!debug)
				return;
			String text = message.getDebugText();
			for (int i = 0; i < result.args.length; i++) {
				text = text.replace("{" + i + "}", result.args[i].toString());
			}
			if ((critical || !result.isSuccess()) && debugIndent > 0)
				debugIndent--;
			String icon;
			if (result.isSuccess())
				icon = (critical ? ChatColor.DARK_GREEN : ChatColor.GREEN) + "O";
			else
				icon = (critical ? ChatColor.DARK_RED : ChatColor.RED) + "X";
			text = "[TreeFeller] " + getDebugIndent(1) + icon + ChatColor.RESET + " " + text;
			getLogger().log(Level.INFO, text);
			if (player != null)
				player.sendMessage(text);
		}
	}

	private void debug(Player player, boolean critical, boolean success, String text, Object... vars) {
		Message message = Message.getMessage(text);
		if (message != null) {
			message.send(player, vars);
			text = message.getDebugText();
		}
		for (int i = 0; i < vars.length; i++) {
			text = text.replace("{" + i + "}", vars[i].toString());
		}
		if (!debug)
			return;
		if ((critical || !success) && debugIndent > 0)
			debugIndent--;
		String icon;
		if (success)
			icon = (critical ? ChatColor.DARK_GREEN : ChatColor.GREEN) + "O";
		else
			icon = (critical ? ChatColor.DARK_RED : ChatColor.RED) + "X";
		text = "[TreeFeller] " + getDebugIndent(1) + icon + ChatColor.RESET + " " + text;
		getLogger().log(Level.INFO, text);
		if (player != null)
			player.sendMessage(text);
	}

	private String getDebugIndent() {
		return getDebugIndent(0);
	}

	private String getDebugIndent(int end) {
		String indent = "";
		for (int i = 0; i < debugIndent - end + 1; i++) {
			indent = indent + "-";
		}
		return indent;
	}

	void dropExp(World world, Location location, int xp) {
		while (xp > 2477) {
			dropExpOrb(world, location, 2477);
			xp -= 2477;
		}
		while (xp > 1237) {
			dropExpOrb(world, location, 1237);
			xp -= 1237;
		}
		while (xp > 617) {
			dropExpOrb(world, location, 617);
			xp -= 617;
		}
		while (xp > 307) {
			dropExpOrb(world, location, 307);
			xp -= 307;
		}
		while (xp > 149) {
			dropExpOrb(world, location, 149);
			xp -= 149;
		}
		while (xp > 73) {
			dropExpOrb(world, location, 73);
			xp -= 73;
		}
		while (xp > 37) {
			dropExpOrb(world, location, 37);
			xp -= 37;
		}
		while (xp > 17) {
			dropExpOrb(world, location, 17);
			xp -= 17;
		}
		while (xp > 7) {
			dropExpOrb(world, location, 7);
			xp -= 7;
		}
		while (xp > 3) {
			dropExpOrb(world, location, 3);
			xp -= 3;
		}
		while (xp > 1) {
			dropExpOrb(world, location, 1);
			xp--;
		}
	}

	public void dropExpOrb(World world, Location location, int xp) {
		ExperienceOrb orb = (ExperienceOrb) world.spawnEntity(location, EntityType.EXPERIENCE_ORB);
		orb.setExperience(orb.getExperience() + xp);
	}

	public static Tree detect(Block clickedBlock, Player player) {
		ArrayList<Material> allLogs = new ArrayList<>();
		allLogs.add(Material.OAK_LOG);
		allLogs.add(Material.BIRCH_LOG);
		allLogs.add(Material.SPRUCE_LOG);
		allLogs.add(Material.DARK_OAK_LOG);
		allLogs.add(Material.ACACIA_LOG);
		allLogs.add(Material.JUNGLE_LOG);
		allLogs.add(Material.WARPED_STEM);
		allLogs.add(Material.CRIMSON_STEM);
		allLogs.add(Material.OAK_WOOD);
		allLogs.add(Material.BIRCH_WOOD);
		allLogs.add(Material.SPRUCE_WOOD);
		allLogs.add(Material.DARK_OAK_WOOD);
		allLogs.add(Material.ACACIA_WOOD);
		allLogs.add(Material.JUNGLE_WOOD);
		allLogs.add(Material.WARPED_HYPHAE);
		allLogs.add(Material.CRIMSON_HYPHAE);
		allLogs.add(Material.MUSHROOM_STEM);
		HashMap<Integer, ArrayList<Block>> trunk = getBlocks(allLogs, clickedBlock, Option.SCAN_DISTANCE.getValue(),
				Integer.MAX_VALUE, true, true, true);
		HashSet<Material> logs = new HashSet<>();
		for (int i : trunk.keySet()) {
			for (Block b : trunk.get(i))
				logs.add(b.getType());
		}
		if (logs.isEmpty()) {
			player.sendMessage(ChatColor.RED + "Failed to detect tree trunk");
			return null;
		}
		ArrayList<Material> allLeaves = new ArrayList<>();
		allLeaves.add(Material.OAK_LEAVES);
		allLeaves.add(Material.BIRCH_LEAVES);
		allLeaves.add(Material.SPRUCE_LEAVES);
		allLeaves.add(Material.DARK_OAK_LEAVES);
		allLeaves.add(Material.ACACIA_LEAVES);
		allLeaves.add(Material.JUNGLE_LEAVES);
		allLeaves.add(Material.WARPED_WART_BLOCK);
		allLeaves.add(Material.NETHER_WART_BLOCK);
		allLeaves.add(Material.SHROOMLIGHT);
		allLeaves.add(Material.RED_MUSHROOM_BLOCK);
		allLeaves.add(Material.BROWN_MUSHROOM_BLOCK);
		ArrayList<Material> allBlocks = new ArrayList<>(logs);
		allBlocks.addAll(allLeaves);
		HashMap<Integer, ArrayList<Block>> properLeaves;
		HashMap<Integer, ArrayList<Block>> badLeaves;
		HashMap<Integer, ArrayList<Block>> terribleLeaves;
		int proper = getTotal(properLeaves = getBlocks(allBlocks, clickedBlock, Option.SCAN_DISTANCE.getValue(),
				Integer.MAX_VALUE, false, false, false));
		int bad = getTotal(badLeaves = getBlocks(allBlocks, clickedBlock, Option.SCAN_DISTANCE.getValue(),
				Integer.MAX_VALUE, false, true, true));
		int terrible = getTotal(terribleLeaves = getBlocks(allBlocks, clickedBlock, Option.SCAN_DISTANCE.getValue(),
				Integer.MAX_VALUE, true, true, true));
		int numLogs = getTotal(trunk);
		int numLeaves;
		HashSet<Material> leaves = new HashSet<>();
		boolean diagonalLeaves = false;
		boolean playerLeaves = false;
		boolean ignoreLeafData = false;
		if (terrible > bad || terrible > proper) {
			numLeaves = terrible - numLogs;
			for (int i : terribleLeaves.keySet()) {
				for (Block b : terribleLeaves.get(i))
					leaves.add(b.getType());
			}
			diagonalLeaves = playerLeaves = ignoreLeafData = true;
		} else if (bad > proper) {
			numLeaves = bad - numLogs;
			for (int i : badLeaves.keySet()) {
				for (Block b : badLeaves.get(i))
					leaves.add(b.getType());
			}
			playerLeaves = ignoreLeafData = true;
		} else {
			numLeaves = proper - numLogs;
			for (int i : properLeaves.keySet()) {
				for (Block b : properLeaves.get(i))
					leaves.add(b.getType());
			}
		}
		leaves.removeAll(logs);
		Tree tree = new Tree(new ArrayList<>(logs), new ArrayList<>(leaves));
		if (numLogs < Option.REQUIRED_LOGS.getValue()) {
			Option.REQUIRED_LOGS.treeValues.put(tree, numLogs / 4);
		}
		if (numLogs > Option.MAX_LOGS.getValue()) {
			Option.MAX_LOGS.treeValues.put(tree, numLogs * 2);
		}
		if (numLeaves < Option.REQUIRED_LEAVES.getValue()) {
			Option.REQUIRED_LEAVES.treeValues.put(tree, numLeaves / 4);
		}
//		if (diagonalLeaves || Objects.equals(Option.DIAGONAL_LEAVES.getValue(), true))
//			Option.DIAGONAL_LEAVES.treeValues.put(tree, diagonalLeaves);
		if (playerLeaves || Objects.equals(Option.PLAYER_LEAVES.getValue(), true))
			Option.PLAYER_LEAVES.treeValues.put(tree, playerLeaves);
//		if (ignoreLeafData || Objects.equals(Option.IGNORE_LEAF_DATA.getValue(), true))
//			Option.IGNORE_LEAF_DATA.treeValues.put(tree, ignoreLeafData);
		// now to find the leaf range...
		ArrayList<Integer> distances = new ArrayList<>(trunk.keySet());
		Collections.sort(distances);
		int theLeafRange = 0;
		int lastCount = 0;
		for (int leafRange = 1; leafRange < Option.SCAN_DISTANCE.getValue(); leafRange++) {
			HashSet<Block> allDaLeaves = new HashSet<>();
			FOR: for (int i : distances) {
				for (Block b : trunk.get(i)) {
					HashMap<Integer, ArrayList<Block>> someLeaves = getBlocks(tree.leaves, b, leafRange,
							Integer.MAX_VALUE, diagonalLeaves, playerLeaves, ignoreLeafData);
					for (int in : someLeaves.keySet()) {
						allDaLeaves.addAll(someLeaves.get(in));
					}
				}
			}
			if (allDaLeaves.size() == lastCount)
				break;
			theLeafRange = leafRange;
			lastCount = allDaLeaves.size();
		}
		if (theLeafRange > Option.LEAF_DETECT_RANGE.getValue())
			Option.LEAF_DETECT_RANGE.treeValues.put(tree, theLeafRange);
		return tree;
	}

	BukkitTask saplingHandler;

	private void createSaplingHandler() {
		if (saplingHandler != null)
			return;
		saplingHandler = new BukkitRunnable() {
			@Override
			public void run() {
				for (Iterator<Sapling> it = saplings.iterator(); it.hasNext();) {
					Sapling sapling = it.next();
					sapling.tick();
					if (sapling.isDead())
						it.remove();
				}
				if (saplings.isEmpty()) {
					saplingHandler = null;
					cancel();
				}
			}
		}.runTaskTimer(this, 0, 1);
	}

	/**
	 * Handle dropped item. Item has already dropped; this handles it for sapling
	 * replant & compatibilities
	 * 
	 * @param detectedTree the tree that was cut down
	 * @param player       the player who dropped the item
	 * @param item         the item that was dropped
	 */
	public void dropItem(DetectedTree detectedTree, Player player, Item item) {
		ItemStack stack = item.getItemStack();
		for (Sapling sapling : saplings) {
			if (sapling.detectedTree == detectedTree) {
				if (sapling.tryPlace(stack)) {
					if (stack.getAmount() == 0) {
						item.remove();
						return;
					}
				}
			}
		}
		item.setItemStack(stack);
//		TreeFellerCompat.dropItem(player, item);
	}
}