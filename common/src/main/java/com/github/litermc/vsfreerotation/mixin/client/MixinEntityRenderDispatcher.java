package com.github.litermc.vsfreerotation.mixin.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
	@WrapMethod(method = "renderHitbox")
	private static void renderHitbox(
		final PoseStack poseStack,
		final VertexConsumer buffer,
		final Entity entity,
		final float partialTick,
		final Operation<Void> operation
	) {
		final FreeEntity<?> fe = ((EntityAccessor) entity).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			operation.call(poseStack, buffer, entity, partialTick);
			return;
		}
		poseStack.pushPose();
		poseStack.rotateAround(fe.getHitboxRotation(partialTick), 0, entity.getBbHeight() / 2, 0);
		operation.call(poseStack, buffer, entity, partialTick);
		poseStack.popPose();
	}

	@WrapOperation(
		method = "renderHitbox",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"
		)
	)
	private static Vec3 renderHitbox$getViewVector(final Entity entity, final float partialTick, final Operation<Vec3> operation) {
		Vec3 vec = operation.call(entity, partialTick);
		final FreeEntity<?> fe = ((EntityAccessor) entity).vsfr$getFreeEntity();
		if (fe != null && fe.canFreeRotate()) {
			Vector3d transformed = fe.getHitboxRotation(partialTick).invert().transform(new Vector3d(vec.x, vec.y, vec.z));
			vec = new Vec3(transformed.x, transformed.y, transformed.z);
		}
		return vec;
	}
}
