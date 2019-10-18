package com.github.commoble.neverwhere.dimension;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NeverwhereDimension extends OverworldDimension
{
	public static final Vec3d FOG_COLOR = new Vec3d(0F, 0F, 0F);

	public NeverwhereDimension(World worldIn, DimensionType typeIn)
	{
		super(worldIn, typeIn);
	}

	@Override
	@Nullable
	public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid)
	{
		return null;
	}

	@Override
	@Nullable
	public BlockPos findSpawn(int posX, int posZ, boolean checkValid)
	{
		return null;
	}

	/**
	 * Calculates the angle of sun and moon in the sky relative to a specified time
	 * (usually worldTime)
	 */
	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks)
	{
		return 0.5F;
	}

	/**
	 * Returns 'true' if in the "main surface world", but 'false' if in the Nether
	 * or End dimensions.
	 */
	@Override
	public boolean isSurfaceWorld()
	{
		return false;
	}

	/**
	 * Return Vec3D with biome specific fog color
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public Vec3d getFogColor(float celestialAngle, float partialTicks)
	{
		return FOG_COLOR;
	}

	/**
	 * Returns true if the given X,Z coordinate should show environmental fog.
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean doesXZShowFog(int p_76568_1_, int p_76568_2_)
	{
		return true;
	}

	/**
	 * True if the player can respawn in this dimension (true = overworld, false =
	 * nether).
	 */
	@Override
	public boolean canRespawnHere()
	{
		return false;
	}

	/**
	 * Creates the light to brightness table
	 */
	@Override
	protected void generateLightBrightnessTable()
	{
		float offset = -0.1F;

		for (int i = 0; i <= 15; ++i)
		{
			float f1 = 1F - i / 15F;
			this.lightBrightnessTable[i] = (1F - f1) / (f1 * 3F + 1F) + offset;
		}

	}

	@Override
	public boolean isDaytime()
	{
		return false;
	}

	@Override
	public float getSunBrightness(float partialTicks)
	{
		return 0F;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vec3d getSkyColor(BlockPos cameraPos, float partialTicks)
	{
		return FOG_COLOR;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vec3d getCloudColor(float partialTicks)
	{
		return FOG_COLOR;
	}

	/**
	 * Gets the Star Brightness for rendering sky.
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getStarBrightness(float partialTicks)
	{
		return 0F;
	}

	@Override
	public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful)
	{
		super.setAllowedSpawnTypes(allowHostile, false);
	}

//	/**
//     * A message to display to the user when they transfer to this dimension.
//    *
//    * @return The message to be displayed
//    */
//   public String getWelcomeMessage()
//   {
//       switch(this.worldObj.rand.nextInt(10))
//       {
//       case 0:
//       	return "You feel an unsettling presence";
//       case 1:
//       	return "You feel a looming presence";
//       case 2:
//       	return "You feel a cold presence";
//       case 3:
//       	return "You feel a disturbing presence";
//       case 4:
//       	return "You feel an empty presence";
//       case 5:
//       	return "You feel lost";
//       case 6:
//       	return "You feel as if you're being watched";
//       case 7:
//       	return "You feel like something's missing";
//       case 8:
//       	return "You feel unwelcome";
//       case 9:
//       	return "You feel as if you've fallen a great distance";
//       default:
//       	return "Entering Neverwhere";
//       }
}
