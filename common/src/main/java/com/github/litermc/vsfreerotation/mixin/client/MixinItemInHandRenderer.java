package com.github.litermc.vsfreerotation.mixin.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {
	@WrapOperation(
		method = "renderHandsWithItems",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"
		)
	)
	public float armBob$getViewXRot(final LocalPlayer player, final float partialTick, final Operation<Float> operation) {
		final FreeLivingEntity<?> fle = (FreeLivingEntity<?>) ((EntityAccessor) player).vsfr$getFreeEntity();
		if (fle == null || !fle.canFreeRotate()) {
			return operation.call(player, partialTick);
		}
		return fle.getHeadPitch(partialTick) * Mth.RAD_TO_DEG;
	}

	@WrapOperation(
		method = "renderHandsWithItems",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"
		)
	)
	public float armBob$getViewYRot(final LocalPlayer player, final float partialTick, final Operation<Float> operation) {
		final FreeLivingEntity<?> fle = (FreeLivingEntity<?>) ((EntityAccessor) player).vsfr$getFreeEntity();
		if (fle == null || !fle.canFreeRotate()) {
			return operation.call(player, partialTick);
		}
		return (fle.getHeadYaw(partialTick) + fle.getBodyYaw(partialTick)) * Mth.RAD_TO_DEG;
	}

	@ModifyArg(
		method = "renderHandsWithItems",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"
		),
		index = 0
	)
	public float armBob$angleWarp(final float angle) {
		return Mth.wrapDegrees(angle * 10) / 10;
	}
}