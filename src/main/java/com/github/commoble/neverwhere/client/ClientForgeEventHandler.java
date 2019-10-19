package com.github.commoble.neverwhere.client;

import com.github.commoble.neverwhere.Neverwhere;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(value= {Dist.CLIENT}, modid=Neverwhere.MODID, bus=Bus.FORGE)
public class ClientForgeEventHandler
{
	@SubscribeEvent
	public static void onPlayStreamingSound(PlayStreamingSourceEvent event)
	{
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player != null && player.world.getDimension().getType() == Neverwhere.getDimensionType() && event.getSound().getCategory() == SoundCategory.MUSIC)
		{
			event.getManager().stop(event.getSound());
			player.playSound(Neverwhere.windSound.get(), 0.5F, 0.8F);
		}
	}
}
