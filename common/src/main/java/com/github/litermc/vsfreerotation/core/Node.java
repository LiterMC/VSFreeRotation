package com.github.litermc.vsfreerotation.core;

import com.github.litermc.vsfreerotation.util.MathUtil;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;

public class Node {
	private final int id;

	private Vector3dc pos = MathUtil.ZERO_VEC3D;
	private Quaternionfc rotation = MathUtil.ZERO_QUATF;
	private Node anchor = null;

	protected Vector3dc posO = MathUtil.ZERO_VEC3D;
	protected Quaternionfc rotationO = MathUtil.ZERO_QUATF;
	protected Node anchorO = null;

	public Node() {
		this(NodeManager.allocateId());
	}

	public Node(final int id) {
		this.id = id;
	}

	/**
	 * Get the unique ID of the node, must be synced between client and server manually.
	 * The ID should never be {@code 0}
	 * @return The node's unique ID
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Get the position relative to anchor.
	 * @return relative position
	 * @see setPosition
	 * @see getAnchor
	 */
	public Vector3dc getPosition() {
		return this.pos;
	}

	public Vector3dc getLastPosition() {
		return this.posO;
	}

	/**
	 * Get the position relative to anchor
	 * @param alpha The ratio of current tick's position to previous one
	 * @return relative position
	 */
	public Vector3d getPosition(final double alpha) {
		return this.getLastPosition().lerp(this.getPosition(), alpha, new Vector3d());
	}

	/**
	 * Set the position relative to anchor.
	 * @param pos relative position, must be immutable
	 * @see getPosition
	 * @see getAnchor
	 */
	public void setPosition(final Vector3dc pos) {
		this.pos = pos;
	}

	/**
	 * Get the transformed position (relative to world anchor)
	 * @return the absolute position
	 */
	public Vector3d getAbsPosition() {
		return this.getAnchorTransform().transformPosition(this.getPosition(), new Vector3d());
	}

	public Vector3d getLastAbsPosition() {
		return this.getLastAnchorTransform().transformPosition(this.getLastPosition(), new Vector3d());
	}

	/**
	 * Get the transformed position (relative to world anchor)
	 * @param alpha The ratio of current tick's position to previous one
	 * @return the absolute position
	 */
	public Vector3d getAbsPosition(final double alpha) {
		return this.getLastAbsPosition().lerp(this.getAbsPosition(), alpha);
	}

	/**
	 * Get the rotation relative to anchor.
	 * @return relative rotation
	 * @see setRotation
	 * @see getAnchor
	 */
	public Quaternionfc getRotation() {
		return this.rotation;
	}

	public Quaternionfc getLastRotation() {
		return this.rotationO;
	}

	/**
	 * Get the rotation relative to anchor
	 * @param alpha The ratio of current tick's rotation to previous one
	 * @return relative rotation
	 */
	public Quaternionf getRotation(final double alpha) {
		return this.getLastRotation().slerp(this.getRotation(), alpha, new Quaternionf());
	}

	/**
	 * Set the rotation relative to anchor.
	 * @param rotation relative rotation, must be immutable
	 * @see getRotation
	 * @see getAnchor
	 */
	public void setRotation(final Quaternionfc rotation) {
		this.rotation = rotation;
	}

	/**
	 * Get the transformed rotation (relative to world anchor)
	 * @return the absolute rotation
	 */
	public Quaternionf getAbsRotation() {
		return this.getAnchorTransform().getNormalizedRotation(new Quaternionf()).mul(this.getRotation());
	}

	public Quaternionf getLastAbsRotation() {
		return this.getLastAnchorTransform().getNormalizedRotation(new Quaternionf()).mul(this.getLastRotation());
	}

	/**
	 * Get the transformed rotation (relative to world anchor)
	 * @param alpha The ratio of current tick's position to previous one
	 * @return the absolute rotation
	 */
	public Quaternionf getAbsRotation(final double alpha) {
		return this.getLastAbsRotation().slerp(this.getAbsRotation(), alpha);
	}

	/**
	 * Get the anchor {@link Node}.
	 * @return the anchor node, {@code null} if this node is relative to the world
	 * @see setAnchor
	 * @see updateAnchor
	 */
	public Node getAnchor() {
		return this.anchor;
	}

	public Node getLastAnchor() {
		return this.anchorO;
	}

	/**
	 * Set the anchor {@link Node}.
	 * This method does not update relative position or rotation, which means absolute position & rotation may be changed.
	 * If you want to keep the node at original absolute position & rotation, use {@link Node#updateAnchor} instead.
	 * @param anchor the anchor node, {@code null} if this node should relative to the world
	 * @see getAnchor
	 */
	public void setAnchor(final Node anchor) {
		this.anchor = anchor;
	}

	/**
	 * Update the anchor {@link Node}.
	 * Absolute position and rotation will not change.
	 * @see getAnchor
	 * @see setAnchor
	 */
	public void updateAnchor(final Node anchor) {
		final Node oldAnchor = this.getAnchor();
		final Matrix4d oldTransform = this.getAnchorTransform();
		this.setAnchor(anchor);
		final Matrix4d newTransform = this.getAnchorTransform();
		final Matrix4dc transform = newTransform.invert().mul(oldTransform);
		this.onUpdateAnchor(oldAnchor, anchor, transform);
	}

	protected void onUpdateAnchor(final Node oldAnchor, final Node newAnchor, final Matrix4dc transform) {
		this.setPosition(transform.transformPosition(this.getPosition(), new Vector3d()));
		this.posO = transform.transformPosition(this.posO, new Vector3d());
		this.setRotation(transform.getNormalizedRotation(new Quaternionf()).mul(this.getRotation(), new Vector3d()));
		this.rotationO = transform.getNormalizedRotation(new Quaternionf()).mul(this.rotationO, new Vector3d());
	}

	/**
	 * Get the transform matrix that convert position & rotation relative to this node's anchor to global.
	 * @return The transform matrix
	 */
	public Matrix4d getAnchorTransform() {
		return this.anchor == null ? new Matrix4d() : this.anchor.getTransform();
	}

	public Matrix4d getLastAnchorTransform() {
		return this.anchorO == null ? new Matrix4d() : this.anchorO.getLastTransform();
	}

	/**
	 * Get the transform matrix that convert position & rotation relative to this node to global.
	 * @return The transform matrix
	 */
	public Matrix4d getTransform() {
		return this.getAnchorTransform().translate(this.pos).rotate(this.rotation);
	}

	public Matrix4d getLastTransform() {
		return this.getLastAnchorTransform().translate(this.posO).rotate(this.rotationO);
	}
}
