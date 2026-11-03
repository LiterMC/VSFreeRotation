package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.accessor.LivingEntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;
import com.github.litermc.vsfreerotation.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.joml.Quaternionf;
import org.joml.Vector3d;
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

	@Inject(method = "handleRelativeFrictionAndCalculateMovement", at = @At("HEAD"), cancellable = true)
	public void handleRelativeFrictionAndCalculateMovement(final Vec3 relMove, final float friction, final CallbackInfoReturnable<Vec3> cir) {
		final FreeLivingEntity<?> fe = (FreeLivingEntity<?>) ((EntityAccessor) this).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}

		this.moveRelative(this.getFrictionInfluencedSpeed(friction), relMove);
		final double movementBefore = this.getDeltaMovement().length();
		// this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
		this.setDeltaMovement(this.getDeltaMovement());
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

		cir.setReturnValue(this.getDeltaMovement());
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
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;shouldDiscardFriction()Z")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V", ordinal = 0)
	)
	public void travel(final LivingEntity self, double x, double y, double z, final Operation<Void> operation) {
		final FreeEntity<?> fe = ((EntityAccessor) self).vsfr$getFreeEntity();
		if (fe != null && fe.canFreeRotate()) {
			// Friction in space
			if (LevelUtil.isSpace(self.level())) {
				final Vec3 movement = self.getDeltaMovement();
				x = movement.x;
				y = movement.y;
				z = movement.z;
				final double inflate = 1.0 / 16;
				final BlockPos pos = null; // self.getSupportiveBlock(inflate);
				if (pos != null) {
					// TODO: maybe check the block's friction
					final double friction = 0.1;
					final double inertia = 1 - friction;
					x *= inertia;
					if (Math.abs(x) < 1e-6) {
						x = 0;
					}
					y *= inertia;
					if (Math.abs(y) < 1e-6) {
						y = 0;
					}
					z *= inertia;
					if (Math.abs(z) < 1e-6) {
						z = 0;
					}
				}
			}
		}
		operation.call(self, x, y, z);
	}
}
