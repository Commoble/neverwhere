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
	
	
	public static double block_place_reflection_chance = 0.3F;
	public static double block_break_reflection_chance = 1.0F;
	public static int reflection_buffer_size = 20;
	
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
		
		block_place_reflection_chance = SERVER.block_place_reflection_chance.get();
		block_break_reflection_chance = SERVER.block_break_reflection_chance.get();
		reflection_buffer_size = SERVER.reflection_buffer_size.get();
	}
	
	public static class ServerConfig
	{
		public final ForgeConfigSpec.IntValue neverwas_spawn_threshold;
		public final ForgeConfigSpec.IntValue neverwas_interest_threshold;
		public final ForgeConfigSpec.IntValue neverwas_follow_threshold;
		public final ForgeConfigSpec.IntValue neverwas_attack_threshold;
		
		public final ForgeConfigSpec.DoubleValue block_place_reflection_chance;
		public final ForgeConfigSpec.DoubleValue block_break_reflection_chance;
		public final ForgeConfigSpec.IntValue reflection_buffer_size;
		
		ServerConfig(ForgeConfigSpec.Builder builder)
		{
			builder.push("Neverwas AI");
			
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
			
			builder.push("Block Reflection");
			
			this.block_place_reflection_chance = builder
					.comment("Probability that placing a block in the overworld will place a block in the Neverwhere")
					.translation("neverwhere.config.block_place_reflection_chance")
					.defineInRange("block_place_reflection_chance", 0.3D, 0.0D, 1.0D);
			this.block_break_reflection_chance = builder
					.comment("Probability that breaking a block in the overworld will place a block in the Neverwhere")
					.translation("neverwhere.config.block_break_reflection_chance")
					.defineInRange("block_break_reflection_chance", 1.0D, 0.0D, 1.0D);
			this.reflection_buffer_size = builder
					.comment("The amount of blockstate changes in a given chunk to queue up before the changes are dumped into Neverwhere")
					.translation("neverwhere.config.reflection_buffer_limit")
					.defineInRange("reflection_buffer_limit", 20, 0, Integer.MAX_VALUE);
			
			builder.pop();
		}
	}
}
