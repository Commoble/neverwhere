package com.github.commoble.neverwhere;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import com.github.commoble.neverwhere.dimension.NeverwhereModDimension;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.Potions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionExpiryEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(value = Neverwhere.MODID)
public class Neverwhere
{
	// registry keys
	public static final String MODID = "neverwhere";
	public static final String NEVERPORTAL = "neverportal";
	public static final String TELEPORTER = "teleporter";
	public static final String NUGGET = "nugget_of_regret";
	public static final String NEVERWAS = "neverwas";
	public static final String NEVERWAS_SPAWN_EGG = "neverwas_spawn_egg";
	public static final String WIND = "wind";
	public static final String TREE = "tree_hole";
	public static final String INSTANT_XP = "instant_xp";
	public static final String REGRET_EFFECT = "regret";
	public static final String LONG_REGRET_EFFECT = "long_regret";

	// registry objects
	public static final RegistryObject<Block> neverPortalBlock = makeRegistryObject(NEVERPORTAL,
			ForgeRegistries.BLOCKS);

	public static final RegistryObject<Item> neverPortalItem = makeRegistryObject(NEVERPORTAL, ForgeRegistries.ITEMS);
	public static final RegistryObject<Item> teleporterItem = makeRegistryObject(TELEPORTER, ForgeRegistries.ITEMS);
	public static final RegistryObject<Item> neverWasSpawnEggItem = makeRegistryObject(NEVERWAS_SPAWN_EGG,
			ForgeRegistries.ITEMS);
	public static final RegistryObject<Item> nuggetItem = makeRegistryObject(NUGGET, ForgeRegistries.ITEMS);

	public static final RegistryObject<Effect> instantXPEffect = makeRegistryObject(INSTANT_XP,
			ForgeRegistries.POTIONS);
	
	public static final RegistryObject<Potion> regretPotion = makeRegistryObject(REGRET_EFFECT, ForgeRegistries.POTION_TYPES);
	public static final RegistryObject<Potion> longRegretPotion = makeRegistryObject(LONG_REGRET_EFFECT, ForgeRegistries.POTION_TYPES);

	// public static final RegistryObject<TileEntityType<?>> neverPortalTEType =
	// makeRegistryObject(NEVERPORTAL,
	// ForgeRegistries.TILE_ENTITIES);

	public static final RegistryObject<EntityType<? extends NeverwasEntity>> neverwas = makeRegistryObject(NEVERWAS,
			ForgeRegistries.ENTITIES);

	public static final RegistryObject<SoundEvent> windSound = makeRegistryObject(WIND, ForgeRegistries.SOUND_EVENTS);

	public static final RegistryObject<AbstractTreeFeature<NoFeatureConfig>> wrongTree = makeRegistryObject(TREE,
			ForgeRegistries.FEATURES);

	// dimension stuff
	public static final ResourceLocation NEVERWHERE_RESOURCE_LOCATION = new ResourceLocation(MODID, MODID);

	public static DimensionType getDimensionType()
	{
		return DimensionType.byName(NEVERWHERE_RESOURCE_LOCATION);
	}

	public static final RegistryObject<ModDimension> neverwhereModDimension = makeRegistryObject(MODID,
			ForgeRegistries.MOD_DIMENSIONS);

	public Neverwhere()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		modBus.addGenericListener(Block.class, multiObjectRegistrator(Neverwhere::onRegisterBlocks));
		modBus.addGenericListener(Item.class, multiObjectRegistrator(Neverwhere::onRegisterItems));
		modBus.addGenericListener(Effect.class, multiObjectRegistrator(Neverwhere::onRegisterEffects));
		modBus.addGenericListener(Potion.class, multiObjectRegistrator(Neverwhere::onRegisterPotions));
		modBus.addGenericListener(SoundEvent.class, multiObjectRegistrator(Neverwhere::onRegisterSounds));
		// modBus.addGenericListener(TileEntityType.class,
		// multiObjectRegistrator(Neverwhere::onRegisterTileEntities));
		modBus.addGenericListener(EntityType.class, multiObjectRegistrator(Neverwhere::onRegisterEntities));
		modBus.addGenericListener(ModDimension.class, singleObjectRegistrator(MODID, new NeverwhereModDimension()));
		modBus.addGenericListener(Feature.class, multiObjectRegistrator(Neverwhere::onRegisterFeatures));

		modBus.addListener(Neverwhere::onCommonSetup);

		forgeBus.addListener(Neverwhere::onRegisterDimensions);
		forgeBus.addListener(Neverwhere::onBlockPlaced);
		forgeBus.addListener(Neverwhere::onBlockBroken);
		forgeBus.addListener(Neverwhere::onPlayerTick);
		forgeBus.addListener(Neverwhere::onCheckSpawn);
		forgeBus.addListener(Neverwhere::onChunkLoad);
		forgeBus.addListener(Neverwhere::onChunkUnload);
		forgeBus.addListener(Neverwhere::onPlayerUsedBoneMeal);
		forgeBus.addListener(Neverwhere::onWorldTick);
//		forgeBus.addListener(Neverwhere::onPotionAdded);
		forgeBus.addListener(Neverwhere::onPotionRemoved);
		forgeBus.addListener(Neverwhere::onPotionExpired);

	}

	public static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> makeRegistryObject(
			final String name, IForgeRegistry<T> registry)
	{
		return RegistryObject.of(MODID + ":" + name, registry);
	}

	public static <T extends IForgeRegistryEntry<T>> Consumer<Register<T>> singleObjectRegistrator(String key, T entry)
	{
		entry.setRegistryName(new ResourceLocation(MODID, key));
		return registry -> registry.getRegistry().register(entry);
	}

	public static <T extends IForgeRegistryEntry<T>> Consumer<Register<T>> multiObjectRegistrator(
			Consumer<Registrator<T>> consumer)
	{
		return event -> consumer.accept(new Registrator<>(event.getRegistry()));
	}

	public static void onRegisterBlocks(Registrator<Block> reg)
	{
		reg.register(NEVERPORTAL, new NeverPortalBlock(Block.Properties.create(Material.PORTAL)
				.hardnessAndResistance(0.3F).sound(SoundType.GLASS).doesNotBlockMovement().noDrops().tickRandomly()));
	}

	// effects are needed during item instantiation at the moment but effects are
	// registered after items
	public static final Effect instantXPInstance = new InstantXPEffect(EffectType.BENEFICIAL, 0x01);
	public static final Effect regretEffect = new TeleportEffect(EffectType.BENEFICIAL, 0x051015);

	public static void onRegisterItems(Registrator<Item> reg)
	{
		reg.register(TELEPORTER, new TeleporterItem(new Item.Properties().group(ItemGroup.MISC)));
		reg.register(NEVERPORTAL,
				new BlockItem(neverPortalBlock.get(), new Item.Properties().group(ItemGroup.DECORATIONS)));

		Food NUGGET_FOOD = (new Food.Builder()).hunger(0).saturation(1F).fastToEat().setAlwaysEdible()
				.effect(new EffectInstance(instantXPInstance), 1F).build();
		reg.register(NUGGET, new Item(new Item.Properties().group(ItemGroup.FOOD).food(NUGGET_FOOD)));
		
		

		// TODO fix setup w.r.t. spawn egg and entity type
		// entity type is currently null when this is registered
		// the only place it's called in is getType which can be overridden here
		// but this is not guaranteed to work in the future
		reg.register(NEVERWAS_SPAWN_EGG,
				new SpawnEggItem(neverwas.get(), 0xEEEEEE, 0xE0e0e0, (new Item.Properties()).group(ItemGroup.MISC))
				{
					@Override
					public EntityType<?> getType(@Nullable CompoundNBT p_208076_1_)
					{
						return Neverwhere.neverwas.get();
					}
				});
	}

	public static void onRegisterEffects(Registrator<Effect> reg)
	{
		reg.register(INSTANT_XP, instantXPInstance);
		reg.register(REGRET_EFFECT, regretEffect);
	}
	
	public static void onRegisterPotions(Registrator<Potion> reg)
	{
		reg.register(REGRET_EFFECT, new Potion(REGRET_EFFECT, new EffectInstance(regretEffect, 3600)));
		reg.register(LONG_REGRET_EFFECT, new Potion(REGRET_EFFECT, new EffectInstance(regretEffect, 9600)));
	}

	public static void onRegisterSounds(Registrator<SoundEvent> reg)
	{
		reg.register(WIND, new SoundEvent(getModRL(WIND)));
	}

	// public static void onRegisterTileEntities(Registrator<TileEntityType<?>> reg)
	// {
	// reg.register(NEVERPORTAL,
	// TileEntityType.Builder.create(NeverPortalTileEntity::new,
	// neverPortalBlock.get()).build(null));
	// }

	public static void onRegisterEntities(Registrator<EntityType<?>> reg)
	{
		reg.register(NEVERWAS, EntityType.Builder.create(NeverwasEntity::new, EntityClassification.MONSTER)
				.build(getModRL(NEVERWAS).toString()));
	}

	public static void onRegisterFeatures(Registrator<Feature<?>> reg)
	{
		reg.register(TREE, new TreeFeature(NoFeatureConfig::deserialize, true, 4,
				neverPortalBlock.get().getDefaultState(), neverPortalBlock.get().getDefaultState(), false));
	}

	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(MODID, name);
	}

	public static String getStringWithDomain(String name)
	{
		return getModRL(name).toString();
	}

	public static void onCommonSetup(FMLCommonSetupEvent event)
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
		
		PotionBrewing.addMix(Potions.AWKWARD, Neverwhere.nuggetItem.get(), Neverwhere.regretPotion.get());
		PotionBrewing.addMix(Neverwhere.regretPotion.get(), Items.REDSTONE, Neverwhere.longRegretPotion.get());
	}

	public static void onRegisterDimensions(RegisterDimensionsEvent event)
	{
		if (getDimensionType() == null)
		{
			DimensionManager.registerDimension(NEVERWHERE_RESOURCE_LOCATION, neverwhereModDimension.get(), null, true);
		}
	}

	public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event)
	{
		IWorld world = event.getWorld();
		BlockState state = event.getState();
		// when a block is placed on the server overworld
		if (!world.isRemote() && world.getDimension().getType() == DimensionType.OVERWORLD
				&& !(event instanceof BlockEvent.EntityMultiPlaceEvent)
				&& world.getRandom().nextDouble() < Config.block_place_reflection_chance
				&& Config.isBlockStateAllowedToReflectPlacement(state))
		{
			BlockPos pos = event.getPos();

			MinecraftServer server = event.getEntity().getServer();

			Neverwhere.addBlockReflectionEntry(world, pos, state, server);
		}
	}

	public static void onBlockBroken(BlockEvent.BreakEvent event)
	{
		IWorld world = event.getWorld();
		// when a block is placed on the server overworld
		if (!world.isRemote() && world.getDimension().getType() == DimensionType.OVERWORLD
				&& world.getRandom().nextDouble() < Config.block_break_reflection_chance)
		{
			BlockPos pos = event.getPos();
			BlockState state = Blocks.AIR.getDefaultState();
			MinecraftServer server = event.getPlayer().getServer();

			Neverwhere.addBlockReflectionEntry(world, pos, state, server);
		}
	}

	private static void addBlockReflectionEntry(IWorld world, BlockPos pos, BlockState state, MinecraftServer server)
	{
		// getting the world in this manner returns null if tSSSShe world is not currently
		// loaded
		ServerWorld otherWorld = DimensionManager.getWorld(server, getDimensionType(), true, false);

		if (otherWorld != null && otherWorld.isBlockLoaded(pos))
		{
			otherWorld.setBlockState(pos, state);
		} else
		{
			NeverwhereReflectionData data = NeverwhereReflectionData.get(world);

			data.putReflection(server, pos, state);
		}
	}

	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		PlayerEntity player = event.player;
		World world = player.world;
		if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) // server stuff
		{
			UUID playerID = player.getUniqueID();
			int portalTime = PortalHelper.getTime(playerID);
			if (portalTime < 0)
			{
				PortalHelper.addTime(playerID, 1);
			}

			if (world.getDimension().getType() == Neverwhere.getDimensionType())
			{
				if (portalTime >= 0
						&& world.rand.nextInt(Config.average_neverwhere_timeout) == 0
						&& player.getActivePotionEffect(Neverwhere.regretEffect) == null)
				{
					PortalHelper.teleportPlayer((ServerPlayerEntity) player, x -> x);
				} else if (world.getDifficulty() != Difficulty.PEACEFUL)
				{
					// handle special spawning of neverwas
					// if at minimum xp threshold for spawning, average one mob/minute (once every
					// 1200 ticks)
					// if at minimum xp threshold for unprovoked attacking, average one mob every
					// ten seconds (once every 200 ticks)
					int spawn_threshold = Config.neverwas_spawn_threshold;
					// int attack_threshold = Config.neverwas_attack_threshold;
					int currentXP = player.experienceLevel;

					// spawn -> 0F, attack -> 1F
					// diff = attack-spawn
					// x = current level
					// slider = (x-spawn) / diff
					// (so we need to handle the case when spawn == attack)
					// if spawn == attack then checking xp >= attack first will be sufficient
					if (currentXP >= spawn_threshold || player.isCreative())
					{
						
						if (world.rand.nextInt(Config.neverwas_spawn_infrequency) == 0)
						{
							List<NeverwasEntity> moblist = ((ServerWorld)world)
								.getEntitiesWithinAABB(
									NeverwasEntity.class,
									player.getBoundingBox().grow(Config.neverwas_spawn_cap_distance));
							if (moblist.size() < Config.neverwas_spawn_local_cap)
							{
								NeverwasEntity.spawnNearPlayer(player);
							}
						}
					}
				}
			}
			else if (world.getDimension().getType() == DimensionType.OVERWORLD)
			{
				if (player.getActivePotionEffect(Neverwhere.regretEffect) != null)
				{
					ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
					PortalHelper.setTime(playerID, PortalHelper.COOLDOWN_AFTER_TELEPORT);
					PortalHelper.teleportPlayer(serverPlayer, Function.identity());
				}
			}
		}
	}

	private static final ToDoubleFunction<Random> voidPlacementOffset = rand -> (rand.nextDouble() * 8D + 8D)
			* (rand.nextBoolean() ? 1 : -1);
	private static final ToIntFunction<Random> voidBlockOffset = rand -> (rand.nextInt(4) - 2);

	public static void onWorldTick(WorldTickEvent event)
	{
		World world = event.world;
		if (!world.isRemote)
		{
			NeverwhereReflectionData data = NeverwhereReflectionData.get(world);

			Random rand = world.rand;

			if (rand.nextInt(Config.portal_void_spawn_infrequency) == 0)
			{
				List<? extends PlayerEntity> players = world.getPlayers();
				int playerCount = players.size();
				if (playerCount > 0)
				{
					PlayerEntity player = players.get(rand.nextInt(playerCount));

					double xOff = voidPlacementOffset.applyAsDouble(rand);
					double yOff = voidPlacementOffset.applyAsDouble(rand);
					double zOff = voidPlacementOffset.applyAsDouble(rand);

					BlockPos basePos = new BlockPos(player.getPositionVec().add(xOff, yOff, zOff));

					for (int i = 0; i < 6; i++)
					{
						int x = voidBlockOffset.applyAsInt(rand);
						int y = voidBlockOffset.applyAsInt(rand);
						int z = voidBlockOffset.applyAsInt(rand);
						BlockPos placePos = basePos.add(x, y, z);

						if (world.getBlockState(placePos).getBlock() == Blocks.AIR)
						{
							world.setBlockState(placePos, Neverwhere.neverPortalBlock.get().getDefaultState()
									.with(NeverPortalBlock.LEVEL, 8));
						}
					}
				}
			}

		}
	}

	// the Neverwhere uses all the biomes of the overworld
	// but we don't want overworld mobs to spawn
	// so we cancel the spawning event
	public static EnumSet<SpawnReason> deniedSpawnReasons = EnumSet.of(SpawnReason.NATURAL,
			SpawnReason.CHUNK_GENERATION, SpawnReason.STRUCTURE, SpawnReason.PATROL);

	public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event)
	{
		if (event.getWorld().getDimension().getType() == Neverwhere.getDimensionType())
		{
			if (deniedSpawnReasons.contains(event.getSpawnReason()))
			{
				event.setResult(Result.DENY);
				;
			}
		}
	}

	public static void onChunkLoad(ChunkEvent.Load event)
	{
		IWorld world = event.getWorld();
		if (world != null && !world.isRemote() && world.getDimension().getType() == Neverwhere.getDimensionType())
		{
			NeverwhereReflectionData data = NeverwhereReflectionData.get(world);
			IChunk chunk = event.getChunk();
			ChunkPos chunkPos = chunk.getPos();

			Map<BlockPos, BlockState> subMap = data.getAndClearChunkData(chunkPos);

			if (subMap.size() > 0)
			{
				subMap.forEach((blockPos, blockState) -> {
					BlockPos posInChunk = new BlockPos(blockPos.getX() & 15, blockPos.getY(), blockPos.getZ() & 15);
					if (!chunk.getBlockState(posInChunk).hasTileEntity()) // for complicated safety reasons
					{
						//chunk.setBlockState(posInChunk, blockState, false);
						Neverwhere.setBlockStateSafely(chunk, posInChunk, blockState);
						world.getPendingBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 1);
					}
				});
			}
		}
	}

	// as Chunk::setBlockState but without calls to onBlockAdded or onBlockReplaced
	public static void setBlockStateSafely(IChunk ichunk, BlockPos pos, BlockState newState)
	{
		if (!(ichunk instanceof Chunk))
		{
			return;
		}
		Chunk chunk = (Chunk) ichunk;
		int xInChunk = pos.getX() & 15;
		int y = pos.getY();
		int zInChunk = pos.getZ() & 15;
		ChunkSection[] chunkSections = chunk.getSections();
		ChunkSection chunkSection = chunkSections[y >> 4];
		if (chunkSection == Chunk.EMPTY_SECTION)
		{
			if (newState.isAir())
			{
				return;
			}

			chunkSection = new ChunkSection(y >> 4 << 4);
			chunkSections[y >> 4] = chunkSection;
		}

		boolean wasChunkSectionEmpty = chunkSection.isEmpty();
		BlockState oldState = chunkSection.setBlockState(xInChunk, y & 15, zInChunk, newState);
		if (oldState == newState)
		{
			return;
		}
		else
		{
			Block newBlock = newState.getBlock();
			Block oldBlock = oldState.getBlock();
			chunk.func_217303_b(Heightmap.Type.MOTION_BLOCKING).update(xInChunk, y, zInChunk, newState);
			chunk.func_217303_b(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).update(xInChunk, y, zInChunk, newState);
			chunk.func_217303_b(Heightmap.Type.OCEAN_FLOOR).update(xInChunk, y, zInChunk, newState);
			chunk.func_217303_b(Heightmap.Type.WORLD_SURFACE).update(xInChunk, y, zInChunk, newState);
			boolean isChunkSectionEmpty = chunkSection.isEmpty();
			if (wasChunkSectionEmpty != isChunkSectionEmpty)
			{
				chunk.getWorld().getChunkProvider().getLightManager().func_215567_a(pos, isChunkSectionEmpty);
			}

			if (chunkSection.getBlockState(xInChunk, y & 15, zInChunk).getBlock() != newBlock)
			{
				return;
			}
			else
			{
				chunk.markDirty();;
				return;
			}
		}
	}

	public static void onChunkUnload(ChunkEvent.Unload event)
	{
		IWorld world = event.getWorld();
		if (world != null && world instanceof ServerWorld && world.getDimension().getType() == DimensionType.OVERWORLD)
		{
			// load the chunk to trigger the chunkload event above
			ChunkPos chunkPos = event.getChunk().getPos();
			if (NeverwhereReflectionData.get(world).doesChunkNeedReflection(chunkPos))
			{
				IChunk chunkInNeverwhere = ((ServerWorld) world).getServer().getWorld(Neverwhere.getDimensionType())
						.getChunk(chunkPos.x, chunkPos.z);
			}
		}
	}

	public static void onPlayerUsedBoneMeal(BonemealEvent event)
	{
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = event.getBlock();
		Block block = state.getBlock();

		if (!world.isRemote && world.getDimension().getType() == DimensionType.OVERWORLD
				&& world.rand.nextDouble() < Config.portal_spawn_on_grow_sapling_chance
				&& block instanceof SaplingBlock)
		{
			SaplingBlock sapling = (SaplingBlock) block;

			if (sapling.canUseBonemeal(world, world.rand, pos, state))
			{
				VoidTree.INSTANCE.spawn(world, pos, state, world.rand);
				event.setResult(Result.ALLOW);
			}
		}
	}

//	public static void onPotionAdded(PotionAddedEvent event)
//	{
//		Entity entity = event.getEntity();
//		if (event.getPotionEffect().getPotion() == Neverwhere.regretEffect
//				&& (event.getOldPotionEffect() == null || event.getOldPotionEffect().getPotion() != Neverwhere.regretEffect)
//				&& entity instanceof ServerPlayerEntity
//				&& entity.world.getDimension().getType() == DimensionType.OVERWORLD)
//		{
//			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
//			PortalHelper.setTime(serverPlayer.getUniqueID(), PortalHelper.COOLDOWN_AFTER_TELEPORT);
//			PortalHelper.teleportPlayer(serverPlayer, Function.identity());
//		}
//	}
//	
	public static void onPotionRemoved(PotionRemoveEvent event)
	{
		onEndOfRegretPotion(event);
	}
	
	public static void onPotionExpired(PotionExpiryEvent event)
	{
		onEndOfRegretPotion(event);
	}
	
	private static void onEndOfRegretPotion(PotionEvent event)
	{
		Entity entity = event.getEntity();
		if (event.getPotionEffect().getPotion() == Neverwhere.regretEffect
				&& entity instanceof ServerPlayerEntity
				&& entity.world.getDimension().getType() == Neverwhere.getDimensionType())
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
			PortalHelper.setTime(serverPlayer.getUniqueID(), PortalHelper.COOLDOWN_AFTER_TELEPORT);
			PortalHelper.teleportPlayer(serverPlayer, Function.identity());
		}
	}
}
