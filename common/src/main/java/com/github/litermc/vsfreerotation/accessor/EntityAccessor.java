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

	void vsfr$readTurnKeybinds(TurningContext context);

	public static class TurningContext {
		public float roll = 0;
		public boolean resetHead = false;
		public boolean lockBodyRotate = false;
		public boolean lockHeadRotate = false;
	}
}
