package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.core.NodeProvider;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;

public final class EntityNodeProvider implements NodeProvider {
	private final Int2ObjectMap<FreeEntity<?>> nodes = new Int2ObjectOpenHashMap<>();

	@Override
	public FreeEntity<?> getNode(final int id) {
		return this.nodes.get(id);
	}

	public void removeNode(final int id) {
		final FreeEntity<?> fe = this.nodes.remove(id);
		if (fe != null) {
			((EntityAccessor) fe.getEntity()).vsfr$setFreeEntity(null);
		}
	}

	public void clearNodes() {
		for (final FreeEntity<?> fe : this.nodes.values()) {
			((EntityAccessor) fe.getEntity()).vsfr$setFreeEntity(null);
		}
		this.nodes.clear();
	}

	public FreeEntity<?> getFreeEntity(final Entity entity) {
		return ((EntityAccessor) entity).vsfr$getFreeEntity();
	}

	public void putFreeEntity(final FreeEntity<?> fe) {
		this.nodes.put(fe.getId(), fe);
	}
}
