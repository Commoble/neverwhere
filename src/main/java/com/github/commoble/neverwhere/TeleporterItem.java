package com.github.commoble.neverwhere;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class TeleporterItem extends Item
{

	public TeleporterItem(Properties properties)
	{
		super(properties);
	}

	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when
	 * this item is used on a Block, see {@link #onItemUse}.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && player instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
			DimensionType dim;
			if (serverPlayer.world.dimension.getType() == Neverwhere.getDimensionType())
			{
				dim = DimensionType.OVERWORLD;
			}
			else
			{
				dim = Neverwhere.getDimensionType(); 
			}
			NeverwhereTeleporter.teleportPlayer(serverPlayer, dim, player.getPosition());
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}
}
