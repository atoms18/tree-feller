package com.thizthizzydizzy.treefeller;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;

public class Tool {
	public Material material;
	public static final HashMap<Material, Integer> toolSpeed = new HashMap<>();
	static {// Perhaps this should be in the config rather than hard-coded...
		toolSpeed.put(Material.WOODEN_AXE, 2);
		toolSpeed.put(Material.STONE_AXE, 4);
		toolSpeed.put(Material.IRON_AXE, 6);
		toolSpeed.put(Material.GOLDEN_AXE, 12);
		toolSpeed.put(Material.DIAMOND_AXE, 8);
		toolSpeed.put(Material.NETHERITE_AXE, 9);
	}

	public Tool(Material material) {
		this.material = material;
	}

	public void print(Logger logger) {
		logger.log(Level.INFO, "Loaded tool: {0}", material);
		for (Option option : Option.options) {
			Object value = option.getValue(this);
			if (value != null) {
				logger.log(Level.INFO, "- {0}: {1}", new Object[] { option.name, option.makeReadable(value) });
			}
		}
	}

	public String writeToConfig() {
		String str = "{type: " + material.name();
		for (Option o : Option.options) {
			if (o.getValue(this) != null)
				str += ", " + o.getGlobalName() + ": " + o.writeToConfig(this);
		}
		return str + "}";
	}
}