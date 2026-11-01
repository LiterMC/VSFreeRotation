package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.network.client.AddFreeEntityNodeClientMessage;
import com.github.litermc.vsfreerotation.network.client.MoveNodeClientMessage;
import com.github.litermc.vsfreerotation.platform.PlatformHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public class MixinServerEntity {
	@Shadow
	@Final
	private Entity entity;
	@Shadow
	@Final
	private Consumer<Packet<?>> broadcast;

	@Inject(method = "sendPairingData", at = @At("RETURN"))
	public void sendPairingData(
		final ServerPlayer player,
		final Consumer<Packet<ClientGamePacketListener>> packetBundle,
		final CallbackInfo ci
	) {
		final FreeEntity<?> fe = ((EntityAccessor) this.entity).vsfr$getFreeEntity();
		if (fe == null) {
			throw new IllegalStateException("FreeEntity should never be null for server side entity");
		}
		packetBundle.accept(PlatformHelper.get().createPacket(new AddFreeEntityNodeClientMessage(fe, this.entity.getId())));
	}

	@Inject(
		method = "sendChanges",
		slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/network/protocol/game/ClientboundTeleportEntityPacket;")),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity;sendDirtyEntityData()V")
	)
	public void sendChanges(final CallbackInfo ci) {
		final FreeEntity<?> fe = ((EntityAccessor) this.entity).vsfr$getFreeEntity();
		if (!fe.canFreeRotate()) {
			return;
		}
		this.broadcast.accept(PlatformHelper.get().createPacket(new MoveNodeClientMessage(fe.getId(), fe.getPosition(), fe.getRotation())));
	}
}
