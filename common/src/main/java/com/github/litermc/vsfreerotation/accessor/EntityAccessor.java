package com.github.litermc.vsfreerotation.accessor;

import com.github.litermc.vsfreerotation.impl.FreeEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface EntityAccessor {
	@Nullable
	FreeEntity<?> vsfr$getFreeEntity();
	void vsfr$setFreeEntity(FreeEntity<?> fe);

	boolean vsfr$isFreeRotationEnabled();
	void vsfr$setFreeRotationFlag(boolean flag);
	boolean vsfr$isBodyRotationLocked();
	void vsfr$setBodyRotationLock(boolean lock);

	void vsfr$raw$setPosRaw(double x, double y, double z);
	void vsfr$raw$setDeltaMovement(Vec3 deltaMovement);
}
