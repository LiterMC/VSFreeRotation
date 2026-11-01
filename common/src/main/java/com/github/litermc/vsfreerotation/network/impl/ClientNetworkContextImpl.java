package com.github.litermc.vsfreerotation.network.impl;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.accessor.LevelAccessor;
import com.github.litermc.vsfreerotation.impl.EntityNodeProvider;
import com.github.litermc.vsfreerotation.impl.FreeEntity;
import com.github.litermc.vsfreerotation.network.client.ClientNetworkContext;

import com.google.auto.service.AutoService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionfc;
import org.joml.Vector3dc;

import java.util.Objects;

@AutoService(ClientNetworkContext.class)
public class ClientNetworkContextImpl implements ClientNetworkContext {
	private ClientLevel getLevel() {
		return Minecraft.getInstance().level;
	}

	private EntityNodeProvider getEntityNodeProvider() {
		return ((LevelAccessor) this.getLevel()).vsfr$getEntityNodeProvider();
	}

	@Override
	public void addFreeEntity(int id, int entityId, CompoundTag data) {
		Entity entity = Objects.requireNonNull(this.getLevel().getEntity(entityId));
		FreeEntity<?> oldFe = ((EntityAccessor) entity).vsfr$getFreeEntity();
		if (oldFe != null && oldFe.getId() != id) {
			throw new IllegalStateException("Entity got two FreeEntity! " + entity);
		}
		FreeEntity<?> fe = FreeEntity.createClient(id, entity);
		((EntityAccessor) entity).vsfr$setFreeEntity(fe);
		this.getEntityNodeProvider().putFreeEntity(fe);
	}

	@Override
	public void moveNode(int id, Vector3dc position, Quaternionfc rotation) {
		FreeEntity<?> fe = this.getEntityNodeProvider().getNode(id);
		fe.setPosition(position);
		fe.setRotation(rotation);
	}

	@Override
	public void relocateNode(int id, int anchorId, Vector3dc pos, Quaternionfc rotation) {
		throw new UnsupportedOperationException("Unimplemented method 'relocateNode'");
	}
}
