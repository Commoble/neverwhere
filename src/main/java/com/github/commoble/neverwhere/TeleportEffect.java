package com.github.commoble.neverwhere;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class TeleportEffect extends Effect
{
	protected TeleportEffect(EffectType typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn);
	}

//	@Override
//	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn,
//			int amplifier)
//	{
//		if (entityLivingBaseIn instanceof ServerPlayerEntity && entityLivingBaseIn.world.getDimension().getType() == DimensionType.OVERWORLD)
//		{
//		}
//		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
//	}

//	@Override
//	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn,
//			AbstractAttributeMap attributeMapIn, int amplifier)
//	{
//
//		if (entityLivingBaseIn instanceof ServerPlayerEntity
//				&& entityLivingBaseIn.world.getDimension().getType() == Neverwhere.getDimensionType()
//				&& entityLivingBaseIn.getActivePotionEffect(Neverwhere.regretEffect) == null)
//		{
//			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entityLivingBaseIn;
//			PortalHelper.setTime(serverPlayer.getUniqueID(), PortalHelper.COOLDOWN_AFTER_TELEPORT);
//			PortalHelper.teleportPlayer(serverPlayer, Function.identity());
//		}
//		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
//
//	}
}
