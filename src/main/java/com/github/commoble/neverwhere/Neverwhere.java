package com.github.commoble.neverwhere;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.commoble.neverwhere.dimension.NeverwhereModDimension;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
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
	public static final String NEVERWAS = "neverwas";
	public static final String NEVERWAS_SPAWN_EGG = "neverwas_spawn_egg";

	// registry objects
	public static final RegistryObject<Block> neverPortalBlock = makeRegistryObject(NEVERPORTAL,
			ForgeRegistries.BLOCKS);

	public static final RegistryObject<Item> neverPortalItem = makeRegistryObject(NEVERPORTAL, ForgeRegistries.ITEMS);
	public static final RegistryObject<Item> teleporterItem = makeRegistryObject(TELEPORTER, ForgeRegistries.ITEMS);
	public static final RegistryObject<Item> neverWasSpawnEggItem = makeRegistryObject(NEVERWAS_SPAWN_EGG,
			ForgeRegistries.ITEMS);

	public static final RegistryObject<TileEntityType<?>> neverPortalTEType = makeRegistryObject(NEVERPORTAL,
			ForgeRegistries.TILE_ENTITIES);

	public static final RegistryObject<EntityType<? extends NeverwasEntity>> neverwas = makeRegistryObject(NEVERWAS,
			ForgeRegistries.ENTITIES);

	// dimension stuff
	public static final ResourceLocation NEVERWHERE_RESOURCE_LOCATION = new ResourceLocation(MODID, MODID);

	public static DimensionType getDimensionType()
	{
		return DimensionType.byName(NEVERWHERE_RESOURCE_LOCATION);
	}

	public static final RegistryObject<ModDimension> neverwhereModDimension = makeRegistryObject(MODID,
			ForgeRegistries.MOD_DIMENSIONS);

	public static final Optional<ClientProxy> CLIENTPROXY = Optional
			.ofNullable(DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> null));

	public Neverwhere()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		modBus.addGenericListener(Block.class, multiObjectRegistrator(Neverwhere::onRegisterBlocks));
		modBus.addGenericListener(Item.class, multiObjectRegistrator(Neverwhere::onRegisterItems));
		modBus.addGenericListener(TileEntityType.class, multiObjectRegistrator(Neverwhere::onRegisterTileEntities));
		modBus.addGenericListener(EntityType.class, multiObjectRegistrator(Neverwhere::onRegisterEntities));
		modBus.addGenericListener(ModDimension.class, singleObjectRegistrator(MODID, new NeverwhereModDimension()));

		forgeBus.addListener(Neverwhere::onRegisterDimensions);
		forgeBus.addListener(Neverwhere::onBlockPlaced);
		forgeBus.addListener(Neverwhere::onBlockBroken);
		forgeBus.addListener(Neverwhere::onPlayerTick);

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
		reg.register(NEVERPORTAL,
				new NeverPortalBlock(Block.Properties.create(Material.PORTAL).hardnessAndResistance(-1.0F, 3600000.0F)
						.doesNotBlockMovement().noDrops().tickRandomly().lightValue(7)));
	}

	public static void onRegisterItems(Registrator<Item> reg)
	{
		reg.register(TELEPORTER, new TeleporterItem(new Item.Properties().group(ItemGroup.MISC)));
		reg.register(NEVERPORTAL,
				new BlockItem(neverPortalBlock.get(), new Item.Properties().group(ItemGroup.DECORATIONS)));
		
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

	public static void onRegisterTileEntities(Registrator<TileEntityType<?>> reg)
	{
		reg.register(NEVERPORTAL,
				TileEntityType.Builder.create(NeverPortalTileEntity::new, neverPortalBlock.get()).build(null));
	}

	public static void onRegisterEntities(Registrator<EntityType<?>> reg)
	{
		reg.register(NEVERWAS,
				EntityType.Builder.create(NeverwasEntity::new, EntityClassification.MONSTER)
						.build(getModRL(NEVERWAS).toString()));
	}

	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(MODID, name);
	}

	public static String getStringWithDomain(String name)
	{
		return getModRL(name).toString();
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
		// when a block is placed on the server overworld
		if (!world.isRemote() && world.getDimension().getType() == DimensionType.OVERWORLD
				&& !event.getState().hasTileEntity())// && world.getRandom().nextFloat() < 0.2F)
		{
			if (!(event instanceof BlockEvent.EntityMultiPlaceEvent)) // dealing with these is more complicated than
																		// it's worth
			{
				BlockPos pos = event.getPos();
				MinecraftServer server = event.getEntity().getServer();
				ServerWorld otherWorld = server.getWorld(Neverwhere.getDimensionType());
				if (otherWorld.isBlockLoaded(pos))
				{
					otherWorld.setBlockState(event.getPos(), event.getState());
				} else
				{
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();
				}
			}
		}
	}

	public static void onBlockBroken(BlockEvent.BreakEvent event)
	{
		IWorld world = event.getWorld();
		// when a block is placed on the server overworld
		if (!world.isRemote() && world.getDimension().getType() == DimensionType.OVERWORLD)
		{
			event.getPlayer().getServer().getWorld(Neverwhere.getDimensionType()).removeBlock(event.getPos(), false);
		}
	}

	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		PlayerEntity player = event.player;
		World world = player.world;
		if (!world.isRemote) // server stuff
		{
			UUID playerID = player.getUniqueID();
			int portalTime = PortalHelper.getTime(playerID);
			if (portalTime < 0)
			{
				PortalHelper.addTime(playerID, 1);
			}
			if (portalTime == -1 && world.getDimension().getType() == Neverwhere.getDimensionType()
					&& !PortalHelper.displayedSpookyMessageRecently.contains(playerID))
			{
				player.sendStatusMessage(new TranslationTextComponent("You have no idea how alone you are, ")
						.appendSibling(player.getDisplayName()), true);
				PortalHelper.displayedSpookyMessageRecently.add(playerID);
			}
			if (portalTime >= 0 && world.rand.nextInt(100) == 0)
			{
				PortalHelper.displayedSpookyMessageRecently.remove(playerID);
			}
		}
	}
}
