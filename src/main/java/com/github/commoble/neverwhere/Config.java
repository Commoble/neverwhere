package com.github.commoble.neverwhere;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;

@Mod.EventBusSubscriber(modid=Neverwhere.MODID, bus = Bus.MOD)
public class Config
{
	// the layer between the values we want to reference in java and the actual config file somewhere
	public static final ServerConfig SERVER;	// the thing that holds data from the config file
	public static final ForgeConfigSpec SERVER_SPEC;	// used by the event handler to make sure the given config file is our config file
	
	// the actual values our java will reference
	public static int neverwas_spawn_threshold = 5;
	public static int neverwas_interest_threshold=10;
	public static int neverwas_follow_threshold=15;
	public static int neverwas_attack_threshold=20;
	
	// config the configs
	static
	{
		final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}
	
	// event for refreshing the config data
	@SubscribeEvent
	public static void onConfigEvent(ModConfigEvent event)
	{
		ModConfig config = event.getConfig();
		if (config.getSpec() == SERVER_SPEC)
		{
			refreshServer();
		}
	}
	
	public static void refreshServer()
	{
		neverwas_spawn_threshold = SERVER.neverwas_spawn_threshold.get();
		neverwas_interest_threshold = SERVER.neverwas_interest_threshold.get();
		neverwas_follow_threshold = SERVER.neverwas_follow_threshold.get();
		neverwas_attack_threshold = SERVER.neverwas_attack_threshold.get();
	}
	
	public static class ServerConfig
	{
		public final ForgeConfigSpec.IntValue neverwas_spawn_threshold;
		public final ForgeConfigSpec.IntValue neverwas_interest_threshold;
		public final ForgeConfigSpec.IntValue neverwas_follow_threshold;
		public final ForgeConfigSpec.IntValue neverwas_attack_threshold;
		
		ServerConfig(ForgeConfigSpec.Builder builder)
		{
			builder.push("general");
			
			this.neverwas_spawn_threshold = builder
					.comment("Minimum experience level for Neverwere to spawn near a player in the Neverwhere")
					.translation("neverwhere.config.neverwas_spawn_threshold")
					.defineInRange("neverwas_spawn_threshold", 5, 0, Integer.MAX_VALUE);
			this.neverwas_interest_threshold = builder
					.comment("Minimum experience level for Neverwere to be less afraid of the player")
					.translation("neverwhere.config.neverwas_interest_threshold")
					.defineInRange("neverwas_interest_threshold", 10, 0, Integer.MAX_VALUE);
			this.neverwas_follow_threshold = builder
					.comment("Minimum experience level for Neverwere to approach the player")
					.translation("neverwhere.config.neverwas_follow_threshold")
					.defineInRange("neverwas_follow_threshold", 15, 0, Integer.MAX_VALUE);
			this.neverwas_attack_threshold = builder
					.comment("Minimum experience level for Neverwere to attack the player without provocation")
					.translation("neverwhere.config.neverwas_attack_threshold")
					.defineInRange("neverwas_attack_threshold", 20, 0, Integer.MAX_VALUE);
			
			builder.pop();
		}
	}
}
