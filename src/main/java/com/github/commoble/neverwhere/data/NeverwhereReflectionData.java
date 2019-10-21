package com.github.commoble.neverwhere.data;

import java.util.HashMap;
import java.util.stream.IntStream;

import com.github.commoble.neverwhere.Config;
import com.github.commoble.neverwhere.Neverwhere;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
	
	private HashMap<ChunkPos, HashMap<BlockPos, BlockState>> map = new HashMap<>();
	
	private static final NeverwhereReflectionData CLIENT_DUMMY = new NeverwhereReflectionData();
	
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
	public void put(MinecraftServer server, BlockPos blockPos, BlockState state)
	{
		ChunkPos chunkPos = new ChunkPos(blockPos);

		HashMap<BlockPos, BlockState> subMap;
		
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
	
	public HashMap<BlockPos, BlockState> getAndClearChunkData(ChunkPos chunkPos)
	{
		HashMap<BlockPos, BlockState> subMap;
		
		HashMap<BlockPos, BlockState> emptyMap = new HashMap<>();
		
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
		HashMap<ChunkPos, HashMap<BlockPos, BlockState>> newMap = new HashMap<>();
		
		ListNBT chunkList = nbt.getList(CHUNKS, 10);
		int chunkListSize = chunkList.size();
		
		IntStream.range(0,chunkListSize).mapToObj(chunkIterator -> chunkList.getCompound(chunkIterator)).forEach(chunkNBT -> 
		{
			ChunkPos chunkPos = new ChunkPos(chunkNBT.getLong(CHUNKPOS));
			ListNBT blockList = chunkNBT.getList(BLOCKS, 10);
			int blockListSize = blockList.size();
			
			HashMap<BlockPos, BlockState> subMap = new HashMap<>();
			
			IntStream.range(0, blockListSize).mapToObj(blockIterator -> blockList.getCompound(blockIterator)).forEach(blockNBT ->
			{
				BlockPos blockPos = NBTUtil.readBlockPos(blockNBT.getCompound(BLOCKPOS));
				BlockState state = NBTUtil.readBlockState(blockNBT.getCompound(BLOCKSTATE));
				
				subMap.put(blockPos, state);
			});
			
			newMap.put(chunkPos, subMap);
		});
		
		this.map = newMap;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		ListNBT listOfChunks = new ListNBT();
		this.map.entrySet().forEach(entry ->
		{
			ChunkPos chunkPos = entry.getKey();
			HashMap<BlockPos, BlockState> subMap = entry.getValue();
			
			ListNBT listOfBlocks = new ListNBT();
			
			subMap.entrySet().forEach(subEntry ->
			{
				BlockPos pos = subEntry.getKey();
				BlockState state = subEntry.getValue();
				
				CompoundNBT nbtForBlock = new CompoundNBT();
				nbtForBlock.put(BLOCKPOS, NBTUtil.writeBlockPos(pos));
				nbtForBlock.put(BLOCKSTATE, NBTUtil.writeBlockState(state));
				
				listOfBlocks.add(nbtForBlock);
			});
			
			CompoundNBT nbtForChunk = new CompoundNBT();
			nbtForChunk.putLong(CHUNKPOS, chunkPos.asLong());
			nbtForChunk.put(BLOCKS, listOfBlocks);
			
			listOfChunks.add(nbtForChunk);
		});
		
		compound.put(CHUNKS, listOfChunks);
		
		return compound;
	}

}
