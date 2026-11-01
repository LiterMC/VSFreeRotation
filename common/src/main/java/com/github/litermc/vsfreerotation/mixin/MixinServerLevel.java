package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.network.client.AddFreeEntityNodeClientMessage;
import com.github.litermc.vsfreerotation.platform.PlatformHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends MixinLevel {
	@Inject(method = "addPlayer", at = @At("RETURN"))
	private void addPlayer(final ServerPlayer player, final CallbackInfo ci) {
		final FreeEntity<?> fe = ((EntityAccessor) player).vsfr$getFreeEntity();
		this.vsfr$getEntityNodeProvider().putFreeEntity(fe);
		player.connection.send(PlatformHelper.get().createPacket(new AddFreeEntityNodeClientMessage(fe, player.getId())));
	}

	@Inject(method = "addEntity", at = @At("RETURN"))
	private void addEntity(final Entity entity, final CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			return;
		}
		this.vsfr$getEntityNodeProvider().putFreeEntity(((EntityAccessor) entity).vsfr$getFreeEntity());
	}
}
