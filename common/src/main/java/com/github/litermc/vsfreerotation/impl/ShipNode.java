package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.core.Node;
import org.joml.Matrix4d;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

public class ShipNode extends Node {
	private final Ship ship;

	private final Matrix4d transformO = null;

	public ShipNode(final Ship ship) {
		super();
		this.ship = ship;
	}

	public ShipNode(final int id, final Ship ship) {
		super(id);
		this.ship = ship;
	}

	public Ship getShip() {
		return this.ship;
	}

	@Override
	public Vector3dc getPosition() {
		return new Vector3d();
	}

	@Override
	public Quaternionfc getRotation() {
		return new Quaternionf(this.ship.getTransform().getRotation());
	}

	@Override
	public void setAnchor(final Node anchor) {
		if (anchor != null) {
			throw new UnsupportedOperationException("Cannot set non-null anchor for ShipNode");
		}
	}

	@Override
	public Matrix4d getTransform() {
		return new Matrix4d(this.ship.getShipToWorld());
	}

	@Override
	public Matrix4d getLastTransform() {
		return this.transformO != null ? new Matrix4d(this.transformO) : this.getTransform();
	}

	public void tick() {
		this.posO = this.getPosition();
		this.rotationO = this.getRotation();
		this.anchorO = this.getAnchor();
	}
}
