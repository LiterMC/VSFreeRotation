package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "isPlayerCollidingWithAnythingNew", at = @At("HEAD"), cancellable = true)
	private void isPlayerCollidingWithAnythingNew(
		final LevelReader level,
		final AABB box,
		final double x,
		final double y,
		final double z,
		final CallbackInfoReturnable<Boolean> cir
	) {
		FreeEntity<?> fe = ((EntityAccessor) this.player).vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		cir.setReturnValue(false);
	}
}
