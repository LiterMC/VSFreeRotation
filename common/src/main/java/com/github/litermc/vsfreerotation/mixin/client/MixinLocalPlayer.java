package com.github.litermc.vsfreerotation.mixin.client;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.client.key.KeyBindings;
import com.github.litermc.vsfreerotation.client.platform.ClientPlatformHelper;
import com.github.litermc.vsfreerotation.config.Config;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.impl.FreeLivingEntity;
import com.github.litermc.vsfreerotation.network.server.PlayerMoveServerMessage;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.checkerframework.common.aliasing.qual.Unique;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer implements EntityAccessor {
	@Shadow
	@Final
	public ClientPacketListener connection;
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

	@Unique
	private long lastRollTime;

	protected MixinLocalPlayer() {
		super(null, null);
	}

	@Shadow
	protected abstract boolean isControlledCamera();

	@Inject(method = "sendPosition", at = @At("RETURN"))
	public void sendPosition(final CallbackInfo ci) {
		if (!this.isControlledCamera()) {
			return;
		}
		final FreeLivingEntity<?> fe = (FreeLivingEntity<?>) this.vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return;
		}
		this.connection.send(ClientPlatformHelper.get().createPacket(new PlayerMoveServerMessage(
			fe.getPosition(),
			fe.getRotation(),
			fe.getHeadPitch(),
			fe.getHeadYaw()
		)));
	}

	@Override
	public void vsfr$readTurnKeybinds(final TurningContext context) {
		final long now = System.nanoTime();
		if (KeyBindings.UNLOCK_HEAD_ROTATION.consumeDoubleClick()) {
			context.resetHead = true;
			this.lastRollTime = now;
			return;
		}
		if (!context.lockBodyRotate) {
			context.lockHeadRotate = !KeyBindings.UNLOCK_HEAD_ROTATION.isDown();
			int rollDir = 0;
			if (KeyBindings.ROLL_CLOCKWISE.isDown()) {
				rollDir++;
			}
			if (KeyBindings.ROLL_COUNTER_CLOCKWISE.isDown()) {
				rollDir--;
			}
			context.roll = rollDir * (float) Config.playerRollSpeed * Math.min((float) ((now - this.lastRollTime) / 1.0e9), 0.1f) * Mth.DEG_TO_RAD;
		}
		this.lastRollTime = now;
	}

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

	@WrapOperation(
		method = "aiStep",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isFallFlying()Z")
		),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isControlledCamera()Z"
		)
	)
	public boolean aiStep$creativeFly(final LocalPlayer self, final Operation<Boolean> operation) {
		if (!operation.call(self)) {
			return false;
		}
		final FreeEntity<?> fe = this.vsfr$getFreeEntity();
		if (fe == null || !fe.canFreeRotate()) {
			return true;
		}
		return false;
	}

	@Inject(method = "serverAiStep", at = @At("RETURN"))
	public void serverAiStep(final CallbackInfo ci) {
		if (!this.isControlledCamera()) {
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
