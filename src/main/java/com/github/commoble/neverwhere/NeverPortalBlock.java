package com.github.commoble.neverwhere;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class NeverPortalBlock extends Block
{
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_0_8;

	public NeverPortalBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(LEVEL, Integer.valueOf(8)));
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, worldIn.rand.nextInt(5) + 1);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(LEVEL);
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random)
	{
		worldIn.playSound(null, pos, Neverwhere.windSound.get(), SoundCategory.AMBIENT, worldIn.rand.nextFloat(),
				worldIn.rand.nextFloat());
		BlockPos up = pos.up();
		float brightness = worldIn.getBrightness(up);
		int level = state.get(LEVEL);
		if (level > 0)
		{
			Direction dir = Direction.byIndex(worldIn.rand.nextInt(6));
			BlockPos nextPos = pos.offset(dir);
			BlockState nextState = worldIn.getBlockState(nextPos);
			BlockPos posToSet;
			if (nextState.getBlock() == Blocks.AIR && worldIn.rand.nextFloat() < brightness *1.5F)
			{
				posToSet = nextPos;
			} else
			{
				posToSet = pos;
			}
			worldIn.setBlockState(posToSet,
					Block.getValidBlockForPosition(state.with(LEVEL, level - 1), worldIn, posToSet));
			worldIn.getPendingBlockTicks().scheduleTick(posToSet, this, worldIn.rand.nextInt(5)+1);
		} else// if (random.nextFloat() < brightness)
		{
			Arrays.stream(Direction.values()).forEach(dir -> {
				if (random.nextFloat() * 1.5F < brightness)
				{
					BlockPos adjacentPos = pos.offset(dir);
					worldIn.getPendingBlockTicks().scheduleTick(adjacentPos,
							worldIn.getBlockState(adjacentPos).getBlock(), random.nextInt(5)+1);
				}
			});

			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return VoxelShapes.fullCube();
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		return true;
	}

	/**
	 * Called When an Entity Collided with the Block
	 */
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		// server stuff
		if (!worldIn.isRemote && entityIn instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entityIn;
			UUID playerID = serverPlayer.getUniqueID();
			int portalTime = PortalHelper.addTime(playerID, 2); // add 2 ticks because the player tick event is
																// subtracting 1
			if (portalTime >= PortalHelper.TICKS_BEFORE_TELEPORT) // time to teleport
			{
				PortalHelper.setTime(playerID, PortalHelper.COOLDOWN_AFTER_TELEPORT);
				PortalHelper.teleportPlayer(serverPlayer, Function.identity());
			} else if (portalTime < 0 && portalTime > PortalHelper.GRACE_PERIOD_RESET)
			{
				PortalHelper.setTime(playerID, PortalHelper.GRACE_PERIOD_RESET);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(LEVEL, 8);
	}

}
