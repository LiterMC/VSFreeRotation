package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.accessor.LevelAccessor;
import com.github.litermc.vsfreerotation.core.FreeEntity;
import com.github.litermc.vsfreerotation.impl.EntityNodeProvider;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

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

	@Shadow
	public abstract Level level();

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
		final EntityNodeProvider provider = ((LevelAccessor) (this.level())).vsfr$getEntityNodeProvider();
		provider.removeNode(fe.getId());
	}
}
