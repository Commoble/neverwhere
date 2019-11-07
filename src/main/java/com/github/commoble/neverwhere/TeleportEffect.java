package com.github.commoble.neverwhere;

import java.util.function.Function;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.world.dimension.DimensionType;

public class TeleportEffect extends Effect
{
	protected TeleportEffect(EffectType typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn,
			int amplifier)
	{
		if (entityLivingBaseIn instanceof ServerPlayerEntity && entityLivingBaseIn.world.getDimension().getType() == DimensionType.OVERWORLD)
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entityLivingBaseIn;
			PortalHelper.setTime(serverPlayer.getUniqueID(), PortalHelper.COOLDOWN_AFTER_TELEPORT);
			PortalHelper.teleportPlayer(serverPlayer, Function.identity());
		}
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
	}

	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn,
			AbstractAttributeMap attributeMapIn, int amplifier)
	{

		if (entityLivingBaseIn instanceof ServerPlayerEntity && entityLivingBaseIn.world.getDimension().getType() == Neverwhere.getDimensionType())
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entityLivingBaseIn;
			PortalHelper.setTime(serverPlayer.getUniqueID(), PortalHelper.COOLDOWN_AFTER_TELEPORT);
			PortalHelper.teleportPlayer(serverPlayer, Function.identity());
		}
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);

	}
}
