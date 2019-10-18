package com.github.commoble.neverwhere;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

public class NeverwhereTeleporter
{
	public static void teleportPlayer(ServerPlayerEntity player, DimensionType destinationType, BlockPos pos)
	{
		ServerWorld nextWorld = player.getServer().getWorld(destinationType);
		nextWorld.getChunk(pos); // ensure chunk is loaded
		player.teleport(nextWorld, pos.getX(), pos.getY(), pos.getZ(), player.rotationYaw, player.rotationPitch);
	}
}
