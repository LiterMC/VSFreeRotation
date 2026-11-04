package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.accessor.LivingEntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;
import com.github.litermc.vsfreerotation.util.LevelUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements LivingEntityAccessor {
	@Unique
	private static final EntityDataAccessor<Boolean> HEAD_ROTATION_LOCKED = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

	protected MixinLivingEntity() {
		super(null, null);
	}

	@Inject(method = "defineSynchedData", at = @At("HEAD"))
	public void defineSynchedData(final CallbackInfo ci) {
		this.entityData.define(HEAD_ROTATION_LOCKED, false);
	}

	@Shadow
	protected abstract float getFlyingSpeed();

	@Shadow
	protected abstract SoundEvent getFallDamageSound(int height);

	@Shadow
	protected abstract float getFrictionInfluencedSpeed(float friction);

	@Shadow
	protected abstract Vec3 handleOnClimbable(Vec3 deltaMovement);

	@Override
	public boolean vsfr$isHeadRotationLocked() {
		return this.entityData.get(HEAD_ROTATION_LOCKED);
	}

	@Override
	public void vsfr$setHeadRotationLock(boolean lock) {
		this.entityData.set(HEAD_ROTATION_LOCKED, lock);
	}

	@Inject(method = "isFallFlying", at = @At("HEAD"), cancellable = true)
	public void isFallFlying(final CallbackInfoReturnable<Boolean> cir) {
		final FreeEntity<?> fe = ((EntityAccessor) this).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate() || fe.isBodyRotationLocked()) {
			return;
		}
		cir.setReturnValue(false);
	}

	@Inject(method = "handleOnClimbable", at = @At("HEAD"), cancellable = true)
	protected void handleOnClimbable(Vec3 movement, final CallbackInfoReturnable<Vec3> cir) {
		final FreeEntity<?> fe = ((EntityAccessor) this).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		// TODO: climbable
		cir.setReturnValue(movement);
	}

	@Inject(method = "handleRelativeFrictionAndCalculateMovement", at = @At("HEAD"), cancellable = true)
	public void handleRelativeFrictionAndCalculateMovement(final Vec3 relMove, final float friction, final CallbackInfoReturnable<Vec3> cir) {
		final FreeLivingEntity<?> fe = (FreeLivingEntity<?>) ((EntityAccessor) this).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}

		this.moveRelative(this.getFrictionInfluencedSpeed(friction), relMove);

		double airFriction = 0.91;
		if (LevelUtil.isSpace(this.level())) {
			airFriction = 1;
			final double inflate = 1.0 / 16;
			final BlockPos pos = null; // this.getSupportiveBlock(inflate);
			if (pos != null) {
				final Vec3 movement = this.getDeltaMovement();
				final double blockFriction = 0.1; // TODO: get actualy block friction
				final double inertia = 1 - blockFriction;
				final double MIN_MOVEMENT = 0.001;
				double x = movement.x - inertia;
				if (Math.abs(x) < MIN_MOVEMENT) {
					x = 0;
				}
				double y = movement.y - inertia;
				if (Math.abs(y) < MIN_MOVEMENT) {
					y = 0;
				}
				double z = movement.z - inertia;
				if (Math.abs(z) < MIN_MOVEMENT) {
					z = 0;
				}
				this.setDeltaMovement(x, y, z);
			}
		}

		this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
		final double movementBefore = this.getDeltaMovement().length();
		this.move(MoverType.SELF, this.getDeltaMovement());

		// Fall damage check
		if (!this.level().isClientSide && !this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
			final double movementAfter = this.getDeltaMovement().length();
			final double movementDiff = movementBefore - movementAfter;
			final int fallDamage = (int) (movementDiff * 10 - 6);
			if (fallDamage > 0) {
				this.playSound(this.getFallDamageSound(fallDamage), 1, 1);
				this.hurt(this.damageSources().flyIntoWall(), fallDamage);
			}
		}

		cir.setReturnValue(this.getDeltaMovement().scale(airFriction));
	}

	@Inject(method = "shouldDiscardFriction", at = @At("RETURN"), cancellable = true)
	public void shouldDiscardFriction(final CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			return;
		}
		final FreeEntity<?> fe = ((EntityAccessor) this).vsfr$getFreeEntity();
		if (fe != null && fe.canFreeRotate()) {
			cir.setReturnValue(false);
		}
	}

	@WrapOperation(
		method = "travel",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isNoGravity()Z")
	)
	public boolean travel$isNoGravity(final LivingEntity self, final Operation<Boolean> operation) {
		if (operation.call(self)) {
			return true;
		}
		FreeEntity<?> fe = ((EntityAccessor) this).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return false;
		}
		return ((Object) this) instanceof Player player && player.getAbilities().flying;
	}
}
