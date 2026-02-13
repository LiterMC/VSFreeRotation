package com.github.litermc.vsfreerotation.impl;

import com.github.litermc.vsfreerotation.core.Node;
import com.github.litermc.vsfreerotation.util.SerializeUtil;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

public class ShipNode extends Node {
	private final Ship ship;

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
	protected void onUpdateAnchor(final Node oldAnchor, final Node newAnchor, final Matrix4dc transform) {
		super.onUpdateAnchor(oldAnchor, newAnchor, transform);
	}

	public void tick() {
		this.posO = this.getPosition();
		this.rotataionO = this.getRotataion();
		this.anchorO = this.getAnchor();
	}
}
