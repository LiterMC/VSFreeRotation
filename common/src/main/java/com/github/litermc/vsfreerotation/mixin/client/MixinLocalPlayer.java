package com.github.litermc.vsfreerotation.mixin.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer implements EntityAccessor {
	@Shadow
	public Input input;
	@Shadow
	public float yBob;
	@Shadow
	public float xBob;
	@Shadow
	public float yBobO;
	@Shadow
	public float xBobO;

	protected MixinLocalPlayer() {
		super(null, null);
	}

	@Shadow
	protected abstract boolean isControlledCamera();

	// @ModifyExpressionValue(
	// 	method = {"tick", "sendPosition"},
	// 	at = @At(value = "NEW", target = "Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$Rot;")
	// )
	// public ServerboundMovePlayerPacket.Rot new$ServerboundMovePlayerPacket$Rot(final ServerboundMovePlayerPacket.Rot packet) {
	// 	final EntityRotationPacketAccessor packetAccessor = ((EntityRotationPacketAccessor)(packet));
	// 	packetAccessor.vsch$rotation().set(this.vsch$getBodyRotation());
	// 	packetAccessor.vsch$setHeadPitch(this.vsch$getHeadPitch());
	// 	packetAccessor.vsch$setHeadYaw(this.vsch$getHeadYaw());
	// 	return packet;
	// }

	// @ModifyExpressionValue(
	// 	method = "sendPosition",
	// 	at = @At(value = "NEW", target = "Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$PosRot;")
	// )
	// public ServerboundMovePlayerPacket.PosRot sendPosition$new$ServerboundMovePlayerPacket$PosRot(final ServerboundMovePlayerPacket.PosRot packet) {
	// 	final EntityRotationPacketAccessor packetAccessor = ((EntityRotationPacketAccessor)(packet));
	// 	packetAccessor.vsch$rotation().set(this.vsch$getBodyRotation());
	// 	packetAccessor.vsch$setHeadPitch(this.vsch$getHeadPitch());
	// 	packetAccessor.vsch$setHeadYaw(this.vsch$getHeadYaw());
	// 	return packet;
	// }

	@WrapOperation(
		method = "aiStep",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isShiftKeyDown()Z", ordinal = 0)
	)
	protected boolean aiStep$setCrouching$isShiftKeyDown(final LocalPlayer self, final Operation<Boolean> operation) {
		if (!operation.call(self)) {
			return false;
		}
		if (this.getAbilities().flying) {
			return true;
		}
		final FreeEntity<?> fe = this.vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate() || fe.isBodyRotationLocked()) {
			return true;
		}
		return this.onGround();
	}

	@Inject(method = "serverAiStep", at = @At("RETURN"))
	public void serverAiStep(final CallbackInfo ci) {
		if (this.getAbilities().flying || !this.isControlledCamera()) {
			return;
		}
		final FreeEntity<?> fe = this.vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		this.yya = fe.isBodyRotationLocked() ? 0 : ((this.input.jumping ? 1 : 0) + (this.input.shiftKeyDown ? -1 : 0));
		this.xBob = this.xBobO + Mth.wrapDegrees(this.getViewXRot(1) - this.xBobO) * 0.5f;
		this.yBob = this.yBobO + Mth.wrapDegrees(this.getViewYRot(1) - this.yBobO) * 0.5f;
	}

	@Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
	private void moveTowardsClosestSpace(final double x, final double z, final CallbackInfo ci) {
		final FreeEntity<?> fe = this.vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		ci.cancel();
	}

	@Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
	private void canStartSprinting(final CallbackInfoReturnable<Boolean> cir) {
		final FreeEntity<?> fe = this.vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate() || fe.isBodyRotationLocked()) {
			return;
		}
		cir.setReturnValue(false);
	}
}
