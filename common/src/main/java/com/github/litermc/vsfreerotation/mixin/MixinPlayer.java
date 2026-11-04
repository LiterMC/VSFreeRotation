package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
	protected MixinPlayer() {
		super(null, null);
	}

	@Shadow
	public abstract Abilities getAbilities();

	@ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
	public Vec3 travel$movement(Vec3 movement) {
		float yya = 0;
		if (this.level().isClientSide && ((Object) this) instanceof LocalPlayer player) {
			if (player.input.jumping) {
				yya += 1;
			}
			if (player.input.shiftKeyDown) {
				yya -= 1;
			}
			if (yya != 0) {
				movement = movement.add(0, yya, 0);
			}
		}
		return movement;
	}

	@WrapOperation(
		method = "travel",
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;abilities:Lnet/minecraft/world/entity/player/Abilities;")
		),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(DDD)V"
		)
	)
	public void travel$setDeltaMovement(final Player self, final double x, final double y, final double z, final Operation<Void> operation) {
		FreeEntity<?> fe = ((EntityAccessor) self).vsfr$getFreeEntity();
		if (fe != null && fe.canFreeRotate()) {
			return;
		}
		operation.call(self, x, y, z);
	}
}
