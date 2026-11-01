package com.github.litermc.vsfreerotation.mixin.client;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {
	@ModifyArg(
		method = "renderHandsWithItems",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"
		),
		index = 0
	)
	public float bobAngleWarp(final float angle) {
		return Mth.wrapDegrees(angle * 10) / 10;
	}
}