package com.github.litermc.vsfreerotation.mixin;

import com.github.litermc.vsfreerotation.accessor.LevelAccessor;
import com.github.litermc.vsfreerotation.impl.EntityNodeProvider;

import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public abstract class MixinLevel implements LevelAccessor {
	@Unique
	private final EntityNodeProvider entityNodeProvider = new EntityNodeProvider();

	@Override
	public EntityNodeProvider vsfr$getEntityNodeProvider() {
		return this.entityNodeProvider;
	}
}
