package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.accessor.LevelAccessor;
import com.github.litermc.vsfreerotation.impl.EntityNodeProvider;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;
import com.github.litermc.vsfreerotation.util.CollisionUtil;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity implements EntityAccessor {
	@Unique
	private static final EntityDataAccessor<Boolean> FREE_ROTATION_FLAG = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	@Unique
	private static final EntityDataAccessor<Boolean> BODY_ROTATION_LOCKED = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	@Unique
	private static final float BODY_ROT_CLAMP = 50 * Mth.DEG_TO_RAD;

	@Shadow
	@Final
	protected SynchedEntityData entityData;
	@Shadow
	public boolean noPhysics;
	@Shadow
	public boolean wasOnFire;
	@Shadow
	public boolean isInPowderSnow;
	@Shadow
	protected Vec3 stuckSpeedMultiplier;
	@Shadow
	public boolean horizontalCollision;
	@Shadow
	public boolean verticalCollision;
	@Shadow
	public boolean verticalCollisionBelow;
	@Shadow
	public boolean minorHorizontalCollision;
	@Shadow
	public float walkDist;
	@Shadow
	public float moveDist;
	@Shadow
	public float flyDist;
	@Shadow
	private float nextStep;

	@Unique
	private FreeEntity<?> freeEntity = null;

	@Unique
	private boolean setPosRawFlag = false;
	@Unique
	private boolean setDeltaMovementFlag = false;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(final EntityType<?> type, final Level level, final CallbackInfo ci) {
		this.entityData.define(FREE_ROTATION_FLAG, false);
		this.entityData.define(BODY_ROTATION_LOCKED, false);

		if (!level.isClientSide()) {
			this.freeEntity = FreeEntity.createServer((Entity) (Object) this);
		}
	}

	@Shadow
	public abstract Level level();

	@Shadow
	public abstract Vec3 position();

	@Shadow
	public abstract AABB getBoundingBox();

	@Shadow
	public abstract boolean isRemoved();

	@Shadow
	public abstract boolean onGround();

	@Shadow
	public abstract boolean isOnFire();

	@Shadow
	public abstract boolean isInWater();

	@Shadow
	public abstract boolean isInWaterRainOrBubble();

	@Shadow
	public abstract boolean isPassenger();

	@Shadow
	public abstract void setPosRaw(double x, double y, double z);

	@Shadow
	public abstract void setPos(double x, double y, double z);

	@Shadow
	public abstract Vec3 getDeltaMovement();

	@Shadow
	public abstract void setDeltaMovement(Vec3 movement);

	@Shadow
	public abstract void setDeltaMovement(double x, double y, double z);

	@Shadow
	public abstract Pose getPose();

	@Shadow
	public abstract EntityDimensions getDimensions(Pose pose);

	@Shadow
	public abstract float getBbHeight();

	@Shadow
	public abstract float getEyeHeight();

	@Shadow
	public abstract Vec3 getEyePosition(float partialTick);

	@Shadow
	protected abstract float nextStep();

	@Shadow
	protected abstract void waterSwimSound();

	@Shadow
	protected abstract void playEntityOnFireExtinguishedSound();

	@Shadow
	protected abstract boolean vibrationAndSoundEffectsFromBlock(BlockPos pos, BlockState state, boolean playStepSound, boolean broadcastGameEvent, Vec3 vec3);

	@Shadow
	protected abstract void tryCheckInsideBlocks();

	@Shadow
	public abstract int getRemainingFireTicks();

	@Shadow
	public abstract void setRemainingFireTicks(int remainingFireTicks);

	@Shadow
	protected abstract Vec3 limitPistonMovement(Vec3 movement);

	@Shadow
	public abstract void setOnGroundWithKnownMovement(boolean onGround, Vec3 movement);

	@Shadow
	public abstract BlockPos getOnPos();

	@Shadow
	public abstract BlockPos getOnPosLegacy();

	@Shadow
	protected abstract Entity.MovementEmission getMovementEmission();

	@Shadow
	protected abstract int getFireImmuneTicks();

	@Shadow
	public abstract void gameEvent(GameEvent event);

	@Override
	public FreeEntity<?> vsfr$getFreeEntity() {
		return this.freeEntity;
	}

	@Override
	public void vsfr$setFreeEntity(final FreeEntity<?> fe) {
		this.freeEntity = fe;
	}

	@Override
	public boolean vsfr$isFreeRotationEnabled() {
		return this.entityData.get(FREE_ROTATION_FLAG);
	}

	@Override
	public void vsfr$setFreeRotationFlag(final boolean flag) {
		this.entityData.set(FREE_ROTATION_FLAG, flag);
	}

	@Override
	public boolean vsfr$isBodyRotationLocked() {
		return this.entityData.get(BODY_ROTATION_LOCKED);
	}

	@Override
	public void vsfr$setBodyRotationLock(final boolean lock) {
		this.entityData.set(BODY_ROTATION_LOCKED, lock);
	}

	@Inject(method = "setRemoved", at = @At("RETURN"))
	public void setRemoved(final Entity.RemovalReason reason, final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		this.freeEntity = null;
		final EntityNodeProvider provider = ((LevelAccessor) this.level()).vsfr$getEntityNodeProvider();
		provider.removeNode(fe.getId());
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	public void setLevel(final Level newLevel, final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final Level oldLevel = this.level();
		if (oldLevel == newLevel) {
			return;
		}
		if (oldLevel != null) {
			final EntityNodeProvider oldProvider = ((LevelAccessor) oldLevel).vsfr$getEntityNodeProvider();
			oldProvider.removeNode(fe.getId());
		}
		if (newLevel != null) {
			final EntityNodeProvider newProvider = ((LevelAccessor) newLevel).vsfr$getEntityNodeProvider();
			newProvider.putFreeEntity(fe);
		}
	}

	@Inject(method = "setOldPosAndRot", at = @At("RETURN"))
	public void setOldPosAndRot(final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		fe.setOldPosAndRot();
	}

	@Override
	public void vsfr$raw$setPosRaw(final double x, final double y, final double z) {
		this.setPosRawFlag = true;
		try {
			this.setPosRaw(x, y, z);
		} finally {
			this.setPosRawFlag = false;
		}
	}

	@Inject(method = "setPosRaw", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;position:Lnet/minecraft/world/phys/Vec3;", opcode = Opcodes.PUTFIELD))
	public void setPosRaw(final double x, final double y, final double z, final CallbackInfo ci) {
		if (this.setPosRawFlag) {
			return;
		}
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final Matrix4dc anchor = fe.getAnchorTransform().invert();
		fe.setPosition(anchor.transformPosition(new Vector3d(x, y, z)).add(0, this.getBbHeight() / 2, 0));
	}

	@Override
	public void vsfr$raw$setDeltaMovement(final Vec3 movement) {
		this.setDeltaMovementFlag = true;
		try {
			this.setDeltaMovement(movement);
		} finally {
			this.setDeltaMovementFlag = false;
		}
	}

	@Inject(method = "setDeltaMovement", at = @At("HEAD"))
	public void setDeltaMovement(final Vec3 movement, final CallbackInfo ci) {
		if (this.setDeltaMovementFlag) {
			return;
		}
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final Matrix4dc anchor = fe.getAnchorTransform().invert();
		fe.setMovement(anchor.transformDirection(movement.x, movement.y, movement.z, new Vector3d()));
	}

	@WrapMethod(method = "setPose")
	public void setPose(final Pose newPose, final Operation<Void> operation) {
		final Pose oldPose = this.getPose();
		operation.call(newPose);
		if (oldPose == newPose) {
			return;
		}

		if (!this.level().isClientSide) {
			return;
		}
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final float oldHeight = this.getDimensions(oldPose).height;
		final float newHeight = this.getDimensions(newPose).height;
		if (oldHeight == newHeight) {
			return;
		}
		if (fe.getAnchor() == null) {
			return;
		}
		final Matrix4dc anchor = fe.getAnchorTransform().invert();
		final Vec3 pos = this.position();
		fe.setPosition(
			anchor.transformPosition(new Vector3d(pos.x, pos.y, pos.z))
				.add(fe.getRotation().transform(new Vector3d(0, newHeight / 2, 0)))
		);
	}

	@Inject(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
	public final void getEyePosition(final CallbackInfoReturnable<Vec3> cir) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		cir.setReturnValue(this.getEyePosition(1));
	}

	@Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
	public final void getEyePosition(final float partialTick, final CallbackInfoReturnable<Vec3> cir) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		final Vector3d position = fe.getAbsPosition(partialTick);
		final double eyeHeight = this.getEyeHeight() - this.getBbHeight() / 2.0;
		if (eyeHeight != 0) {
			position.add(fe.getAbsRotation(partialTick).transform(new Vector3d(0, eyeHeight, 0)));
		}
		cir.setReturnValue(new Vec3(position.x, position.y, position.z));
	}

	@Override
	public void vsfr$readTurnKeybinds(final TurningContext context) {
	}

	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	public void turn(final double x, final double y, final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		ci.cancel();

		final FreeLivingEntity<?> fle = this.freeEntity instanceof final FreeLivingEntity<?> e ? e : null;

		// TODO: prevent / clamp turning when going to hit block

		final TurningContext context = new TurningContext();
		context.lockBodyRotate = fe.isBodyRotationLocked();
		context.lockHeadRotate = fle == null || fle.isHeadRotationLocked();
		this.vsfr$readTurnKeybinds(context);

		if (context.resetHead) {
			if (fle != null) {
				fle.setHeadPitch(0);
			}
			return;
		}

		final float roll = context.roll;
		final boolean lockBodyRotate = context.lockBodyRotate;
		final boolean lockHeadRotate = context.lockHeadRotate;

		if (x != 0 || y != 0 || roll != 0) {
			final float yaw = -((float) x) * 0.15f * Mth.DEG_TO_RAD;
			final float pitch = ((float) y) * 0.15f * Mth.DEG_TO_RAD;

			final Quaternionf relRotation = new Quaternionf();
			if (lockHeadRotate) {
				relRotation.rotationY(yaw);
				if (!lockBodyRotate) {
					relRotation.rotateX(pitch);
				}
			} else if (fle != null) {
				final float oldHeadYaw = fle.getHeadYaw();
				final float oldHeadPitch = fle.getHeadPitch();

				float bodyYawChange = 0;
				float newHeadYaw = oldHeadYaw + yaw;
				if (newHeadYaw > BODY_ROT_CLAMP) {
					bodyYawChange = newHeadYaw - BODY_ROT_CLAMP;
					newHeadYaw = BODY_ROT_CLAMP;
				} else if (newHeadYaw < -BODY_ROT_CLAMP) {
					bodyYawChange = newHeadYaw + BODY_ROT_CLAMP;
					newHeadYaw = -BODY_ROT_CLAMP;
				}
				if (bodyYawChange != 0) {
					fle.bodyYawO += bodyYawChange;
					fle.setBodyYaw(fle.getBodyYaw() + bodyYawChange);
					relRotation.rotationY(bodyYawChange);
				}
				fle.headYawO += newHeadYaw - oldHeadYaw;
				fle.setHeadYaw(newHeadYaw);

				float newHeadPitch = oldHeadPitch + pitch;
				if (newHeadPitch > Mth.HALF_PI) {
					if (!lockBodyRotate) {
						relRotation.rotateX(newHeadPitch - Mth.HALF_PI);
					}
					newHeadPitch = Mth.HALF_PI;
				} else if (newHeadPitch < -Mth.HALF_PI) {
					if (!lockBodyRotate) {
						relRotation.rotateX(newHeadPitch + Mth.HALF_PI);
					}
					newHeadPitch = -Mth.HALF_PI;
				}
				fle.headPitchO += newHeadPitch - oldHeadPitch;
				fle.setHeadPitch(newHeadPitch);
			}
			if (!lockBodyRotate) {
				relRotation.rotateZ(roll);
				if (lockHeadRotate && fle != null) {
					final Quaternionf headLocal = new Quaternionf().rotationYXZ(fle.getHeadYaw(), fle.getHeadPitch(), 0);
					relRotation.premul(headLocal);
					relRotation.mul(headLocal.conjugate());
				}
			}

			fe.setRotation(new Quaternionf(fe.getRotation()).mul(relRotation).normalize());
			fe.rotationO = new Quaternionf(fe.getLastRotation()).mul(relRotation).normalize();
		}
	}

	@Inject(method = "baseTick", at = @At("HEAD"))
	public void baseTick(final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		this.level().getProfiler().push("freeEntityTick");
		fe.tick();
		this.level().getProfiler().pop();
	}

	@Inject(
		method = "load",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
			shift = At.Shift.AFTER
		)
	)
	public void readAdditionalSaveData(final CompoundTag data, final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		if (data.contains("FreeRotationData", Tag.TAG_COMPOUND)) {
			fe.load(data.getCompound("FreeRotationData"));
		}
	}

	@Inject(
		method = "saveWithoutId",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
			shift = At.Shift.AFTER
		)
	)
	public void addAdditionalSaveData(final CompoundTag data, final CallbackInfoReturnable<CompoundTag> ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final CompoundTag frData = new CompoundTag();
		fe.save(frData);
		data.put("FreeRotationData", frData);
	}

	@Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
	public void moveRelative(final float power, final Vec3 relMove, final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		ci.cancel();

		final double lengthSqr = relMove.lengthSqr();
		if (lengthSqr < 1e-8) {
			return;
		}
		final Vector3d move = new Vector3d(relMove.x, relMove.y, relMove.z);
		if (lengthSqr > 1) {
			move.normalize();
		}
		final Quaternionfc rotation = fe.getRotation();
		final Vector3f angles = fe.getHitboxRotation().getEulerAnglesYXZ(new Vector3f());
		final Quaternionf moveRot = new Quaternionf().rotationY(angles.y);
		if (fe instanceof final FreeLivingEntity<?> fle) {
			float targetYaw = (float) Mth.atan2(-relMove.x, relMove.z);
			if (targetYaw < -Mth.PI * 5 / 8) {
				targetYaw += Mth.PI;
			} else if (targetYaw > Mth.PI * 5 / 8) {
				targetYaw -= Mth.PI;
			}
			targetYaw = Math.min(Math.max(targetYaw, -BODY_ROT_CLAMP), BODY_ROT_CLAMP);

			final float yaw = fle.getHeadYaw();
			moveRot.rotateY(yaw + fle.getBodyYaw());
			final float yawFix = (yaw - targetYaw) * 0.3f;
			if (Math.abs(yawFix) > 1e-6) {
				fle.setBodyYaw(fle.getBodyYaw() + yawFix);
				fle.setHeadYaw(yaw - yawFix);
				fle.setRotation(new Quaternionf(rotation).rotateY(yawFix));
			}
			if (!fle.isBodyRotationLocked()) {
				moveRot.rotateX(fle.getHeadPitch());
			}
		}
		moveRot.rotateX(angles.x).rotateZ(angles.z);

		move.mul(power);
		moveRot.transform(move);
		this.setDeltaMovement(this.getDeltaMovement().add(move.x, move.y, move.z));
	}

	@Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
	protected void moveTowardsClosestSpace(final double x, final double y, final double z, final CallbackInfo ci) {
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		ci.cancel();
		// TODO
	}

	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	public void move(final MoverType moverType, Vec3 movementVec, final CallbackInfo ci) {
		if (this.noPhysics) {
			return;
		}
		final FreeEntity<?> fe = this.freeEntity;
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		ci.cancel();

		this.wasOnFire = this.isOnFire();

		if (moverType == MoverType.PISTON) {
			movementVec = this.limitPistonMovement(movementVec);
			if (movementVec.equals(Vec3.ZERO)) {
				return;
			}
		}

		final Vector3d movement = new Vector3d(movementVec.x, movementVec.y, movementVec.z);
		if (this.stuckSpeedMultiplier.lengthSqr() > 1e-7) {
			movement.mul(this.stuckSpeedMultiplier.x, this.stuckSpeedMultiplier.y, this.stuckSpeedMultiplier.z);
			this.stuckSpeedMultiplier = Vec3.ZERO;
			this.setDeltaMovement(Vec3.ZERO);
		}
		fe.maybeBackOffFromEdge(movement, moverType);

		final Quaternionfc rotation = fe.getAbsRotation();
		final Vector3d movementWill = rotation.transform(new Vector3d(movement));

		movement.set(CollisionUtil.collide(fe, movement));
		movementVec = new Vec3(movement.x, movement.y, movement.z);

		final Vector3d movementActual = rotation.transform(new Vector3d(movement));

		final double movedDist = movement.length();
		if (movedDist > 1e-4) {
			// TODO: reset fallDistance
			final Vec3 pos = this.position();
			this.setPos(pos.x + movement.x, pos.y + movement.y, pos.z + movement.z);
		}

		final boolean collideX = Math.abs(movementWill.x - movementActual.x) > 1e-5;
		final boolean collideZ = Math.abs(movementWill.z - movementActual.z) > 1e-5;
		final boolean collideY = Math.abs(movementWill.y - movementActual.y) > 1e-5;
		this.horizontalCollision = collideX || collideZ;
		this.verticalCollision = collideY;
		this.verticalCollisionBelow = collideY && movementWill.y < 0;
		// TODO: detect minorHorizontalCollision
		this.minorHorizontalCollision = this.horizontalCollision && false;

		this.setOnGroundWithKnownMovement(this.verticalCollisionBelow, movementVec);
		final BlockPos groundPos = this.getOnPosLegacy();
		final BlockState groundState = this.level().getBlockState(groundPos);
		// this.checkFallDamage(movementActual.y, this.onGround(), groundState, groundPos);
		if (this.isRemoved()) {
			return;
		}
		if (collideX || collideZ) {
			final Vec3 dm = this.getDeltaMovement();
			final Vector3d mv = rotation.transform(new Vector3d(dm.x, dm.y, dm.z));
			if (collideX) {
				mv.x = 0;
			}
			if (collideZ) {
				mv.z = 0;
			}
			rotation.transformInverse(mv);
			this.setDeltaMovement(mv.x, mv.y, mv.z);
		}
		if (collideY) {
			// TODO: Block.updateEntityAfterFallOn
		}
		if (this.onGround()) {
			// TODO: Block.stepOn
		}
		final Entity.MovementEmission movementEmission = this.getMovementEmission();
		if (movementEmission.emitsAnything() && !this.isPassenger()) {
			final float scaledMoveDist = (float) (movedDist * 0.6);
			this.flyDist += scaledMoveDist;
			this.walkDist += scaledMoveDist;
			this.moveDist += scaledMoveDist;
			final BlockPos steppingPos = this.getOnPos();
			final BlockState stepping = this.level().getBlockState(steppingPos);
			if (!stepping.isAir() && this.moveDist > this.nextStep) {
				final boolean steppingOnGround = steppingPos.equals(groundPos);
				final Vec3 movementWillVec = new Vec3(movementWill.x, movementWill.y, movementWill.z);
				boolean stepped = this.vibrationAndSoundEffectsFromBlock(groundPos, groundState, movementEmission.emitsSounds(), steppingOnGround, movementWillVec);
				if (!steppingOnGround) {
					stepped |= this.vibrationAndSoundEffectsFromBlock(steppingPos, stepping, false, movementEmission.emitsEvents(), movementWillVec);
				}
				if (stepped) {
					this.nextStep = this.nextStep();
				} else if (this.isInWater()) {
					this.nextStep = this.nextStep();
					if (movementEmission.emitsSounds()) {
						this.waterSwimSound();
					}
					if (movementEmission.emitsEvents()) {
						this.gameEvent(GameEvent.SWIM);
					}
				}
			}
		}
		this.tryCheckInsideBlocks();
		this.setDeltaMovement(movementVec);
		if (
			this.level().getBlockStatesIfLoaded(this.getBoundingBox().deflate(1e-6))
				.noneMatch((state) -> state.is(BlockTags.FIRE) || state.is(Blocks.LAVA))
		) {
			if (this.getRemainingFireTicks() <= 0) {
				this.setRemainingFireTicks(-this.getFireImmuneTicks());
			}
			if (this.wasOnFire && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
				this.playEntityOnFireExtinguishedSound();
			}
		}
		if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
			this.setRemainingFireTicks(-this.getFireImmuneTicks());
		}
	}
}
