package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.accessor.EntityAccessor;
import com.github.litermc.vsfreerotation.core.Node;
import com.github.litermc.vsfreerotation.core.NodeManager;
import com.github.litermc.vsfreerotation.util.MathUtil;
import com.github.litermc.vsfreerotation.util.SerializeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class FreeEntity extends Node {
	private final Entity entity;
	private Vector3dc movement = MathUtil.ZERO_VEC3D;

	public FreeEntity(final Entity entity) {
		super();
		this.entity = entity;
	}

	public FreeEntity(final int id, final Entity entity) {
		super(id);
		this.entity = entity;
	}

	public Entity getEntity() {
		return this.entity;
	}

	@Override
	public void setPosition(final Vector3dc pos) {
		super.setPosition(pos);
		final Vector3d absPos = this.getAbsPosition();
		((EntityAccessor) this.entity).vsfr$raw$setPosRaw(absPos.x, absPos.y, absPos.z);
	}

	/**
	 * Get the movement relative to anchor in blocks/tick
	 * @return relative movement
	 */
	public Vector3dc getMovement() {
		return this.movement;
	}

	/**
	 * Set the movement relative to anchor in blocks/tick
	 * @param movement relative movement
	 */
	public void setMovement(final Vector3dc movement) {
		this.movement = movement;
		final Vector3d absMovement = this.getAnchorTransform().transformDirection(movement, new Vector3d());
		((EntityAccessor) this.entity).vsfr$raw$setDeltaMovement(new Vec3(absMovement.x, absMovement.y, absMovement.z));
	}

	/**
	 * Add the movement
	 * @param movement movement delta
	 */
	public final void addMovement(final Vector3dc movement) {
		this.setMovement(this.getMovement().add(movement, new Vector3d()));
	}

	@Override
	protected void onUpdateAnchor(final Node oldAnchor, final Node newAnchor, final Matrix4dc transform) {
		super.onUpdateAnchor(oldAnchor, newAnchor, transform);
		final Vector3dc movement = this.getMovement();
		if (movement.equals(MathUtil.ZERO_VEC3D)) {
			this.setMovement(transform.transformDirection(movement, new Vector3d()));
		}
	}

	public void tick() {
		this.posO = this.getPosition();
		this.rotationO = this.getRotation();
		this.anchorO = this.getAnchor();
	}

	public void save(final CompoundTag data) {
		SerializeUtil.putVector3d(data, this.getPosition());
		data.put("rotation", SerializeUtil.quaternionfToList(this.getRotation()));
		final Node anchor = this.getAnchor();
		if (anchor != null) {
			data.putInt("anchor", anchor.getId());
		}
	}

	public void load(final CompoundTag data) {
		this.setPosition(SerializeUtil.getVector3d(data));
		this.posO = this.getPosition();
		this.setRotation(SerializeUtil.listToQuaternionf(data.getList("rotation", Tag.TAG_FLOAT)));
		this.rotationO = this.getRotation();
		this.setAnchor(NodeManager.getNode(data.getInt("anchor")));
		this.anchorO = this.getAnchor();
	}
}
