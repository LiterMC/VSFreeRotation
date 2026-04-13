package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.accessor.LevelAccessor;
import com.github.litermc.vsfreerotation.impl.EntityNodeProvider;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements EntityAccessor {
	@Unique
	private FreeEntity freeEntity = null;

	@Unique
	private boolean setPosRawFlag = false;
	@Unique
	private boolean setDeltaMovementFlag = false;

	@Shadow
	public abstract Level level();

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(final EntityType<?> type, final Level level, final CallbackInfo ci) {
		if (level instanceof ServerLevel serverLevel) {
			this.freeEntity = new FreeEntity((Entity) (Object) this);
			((LevelAccessor) serverLevel).vsfr$getEntityNodeProvider().putFreeEntity(this.freeEntity);
		}
	}

	@Override
	public FreeEntity vsfr$getFreeEntity() {
		return this.freeEntity;
	}

	@Override
	public void vsfr$setFreeEntity(final FreeEntity fe) {
		this.freeEntity = fe;
	}

	@Inject(method = "setRemoved", at = @At("RETURN"))
	public void setRemoved(final Entity.RemovalReason reason, final CallbackInfo ci) {
		final FreeEntity fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		this.freeEntity = null;
		final EntityNodeProvider provider = ((LevelAccessor) this.level()).vsfr$getEntityNodeProvider();
		provider.removeNode(fe.getId());
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	public void setLevel(final Level newLevel, final CallbackInfo ci) {
		final FreeEntity fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final Level oldLevel = this.level();
		if (oldLevel == newLevel) {
			return;
		}
		final EntityNodeProvider oldProvider = ((LevelAccessor) oldLevel).vsfr$getEntityNodeProvider();
		oldProvider.removeNode(fe.getId());
		final EntityNodeProvider newProvider = ((LevelAccessor) newLevel).vsfr$getEntityNodeProvider();
		newProvider.putFreeEntity(fe);
	}

	@Shadow
	public abstract void setPosRaw(double x, double y, double z);

	@Override
	public void vsfr$raw$setPosRaw(final double x, final double y, final double z) {
		this.setPosRawFlag = true;
		try {
			this.setPosRaw(x, y, z);
		} finally {
			this.setPosRawFlag = false;
		}
	}

	@Inject(method = "setPosRaw", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;position:Lnet/minecraft/world/phys/Vec3;", opcode = Opcodes.GETFIELD))
	public void setPosRaw(final double x, final double y, final double z, final CallbackInfo ci) {
		if (this.setPosRawFlag) {
			return;
		}
		final FreeEntity fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final Matrix4d anchor = fe.getAnchorTransform().invert();
		fe.setPosition(anchor.transformPosition(x, y, z, new Vector3d()));
	}

	@Shadow
	public abstract void setDeltaMovement(Vec3 movement);

	@Override
	public void vsfr$raw$setDeltaMovement(final Vec3 movement) {
		this.setDeltaMovementFlag = true;
		try {
			this.setDeltaMovement(movement);
		} finally {
			this.setDeltaMovementFlag = false;
		}
	}

	@Inject(method = "setDeltaMovement", at = @At("HEAD"))
	public void setDeltaMovement(final Vec3 movement, final CallbackInfo ci) {
		if (this.setDeltaMovementFlag) {
			return;
		}
		final FreeEntity fe = this.freeEntity;
		if (fe == null) {
			return;
		}
		final Matrix4d anchor = fe.getAnchorTransform().invert();
		fe.setMovement(anchor.transformDirection(movement.x, movement.y, movement.z, new Vector3d()));
	}
}
