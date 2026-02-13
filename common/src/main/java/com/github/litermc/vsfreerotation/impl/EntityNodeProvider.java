package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.core.Node;
import com.github.litermc.vsfreerotation.core.NodeProvider;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;

public final class EntityNodeProvider implements NodeProvider {
	private final Int2ObjectMap<FreeEntity> nodes = new Int2ObjectOpenHashMap<>();

	@Override
	public Node getNode(final int id) {
		return this.nodes.get(id);
	}

	public void removeNode(final int id) {
		final FreeEntity fe = this.nodes.remove(id);
		if (fe != null) {
			((EntityAccessor) (fe.getEntity())).vsfr$setFreeEntity(null);
		}
	}

	public void clearNodes() {
		this.nodes.clear();
	}

	public FreeEntity getFreeEntity(final Entity entity) {
		return ((EntityAccessor) (entity)).vsfr$getFreeEntity();
	}

	public FreeEntity getOrCreateFreeEntity(final Entity entity) {
		final FreeEntity fe = ((EntityAccessor) (entity)).vsfr$getFreeEntity();
		if (fe != null) {
			return fe;
		}
		final FreeEntity fe = new FreeEntity(entity);
		this.putFreeEntity(fe);
		return fe;
	}

	public FreeEntity createFreeEntity(final int id, final Entity entity) {
		final FreeEntity fe = new FreeEntity(id, entity);
		this.putFreeEntity(fe);
		return fe;
	}

	private void putFreeEntity(final FreeEntity fe) {
		((EntityAccessor) (fe.getEntity())).vsfr$setFreeEntity(fe);
		this.nodes.put(fe.getId(), fe);
	}
}
