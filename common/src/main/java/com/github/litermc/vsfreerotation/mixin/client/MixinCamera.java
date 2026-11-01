package com.github.litermc.vsfreerotation.mixin.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

import org.joml.Quaternionf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
		method = "setRotation",
		at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;", remap = false)
	)
	private Quaternionf setRotation$rotationYXZ(
		final Quaternionf rotation,
		final float y, final float x, final float z,
		final Operation<Quaternionf> operation
	) {
		final FreeEntity<?> fe = ((EntityAccessor) entity).vsfr$getFreeEntity();
		if (fe != null && fe.canFreeRotate()) {
			return rotation;
		}
		return operation.call(rotation, y, x, z);
	}
}
