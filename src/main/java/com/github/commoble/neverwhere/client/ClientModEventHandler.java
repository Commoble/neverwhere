package com.github.commoble.neverwhere.client;

import com.github.commoble.neverwhere.NeverwasEntity;
import com.github.commoble.neverwhere.Neverwhere;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value= {Dist.CLIENT}, modid=Neverwhere.MODID, bus=Bus.MOD)
public class ClientModEventHandler
{
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		//ClientRegistry.bindTileEntitySpecialRenderer(NeverPortalTileEntity.class, new NeverPortalTileEntityRenderer());
		RenderingRegistry.registerEntityRenderingHandler(NeverwasEntity.class, NeverwasRenderer::new);
	}
}
