package com.github.litermc.vsfreerotation.mixin.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {
	@Shadow
	private Entity entity;
	@Shadow
	@Final
	private Quaternionf rotation;
	@Shadow
	private float eyeHeight;
	@Shadow
	private float eyeHeightOld;
	@Unique
	private float height;
	@Unique
	private float heightOld;

	@Inject(
		method = "setup",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0)
	)
	public void setup(
		final BlockGetter level,
		final Entity entity,
		final boolean isThirdPerson,
		final boolean isMirrored,
		final float partialTick,
		final CallbackInfo ci
	) {
		final FreeEntity<?> fe = ((EntityAccessor) entity).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		if (fe instanceof final FreeLivingEntity<?> fle) {
			this.rotation.set(fle.getHeadRotation(partialTick));
		} else {
			this.rotation.set(fe.getAbsRotation(partialTick));
		}
		if (isThirdPerson && isMirrored) {
			this.rotation.rotateY(Mth.PI);
		}
	}

	@WrapOperation(
		method = "setup",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", remap = false)
	)
	private void setPosition(
		final Camera self,
		double x,
		double y,
		double z,
		final Operation<Void> operation,
		@Local(argsOnly = true, ordinal = 0) final float partialTick
	) {
		final FreeEntity<?> fe = ((EntityAccessor) this.entity).vsfr$getFreeEntity();
		if (fe != null && fe.canFreeRotate()) {
			final Vector3d position = fe.getAbsPosition(partialTick);
			final double eyeHeight = Mth.lerp(partialTick, this.eyeHeightOld - this.heightOld / 2, this.eyeHeight - this.height / 2);
			if (eyeHeight != 0) {
				position.add(fe.getAbsRotation(partialTick).transform(new Vector3d(0, eyeHeight, 0)));
			}
			x = position.x;
			y = position.y;
			z = position.z;
		}
		operation.call(self, x, y, z);
	}

	@WrapOperation(
		method = "setRotation",
		at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;", remap = false)
	)
	private Quaternionf setRotation$rotationYXZ(
		final Quaternionf rotation,
		final float y, final float x, final float z,
		final Operation<Quaternionf> operation
	) {
		final FreeEntity<?> fe = ((EntityAccessor) this.entity).vsfr$getFreeEntity();
		if (fe != null && fe.canFreeRotate()) {
			return rotation;
		}
		return operation.call(rotation, y, x, z);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void tick(final CallbackInfo ci) {
		if (this.entity == null) {
			return;
		}
		this.heightOld = this.height;
		this.height += (this.entity.getBbHeight() - this.heightOld) * 0.5F;
	}
}
