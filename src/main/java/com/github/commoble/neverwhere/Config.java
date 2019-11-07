package com.github.commoble.neverwhere;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

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
	
	public static int neverwas_spawn_infrequency = 200;
	public static double neverwas_spawn_cap_distance = 32D;
	public static int neverwas_spawn_local_cap = 20;
	
	private static final List<String> DEFAULT_WHITELIST = new ArrayList<>();
	private static final List<String> DEFAULT_BLACKLIST = Lists.newArrayList(
			"neverwhere:neverportal",
			"minecraft:anvil",
			"minecraft:bedrock",
			"minecraft:coal_block",
			"minecraft:diamond_block",
			"minecraft:emerald_block",
			"minecraft:gold_block",
			"minecraft:iron_block",
			"minecraft:quartz_block",
			"minecraft:redstone_block",
			"minecraft:lapis_lazuli_block",
			"minecraft:bookshelf",
			"minecraft:clay",
			"minecraft:diamond_ore",
			"minecraft:emerald_ore",
			"minecraft:gold_ore",
			"minecraft:tnt"
			);
	
	public static double block_place_reflection_chance = 0.3F;
	public static double block_break_reflection_chance = 1.0F;
	public static int reflection_buffer_size = 20;
	public static Set<Block> block_reflection_whitelist = new HashSet<>();
	public static Set<Block> block_reflection_blacklist = getBlockKeysAsBlocks(DEFAULT_BLACKLIST);
	
	public static double portal_spawn_on_grow_sapling_chance = 0.1D;
	public static int portal_void_spawn_infrequency = 36000;
	
	public static int average_neverwhere_timeout = 5000;
	
	public static boolean isBlockStateAllowedToReflectPlacement(BlockState state)
	{
		if (state.hasTileEntity())
			return false;
		
		if (block_reflection_whitelist.size() > 0)
			return block_reflection_whitelist.contains(state.getBlock());
		
		return !block_reflection_blacklist.contains(state.getBlock());
	}
	
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
	
	private static Set<Block> getBlockKeysAsBlocks(List<? extends String> keys)
	{
		return keys.stream().map(string -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string))).collect(Collectors.toCollection(HashSet::new));
	}
	
	public static void refreshServer()
	{
		neverwas_spawn_threshold = SERVER.neverwas_spawn_threshold.get();
		neverwas_interest_threshold = SERVER.neverwas_interest_threshold.get();
		neverwas_follow_threshold = SERVER.neverwas_follow_threshold.get();
		neverwas_attack_threshold = SERVER.neverwas_attack_threshold.get();
		
		neverwas_spawn_infrequency = SERVER.neverwas_spawn_infrequency.get();
		neverwas_spawn_cap_distance = SERVER.neverwas_spawn_cap_distance.get();
		neverwas_spawn_local_cap = SERVER.neverwas_spawn_local_cap.get();
		
		block_place_reflection_chance = SERVER.block_place_reflection_chance.get();
		block_break_reflection_chance = SERVER.block_break_reflection_chance.get();
		reflection_buffer_size = SERVER.reflection_buffer_size.get();
		block_reflection_whitelist = getBlockKeysAsBlocks(SERVER.block_reflection_whitelist.get());
		block_reflection_blacklist = getBlockKeysAsBlocks(SERVER.block_reflection_blacklist.get());
		
		portal_spawn_on_grow_sapling_chance = SERVER.portal_spawn_on_grow_sapling_chance.get();
		portal_void_spawn_infrequency = SERVER.portal_void_spawn_infrequency.get();
		
		average_neverwhere_timeout = SERVER.average_neverwhere_timeout.get();
		
	}
	
	public static class ServerConfig
	{
		public final ForgeConfigSpec.IntValue neverwas_spawn_threshold;
		public final ForgeConfigSpec.IntValue neverwas_interest_threshold;
		public final ForgeConfigSpec.IntValue neverwas_follow_threshold;
		public final ForgeConfigSpec.IntValue neverwas_attack_threshold;
		
		public final ForgeConfigSpec.IntValue neverwas_spawn_infrequency;
		public final ForgeConfigSpec.DoubleValue neverwas_spawn_cap_distance;
		public final ForgeConfigSpec.IntValue neverwas_spawn_local_cap;
		
		public final ForgeConfigSpec.DoubleValue block_place_reflection_chance;
		public final ForgeConfigSpec.DoubleValue block_break_reflection_chance;
		public final ForgeConfigSpec.IntValue reflection_buffer_size;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> block_reflection_whitelist;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> block_reflection_blacklist;
		
		public final ForgeConfigSpec.DoubleValue portal_spawn_on_grow_sapling_chance;
		public final ForgeConfigSpec.IntValue portal_void_spawn_infrequency;
		
		public final ForgeConfigSpec.IntValue average_neverwhere_timeout;
		
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
			
			builder.push("Neverwas Spawning");
			
			this.neverwas_spawn_infrequency = builder
					.comment("Improbability of Neverwas spawning near a player every tick. e.g. 200 => 1/200 chance each tick, or about one every ten seconds")
					.translation("neverwhere.neverwas_spawn_infrequency")
					.defineInRange("neverwas_spawn_infrequency", 200, 1, Integer.MAX_VALUE);
			this.neverwas_spawn_cap_distance = builder
					.comment("The cubic radius around a player in which neverwas spawning considers nearby neverwas mobs when determining whether to spawn another one")
					.translation("neverwhere.neverwas_spawn_cap_distance")
					.defineInRange("neverwas_spawn_cap_distance", 32D, 0D, Double.MAX_VALUE);
			this.neverwas_spawn_local_cap = builder
					.comment("If this many neverwas exist within a cube around the player defined by the spawn cap distance, no more will be spawned")
					.translation("neverwhere.neverwas_spawn_local_cap")
					.defineInRange("neverwas_spawn_local_cap", 20, 0, Integer.MAX_VALUE);
			
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
			this.block_reflection_whitelist = builder
					.comment("Whitelist of blocks that are allowed to be reflected to the Neverwhere when placed in the Overworld. If this list is empty, all blocks that are not in the Blacklist will be allowed")
					.translation("neverwhere.config.block_reflection_whitelist")
					.defineList("reflection_whitelisted_blocks", Config.DEFAULT_WHITELIST, obj -> obj instanceof String && ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(((String)obj))));
			this.block_reflection_blacklist = builder
					.comment("Blacklist of Blocks that will never be reflected to the Neverwhere when placed in the Overworld. Blocks with TileEntities will never be reflected. If the whitelist is non-empty, the whitelist takes precedence over this list.")
					.translation("neverwhere.config.block_reflection_blacklist")
					.defineList("reflection_blacklisted_blocks", Config.DEFAULT_BLACKLIST, obj -> obj instanceof String && ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(((String)obj))));
			
			builder.pop();
			
			builder.push("Portal Spawning");
			
			this.portal_spawn_on_grow_sapling_chance = builder
					.comment("Probability that using bonemeal on saplings spawns a tree-shaped hole in the universe instead of a tree")
					.translation("neverwhere.config.portal_spawn_on_grow_sapling_chance")
					.defineInRange("portal_spawn_on_grow_sapling_chance", 0.1D, 0.0D, 1.0D);
			this.portal_void_spawn_infrequency = builder
					.comment("Average time in ticks between incursions of yawning voids. Probability of one mysteriously appearing near a random player in a given tick is 1/x where x is this value")
					.translation("neverwhere.config.portal_void_spawn_infrequency")
					.defineInRange("portal_void_spawn_infrequency", 36000, 1, Integer.MAX_VALUE);
			
			builder.pop();
			
			builder.push("Misc");
			
			this.average_neverwhere_timeout = builder
					.comment("Average time in ticks in the neverwhere before being booted back to the overworld")
					.translation("neverwhere.config.average_neverwhere_timeout")
					.defineInRange("average_neverwhere_timeout", 5000, 1, Integer.MAX_VALUE);
			
			builder.pop();
		}
	}
}
