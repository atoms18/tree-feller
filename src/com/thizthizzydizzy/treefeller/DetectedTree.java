package com.thizthizzydizzy.treefeller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class DetectedTree {
	public final Tool tool;
	public final Tree tree;
	public final HashMap<Integer, ArrayList<Block>> trunk;
	public final HashMap<Integer, ArrayList<Block>> leaves;
	public ArrayList<Block> stump = new ArrayList<>();
	public ArrayList<Sapling> saplings = new ArrayList<>();

	public DetectedTree(Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> trunk,
			HashMap<Integer, ArrayList<Block>> leaves) {
		this.tool = tool;
		this.tree = tree;
		this.trunk = trunk;
		this.leaves = leaves;
	}

	public void addSapling(Block block, HashSet<Material> saplings) {
		this.saplings.add(new Sapling(this, block, saplings, false, 50));
	}
}