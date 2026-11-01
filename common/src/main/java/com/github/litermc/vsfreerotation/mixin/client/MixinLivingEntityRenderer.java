package com.github.litermc.vsfreerotation.mixin.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;

import org.joml.Quaternionf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V"
		)
	)
	protected void render$setupRotations(
		final LivingEntityRenderer<?, ?> self,
		final LivingEntity entity,
		final PoseStack poseStack,
		final float bob,
		final float yaw,
		final float partialTick,
		final Operation<Void> operation
	) {
		final FreeEntity<?> fe = ((EntityAccessor) entity).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			operation.call(self, entity, poseStack, bob, yaw, partialTick);
			return;
		}

		final Quaternionf rotation = fe.getAbsRotation(partialTick);

		final EntityDimensions dim = entity.getDimensions(entity.getPose());
		poseStack.rotateAround(rotation, 0, dim.height / 2, 0);

		operation.call(self, entity, poseStack, bob, 0f, partialTick);
	}

	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"
		)
	)
	public void render$setupAnim(
		final EntityModel<?> model,
		final Entity entity,
		final float limbSwing,
		final float limbSwingAmount,
		final float age,
		float headYaw,
		float headPitch,
		final Operation<Void> operation,
		@Local(argsOnly = true, ordinal = 1) final float partialTick
	) {
		if (((EntityAccessor) entity).vsfr$getFreeEntity() instanceof final FreeLivingEntity<?> fe && fe.canFreeRotate()) {
			headYaw = -fe.getHeadYaw(partialTick) * Mth.RAD_TO_DEG;
			headPitch = fe.getHeadPitch(partialTick) * Mth.RAD_TO_DEG;
		}
		operation.call(model, entity, limbSwing, limbSwingAmount, age, headYaw, headPitch);
	}
}
