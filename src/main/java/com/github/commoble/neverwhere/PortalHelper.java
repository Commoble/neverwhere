package com.github.commoble.neverwhere;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;


/**
 * Some general rules:
 * The player will be teleported after N ticks in the portal (currently 5)
 * The player can teleport after time >= N
 * Once the player has teleported, time will be set to -300
 * If time < 0, the time will be incremented in the player tick event
 */
public class PortalHelper
{
	private static final Object2IntOpenHashMap<UUID> timers = new Object2IntOpenHashMap<UUID>();
	
	public static final Set<UUID> displayedSpookyMessageRecently = new HashSet<UUID>();
	
	public static final int TICKS_BEFORE_TELEPORT = 5;
	public static final int COOLDOWN_AFTER_TELEPORT = -300;
	public static final int GRACE_PERIOD_RESET = -80;
	
	/** Set a player's portal timer to the given time **/
	public static void setTime(UUID playerID, int time)
	{
		timers.put(playerID, time);
	}
	
	/** Retrieve the player's current portal time (returns 0 if the time has not been set) **/
	public static int getTime(UUID playerID)
	{
		return timers.getInt(playerID);
	}
	
	/** Adds the given time to the player's current time (which is 0 if it has not previously been set), and returns the new time value for that player **/
	public static int addTime(UUID playerID, int time)
	{
		int newTime = timers.getInt(playerID) + time;
		timers.put(playerID, newTime);
		return newTime;
	}
	
	/** The given Function maps the player's position in the old world to the position in the new world **/
	public static void teleportPlayer(ServerPlayerEntity serverPlayer, Function<BlockPos, BlockPos> positionMapper)
	{
		DimensionType dim;
		if (serverPlayer.world.dimension.getType() == Neverwhere.getDimensionType())
		{
			dim = DimensionType.OVERWORLD;
		}
		else
		{
			dim = Neverwhere.getDimensionType(); 
		}
		BlockPos newPos = positionMapper.apply(serverPlayer.getPosition());
		NeverwhereTeleporter.teleportPlayer(serverPlayer, dim, newPos);
	}
}
