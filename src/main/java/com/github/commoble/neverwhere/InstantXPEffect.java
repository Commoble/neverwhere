package com.github.commoble.neverwhere;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class InstantXPEffect extends Effect
{

	protected InstantXPEffect(EffectType typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn);
	}

	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn,
			int amplifier)
	{
		if (entityLivingBaseIn instanceof PlayerEntity)
		{
			((PlayerEntity)entityLivingBaseIn).giveExperiencePoints(entityLivingBaseIn.world.rand.nextInt(3)+2);
		}
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}
}
