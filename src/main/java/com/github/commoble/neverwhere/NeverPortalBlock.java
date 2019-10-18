package com.github.commoble.neverwhere;

import java.util.UUID;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NeverPortalBlock extends Block
{
	// these states are used to determine which faces of the cube to render
	// the TESR does some rendering that can't be done with a normal block model so it draws six faces instead
	// these properties return TRUE if there is a block on that side that should prevent rendering of that face
	public static final BooleanProperty DOWN = SixWayBlock.DOWN;
	public static final BooleanProperty UP = SixWayBlock.UP;
	public static final BooleanProperty NORTH = SixWayBlock.NORTH;
	public static final BooleanProperty SOUTH = SixWayBlock.SOUTH;
	public static final BooleanProperty WEST = SixWayBlock.WEST;
	public static final BooleanProperty EAST = SixWayBlock.EAST;
	
	public static final BooleanProperty[] propertiesByRenderDirection = {EAST, WEST, DOWN, UP, NORTH, SOUTH};
	
	public NeverPortalBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState()
				.with(NORTH, Boolean.valueOf(false))
				.with(EAST, Boolean.valueOf(false))
				.with(SOUTH, Boolean.valueOf(false))
				.with(WEST, Boolean.valueOf(false))
				.with(DOWN, Boolean.valueOf(false))
				.with(UP, Boolean.valueOf(false)));
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos)
	{
		return stateIn.with(SixWayBlock.FACING_TO_PROPERTY_MAP.get(facing),
				Boolean.valueOf(this.doesSideBlockRendering(worldIn, facingPos, facingState, facing)));
	}
	
	public boolean doesSideBlockRendering(IWorld world, BlockPos blockingPos, BlockState blockingState, Direction sideOfPortal)
	{
		return (blockingState.getBlock() == Neverwhere.neverPortalBlock.get()
				||
				(blockingState.isOpaqueCube(world, blockingPos))
				
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST);
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return Neverwhere.neverPortalTEType.get().create();
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.INVISIBLE;
	}

	@Override
	@Deprecated
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentState, Direction side)
	{
		Block adjacentBlock = adjacentState.getBlock();
		return adjacentBlock == Neverwhere.neverPortalBlock.get() || super.isSideInvisible(state, adjacentState, side);
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
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity)entityIn;
			UUID playerID = serverPlayer.getUniqueID();
			int portalTime = PortalHelper.addTime(playerID, 2); // add 2 ticks because the player tick event is subtracting 1
			if (portalTime >= PortalHelper.TICKS_BEFORE_TELEPORT)	// time to teleport
			{
				PortalHelper.setTime(playerID, PortalHelper.COOLDOWN_AFTER_TELEPORT);
				PortalHelper.teleportPlayer(serverPlayer, Function.identity());
			}
			else if (portalTime < 0 && portalTime > PortalHelper.GRACE_PERIOD_RESET)
			{
				PortalHelper.setTime(playerID, PortalHelper.GRACE_PERIOD_RESET);
			}
		}
	}
}
