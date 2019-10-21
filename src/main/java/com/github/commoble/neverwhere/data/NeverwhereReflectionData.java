package com.github.commoble.neverwhere.data;

import java.util.HashMap;
import java.util.stream.IntStream;

import com.github.commoble.neverwhere.Neverwhere;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class NeverwhereReflectionData extends WorldSavedData
{
	private static final String DATA_NAME = Neverwhere.MODID + ":reflection_data";
	
	private static final String CHUNKS = "chunk";
	private static final String CHUNKPOS = "chunk";
	private static final String BLOCKS = "blocks";
	private static final String BLOCKPOS = "blockpos";
	private static final String BLOCKSTATE = "blockstate";
	
	private HashMap<ChunkPos, HashMap<BlockPos, BlockState>> map = new HashMap<>();
	
	private static final NeverwhereReflectionData CLIENT_DUMMY = new NeverwhereReflectionData();
	
	// get the data from the world saved data manager, instantiating it first if it doesn't exist
	public static NeverwhereReflectionData get(World world)
	{
		if (!(world instanceof ServerWorld))
		{
			return CLIENT_DUMMY;
		}
		
		ServerWorld overworld = world.getServer().getWorld(DimensionType.OVERWORLD);
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
	
	public void put(BlockPos blockPos, BlockState state)
	{
		ChunkPos chunkPos = new ChunkPos(blockPos);

		HashMap<BlockPos, BlockState> subMap;
		
		if (this.map.containsKey(chunkPos))
		{
			subMap = this.map.get(chunkPos);
		}
		else
		{
			subMap = this.map.put(chunkPos, new HashMap<BlockPos, BlockState>());
		}
		
		subMap.put(blockPos, state);
		
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
			ListNBT blockList = nbt.getList(BLOCKS, 10);
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
