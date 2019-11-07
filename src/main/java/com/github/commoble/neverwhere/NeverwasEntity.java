package com.github.commoble.neverwhere;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NeverwasEntity extends MonsterEntity
{
	private int angerLevel;
	private UUID angerTargetUUID;

	public NeverwasEntity(EntityType<? extends NeverwasEntity> entityTypeIn, World worldIn)
	{
		super(entityTypeIn, worldIn);
	}

	public static void spawnNearPlayer(PlayerEntity player)
	{
		World world = player.world;
		double x = player.posX + world.rand.nextDouble() * 32D - 16D;
		double z = player.posZ + world.rand.nextDouble() * 32D - 16D;
		double y = player.posY + world.rand.nextDouble() * 16D + 16D;
		;

		Neverwhere.neverwas.get().spawn(world, null, null, new BlockPos(x, y, z), SpawnReason.MOB_SUMMONED, false,
				false);

		// NeverwasEntity neverwas = Neverwhere.neverwas.get().create(world);
		// neverwas.setLocationAndAngles(x,y,z, player.rotationYaw, 0.0F);
		// world.addEntity(neverwas);
	}

	@Override
	protected void registerGoals()
	{
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
		this.goalSelector.addGoal(3,
				new AvoidEntityGoal<>(this, PlayerEntity.class, EntityPredicates.CAN_AI_TARGET::test, 32.0F, 1.5D, 1D,
						entity -> entity != this.getRevengeTarget() && entity instanceof PlayerEntity
								&& ((PlayerEntity) entity).experienceLevel < Config.neverwas_interest_threshold));
		this.goalSelector.addGoal(4,
				new AvoidEntityGoal<>(this, PlayerEntity.class, EntityPredicates.CAN_AI_TARGET::test, 16.0F, 1D, 0.5D,
						entity -> entity != this.getRevengeTarget() && entity instanceof PlayerEntity
								&& ((PlayerEntity) entity).experienceLevel >= Config.neverwas_interest_threshold
								&& ((PlayerEntity) entity).experienceLevel < Config.neverwas_follow_threshold));

		this.goalSelector.addGoal(5, new MoveTowardsInterestingPlayerGoal(this, 0.5D, 25f));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new NeverwasEntity.HurtByAggressorGoal(this));
		this.targetSelector.addGoal(2, new NeverwasEntity.TargetAggressorGoal(this));
		this.targetSelector.addGoal(3,
				new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, false, false,
						entity -> entity instanceof PlayerEntity
								&& ((PlayerEntity) entity).experienceLevel >= Config.neverwas_attack_threshold));
	}

	/**
	 * Hint to AI tasks that we were attacked by the passed EntityLivingBase and
	 * should retaliate. Is not guaranteed to change our actual active target (for
	 * example if we are currently busy attacking someone else)
	 */
	@Override
	public void setRevengeTarget(@Nullable LivingEntity livingBase)
	{
		super.setRevengeTarget(livingBase);
		if (livingBase != null)
		{
			this.angerTargetUUID = livingBase.getUniqueID();
		} else
		{
			this.angerTargetUUID = null;
		}

	}

	@Override
	public void fall(float distance, float damageMultiplier)
	{
		// NOPE immune to fall damage
	}

	@Override
	protected void registerAttributes()
	{
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4F);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(1.5D);
	}

	@Override
	protected void updateAITasks()
	{
		LivingEntity revengeTarget = this.getRevengeTarget();
		if (this.isAngry())
		{

			--this.angerLevel;
			LivingEntity attackTarget = revengeTarget != null ? revengeTarget : this.getAttackTarget();
			if (!this.isAngry() && attackTarget != null)
			{
				if (!this.canEntityBeSeen(attackTarget))
				{
					this.setRevengeTarget((LivingEntity) null);
					this.setAttackTarget((LivingEntity) null);
				} else
				{
					this.angerLevel = this.randomAngerLevel();
				}
			}
		}

		if (this.isAngry() && this.angerTargetUUID != null && revengeTarget == null)
		{
			PlayerEntity playerentity = this.world.getPlayerByUuid(this.angerTargetUUID);
			this.setRevengeTarget(playerentity);
			this.attackingPlayer = playerentity;
			this.recentlyHit = this.getRevengeTimer();
		}

		super.updateAITasks();
	}

	@Override
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		compound.putShort("Anger", (short) this.angerLevel);
		if (this.angerTargetUUID != null)
		{
			compound.putString("HurtBy", this.angerTargetUUID.toString());
		} else
		{
			compound.putString("HurtBy", "");
		}

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		this.angerLevel = compound.getShort("Anger");
		String s = compound.getString("HurtBy");
		if (!s.isEmpty())
		{
			this.angerTargetUUID = UUID.fromString(s);
			PlayerEntity playerentity = this.world.getPlayerByUuid(this.angerTargetUUID);
			this.setRevengeTarget(playerentity);
			if (playerentity != null)
			{
				this.attackingPlayer = playerentity;
				this.recentlyHit = this.getRevengeTimer();
			}
		}

	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (this.isInvulnerableTo(source))
		{
			return false;
		} else
		{
			Entity entity = source.getTrueSource();
			if (entity instanceof PlayerEntity && !((PlayerEntity) entity).isCreative() && this.canEntityBeSeen(entity))
			{
				this.becomeAngryAt(entity);
			}

			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return Neverwhere.windSound.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ENTITY_GHAST_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_GHAST_DEATH;
	}

	protected SoundEvent getStepSound()
	{
		return Neverwhere.windSound.get();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn)
	{
		this.playSound(this.getStepSound(), 0.15F, 1.0F);
	}

	@Override
	public CreatureAttribute getCreatureAttribute()
	{
		return CreatureAttribute.UNDEAD;
	}

	private boolean becomeAngryAt(Entity p_70835_1_)
	{
		this.angerLevel = this.randomAngerLevel();
		if (p_70835_1_ instanceof LivingEntity)
		{
			this.setRevengeTarget((LivingEntity) p_70835_1_);
		}

		return true;
	}

	private int randomAngerLevel()
	{
		return 400 + this.rand.nextInt(400);
	}

	private boolean isAngry()
	{
		return this.angerLevel > 0;
	}

	static class HurtByAggressorGoal extends HurtByTargetGoal
	{
		public HurtByAggressorGoal(NeverwasEntity p_i45828_1_)
		{
			super(p_i45828_1_);
			this.setCallsForHelp(new Class[] { ZombieEntity.class });
		}

		@Override
		protected void setAttackTarget(MobEntity mobIn, LivingEntity targetIn)
		{
			if (mobIn instanceof NeverwasEntity && this.goalOwner.canEntityBeSeen(targetIn)
					&& ((NeverwasEntity) mobIn).becomeAngryAt(targetIn))
			{
				mobIn.setAttackTarget(targetIn);
			}

		}
	}

	static class TargetAggressorGoal extends NearestAttackableTargetGoal<PlayerEntity>
	{
		public TargetAggressorGoal(NeverwasEntity p_i45829_1_)
		{
			super(p_i45829_1_, PlayerEntity.class, true);
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean shouldExecute()
		{
			return ((NeverwasEntity) this.goalOwner).isAngry() && super.shouldExecute();
		}
	}

	static class MoveTowardsInterestingPlayerGoal extends MoveTowardsTargetGoal
	{
		public static final EntityPredicate PLAYER_PREDICATE = new EntityPredicate()
				.setCustomPredicate(entity -> entity instanceof PlayerEntity
						&& ((PlayerEntity) entity).experienceLevel >= Config.neverwas_follow_threshold
						&& ((PlayerEntity) entity).experienceLevel < Config.neverwas_attack_threshold);

		public MoveTowardsInterestingPlayerGoal(CreatureEntity creature, double speedIn, float targetMaxDistance)
		{
			super(creature, speedIn, targetMaxDistance);
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean shouldExecute()
		{
			this.field_75429_b = this.getBestPlayerToMoveTo();
			if (this.field_75429_b == null)
			{
				return false;
			} else if (this.field_75429_b.getDistanceSq(this.creature) > this.maxTargetDistance
					* this.maxTargetDistance)
			{
				return false;
			} else
			{
				Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.creature, 16, 7,
						new Vec3d(this.field_75429_b.posX, this.field_75429_b.posY, this.field_75429_b.posZ));
				if (vec3d == null)
				{
					return false;
				} else
				{
					this.movePosX = vec3d.x;
					this.movePosY = vec3d.y;
					this.movePosZ = vec3d.z;
					return true;
				}
			}
		}

		@Nullable
		public PlayerEntity getBestPlayerToMoveTo()
		{
			return this.creature.world
					.getTargettablePlayersWithinAABB(PLAYER_PREDICATE, this.creature,
							this.creature.getBoundingBox().grow(this.maxTargetDistance, this.maxTargetDistance,
									this.maxTargetDistance))
					.stream() // players have already been filtered to be within the correct experience range
					.reduce((p1, p2) -> p2.experienceTotal > p1.experienceTotal ? p2 : p1).orElse(null);
		}
	}
}
