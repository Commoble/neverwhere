package com.github.commoble.neverwhere.data;

import java.util.HashMap;
import java.util.Map;

import com.github.commoble.neverwhere.Config;
import com.github.commoble.neverwhere.NBTMapHelper;
import com.github.commoble.neverwhere.Neverwhere;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class NeverwhereReflectionData extends WorldSavedData
{
	private static final String DATA_NAME = Neverwhere.MODID + ":reflection_data";
	
	private static final String CHUNKS = "chunks";
	private static final String CHUNKPOS = "chunkpos";
	private static final String BLOCKS = "blocks";
	private static final String BLOCKPOS = "blockpos";
	private static final String BLOCKSTATE = "blockstate";
	
	private Map<ChunkPos, Map<BlockPos, BlockState>> map = new HashMap<>();
	
	private static final NeverwhereReflectionData CLIENT_DUMMY = new NeverwhereReflectionData();
	
	private static final NBTMapHelper<BlockPos, BlockState> BLOCKS_MAPPER = new NBTMapHelper<>(
			BLOCKS,
			(nbt, blockPos) -> nbt.put(BLOCKPOS, NBTUtil.writeBlockPos(blockPos)),
			nbt -> NBTUtil.readBlockPos(nbt.getCompound(BLOCKPOS)),
			(nbt, blockState) -> nbt.put(BLOCKSTATE, NBTUtil.writeBlockState(blockState)),
			nbt -> NBTUtil.readBlockState(nbt.getCompound(BLOCKSTATE))
			);
	
	private static final NBTMapHelper<ChunkPos, Map<BlockPos, BlockState>> CHUNKS_MAPPER = new NBTMapHelper<ChunkPos, Map<BlockPos, BlockState>>(
			CHUNKS,
			(nbt, chunkPos) -> nbt.putLong(CHUNKPOS, chunkPos.asLong()),
			nbt -> new ChunkPos(nbt.getLong(CHUNKPOS)),
			(nbt, map) -> BLOCKS_MAPPER.write(map, nbt),
			nbt -> BLOCKS_MAPPER.read(nbt)
			);
	
	// get the data from the world saved data manager, instantiating it first if it doesn't exist
	public static NeverwhereReflectionData get(IWorld world)
	{
		if (!(world instanceof ServerWorld))
		{
			return CLIENT_DUMMY;
		}
		
		ServerWorld overworld = ((ServerWorld)world).getServer().getWorld(DimensionType.OVERWORLD);
		DimensionSavedDataManager storage = overworld.getSavedData();
		return storage.getOrCreate(NeverwhereReflectionData::new, DATA_NAME);
	}
	
	public NeverwhereReflectionData()
	{
		this(DATA_NAME);
	}
	
	public NeverwhereReflectionData(String name)
	{
		super(name);
	}
	/**
	 * Puts a (blockpos, blockstate) entry into the submap for the chunk that contains that blockpos
	 * If the submap's size exceeds the configurable limit after this operation,
	 * then the map's contents are dumped into the relevant chunk in the Neverwhere
	 */
	public void putReflection(MinecraftServer server, BlockPos blockPos, BlockState state)
	{
		ChunkPos chunkPos = new ChunkPos(blockPos);

		Map<BlockPos, BlockState> subMap;
		
		if (this.map.containsKey(chunkPos))
		{
			subMap = this.map.get(chunkPos);
		}
		else
		{
			subMap = new HashMap<BlockPos, BlockState>();
			this.map.put(chunkPos, subMap);
		}
		
		subMap.put(blockPos, state);
		
		if (subMap.size() > Config.reflection_buffer_size)
		{
			ServerWorld neverwhereWorld = server.getWorld(Neverwhere.getDimensionType());
			neverwhereWorld.getChunk(blockPos);	// triggers the chunkload event
		}
		
		this.markDirty();
	}
	
	public Map<BlockPos, BlockState> getAndClearChunkData(ChunkPos chunkPos)
	{
		Map<BlockPos, BlockState> subMap;
		
		Map<BlockPos, BlockState> emptyMap = new HashMap<>();
		
		if (this.map.containsKey(chunkPos))
		{
			subMap = this.map.get(chunkPos);
			this.map.put(chunkPos, emptyMap);
			this.markDirty();
			
			return subMap;
		}
		else
		{
			return emptyMap;
		}
	}

	@Override
	public void read(CompoundNBT nbt)
	{
		this.map = CHUNKS_MAPPER.read(nbt);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		CHUNKS_MAPPER.write(this.map, compound);
		return compound;
	}

}
