package com.github.commoble.neverwhere;

import java.util.Random;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class VoidTree extends Tree
{
	public static final VoidTree INSTANCE = new VoidTree();
	
	@Override
	protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random)
	{
		return Neverwhere.wrongTree.get();
	}
}
