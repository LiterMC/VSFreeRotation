package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.core.Node;
import com.github.litermc.vsfreerotation.util.MathUtil;
import com.github.litermc.vsfreerotation.util.SerializeUtil;

import net.minecraft.world.entity.Entity;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
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

	/**
	 * Get the movement relative to anchor
	 * @return relative movement
	 */
	public Vector3dc getMovement() {
		return this.movement;
	}

	/**
	 * Set the movement relative to anchor
	 * @param movement relative movement
	 */
	public void setMovement(final Vector3dc movement) {
		this.movement = movement;
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
		this.rotataionO = this.getRotataion();
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
		this.setRotation(SerializeUtil.listToQuaternionf(data.getList("rotation")));
		this.rotationO = this.getRotation();
		this.setAnchor(data.getAnchor(data.getInt("anchor")));
		this.anchorO = this.getAnchor();
	}
}
