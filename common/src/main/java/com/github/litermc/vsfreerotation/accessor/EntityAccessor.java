package com.github.litermc.vsfreerotation.accessor;

import com.github.litermc.vsfreerotation.impl.FreeEntity;
import net.minecraft.world.phys.Vec3;

public interface EntityAccessor {
	FreeEntity vsfr$getFreeEntity();
	void vsfr$setFreeEntity(FreeEntity fe);

	void vsfr$raw$setPosRaw(double x, double y, double z);
	void vsfr$raw$setDeltaMovement(Vec3 deltaMovement);
}
